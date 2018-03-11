package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun;

import com.edwardium.RPGEngine.Engine;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile.PistolBullet;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Vector2D;

public class GunPistol extends GameItemGun {
	public GunPistol(Vector2D position) {
		super(position, "Pistol");

		this.maxCooldown = 5f;
		this.fireVelocity = 400f;
	}

	@Override
	public boolean use(GameCharacter by, Vector2D to, GameObject at) {
		if (canUse(by, to, at)) {
			this.cooldown = maxCooldown;
			this.lastUse = new UseInfo(by,to, at);

			Vector2D velocityVector = Vector2D.subtract(to, by.position).setMagnitude(fireVelocity);
			PistolBullet bullet = new PistolBullet(Vector2D.add(by.position, by.getFacingDirection().setMagnitude(28f)), velocityVector);
			bullet.rotation = velocityVector.getAngle();
			Engine.gameEngine.registerGameObject(bullet);

			return true;
		} else {
			return false;
		}
	}
}
