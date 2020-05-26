package bfroehlich.memory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class Hider extends JPanel {

	private int imageNumber;
	private JLabel label;
	private Image image;
	private boolean faceUp;

	private static Image questionMark;
	
	public Hider(Image image, int imageNumber, Dimension size) {
		BoxLayout box = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(box);
		this.imageNumber = imageNumber;
		this.image = image.getScaledInstance(size.width, size.height, Image.SCALE_DEFAULT);
		if(questionMark == null) {
			questionMark = Main.loadBufferedImage("q2.png", size);
		}
		label = new JLabel(new ImageIcon(questionMark));
		add(label);
		//label.setBorder(new LineBorder(Color.RED, 3, true));
	}
	
	public void setFaceUp(boolean shown) {
		this.faceUp = shown;
		if(shown && (image != null)) {
			label.setIcon(new ImageIcon(image));
		}
		else {
			label.setIcon(new ImageIcon(questionMark));
		}
	}
	
	public boolean isFaceUp() {
		return faceUp;
	}
	
	public int getImageNumber() {
		return imageNumber;
	}
}