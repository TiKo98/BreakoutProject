package project.breakout.view;

import java.awt.Color;

import acm.graphics.GArc;
import acm.graphics.GCompound;
import acm.graphics.GObject;
import acm.graphics.GRect;
import acm.graphics.GRectangle;
import jdk.nashorn.internal.ir.BreakableNode;

/**
 * This class represents the paddle in the BreakoutView.
 *
 */
public class BreakoutPaddle extends GCompound {
	private GRect rectOfPaddle;
	private GArc roundLeft;
	private GArc roundRight;

	/**
	 * Constructor for the BreakoutPaddle.
	 * 
	 * @param width
	 *            The desired width of the paddle.
	 * @param height
	 *            The desired height of the paddle.
	 * @param debug
	 *            {@code true} if debug mode is requested, {@code false} if not.
	 */
	public BreakoutPaddle(int width, int height, boolean debug) {
		// create a paddle with round edges
		rectOfPaddle = new GRect(width * 0.8, height);
		rectOfPaddle.setFilled(true);

		double arcWidth = width * 0.2;
		System.out.println("desired arcWidth: " + arcWidth / 2);
		roundLeft = new GArc(arcWidth, height, 90, 180);
		roundLeft.setFilled(true);
		System.out.println("roundLeft.getWidth():" + roundLeft.getWidth());

		roundRight = new GArc(arcWidth, height, 90, -180);
		roundRight.setFilled(true);

		// add to compound
		add(roundLeft);
		double rectOfPaddleX = roundLeft.getWidth();
		add(rectOfPaddle, rectOfPaddleX, 0);
		double roundRightX = width * 0.8;
		add(roundRight, roundRightX, 0);

		if (debug) {
			// try to show bounds of roundRight arc
			showBounds(roundRight);
			showBounds(roundLeft);
			showBounds(rectOfPaddle);
		}
	}

	/**
	 * Constructor for the BreakoutPaddle.
	 * 
	 * @param width
	 *            The desired width of the paddle.
	 * @param height
	 *            The desired height of the paddle.
	 */
	public BreakoutPaddle(int width, int height) {
		this(width, height, false);
	}

	private void showBounds(GObject obj) {
		GRectangle bounds = obj.getBounds();
		add(new GRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
	}

	/**
	 * Sets the color of the {@code BreakoutPaddle}.
	 */
	public void setColor(Color color) {
		roundLeft.setFillColor(color);
		roundRight.setFillColor(color);
		rectOfPaddle.setFillColor(color);

		roundLeft.setColor(color);
		roundRight.setColor(color);
		rectOfPaddle.setColor(color);
	}
	
	public void setSize(int width, int height) {
		this.removeAll();
		// TODO implement me!
	}

}
