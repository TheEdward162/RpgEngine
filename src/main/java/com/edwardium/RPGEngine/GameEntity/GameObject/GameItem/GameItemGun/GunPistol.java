package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.Control.SceneController.GameSceneController;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile.PistolBullet;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Renderer.Animation.TextureAnimation;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;

public class GunPistol extends GameItemGun {

	public GunPistol(Vector2D position) {
		super(position, "Pistol");

		this.maxCooldown = 0.5f;
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
			GameSceneController gsc = Engine.gameEngine.getCurrentGameController();
			if (gsc != null && gsc.canSpawnType(GameSceneController.SpawnType.PROJECTILE)) {
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
