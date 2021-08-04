package pl.polsl.utils.data;

import pl.polsl.model.Distance;
import pl.polsl.model.Node;
import pl.polsl.solutions.InvalidAssumptionsError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DataUtils {

    public Distance[][] calculateNodeDistances(List<Node> nodes) {
        int numOfNodes = nodes.size();
        Distance[][] distances = new Distance[numOfNodes][numOfNodes];
        int xDiff, yDiff;
        for (int i = 0; i < numOfNodes; i++) {
            for (int j = 0; j < numOfNodes; j++) {
                xDiff = nodes.get(i).getX() - nodes.get(j).getX();
                yDiff = nodes.get(i).getY() - nodes.get(j).getY();
                distances[i][j] = new Distance();
                distances[i][j].setDistance(Math.round(Math.sqrt((xDiff * xDiff) + (yDiff * yDiff)) * 100) / 100.0);
                distances[i][j].setTime(Math.max(1, (int) Math.round(distances[i][j].getDistance() / 100)));
            }
        }
        return distances;
    }

    public void validateNodes(List<Node> nodes, int numberOfVehicle, int vehicleCapacity) {
        double demandSum = 0;
        Map<Integer, Integer> coordinatesMap = new HashMap<>();
        for (Node node: nodes) {
            if (Objects.equals(coordinatesMap.get(node.getX()), node.getY())) {
                throw new InvalidAssumptionsError("Two nodes cannot have the same coordinates, invalid assumptions");
            }
            coordinatesMap.put(node.getX(), node.getY());
            demandSum+=node.getDemand();
        }
        if (demandSum > numberOfVehicle * vehicleCapacity) {
            throw new InvalidAssumptionsError("Invalid assumptions: Not enough capacity for given load, minimum is " + demandSum);
        }
    }
}
