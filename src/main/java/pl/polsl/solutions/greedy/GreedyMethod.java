package pl.polsl.solutions.greedy;

import pl.polsl.model.Distance;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionResults;
import pl.polsl.model.Vehicle;
import pl.polsl.solutions.InvalidAssumptionsError;
import pl.polsl.solutions.VRPInitialSolutionMethod;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GreedyMethod extends VRPInitialSolutionMethod {

    public static final boolean nodeChoiceBasedOnTime = true;

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
            //if true, will calculate nearest node based on time (including time windows wait), if false will take closest distance
            int nextNodeIndex = nodeChoiceBasedOnTime ? getClosestTimeNodeId(distances[currentNode], currentVehicleTime) : getClosestDistanceNodeId(distances[currentNode]);
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
                vehicles[currVehicleId].decrementCurrentFreeLoad(nextNode.getDemand());
                nextNode.setVisited(true);
                currentNode = nextNodeIndex;
            }
        }

        for (int i = 0; i < numOfVehicles; i++) {
            if (routesMap.get(i).size() == 1) {
                routesMap.remove(i);
            }
        }

        return new SolutionResults(routesMap, distanceTraveled, totalSolutionTime, timeSpentWaiting, vehicles, nodes);
    }

    public int getClosestDistanceNodeId(Distance[] distances) {
        int index = 0;
        double minValue = Double.MAX_VALUE;
        for (int i = 1; i < distances.length; i++) {
            if (canVisitNode(i) && distances[i].getDistance() < minValue) {
                minValue = distances[i].getDistance();
                index = i;
            }
        }
        return index;
    }

    public int getClosestTimeNodeId(Distance[] distances, LocalTime currentVehicleTime) {
        int index = 0;
        double minValue = Double.MAX_VALUE;
        for (int i = 1; i < distances.length; i++) {
            int travelTime = getTravelTimeWithTimeWindows(i, distances[i].getTime(), currentVehicleTime);
            if (canVisitNode(i) && travelTime < minValue) {
                minValue = travelTime;
                index = i;
            }
        }
        return index;
    }

    public int getTravelTimeWithTimeWindows(int nodeId, int travelTime, LocalTime currentVehicleTime) {
        Node node = nodes.get(nodeId);
        long timeSpentWaiting = 0;
        LocalTime vehicleTimeAfterArrival = currentVehicleTime.plusHours(travelTime);
        if (isNotInTimeWindow(vehicleTimeAfterArrival, node)) {
            timeSpentWaiting = Duration.between(vehicleTimeAfterArrival, node.getAvailableFrom()).toHours();
            timeSpentWaiting = timeSpentWaiting > 0 ? timeSpentWaiting : timeSpentWaiting + 24;
        }
        return travelTime + Math.toIntExact(timeSpentWaiting);
    }
}
