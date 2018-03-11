package com.edwardium.RPGEngine.GameEntity;

import com.edwardium.RPGEngine.Renderer.Texture;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class GameAnimation {

	public static Vector2D sheetSize = new Vector2D(512, 512);

	private float currentTime = 0f;
	private final float length;
	private final int steps;

	private final TextureInfo baseTexture;
	private final Vector2D textureOffsetJump;

	public boolean running = true;
	public boolean jumpToZero = false;
	public boolean loops = false;

	public GameAnimation(float length, int steps, TextureInfo baseTexture, Vector2D textureOffsetJump) {
		this.length = length;
		this.steps = steps;

		this.baseTexture = baseTexture;
		this.textureOffsetJump = textureOffsetJump;
	}

	public void run() {
		this.currentTime = 0f;
		this.running = true;
	}

	public void update(float elapsedTime) {
		if (!running)
			return;

		float newTime = currentTime + elapsedTime;
		if (newTime >= length) {
			running = false;
			if (loops) {
				newTime -= length;
				running = true;
			} else if (jumpToZero) {
				newTime = 0;
			} else {
				newTime = length;
			}
		}
		this.currentTime = newTime;
	}

	public TextureInfo getCurrentTexture() {
		int currentStep = Math.round(currentTime / length * steps);
		Vector2D currentOffset = new Vector2D(textureOffsetJump).multiply(currentStep).add(baseTexture.textureOffset);

		return new TextureInfo(baseTexture.textureName, currentOffset, baseTexture.textureSize);
	}

}
