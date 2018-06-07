package mosaic.picture.worker;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import mosaic.log.Log;
import mosaic.log.LogLevel;
import mosaic.picture.ImageAnalyzer;
import mosaic.picture.ImageUtils;

public class PlacementWorker extends ImageAnalyzerWorker{
	
	private HashMap<File, ArrayList<int[]>> coordMap;
	
	public PlacementWorker(ImageAnalyzer ia, String name, HashMap<File, ArrayList<int[]>> coordMap){
		super(ia, name);
		this.coordMap = coordMap;
		
		this.start();
	}

	@Override
	public void run() {
		Log.log(LogLevel.Info, "Worker "+this.getName()+" commencing work!");
		for(File file: coordMap.keySet()){
			if(coordMap.get(file) == null){ //Check to avoid NullPointerException
				continue;
			}
			BufferedImage img = ia.fh.loadImage(file);
			ArrayList<int[]> coords = coordMap.get(file);
			img = ImageUtils.resizeImage(img, new Dimension(ia.postSlotWidth,  ia.postSlotHeight), ia.keepRatio, ia.overlapImages);
			Log.log(LogLevel.Debug, "["+this.getName()+"] Resized image "+file.getName()+" to "+img.getWidth()+"x"+img.getHeight()+" !");
			
			Log.log(LogLevel.Info, "["+this.getName()+"] Going to place \""+file.getName()+"\" "+coords.size()+" time(s)!");
			int count = 0;
			for(int[] coord: coords){
				ia.placeImage(coord[0], coord[1], img, ia.keepRatio);
				if(count % 100 == 0){
					Log.log(LogLevel.Info, "["+this.getName()+"] Placed "+count+"/"+coords.size()+" instances! Continuing ...");
				}
				count++;
			}
			Log.log(LogLevel.Debug, "["+this.getName()+"] Finished placing all "+file.getName()+" ! Switching file!");
		}
		Log.log(LogLevel.Info, "["+this.getName()+"] Finished all work! Terminating ...");
	}

}
