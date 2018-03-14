package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun;

import com.edwardium.RPGEngine.Engine;
import com.edwardium.RPGEngine.GameEntity.GameAnimation;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile.PistolBullet;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class GunPistol extends GameItemGun {

	private GameAnimation fireAnimation;

	public GunPistol(Vector2D position) {
		super(position, "Pistol");

		this.maxCooldown = 5f;
		this.fireVelocity = 100f;

		this.fireAnimation = new GameAnimation(2f, 1, new TextureInfo("sheet1", new Vector2D(64, 0), new Vector2D(32, 32)), new Vector2D(-32, 0));
	}

	@Override
	public TextureInfo getInventoryTexture() {
		return new TextureInfo("sheet1", new Vector2D(0, 0), new Vector2D(32, 32));
	}

	@Override
	public TextureInfo getHeldTexture() {
		return fireAnimation.getCurrentTexture();
	}

	@Override
	public Vector2D getHeldSize() {
		return new Vector2D(32, 32);
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

			this.fireAnimation.run();

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void update(float elapsedTime, float environmentDensity) {
		this.fireAnimation.update(elapsedTime);

		super.update(elapsedTime, environmentDensity);
	}
}