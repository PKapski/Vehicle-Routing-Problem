package pl.polsl.utils.console;

import pl.polsl.model.Distance;
import pl.polsl.model.SolutionResults;

import java.util.List;

public class ConsoleUtils {
    public void printInitialConditions(Distance[][] inputData, int numberOfNodes, int numberOfVehicles, int vehicleCapacity) {
        System.out.print("Input: \n");
        for (int i = 0; i < numberOfNodes; i++) {
            for (int j = 0; j < numberOfNodes; j++) {
                System.out.print(inputData[i][j].getDistance() + "  ");
            }
            System.out.println();
        }
        System.out.println("Number of vehicles: " + numberOfVehicles);
        System.out.println("Vehicle capacity: " + vehicleCapacity);
    }

    public void printResults(int numberOfVehicles, SolutionResults results) {
        System.out.println("\n\nRESULTS:\n\nPaths:");
        for (int i = 0; i < numberOfVehicles; i++) {
            System.out.print("Vehicle " + i + " path: ");
            List<Integer> list = results.getRoutesMap().get(i);
            for (Integer integer : list) {
                System.out.print(integer + " ");
            }
            System.out.println();
        }
        System.out.println("Total distance traveled: " + results.getDistanceTraveled());
        System.out.println("Total solution time: " + results.getTotalSolutionTime());
        System.out.println("Total time waiting for time windows: " + results.getTimeSpentWaiting());
    }
}
