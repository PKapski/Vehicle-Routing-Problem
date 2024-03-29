import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionResults;
import pl.polsl.process.VRPProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VRP_Main_Fx extends Application {

    private static SolutionResults results;

    public static void main(String[] args) throws IOException {
        results = VRPProcessor.runVRP();
        launch();
    }

    @Override
    public void start(Stage stage) {
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.BLACK, Color.PINK, Color.ORANGE, Color.YELLOW, Color.BROWN};

        Map<Integer, Node> nodesMap = getNodesMap();
        Group root = new Group();
        Circle circle = createCircle(results.getNodes().get(0), Color.RED);
        root.getChildren().add(circle);
        int vehicleIndex = 0;
        for (ArrayList<Integer> route : results.getRoutesMap().values()) {
            if (route.size() == 1) {
                continue;
            }
            Color color;
            if (vehicleIndex < colors.length) {
                color = colors[vehicleIndex];
            } else {
                color = Color.color(Math.random(), Math.random(), Math.random());
            }
            for (int i = 0; i < route.size() - 1; i++) {
                Node node = nodesMap.get(route.get(i + 1));
                if (route.get(i + 1) != 0) {
                    circle = createCircle(node, Color.BLACK);
                    Text text = new Text(node.getX() - 7, node.getY() - 7, node.getName());
                    root.getChildren().add(circle);
                    root.getChildren().add(text);
                }
                Line line = new Line();
                line.setStroke(color);
                line.setStartX(nodesMap.get(route.get(i)).getX());
                line.setStartY(nodesMap.get(route.get(i)).getY());
                line.setEndX(node.getX());
                line.setEndY(node.getY());
                root.getChildren().add(line);
            }
            vehicleIndex++;
        }

        Scene scene = new Scene(root, 800, 800);
        stage.setScene(scene);
        stage.setTitle("VRPTW solution");
        stage.show();
    }

    private Map<Integer, Node> getNodesMap() {
        Map<Integer, Node> nodesMap = new HashMap<>();
        for (Node node : results.getNodes()) {
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
