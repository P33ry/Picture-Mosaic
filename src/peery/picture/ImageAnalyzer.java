package peery.picture;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import peery.file.FileHandler;
import peery.log.Log;
import peery.log.LogLevel;
import peery.picture.worker.PlacementWorker;
import peery.picture.worker.PlainImageAnalyzerWorker;
import peery.picture.worker.MatchWorker;
import peery.picture.worker.TargetImageAnalyzerWorker;

public class ImageAnalyzer {
	
	public BufferedImage target;
	public FileHandler fh;
	
	private Dimension /*biggestInputSize,*/ targetSize;
	public int preSlotWidth, preSlotHeight, postSlotWidth, postSlotHeight, slotX, slotY, slotCount;
	public BufferedImage canvas;
	private HashMap<String, Integer> index;
	
	private int alphaThreshhold;
	public final boolean keepRatio;
	public int[] gridEnd;
	
	//Input Classification Worker
	private int inputWorkersLimit;
	private static final String inputWorkerName = "inputWorker";
	private PlainImageAnalyzerWorker[] inputWorkers;
	private ArrayList<File> inputFiles;
	//
	
	//Target Classification Worker
	private int targetWorkersLimit;
	private static final String targetWorkerName = "targetWorker";
	private TargetImageAnalyzerWorker[] targetWorkers;
	public HashMap<String, Integer> slotClassifications; //TODO change back private
	//
	
	//Matching Worker
	private int matchWorkersLimit;
	private static final String matchWorkerName = "matchWorker";
	private MatchWorker[] matchWorkers;
	private HashMap<int[], File[]> fileMap;
	//
	
	//Placement Worker
	private static final String placeWorkerName = "placeWorker";
	private PlacementWorker placeWorker;
	//
	
	public ImageAnalyzer(FileHandler fh, int inputWorkersLimit, int targetWorkersLimit, 
			int matchWorkersLimit, int placeWorkersLimit, int alphaThreshhold, boolean keepRatio){
		this.fh = fh;
		this.target = fh.loadImage(fh.TargetImageFile);
		this.alphaThreshhold = alphaThreshhold;
		this.keepRatio = keepRatio;
		this.gridEnd = new int[2];
		
		this.inputWorkersLimit = inputWorkersLimit;
		this.targetWorkersLimit = targetWorkersLimit;
		this.matchWorkersLimit = matchWorkersLimit;
		
		this.slotClassifications = new HashMap<String, Integer>();
		this.fileMap = new HashMap<int[], File[]>();
		this.inputFiles = new ArrayList<File>();
		
		this.inputWorkers = new PlainImageAnalyzerWorker[inputWorkersLimit];
		this.targetWorkers = new TargetImageAnalyzerWorker[targetWorkersLimit];
		this.matchWorkers = new MatchWorker[matchWorkersLimit];
		this.index = fh.loadIndex();
	}
	
	/**
	 * Checks if any of the Worker's are still out there doing stuff.
	 * 
	 * @return true if at least one worker of any kind is still alive!
	 */
	public boolean isPrepInProgress(){
		for(PlainImageAnalyzerWorker pWorker: inputWorkers){
			if(pWorker != null && pWorker.isAlive()){
				return true;
			}
		}
		for(TargetImageAnalyzerWorker tWorker: targetWorkers){
			if(tWorker != null && tWorker.isAlive()){
				return true;
			}
		}
		return false;
	}
	
	public boolean isMatchInProgress(){
		for(MatchWorker mWorker: matchWorkers){
			if(mWorker != null && mWorker.isAlive()){
				return true;
			}
		}
		return false;
	}
	
	public boolean isPlaceInProgress(){
		if(this.placeWorker != null && this.placeWorker.isAlive()){
			return true;
		}
		return false;
	}
	
	public synchronized void reloadIndex(){
		Log.log(LogLevel.Debug, "Reloading index!");
		this.index = fh.loadIndex();
	}
	
	/**
	 * Updates the index by spawning worker threads to classify new input Images.
	 * 
	 * Checks for already indexed images before adding them to the workload.
	 * To check if workers are still running check worker.getRunning() or worker.isAlive() (latter preferred)
	 * Worker instances are found in this.inputWorkers
	 */
	public void updateIndex(){
		Log.log(LogLevel.Info, "Starting to update Index.");
		
		Log.log(LogLevel.Debug, "Filling work list with (unread) input files ...");
		for(File f: fh.InputImagesFolder.listFiles()){
			if(this.fh.loadIndexEntry(index, f) == null){
				Log.log(LogLevel.Debug, "Unindexed input file "+f.getName()+" found! Adding to worklist ...");
				inputFiles.add(f);
			}else{
				Log.log(LogLevel.Debug, "Already indexed input file "+f.getName()+" found!");
			}
		}
		Log.log(LogLevel.Info, "Work list filled with "+inputFiles.size()+" file(s)!");

		if(inputFiles.size() <= 0){
			Log.log(LogLevel.Info, "No work then! Ending Index Update ...");
			return;
		}
		Log.log(LogLevel.Info, "Spawning input workers ...");
		inputWorkers = new PlainImageAnalyzerWorker[inputWorkersLimit];
		int count = 0;
		for(int i = 0; i < inputWorkersLimit; i++){
			if(inputFiles.size() <= i){
				Log.log(LogLevel.Debug, "Allowed more workers than ther was work. Stopped spawning on "+i+" worker(s)!");
				break;
			}
			inputWorkers[i] = new PlainImageAnalyzerWorker(this, inputWorkerName+Integer.toString(i),
					inputFiles.get(i), this.alphaThreshhold);
			count++;
		}
		Log.log(LogLevel.Info, count+"  worker(s) spawned!");
	}
	
	/**
	 * Securely retrieves new workload for PlainImageAnalyzerWorker instances.
	 * 
	 * Returns null when the list is empty.
	 * @return File for PlainImageAnalyzerWorker to work with.
	 */
	public synchronized File getNewInputFile(){
		if(inputFiles.size() == 0){
			return null;
		}
		File file = inputFiles.get(0);
		inputFiles.remove(0);
		return file;
	}
	
	/**
	 * Starts classification of the target image's slots via spawning threaded workers.
	 * 
	 * To check if workers are still running check worker.getRunning() or worker.isAlive() (latter preferred)
	 * Worker instances are found in this.targetWorkers
	 */
	public void classifyTarget(){
		Log.log(LogLevel.Info, "Starting Target Classification. Calculating workload and spawning worker(s) ...");
		this.targetWorkers = new TargetImageAnalyzerWorker[targetWorkersLimit+1];
		Log.log(LogLevel.Debug, slotCount+" slot(s) need to be classified!");
		int workload = this.slotCount / (this.targetWorkersLimit);
		int initialWork = this.slotCount % workload;
		int currWorker = 0;
		if(initialWork != 0){
			this.targetWorkers[0] = new TargetImageAnalyzerWorker(this, this.targetWorkerName+"0", 0, initialWork);
			currWorker++;
		}
		
		int currWork = initialWork;
		for(int i = currWorker; i < targetWorkersLimit; i++){
			targetWorkers[i] = new TargetImageAnalyzerWorker(this, this.targetWorkerName+Integer.toString(i), currWork, currWork+workload);
			currWork += workload;
		}
		targetWorkers[targetWorkersLimit] = new TargetImageAnalyzerWorker(this, this.targetWorkerName+Integer.toString(targetWorkersLimit), currWork, currWork+workload); 
		Log.log(LogLevel.Debug, "Ended on assigning "+(currWork+workload)+" slot(s)!");
		Log.log(LogLevel.Info, "Spawned "+(currWorker+1)+" target worker(s)");
	}
	
	/**
	 * Adds the given classification map to the classification map of the TargetImage.
	 * 
	 * (invoked by target worker instances to deliver finished workloads)
	 * @param clFragment HashMap with classifications and coordinates (slot) as key.
	 */
	public synchronized void addSlotClassifications(HashMap<int[], Integer> clFragment, String workerName){
		for(int[] key: clFragment.keySet()){
			if(!slotClassifications.containsKey(ImageUtils.parseCoord(key))){
				this.slotClassifications.put(ImageUtils.parseCoord(key), clFragment.get(key));
				Log.log(LogLevel.Debug, "Got a classification added by "+workerName+" "+key[0]+" "+key[1]);
				/*if(key[0] == 199 && key[1] == 0){
					Log.log(LogLevel.Error, "ImageAnalyzer.addSlotClassifications() - key[0]==30 && key[1]==0");
					Log.log(LogLevel.Error, "Brrrriiiing!");
					Log.log(LogLevel.Error, "parseCoord: "+ImageUtils.parseCoord(key));
					Log.log(LogLevel.Error, "deliveredFrag: "+clFragment.get(key));
					Log.log(LogLevel.Error, "result: "+this.slotClassifications.get(ImageUtils.parseCoord(key)));
					Log.log(LogLevel.Error, "");
				}*/
			}else{
				Log.log(LogLevel.Error, "Caused by ["+workerName+"] with "+key[0]+" "+key[1]);
				Log.log(LogLevel.Error, "Multiple classifcation of target slot detected! Workloads were not sliced correctly or coordinates are screwed up!");
				continue;
			}
		}
		
	}
	
	public void calculateMatches(){
		Log.log(LogLevel.Info, "Starting to match slots to indexed Images. Spawning workers ...");
		this.matchWorkers = new MatchWorker[matchWorkersLimit];
		int workload = this.slotCount / this.matchWorkersLimit;
		int initialWork = this.slotCount % workload;
		int currWorker = 0;
		int[] lel = {62, 68};
		if(initialWork != 0){
			this.matchWorkers[0] = new MatchWorker(this, matchWorkerName+"0", 0, initialWork, 0,
					slotClassifications, index);
			currWorker++;
		}
		
		int currWork = initialWork;
		for(int i = currWorker; i < matchWorkersLimit; i++){
			matchWorkers[i] = new MatchWorker(this, matchWorkerName+"0", currWork, currWork+workload, 0,
					slotClassifications, index);;
			currWork += workload;
		}
		Log.log(LogLevel.Info, "Spawned "+(currWorker+1)+" match worker(s)");
	}
	
	/**
	 * Merges the given fileMapFragment with this instance's one.
	 * 
	 * (invoked by MatchWorker instances)
	 * @param fileMapFragment
	 */
	public synchronized void addFileMaps(HashMap<int[], File[]> fileMapFragment){
		for(int[] key: fileMapFragment.keySet()){
			if(!fileMap.containsKey(key)){
				this.fileMap.put(key, fileMapFragment.get(key));
			}else{
				Log.log(LogLevel.Error, "Multiple fileMap fragments for "+key.toString()+" available! Something went wrong ...");
				continue;
			}
		}
	}
	
	/**
	 * Starts processing the fileMap and queues workloads for PlacementWorker instances and spawns them.
	 */
	public void placeFragments(){
		Log.log(LogLevel.Info, "Starting to gather Coordinates for each Image.");
		HashMap<File, ArrayList<int[]>> coordMap = new HashMap<File, ArrayList<int[]>>();
		for(int[] coord: fileMap.keySet()){
			File file = fileMap.get(coord)[0];
			
			if(!coordMap.containsKey(file)){
				coordMap.put(file, new ArrayList<int[]>());
			}
			coordMap.get(file).add(coord);
		}
		Log.log(LogLevel.Info, "Start placing fragments on the target.");
		this.placeWorker = new PlacementWorker(this, this.placeWorkerName, coordMap);
	}
	
	public void rasterizeTarget(int gridX, int gridY, float targetSizeMultiplier, 
			double gridErrorThresh, int adaptionCount, double adaptionStep){
		Log.log(LogLevel.Info, "Rasterizing target image ...");
		Log.log(LogLevel.Info, "Aiming for "+gridX+"*"+gridY+"="+(gridX*gridY)+" tiles ...");
		Log.log(LogLevel.Debug, "Target is "+target.getWidth()+"x"+target.getHeight()+" big!");
		//this.biggestInputSize = this.fh.loadBiggestDimension();
		this.targetSize = new Dimension(this.target.getWidth(), this.target.getHeight());
		
		int count = 0;
		DecimalFormat df = new DecimalFormat("#");
		while(true){
			double resultSlotWidth = ((targetSize.width*targetSizeMultiplier)/gridX);
			double resultSlotHeight = ((targetSize.height*targetSizeMultiplier)/gridY);
			postSlotWidth = (int)resultSlotWidth;
			postSlotHeight = (int)resultSlotHeight;
			
			Log.log(LogLevel.Debug, "Perfect slot Size would be "+resultSlotWidth+"x"+resultSlotHeight);
			double widthLoss = Math.abs(resultSlotWidth - Double.parseDouble(df.format(resultSlotWidth)));
			double heightLoss = Math.abs(resultSlotHeight - Double.parseDouble(df.format(resultSlotHeight)));
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
				if(count > adaptionCount){
					Log.log(LogLevel.Critical, "Could not adapt to grid misplacement error! The result might be cut off or missing parts!");
					break;
				}
				continue;
			}else{
				break;
			}
		}
		
		
		preSlotWidth = (int) ((targetSize.width)/gridX);
		preSlotHeight = (int) ((targetSize.height)/gridY);
		//
		
		
		Log.log(LogLevel.Debug, "Target will be "+(int)(targetSize.width*targetSizeMultiplier)
				+"x"+(int)(targetSize.height*targetSizeMultiplier)+" big.");
		Log.log(LogLevel.Debug, "Slots are "+preSlotWidth+"x"+preSlotHeight+" big.");
		slotX = gridX;
		slotY = gridY;
		slotCount = gridX*gridY;
		//ia.slotWidth*gridX+"x"+ia.slotHeight*gridY
		//canvas = new BufferedImage((int)(targetSize.getWidth()*targetSizeMultiplier), (int)(targetSize.getHeight()*targetSizeMultiplier), BufferedImage.TYPE_INT_ARGB);
		canvas = new BufferedImage(postSlotWidth*gridX, postSlotHeight*gridY, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) canvas.getGraphics();
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
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
	public synchronized void placeImage(int gridX, int gridY, BufferedImage input, boolean keepRatio){
		assert(gridX < slotX && gridY < slotY);
		assert(input.getWidth() < postSlotWidth && input.getHeight() < postSlotHeight);
		
		int picWidth, picHeight;
		if(keepRatio){
			picWidth = input.getWidth();
			picHeight = input.getHeight();
		}else{
			picWidth = postSlotWidth;
			picHeight = postSlotHeight;
		}
		
		Graphics2D g2 = (Graphics2D)canvas.getGraphics();
		g2.drawImage(input, gridX*postSlotWidth, gridY*postSlotHeight, input.getWidth(), input.getHeight(), null);
		if(gridEnd[0] < gridX*postSlotWidth+postSlotWidth){
			gridEnd[0] = gridX*postSlotWidth+postSlotWidth;
		}
		if(gridEnd[1] < gridY*postSlotHeight+postSlotHeight){
			gridEnd[1] = gridY*postSlotHeight+postSlotHeight;
		}
		g2.dispose();
		//Log.log(LogLevel.Error, "Drawn picture at "+gridX*postSlotWidth+" "+gridY*postSlotHeight+" with "+input.getWidth()+"x"+input.getHeight());
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
