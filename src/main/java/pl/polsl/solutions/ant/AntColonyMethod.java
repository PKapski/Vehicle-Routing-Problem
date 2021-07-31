package pl.polsl.solutions.ant;

import pl.polsl.model.Distance;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionResults;
import pl.polsl.solutions.SolutionMethodStrategy;
import pl.polsl.solutions.VRPSolutionMethod;
import pl.polsl.solutions.greedy.GreedyMethod;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.Math.pow;

public class AntColonyMethod extends VRPSolutionMethod implements SolutionMethodStrategy {

    private static final int MAX_ITERATIONS = 1000;
    private static final int NUMBER_OF_ANTS = 50;
    private static final int PHEROMONE_IMPORTANCE = 1;
    private static final int PHEROMONE_CONSTANT = 1;
    private static final int DISTANCE_IMPORTANCE = 4;
    private static final double PHEROMONE_EVAPORATION = 0.8;
    private double[][] pheromones;
    double[][] currentIterationPheromones;

    @Override
    public SolutionResults getSolution(List<Node> nodes, Distance[][] distances, int numOfVehicles, int vehicleCapacity, LocalTime startingTime) {
        SolutionResults solution = new GreedyMethod().getSolution(nodes, distances, numOfVehicles, vehicleCapacity, startingTime);
        initializeVariables(nodes, distances, startingTime, NUMBER_OF_ANTS, solution.getTotalSolutionTime());

        int vehiclesCount = solution.getRoutesMap().size();
        int iterationCount = 0;

        while (iterationCount != MAX_ITERATIONS) {
            clearCurrentIterationPheromones();
            for (int i = 0; i < NUMBER_OF_ANTS; i++) {
                Ant ant = new Ant(vehiclesCount, vehicleCapacity);
                for (int j = 0; j < vehiclesCount; j++) {
                    int currentNodeId = 0;
                    do {
                        currentNodeId = getNextNodeId(distances[currentNodeId], ant.getCurrentFreeLoad(), currentNodeId);
                        ant.addNodeToCurrentRoute(nodes.get(currentNodeId));
                        nodes.get(currentNodeId).setVisited(true);
                    } while (currentNodeId != 0);
                    ant.setNextVehicle();
                }

                double antSolutionTime = calculateSolutionTime(ant.getRoutesMap());
                if (antSolutionTime < solution.getTotalSolutionTime()) {
                    solution.copyRoutesMap(ant.getRoutesMap());
                    solution.setTotalSolutionTime(antSolutionTime);
                }
                saveCurrentAntPheromones(ant, antSolutionTime);
                unvisitAllNodes(nodes);
            }

            updatePheromones();
            iterationCount++;
        }
        solution.calculateFinalSolutionValues(nodes, distances, startingTime);
        return solution;
    }

    private void clearCurrentIterationPheromones() {
        for (double[] arr : currentIterationPheromones) {
            Arrays.fill(arr, 0);
        }
    }

    private void updatePheromones() {
        for (int i = 0; i < pheromones.length; i++) {
            for (int j = 0; j < pheromones.length; j++) {
                pheromones[i][j] = (1 - PHEROMONE_EVAPORATION) * pheromones[i][j] + currentIterationPheromones[i][j];
            }
        }
    }

    private void saveCurrentAntPheromones(Ant ant, double antSolutionTime) {
        double pheromoneValue = PHEROMONE_CONSTANT / antSolutionTime;
        for (ArrayList<Integer> list : ant.getRoutesMap().values()) {
            for (int i = 0; i < list.size() - 1; i++) {
                currentIterationPheromones[list.get(i)][list.get(i + 1)] += pheromoneValue;
                currentIterationPheromones[list.get(i + 1)][list.get(i)] += pheromoneValue;

            }
        }
    }

    private void unvisitAllNodes(List<Node> nodes) {
        nodes.forEach(x -> x.setVisited(false));
    }

    public int getNextNodeId(Distance[] distances, double currentFreeLoad, int currentNodeId) {
        double[] moveProbabilities = new double[distances.length];
        double probabilitiesSum = 0;
        for (int i = 0; i < distances.length; i++) {
            if (canVisitNode(i, currentFreeLoad)) {
                double probabilityNominator = pow(pheromones[currentNodeId][i], PHEROMONE_IMPORTANCE) * pow(1.0 / distances[i].getTime(), DISTANCE_IMPORTANCE);
                probabilitiesSum += probabilityNominator;
                moveProbabilities[i] = probabilityNominator;
            } else {
                moveProbabilities[i] = 0;
            }
        }

        if (probabilitiesSum == 0) {
            return 0;
        }

        for (int i = 1; i < moveProbabilities.length; i++) {
            moveProbabilities[i] /= probabilitiesSum;
        }

        int nextNodeId = 0;
        for (double rand = Math.random(); nextNodeId < moveProbabilities.length; nextNodeId++) {
            rand -= moveProbabilities[nextNodeId];
            if (rand <= 0.0) break;
        }
        return nextNodeId;
    }

    protected boolean canVisitNode(int i, double currentFreeLoad) {
        return !nodes.get(i).isVisited() && currentFreeLoad >= nodes.get(i).getDemand() && i!=0;
    }

    private double calculateSolutionTime(Map<Integer, ArrayList<Integer>> routesMap) {
        double totalSolutionTime = 0;
        for (int i = 0; i < routesMap.size(); i++) {
            LocalTime localTime = startingTime;
            ArrayList<Integer> route = routesMap.get(i);
            for (int j = 0; j < route.size() - 1; j++) {
                Distance distance = distances[route.get(j)][route.get(j + 1)];
                totalSolutionTime += distance.getTime();
                localTime = localTime.plusHours(distance.getTime());
                Node nextNode = nodes.get(route.get(j + 1));
                if (localTime.isAfter(nextNode.getAvailableTo()) || localTime.isBefore(nextNode.getAvailableFrom())) {
                    long currentWaitingTime = Duration.between(localTime, nextNode.getAvailableFrom()).toHours();
                    currentWaitingTime = currentWaitingTime > 0 ? currentWaitingTime : currentWaitingTime + 24;
                    totalSolutionTime += currentWaitingTime;
                }
                totalSolutionTime += nextNode.getServiceTime();
                localTime = localTime.plusHours(nextNode.getServiceTime());
            }
        }
        return totalSolutionTime;
    }

    public void initializeVariables(List<Node> nodes, Distance[][] distances, LocalTime startingTime, int numberOfAnts, double totalSolutionTime) {
        super.initializeVariables(nodes, distances, startingTime);
        nodes.forEach(x -> x.setVisited(false));
        int nodesCount = nodes.size();
        double initialPheromoneValue = numberOfAnts / totalSolutionTime;
        pheromones = new double[nodesCount][nodesCount];
        for (int i = 0; i < nodesCount; i++) {
            for (int j = 0; j < nodesCount; j++) {
                pheromones[i][j] = initialPheromoneValue;
            }
        }
        currentIterationPheromones = new double[nodesCount][nodesCount];
    }
}
