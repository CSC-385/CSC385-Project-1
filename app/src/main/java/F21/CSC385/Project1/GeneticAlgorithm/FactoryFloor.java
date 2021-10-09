package F21.CSC385.Project1.GeneticAlgorithm;

import F21.CSC385.Project1.GeneticAlgorithm.Util.Constants;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.*;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Representation of a Factory Floor
 * @author Gregory Maldonado
 */
public class FactoryFloor implements Runnable {

    public static Population population;
    private volatile List<Station> stations = new ArrayList<>();
    private volatile Station[][] floor = new Station[Constants.FACTORY_FLOOR_SIZE][Constants.FACTORY_FLOOR_SIZE];
    private final HashMap<Integer, Station> hashedStations = new HashMap<>();
    private double fitnessScore;
    private final static Exchanger<Station[][]> exchanger = new Exchanger<>();
    private volatile int currentGeneration;

    /** Setup for the Factory Floor
     */
    public FactoryFloor() {
        setup(Constants.MAXIMUM_STATIONS);
    }

    /** Setup for the Factory Floor already having existing Stations and an existing mapped Floor, made for thread safe copying
     *
     * @param stations A list of existing Stations
     * @param floor An existing mapped Floor
     */
    public FactoryFloor(List<Station> stations, Station[][] floor) {
        this.stations = stations;
        stations.forEach( station -> {
            floor[station.getX()][station.getY()] = station;
        });
    }

    /** Set up process for the Factory Floor
     *
     * @param Stations the number of Stations to be placed on the Factory Floor
     */
    public void setup(int Stations) {

        for (int i = 0; i < Constants.FACTORY_FLOOR_SIZE; i++) {
            for (int j = 0; j < Constants.FACTORY_FLOOR_SIZE; j++) {
                floor[i][j] = null;
            }
        }

        for (int i = 0; i < Stations; i++ ) { stations.add(new Station(this)); }

        fitnessScore = getFitnessScore();

        stations.forEach( (station -> {
            hashedStations.put(station.hashCode(), station) ;
        }));

    }

    /** Runnable interface method for Threads, starting point for individual Genetic Algorithm
     */
    @Override
    public void run() {

        currentGeneration = 0;

        while (currentGeneration < Constants.MAXIMUM_GENERATIONS) {

            while (Population.updatePause) {}
            System.out.println("Generation " + currentGeneration );
            for (int M = 0; M < Constants.MAXIMUM_MUTATIONS; M++) { mutation(); }

            // Compute Fitness
            fitnessScore = this.getFitnessScore();

            Station[][] sentChunk = this.getChunk();
            Station[][] receivedChunk = null;

            try {
                receivedChunk = exchanger.exchange(sentChunk, 5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }

            if (receivedChunk != null) {

                FactoryFloor newFloor = new FactoryFloor(new ArrayList<>(stations), this.floor.clone());

                newFloor.updateChunk(receivedChunk);

                if (newFloor.getFitnessScore() > this.getFitnessScore()) {
                    this.stations = new ArrayList<>(newFloor.stations);
                    this.floor = newFloor.floor.clone();
                }

            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.fitnessScore = getFitnessScore();
            currentGeneration++;
        }
    }

    /** Gets the mapped floor
     *
     * @return the mapped floor containing all of the Stations at their X, Y coordinates
     */
    public Station[][] getFloor() { return floor; }

    /** gets the list of Stations
     *
     * @return List of Stations on the Factory Floor
     */
    public List<Station> getStations() { return stations; }

    /** Computes the Fitness Score for this Factory Floor
     *
     * @return the Fitness Score
     */
    public double getFitnessScore() {
        HashMap<Long, Double> scores = new HashMap<>();

        AtomicDouble totalScore = new AtomicDouble(0);

        for (Station station_N : stations) {
            for (Station station_M : stations) {

                if (station_N.hashCode() == station_M.hashCode())
                    continue;

                long hashCode = station_N.hashCode() * station_M.hashCode();

                if (! scores.containsKey(hashCode)) {
                    scores.put(
                            hashCode,
                            station_N.getStationsScore(station_M)
                    );
                }
            }
        }
        scores.forEach( (hashcode, score) -> totalScore.getAndAdd(score) );

        return totalScore.get() / scores.size();
    }

    /** String Representation of the Factory Floor
     *
     * @return String Representation of the Factory Floor
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Station station : stations) {
            sb.append(station.toString()).append("\t");
        }

        sb.append("\n Factory Floor ").append(hashCode()).append("\t Fitness Score: ").append(this.fitnessScore);
        return sb.toString();
    }

    /** Gets a Random chunk on the Factory Floor for thread exchanging purposes
     *
     * @return A Random chunk (Section 1/4 of the entire floor)
     */
    private Station[][] getChunk() {
        float chunkChance = new Random().nextFloat();


        int chunkSize = (int)( 0.50 * Constants.FACTORY_FLOOR_SIZE);

        Station[][] chunk = new Station[Constants.FACTORY_FLOOR_SIZE][Constants.FACTORY_FLOOR_SIZE];

        // Top left chunk
        if (chunkChance <= 0.25f) {
            for (int i = 0; i < chunkSize; i++) {
                for (int j = 0; j < chunkSize; j++) {
                    chunk[i][j] = floor[i][j];
                }
            }
        }
        // top right chunk
        else if (chunkChance > 0.25f && chunkChance <= 0.5f) {
            for (int i = chunkSize; i < Constants.FACTORY_FLOOR_SIZE; i++) {
                for (int j = 0; j < chunkSize; j++) {
                    chunk[i][j] = floor[i][j];
                }
            }
        }
        // bottom left
        else if (chunkChance > 0.5f && chunkChance <= 0.75f) {
            for (int i = 0; i < chunkSize; i++) {
                for (int j = chunkSize; j < Constants.FACTORY_FLOOR_SIZE; j++) {
                    chunk[i][j] = floor[i][j];
                }
            }
        }
        // botom right
        else if(chunkChance > 0.75f && chunkChance <= 1.0f) {
            for (int i = chunkSize; i < Constants.FACTORY_FLOOR_SIZE; i++) {
                for (int j = chunkSize; j < Constants.FACTORY_FLOOR_SIZE; j++) {
                    chunk[i][j] = floor[i][j];
                }
            }
        }
        return chunk;
    }

    /** Mutates the properties of the Factory Floor for Genetic Algorithm Optimization
     */
    public void mutation() {
        try {
            float chanceOfMutation = ThreadLocalRandom.current().nextFloat();
            ThreadLocalRandom randomProperty = ThreadLocalRandom.current();


            if (chanceOfMutation <= Constants.P_NONE)
                return;

            int stationIndex = ThreadLocalRandom.current().nextInt(stations.size());
            Station station = stations.get(stationIndex);
            station.setFactoryFloor(this);

            if (chanceOfMutation > Constants.P_NONE && chanceOfMutation <= Constants.P_NONE + Constants.P_X) {
                int randomX = randomProperty.nextInt(Constants.FACTORY_FLOOR_SIZE);
                if (floor[randomX][station.getY()] == null) {
                    floor[station.getX()][station.getY()] = null;
                    station.setX(randomX);
                    floor[station.getX()][station.getY()] = station;
                }
            } else if (chanceOfMutation > Constants.P_NONE + Constants.P_X && chanceOfMutation <= Constants.P_NONE + 2 * Constants.P_Y) {
                int randomY = randomProperty.nextInt(Constants.FACTORY_FLOOR_SIZE);
                if (floor[station.getX()][randomY] == null) {
                    floor[station.getX()][station.getY()] = null;
                    station.setY(randomY);
                    floor[station.getX()][station.getY()] = station;
                }
            } else if (chanceOfMutation > Constants.P_NONE + 2 * Constants.P_Y && chanceOfMutation <= Constants.P_NONE + 3 * Constants.P_F) {
                station.setF(randomProperty.nextInt(Constants.STATION_FLAVORS));
            } else if (chanceOfMutation > Constants.P_NONE + 3 * Constants.P_F && chanceOfMutation <= Constants.P_NONE + (3 * Constants.P_F) + Constants.P_ALL) {
                int randomX = randomProperty.nextInt(Constants.FACTORY_FLOOR_SIZE);
                int randomY = randomProperty.nextInt(Constants.FACTORY_FLOOR_SIZE);
                int randomF = randomProperty.nextInt(Constants.STATION_FLAVORS);
                if (floor[randomX][randomY] == null) {
                    floor[station.getX()][station.getY()] = null;
                    station.setXYF(randomX, randomY, randomF);
                    floor[station.getX()][station.getY()] = station;
                }
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    /** Replaces a given chunk on the Factory Floor
     *
     * @param chunk A exchanged chunk from another Factory Floor thread
     */
    private void updateChunk(Station[][] chunk) {

        floor = chunk;
        List<Station> newStationList = new ArrayList<>();
        int availableSpots = Constants.MAXIMUM_STATIONS;

        for (int i = 0; i < Constants.FACTORY_FLOOR_SIZE; i++) {
            for (int j = 0; j < Constants.FACTORY_FLOOR_SIZE; j++) {
                if (floor[i][j] != null) {
                    Station station = floor[i][j];
                    Station newStation = new Station(station.getX(), station.getY(), station.getFlavor());
                    newStationList.add(newStation);
                    floor[i][j] = newStation;
                    availableSpots --;
                }
            }
        }

        while (availableSpots > 0) {

            boolean stationPicked = false;

            while (!stationPicked) {
                Station pickedStation = stations.get(new Random().nextInt(stations.size()));
                Station newStation = new Station(pickedStation.getX(), pickedStation.getY(), pickedStation.getFlavor());
                if (floor[pickedStation.getX()][pickedStation.getY()] == null) {
                    stationPicked = true;
                    newStationList.add(newStation);
                    stations.remove(pickedStation);
                }
            }

            availableSpots--;
        }
        stations = newStationList;
    }

    /** Get the current generation for offspring Factory Floor
     *
     * @return Current Genetic Algorithm Generation
     */
    public int getCurrentGeneration() { return this.currentGeneration; }
}
