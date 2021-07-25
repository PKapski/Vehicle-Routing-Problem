package pl.polsl.solutions;

import pl.polsl.model.Node;
import pl.polsl.model.Vehicle;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VRPInitialSolutionMethod {

    protected List<Node> nodes;
    protected Vehicle[] vehicles;
    protected int currVehicleId;

    protected Map<Integer, ArrayList<Integer>> initVehiclesAndRoutes(int numOfVehicles, int vehicleCapacity) {
        Map<Integer, ArrayList<Integer>> routesMap = new HashMap<>();
        for (int i = 0; i < numOfVehicles; i++) {
            vehicles[i] = new Vehicle(i, vehicleCapacity);
            routesMap.put(i, new ArrayList<>());
            routesMap.get(i).add(0);
        }
        return routesMap;
    }

    protected boolean isNotInTimeWindow(LocalTime currentVehicleTime, Node nextNode) {
        return currentVehicleTime.isAfter(nextNode.getAvailableTo()) || currentVehicleTime.isBefore(nextNode.getAvailableFrom());
    }

    protected boolean canVisitNode(int i) {
        return !nodes.get(i).isVisited() && vehicles[currVehicleId].getCurrentFreeLoad() >= nodes.get(i).getDemand();
    }
}
