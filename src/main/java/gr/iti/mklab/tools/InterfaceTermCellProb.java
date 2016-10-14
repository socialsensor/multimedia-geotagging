package gr.iti.mklab.tools;

/**
 * Interface of tag-cell probability calculator 
 * @author gkordo
 *
 */
public interface InterfaceTermCellProb {

	/**
	 * Function where the tag-cell probabilities are calculated and stored in a defined file.
	 * @param dir : directory of the project
	 * @param trainFile : file that contains the train set
	 * @param outFile : output file
	 * @param scale : grid scale
	 * @throws Exception
	 */
	public void calculatorTermCellProb(String dir, String trainFile, String outFile, int scale) throws Exception;
}
