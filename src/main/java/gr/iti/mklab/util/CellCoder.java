package gr.iti.mklab.util;

/**
 * In order to allocate as little as possible memory a coding system for cell identification is been applied.
 * Since long variables allocate less memory, it is more preferable to be used over string.
 * @author gkordo
 *
 */
public class CellCoder {

	public static long cellEncoding(String cellID){

		long cellCode = 0;
		cellCode = (long) (Math.abs(Double.parseDouble(cellID.split("_")[0]))*1000000+Math.abs(Double.parseDouble(cellID.split("_")[1]))*100);
		if(Double.parseDouble(cellID.split("_")[0])<0){
			cellCode+=10000000000L;
		}
		if(Double.parseDouble(cellID.split("_")[1])<0){
			cellCode+=1000000000L;
		}
		return cellCode;
	}

	public static double[] cellDecoding(long cellCode){

		double[] coord = new double[2];
		coord[0] = cellCode%10000/100.0;
		if(cellCode/1000000000L==1||cellCode/1000000000L==11){
			coord[0]*=-1;
		}

		coord[1] = cellCode/10000%100000/100.0;
		if(cellCode/10000000000L==1){
			coord[1]*=-1;
		}

		return coord;
	}
}
