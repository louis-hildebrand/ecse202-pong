package ppPackage;

import acm.program.GraphicsProgram;
import acm.util.RandomGenerator;
import static ppPackage.ppSimParams.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;

/**
 * The main class for the program. Serves as the entry point—setting up the user
 * interface and objects like the paddles and ball—and manages the interactive
 * elements of the GUI.
 */
@SuppressWarnings("serial")
public class ppSimPaddleAgent extends GraphicsProgram
{
	private ppPaddle paddle;
	private ppPaddleAgent agent;
	private ppTable table;
	private ppBall ball;
	private RandomGenerator rgen = RandomGenerator.getInstance();
	private boolean traceOn;
	private int agentScore;
	private JLabel agentScoreBoard;
	private int playerScore;
	private JLabel playerScoreBoard;
	private JSlider timeFactor;
	private JSlider agentReactTime;

	/**
	 * The entry point for the program. Sets up the user interface, paddle, agent,
	 * and ball. Then, automatically starts the first round of play.
	 * 
	 * Based on code snippets provided by Prof. Frank Ferrie
	 */
	public void init()
	{
		traceOn = true;

		resize(SCR_WIDTH + BORDER, SCR_HEIGHT + BORDER);

		rgen.setSeed(RSEED);

		// Set up GUI

		/*
		 * Trace
		 * 
		 * Note: This is implemented as a regular button to allow the trace to be
		 * toggled back and forth at any time. Prof. Ferrie confirmed during office
		 * hours that this is allowed.
		 */
		JButton trace = new JButton("Toggle trace");
		trace.setActionCommand("TOGGLE TRACE");
		add(trace, SOUTH);
		// New serve
		JButton newServe = new JButton("New serve");
		newServe.setActionCommand("NEW SERVE");
		add(newServe, SOUTH);
		// Space
		add(new JLabel("   "), SOUTH);
		// Slider to control game speed (i.e. conversion of real time to in-game time)
		add(new JLabel("fast game"), SOUTH);
		timeFactor = new JSlider(1500, 5000, 5000);
		add(timeFactor, SOUTH);
		add(new JLabel("slow game"), SOUTH);
		// Space
		add(new JLabel("    "), SOUTH);
		// Agent react time slider
		add(new JLabel("fast agent"), SOUTH);
		agentReactTime = new JSlider(100, 200, 200);
		add(agentReactTime, SOUTH);
		add(new JLabel("slow agent"), SOUTH);
		// Space
		add(new JLabel("    "), SOUTH);
		// Clear screen and score
		JButton clear = new JButton("Restart");
		clear.setActionCommand("CLEAR");
		add(clear, SOUTH);
		// Quit
		JButton quit = new JButton("Quit");
		quit.setActionCommand("QUIT");
		add(quit, SOUTH);
		// Agent score
		agentScore = 0;
		add(new JTextField("Agent     "), NORTH);
		agentScoreBoard = new JLabel("  00  ");
		agentScoreBoard.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		agentScoreBoard.setMinimumSize(new Dimension(100, agentScoreBoard.getSize().height));
		add(agentScoreBoard, NORTH);
		// Player score
		playerScore = 0;
		playerScoreBoard = new JLabel("  00  ");
		playerScoreBoard.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		add(playerScoreBoard, NORTH);
		add(new JTextField("Player    "), NORTH);

		addMouseListeners();
		addActionListeners();

		// Create table, paddle, and agent
		table = new ppTable(this);
		paddle = new ppPaddle(PADDLE_XINIT, PADDLE_YINIT, PADDLE_COLOR, table);
		paddle.start();
		agent = new ppPaddleAgent(AGENT_XINIT, AGENT_YINIT, AGENT_COLOR, table);
		agent.start();

		startRound();
	}

	/**
	 * Generates a new ppBall object with random initial height, initial velocity,
	 * and energy loss factor. Also attaches the paddle and agent to the ball.
	 * 
	 * Based on code snippets provided by Prof. Frank Ferrie
	 * 
	 * @return A randomly-generated ppBall object
	 */
	public ppBall newBall()
	{
		double randYinit = rgen.nextDouble(YINIT_MIN, YINIT_MAX);
		double randLoss = rgen.nextDouble(LOSS_MIN, LOSS_MAX);
		double randV0 = rgen.nextDouble(V0_MIN, V0_MAX);
		double randTheta = rgen.nextDouble(THETA_MIN, THETA_MAX);

		ball = new ppBall(XINIT, randYinit, randV0, randTheta, BALL_COLOR, randLoss, table, traceOn);
		ball.setPaddle(paddle);
		ball.setAgent(agent);

		return ball;
	}

	/**
	 * Clears the display. Also unfreezes the paddle, allowing the user to move it
	 * around again.
	 */
	public void resetScreen()
	{
		table.newScreen();
		ball = null; // Prevent "toggle trace" from showing previous trace
		add(agent.getImage());
		add(paddle.getImage());
		paddle.unfreeze();
	}

	/**
	 * Gets a new ppBall using newBall() and starts a new round of play. In case
	 * there are objects remaining on the display from a previous round, the display
	 * is cleared using resetScreen().
	 */
	public void startRound()
	{
		resetScreen();

		ball = newBall();
		agent.attachBall(ball);
		agent.setReactTime(getAgentReactTime());

		ball.start();
	}

	/**
	 * Increments the agent's score by one on the scoreboard (up to a maximum of 99
	 * points)
	 */
	public void addPointAgent()
	{
		if (agentScore >= 99)
			return;

		agentScore++;
		agentScoreBoard.setText(String.format("  %02d  ", agentScore));
	}

	/**
	 * Increments the player's score by one on the scoreboard (up to a maximum of 99
	 * points)
	 */
	public void addPointPlayer()
	{
		if (playerScore >= 99)
			return;

		playerScore++;
		playerScoreBoard.setText(String.format("  %02d  ", playerScore));
	}

	/**
	 * Resets both the agent's score and the player's score to zero.
	 */
	private void clearScores()
	{
		agentScore = 0;
		agentScoreBoard.setText("  00  ");
		playerScore = 0;
		playerScoreBoard.setText("  00  ");
	}

	/**
	 * Provides access to the conversion factor from game time to real time, as
	 * specified by the user through the onscreen slider.
	 * 
	 * @return The conversion factor from TICK to milliseconds
	 */
	public int getTimeFactor()
	{
		return timeFactor.getValue();
	}

	/**
	 * Provides access to the agent's reaction time, as specified by the user
	 * through the onscreen slider.
	 * 
	 * @return The agent's reaction time (in milliseconds)
	 */
	public int getAgentReactTime()
	{
		return agentReactTime.getValue();
	}

	/**
	 * When the user moves the mouse, the paddle is moved to the same location
	 * (unless it is frozen)
	 * 
	 * Provided primarily by Prof. Frank Ferrie
	 */
	public void mouseMoved(MouseEvent e)
	{
		if (!paddle.isFrozen())
			paddle.setY(ppTable.scrToY((double) e.getY()));
	}

	/**
	 * When the user presses a button, performs the corresponding action.
	 * 
	 * Based on code snippets provided by Prof. Frank Ferrie
	 */
	public void actionPerformed(ActionEvent e)
	{
		switch (e.getActionCommand())
		{
			case "TOGGLE TRACE":
				traceOn = !traceOn;
				if (ball != null)
					ball.toggleTrace();
				break;
			case "CLEAR":
				if (ball != null && ball.ballInPlay())
					ball.interruptGame();
				agent.setY(AGENT_YINIT);
				agent.freeze();
				resetScreen();
				clearScores();
				break;
			case "NEW SERVE":
				if (ball == null || !ball.ballInPlay())
					startRound();
				break;
			case "QUIT":
				System.exit(0);
				break;
		}
	}
}
