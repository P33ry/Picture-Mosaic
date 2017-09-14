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
		if(targetSize.getWidth() > targetSize.getHeight()){
			tmp = input.getScaledInstance(-1, (int)targetSize.getHeight(), Image.SCALE_SMOOTH);
			img = new BufferedImage(tmp.getWidth(null), tmp.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g2 = (Graphics2D) img.getGraphics();
			g2.drawImage(tmp, 0, 0, null);
			g2.dispose();
			return img;
		}else{
			tmp = input.getScaledInstance((int)targetSize.getWidth(), -1, Image.SCALE_SMOOTH);
			img = new BufferedImage(tmp.getWidth(null), tmp.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g2 = (Graphics2D) img.getGraphics();
			g2.drawImage(tmp, 0, 0, null);
			g2.dispose();
			return img;
		}
	}
	
	/**
	 * Converts from a numbered Slot to a specific slot start coordinate. 
	 * 
	 * @param num
	 * @param preMagnification	true if slot sizes before the magnification are to be used.
	 * @return
	 */
	public static int[] getSlotCoord(ImageAnalyzer ia, int num, boolean preMagnification){
		int slotWidth, slotHeight;
		if(preMagnification){
			slotWidth = ia.preSlotWidth;
			slotHeight = ia.preSlotHeight;
		}else{
			slotWidth = ia.postSlotWidth;
			slotHeight = ia.postSlotHeight;
		}
		
		//TODO -----> FIX überschlag von Zeile 0 in 1; x zählt zu viel!
		int ySlots = num/(ia.slotY-1);
		int xSlots = num%(ia.slotX-1);
		int[] coords = {xSlots*slotWidth, ySlots*slotHeight};
		return coords;
	}
	
	public static String parseCoord(int[] coord){
		return coord[0]+"-"+coord[1];
	}
}
