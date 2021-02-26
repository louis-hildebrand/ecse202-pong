package ppPackage;

import java.awt.Color;
import java.util.ArrayList;
import acm.graphics.GOval;
import static ppPackage.ppSimParams.*;

/**
 * Models the motion of a ping pong ball given certain initial conditions
 */
public class ppBall extends Thread
{
	private double Xinit;
	private double Yinit;
	private double V0;
	private double theta;
	private Color color;
	private double loss;
	private ppTable table;
	private GOval ball;
	private ppPaddle paddle;
	private ppPaddleAgent agent;
	private ArrayList<GOval> tracePts;
	private boolean traceOn;
	private boolean ballInPlay;

	private double x;
	private double y;
	private double vx;
	private double vy;

	/**
	 * Creates an instance of ppBall with the following parameters, and then adds
	 * its GOval representation to the display.
	 * 
	 * Based on code snippets provided by Prof. Frank Ferrie
	 * 
	 * @param Xinit   Initial x-position of the ball (measured at the ball's center,
	 *                in m)
	 * @param Yinit   Initial y-position of the ball (measured at the ball's center,
	 *                in m)
	 * @param V0      Initial speed (in m/s)
	 * @param theta   Launch angle (in degrees)
	 * @param color   Color of the ball
	 * @param loss    Collision energy loss factor (in the range [0, 1], where 0
	 *                means no loss and 1 means the ball stops on contact)
	 * @param table   Reference to the ppTable object that handles the display
	 * @param traceOn If TRUE, trace points are drawn as the ball moves
	 */
	public ppBall(double Xinit, double Yinit, double V0, double theta, Color color, double loss, ppTable table, boolean traceOn)
	{
		// Copy arguments to instance variables
		this.Xinit = Xinit;
		this.Yinit = Yinit;
		this.V0 = V0;
		this.theta = theta;
		this.color = color;
		this.loss = loss;
		this.table = table;
		this.traceOn = traceOn;

		// Initialize simulation instance variables
		ballInPlay = true;
		x = Xinit;
		y = Yinit;
		vx = V0 * Math.cos(theta * Math.PI / 180);
		vy = V0 * Math.sin(theta * Math.PI / 180);

		// Create the GOval representation of the ping pong ball and add it to the table
		ball = new GOval(ppTable.toScrX(Xinit - BALL_RAD), ppTable.toScrY(Yinit + BALL_RAD), 2 * BALL_RAD * SCALE, 2 * BALL_RAD * SCALE);
		ball.setFilled(true);
		ball.setColor(this.color);
		table.getDisplay().add(ball);

		// Initialize list of trace points
		tracePts = new ArrayList<GOval>();
	}

	/**
	 * Provides the ball with a reference to the player's paddle.
	 * 
	 * @param paddle The player's ppPaddle
	 */
	public void setPaddle(ppPaddle paddle)
	{
		this.paddle = paddle;
	}

	/**
	 * Provides the ball with a reference to the agent.
	 * 
	 * @param agent The ppPaddleAgent object to be used in this round.
	 */
	public void setAgent(ppPaddleAgent agent)
	{
		this.agent = agent;
	}

	/**
	 * Simulates the motion of the ping pong ball
	 * 
	 * Based on code snippets provided by Prof. Frank Ferrie
	 */
	public void run()
	{
		table.getDisplay().pause(3000); // Wait to let the user see the initial position of the ball
		agent.unfreeze(); // Let the agent start moving

		if (DEBUG)
			System.out.println("DEBUG mode enabled: press ENTER when the '>' prompt\nappears to move to the next step in the simulation");

		// Initialize simulation parameters
		double x0 = Xinit;
		double y0 = Yinit;
		double v0x = V0 * Math.cos(theta * Math.PI / 180);
		double v0y = V0 * Math.sin(theta * Math.PI / 180);
		double KEx;
		double KEy;
		double t = 0;

		// Main simulation loop
		while (ballInPlay)
		{
			// Get current position and velocity
			x = x0 + xDisp(t, v0x);
			y = y0 + yDisp(t, v0y);
			vx = xVel(t, v0x);
			vy = yVel(t, v0y);

			// Collision with floor
			if (vy < 0 && y - BALL_RAD <= 0)
			{
				KEx = 0.5 * BALL_MASS * vx * vx * (1 - loss);
				KEy = 0.5 * BALL_MASS * vy * vy * (1 - loss);

				// If the ball is hitting the floor (i.e. no potential energy) and has negligible kinetic energy, end round immediately
				if (KEx + KEy < KE_MIN)
				{
					if (vx < 0)
						finishGame(EndState.NO_ENERGY_PLAYER);
					else
						finishGame(EndState.NO_ENERGY_AGENT);
					break;
				}

				v0x = Math.sqrt(2 * KEx / BALL_MASS);
				v0y = Math.sqrt(2 * KEy / BALL_MASS);

				if (vx < 0)
					v0x = -v0x;

				x0 = x;
				y0 = BALL_RAD;
				y = y0; // Update y so that the ball gets printed at the right place
				vx = v0x; // Update vx and vy in case the ball also hits a wall in the same tick (i.e. corner collision) and the new velocity is needed
				vy = v0y;
				t = 0;
			}
			// Collision with agent or with left boundary
			if (vx < 0 && x - BALL_RAD <= agent.getX() + PADDLE_WIDTH / 2)
			{
				if (agent.contact(agent.getX() - PADDLE_WIDTH / 2 + BALL_RAD, y, true))
				{
					// Get the ball's velocity after the collision.
					// The agent's y-velocity is scaled down to keep the ball's velocity within a reasonable range.
					v0x = collisionVox(vx, AGENT_VX);
					v0y = collisionVoy(vy, AGENT_VY_FACTOR * agent.getVy());

					x0 = X_LEFT_WALL + BALL_RAD;
					x = x0; // Update x so that the ball gets printed at the right place
					y0 = y;
					vx = v0x; // Update vx and vy so that the correct values appear in the TEST output
					vy = v0y;
					t = 0;
				}
				// If the ball reaches the left "wall" but the agent isn't there to intercept it, the round ends
				else
				{
					finishGame(EndState.OUT_LEFT);
					break;
				}
			}
			// Collision with paddle or with right boundary
			if (vx > 0 && x + BALL_RAD >= paddle.getX() - PADDLE_WIDTH / 2)
			{
				if (paddle.contact(paddle.getX() + PADDLE_WIDTH / 2 - BALL_RAD, y, false))
				{
					// Get the ball's velocity after the collision.
					// The player's paddle's y-velocity is reduced to keep the ball's velocity within a reasonable range.
					v0x = collisionVox(vx, PADDLE_VX);
					v0y = collisionVoy(vy, PLAYER_VY_FACTOR * paddle.getVy());

					x0 = paddle.getX() - PADDLE_WIDTH / 2 - BALL_RAD;
					x = x0; // Update x so that the ball gets printed at the right place
					y0 = y;
					vx = v0x; // Update vx and vy so that the correct values appear in the TEST output
					vy = v0y;
					t = 0;
				}
				// If the ball is at the paddle's position but the paddle isn't there to intercept it, the round ends
				else
				{
					finishGame(EndState.OUT_RIGHT);
					break;
				}
			}
			// Collision with ceiling
			if (vy > 0 && y + BALL_RAD >= YMAX)
			{
				if (vx < 0)
					finishGame(EndState.TOP_PLAYER);
				else
					finishGame(EndState.TOP_AGENT);
				break;
			}

			if (TEST)
				System.out.printf("t: %.2f X: %.2f Y: %.2f Vx: %.2f Vy: %.2f\n", t, x, y, vx, vy);

			// Print ball and add a dot to plot the ball's trajectory
			ball.setLocation(ppTable.toScrX(x - BALL_RAD), ppTable.toScrY(y + BALL_RAD));
			trace(x, y);

			// Pause the ball for TICK seconds (scaled by the current value of the time factor slider to keep the game at a reasonable pace)
			table.getDisplay().pause(TICK * table.getDisplay().getTimeFactor());

			t += TICK;

			if (DEBUG)
				table.getDisplay().readLine(">");
		}

	}

	/**
	 * Allows other classes to access the GOval representation of the ball
	 * 
	 * @return A reference to the GOval object used to represent this ppBall
	 */
	public GOval getBall()
	{
		return ball;
	}

	/**
	 * Shows whether or not the ball is currently in play.
	 * 
	 * @return TRUE if the ball is still in play and FALSE otherwise
	 */
	public boolean ballInPlay()
	{
		return ballInPlay;
	}

	/**
	 * Ends the current round.
	 */
	public void interruptGame()
	{
		this.ballInPlay = false;
	}

	/**
	 * @return The current x-coordinate of the ball (in meters)
	 */
	public double getX()
	{
		return x;
	}

	/**
	 * @return The current y-coordinate of the ball (in meters)
	 */
	public double getY()
	{
		return y;
	}

	/**
	 * @return The x-component of the ball's velocity (in m/s)
	 */
	public double getVx()
	{
		return vx;
	}

	/**
	 * @return The y-component of the ball's velocity (in m/s)
	 */
	public double getVy()
	{
		return vy;
	}

	/**
	 * If the trace is currently being shown, removes all existing points and turns
	 * off the trace in the future (and vice-versa when the trace is currently not
	 * being shown).
	 */
	public void toggleTrace()
	{
		traceOn = !traceOn;

		for (GOval trace : tracePts)
		{
			if (traceOn)
				table.getDisplay().add(trace);
			else
				table.getDisplay().remove(trace);
		}
	}

	/**
	 * Performs all the necessary actions to finish the round.
	 * 
	 * The method draws the final position of the ball, draws the final trace point,
	 * updates the score, sets the ball not in play, and freezes both paddles.
	 * 
	 * @param state The reason the game ended (e.g. OUT_LEFT if the ball went out of
	 *              bounds on the left side, TOP_PLAYER if the ball went out of
	 *              bounds on top after being hit by the player).
	 */
	private void finishGame(EndState state)
	{
		// Print ball's final position and trace point, awards a point to the player or to the agent as appropriate
		switch (state)
		{
			case OUT_LEFT:
				ball.setLocation(ppTable.toScrX(agent.getX() - PADDLE_WIDTH / 2), ppTable.toScrY(y + BALL_RAD));
				trace(agent.getX() - PADDLE_WIDTH / 2 + BALL_RAD, y);
				table.getDisplay().addPointPlayer();
				break;
			case OUT_RIGHT:
				ball.setLocation(ppTable.toScrX(paddle.getX() + PADDLE_WIDTH / 2 - 2 * BALL_RAD), ppTable.toScrY(y + BALL_RAD));
				trace(paddle.getX() + PADDLE_WIDTH / 2 - BALL_RAD, y);
				table.getDisplay().addPointAgent();
				break;
			case TOP_AGENT:
				ball.setLocation(ppTable.toScrX(x - BALL_RAD), ppTable.toScrY(YMAX));
				trace(x, YMAX - BALL_RAD);
				table.getDisplay().addPointPlayer();
				break;
			case TOP_PLAYER:
				ball.setLocation(ppTable.toScrX(x - BALL_RAD), ppTable.toScrY(YMAX));
				trace(x, YMAX - BALL_RAD);
				table.getDisplay().addPointAgent();
				break;
			case NO_ENERGY_AGENT:
				ball.setLocation(ppTable.toScrX(x - BALL_RAD), ppTable.toScrY(2 * BALL_RAD));
				trace(x, BALL_RAD);
				table.getDisplay().addPointPlayer();
				break;
			case NO_ENERGY_PLAYER:
				ball.setLocation(ppTable.toScrX(x - BALL_RAD), ppTable.toScrY(2 * BALL_RAD));
				trace(x, BALL_RAD);
				table.getDisplay().addPointAgent();
				break;
		}

		// Set the ball not in play, freeze both paddles to clearly show final state
		ballInPlay = false;
		paddle.freeze();
		agent.freeze();
	}

	/**
	 * Draws a trace point if tracing is enabled. In either case, the trace point is
	 * added to the list of trace points to allow it to be displayed in the future,
	 * if necessary.
	 * 
	 * @param x The x-position at which to add the trace point (in meters)
	 * @param y The y-position at which to add the trace point (in meters)
	 */
	private void trace(double x, double y)
	{
		GOval trace = new GOval(ppTable.toScrX(x), ppTable.toScrY(y), PT_DIAMETER, PT_DIAMETER);
		tracePts.add(trace);
		if (traceOn)
			table.getDisplay().add(trace);
	}

	/**
	 * Calculates the ball's displacement in the x-direction at a given time
	 * 
	 * Formula provided by Prof. Ferrie
	 * 
	 * @param t   Time (in sec)
	 * @param v0x Initial x-velocity (in m/s)
	 * @return The ball's displacement in the x-direction after <code>t</code>
	 *         seconds
	 */
	private static double xDisp(double t, double v0x)
	{
		return VT * v0x / G * (1 - Math.exp(-G * t / VT));
	}

	/**
	 * Calculates the ball's displacement in the y-direction at a given time
	 * 
	 * Formula provided by Prof. Ferrie
	 * 
	 * @param t   Time (in sec)
	 * @param v0y Initial y-velocity (in m/s)
	 * @return The ball's displacement in the y-direction after <code>t</code>
	 *         seconds
	 */
	private static double yDisp(double t, double v0y)
	{
		return VT / G * (VT + v0y) * (1 - Math.exp(-G * t / VT)) - VT * t;
	}

	/**
	 * Calculates the x-component of the ball's velocity at a given time
	 * 
	 * Formula provided by Prof. Ferrie
	 * 
	 * @param t   Time (in sec)
	 * @param v0x Initial x-velocity (in m/s)
	 * @return The x-component of the ball's velocity after <code>t</code> seconds
	 */
	private static double xVel(double t, double v0x)
	{
		return v0x * Math.exp(-G * t / VT);
	}

	/**
	 * Calculates the y-component of the ball's velocity at a given time
	 * 
	 * Formula provided by Prof. Ferrie
	 * 
	 * @param t   Time (in sec)
	 * @param v0y Initial y-velocity
	 * @return The y-component of the ball's velocity after <code>t</code> seconds
	 */
	private static double yVel(double t, double v0y)
	{
		return Math.exp(-G * t / VT) * (v0y + VT) - VT;
	}

	/**
	 * Calculates the x-component of the ball's velocity after striking the player's
	 * paddle or the agent. If the speed exceeds the maximum horizontal speed
	 * specified in ppSimParams, that max speed is returned instead (with the
	 * appropriate sign).
	 * 
	 * The collision is modeled as perfectly elastic, with only the kinetic energy
	 * from the ball's horizontal velocity being considered. For the purposes of
	 * this calculation, the paddle and the agent are both considered to have some
	 * non-zero x-velocity.
	 * 
	 * @param ballVx   The x-component of the ball's velocity (in m/s)
	 * @param paddleVx The x-component of the paddle's velocity (in m/s)
	 * @return The ball's x-velocity (in m/s) immediately after striking the paddle
	 */
	private static double collisionVox(double ballVx, double paddleVx)
	{
		double v0x;
		double sgn;

		double a = BALL_MASS / PADDLE_MASS * (1 + BALL_MASS / PADDLE_MASS);
		double b = -2 * BALL_MASS / PADDLE_MASS * (BALL_MASS / PADDLE_MASS * ballVx + paddleVx);
		double c = (BALL_MASS / PADDLE_MASS * ballVx + paddleVx) * (BALL_MASS / PADDLE_MASS * ballVx + paddleVx)
				- (BALL_MASS / PADDLE_MASS * ballVx * ballVx + paddleVx * paddleVx);

		// If the paddle is "moving" to the right, take the larger (more positive) root
		if (paddleVx > 0)
			sgn = 1;
		// If the paddle is "moving" to the left, take the smaller (more negative) root
		else
			sgn = -1;

		v0x = (-b + sgn * Math.sqrt(b * b - 4 * a * c)) / (2 * a);

		if (Math.abs(v0x) > VX_MAX)
			return sgn * VX_MAX;
		else
			return v0x;
	}

	/**
	 * Calculates the y-component of the ball's velocity after striking the player's
	 * paddle or the agent. If the speed exceeds the maximum vertical speed
	 * specified in ppSimParams, that max speed is returned instead (with the
	 * appropriate sign).
	 * 
	 * The collision is modeled as perfectly elastic, with only the kinetic energy
	 * from the ball's vertical velocity being considered.
	 * 
	 * @param ballVy   The y-component of the ball's velocity (in m/s)
	 * @param paddleVy The y-component of the paddle's velocity (in m/s)
	 * @return The ball's y-velocity (in m/s) immediately after striking the paddle
	 */
	private static double collisionVoy(double ballVy, double paddleVy)
	{
		double v0y;
		double sgn;

		double a = BALL_MASS / PADDLE_MASS * (1 + BALL_MASS / PADDLE_MASS);
		double b = -2 * BALL_MASS / PADDLE_MASS * (BALL_MASS / PADDLE_MASS * ballVy + paddleVy);
		double c = (BALL_MASS / PADDLE_MASS * ballVy + paddleVy) * (BALL_MASS / PADDLE_MASS * ballVy + paddleVy)
				- (BALL_MASS / PADDLE_MASS * ballVy * ballVy + paddleVy * paddleVy);

		// If the paddle is moving up or the paddle is stationary and the ball is moving up, take the positive root
		if (paddleVy > 0 || (paddleVy == 0 && ballVy > 0))
			sgn = 1;
		// If the paddle is moving down or the paddle is stationary and the ball is moving down, take the negative root
		else
			sgn = -1;

		v0y = (-b + sgn * Math.sqrt(b * b - 4 * a * c)) / (2 * a);

		if (Math.abs(v0y) > VY_MAX)
			return sgn * VY_MAX;
		else
			return v0y;
	}

	/**
	 * Specifies the reason the round ended.
	 * 
	 * @author louis
	 */
	private enum EndState
	{
		// Ball out of bounds on the left side (i.e. agent missed)
		OUT_LEFT,
		// Ball out of bounds on the right side (i.e. player missed)
		OUT_RIGHT,
		// Ball hit out of bounds on top by the agent
		TOP_AGENT,
		// Ball hit out of bounds on top by the player
		TOP_PLAYER,
		// Ball ran out of energy and was last touched by the agent
		NO_ENERGY_AGENT,
		// Ball ran out of energy and was last touched by the player
		NO_ENERGY_PLAYER;
	}
}
