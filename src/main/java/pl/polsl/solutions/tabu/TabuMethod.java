package pl.polsl.solutions.tabu;

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

public class TabuMethod implements SolutionMethodStrategy {

    private static final int MAX_ITERATIONS = 400;
    private Distance[][] distances;
    private List<Node> nodes;
    private LocalTime startingTime;

    @Override
    public SolutionResults getSolution(List<Node> nodes, Distance[][] distances, int numOfVehicles, int vehicleCapacity, LocalTime startingTime) {
        int numberOfNodes = nodes.size() - 1;
        this.distances = distances;
        this.nodes = nodes;
        this.startingTime = startingTime;
        SolutionResults bestSolution = new GreedyMethod().getSolution(nodes, distances, numOfVehicles, vehicleCapacity, startingTime);
        Map<Integer, ArrayList<Integer>> routesMap = bestSolution.getRoutesMap();
        int usedVehicles = routesMap.size();
        Vehicle[] vehicles = new Vehicle[bestSolution.getVehicles().length];
        for (int i = 0; i < vehicles.length; i++) {
            vehicles[i] = new Vehicle(bestSolution.getVehicles()[i]);
        }
        double currBestDistance = bestSolution.getDistanceTraveled();
        double currBestSolutionTime = bestSolution.getTotalSolutionTime();
        double currBestTimeSpentWaiting = bestSolution.getTimeSpentWaiting();
        int vehIndex1, vehIndex2, routeNodeIndex1, routeNodeIndex2;
        int iterationCount = 0;
        int[][] tabuMatrix = new int[numberOfNodes + 1][usedVehicles];
        for (int i = 0; i <= numberOfNodes; i++) {
            for (int j = 0; j < usedVehicles; j++) {
                tabuMatrix[i][j] = 0;
            }
        }
        int switchVeh1 = 0, switchVeh2 = 0, switchIndex1 = 0, switchIndex2 = 0, switchTimeChange1 = 0, switchTimeChange2 = 0;
        while (iterationCount != MAX_ITERATIONS) {
            iterationCount++;
            double bestIterationTimeDelta = Double.MAX_VALUE;
            for (vehIndex1 = 0; vehIndex1 < usedVehicles; vehIndex1++) {
                ArrayList<Integer> vehRoute1 = routesMap.get(vehIndex1);
                for (routeNodeIndex1 = 1; routeNodeIndex1 < vehRoute1.size() - 1; routeNodeIndex1++) {
                    for (vehIndex2 = 0; vehIndex2 < usedVehicles; vehIndex2++) {
                        ArrayList<Integer> vehRoute2 = routesMap.get(vehIndex2);
                        for (routeNodeIndex2 = 1; routeNodeIndex2 < vehRoute2.size() - 1; routeNodeIndex2++) {
                            //try to insert node from veh1 to veh2
                            if ((vehIndex1 == vehIndex2) || vehicles[vehIndex2].getCurrentLoad() - nodes.get(vehRoute1.get(routeNodeIndex1)).getDemand() >= 0) {

                                if (vehIndex1 != vehIndex2 || routeNodeIndex1 != routeNodeIndex2) {
                                    int removedNode = vehRoute1.remove(routeNodeIndex1);
                                    int addIndex = routeNodeIndex2;
                                    if (vehIndex1 != vehIndex2 || routeNodeIndex1 >= routeNodeIndex2) {
                                        addIndex++;
                                    }
                                    vehRoute2.add(addIndex, removedNode);

                                    int veh1TimeChange = calculateRouteTime(vehRoute1) - vehicles[vehIndex1].getRouteTime();
                                    int veh2TimeChange = calculateRouteTime(vehRoute2) - vehicles[vehIndex2].getRouteTime();
                                    if (vehIndex1 == vehIndex2) {
                                        veh1TimeChange /= 2;
                                        veh2TimeChange /= 2;
                                    }

                                    int timeDelta = veh1TimeChange + veh2TimeChange;

                                    vehRoute2.remove(addIndex);
                                    vehRoute1.add(routeNodeIndex1, removedNode);

                                    if (tabuMatrix[removedNode][vehIndex2] > 0 && currBestSolutionTime + timeDelta >= bestSolution.getTotalSolutionTime()) {
                                        break;
                                    }

                                    if (timeDelta < bestIterationTimeDelta) {
                                        bestIterationTimeDelta = timeDelta;
                                        switchVeh1 = vehIndex1;
                                        switchVeh2 = vehIndex2;
                                        switchIndex1 = routeNodeIndex1;
                                        switchIndex2 = addIndex;
                                        switchTimeChange1 = veh1TimeChange;
                                        switchTimeChange2 = veh2TimeChange;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            decrementTabuMatrix(tabuMatrix);
            currBestSolutionTime += bestIterationTimeDelta;
            int movedNode = routesMap.get(switchVeh1).remove(switchIndex1);
            routesMap.get(switchVeh2).add(switchIndex2, movedNode);
            vehicles[switchVeh1].setRouteTime(vehicles[switchVeh1].getRouteTime() + switchTimeChange1);
            vehicles[switchVeh1].setCurrentLoad(vehicles[switchVeh1].getCurrentLoad() + nodes.get(movedNode).getDemand());
            vehicles[switchVeh2].setRouteTime(vehicles[switchVeh2].getRouteTime() + switchTimeChange2);
            vehicles[switchVeh2].setCurrentLoad(vehicles[switchVeh2].getCurrentLoad() - nodes.get(movedNode).getDemand());
            tabuMatrix[movedNode][switchVeh2] += 5;
            if (currBestSolutionTime < bestSolution.getTotalSolutionTime()) {
                bestSolution.copyRoutesMap(routesMap);
                bestSolution.setTotalSolutionTime(currBestSolutionTime);
                bestSolution.copyVehicles(vehicles);
            }

        }
        return bestSolution;
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

    public void decrementTabuMatrix(int[][] tabuMatrix) {
        for (int i = 0; i < tabuMatrix.length; i++) {
            for (int j = 0; j < tabuMatrix[i].length; j++) {
                tabuMatrix[i][j]--;
            }
        }
    }
}
