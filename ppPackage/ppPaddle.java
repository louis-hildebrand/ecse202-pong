package ppPackage;

import static ppPackage.ppSimParams.*;
import java.awt.Color;
import acm.graphics.GRect;

/**
 * Models a ping pong paddle
 */
public class ppPaddle extends Thread
{
	private Color color;
	private double x, lastX, vx;
	private double y, lastY, vy;
	private GRect paddleImage;
	private ppTable table;
	private boolean frozen;

	/**
	 * Instantiates and draws a paddle object centered at (x, y)
	 * 
	 * @param x     The initial x-position of the center of the paddle (in m)
	 * @param y     The initial y-position of the center of the paddle (in m)
	 * @param color The paddle's color
	 * @param table A reference to the ppTable object used to control the simulation
	 *              display
	 */
	public ppPaddle(double x, double y, Color color, ppTable table)
	{
		// Copy arguments to instance variables, initialize velocity to 0
		this.color = color;
		this.x = x;
		this.y = y;
		this.lastX = x;
		this.lastY = y;
		this.vx = 0;
		this.vy = 0;
		this.table = table;
		this.frozen = false;

		// Create and draw GRect representation of the paddle
		this.paddleImage =
				new GRect(ppTable.toScrX(x - PADDLE_WIDTH / 2), ppTable.toScrY(y + PADDLE_HEIGHT / 2), SCALE * PADDLE_WIDTH, SCALE * PADDLE_HEIGHT);
		this.paddleImage.setFilled(true);
		paddleImage.setColor(color);
		this.table.getDisplay().add(this.paddleImage);
	}

	/**
	 * Keeps the paddle's velocity updated in real-time (or some factor of real-time
	 * if TIME_FACTOR is not 1000)
	 * 
	 * Provided entirely by Prof. Frank Ferrie
	 */
	public void run()
	{
		while (true)
		{
			if (!isFrozen())
			{
				vx = (x - lastX) / TICK;
				vy = (y - lastY) / TICK;
				lastX = x;
				lastY = y;
			}

			// Pause the paddle for TICK seconds (scaled by the current value of the time factor slider)
			table.getDisplay().pause(TICK * table.getDisplay().getTimeFactor());
		}
	}

	/**
	 * @return A reference to the GRect used to display the paddle
	 */
	public GRect getImage()
	{
		return paddleImage;
	}

	/**
	 * Stops the paddle from moving
	 */
	public void freeze()
	{
		frozen = true;
	}

	/**
	 * Allows the paddle to move freely
	 */
	public void unfreeze()
	{
		frozen = false;
	}

	/**
	 * @return TRUE if the paddle is currently frozen and FALSE otherwise
	 */
	public boolean isFrozen()
	{
		return frozen;
	}

	/**
	 * Moves the paddle to the specified x-position
	 * 
	 * @param newX The x-position (in m) the center of the paddle should be moved to
	 */
	public void setX(double newX)
	{
		lastX = x;
		x = newX;
		paddleImage.setLocation(ppTable.toScrX(newX - PADDLE_WIDTH / 2), ppTable.toScrX(y + PADDLE_HEIGHT / 2));
	}

	/**
	 * Moves the paddle to the specified y-position. The paddle is not allowed to go
	 * through the floor or ceiling.
	 * 
	 * @param newY The y-position the center of the paddle should be moved to
	 */
	public void setY(double newY)
	{
		lastY = y;
		if (newY <= PADDLE_HEIGHT / 2)
		{
			y = PADDLE_HEIGHT / 2;
			paddleImage.setLocation(ppTable.toScrX(x - PADDLE_WIDTH / 2), ppTable.toScrY(PADDLE_HEIGHT));
		}
		else if (newY >= YMAX - PADDLE_HEIGHT / 2)
		{
			y = YMAX - PADDLE_HEIGHT / 2;
			paddleImage.setLocation(ppTable.toScrX(x - PADDLE_WIDTH / 2), ppTable.toScrY(YMAX));
		}
		else if (newY >= PADDLE_HEIGHT / 2)
		{
			y = newY;
			paddleImage.setLocation(ppTable.toScrX(x - PADDLE_WIDTH / 2), ppTable.toScrY(newY + PADDLE_HEIGHT / 2));
		}
	}

	/**
	 * @return The current x-coordinate of the center of the paddle (in meters)
	 */
	public double getX()
	{
		return x;
	}

	/**
	 * @return The current y-coordinate of the center of the paddle (in meters)
	 */
	public double getY()
	{
		return y;
	}

	/**
	 * @return The current x-component of the paddle's velocity (in m/s)
	 */
	public double getVx()
	{
		return this.vx;
	}

	/**
	 * @return The current y-component of the paddle's velocity (in m/s)
	 */
	public double getVy()
	{
		return this.vy;
	}

	/**
	 * @return 1 if the paddle is moving up or is stationary and -1 otherwise
	 */
	public double getSgnVy()
	{
		return (vy >= 0 ? 1 : -1);
	}

	/**
	 * Determines whether a ball at point (Sx, Sy) is "in contact" with the paddle.
	 * 
	 * If the center of the ball is vertically between the top and bottom of the
	 * paddle, it is considered "in contact" on the assumption that the x-coordinate
	 * is such that the ball is at or past the paddle.
	 * 
	 * Otherwise, the distance from the ball's center to the nearest corner of the
	 * paddle is calculated. The ball is in contact with the paddle if and only if
	 * that distance is less than or equal to its radius.
	 * 
	 * @param Sx            The x-coordinate of the ball's center (in meters)
	 * @param Sy            The y-coordinate of the ball's center (in meters)
	 * @param ballFromRight TRUE if the ball is approaching the paddle from the
	 *                      right and FALSE otherwise
	 * @return TRUE if the ball at (Sx, Sy) is "in contact" with the paddle
	 */
	public boolean contact(double Sx, double Sy, boolean ballFromRight)
	{
		double paddleTop = y + PADDLE_HEIGHT / 2;
		double paddleBot = y - PADDLE_HEIGHT / 2;
		double paddleFront = (ballFromRight ? x + PADDLE_WIDTH / 2 : x - PADDLE_WIDTH / 2);

		//System.out.println("CONTACT");
		//System.out.printf("    Bot: %.4f Ball: %.4f Top: %.4f\n", paddleBot, Sy, paddleTop);

		if (Sy >= paddleBot && Sy <= paddleTop)
			return true;
		else if (Sy < paddleBot)
		{
			//System.out.printf("    |(%.2f, %.2f) - (%.2f, %.2f)| = %.2f\n", Sx, Sy, paddleFront, paddleBot, dist(Sx, Sy, paddleFront, paddleBot));
			return dist(Sx, Sy, paddleFront, paddleBot) <= BALL_RAD;
		}
		else
		{
			//System.out.printf("    |(%.2f, %.2f) - (%.2f, %.2f)| = %.2f\n", Sx, Sy, paddleFront, paddleBot, dist(Sx, Sy, paddleFront, paddleBot));
			return dist(Sx, Sy, paddleFront, paddleTop) <= BALL_RAD;
		}
	}

	/**
	 * Calculates the distance between two points
	 * 
	 * @param x0 The x-coordinate of the first point (in meters)
	 * @param y0 The y-coordinate of the first point (in meters)
	 * @param x1 The x-coordinate of the second point (in meters)
	 * @param y1 The y-coordinate of the second point (in meters)
	 * @return The distance between (x0, y0) and (x1, y1) (in meters)
	 */
	private double dist(double x0, double y0, double x1, double y1)
	{
		return Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
	}
}
