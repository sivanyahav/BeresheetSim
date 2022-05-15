import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

public class Beresheet {
    public final double WEIGHT_EMP = 165; // kg
    public final double WEIGHT_FULE = 420; // kg
    public final double WEIGHT_FULL = WEIGHT_EMP + WEIGHT_FULE; // kg
    public final static double MAIN_ENG_F = 430; // N
    public final static double SECOND_ENG_F = 25; // N
    public final static double MAIN_BURN = 0.15; // liter per sec, 12 liter per m'
    public final static double SECOND_BURN = 0.009; // liter per sec 0.6 liter per m'
    public static final double ALL_BURN = MAIN_BURN + 8 * SECOND_BURN;

    private double vs;
    private double hs;
    private double dist;
    private double ang;
    private double alt;
    private double lat;
    private double time;
    private double dt;
    private double acc; // Acceleration rate (m/s^2)
    private double fuel;
    private double weight;
    private double NN; // rate[0,1]
    private boolean first = true;

    private PID pid;
    private Point location;

    public Beresheet() {
        vs = 24.8;
        hs = 932;
        dist = 181 * 1000;
        ang = 58.3;
        alt = 13748; // 30 k"m
        lat = 0;
        time = 0;
        dt = 1;
        acc = 0;
        fuel = 121;
        weight = WEIGHT_EMP + fuel;

        pid = new PID(0.7, 1, 0.01, 1);
        NN = 0.7;

        location = new Point(0, 100);

    }

    public double getVS() {
        return vs;
    }

    public double getHS() {
        return hs;
    }

    public double getDist() {
        return dist;
    }

    public double getAng() {
        return ang;
    }

    public double getAlt() {
        return alt;
    }

    public double getTime() {
        return time;
    }

    public double getDT() {
        return dt;
    }

    public double getAcc() {
        return acc;
    }

    public double getFuel() {
        return fuel;
    }

    public double getWeight() {
        return weight;
    }

    public double getNN() {
        return NN;
    }

    public Point getLocation() {
        return location;
    }

    public void setVS(double vs) {
        this.vs = vs;
    }

    public void setHS(double hs) {
        this.hs = hs;
    }

    public void setDist(double d) {
        this.dist = d;
    }

    public void setAng(double a) {
        this.ang = a;
    }

    public void setAlt(double alt) {
        this.alt = alt;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setTime(double t) {
        this.time = t;
    }

    public void setDT(double dt) {
        this.dt = dt;
    }

    public void setAcc(double acc) {
        this.acc = acc;
    }

    public void setFuel(double f) {
        this.fuel = f;
    }

    public void setWeight(double w) {
        this.weight = w;
    }

    public void setNN(double nn) {
        this.NN = nn;
    }

    public void setLocation(double x, double y) {
        this.location.x = x;
        this.location.y = y;
    }
    private double accMax(double weight){
       double force = MAIN_ENG_F + SECOND_ENG_F*8;
       return force/weight;
    }

    public void updateEngines() {
        if (alt < 2000 && ang > 0) {
            ang -= 3 * dt;
            if (ang < 1) {
                ang = 0;
            }
        }
    }

    public void timer() {
        time = (time + dt);
    }

    public void loactionUpdate() {
        alt = (alt - dt * vs); // y
        lat = (lat + dt * hs); // x
        dist = (new Point(lat, alt).distance2D(Moon.realDestinationPoint));
    }

    public void speedControl(double h_acc, double v_acc) {
        if (hs > 0)
            hs = (hs - h_acc * dt) < 0 ? 0.1 : (hs - h_acc * dt);
        if (hs < 2 && alt <= 2000)
            hs = 0;
        vs = (vs - v_acc * dt) < 2 ? 0.3 : (vs - v_acc * dt);
        if (alt < 15 && vs > 2) {
            vs = (vs - 2) < 2 ? 0.3 : (vs - 2);
        }
    }

    public void fuelControl() {
        double dw = dt * Beresheet.ALL_BURN * NN; // Difference weight
        if (fuel > 0) {
            fuel -= dw;
            weight = (WEIGHT_EMP + fuel);
            acc = (NN * accMax(weight));
        } else { // ran out of fuel
            acc = 0;
        }
    }

    public void NNControl() {
        if (alt > 2000) {
            if (vs > 25) {
                NN = PID.constrain(NN + 0.003 * dt);
            }
            if (vs < 20) {
                NN = PID.constrain(NN - 0.003 * dt);
            }
        } else {
            NN = PID.constrain(pid.control(dt, 0.5 - NN));

            if (alt < 125) {
                NN = 1;
                if (vs < 5) {
                    NN = 0.7;
                }
            }
            if (alt < 5) {
                NN = 0.38;
            }
        }
    }

    public void print(FileWriter fileWriter, PrintWriter outfile){
        if(first){
            printAndLog("Simulating Bereshit's Landing:\n", System.out, outfile);
            String headers = String.format("%8s | %8s | %10s | %15s | %15s | %8s | %10s | %7s | %7s\n",
                    "time", "vs", "hs", "dist", "alt", "ang", "weight", "acc", "fuel");
            printAndLog(headers, System.out, outfile);
            printAndLog("------------------------------------------------------------------------------------------------------------------\n", System.out, outfile);
            first = false;
        }
        else{
            String data = String.format("%8s | %8s | %10s | %15s | %15s | %8s | %10s | %7s | %7s\n",
                    round(time), round(vs), round(hs), round(dist), round(alt), round(ang), round(weight), round(acc), round(fuel));
            printAndLog(data, System.out, outfile);
        }
    }

    public void printFuel(double fuelPercentage, PrintWriter outfile){
        String summary = "\nFinished simulation with " + round(fuel) + " litres of fuel which are "+
                round(fuelPercentage) + "% of the initial amount.";
        printAndLog(summary, System.out, outfile);

    }

    public static String round(double val)
    {
        return String.format("%.2f", val);
    }

    private static void printAndLog(final String msg, PrintStream out1, PrintWriter out2) {
        out1.println(msg);
        out2.write(msg);
    }
}
