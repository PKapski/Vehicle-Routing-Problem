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

    private final int MAX_ITERATIONS = 200;
    private Distance[][] distances;
    private List<Node> nodes;
    private LocalTime startingTime;

    @Override
    public SolutionResults getSolution(List<Node> nodes, Distance[][] distances, int numOfVehicles, int vehicleCapacity, LocalTime startingTime) {
        int numberOfNodes = nodes.size() - 1;
        this.distances = distances;
        this.nodes = nodes;
        this.startingTime = startingTime;
        SolutionResults results = new GreedyMethod().getSolution(nodes, distances, numOfVehicles, vehicleCapacity, startingTime);
        Map<Integer, ArrayList<Integer>> routesMap = results.getRoutesMap();
        int usedVehicles = routesMap.size();
        Vehicle[] vehicles = results.getVehicles();
        double currBestDistance = results.getDistanceTraveled();
        double currBestSolutionTime = results.getDistanceTraveled();
        double currBestTimeSpentWaiting = results.getTimeSpentWaiting();
        int vehIndex1, vehIndex2, routeNodeIndex1, routeNodeIndex2;
        int iterationCount = 0;
        int[][] TABU_MATRIX = new int[numberOfNodes + 1][numberOfNodes + 1];
        for (int i = 0; i <= numberOfNodes; i++) {
            for (int j = 0; j <= numberOfNodes; j++) {
                TABU_MATRIX[i][j] = 0;
            }
        }

        while (iterationCount != MAX_ITERATIONS) {
            iterationCount++;
            double bestIterationTimeDelta = Double.MAX_VALUE;
            for (vehIndex1 = 0; vehIndex1 < usedVehicles; vehIndex1++) {
                ArrayList<Integer> vehRoute1 = new ArrayList<>(routesMap.get(vehIndex1));
                for (routeNodeIndex1 = 1; routeNodeIndex1 < vehRoute1.size() - 1; routeNodeIndex1++) {
                    for (vehIndex2 = 0; vehIndex2 < usedVehicles; vehIndex2++) {
                        ArrayList<Integer> vehRoute2 = new ArrayList<>(routesMap.get(vehIndex2));
                        for (routeNodeIndex2 = 1; routeNodeIndex2 < vehRoute2.size() - 1; routeNodeIndex2++) {
                            //try to insert node from veh1 to veh2
                            if ((vehIndex1 == vehIndex2) || vehicles[vehIndex2].getCurrentLoad() - vehRoute1.get(routeNodeIndex1) >= 0) {

                                int switchNodeIndex = vehRoute1.get(routeNodeIndex1);
                                vehRoute1.remove(routeNodeIndex1);
                                if (vehRoute1 == vehRoute2) {
                                    if (vehIndex1 < vehIndex2) {
                                        vehRoute2.add(routeNodeIndex2, switchNodeIndex);
                                    } else {
                                        vehRoute2.add(routeNodeIndex2 + 1, switchNodeIndex);
                                    }
                                } else {
                                    vehRoute2.add(routeNodeIndex2 + 1, switchNodeIndex);
                                }

                                int timeDelta = vehicles[vehIndex1].getRouteTime() - calculateRouteTime(vehRoute1)
                                        + vehicles[vehIndex2].getRouteTime() - calculateRouteTime(vehRoute2);

                                if (timeDelta < bestIterationTimeDelta) {
                                    //TODO: set proper values
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
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
        }
        return totalRouteTime;
    }
}
