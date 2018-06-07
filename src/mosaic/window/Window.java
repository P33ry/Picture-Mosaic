package mosaic.window;

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;

import mosaic.Mosaic;

public class Window extends JFrame{
	
	private JPanel imagePanel, settingsPanel;
	
	public Window(String name, int width, int height, Component display){
		this.setSize(width, height);
		this.setTitle(name);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		imagePanel = new JPanel();
		settingsPanel = new SettingsPanel();

		this.add(imagePanel);
		this.add(settingsPanel);
		
		//this.pack();
		this.setVisible(true);
	}
	
	public static void main(String[] args){
		int width = 800, height = 600;
		Window win = new Window(Mosaic.programmName+" "+Mosaic.versionString, width, height, 
				new MosaicDisplay(width, height));
	}
}
