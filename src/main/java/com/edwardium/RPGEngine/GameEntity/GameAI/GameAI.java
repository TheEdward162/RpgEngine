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
}
