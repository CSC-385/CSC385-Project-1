package F21.CSC385.Project1.GeneticAlgorithm;

import F21.CSC385.Project1.GeneticAlgorithm.Util.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.Test;

public class StationTest {
    private Station station = new Station(0, 0, 0);

    @Test
    public void test_toString() {
        Assertions.assertEquals("(0,0)\t0", station.toString());
    }

    @Test
    public void test_checkFloorAvailability() {
        FactoryFloor factoryFloor = new FactoryFloor();
        station.setFactoryFloor(factoryFloor);
        Assertions.assertTrue(station.checkFloorAvailability(station.getX(), station.getY()));
    }

    @Test
    public void test_getDistance() {
        Station otherStation = new Station(0, 1, 0);
        Assertions.assertEquals(1, station.getDistance(otherStation));
    }

    @Test
    public void test_getFlavorDifference() {
        Station otherStation = new Station(0, 1, 1);
        Assertions.assertEquals(1, station.getFlavorDifference(otherStation));
    }

    @Test
    public void test_getStationScore() {
        Station otherStation = new Station(0, 1, 0);
        Assertions.assertEquals(Math.cos( 1 / (Constants.FACTORY_FLOOR_SIZE * Math.sqrt(2)) ), station.getStationsScore(otherStation) );
    }



}
