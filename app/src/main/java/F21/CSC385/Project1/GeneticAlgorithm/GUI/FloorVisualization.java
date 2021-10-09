package F21.CSC385.Project1.GeneticAlgorithm.GUI;

import F21.CSC385.Project1.GeneticAlgorithm.FactoryFloor;
import F21.CSC385.Project1.GeneticAlgorithm.Util.Constants;
import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/** Graphical User Interface for the visualization of the best Factory Floor Plan
 * @author Gregory Maldonado
 */
public class FloorVisualization extends JFrame {

    private volatile FactoryFloor floor;
    private int spacing;
    private final int PADDING = 150;
    Graphics2D g2d;

    /** The setup process of the GUI
     */
    public FloorVisualization() {
        this.spacing = (Constants.WINDOW_SIZE - PADDING) / ( Constants.FACTORY_FLOOR_SIZE );


        setSize(Constants.WINDOW_SIZE, Constants.WINDOW_SIZE);
        setTitle("Best Floor Design");
        setVisible(true);
    }

    /** Paints each Station on the given Factory Floor
     *
     * @param g Graphics library used to make the elements on the GUI
     */
    @Override
    public void paint(Graphics g) {
        this.g2d = (Graphics2D) g;
        try {
            if (floor != null) {

                System.out.println(floor + " \t " + floor.getStations().size());
                g2d.setColor(Color.GREEN);

                floor.getStations().forEach((station) -> {
                    if (station != null) {
                        g2d.setColor(Constants.colors[station.getFlavor()]);
                        g2d.fillRect((station.getX() * spacing) + PADDING / 2, (station.getY() * spacing) + PADDING / 2, spacing, spacing);
                        g2d.setColor(Color.BLACK);
                        g2d.drawString(
                                "(" + station.getX() + ", " + station.getY() + ")",
                                (station.getX() * spacing + (spacing / 2)) + PADDING / 2 - 15,
                                (station.getY() * spacing + (spacing / 2)) + PADDING / 2
                        );
                    }
                });
                g2d.drawString(
                        "Generation: " + floor.getCurrentGeneration(),
                        50,
                        Constants.WINDOW_SIZE - 50
                );
                DecimalFormat df = new DecimalFormat("###.######");
                g2d.drawString("Fitness Score: " + df.format(floor.getFitnessScore()),
                        Constants.WINDOW_SIZE - 200,
                        Constants.WINDOW_SIZE - 50
                );
            }
        } catch (Exception e) {e.printStackTrace();}
        repaint();
    }

    /** Repaints the GUI on a timer interval for updates
     */
    @Override
    public void repaint() {
        super.repaint();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setBackground(Color.WHITE);
    }

    /** Sets the Factory Floor to be shown on the GUI
     *
     * @param floor Factory Floor to be shown on the GUI
     */
    public void setFloor(FactoryFloor floor) { this.floor = floor; }

}
