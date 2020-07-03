import pl.polsl.model.Distance;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionMethod;
import pl.polsl.model.SolutionResults;
import pl.polsl.solutions.SolutionMethodStrategy;
import pl.polsl.solutions.greedy.GreedyMethod;
import pl.polsl.utils.console.ConsoleUtils;
import pl.polsl.utils.data.DataUtils;
import pl.polsl.utils.files.FilesManager;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public class VRP_Main {

    public static final String NUMBER_OF_VEHICLES = "numberOfVehicles";
    public static final String VEHICLE_CAPACITY = "vehicleCapacity";
    public static final String START_HOUR = "startHour";
    public static final String METHOD = "method";

    public static void main(String[] args) throws IOException {
        FilesManager filesManager = new FilesManager();
        ConsoleUtils consoleUtils = new ConsoleUtils();
        DataUtils dataUtils = new DataUtils();
        List<Node> nodes = filesManager.loadNodesFromCSV("src/main/resources/initialData/inputCSVData.csv");
        Distance[][] distances = dataUtils.calculateNodeDistances(nodes);
        int numberOfNodes = distances.length;
        int numberOfVehicles = Integer.parseInt(System.getProperty(NUMBER_OF_VEHICLES, "4"));
        int vehicleCapacity = Integer.parseInt(System.getProperty(VEHICLE_CAPACITY, "15"));
        LocalTime startingTime = LocalTime.of(Integer.parseInt(System.getProperty(START_HOUR, "4")),0);
        consoleUtils.printInitialConditions(distances, numberOfNodes, numberOfVehicles, vehicleCapacity);

        SolutionMethodStrategy strategy;
        SolutionMethod method = SolutionMethod.valueOf(System.getProperty(METHOD, SolutionMethod.GREEDY.name()).toUpperCase());
        switch (method) {
            case GREEDY:
                strategy = new GreedyMethod();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + System.getProperty(METHOD));
        }
        SolutionResults results = strategy.getSolution(nodes, distances, numberOfVehicles, vehicleCapacity, startingTime);
        consoleUtils.printResults(numberOfVehicles, results);
    }


}
