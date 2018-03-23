package com.edwardium.RPGEngine.Renderer.Animation;

import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class TextureAnimation extends Animation {
	protected final int steps;

	protected final TextureInfo baseTexture;
	protected final Vector2D textureOffsetJump;

	public TextureAnimation(float length, int steps, TextureInfo baseTexture, Vector2D textureOffsetJump) {
		super(length);
		this.steps = steps;

		this.baseTexture = baseTexture;

		if (textureOffsetJump == null)
			textureOffsetJump = new Vector2D();
		this.textureOffsetJump = textureOffsetJump;
	}

	public TextureInfo getCurrentTexture() {
		int currentStep = Math.round(currentTime / length * steps);
		Vector2D currentOffset = new Vector2D(textureOffsetJump).multiply(currentStep).add(baseTexture.textureOffset);

		return new TextureInfo(baseTexture.textureName, null, currentOffset, baseTexture.textureSize);
	}

}