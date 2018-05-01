package com.edwardium.RPGEngine.GameEntity.GameAI;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;

import javax.json.JsonObject;

public class PlayerAI extends GameAI {
	public PlayerAI(GameCharacter player) {
		super(player);
	}
	public PlayerAI(GameCharacter player, JsonObject sourceObj) {
		super(player, sourceObj);
	}

	@Override
	public void onUpdate(float elapsedTime) {
		super.onUpdate(elapsedTime);
	}

	@Override
	public JsonObject toJSON() {
		return super.toJSONBuilder().build();
	}
}
