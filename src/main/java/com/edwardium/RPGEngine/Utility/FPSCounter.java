package com.edwardium.RPGEngine.Utility;

/**
 * Static class for counting FPS.
 */
public class FPSCounter {
	private static double history[] = null;

	private static int headPointer;

	private FPSCounter() {}

	/**
	 * Initializes the counter and sets the size of the internal history array.
	 *
	 * @param length Length of the time history.
	 */
	public static void init(int length) {
		history = new double[length];
		headPointer = 0;
	}

	/**
	 * @param elapsedTime Time that has elapsed since last frame.
	 */
	public static void update(double elapsedTime) {
		if (history == null || history.length <= 0)
			return;

		int nextIndex = headPointer + 1;
		if (nextIndex >= history.length)
			nextIndex -= history.length;

		history[nextIndex] = elapsedTime;
		headPointer = nextIndex;
	}

	/**
	 * @return Average FPS calculated from current history.
	 */
	public static float getFPS() {
		if (history == null || history.length <= 0)
			return 0;

		float average = 0;
		for (double aHistory : history) {
			average += aHistory;
		}
		average /= history.length;

		return (float)1E9 / average;
	}
}
