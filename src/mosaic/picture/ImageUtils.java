package mosaic.picture;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import mosaic.log.Log;
import mosaic.log.LogLevel;

public class ImageUtils {
	
	private static final int scalingMethod = Image.SCALE_SMOOTH;
	
	/**
	 * TODO Needs cleanup
	 * @param input
	 * @param targetSize
	 * @param keepRatio
	 * @param overlapImages
	 * @return
	 */
	public static BufferedImage resizeImage(BufferedImage input, Dimension targetSize, boolean keepRatio,
			boolean overlapImages){
		Image tmp;
		BufferedImage img;
		int imageWidth, imageHeight;
		if(!keepRatio){
			imageWidth = (int)targetSize.getWidth();
			imageHeight = (int)targetSize.getHeight();
		}
		else{
			if(!overlapImages){
				if(input.getWidth() > input.getHeight()){
					imageWidth = (int)targetSize.getWidth();
					imageHeight = -1;
				}else{
					imageWidth = -1;
					imageHeight = (int)targetSize.getHeight();
				}
			}else{
				if(input.getWidth() < input.getHeight()){
					imageWidth = (int)targetSize.getWidth();
					imageHeight = -1;
				}else{
					imageWidth = -1;
					imageHeight = (int)targetSize.getHeight();
				}
			}
		}
		
		tmp = input.getScaledInstance(imageWidth, imageHeight, scalingMethod);
		img = new BufferedImage(tmp.getWidth(null), tmp.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g2 = (Graphics2D) img.getGraphics();
		g2.drawImage(tmp, 0, 0, null);
		g2.dispose();
		return img;
	}
	
	public static void cutOutGrid(ImageAnalyzer ia){
		Log.log(LogLevel.Info, "Cutting out what the grid covered!");
		Log.log(LogLevel.Debug, "Cutting out 0 0 "+ia.gridEnd[0]+" "+ia.gridEnd[1]);
		ia.canvas = ia.canvas.getSubimage(0, 0, ia.gridEnd[0], ia.gridEnd[1]);
	}
	
	/**
	 * Converts the given number into the slot coordinates (not pixel coordinates).
	 * @param ia
	 * @param num
	 * @return
	 */
	public static int[] getSlotCoord(ImageAnalyzer ia, int num){
		int[] coords = new int[2];
		coords[0] = num%(ia.slotX+1);
		coords[1] = num/(ia.slotX+1);
		return coords;
	}
	
	/**
	 * Converts from a numbered Slot to a specific slot start coordinate. 
	 * 
	 * @param num
	 * @param preMagnification	true if slot sizes before the magnification are to be used.
	 * @return
	 */
	public static int[] getSlotCoordPixels(ImageAnalyzer ia, int[] coords, boolean preMagnification){
		//TODO BUGGGSSS
		/*
		 * Error collision values (num1 num2 preSlotDimensions -> result):
		 * 18614 18855  7x3 -> 399x174
		 * 18565 18806  7x3 -> 56x174
		 * 6735 6976	7x3 -> 1596x63
		 * 5833 6074    7x3 -> 343x54
		 * 382 623		7x3 -> 987x3
		 */
		int slotWidth, slotHeight;
		if(preMagnification){
			slotWidth = ia.preSlotWidth;
			slotHeight = ia.preSlotHeight;
		}else{
			slotWidth = ia.postSlotWidth;
			slotHeight = ia.postSlotHeight;
		}
		
		//TODO -----> FIX überschlag von Zeile 0 in 1; x zählt zu viel!
		int[] pixelCoords = {coords[0]*slotWidth, coords[1]*slotHeight};
		return pixelCoords;
	}
	
	public static String parseCoord(int[] coord){
		return coord[0]+"-"+coord[1];
	}
}
