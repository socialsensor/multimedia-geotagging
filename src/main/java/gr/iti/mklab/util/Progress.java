package gr.iti.mklab.util;

import org.apache.log4j.Logger;

public class Progress {

	private long gStartTime, lastTime;
	private int div, scaleTime;
	private String mesPerCent, mesTime, messege;
	private int sec;
	private Logger logger;

	public Progress(long gStartTime, int limitCountLines, int scalePerCent, int scaleTime, String messege, Logger logger){
		this.gStartTime = gStartTime;

		this.mesPerCent = "%";
		if(scalePerCent==10){this.mesPerCent = "0" + this.mesPerCent;}

		this.scaleTime = scaleTime;
		this.mesTime = "m";
		if(scaleTime==1){this.mesTime = "s";}

		this.div = limitCountLines/scalePerCent;
		this.messege = messege;
		
		this.logger = logger;
	}
	
	public Progress(long gStartTime, int sec, int scaleTime, String messege, Logger logger){
		this.sec = sec;
		this.gStartTime = gStartTime;
		
		this.scaleTime = scaleTime;
		
		this.mesTime = "min";
		this.messege = messege;
		if(scaleTime==1){this.mesTime = "s";}
		
		this.logger = logger;
	}
	
	public void showMessege(long stopTime){
		if(stopTime-lastTime>sec*1000){
			logger.info(messege+" > "+ (stopTime-gStartTime)/(scaleTime*1000) + mesTime);
			lastTime=stopTime;
		}
	}
	
	public void showProgress(int count, long stopTime){
		if(count%div==0){
			logger.info(messege+" > "+count/div+ mesPerCent + " > " + (stopTime-gStartTime)/(scaleTime*1000) + mesTime);
		}
	}
}
