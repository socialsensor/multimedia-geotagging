package gr.iti.mklab.util;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.log4j.Logger;

public class EasyBufferedWriter extends BufferedWriter {
	
	protected Logger logger;
	
	static final Writer createWriter(String textFile, Logger logger){
		try {
			return new OutputStreamWriter(new FileOutputStream(textFile), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	public EasyBufferedWriter(String textFile) {
		super(createWriter(textFile, Logger.getLogger("eu.socialsensor.util.EasyBufferedWriter")));
		this.logger = Logger.getLogger("eu.socialsensor.util.EasyBufferedWriter");
		logger.debug("opened " + textFile);
	}
	
	@Override
	public void close() {
		try {
			super.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void write(String s) {
		try {
			super.write(s);
		} catch (IOException e){
			logger.error(e.getMessage());
		}
	}

	@Override
	public void newLine() {
		try {
			super.newLine();
		} catch (IOException e){
			logger.error(e.getMessage());
		}
	}

	
	
}
