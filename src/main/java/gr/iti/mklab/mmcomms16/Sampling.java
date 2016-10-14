package gr.iti.mklab.mmcomms16;

public abstract class Sampling {

	protected abstract Object loadData(String testFile);
	
	protected abstract Object writeInFile(Object data);
}
