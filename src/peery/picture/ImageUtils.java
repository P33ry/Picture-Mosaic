package peery.picture;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImageUtils {
	
	public static BufferedImage resizeImage(BufferedImage input, Dimension targetSize, boolean keepRatio){
		Image tmp;
		BufferedImage img;
		if(!keepRatio){
			tmp = input.getScaledInstance((int)targetSize.getWidth(), (int)targetSize.getHeight(), Image.SCALE_SMOOTH);
			img = new BufferedImage(tmp.getWidth(null), tmp.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g2 = (Graphics2D) img.getGraphics();
			g2.drawImage(tmp, 0, 0, null);
			g2.dispose();
			return img;
		}
		if(input.getWidth() > input.getHeight()){
			tmp = input.getScaledInstance((int)targetSize.getWidth(), -1, Image.SCALE_SMOOTH);
			img = new BufferedImage(tmp.getWidth(null), tmp.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g2 = (Graphics2D) img.getGraphics();
			g2.drawImage(tmp, 0, 0, null);
			g2.dispose();
			return img;
		}else{
			tmp = input.getScaledInstance(-1, (int)targetSize.getHeight(), Image.SCALE_SMOOTH);
			img = new BufferedImage(tmp.getWidth(null), tmp.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g2 = (Graphics2D) img.getGraphics();
			g2.drawImage(tmp, 0, 0, null);
			g2.dispose();
			return img;
		}
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
