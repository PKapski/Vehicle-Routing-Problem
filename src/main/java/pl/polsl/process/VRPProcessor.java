package pl.polsl.process;

import pl.polsl.model.Distance;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionMethod;
import pl.polsl.model.SolutionResults;
import pl.polsl.solutions.SolutionMethodStrategy;
import pl.polsl.solutions.ant.AntColonyMethod;
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

    public static final String NUMBER_OF_VEHICLES_ARG = "numberOfVehicles";
    public static final String VEHICLE_CAPACITY_ARG = "vehicleCapacity";
    public static final String START_HOUR_ARG = "startHour";
    public static final String METHOD_ARG = "method";
    public static final String INPUT_FILE_ARG = "inputFile";
    public static final int PROBABILISTIC_METHODS_RUN_COUNT = 10;
    public static final List<SolutionMethod> PROBABILISTIC_METHODS = List.of(SolutionMethod.SA, SolutionMethod.ANT);

    public static SolutionResults runVRP() throws FileNotFoundException {
        FilesManager filesManager = new FilesManager();
        ConsoleUtils consoleUtils = new ConsoleUtils();
        DataUtils dataUtils = new DataUtils();
        String fileName = System.getProperty(INPUT_FILE_ARG, "src/main/resources/initialData/inputCSVData3.csv");
        List<Node> nodes = filesManager.loadNodesFromCSV(fileName);
        Distance[][] distances = dataUtils.calculateNodeDistances(nodes);
        int numberOfNodes = distances.length;
        int numberOfVehicles = Integer.parseInt(System.getProperty(NUMBER_OF_VEHICLES_ARG, "4"));
        int vehicleCapacity = Integer.parseInt(System.getProperty(VEHICLE_CAPACITY_ARG, "15"));
        LocalTime startingTime = LocalTime.of(Integer.parseInt(System.getProperty(START_HOUR_ARG, "4")), 0);
        consoleUtils.printInitialConditions(distances, numberOfNodes, numberOfVehicles, vehicleCapacity);

        SolutionMethodStrategy strategy;
        SolutionMethod method = SolutionMethod.valueOf(System.getProperty(METHOD_ARG, SolutionMethod.TABU.name()).toUpperCase());
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
            case ANT:
                strategy = new AntColonyMethod();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + System.getProperty(METHOD_ARG));
        }

        SolutionResults results = null;
        long startTime = 0;
        long stopTime = 0;
        if (PROBABILISTIC_METHODS.contains(method)) {
            for (int i = 0; i < PROBABILISTIC_METHODS_RUN_COUNT; i++) {
                long currentStartTime = System.nanoTime();
                SolutionResults currentResults = strategy.getSolution(nodes, distances, numberOfVehicles, vehicleCapacity, startingTime);
                nodes.forEach(x -> x.setVisited(false));
                long currentStopTime = System.nanoTime();

                if (results == null || currentResults.getTotalSolutionTime() < results.getTotalSolutionTime()) {
                    results = currentResults;
                    startTime = currentStartTime;
                    stopTime = currentStopTime;
                }
            }
        } else {
            startTime = System.nanoTime();
            results = strategy.getSolution(nodes, distances, numberOfVehicles, vehicleCapacity, startingTime);
            stopTime = System.nanoTime();
        }

        consoleUtils.printResults(results, stopTime - startTime);
        return results;
    }
}
