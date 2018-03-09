package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Vector2D;

public abstract class GameItem extends GameObject {

	public GameItem(Vector2D position) {
		this(position, "");
	}

	public GameItem(Vector2D position, String name) {
		super(position);

		this.name = name;
	}

	public abstract boolean isUsable();
}