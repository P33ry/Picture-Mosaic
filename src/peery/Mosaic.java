package peery;

import java.io.File;
import java.util.HashMap;

import peery.file.FileHandler;
import peery.file.SettingsHandler;
import peery.log.Log;
import peery.log.LogLevel;
import peery.picture.ImageAnalyzer;
import peery.picture.ImageUtils;

public class Mosaic extends Thread{
	
	public static final String programmName = "Mosaic",
	versionString = "Alpha-0.40";
	
	private static String outputName = "Output";
	private static int gridWidth = 100, gridHeight = 100, targetMulti = 2,
			alphaThreshhold = 30,
			adaptionCount = 300,
			inputWorkerLimit = 10,
			targetWorkerLimit = 8,
			matchWorkerLimit = 10,
			placeWorkerLimit = 2; //get overwritten by file settings
	private static double adaptionStep = 1.1, gridErrorThresh = 0.15;
	private static boolean keepRatio = true, overlapImages = true;
	/*
	 * 
	 * TO DO:
	 * Write down stats what image was used (how often)
	 * Make settings save & load from a settings file
	 * 
	 * Performance:
	 * 
	 * FIX:
	 *  rasterization doesn't cover everything!
	 *  alphaThreshhold is currently dead
	 * 
	 * Feature:
	 *  explore keeping Input Image Ratio's
	 *  explore guarantee of usage of at least once, each image.
	 */
	
	public SettingsHandler sh;
	public FileHandler fh;
	public ImageAnalyzer ia;
	
	public Mosaic(){
		fh = new FileHandler("resources");
		Log.log(LogLevel.Info, "Starting "+programmName+" "+versionString);
		ia = new ImageAnalyzer(fh, inputWorkerLimit, targetWorkerLimit, matchWorkerLimit, 
				placeWorkerLimit, alphaThreshhold, keepRatio, overlapImages);
		sh = new SettingsHandler(fh, "settings.txt");
		this.start();
	}
	
	public void run(){
		Log.log.perfLog("Started "+programmName+" v."+versionString);
		
		Log.log(LogLevel.Info, "Checking for settings file ...");
		if(sh.settingsFile.exists()){
			Log.log(LogLevel.Info, "Importing settings from file ...");
			importSettings(sh.loadSettings());
		}else{
			Log.log(LogLevel.Info, "Couldn't find a settings file. Dumping hard-coded settings ...");
			sh.saveSettings(exportSettings());
		}
		System.out.println("gridHeightxxx:"+gridHeight);
		
		Log.log.perfLog("Starting indexing ...");
		prepMatching();
		
		while(ia.isPrepInProgress()){
			try {
				Mosaic.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Log.log.perfLog("Finished Index!");
		Log.log.perfLog("Calculating Matches and reloading Index ...");
		prepAssembly();
		while(ia.isMatchInProgress()){
			try {
				Mosaic.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Log.log.perfLog("Finished Matching!");
		Log.log.perfLog("Starting placement of images ...");
		createMosaic();
		while(ia.isPlaceInProgress()){
			try {
				Mosaic.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		ImageUtils.cutOutGrid(ia);
		Log.log.perfLog("Finished Placement!");
		Log.log(LogLevel.Info, "Finished placement. Output is done ...");
		Log.log.perfLog("Saving output to file ...");
		fh.saveImage(ia.canvas, new File(fh.OutputFolder+fh.fs+outputName+"-"+fh.OutputFolder.listFiles().length+".png"));
		Log.log.perfLog("Everything done! Exiting ...");
		Log.log.finishPerfLog();
		Log.shutdownLog();
	}
	
	/**
	 * Prepares the assembly of the Mosaic.
	 * 
	 * Starts threads to index all not indexed images and rasterizes and classifies the Target.
	 */
	public void prepMatching(){
		ia.rasterizeTarget(gridWidth, gridHeight, targetMulti, gridErrorThresh, adaptionCount, adaptionStep);
		ia.updateIndex();
		ia.classifyTarget();
	}
	
	public void prepAssembly(){
		ia.reloadIndex();
		ia.calculateMatches();
	}
	
	public void createMosaic(){
		Log.log(LogLevel.Info, "Starting Creation of Mosaic !");
		ia.placeFragments();
	}
	
	public void importSettings(HashMap<String, String> settings){
		if(0 < settings.get("version").compareTo(versionString)){
			Log.log(LogLevel.Error, "Won't import settings! Version String doesn't match!");
			Log.log(LogLevel.Error, "Read: "+versionString+" Expected: "+settings.get("version"));
			return;
		}
		outputName = settings.get("outputName");
		gridWidth = Integer.parseInt(settings.get("gridWidth"));
		gridHeight = Integer.parseInt(settings.get("gridHeight"));
		targetMulti = Integer.parseInt(settings.get("targetMultiplier"));
		alphaThreshhold = Integer.parseInt(settings.get("alphaThreshhold"));
		adaptionCount = Integer.parseInt(settings.get("adaptionCount"));
		inputWorkerLimit = Integer.parseInt(settings.get("inputWorkerLimit"));
		targetWorkerLimit = Integer.parseInt(settings.get("targetWorkerLimit"));
		matchWorkerLimit = Integer.parseInt(settings.get("matchWorkerLimit"));
		placeWorkerLimit = Integer.parseInt(settings.get("placeWorkerLimit"));
		adaptionStep = Double.parseDouble(settings.get("adaptionStep"));
		gridErrorThresh = Double.parseDouble(settings.get("gridErrorThresh"));
		keepRatio = Boolean.parseBoolean(settings.get("keepRatio"));
		overlapImages = Boolean.parseBoolean(settings.get("overlapImages"));
	}
	
	public HashMap<String, String> exportSettings(){
		HashMap<String, String> settings = new HashMap<String, String>();
		settings.put("version", versionString);
		settings.put("outputName", outputName);
		settings.put("gridWidth", Integer.toString(gridWidth));
		settings.put("gridHeight", Integer.toString(gridHeight));
		settings.put("targetMultiplier", Integer.toString(targetMulti));
		settings.put("alphaThreshhold", Integer.toString(alphaThreshhold));
		settings.put("adaptionCount", Integer.toString(adaptionCount));
		settings.put("inputWorkerLimit", Integer.toString(inputWorkerLimit));
		settings.put("targetWorkerLimit", Integer.toString(targetWorkerLimit));
		settings.put("matchWorkerLimit", Integer.toString(matchWorkerLimit));
		settings.put("placeWorkerLimit", Integer.toString(placeWorkerLimit));
		settings.put("adaptionStep", Double.toString(adaptionStep));
		settings.put("gridErrorThresh", Double.toString(gridErrorThresh));
		settings.put("keepRatio", Boolean.toString(keepRatio));
		settings.put("overlapImages", Boolean.toString(overlapImages));
		
		return settings;
	}
	
	public static void main(String[] args){
		new Mosaic();
	}
}
