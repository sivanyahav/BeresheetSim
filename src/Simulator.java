import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Simulator {
    Beresheet bs;
    static double lastALT;
    static double lastHS;
    static double lastVS;
    static int x;
    static int y;

    FileWriter fileWriter = new FileWriter("log.txt");
    PrintWriter outfile = new PrintWriter(fileWriter);

    public Simulator() throws IOException {
        bs = new Beresheet();
        x = (int) bs.getLocation().x;
        y = (int) bs.getLocation().y;

        bs.print(fileWriter, outfile);

        loop();

        double fuelPercentage = 100 * (bs.getFuel() / 121);
        bs.printFuel(fuelPercentage, outfile);
        outfile.close();
    }



    public void loop() {

        lastALT = bs.getAlt();
        lastHS = bs.getHS();

        while (bs.getAlt() > Moon.realDestinationPoint.y && bs.getLat() < Moon.realDestinationPoint.x) {

            if (bs.getTime() % 10 == 0 || bs.getAlt() < 100) {
                bs.print(fileWriter, outfile);
            }

            bs.NNControl();
            bs.updateEngines();

            // main computations
            double ang_rad = Math.toRadians(bs.getAng());
            double h_acc = Math.sin(ang_rad) * bs.getAcc();
            double v_acc = Math.cos(ang_rad) * bs.getAcc();
            double vacc = Moon.getAcc(bs.getHS());

            bs.timer();

            //update fuel remaining
            bs.fuelControl();
            v_acc -= vacc;

            //update speed
            bs.speedControl(h_acc, v_acc);

            //update spaceship location
            bs.loactionUpdate();

            if ((lastALT - bs.getAlt() > 1000) && (lastHS - bs.getHS() > 60)) {

                y = (int) (bs.getLocation().y);
                x = (int) (bs.getLocation().x);

                bs.setLocation(x, y);

                lastHS = bs.getHS();
                lastALT = bs.getAlt();
            }

            else if (bs.getAlt() < 1000) {
                y = (int) (bs.getLocation().y + 1);
                bs.setLocation(bs.getLocation().x, y);
            }

        }

    }

    public static void main(String[] args) throws IOException {

        new Simulator();

    }

}
