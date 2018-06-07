package mosaic.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import mosaic.log.Log;
import mosaic.log.LogLevel;

public class SettingsHandler {
	
	public File settingsFile;
	
	public SettingsHandler(FileHandler fh, String settings){
		this.settingsFile = new File(fh.sourceFolder.getAbsolutePath()+fh.fs+settings);;
	}
	
	/**
	 * Saves the settings to file.
	 * @param settings
	 */
	public void saveSettings(HashMap<String, String> settings){
		try {
			Log.log(LogLevel.Info, "Saving settings to file ...");
			BufferedWriter bw = new BufferedWriter(new FileWriter(settingsFile, false));
			for(String key: settings.keySet()){
				bw.write(key+"="+settings.get(key)+"\n");
				bw.flush();
			}
			bw.close();
		} catch (IOException e) {
			Log.log(LogLevel.Error, "Couldn't write settings file at "+settingsFile.getAbsolutePath()+" ! Are write permissions missing?");
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads all settings from file.
	 * @return
	 */
	public HashMap<String, String> loadSettings(){
		HashMap<String, String> settings;
		try {
			settings = new HashMap<String, String>();
			Scanner sc = new Scanner(new BufferedReader(new FileReader(settingsFile)));
			while(sc.hasNext()){
				String raw = sc.nextLine();
				if(!raw.matches(".*=.*")){
					continue;
				}
				String[] entry = raw.split("=");
				//System.out.println(entry[0]+":"+entry[1]);
				settings.put(entry[0], entry[1]);
			}
			sc.close();
			return settings;
		} catch (FileNotFoundException e) {
			Log.log(LogLevel.Error, "Couldn't load settings file at "+settingsFile.getAbsolutePath()+" ! Are read permissions missing?");
			e.printStackTrace();
		}
		return null;
	}
	
}
