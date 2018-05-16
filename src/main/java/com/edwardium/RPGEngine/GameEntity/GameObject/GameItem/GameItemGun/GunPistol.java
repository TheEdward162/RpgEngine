package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.Control.SceneController.PlaySceneController;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile.PistolBullet;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Renderer.Animation.TextureAnimation;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Light;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;

public class GunPistol extends GameItemGun {

	public GunPistol(Vector2D position) {
		super(position, "Pistol");

		this.maxCooldown = 0.7f;
		this.fireVelocity = 1300f;

		this.fireAnimation = new TextureAnimation(0.2f, 1, new TextureInfo("sheet1", null, new Vector2D(64, 0), new Vector2D(32, 32)), new Vector2D(-32, 0));
		this.fireAnimation.jumpToEnd();
	}

	public GunPistol(JsonObject sourceObj) {
		this(Vector2D.fromJSON(sourceObj.getJsonObject("position")));
		super.membersFromJson(sourceObj);
	}

	@Override
	public boolean use(GameCharacter by, Vector2D to, GameObject at) {
		if (canUse(by, to, at)) {
			PlaySceneController gsc = Engine.gameEngine.getCurrentPlayController();
			if (gsc != null && gsc.canSpawnType(PlaySceneController.SpawnType.PROJECTILE)) {
				this.cooldown = maxCooldown;
				this.lastUse = new UseInfo(by, to, at);

				Vector2D velocityVector = Vector2D.subtract(to, by.position).setMagnitude(fireVelocity);
				PistolBullet bullet = new PistolBullet(by.getFacingDirection().setMagnitude(38f).add(by.position), velocityVector);
				bullet.rotation = velocityVector.getAngle();
				gsc.registerGameObject(bullet);

				this.fireAnimation.run();
				return true;
			}

			return false;
		} else {
			return false;
		}
	}

	@Override
	public TextureInfo getInventoryTexture() {
		return new TextureInfo("sheet1", null, new Vector2D(0, 0), new Vector2D(32, 32));
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
	public void updateLights(PlaySceneController gsc) {
		int animStep = fireAnimation.getStep();
		if (animStep == 0) {
			gsc.registerLight(new Light(new Vector2D(28f, 0f).setAngle(this.rotation).add(this.position), new Color(1f, 0.502f, 0f), 3f));
		}
		super.updateLights(gsc);
	}

	@Override
	public void render(Renderer gameRenderer) {
		if (isDrawn) {
			gameRenderer.drawRectangle(new Renderer.RenderInfo(this.position, 32f, this.rotation,
					getInventoryTexture(), true));
		}
		super.render(gameRenderer);
	}

	public JsonObject toJSON() {
		return super.toJSONBuilder().add_optional("fireVelocity", fireVelocity, 1000f).build();
	}
}
