package com.edwardium.RPGEngine.GameEntity.GameAI;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;

public abstract class GameAI {
	public enum CharacterState { IDLE, CHARGING, LOCKED }

	protected final GameCharacter character;
	public CharacterState currentState;

	protected GameAI(GameCharacter character) {
		this.character = character;
		this.currentState = CharacterState.IDLE;
	}

	public abstract boolean canRotate();

	public abstract boolean canWalk();

	public abstract boolean canUseItem();

	public abstract void onUpdate(float elapsedTime);
}
