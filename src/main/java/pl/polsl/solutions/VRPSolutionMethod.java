package pl.polsl.solutions;

import pl.polsl.model.Distance;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionResults;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class VRPSolutionMethod implements SolutionMethodStrategy {

    protected Distance[][] distances;
    protected List<Node> nodes;
    protected LocalTime startingTime;

    @Override
    public SolutionResults getSolution(List<Node> nodes, Distance[][] distances, int numOfVehicles, int vehicleCapacity, LocalTime startingTime) {
        return null;
    }

    protected int calculateRouteTime(ArrayList<Integer> route) {
        int totalRouteTime = 0;
        LocalTime localTime = startingTime;
        for (int i = 0; i < route.size() - 1; i++) {
            int timeBetweenNodes = distances[route.get(i)][route.get(i + 1)].getTime();
            totalRouteTime += timeBetweenNodes;
            localTime = localTime.plusHours(timeBetweenNodes);
            Node nextNode = nodes.get(route.get(i + 1));
            if (isNotInTimeWindow(localTime, nextNode)) {
                double currentWaitingTime = Math.ceil(Duration.between(localTime, nextNode.getAvailableFrom()).toMinutes() / 60.0);
                totalRouteTime += currentWaitingTime > 0 ? currentWaitingTime : currentWaitingTime + 24;
            }
            totalRouteTime += nextNode.getServiceTime();
            localTime = localTime.plusHours(nextNode.getServiceTime());
        }
        return totalRouteTime;
    }

    public void initializeVariables(List<Node> nodes, Distance[][] distances, LocalTime startingTime) {
        this.distances = distances;
        this.nodes = nodes;
        this.startingTime = startingTime;
    }

    protected boolean isNotInTimeWindow(LocalTime currentVehicleTime, Node nextNode) {
        return currentVehicleTime.isAfter(nextNode.getAvailableTo()) || currentVehicleTime.isBefore(nextNode.getAvailableFrom());
    }
}
