public class PID {

    private double P, I, D;
    private double lastError, sumIntegral;
    private int max;

    public PID(double P, double I, double D, int max) {
        this.P = P;
        this.I = I;
        this.D = D;
        this.lastError = 0;
        this.sumIntegral = 0;
        this.max = max;
    }

    public double control(double dt, double error) {
        this.sumIntegral += this.I * error * dt;
        double difference = (error - lastError) / dt;
        double constIntegral = (this.sumIntegral <= max) ? this.sumIntegral : max;
        double output = this.P * error + this.D * difference + constIntegral;
        this.lastError = error;
        return output;
    }


    public static double constrain(double x) {
        x = x > 1 ? 1 : x;
        return x < 0 ? 0 : x;
    }

}