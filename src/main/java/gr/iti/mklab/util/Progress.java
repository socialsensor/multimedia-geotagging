package gr.iti.mklab.util;

public class Progress {

	private long gStartTime;
	private int div, scaleTime;
	private String mesPerCent, mesTime;
	
	
	public Progress(long gStartTime, int limitCountLines, int scalePerCent, int scaleTime){
		this.gStartTime = gStartTime;
		
		this.mesPerCent = "%";
		if(scalePerCent==10){this.mesPerCent = "0" + this.mesPerCent;}
		
		this.scaleTime = scaleTime;
		this.mesTime = "min";
		if(scaleTime==1){this.mesTime = "s";}
		
		this.div = limitCountLines/scalePerCent;
	}
	
	public void showProgress(int count, long stopTime){
		
		if(count%div==0){System.out.println(count/div+ mesPerCent + " " + (stopTime-gStartTime)/(scaleTime*1000) + mesTime);}
		
	}
	
}
