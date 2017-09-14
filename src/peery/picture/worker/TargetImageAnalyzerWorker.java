package peery.picture.worker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.util.HashMap;

import peery.log.Log;
import peery.log.LogLevel;
import peery.picture.ImageAnalyzer;
import peery.picture.ImageUtils;

public class TargetImageAnalyzerWorker extends ImageAnalyzerWorker{
	
	private ImageAnalyzer ia;
	private int startCoord, endCoord;
	
	/**
	 * 
	 * @param ia 		ImageAnalyzer to work for
	 * @param name		Thread name to use (should be unique)
	 * @param img		Image to work on
	 * @param startCoord	slot x coordinate to start from
	 * @param endCoord	slot y coordinate to start from
	 */
	public TargetImageAnalyzerWorker(ImageAnalyzer ia, String name, int startCoord, int endCoord){
		super(ia, name);
		this.ia = ia;
		this.startCoord = startCoord;
		this.endCoord = endCoord;
		
		this.start();
	}
	
	public void run(){
		Log.log(LogLevel.Info, "Worker "+this.getName()+" up and running! Let's roll ...");
		HashMap<int[], Integer> result = this.classifyArea(startCoord, endCoord);
		ia.addSlotClassifications(result);
		Log.log(LogLevel.Info, "Worker "+this.getName()+" finished work! Terminating ...");
	}
	
	/**
	 * Classifies the specified Area of the target image and returns the average colors.
	 * 
	 * Returns a HashMap with the slot coordinates (slot) as key.
	 * @param startCoord	slotX to start in
	 * @param endCoord	slotY to start in
	 * @param workArea  number of slots to work through
	 * @param target	image to work on
	 */
	public HashMap<int[], Integer> classifyArea(int startCoord, int endCoord){
		BufferedImage target = ia.target;
		HashMap<int[], Integer> results = new HashMap<int[], Integer>();
		System.out.println("Was ordered: "+startCoord+" "+endCoord);
		for(int i = startCoord; i < endCoord; i++){
			int[] coordPixels = ImageUtils.getSlotCoord(ia, i, true);
			if(coordPixels[0]+ia.preSlotWidth >= ia.target.getWidth() || coordPixels[1]+ia.preSlotHeight >= ia.target.getHeight()){
				//Dirty FIX
				//This will inevitably land outside the Raster otherwise. I should prevent these Coords from the start
				//Log.log(LogLevel.Error, "Didn't classify "+coordPixels[0]+" "+coordPixels[1]);
				continue;
			}
			int rgb = classifySlot(coordPixels[0], coordPixels[1], ia.preSlotWidth, ia.preSlotHeight, ia.target);
			int[] coordSlot = {coordPixels[0]/ia.preSlotWidth, coordPixels[1]/ia.preSlotHeight};
			
			/*if(coordPixels[0] == 210 && coordPixels[1] == 0){//TODO remove
				System.out.println("Brrrring!");
				Log.log(LogLevel.Error, "Brrrriiing"+rgb+" ");
				System.out.println(rgb);
				System.out.println(coordSlot[0]+" "+coordSlot[1]);
			}*/
			
			results.put(coordSlot, rgb);
		}	
		return results;
	}
	
	/**
	 * Classifies a single specified slot on the given Image.
	 * @param gridX		Slot specification in X
	 * @param gridY		Slot specification in Y
	 * @param slotWidth	Slot size in x direction
	 * @param slotHeight	Slot size in y direction
	 * @param target	Image to work on
	 * @return average Color as RGB value
	 */
	public int classifySlot(int gridX, int gridY, int slotWidth, int slotHeight, BufferedImage target){
		System.out.println(gridX+" "+gridY+" "+slotWidth+" "+slotHeight);
		Log.log(LogLevel.Debug, "["+this.getName()+"] Slicing slot "+gridX+"x"+gridY+" out of the target for classification ...");
		BufferedImage subImage = target.getSubimage(gridX, gridY, slotWidth, slotHeight);
		ColorModel cm = ColorModel.getRGBdefault();
		float red = 0, green = 0, blue = 0;
		int pixels = 0;
		for(int x = 0; x < subImage.getWidth(); x++){
			for(int y = 0; y < subImage.getHeight(); y++){
				int rgb = subImage.getRGB(x, y);
				red += cm.getRed(rgb);
				green += cm.getGreen(rgb);
				blue += cm.getBlue(rgb);
				pixels++;
			}
		}
		red = red/pixels;
		green = green/pixels;
		blue = blue/pixels;
		int rgb = new Color((int)red, (int)green, (int)blue).getRGB();
		Log.log(LogLevel.Debug, "["+this.getName()+"] Classified slot "+gridX+"x"+gridY+" with following rgb result: value:"+rgb+
				" red:"+red+", green:"+green+", blue:"+blue);
		return rgb;
	}

}
