package com.edwardium.RPGEngine.GameEntity.GameAnimation;

import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class GameTextureAnimation extends GameAnimation {
	private final int steps;

	private final TextureInfo baseTexture;
	private final Vector2D textureOffsetJump;

	public GameTextureAnimation(float length, int steps, TextureInfo baseTexture, Vector2D textureOffsetJump) {
		super(length);
		this.steps = steps;

		this.baseTexture = baseTexture;
		this.textureOffsetJump = textureOffsetJump;
	}

	public TextureInfo getCurrentTexture() {
		int currentStep = Math.round(currentTime / length * steps);
		Vector2D currentOffset = new Vector2D(textureOffsetJump).multiply(currentStep).add(baseTexture.textureOffset);

		return new TextureInfo(baseTexture.textureName, null, currentOffset, baseTexture.textureSize);
	}

}
