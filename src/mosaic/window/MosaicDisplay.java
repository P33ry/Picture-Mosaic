package mosaic.window;

import java.awt.Canvas;
import java.awt.Graphics2D;
//import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class MosaicDisplay extends Canvas{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2738927210561389362L;
	
	private Graphics2D g2 = (Graphics2D) this.getGraphics();
	
	public MosaicDisplay(int width, int height){
		this.setSize(width, height);
	}
	
	public void drawImage(BufferedImage img, int x, int y){
		
		g2.drawImage(img, null, x, y);
	}
	
	public void clear(){
		//g2.draw(new Rectangle(this.getWidth(), this.getHeight()));
		this.invalidate();
	}

}
