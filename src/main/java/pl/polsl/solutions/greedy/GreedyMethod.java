package pl.polsl.solutions.greedy;

import pl.polsl.solutions.InvalidAssumptionsError;
import pl.polsl.solutions.SolutionMethodStrategy;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionResults;
import pl.polsl.model.Vehicle;

import java.util.*;

public class GreedyMethod implements SolutionMethodStrategy {

    private List<Node> nodes;
    private Vehicle[] vehicles;
    private int currVehicleId;

    public SolutionResults getSolution(List<Node> nodes, double[][] distances, int numOfVehicles, int vehicleCapacity) {
        int numberOfNodes = nodes.size()-1;
        double distanceTraveled=0;
        this.nodes = nodes;
        currVehicleId = 0;
        vehicles = new Vehicle[numOfVehicles];
        Map<Integer, ArrayList<Integer>> routesMap = new HashMap<>();
        for (int i = 0; i < numOfVehicles; i++) {
            vehicles[i] = new Vehicle(i, vehicleCapacity);
            routesMap.put(i, new ArrayList<>());
            routesMap.get(i).add(0);
        }
        int nextNode;
        while (numberOfNodes > 0) {
            nextNode = getMinimumPossibleNodeId(distances[vehicles[currVehicleId].getCurrentNode()]);
            distanceTraveled += distances[vehicles[currVehicleId].getCurrentNode()][nextNode];
            routesMap.get(vehicles[currVehicleId].getId()).add(nextNode);
            if (nextNode==0) {
                currVehicleId++;
                if (currVehicleId==numOfVehicles) {
                    throw new InvalidAssumptionsError("No more vehicles avaible for other Nodes. Invalid assumptions.");
                }
            } else {
                numberOfNodes--;
                vehicles[currVehicleId].setCurrentLoad(vehicles[currVehicleId].getCurrentLoad() - nodes.get(nextNode).getDemand());
                nodes.get(nextNode).setWasVisited(true);
                vehicles[currVehicleId].setCurrentNode(nextNode);
            }
        }
        nextNode=0;
        distanceTraveled += distances[vehicles[currVehicleId].getCurrentNode()][nextNode];
        routesMap.get(vehicles[currVehicleId].getId()).add(nextNode);

        return new SolutionResults(routesMap, distanceTraveled);
    }

    public int getMinimumPossibleNodeId(double[] list) {
        int index = -1;
        double minValue = 999999;
        for (int i = 1; i < list.length; i++) {
            if (!nodes.get(i).wasVisited && vehicles[currVehicleId].getCurrentLoad() - nodes.get(i).getDemand() >= 0) {
                if (list[i]<minValue) {
                    minValue = list[i];
                    index = i;
                }
            }
        }
        return index == -1 ? 0 : index;
    }
}
