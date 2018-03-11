package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem;

import com.edwardium.RPGEngine.Engine;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameBullet.GamePistolBullet;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Vector2D;

public class GameItemPistol extends GameItem implements IGameUsableItem {

	public float maxCooldown = 5;
	public float cooldown = 0;

	public float fireVelocity = 120f;

	public GameItemPistol(Vector2D position) {
		super(position, "Pistol");
	}

	@Override
	public boolean isUsable() {
		return true;
	}

	@Override
	public boolean canUse(GameCharacter by, Vector2D to, GameObject at) {
		return this.cooldown == 0;
	}

	@Override
	public boolean use(GameCharacter by, Vector2D to, GameObject at) {
		if (canUse(by, to, at)) {

			Vector2D velocityVector = Vector2D.subtract(to, by.position).setMagnitude(fireVelocity);
			GamePistolBullet bullet = new GamePistolBullet(Vector2D.add(by.position, by.getFacingDirection().setMagnitude(28f)), velocityVector);
			bullet.rotation = velocityVector.getAngle();
			Engine.gameEngine.registerGameObject(bullet);

			this.cooldown = maxCooldown;

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void update(float elapsedTime, float environmentDensity) {
		if (cooldown > 0) {
			cooldown = Math.max(0, cooldown - elapsedTime);
		}

		super.update(elapsedTime, environmentDensity);
	}
}
