package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.Vector2D;

public abstract class GameProjectile extends GameItem {

	public float minimumSpeed = 35f;
	public boolean canPenetrate = true;

	public float maximumDistance = -1f;
	private float distanceTravelled = 0f;

	protected GameProjectile(Vector2D position, String name, Vector2D velocity) {
		super(position, name);

		this.velocity = velocity;
		this.mass = 0.008f;
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
	public void update(float elapsedTime, float environmentDensity) {
		super.update(elapsedTime, environmentDensity);

		this.distanceTravelled += this.velocity.getMagnitude() * elapsedTime;

		// delete the bullet if it's speed is too low or if it has travelled it's maximum distance
		if (velocity.getMagnitude() < minimumSpeed || (maximumDistance > 0 && distanceTravelled > maximumDistance))
			this.toDelete = true;
	}
}
