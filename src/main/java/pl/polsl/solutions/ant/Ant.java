package pl.polsl.solutions.ant;

import lombok.Getter;
import lombok.Setter;
import pl.polsl.model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Ant {

    private Map<Integer, ArrayList<Integer>> routesMap = new HashMap<>();
    private double currentFreeLoad;
    private int currentVehicleIndex = 0;
    private double startingVehicleCapacity;

    public Ant(int numberOfVehicles, int vehicleCapacity) {
        for (int i=0; i<numberOfVehicles; i++) {
            routesMap.put(i, new ArrayList<>());
            routesMap.get(i).add(0);
        }
        currentFreeLoad = vehicleCapacity;
        startingVehicleCapacity = vehicleCapacity;
    }

    public void addNodeToCurrentRoute(Node node) {
        routesMap.get(currentVehicleIndex).add(node.getId());
        currentFreeLoad-=node.getDemand();
    }

    public void setNextVehicle() {
        currentVehicleIndex++;
        currentFreeLoad = startingVehicleCapacity;
    }
}
