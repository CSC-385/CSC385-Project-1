package F21.CSC385.Project1.GeneticAlgorithm;

import F21.CSC385.Project1.GeneticAlgorithm.Util.Constants;
import java.util.Random;

/** Representation of a Station that could be placed on a Factory Floor
 * @author Gregory Maldonado
 */
public class Station {

    /** The Factory Floor the Station is placed on */
    private FactoryFloor factoryFloor;

    private int flavor;
    private int x, y;

    /** Creates a Station given a Factory Floor to be placed on
     *
     * @param factoryFloor the Factory Floor the Station is placed on
     */
    public Station(FactoryFloor factoryFloor) {
        this.factoryFloor = factoryFloor;
        boolean availableLocation;

        Random rd = new Random();
        // Finds a random x, y coordinate that is not already occupied by another station
        do {
            this.x = rd.nextInt(Constants.FACTORY_FLOOR_SIZE);
            this.y = rd.nextInt(Constants.FACTORY_FLOOR_SIZE);

            availableLocation = checkFloorAvailability(x, y);
        } while (!availableLocation);   // Checks if given x, y is taken and sets the Factory Floor grid to this Station
        factoryFloor.getFloor()[x][y] = this;
        this.flavor = rd.nextInt(Constants.STATION_FLAVORS) + 1;

    }

    /** Creates a Station with given x, y, flavor. Made for making copies for only thread safe exchanging
     *
     * @param x the X location on the Factory Floor
     * @param y the Y location on the Factory Floor
     * @param flavor the flavor of the station
     */
    public Station(int x, int y, int flavor) {
        this.x = x;
        this.y = y;
        this.flavor = flavor;
    }

    /** Gets the X coordinate
     *
     * @return the X coordinate of the Station
     */
    public int getX() { return x; }

    /** Gets the Y coordinate
     *
     * @return the Y coordinate of the Station
     */
    public int getY() { return y; }

    /** Gets the Flavor of the Station
     *
     * @return the Flavor of the Station
     */
    public int getFlavor() { return flavor; }

    /** Sets a host Factory Floor for this Station
     *
     * @param factoryFloor host Factory Floor
     */
    public void setFactoryFloor(FactoryFloor factoryFloor) { this.factoryFloor = factoryFloor; }

    /** Sets the X coordinate of the Station if not occupied
     *
     * @param x new X coordinate for the Station
     */
    public void setX(int x) {
        if (checkFloorAvailability(x, this.y))
            this.x = x;
    }

    /** Sets the Y coordinate of the Station if not occupied
     *
     * @param y new Y coordinate for the Station
     */
    public void setY(int y) {
        if (checkFloorAvailability(this.x, y))
            this.y = y;
    }

    /** Sets the Flavor of the Station
     *
     * @param f new Flavor for the Station
     */
    public void setF(int f) { this.flavor = f; }

    /** Sets x, y, f all at the same time, made for thread safe copies
     *
     * @param x new X coordinate for the Station
     * @param y new Y coordinate for the Station
     * @param f new Flavor coordinate for the Station
     */
    public void setXYF(int x, int y, int f) { setX(x); setY(y); setF(f); }

    /** Returns a string representation of the Station
     *
     * @return String representation of the Station
     */
    public String toString() {
        return "(" + x + "," + y + ")" + "\t" + flavor;
    }

    /** Checks if another station is already occupying a location on the host Factory Floor
     *
     * @param x this Station x coordinate
     * @param y this Station y coordinate
     * @return if host Factory Floor has an available location at x, y
     */
    public boolean checkFloorAvailability(int x, int y) { return factoryFloor.getFloor()[x][y] == null; }

    /** Gets the Euclidean distance between this Station and another Station
     *
     * @param otherStation A Station some distance from this Station
     * @return The Euclidean distance between the two stations
     */
    public double getDistance(Station otherStation) {
        return Math.sqrt( (this.x - otherStation.x) * (this.x - otherStation.x) + (this.y - otherStation.y) * (this.y - otherStation.y) );
    }

    /** Gets the numerical difference of flavors between this Station and another Station
     *
     * @param otherStation A Station placed on the same host Factory Floor
     * @return Numerical difference of flavors between the two stations
     */
    public int getFlavorDifference(Station otherStation) {
        return Math.abs( this.flavor - otherStation.flavor );
    }

    /** Calculates a numerical score to measure how effect two Stations are
     *
     * @param otherStation A Station placed on the same host Factory Floor
     * @return A numerical score to measure how effect two Stations are
     */
    public double getStationsScore(Station otherStation) {
        int flavorDifference = getFlavorDifference(otherStation);
        int HalfOfFlavors = (int) Math.floor(Constants.STATION_FLAVORS);

        return (flavorDifference < HalfOfFlavors) ?
                Math.cos( getDistance(otherStation) / (Constants.FACTORY_FLOOR_SIZE * Math.sqrt(2)) ) :
                Math.sin( getDistance(otherStation) / (Constants.FACTORY_FLOOR_SIZE * Math.sqrt(2)) ) ;

    }

}
