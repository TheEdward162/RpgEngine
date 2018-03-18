package com.edwardium.RPGEngine;

public class FPSCounter {
	private static double history[] = null;

	private static int headPointer;

	public static void init(int length) {
		history = new double[length];
		headPointer = 0;
	}

	public static void update(double elapsedTime) {
		if (history == null || history.length <= 0)
			return;

		int nextIndex = headPointer + 1;
		if (nextIndex >= history.length)
			nextIndex -= history.length;

		history[nextIndex] = elapsedTime;
		headPointer = nextIndex;
	}

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
