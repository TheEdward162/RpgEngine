package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;

public interface IGameActivableItem {
	boolean canActivate(GameCharacter by);
	boolean activate(GameCharacter by);
}
