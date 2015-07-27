package uniko.west.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileLoader {

	/*
	 * this method copies a file from a given url to a local directory folder
	 */
	public static void getFile(String urlToFile, String pathToLocalFile) {
		File localFile = new File(pathToLocalFile);
		if (localFile.exists()) {
			localFile.delete();
		}
		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(new URL(urlToFile).openStream()))) {
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(
					localFile));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				bufferedWriter.write(line + "\n");
			}
			bufferedReader.close();
			bufferedWriter.close();
		} catch (IOException ex) {
			Logger.getLogger(FileLoader.class.getName()).log(
					Level.SEVERE,
					"urlToFile: " + urlToFile + ", pathToLocalFile: "
							+ pathToLocalFile, ex);
		}
	}

}
