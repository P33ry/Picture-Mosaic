package mosaic.picture.worker;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;

import mosaic.log.Log;
import mosaic.log.LogLevel;
import mosaic.picture.ImageAnalyzer;

public class PlainImageAnalyzerWorker extends ImageAnalyzerWorker{
	
	private File file;
	private int alphaThreshhold;
	
	public PlainImageAnalyzerWorker(ImageAnalyzer ia, String name, File file, int alphaThreshhold){
		super(ia, name);
		this.alphaThreshhold = alphaThreshhold;
		this.file = file;
		
		this.start();
	}
	
	public void run(){
		Log.log(LogLevel.Info, "Worker "+this.getName()+" up and running! Let's roll...");
		while(running){
			System.out.println(file.getAbsolutePath());
			BufferedImage img = this.ia.fh.loadImage(file);
			if(img == null){
				this.file = ia.getNewInputFile();
				if(this.file == null){
					running = false;
					break;
				}
			}
			int rgb = this.classifyImage(img, file, alphaThreshhold);
			ia.fh.appendToIndex(file, rgb);
			Log.log(LogLevel.Debug, "["+this.getName()+"] finished workunit:"+file.getName()+" !");
			file = ia.getNewInputFile();
			while(file != null && !file.exists()  ){
				Log.log(LogLevel.Critical, "Non-existent file eneded up as a task for worker "+this.getName()+" !");
				file = ia.getNewInputFile();
				Log.log(LogLevel.Debug, "["+this.getName()+"] got new workunit:"+file.getName()+" !");
			}
			if(file == null){
				Log.log(LogLevel.Debug, "["+this.getName()+"] New workload was null ! Ran out of work ...");
				running = false;
				break;
			}
		}
		Log.log(LogLevel.Info, "Worker "+this.getName()+" has finished work! Terminating ...");
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
		Log.log(LogLevel.Debug, "Starting classification of "+file.getName()+" with width:"+
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

}
