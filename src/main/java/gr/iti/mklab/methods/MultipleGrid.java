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
	public static void determinCellIDsForSS(String dir, String resultFile,
			String resultCorserGrid, String resultFinerGrid){

		logger.info("Process: Multiple Grid Technique\t|\t"
				+ "Status: INITIALIZE");
		// Initialize parameters
		EasyBufferedReader resultLMGCReader = new EasyBufferedReader(dir + resultCorserGrid);
		EasyBufferedReader resultLMGFReader = new EasyBufferedReader(dir + resultFinerGrid);
		EasyBufferedWriter writer = new EasyBufferedWriter(dir + resultFile);

		String inputRG2;
		String inputRG3;

		logger.info("Process: Multiple Grid Technique\t|\t"
				+ "Status: STARTED");

		while ((inputRG2=resultLMGCReader.readLine())!=null 
				&& (inputRG3=resultLMGFReader.readLine())!=null){
			if(!inputRG2.split("\t")[1].equals("N/A")){
				String cellID = deterimCellID(inputRG2.split("\t")[1], inputRG3.split("\t")[1]);
				if(!cellID.isEmpty()){
					writer.write(inputRG2.split("\t")[0] + "\t" + inputRG2.split("\t")[1]
							+ ":" + cellID); // selected cell ID and the sell of the coarser granularity
				}else{
					writer.write(inputRG2.split("\t")[0] + "\t" + inputRG2.split("\t")[1]
							+ ":" + inputRG2.split("\t")[1]);
				}
				writer.newLine();
			} else{
				writer.write(inputRG2.split("\t")[0] + "\tN/A");
			}
		}

		logger.info("Process: Multiple Grid Technique\t|\t"
				+ "Status: COMPLETED");

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

				// check whether the estimated cell of the finer grid laying inside the borders of the estimated cell of the coarser grid
				if(cellIDG3[0]>=(cellIDG2[0]-0.005) && cellIDG3[0]<=(cellIDG2[0]+0.005)
						&& cellIDG3[1]>=(cellIDG2[1]-0.005) && cellIDG3[1]<=(cellIDG2[1]+0.005)){
					cellID = inputRG3;
				}
			}
		}

		return cellID;
	}

}
