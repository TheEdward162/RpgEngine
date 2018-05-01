package com.edwardium.RPGEngine.GameEntity.GameAI;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.IO.JsonBuilder;
import com.edwardium.RPGEngine.Utility.GameSerializable;

import javax.json.JsonObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class GameAI implements GameSerializable {
	public enum CharacterState { IDLE, CHARGING, LOCKED }

	protected final GameCharacter character;
	public CharacterState currentState;

	protected GameAI(GameCharacter character) {
		this.character = character;
		this.currentState = CharacterState.IDLE;
	}
	protected GameAI(GameCharacter character, JsonObject sourceObj) {
		this(character);

		try {
			currentState = CharacterState.values()[sourceObj.getJsonNumber("state").intValue()];
		} catch (ArrayIndexOutOfBoundsException | NullPointerException | ClassCastException ignored) { }
	}

	public boolean canRotate() {
		return currentState == CharacterState.IDLE;
	}

	public boolean canWalk() {
		return currentState == CharacterState.IDLE;
	}

	public boolean canWalkStop() {
		return currentState == CharacterState.IDLE || currentState == CharacterState.CHARGING;
	}

	public boolean canUseItem() {
		return currentState == CharacterState.IDLE;
	}

	public void onUpdate(float elapsedTime) {
		if (currentState == CharacterState.CHARGING && character.velocity.getMagnitude() != 0)
			this.currentState = CharacterState.IDLE;
	}

	public static GameAI fromJSON(JsonObject sourceObj, GameCharacter character) {
		String className = "PlayerAI";
		try {
			className = sourceObj.getString("cname");
		} catch (NullPointerException | ClassCastException ignored) { }

		GameAI result;
		try {
			Class<?> clazz = Class.forName(className);
			Constructor<?> ctor = clazz.getConstructor(GameCharacter.class, JsonObject.class);
			result =  (GameAI) ctor.newInstance(character, sourceObj);
		} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			result = new PlayerAI(character, sourceObj);
		}
		return result;
	}

	protected JsonBuilder toJSONBuilder() {
		return new JsonBuilder().add("cname", getClass().getSimpleName()).add_optional("state", currentState.ordinal(), CharacterState.IDLE.ordinal());
	}
}
