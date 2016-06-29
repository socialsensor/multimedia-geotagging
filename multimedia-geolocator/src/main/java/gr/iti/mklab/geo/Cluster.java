package gr.iti.mklab.geo;

import java.util.HashSet;
import java.util.Set;

import gr.iti.mklab.util.GeoCellCoder;

/**
 * Class that implements the cell clusters.
 * @author gkordo
 *
 */
public class Cluster {
	
	/**
	 * Set of neighbor cells contained in cluster
	 */
	private Set<GeoCell> cells = new HashSet<GeoCell>();

	public Cluster(GeoCell cell){
		this.cells.add(cell);
	}

	/**
	 * Function that indicated whether a tested cell belongs in the cluster
	 * @param testCell : under investigation cell
	 * @param margin : margin that indicated the neighbor cells
	 * @return
	 */
	public boolean isInNeighboorhood(GeoCell testCell, double margin){
		boolean flag = false;
		for(GeoCell cell:cells){
			if(isNeighboorWith(testCell, cell, margin)){
				cells.add(testCell);
				flag = true;
				break;
			}
		}
		return flag;
	}

	/**
	 * Function that determine whether two cell are neighbors
	 * @param testCell : under investigation cell
	 * @param clusterCell : cluster cell
	 * @param margin : margin that indicated the neighbor cells
	 * @return
	 */
	public boolean isNeighboorWith(GeoCell testCell, GeoCell clusterCell, double margin) {

		double[] cellC = GeoCellCoder.cellDecoding(clusterCell.getID());
		double[] cellT = GeoCellCoder.cellDecoding(testCell.getID());

		if((cellT[0]>=(cellC[0]-margin))&&(cellT[0]<=(cellC[0]+margin))
				&&(cellT[1]>=(cellC[1]-margin))&&(cellT[1]<=(cellC[1]+margin))){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Function that returns the size of the cluster
	 * @return
	 */
	public int size(){
		return cells.size();
	}

	/**
	 * Function that returns the representative cell of the cluster
	 * @return
	 */
	public Long getRepresentativeCell(){
		Long maxID = null;
		double maxProb = 0;
		for(GeoCell cell:cells){
			if(cell.getTotalProb()>maxProb){
				maxID = cell.getID();
				maxProb = cell.getTotalProb();
			}
		}
		return maxID;
	}

	/**
	 * Function that returns the total cluster probability
	 * @return
	 */
	public double getTotalClusterProb(){

		double totalProb = 0.0;

		for(GeoCell cell:cells){
			totalProb += cell.getTotalProb();
		}

		return totalProb;
	}

	public String toString(){
		String out = "";
		for(GeoCell cell:cells){
			out += cell.getID()+",";
		}
		return out.substring(0,out.length()-1);
	}
}
