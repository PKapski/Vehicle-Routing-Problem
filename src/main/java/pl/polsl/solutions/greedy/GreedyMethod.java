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
        int totalSolutionTime = 0, currentVehicleRouteTime = 0, timeSpentWaiting = 0;
        int currentNode = 0;
        LocalTime currentVehicleTime = startingTime;
        this.nodes = nodes;
        currVehicleId = 0;
        vehicles = new Vehicle[numOfVehicles];
        Map<Integer, ArrayList<Integer>> routesMap = new HashMap<>();
        for (int i = 0; i < numOfVehicles; i++) {
            vehicles[i] = new Vehicle(i, vehicleCapacity);
            routesMap.put(i, new ArrayList<>());
            routesMap.get(i).add(0);
        }
        int nextNodeIndex;
        boolean terminate = false;
        while (!terminate) {
            nextNodeIndex = getMinimumPossibleNodeId(distances[currentNode]);
            Distance currentPathDistance = distances[currentNode][nextNodeIndex];
            distanceTraveled += currentPathDistance.getDistance();
            currentVehicleRouteTime += currentPathDistance.getTime();
            currentVehicleTime = currentVehicleTime.plusHours(currentPathDistance.getTime());
            Node nextNode = nodes.get(nextNodeIndex);
            if (currentVehicleTime.isAfter(nextNode.getAvailableTo()) || currentVehicleTime.isBefore(nextNode.getAvailableFrom())) {
                long currentWaitingTime = Duration.between(currentVehicleTime, nextNode.getAvailableFrom()).toHours();
                currentVehicleRouteTime += currentWaitingTime > 0 ? currentWaitingTime : currentWaitingTime + 24;
                timeSpentWaiting += currentWaitingTime > 0 ? currentWaitingTime : currentWaitingTime + 24;
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
                nextNode.setWasVisited(true);
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

    public int getMinimumPossibleNodeId(Distance[] list) {
        int index = -1;
        double minValue = Double.MAX_VALUE;
        for (int i = 1; i < list.length; i++) {
            if (!nodes.get(i).wasVisited && vehicles[currVehicleId].getCurrentLoad() - nodes.get(i).getDemand() >= 0) {
                if (list[i].getDistance() < minValue) {
                    minValue = list[i].getDistance();
                    index = i;
                }
            }
        }
        return index == -1 ? 0 : index;
    }
}
