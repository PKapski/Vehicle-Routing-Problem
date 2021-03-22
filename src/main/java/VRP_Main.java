import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import pl.polsl.model.Distance;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionMethod;
import pl.polsl.model.SolutionResults;
import pl.polsl.solutions.SolutionMethodStrategy;
import pl.polsl.solutions.greedy.GreedyMethod;
import pl.polsl.solutions.tabu.TabuMethod;
import pl.polsl.utils.console.ConsoleUtils;
import pl.polsl.utils.data.DataUtils;
import pl.polsl.utils.files.FilesManager;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VRP_Main extends Application {

    public static final String NUMBER_OF_VEHICLES = "numberOfVehicles";
    public static final String VEHICLE_CAPACITY = "vehicleCapacity";
    public static final String START_HOUR = "startHour";
    public static final String METHOD = "method";
    private static SolutionResults results;
    private static List<Node> nodes;

    public static void main(String[] args) throws IOException {
        FilesManager filesManager = new FilesManager();
        ConsoleUtils consoleUtils = new ConsoleUtils();
        DataUtils dataUtils = new DataUtils();
        nodes = filesManager.loadNodesFromCSV("src/main/resources/initialData/inputCSVData.csv");
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
            default:
                throw new IllegalStateException("Unexpected value: " + System.getProperty(METHOD));
        }
        results = strategy.getSolution(nodes, distances, numberOfVehicles, vehicleCapacity, startingTime);
        consoleUtils.printResults(results);
        launch();
    }


    @Override
    public void start(Stage stage) {
        Map<Integer, Node> nodesMap = getNodesMap();
        Group root = new Group();
        Circle circle = createCircle(nodes.get(0), Color.RED);
        root.getChildren().add(circle);
        for (ArrayList<Integer> route : results.getRoutesMap().values()) {
            if (route.size() == 1) {
                continue;
            }

            for (int i = 0; i < route.size() - 1; i++) {
                if (route.get(i + 1) != 0) {
                    circle = createCircle(nodesMap.get(route.get(i + 1)), Color.BLACK);
                    root.getChildren().add(circle);
                }
                Line line = new Line();
                line.setStartX(nodesMap.get(route.get(i)).getX());
                line.setStartY(nodesMap.get(route.get(i)).getY());
                line.setEndX(nodesMap.get(route.get(i + 1)).getX());
                line.setEndY(nodesMap.get(route.get(i + 1)).getY());
                root.getChildren().add(line);
            }
        }

        Scene scene = new Scene(root, 800, 800);
        stage.setScene(scene);
        stage.show();
    }

    private Map<Integer, Node> getNodesMap() {
        Map<Integer, Node> nodesMap = new HashMap<>();
        for (Node node : nodes) {
            nodesMap.put(node.getId(), node);
        }
        return nodesMap;
    }

    private Circle createCircle(Node node, Paint color) {
        Circle circle = new Circle();
        circle.setCenterX(node.getX());
        circle.setCenterY(node.getY());
        circle.setRadius(5.0f);
        circle.setFill(color);
        return circle;
    }
}
