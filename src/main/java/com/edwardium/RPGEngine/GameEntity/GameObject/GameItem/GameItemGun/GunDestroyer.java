package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.Control.SceneController.GameSceneController;
import com.edwardium.RPGEngine.GameEntity.GameAI.GameAI;
import com.edwardium.RPGEngine.GameEntity.GameAnimation.GameTextureAnimation;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile.DestroyerProjectile;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class GunDestroyer extends GameItemGun {

	private GameTextureAnimation fireAnimation;

	public GunDestroyer(Vector2D position) {
		super(position, "Destroyer Gun");

		this.maxCooldown = 0.3f;

		this.maxChargeup = 0.5f;
		this.fireVelocity = 3000f;

		this.fireAnimation = new GameTextureAnimation(maxChargeup, 5, new TextureInfo("sheet1", null, new Vector2D(32, 32), new Vector2D(32, 32)), new Vector2D(32, 0));
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
			this.lastUse = new UseInfo(by, new Vector2D(to), at);

			this.fireAnimation.run();

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean cancelUse() {
		if (this.cooldown > 0 && this.chargeup <= maxChargeup) {
			this.chargeup = maxChargeup + 1;
			this.fireAnimation.reset();

			return true;
		}

		return false;
	}

	@Override
	public void update(float elapsedTime, float environmentDensity) {
		this.fireAnimation.update(elapsedTime);

		if (this.chargeup == maxChargeup) {
			this.chargeup++;

			GameSceneController gsc = Engine.gameEngine.getCurrentGameController();
			if (gsc != null && gsc.canSpawnType(GameSceneController.SpawnType.PROJECTILE)) {
				Vector2D velocityVector = Vector2D.subtract(this.lastUse.to, this.lastUse.by.position).setMagnitude(fireVelocity);
				DestroyerProjectile projectile = new DestroyerProjectile(Vector2D.add(this.lastUse.by.position, this.lastUse.by.getFacingDirection().setMagnitude(64f)), velocityVector);
				projectile.rotation = velocityVector.getAngle();
				gsc.registerGameObject(projectile);
			}

			if (this.lastUse.by.ai.currentState == GameAI.CharacterState.CHARGING)
				this.lastUse.by.ai.currentState = GameAI.CharacterState.IDLE;
		}

		super.update(elapsedTime, environmentDensity);
	}
}
