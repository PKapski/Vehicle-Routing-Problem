package pl.polsl.solutions.simulatedannealing;

import pl.polsl.model.Distance;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionResults;
import pl.polsl.model.Vehicle;
import pl.polsl.solutions.SolutionMethodStrategy;
import pl.polsl.solutions.VRPSolutionMethod;
import pl.polsl.solutions.greedy.GreedyMethod;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SimulatedAnnealingMethod extends VRPSolutionMethod implements SolutionMethodStrategy {

    private static final int ITERATION_ATTEMPTS = 20;
    private static final double coolingFactor = 0.98; //range: <0.8,0.99>
    private double currentTemperature = 10.0;

    @Override
    public SolutionResults getSolution(List<Node> nodes, Distance[][] distances, int numOfVehicles, int vehicleCapacity, LocalTime startingTime) {
        SolutionResults solution = new GreedyMethod().getSolution(nodes, distances, numOfVehicles, vehicleCapacity, startingTime);
        Map<Integer, ArrayList<Integer>> routesMap = solution.getRoutesMap();
        int vehiclesCount = routesMap.size();
        initializeVariables(nodes, distances, startingTime);

        Vehicle[] vehicles = new Vehicle[solution.getVehicles().length];
        for (int i = 0; i < vehicles.length; i++) {
            vehicles[i] = new Vehicle(solution.getVehicles()[i]);
        }

        double deltaSum = 0;

        int iterationCount = 0;
        double currentSolutionTime = solution.getTotalSolutionTime();
        while (currentTemperature > 1.0) {
            boolean swapNodes = iterationCount % 2 == 0;
            for (int i = 0; i < ITERATION_ATTEMPTS; i++) {
                boolean isValidOperation = false;
                if (swapNodes) {
                    while (!isValidOperation) {
                        int vehIndex1 = getRandomNumber(0, vehiclesCount);
                        int vehIndex2 = getRandomNumber(0, vehiclesCount);
                        int nodeIndex1 = getRandomNumber(1, routesMap.get(vehIndex1).size() - 1);
                        int nodeIndex2 = getRandomNumber(1, routesMap.get(vehIndex2).size() - 1);
                        Node node1 = nodes.get(routesMap.get(vehIndex1).get(nodeIndex1));
                        Node node2 = nodes.get(routesMap.get(vehIndex2).get(nodeIndex2));

                        if ((vehIndex1 == vehIndex2 && nodeIndex1 != nodeIndex2)
                                || (vehIndex1 != vehIndex2
                                && vehicles[vehIndex1].getCurrentFreeLoad() >= node2.getDemand() - node1.getDemand()
                                && vehicles[vehIndex2].getCurrentFreeLoad() >= node1.getDemand() - node2.getDemand())) {

                            ArrayList<Integer> vehRoute1 = routesMap.get(vehIndex1);
                            ArrayList<Integer> vehRoute2 = routesMap.get(vehIndex2);

                            vehRoute1.set(nodeIndex1, node2.getId());
                            vehRoute2.set(nodeIndex2, node1.getId());

                            int veh1TimeChange = vehicles[vehIndex1].getRouteTime() - calculateRouteTime(vehRoute1);
                            int veh2TimeChange = (vehIndex1 == vehIndex2) ? 0 : vehicles[vehIndex2].getRouteTime() - calculateRouteTime(vehRoute2);

                            int timeDelta = veh1TimeChange + veh2TimeChange;

                            if (shouldRejectSolution(timeDelta)) { //warunek odrzucenia rozwiązania
                                vehRoute1.set(nodeIndex1, node1.getId());
                                vehRoute2.set(nodeIndex2, node2.getId());
                                continue;
                            }

                            currentSolutionTime = currentSolutionTime - timeDelta;
                            vehicles[vehIndex1].incrementCurrentFreeLoad(node1.getDemand() - node2.getDemand());
                            vehicles[vehIndex2].incrementCurrentFreeLoad(node2.getDemand() - node1.getDemand());
                            vehicles[vehIndex1].setRouteTime(vehicles[vehIndex1].getRouteTime() - veh1TimeChange);
                            vehicles[vehIndex2].setRouteTime(vehicles[vehIndex2].getRouteTime() - veh2TimeChange);
                            deltaSum+=timeDelta;
                            isValidOperation = true;
                        }
                    }
                } else {
                    while (!isValidOperation) {
                        int vehIndex1 = getRandomNumber(0, vehiclesCount);
                        int vehIndex2 = getRandomNumber(0, vehiclesCount);
                        if (routesMap.get(vehIndex1).size() <= 3 || routesMap.get(vehIndex2).size() <= 3) {
                            continue;
                        }
                        int nodeIndex1 = getRandomNumber(1, routesMap.get(vehIndex1).size() - 2);
                        int nodeIndex2 = getRandomNumber(1, routesMap.get(vehIndex2).size() - 2);
                        Node node1 = nodes.get(routesMap.get(vehIndex1).get(nodeIndex1));

                        if ((vehIndex1 == vehIndex2 && nodeIndex1 != nodeIndex2)
                                || (vehIndex1 != vehIndex2 && vehicles[vehIndex2].getCurrentFreeLoad() >= node1.getDemand())) {

                            ArrayList<Integer> vehRoute1 = routesMap.get(vehIndex1);
                            ArrayList<Integer> vehRoute2 = routesMap.get(vehIndex2);

                            vehRoute1.remove(nodeIndex1);
                            int addIndex = nodeIndex2;
                            if (vehIndex1 != vehIndex2 || nodeIndex1 < nodeIndex2) {
                                addIndex++;
                            }
                            vehRoute2.add(addIndex, node1.getId());

                            int veh1TimeChange = vehicles[vehIndex1].getRouteTime() - calculateRouteTime(vehRoute1);
                            int veh2TimeChange = (vehIndex1 == vehIndex2) ? 0 : vehicles[vehIndex2].getRouteTime() - calculateRouteTime(vehRoute2);

                            int timeDelta = veh1TimeChange + veh2TimeChange;

                            if (shouldRejectSolution(timeDelta)) { //warunek odrzucenia rozwiązania
                                vehRoute2.remove(addIndex);
                                vehRoute1.add(nodeIndex1, node1.getId());
                                continue;
                            }

                            currentSolutionTime = currentSolutionTime - timeDelta;
                            vehicles[vehIndex1].incrementCurrentFreeLoad(node1.getDemand());
                            vehicles[vehIndex2].decrementCurrentFreeLoad(node1.getDemand());
                            vehicles[vehIndex1].setRouteTime(vehicles[vehIndex1].getRouteTime() - veh1TimeChange);
                            vehicles[vehIndex2].setRouteTime(vehicles[vehIndex2].getRouteTime() - veh2TimeChange);
                            deltaSum = deltaSum + timeDelta;
                            isValidOperation = true;
                        }
                    }
                }
            }
            iterationCount++;
            currentTemperature *= coolingFactor;
        }

        solution.copyRoutesMap(routesMap);
        solution.setTotalSolutionTime(currentSolutionTime);
        solution.copyVehicles(vehicles);
        solution.calculateFinalSolutionValues(nodes, distances, startingTime);
        return solution;
    }

    private boolean shouldRejectSolution(int timeDelta) {
        return timeDelta < 0 && Math.random() >= Math.exp(timeDelta / currentTemperature);
    }

    public void initializeVariables(List<Node> nodes, Distance[][] distances, LocalTime startingTime) {
        this.distances = distances;
        this.nodes = nodes;
        this.startingTime = startingTime;
    }

    public int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }
}
