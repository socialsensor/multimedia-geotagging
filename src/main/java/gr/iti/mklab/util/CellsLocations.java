package gr.iti.mklab.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * For every cell, calculate the average of the locations of the images that are contained in it
 * @author gkordo
 *
 */
public class CellsLocations {

	private Map<String,Double[]> cellLocMap = new HashMap<String,Double[]>();
	private static Logger logger = Logger.getLogger("gr.iti.mklab.util.CellsLocations");
	
	public CellsLocations(String trainFile, int scale){	

		EasyBufferedReader reader = new EasyBufferedReader(trainFile);

		String input;

		int count = 0;
		
		logger.info("calculate the average location of images in every cell");
		Progress prog = new Progress(System.currentTimeMillis(),10,1, "calculate");

		while ((input= reader.readLine())!=null){
			
			prog.showProgress(count, System.currentTimeMillis());
			count++;

			BigDecimal tmpLonCenter = new BigDecimal(Double.parseDouble(input.split("\\s")[6])).setScale(scale, BigDecimal.ROUND_HALF_UP);
			BigDecimal tmpLatCenter = new BigDecimal(Double.parseDouble(input.split("\\s")[7])).setScale(scale, BigDecimal.ROUND_HALF_UP);

			String cell = String.valueOf(tmpLonCenter)+"_"+String.valueOf(tmpLatCenter);

			if(cellLocMap.containsKey(cell)){
				Double[] tmp = cellLocMap.get(cell);

				tmp[0] += Double.parseDouble(input.split("\\s")[6]);
				tmp[1] += Double.parseDouble(input.split("\\s")[7]);
				tmp[2]++;

				cellLocMap.put(cell,tmp);			
			}else{
				Double[] tmp = new Double[3];

				tmp[0] = Double.parseDouble(input.split("\\s")[6]);
				tmp[1] = Double.parseDouble(input.split("\\s")[7]);
				tmp[2] = 1.0;

				cellLocMap.put(cell,tmp);
			}
		}
		
		logger.info("cells' average locations calculated");
		reader.close();
	}

	public double[] getLargerCellLocation(){

		double max = 0.0;
		double[] maxLoc = new double[2];
		for(Entry<String, Double[]> entryCell: cellLocMap.entrySet()){
			Double[] location = cellLocMap.get(entryCell.getKey());
			
			if(max<location[2]){
				max=location[2];
				maxLoc[0]=location[0]/location[2];
				maxLoc[1]=location[1]/location[2];
			}
		}
		return maxLoc;
	}


	public void writeCellsLocationsInFile(String exportFile){

		EasyBufferedWriter writer = new EasyBufferedWriter(exportFile);

		for(Entry<String, Double[]> entryCell: cellLocMap.entrySet()){
			
			Double[] location = cellLocMap.get(entryCell.getKey());

			writer.write(entryCell.getKey()+" "+String.valueOf(location[0]/location[2])+" "+String.valueOf(location[1]/location[2]));
			writer.newLine();
		}
		writer.close();
	}

	public static Map<String,Double[]> loadCellsLocations(String cellsLocationFile){

		EasyBufferedReader reader = new EasyBufferedReader(cellsLocationFile);

		Map<String,Double[]> cellLocMapIn = new HashMap<String,Double[]>();

		String input , cell;

		while ((input= reader.readLine())!=null){
			Double[] loc = new Double[2];
			loc[0] = Double.parseDouble(input.split("\\s")[1]);
			loc[1] = Double.parseDouble(input.split("\\s")[2]);

			cell = input.split("\\s")[0];

			cellLocMapIn.put(cell,loc);
		}

		reader.close();
		return cellLocMapIn;
	}

}
