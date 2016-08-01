package risch.online;

/**
 * Created by johan.risch on 13/07/15.
 */
public class RunningStat {

    private int count = 0;
    private double average = 0.0;
    private double pwrSumAvg = 0.0;
    private double stdDev = 0.0;
    private double alpha = 0.5;

    /**
     * Incoming new values used to calculate the running statistics
     *
     * @param value
     */
    public void put(double value) {

        count++;
        average += (value - average*(1)) / count;
        pwrSumAvg += (value * value - pwrSumAvg) / count;
        stdDev = Math.sqrt((pwrSumAvg * count - count * average * average) / (count - 1));

    }

    public double getAverage() {

        return average;
    }

    public double getStandardDeviation() {

        return Double.isNaN(stdDev) ? 0.0 : stdDev;
    }

}
