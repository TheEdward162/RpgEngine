package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Utility.Vector2D;

public interface IGameUsableItem {
	boolean canUse(GameCharacter by, Vector2D to, GameObject at);
	boolean use(GameCharacter by, Vector2D to, GameObject at);
	boolean cancelUse();

	float getCooldown();
	float getMaxCooldown();

	float getChargeup();
	float getMaxChargeup();
}
