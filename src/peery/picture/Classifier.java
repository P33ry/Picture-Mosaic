package peery.picture;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;

import peery.file.FileHandler;
import peery.log.Log;
import peery.log.LogLevel;

public class Classifier {
	
	public BufferedImage target;
	
	public Classifier(FileHandler fh){
		BufferedImage target = fh.loadImage(fh.TargetImageFile);
	}
	
	public void rasterizeTarget(){
		
	}
	
	/**
	 * Plain calculation of average Color of an Image via RGB. 
	 * Takes average of RGB values of each pixel.
	 * Does not account for alpha.
	 * @param img Image to process
	 * @param alphaThreshhold value under which pixels are discarded
	 * @return RGB value of the average color
	 */
	public int classifyImage(BufferedImage img, File file, int alphaTreshhold){
		int width = img.getWidth();
		int height = img.getHeight();
		Log.log(LogLevel.Debug, "Starting classification of "+file.getPath()+" with width:"+
		width+" and height:"+height+" ...");
		
		ColorModel cm = ColorModel.getRGBdefault();
		float red = 0, green = 0, blue = 0;
		int pixels = 0;
		for(int x = 0; x < img.getWidth(); x++){
			for(int y = 0; y < img.getHeight(); y++){
				int rgb = img.getRGB(x, y);
				red += cm.getRed(rgb);
				blue += cm.getBlue(rgb);
				green += cm.getGreen(rgb);
				pixels++;
			}
		}
		red = red/pixels;
		green = green/pixels;
		blue = blue/pixels;
		int rgb = new Color((int)red, (int)green, (int)blue).getRGB();
		Log.log(LogLevel.Debug, "Classified "+file.getPath()+" with following rgb result: value:"+rgb+
		" red:"+red+", green:"+green+", blue:"+blue);
		return rgb;
	}
	
	public static void main(String[] args){
		FileHandler fh = new FileHandler("/home/peery/Software_Projects/EclipseWorkspace/Picture Mosaic/resources");
		BufferedImage[] bfs = fh.loadAllImages();
		Classifier cl = new Classifier(fh);
		cl.classifyImage(bfs[0], new File("DEBUG_VALUE"), 30);
		System.out.println(fh.loadBiggestDimension());
	}

}
