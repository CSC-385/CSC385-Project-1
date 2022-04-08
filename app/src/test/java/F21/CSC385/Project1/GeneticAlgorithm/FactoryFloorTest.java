package F21.CSC385.Project1.GeneticAlgorithm;

import F21.CSC385.Project1.GeneticAlgorithm.Util.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FactoryFloorTest {

    List<Station> stations = new ArrayList<>();
    FactoryFloor factoryFloor = new FactoryFloor(stations, new Station[Constants.FACTORY_FLOOR_SIZE][Constants.FACTORY_FLOOR_SIZE]);

    @Test
    public void test_getStations() {
        stations.add(new Station(0, 0, 1));
        Assertions.assertEquals(1, factoryFloor.getStations().size());
        stations.remove(0);
    }

    @Test
    public void test_getFitnessScore() {
        stations.add(new Station(0, 0, 0));
        stations.add(new Station(0, 1, 0));
        Assertions.assertEquals(Math.cos( 1 / (Constants.FACTORY_FLOOR_SIZE * Math.sqrt(2)) ), factoryFloor.getFitnessScore());
        stations.remove(0); stations.remove(0);
    }

    @Test
    public void test_getFloor() {
        Assertions.assertEquals(Constants.FACTORY_FLOOR_SIZE, factoryFloor.getFloor().length);
    }

}
