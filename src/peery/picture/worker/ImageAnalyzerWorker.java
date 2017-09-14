package peery.picture.worker;

import peery.picture.ImageAnalyzer;

public abstract class ImageAnalyzerWorker extends Thread{
	
	protected ImageAnalyzer ia;
	protected boolean running;
	
	public ImageAnalyzerWorker(ImageAnalyzer ia, String name){
		this.ia = ia;
		this.setName(name);
		
		running = true;
	}
	
	public abstract void run();
	
	public boolean getRunning(){
		return this.running;
	}

}
