package F21.CSC385.Project1.GeneticAlgorithm;

import F21.CSC385.Project1.GeneticAlgorithm.GUI.FloorVisualization;
import F21.CSC385.Project1.GeneticAlgorithm.Util.Constants;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/** Representation of the Population of Factory Floors needed for the Genetic Algorithm
 * @author Gregory Maldonado
 */
public class Population {

    private static List<FactoryFloor> floors = new ArrayList<>();
    private static List<FactoryFloor> orderedFloors;
    private static status futureStatus = status.INPROGRESS;
    public static HashMap<Integer, FactoryFloor> hashedFloors = new HashMap<>();
    public static volatile boolean updatePause;

    public static volatile FactoryFloor topPerformingFloor ;

    static int topPercent;

    /** Entry point for the Genetic Algorithm to start
     */
    public void start() {
        ExecutorService ex = Executors.newWorkStealingPool();
        for (int i = 0; i < Constants.MAXIMUM_FLOORS; i++) {
            floors.add(new FactoryFloor());
            hashedFloors.put(floors.get(i).hashCode(), floors.get(i));
        }

        orderedFloors = new ArrayList<>(floors);
        topPercent = (int) Math.floor( 0.10 * orderedFloors.size() );

        FactoryFloor.population = this;
        List<Future> futures = floors.stream()
                .map(ex::submit)
                .collect(Collectors.toList());

        FloorVisualization GUI = new FloorVisualization();

        Thread updateThread = new Thread( () -> {
            GUI.setFloor(floors.get(0));
            while (futureStatus == status.INPROGRESS) {
                try {
                    Thread.sleep(400);
                    updatePause = true;

                    updateTopList();
                    GUI.setFloor(topPerformingFloor);
                    GUI.repaint();

                } catch (InterruptedException e) { e.printStackTrace(); }
                updatePause = false;
            }
        });

        new Thread( () -> {
            if (!updateThread.isAlive())
                updatePause = false;
        }).start();

        updateThread.start();

        while (futureStatus == status.INPROGRESS ) {
            futureStatus = (int) futures.stream()
                    .filter(Future::isDone).count() == 0 ? status.INPROGRESS : status.FINISHED;
        }
    }

    /** Comparable interface Class for Factory Floors
     * @author Gregory Maldonado
     */
    static class FactoryFloorCompare implements Comparator<FactoryFloor> {

        /** Compares two Factory Floors, used for finding the best Factory Floor in the population
         *
         * @param factoryFloor First Factory Floor
         * @param otherFactoryFloor Second Factory Floor
         * @return Compared return value for Factory Floor fitness
         */
        @Override
        public int compare(FactoryFloor factoryFloor, FactoryFloor otherFactoryFloor) {
            return (factoryFloor != null && otherFactoryFloor != null) ? Double.compare(factoryFloor.getFitnessScore(), otherFactoryFloor.getFitnessScore()) : 0;
        }
    }

    /** Updates which Factory Floor has the best fitness score using the FactoryFloorCompare class
     */
    public static void updateTopList() {
        if (futureStatus == status.INPROGRESS) {
            orderedFloors.sort(new FactoryFloorCompare());
            Collections.reverse(orderedFloors);
            FactoryFloor bestPerformingFloor = null;

            for (FactoryFloor floor : orderedFloors) {
                if (bestPerformingFloor == null) { bestPerformingFloor = floor; continue; }
                if (floor.getFitnessScore() > bestPerformingFloor.getFitnessScore()) {
                    bestPerformingFloor = floor;
                }
            }
            if (bestPerformingFloor != null) topPerformingFloor = bestPerformingFloor;
        }
    }

    /** Enumeration for thread aliveness
     */
    private enum status {
        FINISHED, INPROGRESS
    }

}
