package ppPackage;

import java.awt.Color;

/**
 * Utility class containing all the constant program parameters
 * 
 * Most parameters provided by Prof. Frank Ferrie
 */
public class ppSimParams
{
	// Testing-related
	static final long RSEED = 8976232; // Fixed seed so that the initial conditions are known and repeated
	static final boolean TEST = false; // When TRUE, the time, position, and velocity are printed at each time step
	static final boolean DEBUG = false; // Enable debug messages and single step if true (?? only single step is actually implemented, and is never even used)
	static final boolean SHOW_TRACE = false; // Whether or not to add a dotted line to mark the ball's trajectory

	// Physical constants
	static final double G = 9.8; // Gravitational acceleration (in m/s)
	static final double K = 0.1316; // Coefficient of drag
	static final double KE_MIN = 0.001; // If the sum of the ball's kinetic energy in the x and y directions falls below this value, the ball stops
	static final double TICK = 0.01; // Time step (in sec)
	static final double BALL_MASS = 0.0027; // Ball's mass (in kg)
	static final double PADDLE_MASS = 0.1;
	static final double BALL_RAD = 0.02; // Ball's radius (in m)
	static final double VT = BALL_MASS * G / (4 * Math.PI * K * BALL_RAD * BALL_RAD); // Terminal velocity (m/s)
	static final double PADDLE_HEIGHT = 8 * 2.54 / 100; // Paddle height (in m)
	static final double PADDLE_WIDTH = 0.5 * 2.54 / 100; // Paddle width (in m)
	static final double AGENT_MAX_SPEED = 6;
	static final double AGENT_VX = 1.2;
	static final double PADDLE_VX = -1.2;
	static final double AGENT_VY_FACTOR = 0.4;
	static final double PLAYER_VY_FACTOR = 0.8;
	static final double VX_MAX = 9;
	static final double VY_MAX = 7;

	// Graphical parameters and important positions on table
	static final Color BALL_COLOR = Color.RED;
	static final Color PADDLE_COLOR = Color.GREEN;
	static final Color AGENT_COLOR = Color.BLUE;
	static final double PT_DIAMETER = 1;
	static final int SCR_WIDTH = 1080; // Distance from the left side of the applet to the right wall (in pixel units)
	static final int SCR_HEIGHT = 600; // Distance from the top of the applet to the floor (in pixel units)
	static final int BORDER = 150; // Extra space below and to the right of the table (in pixel units)
	static final double XMAX = 2.74; // Distance from the left side of the applet to the right wall (in m)
	static final double YMAX = 1.52; // Distance from the top of the applet to the floor (in m)
	static final double SCALE = SCR_HEIGHT / YMAX; // Pixels per meter
	static final double X_LEFT_WALL = 0.1; // Distance from the left side of the applet to the left wall (in m)
	static final double WALL_THICKNESS_PX = 2; // Thickness of the walls and floor (in pixel units)
	static final double PD = 1; // Diameter of trace points (in pixel units)
	static final double PADDLE_XINIT = XMAX - PADDLE_WIDTH / 2; // The initial x-position of the paddle's center (in m)
	static final double PADDLE_YINIT = YMAX / 2; // The initial y-position of the paddle's center (in m)
	static final double AGENT_XINIT = X_LEFT_WALL + PADDLE_WIDTH / 2;
	static final double AGENT_YINIT = YMAX / 2;
	static final double XINIT = AGENT_XINIT + PADDLE_WIDTH / 2 + BALL_RAD; // Initial x-position of the balls

	// Ranges for randomly-generated initial conditions
	static final double LOSS_MIN = 0.2; // Minimum for randomly-generated energy loss
	static final double LOSS_MAX = 0.2; // Maximum for randomly-generated energy loss
	static final double V0_MIN = 5; // Minimum for randomly-generated initial speed
	static final double V0_MAX = 5; // Maximum for randomly-generated initial speed
	static final double THETA_MIN = 0; // Minimum for randomly-generated launch angle
	static final double THETA_MAX = 20; // Maximum for randomly-generated launch angle
	static final double YINIT_MIN = 0.25 * YMAX; // Minimum for randomly-generated initial y-position
	static final double YINIT_MAX = 0.75 * YMAX; // Maximum for randomly-generated initial x-position
}
