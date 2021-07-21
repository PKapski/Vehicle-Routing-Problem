package pl.polsl.solutions.simulatedannealing;

import pl.polsl.model.Distance;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionResults;
import pl.polsl.model.Vehicle;
import pl.polsl.solutions.SolutionMethodStrategy;
import pl.polsl.solutions.greedy.GreedyMethod;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimulatedAnnealingMethod implements SolutionMethodStrategy {

    private static final int MAX_ITERATIONS = 10000;
    private static final double coolingFactor = 0.98; //range: <0.8,0.99>
    private double currentTemperature = 1000.0;
    private Distance[][] distances;
    private List<Node> nodes;
    private LocalTime startingTime;

    @Override
    public SolutionResults getSolution(List<Node> nodes, Distance[][] distances, int numOfVehicles, int vehicleCapacity, LocalTime startingTime) {
        SolutionResults solution = new GreedyMethod().getSolution(nodes, distances, numOfVehicles, vehicleCapacity, startingTime);
        Map<Integer, ArrayList<Integer>> routesMap = solution.getRoutesMap();
        int usedVehicles = routesMap.size();
        initializeVariables(nodes, distances, startingTime, usedVehicles);

        Vehicle[] vehicles = new Vehicle[solution.getVehicles().length];
        for (int i = 0; i < vehicles.length; i++) {
            vehicles[i] = new Vehicle(solution.getVehicles()[i]);
        }

        double currBestSolutionTime = solution.getTotalSolutionTime();

        int iterationCount = 0;

        //Buffers holding information about the best move
        int moveFromVehicle = 0, moveToVehicle = 0, moveIndexFrom = 0, moveIndexTo = 0, moveFromTimeChange = 0, moveToTimeChange = 0;

        while (iterationCount != MAX_ITERATIONS) {
            iterationCount++;
            double bestIterationTimeDelta = Double.MAX_VALUE;
            for (int vehIndex1 = 0; vehIndex1 < usedVehicles; vehIndex1++) {
                ArrayList<Integer> vehRoute1 = routesMap.get(vehIndex1);
                for (int routeNodeIndex1 = 1; routeNodeIndex1 < routesMap.get(vehIndex1).size() - 2; routeNodeIndex1++) {
                    for (int vehIndex2 = 0; vehIndex2 < usedVehicles; vehIndex2++) {
                        ArrayList<Integer> vehRoute2 = routesMap.get(vehIndex2);
                        for (int routeNodeIndex2 = 1; routeNodeIndex2 < routesMap.get(vehIndex2).size() - 2; routeNodeIndex2++) {

                            //try to insert node from veh1 to veh2
                            if ((vehIndex1 == vehIndex2 && routeNodeIndex1 != routeNodeIndex2)
                                    || (vehIndex1 != vehIndex2 && vehicles[vehIndex2].getCurrentLoad() >= nodes.get(vehRoute1.get(routeNodeIndex1)).getDemand())) {

                                int removedNode = vehRoute1.remove(routeNodeIndex1);
                                int addIndex = routeNodeIndex2;
                                if (vehIndex1 != vehIndex2 || routeNodeIndex1 < routeNodeIndex2) {
                                    addIndex++;
                                }
                                vehRoute2.add(addIndex, removedNode);

                                int veh1TimeChange = calculateRouteTime(vehRoute1) - vehicles[vehIndex1].getRouteTime();
                                int veh2TimeChange = (vehIndex1 == vehIndex2) ? 0 : calculateRouteTime(vehRoute2) - vehicles[vehIndex2].getRouteTime();

                                vehRoute2.remove(addIndex);
                                vehRoute1.add(routeNodeIndex1, removedNode);

                                int timeDelta = veh1TimeChange + veh2TimeChange;

                                if (timeDelta < bestIterationTimeDelta) {
                                    bestIterationTimeDelta = timeDelta;
                                    moveFromVehicle = vehIndex1;
                                    moveToVehicle = vehIndex2;
                                    moveIndexFrom = routeNodeIndex1;
                                    moveIndexTo = addIndex;
                                    moveFromTimeChange = veh1TimeChange;
                                    moveToTimeChange = veh2TimeChange;
                                }
                            }
                        }
                    }
                }
            }

            if (bestIterationTimeDelta >= 0 && Math.random() >= Math.exp(bestIterationTimeDelta/currentTemperature)) {
                currentTemperature *= coolingFactor;
                continue;
            }
            currBestSolutionTime += bestIterationTimeDelta;
            int movedNode = routesMap.get(moveFromVehicle).remove(moveIndexFrom);
            routesMap.get(moveToVehicle).add(moveIndexTo, movedNode);

            vehicles[moveFromVehicle].setRouteTime(vehicles[moveFromVehicle].getRouteTime() + moveFromTimeChange);
            vehicles[moveFromVehicle].decrementCurrentLoad(-nodes.get(movedNode).getDemand());
            vehicles[moveToVehicle].setRouteTime(vehicles[moveToVehicle].getRouteTime() + moveToTimeChange);
            vehicles[moveToVehicle].decrementCurrentLoad(nodes.get(movedNode).getDemand());

            if (currBestSolutionTime < solution.getTotalSolutionTime()) {
                solution.copyRoutesMap(routesMap);
                solution.setTotalSolutionTime(currBestSolutionTime);
                solution.copyVehicles(vehicles);
                System.out.println("Nowe najlepsze rozw w iteracji " + iterationCount);
            }
        }
        solution.calculateFinalSolutionValues(nodes, distances, startingTime);
        currentTemperature *= coolingFactor;
        return solution;
    }

    public int calculateRouteTime(ArrayList<Integer> route) {
        int totalRouteTime = 0;
        LocalTime localTime = startingTime;
        for (int i = 0; i < route.size() - 1; i++) {
            int timeBetweenNodes = distances[route.get(i)][route.get(i + 1)].getTime();
            totalRouteTime += timeBetweenNodes;
            localTime = localTime.plusHours(timeBetweenNodes);
            Node nextNode = nodes.get(route.get(i + 1));
            if (localTime.isAfter(nextNode.getAvailableTo()) || localTime.isBefore(nextNode.getAvailableFrom())) {
                long currentWaitingTime = Duration.between(localTime, nextNode.getAvailableFrom()).toHours();
                totalRouteTime += currentWaitingTime > 0 ? currentWaitingTime : currentWaitingTime + 24;
            }
            totalRouteTime += nextNode.getServiceTime();
            localTime = localTime.plusHours(nextNode.getServiceTime());
        }
        return totalRouteTime;
    }

    public void initializeVariables(List<Node> nodes, Distance[][] distances, LocalTime startingTime, int usedVehicles) {
        this.distances = distances;
        this.nodes = nodes;
        this.startingTime = startingTime;
    }
}
