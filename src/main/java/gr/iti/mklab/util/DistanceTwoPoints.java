package gr.iti.mklab.util;

public class DistanceTwoPoints {
	
	public static double computeDistace(Double[] point1, Double[] point2){
		int R = 6371;
		double d2r = (Math.PI / 180D), dlong, dlat,a, c, d;

		dlong = (point2[1] - point1[1]) * d2r;
		dlat = (point2[0] - point1[0]) * d2r;
		
		a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(point2[0] * d2r) 
				* Math.cos(point1[0] * d2r) * Math.pow(Math.sin(dlong / 2), 2);
		c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		d = R * c;

		return d;
	}
}
