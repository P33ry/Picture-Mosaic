package peery.window;

import java.awt.Component;

import javax.swing.JFrame;

public class Window extends JFrame{
	
	public Window(String name, int width, int height, Component display){
		this.setSize(width, height);
		this.setName(name);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.add(display);
		
		this.setVisible(true);
	}

}
