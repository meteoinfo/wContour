package wContour;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {

	public static void main(String[] args) throws IOException {
		FileReader reader = new FileReader("F:\\JavaWorkSpace\\wContourDemo\\sample\\Temp_2010101420.csv");
		BufferedReader input =  new BufferedReader(reader);
		input.readLine();
		
		String line = null;
		List<double[]> stationList = new ArrayList<double[]>();
		double xmin = Float.MAX_VALUE;
		double xmax = -Float.MAX_VALUE;
		double ymin = Float.MAX_VALUE;
		double ymax = -Float.MAX_VALUE;
		while((line = input.readLine()) != null){
			String[] numbers = line.split(",");
			double x = Double.parseDouble(numbers[1]);
			double y = Double.parseDouble(numbers[2]);
			double val = Double.parseDouble(numbers[3]);
			if(x < xmin)	xmin = x;
			if(x > xmax)	xmax = x;
			if(y < ymin)	ymin = y;
			if(y > ymax)	ymax = y;
			stationList.add(new double[]{x,y,val});
		}
		input.close();
		
		double[][] stationData = new double[stationList.size()][];
		for(int i = 0; i < stationList.size(); i++)
			stationData[i] = stationList.get(i);
		//sample 越大，kdTree越有效；
		int sample = 200;
		double[] X = new double[sample];
		double[] Y = new double[sample];
		double xstep = (xmax - xmin)/sample;
		double ystep = (ymax - ymin)/sample;
		for(int i = 0; i < sample; i++){
			X[i] = xmin + xstep*i;
			Y[i] = ymin + ystep*i;
		}
		long st = System.currentTimeMillis();
		double[][] res = Interpolate.cressman_kdTree(stationData, X, Y, Double.NaN);
		long ed = System.currentTimeMillis();
		System.out.println("time: " + (ed - st));
	}
}
