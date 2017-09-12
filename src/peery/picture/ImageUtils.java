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

}
