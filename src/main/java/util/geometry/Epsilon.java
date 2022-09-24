package util.geometry;

public class Epsilon {
    public static double epsilon = 1e-6;
    public static double deviation = 0.1f;
    public static double selectDist = 10f;
    public static boolean isZero(double v) {
        return Math.abs(v) < epsilon;
    }

    public static boolean same(double a, double b) {
        double delta = a-b;
        return (delta>0 ? delta:-delta)<epsilon;
    }

    public static boolean same(double a, double b, double eps) {
        double delta = a-b;
        return (delta>0 ? delta:-delta)<eps;
    }
}
