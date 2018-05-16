package com.edwardium.RPGEngine.Control.SceneController;

import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Renderer;

public abstract class SceneController {

	protected Input gameInput;
	protected SceneController(Input gameInput) {
		this.gameInput = gameInput;
	}

	/**
	 * @param unprocessedTime Time that has passed since last update. In nanoseconds.
	 */
	public abstract void update(double unprocessedTime);

	/**
	 * @param renderer Renderer to render the scene with.
	 */
	public abstract void render(Renderer renderer);

	/**
	 * Called when current scene controller changes but this one is kept in case we want to return to it.
	 */
	public abstract void freeze();

	/**
	 * Called to restore after freeze.
	 * @see SceneController#freeze()
	 */
	public abstract void restore();

	/**
	 * Clean up before destroying this instance.
	 */
	public abstract void cleanup();
}
