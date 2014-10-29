package gr.iti.mklab.method;

import java.util.Arrays;
import java.util.List;

public class GaussianDistribution {

	protected List<Double> p;
	protected double size, m, s;
	
	public GaussianDistribution(List<Double> p){
		this.p = p;
		this.size = p.size();
		
		this.m = getMean();
		this.s = getStdDev();
	}
	
	public double calculateGaussianDistribution(double x){
		
		double f = 0.0;
		
		f = 1/(s*Math.sqrt(2*Math.PI))*Math.pow(Math.E, -(Math.pow(x-m,2)/(2*Math.pow(s,2))));
		
		return f;
	}
	
	public double getMean()
    {
        double sum = 0.0;
        for(double a : p)
            sum += a;
        return sum/size;
    }

	public double getVariance()
    {
        double mean = getMean();
        double temp = 0;
        for(double a :p)
            temp += (mean-a)*(mean-a);
        return temp/size;
    }

	public double getStdDev()
    {
        return Math.sqrt(getVariance());
    }

    public double median() 
    {
       double[] b = new double[p.size()];
       System.arraycopy(p, 0, b, 0, b.length);
       Arrays.sort(b);

       if (p.size() % 2 == 0) 
       {
          return (b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0;
       } 
       else 
       {
          return b[b.length / 2];
       }
    }
}
