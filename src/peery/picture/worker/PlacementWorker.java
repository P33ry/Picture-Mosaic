package peery.picture.worker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import peery.log.Log;
import peery.log.LogLevel;
import peery.picture.ImageAnalyzer;

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
			if(coordMap.get(file) == null){
				continue;
			}
			BufferedImage img = ia.fh.loadImage(file);
			ArrayList<int[]> coords = coordMap.get(file);
			
			Log.log(LogLevel.Info, "["+this.getName()+"] Going to place "+file.getName()+" "+coords.size()+" time(s)!");
			int count = 0;
			for(int[] coord: coords){
				ia.placeImage(coord[0], coord[1], img);
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
