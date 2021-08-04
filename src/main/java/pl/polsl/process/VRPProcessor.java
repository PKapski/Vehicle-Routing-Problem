package pl.polsl.process;

import pl.polsl.model.Distance;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionMethod;
import pl.polsl.model.SolutionResults;
import pl.polsl.solutions.SolutionMethodStrategy;
import pl.polsl.solutions.ant.AntColonyMethod;
import pl.polsl.solutions.greedy.GreedyMethod;
import pl.polsl.solutions.simulatedannealing.SimulatedAnnealingMethod;
import pl.polsl.solutions.tabu.TabuMethod;
import pl.polsl.utils.console.ConsoleUtils;
import pl.polsl.utils.data.DataUtils;
import pl.polsl.utils.files.FilesManager;

import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VRPProcessor {

    public static final String NUMBER_OF_VEHICLES_ARG = "numberOfVehicles";
    public static final String VEHICLE_CAPACITY_ARG = "vehicleCapacity";
    public static final String START_HOUR_ARG = "startHour";
    public static final String METHOD_ARG = "method";
    public static final String INPUT_FILE_ARG = "inputFile";
    public static final String RUN_COUNT_ARG = "runCount";
    public static final List<SolutionMethod> PROBABILISTIC_METHODS = Stream.of(SolutionMethod.SA, SolutionMethod.ANT) .collect(Collectors.toList());

    public static SolutionResults runVRP() throws FileNotFoundException {
        FilesManager filesManager = new FilesManager();
        ConsoleUtils consoleUtils = new ConsoleUtils();
        DataUtils dataUtils = new DataUtils();
        String fileName = System.getProperty(INPUT_FILE_ARG, "src/main/resources/initialData/inputCSVData60.csv");
        List<Node> nodes = filesManager.loadNodesFromCSV(fileName);
        Distance[][] distances = dataUtils.calculateNodeDistances(nodes);
        int numberOfNodes = distances.length;
        int numberOfVehicles = Integer.parseInt(System.getProperty(NUMBER_OF_VEHICLES_ARG, "4"));
        int vehicleCapacity = Integer.parseInt(System.getProperty(VEHICLE_CAPACITY_ARG, "50"));
        LocalTime startingTime = LocalTime.of(Integer.parseInt(System.getProperty(START_HOUR_ARG, "4")), 0);
        int runCount = Integer.parseInt(System.getProperty(RUN_COUNT_ARG, "10"));

        dataUtils.validateNodes(nodes, numberOfVehicles, vehicleCapacity);
        SolutionMethodStrategy strategy;
        SolutionMethod method = SolutionMethod.valueOf(System.getProperty(METHOD_ARG, SolutionMethod.TABU.name()).toUpperCase());
        consoleUtils.printInitialConditions(numberOfNodes, numberOfVehicles, vehicleCapacity, startingTime, method);

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
            for (int i = 0; i < runCount; i++) {
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
