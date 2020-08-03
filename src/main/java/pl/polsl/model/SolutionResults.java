package pl.polsl.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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
}
