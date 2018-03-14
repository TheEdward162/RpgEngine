package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile;

import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.Rectangle;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class DestroyerProjectile extends GameProjectile {
	public DestroyerProjectile(Vector2D position, Vector2D velocity) {
		super(position, "Destroyer Projectile", velocity);

		this.hitbox = new GameHitbox(new Rectangle(new Vector2D(-13f, -4.5f), new Vector2D(13f, 4.5f)));
		this.maximumDistance = 9000; // around 200 meters irl

		this.damage = 50f;
	}

	@Override
	protected Vector2D calculateResistanceForce(float environmentDensity) {
		return new Vector2D();
	}

	@Override
	public void collideWith(GameObject other, Vector2D otherSideNormal) {
		super.collideWith(other, otherSideNormal);

		if (other instanceof GameWall) {
			this.toDelete = true;
		}
	}

	@Override
	public void render(Renderer gameRenderer) {
		if (isDrawn) {
			gameRenderer.drawRectangle(this.position, new Vector2D(23f, 9f), this.velocity.getAngle(),
					new TextureInfo("default", new Color(1, 0.502f, 0)));
		}
		super.render(gameRenderer);
	}

}
