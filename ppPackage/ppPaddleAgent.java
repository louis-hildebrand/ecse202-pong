package ppPackage;

import java.awt.Color;
import static ppPackage.ppSimParams.*;

/**
 * Models a computer-controlled paddle that plays against the user
 * 
 * @author louis
 */
public class ppPaddleAgent extends ppPaddle
{
	private ppTable table;
	private ppBall ball;
	private double vy;
	private int DELAY;

	/**
	 * Creates an agent with the specified initial conditions. The paddle is
	 * considered at first to have no reaction time (i.e. it responds instantly to
	 * changes in the ball's trajectory) and is frozen.
	 * 
	 * @param x     The initial x-coordinate of the center of the agent (in meters)
	 * @param y     The initial y-coordinate of the center of the agent (in meters)
	 * @param color The agent's color
	 * @param table A reference to the ppTable object controlling the display
	 */
	public ppPaddleAgent(double x, double y, Color color, ppTable table)
	{
		super(x, y, color, table);
		this.table = table;
		this.vy = 0;
		this.DELAY = 1; // Initialize DELAY to 1 (instant reactions) to prevent null pointers
		this.freeze();
	}

	/**
	 * Sets the reference to the ball the agent will follow
	 * 
	 * @param ball A reference to the ppBall currently in play
	 */
	public void attachBall(ppBall ball)
	{
		this.ball = ball;
		this.setY(ball.getY());
	}

	/**
	 * Controls the agent in such a way as to play against the user.
	 * 
	 * At set time intervals (specified by the agent reaction time slider), the
	 * agent will calculate the y-position it should aim for to intercept the next
	 * volley. It then determines the velocity needed to reach that position in time
	 * (up to the max speed specified in ppSimParams).
	 * 
	 * In every cycle, the agent is moved some small distance based on its current
	 * velocity and the time step (TICK)
	 */
	public void run()
	{
		// Initialize parameters
		this.setReactTime(table.getDisplay().getAgentReactTime());
		int n = 0;
		double timeToCollision = -1;
		double targetY = AGENT_YINIT;
		double currentY = AGENT_YINIT;

		while (true)
		{
			if (!isFrozen())
			{
				// Update the prediction
				if (n % DELAY == DELAY - 1)
				{
					timeToCollision = predictTime();
					targetY = predictY(timeToCollision);
					currentY = this.getY();
					if (timeToCollision == -1)
						vy = AGENT_MAX_SPEED * (AGENT_YINIT - currentY);
					else
					{
						int sgnVy = (targetY >= currentY ? 1 : -1);
						vy = sgnVy * Math.min(AGENT_MAX_SPEED, Math.abs((targetY - currentY) / timeToCollision));
					}
				}

				// Move the paddle according to its current y velocity
				setY(getY() + TICK * vy);

				// Increment the counter (mod DELAY so that the velocity is updated at set intervals)
				n = (n + 1) % DELAY;
			}

			// Pause the agent for TICK seconds (scaled by the current value of the time factor slider)
			this.table.getDisplay().pause(TICK * table.getDisplay().getTimeFactor());
		}
	}

	/**
	 * Updates the agent's reaction time
	 * 
	 * @param millis The new reaction time (in milliseconds)
	 */
	public void setReactTime(int millis)
	{
		DELAY = (int) Math.round(millis / (1000 * TICK));
	}

	/**
	 * Stops the agent from moving. Overrides the freeze method in ppPaddle to also
	 * make the agent's velocity zero (which prevents the agent from lurching up or
	 * down at the beginning of the next round).
	 */
	public void freeze()
	{
		super.freeze();
		this.vy = 0;
	}

	/**
	 * @return The y-component of the agent's velocity
	 */
	public double getVy()
	{
		return vy;
	}

	/**
	 * Predicts the time until the ball will reach the agent.
	 * 
	 * The estimate is made based on the ball's current x-position and velocity,
	 * assuming constant speed
	 * 
	 * @return The time until the ball will reach the agent (in seconds) or -1 if
	 *         the ball is not approaching the agent
	 */
	private double predictTime()
	{
		double timeToCollision;

		if (ball == null || ball.getVx() >= 0)
			timeToCollision = -1;
		else
			timeToCollision = (ball.getX() - AGENT_XINIT - PADDLE_WIDTH / 2) / -ball.getVx();

		return timeToCollision;
	}

	/**
	 * Determines where the agent should go to intercept the ball.
	 * 
	 * An estimate of the ball's y-position when it reaches the agent is obtained
	 * based on the ball's current y-position and velocity, assuming constant speed.
	 * If the ball is not approaching the agent, the agent returns to the center of
	 * the table. If the predicted position is below the floor or above the ceiling,
	 * the agent aims for the exact bottom or top of the table, respectively.
	 * 
	 * @param timeToCollision The time (in seconds) until the ball reaches the agent
	 *                        or -1 if the ball is not approaching the agent
	 * @return The y-position (in meters) the agent should try to reach
	 */
	private double predictY(double timeToCollision)
	{
		if (timeToCollision == -1)
			return AGENT_YINIT;
		else
		{
			double predictedY = ball.getY() + timeToCollision * ball.getVy();
			if (predictedY <= PADDLE_HEIGHT / 2)
				return PADDLE_HEIGHT / 2;
			else if (predictedY >= YMAX - PADDLE_HEIGHT / 2)
				return YMAX - PADDLE_HEIGHT / 2;
			else
				return predictedY;
		}
	}
}
