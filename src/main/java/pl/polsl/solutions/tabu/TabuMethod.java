package pl.polsl.solutions.tabu;

import pl.polsl.model.Distance;
import pl.polsl.model.Node;
import pl.polsl.model.SolutionResults;
import pl.polsl.model.Vehicle;
import pl.polsl.solutions.VRPSolutionMethod;
import pl.polsl.solutions.greedy.GreedyMethod;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TabuMethod extends VRPSolutionMethod {

    private static final int MAX_ITERATIONS = 1000;
    private static final int TABU_INCREMENT = 5;
    private int[][] swapTabuMatrix;
    private int[][] putTabuMatrix;

    @Override
    public SolutionResults getSolution(List<Node> nodes, Distance[][] distances, int numOfVehicles, int vehicleCapacity, LocalTime startingTime) {
        SolutionResults solution = new GreedyMethod().getSolution(nodes, distances, numOfVehicles, vehicleCapacity, startingTime);
        Map<Integer, ArrayList<Integer>> routesMap = solution.getRoutesMap();
        int vehiclesCount = routesMap.size();
        initializeVariables(nodes, distances, startingTime, vehiclesCount);

        Vehicle[] vehicles = new Vehicle[solution.getVehicles().length];
        for (int i = 0; i < vehicles.length; i++) {
            vehicles[i] = new Vehicle(solution.getVehicles()[i]);
        }

        double currBestSolutionTime = solution.getTotalSolutionTime();

        int iterationCount = 0;

        //Buffers holding information about the best move
        int moveFromVehicle = 0, moveToVehicle = 0, moveIndexFrom = 0, moveIndexTo = 0, moveFromTimeChange = 0, moveToTimeChange = 0;

        while (iterationCount != MAX_ITERATIONS) {
            iterationCount++;
            boolean swapNodes = iterationCount % 2 == 0;
            double bestIterationTimeDelta = Double.MAX_VALUE;
            for (int vehIndex1 = 0; vehIndex1 < vehiclesCount; vehIndex1++) {
                ArrayList<Integer> vehRoute1 = routesMap.get(vehIndex1);
                for (int routeNodeIndex1 = 1; routeNodeIndex1 < routesMap.get(vehIndex1).size() - 2; routeNodeIndex1++) {
                    for (int vehIndex2 = 0; vehIndex2 < vehiclesCount; vehIndex2++) {
                        ArrayList<Integer> vehRoute2 = routesMap.get(vehIndex2);
                        for (int routeNodeIndex2 = 1; routeNodeIndex2 < routesMap.get(vehIndex2).size() - 2; routeNodeIndex2++) {

                            if (swapNodes) {
                                //try to swap nodes between veh1 to veh2
                                if ((vehIndex1 == vehIndex2 && routeNodeIndex1 != routeNodeIndex2)
                                        || (vehIndex1 != vehIndex2
                                        && vehicles[vehIndex1].getCurrentFreeLoad() >= nodes.get(vehRoute2.get(routeNodeIndex2)).getDemand() - nodes.get(vehRoute1.get(routeNodeIndex1)).getDemand()
                                        && vehicles[vehIndex2].getCurrentFreeLoad() >= nodes.get(vehRoute1.get(routeNodeIndex1)).getDemand() - nodes.get(vehRoute2.get(routeNodeIndex2)).getDemand())) {

                                    int swapValue1 = vehRoute1.get(routeNodeIndex1);
                                    int swapValue2 = vehRoute2.get(routeNodeIndex2);

                                    vehRoute1.set(routeNodeIndex1, swapValue2);
                                    vehRoute2.set(routeNodeIndex2, swapValue1);

                                    int veh1TimeChange = calculateRouteTime(vehRoute1) - vehicles[vehIndex1].getRouteTime();
                                    int veh2TimeChange = (vehIndex1 == vehIndex2) ? 0 : calculateRouteTime(vehRoute2) - vehicles[vehIndex2].getRouteTime();

                                    vehRoute1.set(routeNodeIndex1, swapValue1);
                                    vehRoute2.set(routeNodeIndex2, swapValue2);

                                    int timeDelta = veh1TimeChange + veh2TimeChange;

                                    //Tabu && aspiration condition (current solution better than best found solution)
                                    if (swapTabuMatrix[Math.min(swapValue1, swapValue2)][Math.max(swapValue1, swapValue2)] > 0 && currBestSolutionTime + timeDelta >= solution.getTotalSolutionTime()) {
                                        continue;
                                    }

                                    if (timeDelta < bestIterationTimeDelta) {
                                        bestIterationTimeDelta = timeDelta;
                                        moveFromVehicle = vehIndex1;
                                        moveToVehicle = vehIndex2;
                                        moveIndexFrom = routeNodeIndex1;
                                        moveIndexTo = routeNodeIndex2;
                                        moveFromTimeChange = veh1TimeChange;
                                        moveToTimeChange = veh2TimeChange;
                                    }

                                }
                            } else {
                                //try to insert node from veh1 to veh2
                                if ((vehIndex1 == vehIndex2 && routeNodeIndex1 != routeNodeIndex2)
                                        || (vehIndex1 != vehIndex2 && vehicles[vehIndex2].getCurrentFreeLoad() >= nodes.get(vehRoute1.get(routeNodeIndex1)).getDemand())) {

                                    int removedNode = vehRoute1.remove(routeNodeIndex1);
                                    int addIndex = routeNodeIndex2;
                                    if (vehIndex1 != vehIndex2 || routeNodeIndex1 < routeNodeIndex2) {
                                        addIndex++;
                                    }
                                    vehRoute2.add(addIndex, removedNode);

                                    int veh1TimeChange = calculateRouteTime(vehRoute1) - vehicles[vehIndex1].getRouteTime();
                                    int veh2TimeChange = (vehIndex1 == vehIndex2) ? 0 : calculateRouteTime(vehRoute2) - vehicles[vehIndex2].getRouteTime();

                                    vehRoute2.remove(addIndex);
                                    vehRoute1.add(routeNodeIndex1, removedNode);

                                    int timeDelta = veh1TimeChange + veh2TimeChange;

                                    //Tabu && aspiration condition (current solution better than best found solution)
                                    if (putTabuMatrix[removedNode][vehIndex2] > 0 && currBestSolutionTime + timeDelta >= solution.getTotalSolutionTime()) {
                                        continue;
                                    }

                                    if (timeDelta < bestIterationTimeDelta) {
                                        bestIterationTimeDelta = timeDelta;
                                        moveFromVehicle = vehIndex1;
                                        moveToVehicle = vehIndex2;
                                        moveIndexFrom = routeNodeIndex1;
                                        moveIndexTo = addIndex;
                                        moveFromTimeChange = veh1TimeChange;
                                        moveToTimeChange = veh2TimeChange;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            decrementTabuMatrix(swapNodes);
            currBestSolutionTime += bestIterationTimeDelta;
            if (swapNodes) {
                int swapValue1 = routesMap.get(moveFromVehicle).get(moveIndexFrom);
                int swapValue2 = routesMap.get(moveToVehicle).get(moveIndexTo);

                routesMap.get(moveFromVehicle).set(moveIndexFrom, swapValue2);
                routesMap.get(moveToVehicle).set(moveIndexTo, swapValue1);

                swapTabuMatrix[Math.min(swapValue1, swapValue2)][Math.max(swapValue1, swapValue2)] += TABU_INCREMENT;
                vehicles[moveFromVehicle].incrementCurrentFreeLoad(nodes.get(swapValue1).getDemand() - nodes.get(swapValue2).getDemand());
                vehicles[moveToVehicle].incrementCurrentFreeLoad(nodes.get(swapValue2).getDemand() - nodes.get(swapValue1).getDemand());

            } else {
                int movedNode = routesMap.get(moveFromVehicle).remove(moveIndexFrom);
                routesMap.get(moveToVehicle).add(moveIndexTo, movedNode);
                putTabuMatrix[movedNode][moveToVehicle] += TABU_INCREMENT;
                vehicles[moveFromVehicle].incrementCurrentFreeLoad(nodes.get(movedNode).getDemand());
                vehicles[moveToVehicle].decrementCurrentFreeLoad(nodes.get(movedNode).getDemand());
            }

            vehicles[moveFromVehicle].setRouteTime(vehicles[moveFromVehicle].getRouteTime() + moveFromTimeChange);
            vehicles[moveToVehicle].setRouteTime(vehicles[moveToVehicle].getRouteTime() + moveToTimeChange);

            if (currBestSolutionTime < solution.getTotalSolutionTime()) {
                solution.copyRoutesMap(routesMap);
                solution.setTotalSolutionTime(currBestSolutionTime);
                solution.copyVehicles(vehicles);
            }
        }
        solution.calculateFinalSolutionValues(nodes, distances, startingTime);
        return solution;
    }

    public void initializeVariables(List<Node> nodes, Distance[][] distances, LocalTime startingTime, int usedVehicles) {
        super.initializeVariables(nodes, distances, startingTime);
        int nodesCount = nodes.size();
        this.swapTabuMatrix = new int[nodesCount][nodesCount];
        this.putTabuMatrix = new int[nodesCount][usedVehicles];
        for (int i = 0; i < nodesCount; i++) {
            for (int j = 0; j < usedVehicles; j++) {
                putTabuMatrix[i][j] = 0;
            }
        }
    }

    public void decrementTabuMatrix(boolean swapNodes) {
        int[][] tabuMatrix = swapNodes ? swapTabuMatrix : putTabuMatrix;
        for (int i = 0; i < tabuMatrix.length; i++) {
            for (int j = 0; j < tabuMatrix[i].length; j++) {
                tabuMatrix[i][j]--;
            }
        }
    }


}
