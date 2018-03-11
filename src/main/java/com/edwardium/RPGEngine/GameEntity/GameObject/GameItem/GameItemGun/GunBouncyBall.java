package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun;

import com.edwardium.RPGEngine.Engine;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile.BouncyBallProjectile;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Vector2D;

public class GunBouncyBall extends GameItemGun {

	private BouncyBallProjectile currentProjectile = null;

	public GunBouncyBall(Vector2D position) {
		super(position, "Bouncy Ball Gun");

		this.fireVelocity = 40f;
	}

	@Override
	public boolean canUse(GameCharacter by, Vector2D to, GameObject at) {
		return this.currentProjectile == null;
	}

	@Override
	public boolean use(GameCharacter by, Vector2D to, GameObject at) {
		if (canUse(by, to, at)) {
			this.cooldown = maxCooldown;
			this.lastUse = new UseInfo(by,to, at);

			Vector2D velocityVector = Vector2D.subtract(to, by.position).setMagnitude(fireVelocity);
			BouncyBallProjectile projectile = new BouncyBallProjectile(Vector2D.add(by.position, by.getFacingDirection().setMagnitude(28f)), velocityVector);
			projectile.rotation = velocityVector.getAngle();

			this.currentProjectile = projectile;
			Engine.gameEngine.registerGameObject(projectile);

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void update(float elapsedTime, float environmentDensity) {
		if (this.currentProjectile != null && this.currentProjectile.toDelete)
			this.currentProjectile = null;

		super.update(elapsedTime, environmentDensity);
	}
}

