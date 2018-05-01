package com.edwardium.RPGEngine.Renderer;

import com.edwardium.RPGEngine.Utility.Vector2D;

public class TextureInfo {

	public final String textureName;

	public final Vector2D textureOffset;
	public final Vector2D textureSize;

	public final Color textureColor;

	public TextureInfo(String textureName) {
		this(textureName, null);
	}

	public TextureInfo(String textureName, Color color) {
		this(textureName, color, null, null);
	}

	public TextureInfo(String textureName, Color textureColor, Vector2D textureOffset, Vector2D textureSize) {
		this.textureName = textureName;
		this.textureColor = textureColor != null ? textureColor : new Color();

		if (textureOffset == null)
			textureOffset = new Vector2D();
		this.textureOffset = textureOffset;
		this.textureSize = textureSize;
	}

}
