package pl.polsl.solutions;

import pl.polsl.model.Distance;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionResults;

import java.time.LocalTime;
import java.util.List;

public interface SolutionMethodStrategy {
    SolutionResults getSolution(List<Node> nodes, Distance[][] distances, int numOfVehicles, int vehicleCapacity, LocalTime startingTime);
}
