package project.breakout.model;

import java.awt.Point;
import java.util.Timer;

import acm.program.GraphicsProgram;
import acm.util.RandomGenerator;
import project.breakout.controller.BreakoutController;
import project.breakout.controller.BreakoutTimer;
import project.breakout.controller.CollisionController;
import project.breakout.controller.CollisionWith;
import project.breakout.view.BreakoutBrick;
import project.breakout.view.BreakoutView;
import project.breakout.view.LighthouseView;

// TODO think about static or non-static use of this class!

/**
 * This class represents the main class of the breakout game. It takes a Canvas
 * from the BreakoutView-class and draws it on the drawing area. It is
 * controlled by the controller it initializes.
 * 
 * It can be found on GitHub via https://github.com/TiKo98/BreakoutProject
 */
@SuppressWarnings("serial")
public class BreakoutModel extends GraphicsProgram {
	private static int paddleWidth = 100;
	private static int paddleHeight = 10;
	private static int paddleX, paddleY;

	private int ballRadius = 3;
	private double ballX, ballY;
	private static int ballDirection = 290;

	private static int brickWidth = 40;
	private static int brickHeight = 15;
	BreakoutBrick[] brickArray;

	private static int framesPerSecond = 40;
	private static int pixelsPerSecond = 200;
	private long lastFrameAtTime;

	private static BreakoutView view;
	private static CollisionController collisionControl;
	private Timer timer;
	@SuppressWarnings("unused")
	private static BreakoutController controller;
	Thread timerThread;

	private static boolean gameStarted = false;
	private boolean gamePaused = false;

	/**
	 * RUN METHOD - HERE STARTS EVERYTHING!!!
	 */
	@Override
	public void run() {
		initView();
		initController();
		initLighthouse();
	}

	// ------------------initializing methods----------------------------

	/**
	 * Initializes the controller connected with this class.
	 */
	private void initController() {
		collisionControl = new CollisionController();
		controller = new BreakoutController(this, view);
	}

	/**
	 * Initializes the canvas which represents the model of the game in the current
	 * class.
	 */
	private void initView() {
		view = new BreakoutView(getWidth(), getHeight());

		// init paddle
		paddleX = (getWidth() - paddleWidth) / 2;
		paddleY = getHeight() - paddleHeight - 2;
		view.setPaddleLocation(paddleX, paddleY);
		view.setPaddleSize(paddleWidth, paddleHeight);

		// init ball
		ballX = paddleX + paddleWidth / 2;
		ballY = paddleY - 3 * ballRadius;
		view.setBallsPosition(ballX, ballY);
		view.setBallsRadius(ballRadius);

		// init bricks
		brickArray = new BreakoutBrick[3];
		for (int i = 0; i < brickArray.length; i++) {
			brickArray[i] = new BreakoutBrick(brickWidth, brickHeight);
			brickArray[i].setLocation(10 + i * (brickWidth + 10), 50);
		}
		view.updateBricks(brickArray);

		// init view
		removeAll();
		add(view, 0, 0);
	}

	private void initLighthouse() {
		LighthouseView.connectToLighthouse();
		LighthouseView.setBallsPosition(0, 0);
		view.setInfoText("connected");
	}

	// -------------methods for controller-----------
	/**
	 * This method is called by the controller when the mouse was moved.
	 * 
	 * @param point
	 */
	public void updateMouseLocation(Point point) {
		int mouseX = (int) point.getX();
		int paddleHalf = paddleWidth / 2;
		if (mouseX > paddleHalf && mouseX < view.getWidth() - paddleHalf) {
			paddleX = mouseX - paddleHalf;
			view.setPaddleLocation(paddleX, paddleY);

			// move ball over paddle if game not started yet
			if (!gameStarted) {
				ballX = mouseX;
				ballY = paddleY - 3 * ballRadius;
				view.setBallsPosition(ballX, ballY);
			}
		}
	}

	/**
	 * This method is called by the controller when the window was resized by the
	 * user.
	 * 
	 * @param width
	 *            the new width of the window.
	 * @param height
	 *            the new height of the window.
	 */
	public void resizedView(int width, int height) {
		paddleY = height - paddleHeight - 2;
		view.setSize(width, height);
		view.setPaddleLocation(paddleX, paddleY);
	}

	// --------------------game control methods----------------------------

	/**
	 * Called by the timer. Updates the ball position depending on { @code
	 * pixelsPerSecond} and the time gone by since the last frame.
	 */
	public void updateBallsPosition() {
		// compute time since the last frame was created
		double frameTime = (double) (System.currentTimeMillis() - lastFrameAtTime);
		view.setInfoText(String.valueOf(frameTime));
		frameTime /= 1000.0;
		lastFrameAtTime = System.currentTimeMillis();

		// move ball in last known direction
		double xMovedBy = pixelsPerSecond * frameTime * Math.sin(Math.toRadians(ballDirection));
		double yMovedBy = -pixelsPerSecond * frameTime * Math.cos(Math.toRadians(ballDirection));
		ballX += xMovedBy;
		ballY += yMovedBy;

		// The distance the ball moved is a^2 + b^2 = c^2
		double ballMoveDistance = Math.sqrt(Math.pow(xMovedBy, 2) + Math.pow(yMovedBy, 2));
		double pixelsPerFrametime = pixelsPerSecond * frameTime;
		assert ballMoveDistance <= pixelsPerFrametime + 0.01 : "Ball moves faster than pixelsPerSecond allows to!";

		// if there's a collision in the model NOW
		if (collisionControl.isWallCollisionInModel(this) || collisionControl.isBrickCollisionInModel(this)
				|| collisionControl.isPaddleCollisionInModel(this)) {
			ballDirection = directionAfterCollision();

			// clear up the ball direction although it works with directions > 360 and < 0.
			ballDirection = (ballDirection > 360) ? ballDirection - 360 : ballDirection;
			ballDirection = (ballDirection < 0) ? ballDirection + 360 : ballDirection;

			// go back to non-collision-state
			ballX -= xMovedBy;
			ballY -= yMovedBy;

			// compute new movement with now updated ballDirection
			xMovedBy = pixelsPerSecond * frameTime * Math.sin(Math.toRadians(ballDirection));
			yMovedBy = -pixelsPerSecond * frameTime * Math.cos(Math.toRadians(ballDirection));

			// update ball's position
			ballX += xMovedBy;
			ballY += yMovedBy;

			if (collisionControl.allBricksDestroyed(brickArray)) {
				levelDone();
			}

			// show infoText
			assert collisionControl
					.getLastCollisionWith() != null : "lastCollisionWith is null and should be displayed -> NullPointerException";
			view.setInfoText("Last Thing collided: " + collisionControl.getLastCollisionWith().toString());
		}

		view.setBallsPosition(ballX, ballY);
		// view.setInfoText("Balldirection: " + ballDirection);

	}

	/**
	 * Changes the direction of the ball after a collision with the paddle, upper,
	 * left or right wall. When the ball hits the bottom wall the game restarts.
	 * 
	 * @return the new direction of the ball.
	 */
	private int directionAfterCollision() {
		CollisionWith lastCollisionWith = collisionControl.getLastCollisionWith();
		switch (lastCollisionWith) {
		case LEFTWALL:
		case RIGHTWALL:
		case BRICK_Y_AXIS:
			return 360 - ballDirection;
		case UPPERWALL:
		case BRICK_X_AXIS:
			return 180 - ballDirection;
		case BOTTOMWALL:
			restartGame();
			return ballDirection;
		case PADDLE:
			return directionAfterPaddleCollision();
		default:
			return ballDirection;
		}
	}

	/**
	 * Changes the direction of the ball after it hit the paddle. The new ball
	 * direction depends on where the ball hits the paddle.
	 * 
	 * @return ballDirection, the new direction of the ball.
	 */
	private int directionAfterPaddleCollision() {
		double paddleHalfX = paddleX + paddleWidth / 2;
		double deviationFromPaddleMiddle = (ballX - paddleHalfX) / (paddleWidth / 2);

		// update ball direction with normal collision and make it depend on the
		// collision point
		ballDirection = 180 - ballDirection;
		ballDirection += deviationFromPaddleMiddle * 80;

		// clear up the ball direction to perform the next step properly
		ballDirection = (ballDirection > 360) ? ballDirection - 360 : ballDirection;
		ballDirection = (ballDirection < 0) ? ballDirection + 360 : ballDirection;

		// make sure that the ball jumps upwards after hitting the paddle
		ballDirection = (ballDirection > 90 && ballDirection < 180) ? 80 : ballDirection;
		ballDirection = (ballDirection < 270 && ballDirection >= 180) ? 280 : ballDirection;

		return ballDirection;
	}

	/**
	 * Deletes a brick from the brickArray and updates the view.
	 * 
	 * @param lastBrickCollided
	 *            The {@code BreakoutBrick} which collided with the ball.
	 */
	public void deleteBrickAfterCollision(BreakoutBrick lastBrickCollided) {
		for (int i = 0; i < brickArray.length; i++) {
			if (brickArray[i] != null && brickArray[i].equals(lastBrickCollided)) {
				brickArray[i] = null;
			}
		}
		view.removeBrick(lastBrickCollided);
	}

	// ----------------game states methods------------------

	/**
	 * Starting a new Game. This method is called by the controller when the user
	 * starts the game.
	 *
	 * @return {@code true} if game was started successfully, {@code false} if the
	 *         game is already running.
	 */
	public boolean startGame() {
		if (!gameStarted) {

			// set up a new timer which updates the ball's position depending on the frame
			// rate
			timer = new Timer();
			BreakoutTimer timerTask = new BreakoutTimer(this);
			lastFrameAtTime = System.currentTimeMillis();
			long frameTime = 1000 / framesPerSecond;
			timer.schedule(timerTask, 0, frameTime);
			gameStarted = true;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Sets the game to the beginning state.
	 */
	private void restartGame() {
		gameStarted = false;

		// stop timer
		timer.cancel();

		// Re-init view and controllers
		initView();
		initController();

		ballDirection = RandomGenerator.getInstance().nextInt(10) * 10 - 50;
	}

	/**
	 * Handles when a level is completed by the player.
	 */
	public void levelDone() {
		view.levelDone();
	}

	/**
	 * Pauses the game and the timer.
	 */
	public void pauseGame() {
		timer.cancel();
		gamePaused = true;
	}

	/**
	 * Continues the game and the timer starts running.
	 */
	public void continueGame() {
		// set up a new timer which updates the ball's position depending on the frame
		// rate
		timer = new Timer();
		BreakoutTimer timerTask = new BreakoutTimer(this);
		lastFrameAtTime = System.currentTimeMillis();
		long frameTime = 1000 / framesPerSecond;
		timer.schedule(timerTask, 0, frameTime);
		gamePaused = false;
		gameStarted = true;
	}

	// ---------Getters-------------------------
	/**
	 * Returns the ball radius.
	 * 
	 * @return ballRadius, the radius of the ball.
	 */
	public int getBallRadius() {
		return ballRadius;
	}

	/**
	 * Returns the ball's x-Position.
	 * 
	 * @return ballX, the x-Position of the ball.
	 */
	public double getBallX() {
		return ballX;
	}

	/**
	 * Returns the ball's y-Position.
	 * 
	 * @return ballY, the y-Position of the ball.
	 */
	public double getBallY() {
		return ballY;
	}

	/**
	 * Returns the array where the bricks are saved in.
	 * 
	 * @return brickArray
	 */
	public BreakoutBrick[] getBrickArray() {
		return brickArray;
	}

	/**
	 * Returns the width of the paddle.
	 * @return paddleWidth
	 */
	public static int getPaddleWidth() {
		return paddleWidth;
	}

	/** 
	 * Returns the height of the paddle.
	 * @return the paddleHeight
	 */
	public static int getPaddleHeight() {
		return paddleHeight;
	}

	/**
	 * Returns the paddle's x-Position.
	 * @return paddleX
	 */
	public static int getPaddleX() {
		return paddleX;
	}

	/**
	 * Returns the paddle's y-Position.
	 * @return paddleY
	 */
	public static int getPaddleY() {
		return paddleY;
	}

	/**
	 * Returns if the game is paused or not.
	 * @return {@code true} if game is paused, {@code false} if not.
	 */
	public boolean isGamePaused() {
		return gamePaused;
	}
}
