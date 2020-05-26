package bfroehlich.memory;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class Main extends JFrame {
	
	private boolean awaitingGuess;
	private ArrayList<Hider> allHiders;
	private ArrayList<Hider> guessesFloating;
	private ArrayList<Hider> hidersShownThisGame;
	private Dimension hiderSize;
	private Category currentCategory;
	
	private JPanel board;
	private ButtonGroup categoriesGroup;
	
	private ButtonGroup difficultyGroup;
	private JPanel strikesPanel;
	private int strikeCount;
	private JTextArea console;
	
	private Image strikeImage;
	private HashMap<Category, BufferedImage> masterImages;
	private HashMap<Category, ArrayList<Image>> subImagesByCategory;
	
	public enum Category {
		Objects, Animals, Gems, MTG
	}

	public static void main(String[] args) {
		new Main().setVisible(true);;
	}
	
	public Main() {
		super("Memory Match");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		strikeImage = loadImage("wrong.png", new Dimension(20, 20));
		
		masterImages = new HashMap<Category, BufferedImage>();
		BufferedImage objectsImage = loadBufferedImage("objects.png", null);
		masterImages.put(Category.Objects, objectsImage);
		BufferedImage animalsImage = loadBufferedImage("animals2a.jpg", null);
		masterImages.put(Category.Animals, animalsImage);
		subImagesByCategory = new HashMap<Category, ArrayList<Image>>();
		for(Category cat : Category.values()) {
			ArrayList<Image> subs = getSubImages(cat);
			subImagesByCategory.put(cat, subs);
		}
		hiderSize = new Dimension(150, 150);
		
		
		init();
	}
	
	public static Image loadImage(String path, Dimension size) {
		if(path == null) {
			return null;
		}
		Image image = null;
		try {
			image = ImageIO.read(Main.class.getResource("/" + path));
		}
		catch(IOException e) {
			System.err.println(e.getClass());
			System.err.println(e.getMessage());
		}
		
        if(size != null) {
        	image = image.getScaledInstance(size.width, size.height, Image.SCALE_DEFAULT);
        }
        return image;
	}
	
	public static BufferedImage loadBufferedImage(String path, Dimension size) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(Main.class.getResource("/" + path));
			if(size != null && image != null) {
				BufferedImage resized = new BufferedImage(size.width, size.height, image.getType());
				Graphics2D g = resized.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.drawImage(image, 0, 0, size.width, size.height, 0, 0, image.getWidth(),
				    image.getHeight(), null);
				g.dispose();
				image = resized;
			}
		}
		catch(IOException e) {
			System.err.println(e.getClass());
			System.err.println(e.getMessage());
		}
		return image;
	}
	
	private void init() {
		JPanel main = new JPanel();
		add(main);
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
		
		board = new JPanel();
		main.add(board);
		
		JPanel control = new JPanel();
		main.add(control);
		control.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
		
		JPanel categoriesPanel = new JPanel();
		control.add(categoriesPanel);
		categoriesPanel.setLayout(new BoxLayout(categoriesPanel, BoxLayout.Y_AXIS));
		JRadioButton objectsRadio = new JRadioButton("Objects");
		objectsRadio.setActionCommand("Objects");
		categoriesPanel.add(objectsRadio);
		objectsRadio.setSelected(true);
		JRadioButton animalsRadio = new JRadioButton("Animals");
		animalsRadio.setActionCommand("Animals");
		categoriesPanel.add(animalsRadio);
		JRadioButton gemsRadio = new JRadioButton("Gems");
		gemsRadio.setActionCommand("Gems");
		categoriesPanel.add(gemsRadio);
		JRadioButton mtgRadio = new JRadioButton("MTG");
		mtgRadio.setActionCommand("MTG");
		categoriesPanel.add(mtgRadio);
		categoriesGroup = new ButtonGroup();
		categoriesGroup.add(objectsRadio);
		categoriesGroup.add(animalsRadio);
		categoriesGroup.add(gemsRadio);
		categoriesGroup.add(mtgRadio);
		
		JPanel difficultyPanel = new JPanel();
		control.add(difficultyPanel);
		difficultyPanel.setLayout(new BoxLayout(difficultyPanel, BoxLayout.Y_AXIS));
		JRadioButton easy = new JRadioButton("Easy");
		easy.setActionCommand("Easy");
		difficultyPanel.add(easy);
		easy.setSelected(true);
		JRadioButton medium = new JRadioButton("Medium");
		medium.setActionCommand("Medium");
		difficultyPanel.add(medium);
		JRadioButton hard = new JRadioButton("Hard");
		hard.setActionCommand("Hard");
		difficultyPanel.add(hard);
		difficultyGroup = new ButtonGroup();
		difficultyGroup.add(easy);
		difficultyGroup.add(medium);
		difficultyGroup.add(hard);
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
		control.add(buttons);
		
		JButton newGame = new JButton("New Game");
		buttons.add(newGame);
		newGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newGame();
			}
		});
		
		JButton end = new JButton("End");
		buttons.add(end);
		end.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(Hider hider : allHiders) {
					hider.setFaceUp(true);
				}
			}
		});
		
		console = new JTextArea();
		console.setEditable(false);
		console.setLineWrap(true);
		console.setWrapStyleWord(true);
		JScrollPane consoleScroll = new JScrollPane(console);
		consoleScroll.setPreferredSize(new Dimension(250, 100));
		control.add(consoleScroll);
		
		strikesPanel = new JPanel();
		control.add(strikesPanel);
		strikesPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
		
		pack();
	}
	
	private void newGame() {
		guessesFloating = new ArrayList<Hider>();
		hidersShownThisGame = new ArrayList<Hider>();
		board.removeAll();
		strikesPanel.removeAll();
		strikeCount = 0;
		console.setText("");
		
		int numPairs = 0;
		String difficulty = difficultyGroup.getSelection().getActionCommand();
		if(difficulty.equalsIgnoreCase("easy")) {
			board.setLayout(new GridLayout(4, 4));
			numPairs = 8;
		}
		else if(difficulty.equalsIgnoreCase("medium")) {
			board.setLayout(new GridLayout(4, 6));
			numPairs = 12;
		}
		else if(difficulty.equalsIgnoreCase("hard")) {
			board.setLayout(new GridLayout(6, 6));
			numPairs = 18;
		}
		
		String cat = this.categoriesGroup.getSelection().getActionCommand();
		currentCategory = Category.valueOf(cat);
		ArrayList<Image> images = subImagesByCategory.get(currentCategory);
		allHiders = new ArrayList<Hider>();
		int[] indexes = null;
		if(images.size() < numPairs) {
			println("Not enough images");
			indexes = new Random().ints(0, images.size()).limit(numPairs).toArray();
		}
		else {
			indexes = new Random().ints(0, images.size()).distinct().limit(numPairs).toArray();
		}
		for(int i : indexes) {
			for(int j = 0; j < 2; j++) {
				Hider hider = new Hider(images.get(i), i, hiderSize);
				allHiders.add(hider);
				hider.addMouseListener(new MouseListener() {
					public void mouseReleased(MouseEvent e) {
						hiderClicked(hider);
					}
					public void mousePressed(MouseEvent e) {}
					public void mouseExited(MouseEvent e) {}
					public void mouseEntered(MouseEvent e) {}
					public void mouseClicked(MouseEvent e) {}
				});
			}
		}
		Collections.shuffle(allHiders);
		for(Hider hider: allHiders) {
			board.add(hider);
		}
		pack();
		awaitingGuess = true;
	}
	
	private void hiderClicked(Hider hider) {
		if(awaitingGuess && !hider.isFaceUp()) {
			hider.setFaceUp(true);
			if(currentCategory == Category.Animals && hider.getImageNumber() == 2) {
				println("An elephant never forgets");
			}
			
			boolean match = false;
			boolean strike = false;
			String strikeReason = "";
			
			if(guessesFloating.isEmpty()) {
				//if first guess is already shown and its match is unknown, that's a strike, unless only 1 remains
				if(hidersShownThisGame.contains(hider)) {
					if(allHiders.size() - hidersShownThisGame.size() > 1) {
						boolean matchSeen = false;
						for(Hider previousShown : hidersShownThisGame) {
							if(hider.getImageNumber() == previousShown.getImageNumber() && !hider.equals(previousShown)) {
								matchSeen = true;
							}
						}
						if(!matchSeen) {
							strike = true;
							strikeReason += "Looked at a card a second time when its match was unknown. ";
						}
					}
				}
				//if at least one hidden match is previously shown and you don't go for a known match, that's a strike (if > 2 cards are unknown)
				boolean matchShownPreviously = false;
				boolean clickedIsPartOfKnownMatch = false;
				for(Hider hider1 : hidersShownThisGame) {
					for(Hider hider2 : hidersShownThisGame) {
						if((!hider1.isFaceUp() || !hider2.isFaceUp()) && hider1.getImageNumber() == hider2.getImageNumber() && !hider1.equals(hider2)) {
							matchShownPreviously = true;
							if(hider.equals(hider1) || hider.equals(hider2)) {
								clickedIsPartOfKnownMatch = true;
							}
						}
					}
				}
				//...unless there is no match among unknowns, i.e. all unknown cards have a known match: then you may reasonably flip an unknown
				boolean existsMatchAmongUnkowns = false;
				for(Hider hider1 : allHiders) {
					if(!hidersShownThisGame.contains(hider1)) {
						for(Hider hider2 : allHiders) {
							if(!hidersShownThisGame.contains(hider2)) {
								if(hider1.getImageNumber() == hider2.getImageNumber() && !hider1.equals(hider2)) {
									existsMatchAmongUnkowns = true;
								}
							}
						}
					}
				}
				if(matchShownPreviously && !clickedIsPartOfKnownMatch && allHiders.size() - hidersShownThisGame.size() > 2 && existsMatchAmongUnkowns) {
					strike = true;
					strikeReason += "Ignored a known match. ";
				}
				guessesFloating.add(hider);
			}
			else {
				//compare to existing guesses
				Iterator<Hider> it = guessesFloating.iterator();
				while(it.hasNext()) {
					Hider guess = it.next();
					if(guess.getImageNumber() == hider.getImageNumber()) {
						//remove matches from floating: these are permanently shown
						it.remove();
						match = true;
					}
				}
				if(!match) {
					//if the last one clicked was previously shown and it doesn't make a match, that's a strike
					if(hidersShownThisGame.contains(hider)) {
						strike = true;
						strikeReason += "You knew that wasn't the match. ";
					}
					//if the first guess's match is known and a match is not then made, that's a strike
					for(Hider previousGuess : guessesFloating) {
						for(Hider previousShown : hidersShownThisGame) {
							if(previousShown.getImageNumber() == previousGuess.getImageNumber() && !previousShown.equals(previousGuess)) {
								strike = true;
								strikeReason += "You knew where the match was. ";
							}
						}
					}
					guessesFloating.add(hider);
					//show incorrect guesses for 1 sec, then hide them again
					Thread t = new Thread(new Runnable() {
						public void run() {
							awaitingGuess = false;
							try {
								Thread.sleep(1000);
							}
							catch(InterruptedException e) {}
							Iterator<Hider> it = guessesFloating.iterator();
							while(it.hasNext()) {
								Hider guess = it.next();
								guess.setFaceUp(false);
								it.remove();
							}
							awaitingGuess = true;
						}
					});
					t.setPriority(Thread.MIN_PRIORITY);
					t.start();
				}
			}
			if(!hidersShownThisGame.contains(hider)) {
				hidersShownThisGame.add(hider);
			}
			if(strike) {
				addStrike(strikeReason);
			}
			if(match) {
				int notShown = 0;
				for(Hider aHider : allHiders) {
					if(!aHider.isFaceUp()) {
						notShown++;
					}
				}
				if(notShown == 0) {
					gameOver();
				}
			}
		}
	}
	
	private void addStrike(String reason) {
		strikesPanel.add(new JLabel(new ImageIcon(strikeImage)));
		println(reason);
		strikeCount++;
		repaint();
		pack();
	}
	
	private void gameOver() {
		if(strikeCount == 0) {
			println("Perfect");
		}
	}
	
	private ArrayList<Image> getSubImages(Category category) {
		ArrayList<Image> images = new ArrayList<Image>();
		BufferedImage master = masterImages.get(category);
		if(category == Category.Objects) {
			for(int i = 2; i < 68; i++) {
				Image image = getSubImageByIndex(master, 10, 7, 0, 0, 0, 0, 0, 0, i);
				images.add(image);
			}
		}
		else if(category == Category.Animals) {
			for(int i = 0; i < 18; i++) {
				Image image = getSubImageByIndex(master, 6, 3, 105, 60, 380, 310, 260, 260, i);
				images.add(image);
			}
		}
		else if(category == Category.Gems) {
			String[] names = {"tree", "amber", "amethyst", "amethystquartz", "aquamarine", "black", "crystalsphere", "goldgemstone", "green", "rainbow", "red", "red2", "red3", "sapphire", "silver", "pink", "swarovski", "yellow"};
			for(String name : names) {
				images.add(loadImage(name + ".png", null));
			}
		}
		else if(category == Category.MTG) {
			String[] names = {"w", "u", "b", "r", "g", "c", "azorius", "boros", "dimir", "golgari", "gruul", "izzet", "orzhov", "rakdos", "selesnya", "simic", "spark", "blacklotus2"};
			for(String name : names) {
				images.add(loadImage(name + ".png", null));
			}
		}
		return images;
	}
	
	private Image getSubImageByIndex(BufferedImage master, int subunitsX, int subunitsY, int xOffset, int yOffset, int xIncrement, int yIncrement, int subunitWidth, int subunitHeight, int index) {
		if(xIncrement == 0) {
			xIncrement = master.getWidth()/subunitsX;
		}
		if(yIncrement == 0) {
			yIncrement = master.getHeight()/subunitsY;
		}
		if(subunitWidth == 0) {
			subunitWidth = master.getWidth()/subunitsX;
		}
		if(subunitHeight == 0) {
			subunitHeight = master.getHeight()/subunitsY;
		}
		
		Point coors = new Point(index % subunitsX, (int) (index/subunitsX));
		Point p = new Point(xOffset + coors.x*xIncrement, yOffset + coors.y*yIncrement);
		Dimension d = new Dimension(subunitWidth, subunitHeight);
		return getSubImage(master, p, d);
	}
	
	private Image getSubImage(BufferedImage master, Point p, Dimension d) {
		BufferedImage img = master.getSubimage(p.x, p.y, d.width, d.height); //fill in the corners of the desired crop location here
		BufferedImage subImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = subImage.createGraphics();
		g.drawImage(img, 0, 0, null);
		return subImage;
	}
	
	private void print(String text) {
		console.setText(console.getText() + text);
	}
	
	private void println(String text) {
		print(text + "\n");
	}
}