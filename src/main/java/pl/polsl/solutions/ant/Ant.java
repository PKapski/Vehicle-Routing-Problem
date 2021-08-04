package pl.polsl.solutions.ant;

import lombok.Getter;
import lombok.Setter;
import pl.polsl.model.Node;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Ant {

    private Map<Integer, ArrayList<Integer>> routesMap = new HashMap<>();
    private double currentFreeLoad;
    private int currentVehicleIndex = 0;
    private LocalTime currentVehicleTime;
    private double startingVehicleCapacity;
    private LocalTime startingVehicleTime;

    public Ant(int numberOfVehicles, int vehicleCapacity, LocalTime startingTime) {
        for (int i = 0; i < numberOfVehicles; i++) {
            routesMap.put(i, new ArrayList<>());
            routesMap.get(i).add(0);
        }
        currentFreeLoad = vehicleCapacity;
        startingVehicleCapacity = vehicleCapacity;
        startingVehicleTime = startingTime;
        currentVehicleTime = startingTime;
    }

    public void addNodeToCurrentRoute(Node node, int travelTimeWithTimeWindows) {
        routesMap.get(currentVehicleIndex).add(node.getId());
        currentFreeLoad -= node.getDemand();
        currentVehicleTime = currentVehicleTime.plusHours(travelTimeWithTimeWindows);
    }

    public void setNextVehicle() {
        currentVehicleIndex++;
        currentFreeLoad = startingVehicleCapacity;
        currentVehicleTime = startingVehicleTime;
    }
}
