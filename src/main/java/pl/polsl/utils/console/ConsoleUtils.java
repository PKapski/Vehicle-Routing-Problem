package pl.polsl.utils.console;

import pl.polsl.model.Distance;
import pl.polsl.model.SolutionMethod;
import pl.polsl.model.SolutionResults;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConsoleUtils {

    public void printInitialConditions(int numberOfNodes, int numberOfVehicles, int vehicleCapacity, LocalTime startingTime, SolutionMethod method) {
        System.out.print("Input: \n");
        System.out.println("Number of nodes: " + numberOfNodes);
        System.out.println("Number of vehicles: " + numberOfVehicles);
        System.out.println("Vehicle capacity: " + vehicleCapacity);
        System.out.println("Starting time: " + startingTime.toString());
        System.out.println("Solution method: " + method);
    }

    public void printResults(SolutionResults results, long executionTime) {
        System.out.println("\n\nRESULTS:\n\nPaths:");
        for (int i = 0; i < results.getRoutesMap().size(); i++) {
            System.out.print("Vehicle " + i + ": " + results.getVehicles()[i].getRouteTime() + "h, path: ");
            List<Integer> list = results.getRoutesMap().get(i);
            for (Integer integer : list) {
                System.out.print(integer + " ");
            }
            System.out.println();
        }
        System.out.println("Execution time: " + TimeUnit.MILLISECONDS.convert(executionTime, TimeUnit.NANOSECONDS) + " ms");
        System.out.println("Total distance traveled: " + results.getDistanceTraveled());
        System.out.println("Total solution time: " + results.getTotalSolutionTime());
        System.out.println("Total time waiting for time windows: " + results.getTimeSpentWaiting());
    }
}
