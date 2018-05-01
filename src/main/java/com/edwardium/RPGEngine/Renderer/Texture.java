package com.edwardium.RPGEngine.Renderer;

import com.edwardium.RPGEngine.Utility.Vector2D;

public abstract class Texture {
	protected Texture() {
	}

	public abstract boolean isLoaded();
	public abstract int getTextureID();
	public abstract int getTextureUnit();
	public abstract Vector2D getSize();

	public abstract float[] computeSubtexture(Vector2D offset, Vector2D size);
}
