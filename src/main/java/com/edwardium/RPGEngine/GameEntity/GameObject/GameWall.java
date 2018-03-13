package com.edwardium.RPGEngine.GameEntity.GameObject;

import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.Rectangle;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class GameWall extends GameObject {

	private Rectangle shape;
	public boolean penetrable = false;

	public GameWall(Vector2D position, Rectangle shape) {
		super(position, "Wall");

		this.hitbox = new GameHitbox(shape);
		this.shape = shape;

		this.mass = Float.POSITIVE_INFINITY;
	}

	@Override
	public void update(float elapsedTime, float environmentDensity) {
		super.update(elapsedTime, environmentDensity);
	}

	@Override
	public void render(Renderer gameRenderer) {
		if (isDrawn) {
			gameRenderer.drawRectangle(Rectangle.shiftBy(this.shape, this.position), this.rotation, new float[] { 0.3f, 0.3f, 0.3f, 1f }, new TextureInfo("default"));
		}
		super.render(gameRenderer);
	}

	@Override
	public void collideWith(GameObject other, Vector2D otherSideNormal) {

	}
}
