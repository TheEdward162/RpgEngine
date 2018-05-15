package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun;

import com.edwardium.RPGEngine.GameEntity.GameAI.GameAI;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.IGameUsableItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.IO.JsonBuilder;
import com.edwardium.RPGEngine.Renderer.Animation.TextureAnimation;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;

public abstract class GameItemGun extends GameItem implements IGameUsableItem {

	protected class UseInfo {
		public final GameCharacter by;
		public final Vector2D to;
		public final GameObject at;

		public UseInfo(GameCharacter by, Vector2D to, GameObject at) {
			this.by = by;
			this.to = to;
			this.at = at;
		}
	}

	protected UseInfo lastUse = null;

	float maxCooldown = 0f;
	float cooldown = 0;

	float maxChargeup = 0f;
	float chargeup = 0f;

	public float fireVelocity = 1f;

	protected TextureAnimation fireAnimation;

	protected GameItemGun(Vector2D position, String name) {
		super(position, name);

		canPickup = true;
	}

	@Override
	public boolean canUse(GameCharacter by, Vector2D to, GameObject at) {
		boolean returnBool = true;

		if (to != null) {
			float diffAngle = by.getFacingDirection().angleBetween(Vector2D.subtract(to, by.position));
			returnBool = returnBool && diffAngle <= 0.01f;
		}
		return returnBool && this.cooldown == 0;
	}

	@Override
	public abstract boolean use(GameCharacter by, Vector2D to, GameObject at);

	@Override
	public boolean cancelUse() {
		return false;
	}

	@Override
	public float getCooldown() {
		return cooldown;
	}

	@Override
	public float getMaxCooldown() {
		return maxCooldown;
	}

	@Override
	public float getChargeup() {
		return chargeup;
	}

	@Override
	public float getMaxChargeup() {
		return maxChargeup;
	}

	@Override
	public void updatePhysics(float elapsedTime, float environmentDensity) {
		if (this.fireAnimation != null)
			this.fireAnimation.update(elapsedTime);

		if (cooldown > 0) {
			if (chargeup >= 0 && chargeup < maxChargeup) {
				if (lastUse != null && lastUse.by.ai.currentState == GameAI.CharacterState.CHARGING) {
					chargeup = Math.min(maxChargeup, chargeup + elapsedTime);
				} else {
					// cancel charge
					this.cancelUse();
				}
			} else {
				cooldown = Math.max(0, cooldown - elapsedTime);
				if (cooldown == 0)
					this.chargeup = 0;
			}
		}

		super.updatePhysics(elapsedTime, environmentDensity);
	}

	protected GameObject membersFromJson(JsonObject sourceObj) {
		super.membersFromJson(sourceObj);

		try {
			this.maxCooldown = (float)sourceObj.getJsonNumber("maxCooldown").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.cooldown = (float)sourceObj.getJsonNumber("cooldown").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.maxChargeup = (float)sourceObj.getJsonNumber("maxChargeup").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.chargeup = (float)sourceObj.getJsonNumber("chargeup").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			this.fireVelocity = (float)sourceObj.getJsonNumber("fireVelocity").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		// TODO: UseInfo and animations

		return this;
	}

	@Override
	protected JsonBuilder toJSONBuilder() {
		JsonBuilder builder = super.toJSONBuilder();

		if (maxCooldown > 0)
			builder.add("maxCooldown", maxCooldown).add_optional("cooldown", cooldown, 0f);


		if (maxChargeup > 0)
			builder.add("maxChargeup", maxChargeup).add_optional("chargeup", chargeup, 0f);

		return builder;
	}
}
