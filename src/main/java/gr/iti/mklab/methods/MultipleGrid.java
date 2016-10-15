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
	 * Method that perform the Multiple Grid technique and generates
	 * the arguments for the similarity search Class contractor
	 * @param dir : directory of the project
	 * @param resultFile : name of the output file
	 * @param resultCorserGrid : file with the estimated cells of the coarser grid
	 * @param resultFinerGrid : file with the estimated cells of the finer grid
	 */	
	public static void determinCellIDsForSS(String dir, String resultFile,
			String resultCorserGrid, String resultFinerGrid){

		logger.info("Process: Multiple Grid Technique\t|\t"
				+ "Status: INITIALIZE");
		// Initialize parameters
		EasyBufferedReader resultLMGCReader = new EasyBufferedReader(dir + resultCorserGrid);
		EasyBufferedReader resultLMGFReader = new EasyBufferedReader(dir + resultFinerGrid);
		EasyBufferedWriter writer = new EasyBufferedWriter(dir + resultFile);

		String corseMLC;
		String fineMLC;

		logger.info("Process: Multiple Grid Technique\t|\t"
				+ "Status: STARTED");

		while ((corseMLC=resultLMGCReader.readLine())!=null 
				&& (fineMLC=resultLMGFReader.readLine())!=null){
			
			if(!corseMLC.split("\t")[1].equals("N/A")){
				String mlc = deterimBoarders(corseMLC.split("\t")[1], fineMLC.split("\t")[1]);
				if(!mlc.isEmpty()){
					writer.write(corseMLC.split("\t")[0] + "\t" + corseMLC.split("\t")[1]
							+ ":" + mlc); // selected cell ID and the sell of the coarser granularity
				}else{
					writer.write(corseMLC.split("\t")[0] + "\t" + corseMLC.split("\t")[1]
							+ ":" + corseMLC.split("\t")[1]);
				}
				writer.newLine();
			} else{
				writer.write(corseMLC.split("\t")[0] + "\tN/A");
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
	 * @param corseMLC : estimated cell of the coarser grid
	 * @param fineMLC : estimated cell of the finer grid
	 */
	private static String deterimBoarders(String corseMLC, String fineMLC){

		String mlc = corseMLC;

		if (!corseMLC.equals("N/A")){
			Double[] corseLatLon = {Double.parseDouble(corseMLC.split("_")[0]),
					Double.parseDouble(corseMLC.split("_")[1])};

			if(!fineMLC.equals("N/A")){
				Double[] fineLatLon = {Double.parseDouble(fineMLC.split("_")[0]),
						Double.parseDouble(fineMLC.split("_")[1])};

				// check whether the estimated cell of the finer grid laying 
				// inside the borders of the estimated cell of the coarser grid
				if(fineLatLon[0]>=(corseLatLon[0]-0.005) 
						&& fineLatLon[0]<=(corseLatLon[0]+0.005)
						&& fineLatLon[1]>=(corseLatLon[1]-0.005) 
						&& fineLatLon[1]<=(corseLatLon[1]+0.005)){
					mlc = fineMLC;
				}
			}
		}

		return mlc;
	}

}
