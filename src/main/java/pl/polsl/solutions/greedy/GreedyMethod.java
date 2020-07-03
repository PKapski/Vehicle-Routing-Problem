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

    public SolutionResults getSolution(List<Node> nodes, Distance[][] distances, int numOfVehicles, int vehicleCapacity, LocalTime currentTime) {
        int numberOfNodes = nodes.size() - 1;
        double distanceTraveled = 0;
        int totalSolutionTime = 0, currentVehicleTime = 0, timeSpentWaiting = 0;
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
        while (numberOfNodes > 0) {
            nextNodeIndex = getMinimumPossibleNodeId(distances[vehicles[currVehicleId].getCurrentNode()]);
            Distance currentPathDistance = distances[vehicles[currVehicleId].getCurrentNode()][nextNodeIndex];
            distanceTraveled += currentPathDistance.getDistance();
            currentVehicleTime += currentPathDistance.getTime();
            currentTime = currentTime.plusHours(currentPathDistance.getTime());
            Node nextNode = nodes.get(nextNodeIndex);
            if (currentTime.isAfter(nextNode.getAvailableTo()) || currentTime.isBefore(nextNode.getAvailableFrom())) {
                long currentWaitingTime = Duration.between(currentTime, nextNode.getAvailableFrom()).toHours();
                currentVehicleTime += currentWaitingTime > 0 ? currentWaitingTime : currentWaitingTime + 24;
                timeSpentWaiting += currentWaitingTime > 0 ? currentWaitingTime : currentWaitingTime + 24;
            }
            currentVehicleTime += nextNode.getServiceTime();
            routesMap.get(vehicles[currVehicleId].getId()).add(nextNodeIndex);
            if (nextNodeIndex == 0) {
                currVehicleId++;
                if (currVehicleId == numOfVehicles) {
                    throw new InvalidAssumptionsError("No more vehicles available for other Nodes. Invalid assumptions.");
                }
            } else {
                numberOfNodes--;
                totalSolutionTime = Math.max(totalSolutionTime, currentVehicleTime);
                currentVehicleTime = 0;
                vehicles[currVehicleId].setCurrentLoad(vehicles[currVehicleId].getCurrentLoad() - nextNode.getDemand());
                nextNode.setWasVisited(true);
                vehicles[currVehicleId].setCurrentNode(nextNodeIndex);
            }
        }
        nextNodeIndex = 0;
        distanceTraveled += distances[vehicles[currVehicleId].getCurrentNode()][nextNodeIndex].getDistance();
        routesMap.get(vehicles[currVehicleId].getId()).add(nextNodeIndex);

        return new SolutionResults(routesMap, distanceTraveled, totalSolutionTime, timeSpentWaiting);
    }

    public int getMinimumPossibleNodeId(Distance[] list) {
        int index = -1;
        double minValue = 999999;
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
