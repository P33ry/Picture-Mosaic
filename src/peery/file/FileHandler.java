package peery.file;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import peery.log.Log;
import peery.log.LogLevel;

public class FileHandler {
	
	public File sourceFolder, InputImagesFolder, TargetImageFile, OutputFolder;
	/*
	 * sourcePath/ -> all ressources
	 * sourcePath/Images/ -> Input pictures folder
	 * sourcePath/Target -> Target picture file
	 * sourcePath/Output/ -> Output picture folder
	 */
	
	public FileHandler(String sourcePath){
		this.sourceFolder = new File(sourcePath);
		this.InputImagesFolder = new File(sourceFolder.getAbsolutePath()+"/Images");
		this.TargetImageFile = new File(sourceFolder.getAbsolutePath()+"/Target");
		this.OutputFolder = new File(sourceFolder.getAbsolutePath()+"/Output");
		Log.initLog(this.sourceFolder.getAbsolutePath());
		this.validateFolderStructure();
	}
	
	public boolean validateFolderStructure(){
		if(this.sourceFolder.isDirectory()){
			Log.log(LogLevel.Debug, "Detected sourcefolder at "+this.sourceFolder.getAbsolutePath());
			if(this.InputImagesFolder.isDirectory()){
				Log.log(LogLevel.Debug, "Detected Input folder at "+this.InputImagesFolder.getAbsolutePath());
				if(this.OutputFolder.isDirectory()){
					Log.log(LogLevel.Debug, "Detected Output folder at "+this.OutputFolder.getAbsolutePath());
					if(this.TargetImageFile.isFile()){
						Log.log(LogLevel.Debug, "Detected Target Image at "+this.TargetImageFile.getAbsolutePath());
						return true;
					}else{
						Log.log(LogLevel.Critical, "No Target Image found! Exiting...");
						System.exit(1);
					}
				}else{
					Log.log(LogLevel.Info, "No Output folder found.");
					Log.log(LogLevel.Info, "Creating one at "+this.OutputFolder.getAbsolutePath());
					this.OutputFolder.mkdirs();
				}
			}else{
				Log.log(LogLevel.Critical, "No Input folder found.");
				Log.log(LogLevel.Critical, "Creating one at "+this.InputImagesFolder.getAbsolutePath());
				this.InputImagesFolder.mkdirs();
			}
		}else{
			Log.log(LogLevel.Critical, "No source folder found.");
			Log.log(LogLevel.Critical, "Creating one at "+this.sourceFolder.getAbsolutePath());
			this.sourceFolder.mkdirs();
		}
		Log.log(LogLevel.Critical, "Folder validation failed. There could be a permission problem "
				+ "or a folder needed to be created! Please look for earlier errors.");
		return false;
	}
	
	public BufferedImage loadImage(File file){
		if(file.isFile() && file.canRead()){
			BufferedImage img;
			try {
				img = ImageIO.read(file);
				return img;
			} catch (IOException e) {
				Log.log(LogLevel.Debug, "File "+file.getPath()+" failed to load as an Image. What did I just read?");
				e.printStackTrace();
				return null;
			}
		}
		else{
			Log.log(LogLevel.Info, "Can't read file "+file.getPath()+" ! It could be a directory or no read permissions.");
			return null;
		}
	}
	
	public Dimension loadBiggestDimension(){
		File[] files = this.InputImagesFolder.listFiles();
		int width = 0, height = 0, img_count = 0;
		for(File f: files){
			if(f.isFile() && f.canRead()){
				BufferedImage img = loadImage(f);
				if(img == null){
					continue;
				}
				img_count++;
				if(width < img.getWidth()){
					width = img.getWidth();
				}
				if(height < img.getHeight()){
					height = img.getHeight();
				}
			}
			else{
				Log.log(LogLevel.Info, "Can't read file"+f.toString()+"! It could be a directory or no read permissions.");
			}
		}
		Log.log(LogLevel.Info, img_count+" image(s) were loaded...");
		if(width == 0 || height == 0){
			Log.log(LogLevel.Critical, "Incomplete or no dimension values! Could I load any Image?");
		}
		Log.log(LogLevel.Debug, "Biggest dimension is "+width+"x"+height);
		return new Dimension(width, height);
	}
	
	// Would probably kill memory (and performance).
	@Deprecated
	public BufferedImage[] loadAllImages(){
		File[] files = this.InputImagesFolder.listFiles();
		ArrayList<BufferedImage> imgs = new ArrayList<BufferedImage>();
		int img_count = 0;
		for(File f: files){
			BufferedImage img = loadImage(f);
			if(img == null){
				continue;
			}
			imgs.add(img);
			img_count++;
		}
		if(imgs.size() == 0){
			Log.log(LogLevel.Critical, "No Images found in "+this.InputImagesFolder.getAbsolutePath());
			return null;
		}
		Log.log(LogLevel.Info, img_count+" image(s) were loaded...");
		BufferedImage[] bfs = new BufferedImage[imgs.size()];
		for(int i = 0; i < imgs.size(); i++){
			bfs[i] = imgs.get(i);
		}
		return bfs;
	}

}
