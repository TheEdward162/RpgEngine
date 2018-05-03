package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Utility.Vector2D;

public abstract class GameItem extends GameObject {

	public boolean canPickup = false;

	public GameItem(Vector2D position) {
		this(position, "");
	}

	public GameItem(Vector2D position, String name) {
		super(position);

		this.name = name;
	}

	public abstract TextureInfo getInventoryTexture();

	public abstract TextureInfo getHeldTexture();
	public abstract Vector2D getHeldSize();
}
