package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameBullet;

import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.Rectangle;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class GamePistolBullet extends GameBullet {
	public GamePistolBullet(Vector2D position, Vector2D velocity) {
		super(position, "Pistol Bullet", velocity);

		this.minimumSpeed = 50f;
		this.hitbox = new GameHitbox(new Rectangle(new Vector2D(-3.5f * 50, -1.5f * 50), new Vector2D(3.5f * 50, 1.5f * 50)));
	}

	@Override
	public void render(Renderer gameRenderer) {
		if (isDrawn) {
			gameRenderer.drawRectangle(this.position, new Vector2D(7, 3), this.velocity.getAngle(),
					new float[] { 0.4f, 0.4f, 0.4f, 1 }, new TextureInfo("default"));
		}
		super.render(gameRenderer);
	}

	@Override
	public boolean isUsable() {
		return false;
	}
}
