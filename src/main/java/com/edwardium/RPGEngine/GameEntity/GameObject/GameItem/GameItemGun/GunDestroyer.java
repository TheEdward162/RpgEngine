package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.Control.SceneController.GameSceneController;
import com.edwardium.RPGEngine.GameEntity.GameAI.GameAI;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile.DestroyerProjectile;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Renderer.Animation.TextureAnimation;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Light;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;

public class GunDestroyer extends GameItemGun {

	public GunDestroyer(Vector2D position) {
		super(position, "Destroyer Gun");

		this.maxCooldown = 0.5f;

		this.maxChargeup = 0.5f;
		this.fireVelocity = 3000f;

		this.fireAnimation = new TextureAnimation(maxChargeup, 5, new TextureInfo("sheet1", null, new Vector2D(32, 32), new Vector2D(32, 32)), new Vector2D(32, 0));
		this.fireAnimation.jumpToZero = true;
	}

	public GunDestroyer(JsonObject sourceObj) {
		this(Vector2D.fromJSON(sourceObj.getJsonObject("position")));
		super.membersFromJson(sourceObj);
	}

	@Override
	public TextureInfo getInventoryTexture() {
		return new TextureInfo("sheet1", null, new Vector2D(0, 32), new Vector2D(32, 32));
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
	public void updateLights(GameSceneController gsc) {
		int animStep = fireAnimation.getStep();
		if (animStep > 0) {
			gsc.greatestFunctionEVER(new Light(new Vector2D(28f, 0f).setAngle(this.rotation).add(this.position), new Color(1f, 0.502f, 0f), (float) animStep));
		}
		super.updateLights(gsc);
	}

	@Override
	public void updatePhysics(float elapsedTime, float environmentDensity) {
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

		super.updatePhysics(elapsedTime, environmentDensity);
	}

	@Override
	public void render(Renderer gameRenderer) {
		if (isDrawn) {
			gameRenderer.drawRectangle(new Renderer.RenderInfo(this.position, 64f, this.rotation,
					getInventoryTexture(), true));
		}
		super.render(gameRenderer);
	}

	public JsonObject toJSON() {
		return super.toJSONBuilder().add_optional("fireVelocity", fireVelocity, 3000f).build();
	}
}
