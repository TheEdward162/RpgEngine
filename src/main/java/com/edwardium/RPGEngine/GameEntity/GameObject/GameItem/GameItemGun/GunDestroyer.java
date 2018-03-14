package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun;

import com.edwardium.RPGEngine.Engine;
import com.edwardium.RPGEngine.GameEntity.GameAI.GameAI;
import com.edwardium.RPGEngine.GameEntity.GameAnimation;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile.DestroyerProjectile;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class GunDestroyer extends GameItemGun {

	private GameAnimation fireAnimation;

	public GunDestroyer(Vector2D position) {
		super(position, "Destroyer Gun");

		this.maxCooldown = 3f;

		this.maxChargeup = 5f;
		this.fireVelocity = 300f;

		this.fireAnimation = new GameAnimation(maxChargeup, 5, new TextureInfo("sheet1", new Vector2D(32, 32), new Vector2D(32, 32)), new Vector2D(32, 0));
		this.fireAnimation.jumpToZero = true;
	}

	@Override
	public TextureInfo getInventoryTexture() {
		return null;
	}

	@Override
	public TextureInfo getHeldTexture() {
		return this.fireAnimation.getCurrentTexture();
	}

	@Override
	public Vector2D getHeldSize() {
		return new Vector2D(64, 64);
	}

	@Override
	public boolean use(GameCharacter by, Vector2D to, GameObject at) {
		if (canUse(by, to, at)) {
			by.ai.currentState = GameAI.CharacterState.CHARGING;
			this.cooldown = maxCooldown;
			this.lastUse = new UseInfo(by,to, at);

			this.fireAnimation.run();

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void update(float elapsedTime, float environmentDensity) {
		this.fireAnimation.update(elapsedTime);

		if (this.chargeup == maxChargeup) {
			this.chargeup++;

			Vector2D velocityVector = Vector2D.subtract(this.lastUse.to, this.lastUse.by.position).setMagnitude(fireVelocity);
			DestroyerProjectile projectile = new DestroyerProjectile(Vector2D.add(this.lastUse.by.position, this.lastUse.by.getFacingDirection().setMagnitude(64f)), velocityVector);
			projectile.rotation = velocityVector.getAngle();
			Engine.gameEngine.registerGameObject(projectile);

			this.lastUse.by.ai.currentState = GameAI.CharacterState.IDLE;
		}

		super.update(elapsedTime, environmentDensity);
	}
}