package pl.polsl.process;

import pl.polsl.model.Distance;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionMethod;
import pl.polsl.model.SolutionResults;
import pl.polsl.solutions.SolutionMethodStrategy;
import pl.polsl.solutions.greedy.GreedyMethod;
import pl.polsl.solutions.random.RandomMethod;
import pl.polsl.solutions.simulatedannealing.SimulatedAnnealingMethod;
import pl.polsl.solutions.tabu.TabuMethod;
import pl.polsl.utils.console.ConsoleUtils;
import pl.polsl.utils.data.DataUtils;
import pl.polsl.utils.files.FilesManager;

import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.util.List;

public class VRPProcessor {

    public static final String NUMBER_OF_VEHICLES = "numberOfVehicles";
    public static final String VEHICLE_CAPACITY = "vehicleCapacity";
    public static final String START_HOUR = "startHour";
    public static final String METHOD = "method";

    public static SolutionResults runVRP() throws FileNotFoundException {
        FilesManager filesManager = new FilesManager();
        ConsoleUtils consoleUtils = new ConsoleUtils();
        DataUtils dataUtils = new DataUtils();
        List<Node> nodes = filesManager.loadNodesFromCSV("src/main/resources/initialData/inputCSVData3.csv");
        Distance[][] distances = dataUtils.calculateNodeDistances(nodes);
        int numberOfNodes = distances.length;
        int numberOfVehicles = Integer.parseInt(System.getProperty(NUMBER_OF_VEHICLES, "4"));
        int vehicleCapacity = Integer.parseInt(System.getProperty(VEHICLE_CAPACITY, "15"));
        LocalTime startingTime = LocalTime.of(Integer.parseInt(System.getProperty(START_HOUR, "4")), 0);
        consoleUtils.printInitialConditions(distances, numberOfNodes, numberOfVehicles, vehicleCapacity);

        SolutionMethodStrategy strategy;
        SolutionMethod method = SolutionMethod.valueOf(System.getProperty(METHOD, SolutionMethod.TABU.name()).toUpperCase());
        switch (method) {
            case GREEDY:
                strategy = new GreedyMethod();
                break;
            case TABU:
                strategy = new TabuMethod();
                break;
            case SA:
                strategy = new SimulatedAnnealingMethod();
                break;
            case RANDOM:
                strategy = new RandomMethod();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + System.getProperty(METHOD));
        }
        long startTime = System.nanoTime();
        SolutionResults results = strategy.getSolution(nodes, distances, numberOfVehicles, vehicleCapacity, startingTime);
        long stopTime = System.nanoTime();
        consoleUtils.printResults(results, stopTime - startTime);
        return results;
    }
}
