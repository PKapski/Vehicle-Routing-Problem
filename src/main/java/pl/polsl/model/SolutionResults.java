package pl.polsl.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class SolutionResults {

    private Map<Integer, ArrayList<Integer>> routesMap;
    private double distanceTraveled;
    private double totalSolutionTime;
    private double timeSpentWaiting;
    private Vehicle[] vehicles;
    private List<Node> nodes;

    public void copyRoutesMap(Map<Integer, ArrayList<Integer>> newRoute) {
        for (int i = 0; i < routesMap.size(); i++) {
            routesMap.put(i, new ArrayList<>(newRoute.get(i)));
        }
    }

    public void copyVehicles(Vehicle[] newVehicles) {
        for (int i = 0; i < vehicles.length; i++) {
            vehicles[i] = new Vehicle(newVehicles[i]);
        }
    }

    public void calculateFinalSolutionValues(List<Node> nodes, Distance[][] distances, LocalTime startingTime) {
        resetVariables();
        for (int i = 0; i < routesMap.size(); i++) {
            LocalTime localTime = startingTime;
            ArrayList<Integer> route = routesMap.get(i);
            for (int j = 0; j < route.size() - 1; j++) {
                Distance distance = distances[route.get(j)][route.get(j + 1)];
                distanceTraveled += distance.getDistance();
                totalSolutionTime += distance.getTime();
                localTime = localTime.plusHours(distance.getTime());
                Node nextNode = nodes.get(route.get(j + 1));
                if (localTime.isAfter(nextNode.getAvailableTo()) || localTime.isBefore(nextNode.getAvailableFrom())) {
                    long currentWaitingTime = Duration.between(localTime, nextNode.getAvailableFrom()).toHours();
                    currentWaitingTime = currentWaitingTime > 0 ? currentWaitingTime : currentWaitingTime + 24;
                    totalSolutionTime += currentWaitingTime;
                    timeSpentWaiting += currentWaitingTime;
                }
                totalSolutionTime += nextNode.getServiceTime();
                localTime = localTime.plusHours(nextNode.getServiceTime());
            }
        }
    }

    private void resetVariables() {
        distanceTraveled = 0;
        totalSolutionTime = 0;
        timeSpentWaiting = 0;
    }
}
