package peery;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import peery.file.FileHandler;
import peery.log.Log;
import peery.log.LogLevel;
import peery.picture.ImageAnalyzer;

public class Mosaic {
	
	public static final String versionString = "Alpha-0.21";
	
	private static final String outputName = "Output";
	private static final int gridX = 100, gridY = 100, targetMulti = 4,
			alphaThreshhold = 30,
			adaptionCount = 300;
	private static final double adaptionStep = 1.1, gridErrorThresh = 0.15;
	/*
	 * FIX:
	 * TODO investigate picture stretching -> is ImageUtils.resizeImage() even used?
	 * TODO alphaThreshhold is currently dead
	 * 
	 * Feature:
	 * TODO explore keeping Input Image Ratio's
	 * TODO explore guarantee of usage of at least once, each image.
	 */
	
	public FileHandler fh;
	public ImageAnalyzer ia;
	
	public Mosaic(){
		fh = new FileHandler("resources");
		Log.log(LogLevel.Info, "Starting Mosaic "+versionString);
		ia = new ImageAnalyzer(fh);
		createMosaic();
	}
	
	public void createMosaic(){
		ia.rasterizeTarget(gridX, gridY, targetMulti, gridErrorThresh, adaptionCount, adaptionStep);
		HashMap<String, Integer> index = fh.loadIndex();
		
		//Loading index
		if(index == null){
			Log.log(LogLevel.Info, "No index loaded. Working with empty one ...");
			index = new HashMap<String, Integer>();
		}
		//
		//Preparing Setup
		ArrayList<String> fileList = fh.listInputFiles();
		if(fileList.size() == 0){
			Log.log(LogLevel.Error, "No files in Input folder! There NEED to be at least some Images! Exiting...");
			System.exit(1);
		}
		Log.log(LogLevel.Info, "Starting classification of Input now ...");
		int count = 0;
		for(String path: fileList){
			Log.log(LogLevel.Info, "Processing file "+(count++)+"/"+fileList.size()+" ...");
			File file = new File(path);
			BufferedImage img = fh.loadImage(file);
			if(img == null){
				continue;
			}
			int rgb = ia.classifyImage(img, file, alphaThreshhold);
			fh.appendToIndex(index, file, rgb);
		}
		Log.log(LogLevel.Info, "Finished classification. Index is ready for production. Reloading index ...");
		index = fh.loadIndex();
		if(index == null){
			Log.log(LogLevel.Error, "No index after Classification of Input! Can't continue! Exiting...");
			System.exit(1);
		}
		
		Log.log(LogLevel.Debug, "Canvas is "+ia.canvas.getWidth()+"x"+ia.canvas.getHeight()+" big.");
		Log.log(LogLevel.Debug, "Grid will span "+ia.slotWidth*gridX+"x"+ia.slotHeight*gridY+" .");
		Log.log(LogLevel.Info, "Starting classification of target slots.");
		count = 0;
		int maxSlot = ia.slotX*ia.slotY;
		int[][] slotRGBs = new int[ia.slotX][ia.slotY];
		for(int x = 0; x < ia.slotX; x++){
			for(int y = 0; y < ia.slotY; y++){
				Log.log(LogLevel.Info, "Processing slot "+(count++)+"/"+maxSlot+" ...");
				slotRGBs[x][y] = ia.classifySlot(x, y, ia.target);
			}
		}
		Log.log(LogLevel.Info, "Finished classification of target slots.");
		
		Log.log(LogLevel.Info, "Matching indexed Images on slots ...");
		count = 0;
		ArrayList<File> fileHolder = new ArrayList<File>();
		ArrayList<BufferedImage> imgHolder = new ArrayList<BufferedImage>();
		for(int x = 0; x < slotRGBs.length; x++){
			for(int y = 0; y < slotRGBs[x].length; y++){
				Log.log(LogLevel.Info, "Matching slot "+(count++)+"/"+maxSlot+" ...");
				HashMap<String, Integer> matches = ia.matchClosestIndex(index, slotRGBs[x][y], 0);
				String match = matches.keySet().toArray()[0].toString();
				File file = new File(fh.InputImagesFolder+fh.fs+match);
				
				/*
				BufferedImage plain = new BufferedImage(ia.slotWidth, ia.slotHeight, BufferedImage.TYPE_INT_RGB);
				Graphics2D g2 = (Graphics2D) plain.getGraphics();
				g2.setColor(new Color(slotRGBs[x][y]));
				g2.fillRect(0, 0, plain.getWidth(), plain.getHeight());
				g2.dispose();
				ia.placeImage(x, y, plain);
				 */
				BufferedImage img;
				if(!fileHolder.contains(file)){
					fileHolder.add(file);
					img = fh.loadImage(file);
					imgHolder.add(img);
				}else{
					int i = fileHolder.indexOf(file);
					img = imgHolder.get(i);
				}
				ia.placeImage(x, y, img);
			}
		}
		Log.log(LogLevel.Info, "Finished matching. Output is done ...");
		fh.saveImage(ia.canvas, new File(fh.OutputFolder+fh.fs+outputName+"-"+fh.OutputFolder.listFiles().length+".png"));
	}

	public static void main(String[] args){
		new Mosaic();
	}
}
