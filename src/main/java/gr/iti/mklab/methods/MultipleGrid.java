package gr.iti.mklab.methods;

import gr.iti.mklab.util.EasyBufferedReader;
import gr.iti.mklab.util.EasyBufferedWriter;

import org.apache.log4j.Logger;

/**
 * The implementation of the Internal Grid technique
 * @author gkordo
 *
 */
public class MultipleGrid {

	static Logger logger = Logger.getLogger("gr.iti.mklab.method.InternalGrid");

	/**
	 * Method that perform the Internal Grid technique and takes the arguments for the similarity search 
	 * Class contractor
	 * @param dir : directory of the project
	 * @param resultFile : name of the output file
	 * @param resultCorserGrid : file with the estimated cells of the coarser grid
	 * @param resultCorserGrid : file with the estimated cells of the finer grid
	 */	
	public static void determinCellIDsForSS(String dir, String resultFile, String resultCorserGrid, String resultFinerGrid){

		// Initialize parameters
		EasyBufferedReader resultLMGCReader = new EasyBufferedReader(dir + resultCorserGrid);

		EasyBufferedReader resultLMGFReader = new EasyBufferedReader(dir + resultFinerGrid);

		EasyBufferedWriter writer = new EasyBufferedWriter(dir + resultFile);

		String inputRG2;
		String inputRG3;

		int count = 0;

		logger.info("apply Internal Grid Technique for every query image");

		while ((inputRG2=resultLMGCReader.readLine())!=null && (inputRG3=resultLMGFReader.readLine())!=null){

			count++;

			String cellID = deterimCellID(inputRG2.split(";")[1], inputRG3.split(";")[1]);

			if(!cellID.isEmpty()){
				writer.write(inputRG2.split(";")[0] + ";" + inputRG2.split(";")[1]
						+ ">" + cellID); // selected cell ID and the sell of the coarser granularity
			}else{
				writer.write(inputRG2.split(";")[0] + ";" + inputRG2.split(";")[1]
						+ ">" + inputRG2.split(";")[1]);
			}
			
			writer.newLine();
		}

		logger.info("refined cell estimation for " + count + " images");

		writer.close();
		resultLMGCReader.close();
		resultLMGFReader.close();
	}

	/**
	 * Method that determines the borders of the cell that similarity search will take place
	 * @param inputRG2 : estimated cell of the coarser grid
	 * @param inputRG3 : estimated cell of the finer grid
	 */
	private static String deterimCellID(String inputRG2, String inputRG3){

		String cellID = "";

		if (!inputRG2.equals("N/A")){

			Double[] cellIDG2 = {Double.parseDouble(inputRG2.split("_")[0]),Double.parseDouble(inputRG2.split("_")[1])};
			cellID = inputRG2;

			if(!inputRG3.equals("N/A")){
				Double[] cellIDG3 = {Double.parseDouble(inputRG3.split("_")[0]),Double.parseDouble(inputRG3.split("_")[1])};

				if(cellIDG3[0]>=(cellIDG2[0]-0.005) && cellIDG3[0]<=(cellIDG2[0]+0.005)
						&& cellIDG3[1]>=(cellIDG2[1]-0.005) && cellIDG3[1]<=(cellIDG2[1]+0.005)){ // check whether the estimated cell of the finer grid laying inside the borders of the estimated cell of the coarser grid
					cellID = inputRG3;
				}
			}
		}

		return cellID;
	}

}
