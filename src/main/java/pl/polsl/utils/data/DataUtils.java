package pl.polsl.utils.data;

import pl.polsl.model.Node;

import java.util.List;

public class DataUtils {
    public double[][] calculateNodeDistances(List<Node> nodes) {
        int numOfNodes = nodes.size();
        double[][] distances = new double[numOfNodes][numOfNodes];
        int xDiff,yDiff;
        for (int i = 0; i < numOfNodes; i++) {
            for (int j = 0; j < numOfNodes; j++) {
                xDiff = nodes.get(i).getX() - nodes.get(j).getX();
                yDiff = nodes.get(i).getY() - nodes.get(j).getY();
                distances[i][j] = Math.round(Math.sqrt((xDiff*xDiff) + (yDiff*yDiff))*100)/100.0;
            }
        }
        return distances;
    }
}
