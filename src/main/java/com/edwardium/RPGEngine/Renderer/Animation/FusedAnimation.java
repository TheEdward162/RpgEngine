package com.edwardium.RPGEngine.Renderer.Animation;

import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Utility.Vector2D;

public class FusedAnimation extends TextureAnimation {

	private final ColorAnimation colorAnimation;

	public FusedAnimation(float length, int steps, TextureInfo baseTexture, Vector2D textureOffsetJump, ColorAnimation colorAnimation) {
		super(length, steps, baseTexture, textureOffsetJump);
		this.colorAnimation = colorAnimation;
	}

	@Override
	public void run() {
		super.run();
		colorAnimation.run();
	}

	@Override
	public void update(float elapsedTime) {
		super.update(elapsedTime);

		if (running) {
			colorAnimation.update(elapsedTime);
		}
	}

	@Override
	public TextureInfo getCurrentTexture() {
		int currentStep = Math.round(currentTime / length * steps);
		Vector2D currentOffset = new Vector2D(textureOffsetJump).scale(currentStep).add(baseTexture.textureOffset);

		Color currentColor = colorAnimation.getCurrentColor();
		return new TextureInfo(baseTexture.textureName, currentColor, currentOffset, baseTexture.textureSize);
	}
}
