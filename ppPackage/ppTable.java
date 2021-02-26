package ppPackage;

import static ppPackage.ppSimParams.*;
import java.awt.Color;
import acm.graphics.GRect;

/**
 * Utility class for displaying the simulation
 */
public class ppTable
{
	private ppSimPaddleAgent dispRef;

	/**
	 * Connects the ppTable to an instance of ppSimPaddle (i.e. the actual display)
	 * and draws the floor
	 * 
	 * @param dispRef A reference to an instance of ppSimPaddle which controls the
	 *                applet display
	 */
	public ppTable(ppSimPaddleAgent dispRef)
	{
		this.dispRef = dispRef;
	}

	public void newScreen()
	{
		// Clear all existing images
		dispRef.removeAll();

		// Add floor
		GRect floor = new GRect(0, SCR_HEIGHT, SCR_WIDTH + BORDER, WALL_THICKNESS_PX);
		floor.setFilled(true);
		floor.setColor(Color.BLACK);
		dispRef.add(floor);
	}

	/**
	 * Converts an x-coordinate in meters to its corresponding x-coordinate in ACM
	 * pixel units
	 * 
	 * @param xPos An x-coordinate in the world coordinate system (in meters, with
	 *             the origin at the bottom left)
	 * @return The corresponding x-coordinate in the ACM graphical coordinate system
	 *         (in pixels, with the origin at the top left)
	 */
	public static double toScrX(double xPos)
	{
		return SCALE * xPos;
	}

	/**
	 * Converts a y-coordinate in meters to its corresponding y-coordinate in ACM
	 * pixel units
	 * 
	 * @param yPos A y-coordinate in the world coordinate system (in meters, with
	 *             the origin at the bottom left)
	 * @return The corresponding y-coordinate in the ACM graphical coordinate system
	 *         (in pixels, with the origin at the top left)
	 */
	public static double toScrY(double yPos)
	{
		return SCR_HEIGHT - yPos * SCALE;
	}

	/**
	 * Converts an x-coordinate in ACM pixel units to the corresponding x-coordinate
	 * in meters
	 * 
	 * @param ScrX An x-coordinate in the ACM graphical coordinate system (in
	 *             pixels, with the origin at the top left)
	 * @return The corresponding x-coordinate in the world coordinate system (in
	 *         meters, with the origin at the bottom left)
	 */
	public static double scrToX(double ScrX)
	{
		return ScrX / SCALE;
	}

	/**
	 * Converts a y-coordinate in ACM pixel units to the corresponding y-coordinate
	 * in meters
	 * 
	 * @param ScrY A y-coordinate in the ACM graphical coordinate system (in pixels,
	 *             with the origin at the top left)
	 * @return The corresponding y-coordinate in the world coordinate system (in
	 *         meters, with the origin at the bottom left)
	 */
	public static double scrToY(double ScrY)
	{
		return (SCR_HEIGHT - ScrY) / SCALE;
	}

	/**
	 * Allows other classes to access the simulation display
	 * 
	 * @return A reference to the ppSimPaddle object (extending GraphicsProgram)
	 *         which controls the simulation display
	 */
	public ppSimPaddleAgent getDisplay()
	{
		return dispRef;
	}
}
