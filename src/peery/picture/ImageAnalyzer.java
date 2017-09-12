package peery.picture;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;

import peery.file.FileHandler;
import peery.log.Log;
import peery.log.LogLevel;

public class ImageAnalyzer {
	
	public BufferedImage target;
	private FileHandler fh;
	
	private Dimension /*biggestInputSize,*/ targetSize;
	public int slotWidth, slotHeight, slotX, slotY;
	public BufferedImage canvas;
	
	public ImageAnalyzer(FileHandler fh){
		this.fh = fh;
		this.target = fh.loadImage(fh.TargetImageFile);
	}
	
	public void rasterizeTarget(int gridX, int gridY, float targetSizeMultiplier, double gridErrorThresh, int adaptionThreshhold, double adaptionStep){
		Log.log(LogLevel.Info, "Rasterizing target image ...");
		Log.log(LogLevel.Info, "Aiming for "+gridX+"*"+gridY+"="+(gridX*gridY)+" tiles ...");
		//this.biggestInputSize = this.fh.loadBiggestDimension();
		this.targetSize = new Dimension(this.target.getWidth(), this.target.getHeight());
		
		//TMP
		int count = 0;
		DecimalFormat df = new DecimalFormat("#");
		while(true){
			double realSlotWidth = ((targetSize.width*targetSizeMultiplier)/gridX);
			double realSlotHeight = ((targetSize.height*targetSizeMultiplier)/gridY);
			Log.log(LogLevel.Debug, "Perfect slot Size would be "+realSlotWidth+"x"+realSlotHeight);
			double widthLoss = Math.abs(realSlotWidth - Double.parseDouble(df.format(realSlotWidth)));
			double heightLoss = Math.abs(realSlotHeight - Double.parseDouble(df.format(realSlotHeight)));
			if(widthLoss > gridErrorThresh || heightLoss > gridErrorThresh){
				Log.log(LogLevel.Critical, "Grid misplacement error exceeded threshhold! Error is width:"+widthLoss+" height:"+heightLoss);
				if(widthLoss > gridErrorThresh){
					gridX *= adaptionStep;
				}
				if(heightLoss > gridErrorThresh){
					gridY *= adaptionStep;
				}
				Log.log(LogLevel.Info, "This is the "+(++count)+". adaption to ease the error.");
				Log.log(LogLevel.Info, "Aiming for "+gridX+"*"+gridY+"="+(gridX*gridY)+" tiles ...");
				if(count > adaptionThreshhold){
					Log.log(LogLevel.Critical, "Could not adapt to grid misplacement error! The result might be cut off or missing parts!");
					break;
				}
				continue;
			}else{
				break;
			}
		}
		
		
		slotWidth = (int) ((targetSize.width*targetSizeMultiplier)/gridX);
		slotHeight = (int) ((targetSize.height*targetSizeMultiplier)/gridY);
		//
		
		
		Log.log(LogLevel.Debug, "Target will be "+(int)(targetSize.width*targetSizeMultiplier)
				+"x"+(int)(targetSize.height*targetSizeMultiplier)+" big.");
		Log.log(LogLevel.Debug, "Slots are "+slotWidth+"x"+slotHeight+" big.");
		slotX = gridX;
		slotY = gridY;
		//ia.slotWidth*gridX+"x"+ia.slotHeight*gridY
		//canvas = new BufferedImage((int)(targetSize.getWidth()*targetSizeMultiplier), (int)(targetSize.getHeight()*targetSizeMultiplier), BufferedImage.TYPE_INT_ARGB);
		canvas = new BufferedImage(slotWidth*gridX, slotHeight*gridY, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) canvas.getGraphics();
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}
	
	/**
	 * Compares the given rgb value with the index and returns a HashMap of the closest hits.
	 * Unlikely to yield more than 1 result without deviationPercentage
	 * @param index
	 * @param rgb
	 * @param deviation How much the matches can be different from the perfect match, and still be taken.
	 * @return
	 */
	public HashMap<String, Integer> matchClosestIndex(HashMap<String, Integer> index, int rgb, int deviation){
		Log.log(LogLevel.Debug, "Searching for closest match for rgb:"+rgb+" in the index with deviation of "+deviation+".");
		assert(deviation < 100 && 0 <= deviation);
		if(index == null){
			Log.log(LogLevel.Critical, "No Index was given for rgb matching. Loading it from file ...");
			index = this.fh.loadIndex();
		}
		HashMap<String, Double> metrics = new HashMap<String, Double>();
		double currBest = -1;
		for(String key: index.keySet()){
			double metric = getMetric(index.get(key), rgb);
			metrics.put(key, metric);
			if(currBest > metric || currBest == -1){
				currBest = metric;
			}
		}
		Log.log(LogLevel.Info, "Calculated all metrics for rgb:"+rgb+" !");
		
		HashMap<String, Integer> matches = new HashMap<String, Integer>();
		for(String key: metrics.keySet()){
			if(metrics.get(key) == currBest || metrics.get(key) < currBest+(currBest*(deviation/(double)100))){
				matches.put(key, index.get(key));
			}
		}
		Log.log(LogLevel.Debug, "Grabbed all good matches for rgb:"+rgb+" ! Got "+matches.size()+" ...");
		return matches;
	}
	
	/**
	 * Calculates the euclidian metric of two given rgb values.
	 * @param rgb1
	 * @param rgb2
	 * @return
	 */
	private double getMetric(int rgb1, int rgb2){
		ColorModel cm = ColorModel.getRGBdefault();
		double result = Math.sqrt(Math.pow((cm.getRed(rgb1)-cm.getRed(rgb2)), 2)+
				Math.pow(cm.getGreen(rgb1)-cm.getGreen(rgb2), 2)+
				Math.pow(cm.getBlue(rgb1)-cm.getBlue(rgb2), 2));
		return result;
	}
	
	/**
	 * Places the given input in its specified slot (gridX, gridY) on the canvas.
	 * Assumes and checks boundaries calculated by rasterizeTarget().
	 * @param gridX
	 * @param gridY
	 * @param input
	 * @param canvas
	 * @return
	 */
	public void placeImage(int gridX, int gridY, BufferedImage input){
		assert(gridX < slotX && gridY < slotY);
		assert(input.getWidth() < slotWidth && input.getHeight() < slotHeight);
		
		Graphics2D g2 = (Graphics2D)canvas.getGraphics();
		g2.drawImage(input, gridX*slotWidth, gridY*slotHeight, slotWidth, slotHeight, null);
		g2.dispose();
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
		Log.log(LogLevel.Info, "Classified "+file.getPath()+" with following rgb result: value:"+rgb+
		" red:"+red+", green:"+green+", blue:"+blue);
		return rgb;
	}
	
	public int classifySlot(int gridX, int gridY, BufferedImage target){
		int slotWidth = target.getWidth()/slotX, 
			slotHeight = target.getHeight()/slotY;
		BufferedImage subImage = target.getSubimage(gridX*slotWidth, gridY*slotHeight, slotWidth, slotHeight);
		Log.log(LogLevel.Debug, "Slicing slot "+gridX+"x"+gridY+" out of the target for classification ...");
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
		Log.log(LogLevel.Debug, "Classified slot "+gridX+"x"+gridY+" with following rgb result: value:"+rgb+
				" red:"+red+", green:"+green+", blue:"+blue);
		return rgb;
	}
/*
	public static void main(String[] args){
		System.out.println("ZHE DEBUG");
		/*
		FileHandler fh = new FileHandler("/home/peery/Software_Projects/EclipseWorkspace/Picture Mosaic/resources");
		ImageAnalyzer ia = new ImageAnalyzer(fh);
		
		HashMap<String, Integer> index = fh.loadIndex();
		for(File f: fh.InputImagesFolder.listFiles()){
			BufferedImage img = fh.loadImage(f);
			if(img == null){
				continue;
			}
			int indexValue = ia.classifyImage(img, f, 30);
			fh.appendToIndex(index, f, indexValue);
		}
		index = fh.loadIndex();
		System.out.println(index.get("Peery-sketch.png"));
		System.out.println(index.get("rasd"));
		System.out.println(index.get("Larry the Tiger - Candy Shop Logo (Gift).png"));
		System.out.println(ia.matchClosestIndex(index, -10744103, 5));
		//BufferedImage img = ImageUtils.resizeImage(fh.loadImage(fh.InputImagesFolder.listFiles()[0]), new Dimension(500, 300), false);
		//fh.saveImage(img, new File("/home/peery/Software_Projects/EclipseWorkspace/Picture Mosaic/resources/resize-test.png"));
		
	}*/

}
