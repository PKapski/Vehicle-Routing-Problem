package pl.polsl.utils.data;

import pl.polsl.model.Distance;
import pl.polsl.model.Node;

import java.util.List;

public class DataUtils {
    public Distance[][] calculateNodeDistances(List<Node> nodes) {
        int numOfNodes = nodes.size();
        Distance[][] distances = new Distance[numOfNodes][numOfNodes];
        int xDiff, yDiff;
        for (int i = 0; i < numOfNodes; i++) {
            for (int j = 0; j < numOfNodes; j++) {
                xDiff = nodes.get(i).getX() - nodes.get(j).getX();
                yDiff = nodes.get(i).getY() - nodes.get(j).getY();
                distances[i][j] = new Distance();
                distances[i][j].setDistance(Math.round(Math.sqrt((xDiff * xDiff) + (yDiff * yDiff)) * 100) / 100.0);
                distances[i][j].setTime((int) Math.round(distances[i][j].getDistance() / 5));
            }
        }
        return distances;
    }
}
