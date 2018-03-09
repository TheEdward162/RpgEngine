package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameBullet;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public abstract class GameBullet extends GameItem {

	public float minimumSpeed = 35f;
	public boolean canPenetrate = true;

	protected GameBullet(Vector2D position, String name, Vector2D velocity) {
		super(position, name);

		this.velocity = velocity;
	}

	@Override
	public void collideWith(GameObject other) {
		if (other instanceof GameWall) {
			if (canPenetrate && ((GameWall)other).penetrable) {
				// TODO: Attempt to penetrate
			} else {
				this.toDelete = true;
			}
		}
	}

	@Override
	public void update(float elapsedTime, float velocityDiminishFactor) {
		super.update(elapsedTime, velocityDiminishFactor);

		if (this.velocity.getMagnitude() < minimumSpeed)
			this.toDelete = true;
	}
}
