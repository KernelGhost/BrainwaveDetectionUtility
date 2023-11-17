import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.Timer;

public class AirballGame extends JPanel implements KeyListener, ActionListener {
	private static final long serialVersionUID = 3471473953294856384L;
	
	// Window Size
	private final int intWidth;
	private final int intHeight;
	
	// Paddle Size
	private final int intPaddleWidth = 180;
	private final int intPaddleHeight = 20;
	
	// Paddle Position
	private int intPaddleX;
	private final int intPaddleY;
	
	// Paddle RateOfChange
	private int intDelta = 0;				// Stores distance and direction to be moved
	private final double dblDecay = 0.30;	// Determines rate of decay
	
	// Ball Size
	private final int intBallSize = 20;
	
	// Ball Position
	private int intBallPosX;
	private int intBallPosY;
	
	// Ball Direction
	private int intBallDirX = 0;
	private int intBallDirY = 0;
	
	// Color Fade
	private int intColourFadeCounter = 0;	// Used as a counter for the smooth background color fade effect
	
	// Game State
	private boolean boolPaddleHit = false;	// Stores if the ball hit the paddle this game loop cycle
	private int intGameState = 0;			// 0 --> 'Start Game', 1 --> 'Active Game', 2 --> 'Game Over'
	private int intScore;					// Stores the player's score
	private int intDelay = 14;				// Determines the speed at which the game runs
	private Timer timer;					// Controls game cycle speed using intDelay
	
	public AirballGame(int intWidth, int intHeight) {
		// Set Width and Height
		this.intWidth = intWidth;
		this.intHeight = intHeight;
		
		// Position paddle to the bottom and center of the screen
		intPaddleY = intHeight - intPaddleHeight;
		intPaddleX = (intWidth - intPaddleWidth)/2;
		
		// Reset score + Position ball to top and center of paddle
		Initialise();
		
		// Enable Keycapture
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		
		// Begin Timer
		timer = new Timer(intDelay, this);
		timer.start();
	}
	
	private void Initialise() {
		// Place ball to top and center of paddle
		intBallPosX = intPaddleX + (intPaddleWidth - intBallSize)/2;
		intBallPosY = intHeight - (intPaddleHeight + intBallSize);
		
		// Set the score to zero
		intScore = 0;
	}

	@Override
	public void actionPerformed(ActionEvent e) { // Called every intDelay milliseconds
		// Restart the game timer
		timer.start();
		
		// Exponential decay paddle movement animation
		if (intDelta != 0) {
			// If there is distance to move and the game is not over
			intPaddleX += (int) (intDelta * dblDecay);
			intDelta = (int) ((1 - dblDecay) * intDelta);
		}
		
		// Move paddle and ball depending on the game state
		if (intGameState == 0) { // START GAME
			// Move ball to center of paddle
			intBallPosX = intPaddleX + (intPaddleWidth - intBallSize)/2;
		} else if (intGameState == 1) { // ACTIVE GAME
			int intXAdjust = 0;
			int intYAdjust = 0;
			
			// Move the Ball
			intBallPosX += intBallDirX;
			intBallPosY += intBallDirY;
			
			// Check Paddle Collision
			Rectangle rectBall = new Rectangle(intBallPosX, intBallPosY, intBallSize, intBallSize);
			Rectangle rectPaddle = new Rectangle(intPaddleX, intHeight - intPaddleHeight, intPaddleWidth, intPaddleHeight);
			
			if(rectBall.intersects(rectPaddle)) {
				// Check if ball hit top or side of paddle
				rectBall.y -=2;
				if(rectBall.intersects(rectPaddle)) {
					// We hit the side of the paddle
					intBallDirX = -intBallDirX;
					
					// Perform any adjustments
					rectBall.x += intBallDirX;
					
					while (rectBall.intersects(rectPaddle)) {
						intXAdjust++;
						rectBall.x += intBallDirX;
					}
					
					intBallPosX += intXAdjust * intBallDirX;
				} else {
					// We hit the top of the paddle
					intBallDirY = -intBallDirY;
					
					// Perform any adjustments
					rectBall.y += intBallDirY;
					
					while (rectBall.intersects(rectPaddle)) {
						intYAdjust++;
						rectBall.y += intBallDirY;
					}
					
					intBallPosY += intYAdjust * intBallDirY;
				}
				
				intScore++;
				boolPaddleHit = true;
			}
			
			// Check Wall Collision
			if ((intBallPosX + intBallSize) >= intWidth | (intBallPosX <= 0)) {
				intBallDirX = -intBallDirX;
			}
			if (intBallPosY <= 0) {
				intBallDirY = -intBallDirY;
			}
			
			// Check Game Over
			if (intBallPosY >= intHeight + intBallSize) {
				intGameState = 2;
			}
		}
		
		// Repaint the JPanel
		repaint();
	}
	
	// Returns a color based on the game score
	private int[] GetColor(int intScore) {
		int[][] intColours = new int [][]{
			{153, 0, 0},	// Red
			{230, 138, 0},	// Orange
			{0, 102, 0},	// Green
			{0, 0, 153},	// Blue
			{102, 0, 153}	// Purple
		};
		
		int intNewColor[] = new int[] {0,0,0};
		int intWeight = intScore % 10;
		
		int[] intSelect = new int[]{0,0};
		intSelect[0] = ((intScore % 50) - (intScore % 10))/10;
		if (intSelect[0] == 4) {
			intSelect[1] = 0;
		} else {
			intSelect[1] = intSelect[0] + 1;
		}
		
		for (int intCtr = 0; intCtr < 3; intCtr++) {
			intNewColor[intCtr] = (int) ((10 - (intWeight + 1)) * ((double) intColours[intSelect[0]][intCtr]/10) + ((intWeight + 1) * (double) intColours[intSelect[1]][intCtr]/10));
			if (intNewColor[intCtr] > 255) {
				intNewColor[intCtr] = 255;
			}
		}
		
		return intNewColor;
	}
	
	public void paint(Graphics g) {
		// BACKGROUND
		Graphics2D g2 = (Graphics2D) g;
		
		int[] intTargetColor = GetColor(intScore);
		int[] intOldColor = intTargetColor;
		if (intScore > 0) {
			intOldColor = GetColor(intScore - 1);
		}
		
		if (boolPaddleHit) {
			intColourFadeCounter = 0;
			boolPaddleHit = false;
		}
		
		Color newColor;
		if (intColourFadeCounter < 100) {
			// Smooth Background Color Transition
			int[] intNewColor = new int[] {0,0,0};
			for (int intCtr = 0; intCtr < 3; intCtr++) {
				intNewColor[intCtr] = (int) ((100 - (intColourFadeCounter + 1)) * ((double) intOldColor[intCtr]/100) + ((intColourFadeCounter + 1) * (double) intTargetColor[intCtr]/100));
				if (intNewColor[intCtr] > 255) {
					intNewColor[intCtr] = 255;
				}
			}
			newColor = new Color(intNewColor[0],intNewColor[1],intNewColor[2]);
			intColourFadeCounter++;
		} else {
			// Keep Existing Background Color
			newColor = new Color(intTargetColor[0],intTargetColor[1],intTargetColor[2]);
		}
		
		GradientPaint blueToBlack = new GradientPaint(intWidth, 0, Color.BLACK, intWidth, intHeight, newColor);
        g2.setPaint(blueToBlack);
        g.fillRect(1, 1, intWidth, intHeight);
		
		// PADDLE
		g.setColor(Color.WHITE);
		g.fillRect(intPaddleX, intPaddleY, intPaddleWidth, intPaddleHeight);
		
		// BALL
		g.fillOval(intBallPosX, intBallPosY, intBallSize, intBallSize);
		
		// SCORE
		g.setFont(new Font("Lucida Grande", Font.PLAIN, 24)); 
		g.drawString("Score: " + intScore, 10, 25);
		
		// HEADINGS
		if (intGameState == 0) {
			// Start
			g.setFont(new Font("Lucida Grande", Font.PLAIN, 30));
		    FontMetrics metrics = g.getFontMetrics();
		    int intXCord = (intWidth - metrics.stringWidth("Press 'Space' To Begin")) / 2;
		    int intYCord = (intHeight - metrics.getHeight() + metrics.getAscent())/ 2;
		    g.drawString("Press 'Space' To Begin", intXCord, intYCord);
		} else if (intGameState == 2) {
			// Game Over
			g.setColor(Color.RED);
			g.setFont(new Font("Lucida Grande", Font.PLAIN, 50));
		    FontMetrics metrics = g.getFontMetrics();
		    int intXCord = (intWidth - metrics.stringWidth("Game Over!")) / 2;
		    int intYCord = (intHeight - metrics.getHeight() + metrics.getAscent()) / 2;
		    g.drawString("Game Over!", intXCord, intYCord);
		    // Restart
		    g.setColor(Color.WHITE);
			g.setFont(new Font("Lucida Grande", Font.PLAIN, 30));
		    metrics = g.getFontMetrics();
		    intXCord = (intWidth - metrics.stringWidth("Press 'Space' To Restart")) / 2;
		    intYCord = (intHeight - metrics.getHeight() + metrics.getAscent()) / 2 + 40;
		    g.drawString("Press 'Space' To Restart", intXCord, intYCord);
		}
		
		// Dispose
		g.dispose();
		g2.dispose();
	}

	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		// Space Bar
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			if (intGameState == 0) {
				intGameState = 1;
				if((int) (Math.random() * 3) == 0) {
					intBallDirX = -1;
				} else {
					intBallDirX = 1;
				}
				
				intBallDirY = -2;
			} else if (intGameState == 2) {
				// Reset score + Further positioning of paddle and ball
				Initialise();
				
				// Set the game state to 'Start Game'
				intGameState = 0;
			}
		}
		
		// Enable code for crude keyboard control of paddle
		/*
		int intPaddleStep = 10;
		// Right Key
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			if (intPaddleX + intPaddleWidth + intPaddleStep > intWidth) {
				intPaddleX = intWidth - intPaddleWidth;
			} else {
				intPaddleX += intPaddleStep;
			}
		}
		// Left Key
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			if (intPaddleX - intPaddleStep < 0) {
				intPaddleX = 0;
			} else {
				intPaddleX -= intPaddleStep;
			}
		}
		*/
	}
	
	public void MovePaddle(ArrayList<int[]> arrlistForceData) {
		if (arrlistForceData.size() != 0) {
			// Use only the most recent value
			int intAttn = arrlistForceData.get(arrlistForceData.size() - 1)[1];
			
			// Scale input to use values between 25 and 75 as the extremes
			intAttn = (2 * intAttn) - 50;
			if (intAttn > 100) {
				intAttn = 100;
			} else if (intAttn < 0) {
				intAttn = 0;
			}
			
			// Calculate the new position for the paddle
			double dblNewPos = ((double) intAttn/100) * (intWidth - intPaddleWidth);
			
			// Calculate the distance and direction to be moved by the paddle
			intDelta = (int) (dblNewPos - intPaddleX);
		}
	}
}