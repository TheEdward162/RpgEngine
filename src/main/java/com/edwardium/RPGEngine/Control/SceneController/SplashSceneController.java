package com.edwardium.RPGEngine.Control.SceneController;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Animation.FusedAnimation;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Vector2D;

public class SplashSceneController extends SceneController {

	private final float maxTime;
	private float currentTime;

	private final FusedAnimation animation;

	public SplashSceneController(Input gameInput, FusedAnimation animation, float time) {
		super(gameInput);

		this.maxTime = time;
		this.currentTime = 0;

		this.animation = animation;
		animation.run();
	}

	@Override
	public void update(double unprocessedTime) {
		float elapsedTime = (float)(unprocessedTime * Engine.NANO_TIME_MULT);

		this.currentTime += elapsedTime;
		this.animation.update(elapsedTime);

		if (currentTime > maxTime) {
			Engine.gameEngine.restoreLastSceneController();
		}
	}

	@Override
	public void render(Renderer renderer) {
		renderer.drawRectangle(new Vector2D(), renderer.getWindowSize(), 0, this.animation.getCurrentTexture());
	}

	@Override
	public void cleanup() {

	}
}
