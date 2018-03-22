package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.Control.SceneController.GameSceneController;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile.BouncyBallProjectile;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class GunBouncyBall extends GameItemGun {

	private BouncyBallProjectile currentProjectile = null;

	public GunBouncyBall(Vector2D position) {
		super(position, "Bouncy Ball Gun");

		this.fireVelocity = 400f;
	}

	@Override
	public TextureInfo getInventoryTexture() {
		return null;
	}

	@Override
	public TextureInfo getHeldTexture() {
		return null;
	}

	@Override
	public Vector2D getHeldSize() {
		return new Vector2D(1, 1);
	}

	@Override
	public boolean canUse(GameCharacter by, Vector2D to, GameObject at) {
		return this.currentProjectile == null;
	}

	@Override
	public boolean use(GameCharacter by, Vector2D to, GameObject at) {
		if (canUse(by, to, at)) {
			GameSceneController gsc = Engine.gameEngine.getCurrentGameController();
			if (gsc != null && gsc.canSpawnType(GameSceneController.SpawnType.PROJECTILE)) {
				this.cooldown = maxCooldown;
				this.lastUse = new UseInfo(by, to, at);

				Vector2D velocityVector = Vector2D.subtract(to, by.position).setMagnitude(fireVelocity);
				BouncyBallProjectile projectile = new BouncyBallProjectile(Vector2D.add(by.position, by.getFacingDirection().setMagnitude(50f)), velocityVector);
				projectile.rotation = velocityVector.getAngle();

				this.currentProjectile = projectile;
				gsc.registerGameObject(projectile);

				return true;
			}

			return false;
		} else {
			return false;
		}
	}

	@Override
	public boolean cancelUse() {
		return false;
	}

	@Override
	public void update(float elapsedTime, float environmentDensity) {
		if (this.currentProjectile != null && this.currentProjectile.toDelete)
			this.currentProjectile = null;

		super.update(elapsedTime, environmentDensity);
	}
}

