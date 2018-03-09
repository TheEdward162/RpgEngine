package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Vector2D;

public interface IGameUsableItem {
	public boolean canUse(GameCharacter by, Vector2D to, GameObject at);
	public boolean use(GameCharacter by, Vector2D to, GameObject at);
}
