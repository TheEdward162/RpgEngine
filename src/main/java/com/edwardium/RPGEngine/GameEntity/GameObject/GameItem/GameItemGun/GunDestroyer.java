package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun;

import com.edwardium.RPGEngine.Engine;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile.DestroyerProjectile;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Vector2D;

public class GunDestroyer extends GameItemGun {
	public GunDestroyer(Vector2D position) {
		super(position, "Destroyer Gun");

		this.maxCooldown = 3f;

		this.maxChargeup = 5f;
		this.fireVelocity = 500f;
	}

	@Override
	public boolean use(GameCharacter by, Vector2D to, GameObject at) {
		if (canUse(by, to, at)) {
			this.cooldown = maxCooldown;
			this.lastUse = new UseInfo(by,to, at);

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void update(float elapsedTime, float environmentDensity) {
		if (this.chargeup == maxChargeup) {
			this.chargeup++;

			Vector2D velocityVector = Vector2D.subtract(this.lastUse.to, this.lastUse.by.position).setMagnitude(fireVelocity);
			DestroyerProjectile projectile = new DestroyerProjectile(Vector2D.add(this.lastUse.by.position, this.lastUse.by.getFacingDirection().setMagnitude(28f)), velocityVector);
			projectile.rotation = velocityVector.getAngle();
			Engine.gameEngine.registerGameObject(projectile);
		}

		super.update(elapsedTime, environmentDensity);
	}
}
