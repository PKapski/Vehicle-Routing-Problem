package pl.polsl.solutions.greedy;

import pl.polsl.model.Distance;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionResults;
import pl.polsl.model.Vehicle;
import pl.polsl.solutions.InvalidAssumptionsError;
import pl.polsl.solutions.SolutionMethodStrategy;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GreedyMethod implements SolutionMethodStrategy {

    private List<Node> nodes;
    private Vehicle[] vehicles;
    private int currVehicleId;

    public SolutionResults getSolution(List<Node> nodes, Distance[][] distances, int numOfVehicles, int vehicleCapacity, LocalTime startingTime) {
        int numberOfNodes = nodes.size() - 1;
        double distanceTraveled = 0;
        int totalSolutionTime = 0;
        int currentVehicleRouteTime = 0;
        int timeSpentWaiting = 0;
        int currentNode = 0;
        LocalTime currentVehicleTime = startingTime;
        this.nodes = nodes;
        currVehicleId = 0;
        vehicles = new Vehicle[numOfVehicles];
        Map<Integer, ArrayList<Integer>> routesMap = initVehiclesAndRoutes(numOfVehicles, vehicleCapacity);
        boolean terminate = false;
        while (!terminate) {
            int nextNodeIndex = getMinimumPossibleNodeId(distances[currentNode]);
            Distance currentPathDistance = distances[currentNode][nextNodeIndex];
            distanceTraveled += currentPathDistance.getDistance();
            currentVehicleRouteTime += currentPathDistance.getTime();
            currentVehicleTime = currentVehicleTime.plusHours(currentPathDistance.getTime());
            Node nextNode = nodes.get(nextNodeIndex);
            if (isNotInTimeWindow(currentVehicleTime, nextNode)) {
                long currentWaitingTime = Duration.between(currentVehicleTime, nextNode.getAvailableFrom()).toHours();
                currentWaitingTime = currentWaitingTime > 0 ? currentWaitingTime : currentWaitingTime + 24;
                currentVehicleRouteTime += currentWaitingTime;
                timeSpentWaiting += currentWaitingTime;
            }
            currentVehicleRouteTime += nextNode.getServiceTime();
            currentVehicleTime = currentVehicleTime.plusHours(nextNode.getServiceTime());
            routesMap.get(vehicles[currVehicleId].getId()).add(nextNodeIndex);
            if (nextNodeIndex == 0) {
                currVehicleId++;
                totalSolutionTime += currentVehicleRouteTime;
                vehicles[currVehicleId - 1].setRouteTime(currentVehicleRouteTime);
                currentVehicleRouteTime = 0;
                currentVehicleTime = startingTime;
                currentNode = 0;
                if (numberOfNodes == 0) {
                    terminate = true;
                } else {
                    if (currVehicleId == numOfVehicles) {
                        throw new InvalidAssumptionsError("No more vehicles available for other Nodes. Invalid assumptions.");
                    }
                }
            } else {
                numberOfNodes--;
                vehicles[currVehicleId].decrementCurrentLoad(nextNode.getDemand());
                nextNode.setVisited(true);
                currentNode = nextNodeIndex;
            }
        }

        for (int i = 0; i < routesMap.size(); i++) {
            if (routesMap.get(i).size() == 1) {
                routesMap.remove(i);
            }
        }

        return new SolutionResults(routesMap, distanceTraveled, totalSolutionTime, timeSpentWaiting, vehicles);
    }

    private Map<Integer, ArrayList<Integer>> initVehiclesAndRoutes(int numOfVehicles, int vehicleCapacity) {
        Map<Integer, ArrayList<Integer>> routesMap = new HashMap<>();
        for (int i = 0; i < numOfVehicles; i++) {
            vehicles[i] = new Vehicle(i, vehicleCapacity);
            routesMap.put(i, new ArrayList<>());
            routesMap.get(i).add(0);
        }
        return routesMap;
    }

    private boolean isNotInTimeWindow(LocalTime currentVehicleTime, Node nextNode) {
        return currentVehicleTime.isAfter(nextNode.getAvailableTo()) || currentVehicleTime.isBefore(nextNode.getAvailableFrom());
    }

    public int getMinimumPossibleNodeId(Distance[] list) {
        int index = -1;
        for (int i = 1; i < list.length; i++) {
        double minValue = Double.MAX_VALUE;
            if (canVisitNode(i) && list[i].getDistance() < minValue) {
                minValue = list[i].getDistance();
                index = i;
            }
        }
        return index == -1 ? 0 : index;
    }

    private boolean canVisitNode(int i) {
        return !nodes.get(i).isVisited() && vehicles[currVehicleId].getCurrentLoad() >= nodes.get(i).getDemand();
    }
}
