package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun;

import com.edwardium.RPGEngine.GameEntity.GameAI.GameAI;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.IGameUsableItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Vector2D;

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

	public float maxCooldown = 0f;
	public float cooldown = 0;

	public float maxChargeup = 0f;
	public float chargeup = 0f;

	public float fireVelocity = 1f;

	protected GameItemGun(Vector2D position, String name) {
		super(position, name);
	}

	@Override
	public boolean canUse(GameCharacter by, Vector2D to, GameObject at) {
		return this.cooldown == 0;
	}

	@Override
	public abstract boolean use(GameCharacter by, Vector2D to, GameObject at);

	@Override
	public void update(float elapsedTime, float environmentDensity) {
		if (cooldown > 0) {
			if (chargeup < maxChargeup) {
				if (lastUse.by.ai.currentState == GameAI.CharacterState.CHARGING) {
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

		super.update(elapsedTime, environmentDensity);
	}
}
