package pl.polsl.solutions;

import pl.polsl.model.Node;
import pl.polsl.model.SolutionResults;

import java.util.List;

public interface SolutionMethodStrategy {
    SolutionResults getSolution(List<Node> nodes, double[][] distances, int numOfVehicles, int vehicleCapacity);
}
