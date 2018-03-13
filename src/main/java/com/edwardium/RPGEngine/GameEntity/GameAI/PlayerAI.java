package com.edwardium.RPGEngine.GameEntity.GameAI;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;

public class PlayerAI extends GameAI {
	public PlayerAI(GameCharacter player) {
		super(player);

		currentState = CharacterState.IDLE;
	}

	@Override
	public boolean canRotate() {
		return currentState == CharacterState.IDLE;
	}

	@Override
	public boolean canWalk() {
		return currentState == CharacterState.IDLE;
	}

	@Override
	public boolean canUseItem() {
		return currentState == CharacterState.IDLE;
	}

	@Override
	public void onUpdate(float elapsedTime) {

	}
}
