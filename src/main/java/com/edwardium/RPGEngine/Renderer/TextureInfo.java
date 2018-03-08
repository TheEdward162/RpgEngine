package com.edwardium.RPGEngine.Renderer;

import com.edwardium.RPGEngine.Vector2D;

public class TextureInfo {

	public final String textureName;

	public final Vector2D textureOffset;
	public final Vector2D textureSize;

	public TextureInfo(String textureName) {
		this(textureName, null, null);
	}

	public TextureInfo(String textureName, Vector2D textureOffset) {
		this(textureName, textureOffset, null);
	}

	public TextureInfo(String textureName, Vector2D textureOffset, Vector2D textureSize) {
		this.textureName = textureName;
		this.textureOffset = textureOffset;
		this.textureSize = textureSize;
	}

}
