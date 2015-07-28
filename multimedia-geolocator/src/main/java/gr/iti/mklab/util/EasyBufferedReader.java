package gr.iti.mklab.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

public class EasyBufferedReader extends BufferedReader {

	protected Logger logger;
	
	
	static final Reader createReader(String textFile, Logger logger){
		try {
			return new InputStreamReader(new FileInputStream(textFile), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	public EasyBufferedReader(String textFile) {
		super(createReader(textFile, Logger.getLogger("gr.iti.mklab.util.EasyBufferedReader")));
		this.logger = Logger.getLogger("gr.iti.mklab.util.EasyBufferedReader");
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
	public String readLine() {
		try {
			return super.readLine();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	
	
}
