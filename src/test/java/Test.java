

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import wContour.Interpolate;

public class Test {

    public static void main(String[] args) throws IOException {
        File directory = new File(".");
        String fn = null;
        try {
            fn = directory.getCanonicalPath();
        } catch (IOException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        fn = fn + File.separator + "sample" + File.separator + "Temp_2010101420.csv";
        FileReader reader = new FileReader(fn);
        BufferedReader input = new BufferedReader(reader);
        input.readLine();

        String line = null;
        List<double[]> stationList = new ArrayList<>();
        double xmin = Float.MAX_VALUE;
        double xmax = -Float.MAX_VALUE;
        double ymin = Float.MAX_VALUE;
        double ymax = -Float.MAX_VALUE;
        while ((line = input.readLine()) != null) {
            String[] numbers = line.split(",");
            double x = Double.parseDouble(numbers[1]);
            double y = Double.parseDouble(numbers[2]);
            double val = Double.parseDouble(numbers[3]);
            if (x < xmin) {
                xmin = x;
            }
            if (x > xmax) {
                xmax = x;
            }
            if (y < ymin) {
                ymin = y;
            }
            if (y > ymax) {
                ymax = y;
            }
            stationList.add(new double[]{x, y, val});
        }
        input.close();

        double[][] stationData = new double[stationList.size()][];
        for (int i = 0; i < stationList.size(); i++) {
            stationData[i] = stationList.get(i);
        }
        //sample KDTree��
        int sample = 200;
        double[] X = new double[sample];
        double[] Y = new double[sample];
        double xstep = (xmax - xmin) / sample;
        double ystep = (ymax - ymin) / sample;
        for (int i = 0; i < sample; i++) {
            X[i] = xmin + xstep * i;
            Y[i] = ymin + ystep * i;
        }
        long st = System.currentTimeMillis();
        double[][] res = Interpolate.cressman_kdTree(stationData, X, Y, Double.NaN);
        long ed = System.currentTimeMillis();
        System.out.println("time: " + (ed - st));
    }
}
