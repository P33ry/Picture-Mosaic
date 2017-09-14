package peery;

import java.io.File;

import peery.file.FileHandler;
import peery.log.Log;
import peery.log.LogLevel;
import peery.picture.ImageAnalyzer;

public class Mosaic extends Thread{
	
	public static final String programmName = "Mosaic",
	versionString = "Alpha-0.33";
	
	private static final String outputName = "Output";
	private static final int gridX = 100, gridY = 50, targetMulti = 2,
			alphaThreshhold = 30,
			adaptionCount = 300,
			inputWorkerLimit = 10,
			targetWorkerLimit = 4,
			matchWorkerLimit = 10,
			placeWorkerLimit = 2;
	private static final double adaptionStep = 1.1, gridErrorThresh = 0.15;
	private static boolean keepRatio = false;
	/*
	 * 
	 * Performance:
	 * 
	 * 
	 * FIX:
	 *  rasterization doesn't cover everything!
	 *  alphaThreshhold is currently dead
	 * 
	 * Feature:
	 *  explore keeping Input Image Ratio's
	 *  explore guarantee of usage of at least once, each image.
	 */
	
	public FileHandler fh;
	public ImageAnalyzer ia;
	
	public Mosaic(){
		fh = new FileHandler("resources");
		Log.log(LogLevel.Info, "Starting "+programmName+" "+versionString);
		ia = new ImageAnalyzer(fh, inputWorkerLimit, targetWorkerLimit, matchWorkerLimit, 
				placeWorkerLimit, alphaThreshhold, keepRatio);
		
		this.start();
	}
	
	public void run(){
		Log.log.perfLog("Started "+programmName+" v."+versionString);
		Log.log.perfLog("Starting indexing ...");
		prepMatching();
		//Log.log(LogLevel.Error, ia.slotClassifications.toString()+" prep:"+ia.isPrepInProgress());
		//Log.log(LogLevel.Error, ia.slotClassifications.toString()+" prep:"+ia.isPrepInProgress());
		
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
		cutOutGrid();
		Log.log.perfLog("Finished Placement!");
		Log.log(LogLevel.Info, "Finished placement. Output is done ...");
		Log.log.perfLog("Saving output to file ...");
		fh.saveImage(ia.canvas, new File(fh.OutputFolder+fh.fs+outputName+"-"+fh.OutputFolder.listFiles().length+".png"));
		Log.log.perfLog("Everything done! Exiting ...");
		Log.log.finishPerfLog();
	}
	
	/**
	 * Prepares the assembly of the Mosaic.
	 * 
	 * Starts threads to index all not indexed images and rasterizes and classifies the Target.
	 */
	public void prepMatching(){
		ia.rasterizeTarget(gridX, gridY, targetMulti, gridErrorThresh, adaptionCount, adaptionStep);
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
	
	public void cutOutGrid(){
		Log.log(LogLevel.Info, "Cutting out what the grid covered!");
		Log.log(LogLevel.Debug, "Cutting out 0 0 "+ia.gridEnd[0]+" "+ia.gridEnd[1]);
		ia.canvas = ia.canvas.getSubimage(0, 0, ia.gridEnd[0], ia.gridEnd[1]);
	}

	public static void main(String[] args){
		new Mosaic();
	}
}
