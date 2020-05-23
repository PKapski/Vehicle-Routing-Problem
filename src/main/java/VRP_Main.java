import pl.polsl.model.Node;
import pl.polsl.model.SolutionResults;
import pl.polsl.solutions.SolutionMethodStrategy;
import pl.polsl.solutions.greedy.GreedyMethod;
import pl.polsl.utils.console.ConsoleUtils;
import pl.polsl.utils.data.DataUtils;
import pl.polsl.utils.files.FilesManager;

import java.io.IOException;
import java.util.List;

public class VRP_Main {
    public static void main(String[] args) throws IOException {
        FilesManager filesManager = new FilesManager();
        ConsoleUtils consoleUtils = new ConsoleUtils();
        DataUtils dataUtils = new DataUtils();
        //double[][] distances = filesManager.loadDistancesFromFile("src/main/resources/initialData/inputData.txt");
        List<Node> nodes = filesManager.loadNodesFromCSV("src/main/resources/initialData/inputCSVData.csv");
        double[][] distances = dataUtils.calculateNodeDistances(nodes);
        int numberOfNodes = distances.length;
        int numberOfVehicles = Integer.parseInt(System.getProperty("numberOfVehicles"));
        int vehicleCapacity = Integer.parseInt(System.getProperty("vehicleCapacity"));
        consoleUtils.printInitialConditions(distances, numberOfNodes, numberOfVehicles, vehicleCapacity);

        SolutionMethodStrategy strategy;
        switch (System.getProperty("method")) {
            case "greedy":
                strategy = new GreedyMethod();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + System.getProperty("method"));
        }
        SolutionResults results = strategy.getSolution(nodes, distances, numberOfVehicles, vehicleCapacity);
        consoleUtils.printResults(numberOfVehicles, results);
    }


}
