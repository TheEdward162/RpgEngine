package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile;

import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class BouncyBallProjectile extends GameProjectile {

	private int maximumBounces = 3;
	private int bounces = 0;

	public BouncyBallProjectile(Vector2D position, Vector2D velocity) {
		super(position, "Bouncy Ball of Death", velocity);

		this.hitbox = new GameHitbox(15f);
		this.maximumTime = 20f;
	}

	@Override
	protected Vector2D calculateResistanceForce(float environmentDensity) {
		return new Vector2D();
	}

	@Override
	public void collideWith(GameObject other, Vector2D otherSideNormal) {
		if (other instanceof GameWall) {
			if (bounces == maximumBounces) {
				this.toDelete = true;
			} else {
				bounces++;

				Vector2D collideSide = otherSideNormal.getNormal();

				Vector2D rejection = this.velocity.rejection(collideSide);
				this.velocity.subtract(rejection.multiply(2));

			}
		}
	}

	@Override
	public void render(Renderer gameRenderer) {
		if (isDrawn) {
			gameRenderer.drawCircle(15f, this.position, new float[] { 0f, 1f, 0.502f, 1f }, new TextureInfo("default"));
		}
		super.render(gameRenderer);
	}
}
