package com.edwardium.RPGEngine.Control.SceneController;

import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Renderer;

public abstract class SceneController {

	protected Input gameInput;
	protected SceneController(Input gameInput) {
		this.gameInput = gameInput;
	}

	public abstract void update(double unprocessedTime);
	public abstract void render(Renderer renderer);
	public abstract void cleanup();
}
