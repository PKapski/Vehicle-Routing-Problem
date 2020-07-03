package pl.polsl.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class SolutionResults {
    private Map<Integer, ArrayList<Integer>> routesMap;
    private double distanceTraveled;
    private double totalSolutionTime;
    private double timeSpentWaiting;
}
