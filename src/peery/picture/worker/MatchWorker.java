package peery.picture.worker;

import java.awt.image.ColorModel;
import java.io.File;
import java.util.HashMap;

import peery.log.Log;
import peery.log.LogLevel;
import peery.picture.ImageAnalyzer;
import peery.picture.ImageUtils;

public class MatchWorker extends ImageAnalyzerWorker{
	
	private int startCoord, endCoord, deviation;
	HashMap<String, Integer> slotClassifications;
	HashMap<String, Integer> index;
	
	public MatchWorker(ImageAnalyzer ia, String name, int startCoord, int endCoord, int deviation, 
			HashMap<String, Integer> slotClassifications, HashMap<String, Integer> index){
		super(ia, name);
		
		this.deviation = deviation;
		this.startCoord = startCoord;
		this.endCoord = endCoord;
		this.slotClassifications = slotClassifications;
		this.index = index;
		this.ia = ia;
		
		this.start();
	}

	@Override
	public void run() {
		Log.log(LogLevel.Debug, "["+this.getName()+"] has started its duty!");
		HashMap<int[], File[]> fileMap = new HashMap<int[], File[]>();
		for(int i = startCoord; i < endCoord; i++){
			int[] coord = {i%ia.slotX, i/ia.slotX};
			if(coord[0]+ia.preSlotWidth >= ia.target.getWidth() || coord[1]+ia.preSlotHeight >= ia.target.getHeight()){
				//Dirty FIX
				//This will inevitably land outside the Raster otherwise. I should prevent these Coords from the start
				//Log.log(LogLevel.Error, "Didn't classify "+coord[0]+" "+coord[1]);
				continue;
			}
			if(coord[0] == ia.slotX-1 || coord[1] == ia.slotY-1){
				//Dirty Fix
				//for avoiding non-existant keys in the slotClassifications.
				continue;
			}
			if(index == null || slotClassifications.get(ImageUtils.parseCoord(coord)) == null){ //TODO remove
				Log.log(LogLevel.Error, "MatchWorker run() -> slotClass.get(ImageUtils.parse(coord)==null");
				Log.log(LogLevel.Error, "BRrrring"+slotClassifications.get(ImageUtils.parseCoord(coord)));
				Log.log(LogLevel.Error, "parsed: "+ImageUtils.parseCoord(coord));
				Log.log(LogLevel.Error, ""+coord[0]+" "+coord[1]);
				Log.log(LogLevel.Error, "");
				for(String key: slotClassifications.keySet()){
					//Log.log(LogLevel.Error, key);
				}
			}
			HashMap<String, Integer> matches = matchClosestIndex(index, slotClassifications.get(ImageUtils.parseCoord(coord)), deviation);
			Log.log(LogLevel.Debug, "["+this.getName()+"] Matched "+matches.keySet().size()+" image(s) to slot "+i+" !");
			File[] files = new File[matches.keySet().size()];
			for(int n = 0; n < files.length; n++){
				files[n] = new File(ia.fh.InputImagesFolder.getAbsolutePath()+ia.fh.fs+(String) matches.keySet().toArray()[n]);
			}
			
			fileMap.put(coord, files);
		}
		ia.addFileMaps(fileMap);
		Log.log(LogLevel.Info, "["+this.getName()+"] This worker has finished its chunk! Terminating ...");
	}
	
	/**
	 * Compares the given rgb value with the index and returns a HashMap of the closest hits.
	 * Unlikely to yield more than 1 result without deviationPercentage
	 * @param index
	 * @param rgb
	 * @param deviation How much the matches can be different from the perfect match, and still be taken.
	 * @return
	 */
	public HashMap<String, Integer> matchClosestIndex(HashMap<String, Integer> index, int rgb, int deviation){
		if(index == null){
			System.out.println("Briiing!");
		}
		Log.log(LogLevel.Debug, "Searching for closest match for rgb:"+rgb+" in the index with deviation of "+deviation+".");
		assert(deviation < 100 && 0 <= deviation);
		if(index == null){
			Log.log(LogLevel.Critical, "No Index was given for rgb matching. Exiting!");
			return null;
		}
		HashMap<String, Double> metrics = new HashMap<String, Double>();
		double currBest = -1;
		for(String key: index.keySet()){
			double metric = getMetric(index.get(key), rgb);
			metrics.put(key, metric);
			if(currBest > metric || currBest == -1){
				currBest = metric;
			}
		}
		Log.log(LogLevel.Debug, "Calculated all metrics for rgb:"+rgb+" !");
		
		HashMap<String, Integer> matches = new HashMap<String, Integer>();
		for(String key: metrics.keySet()){
			if(metrics.get(key) == currBest || metrics.get(key) < currBest+(currBest*(deviation/(double)100))){
				matches.put(key, index.get(key));
			}
		}
		Log.log(LogLevel.Debug, "Grabbed all good matches for rgb:"+rgb+" ! Got "+matches.size()+" ...");
		return matches;
	}
	
	/**
	 * Calculates the euclidian metric of two given rgb values.
	 * @param rgb1
	 * @param rgb2
	 * @return
	 */
	private double getMetric(int rgb1, int rgb2){
		ColorModel cm = ColorModel.getRGBdefault();
		double result = Math.sqrt(Math.pow((cm.getRed(rgb1)-cm.getRed(rgb2)), 2)+
				Math.pow(cm.getGreen(rgb1)-cm.getGreen(rgb2), 2)+
				Math.pow(cm.getBlue(rgb1)-cm.getBlue(rgb2), 2));
		return result;
	}

}
