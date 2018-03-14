package com.edwardium.RPGEngine.GameEntity.GameAI;

import com.edwardium.RPGEngine.Engine;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GameItemGun;

public class SimpleEnemyAI extends GameAI {

	public SimpleEnemyAI(GameCharacter character) {
		super(character);
	}

	@Override
	public void onUpdate(float elapsedTime) {
		GameCharacter closestEnemy = Engine.gameEngine.getClosestCharacter(character, GameCharacter.CharacterRelationship.ENEMY);
		if (closestEnemy != null) {
			if (character.rotateToPoint(closestEnemy.position)) {
				if (character.inventory.getActiveItem() instanceof GameItemGun) {
					character.useActiveItem(closestEnemy.position, closestEnemy);
				}
			}
		}

		super.onUpdate(elapsedTime);
	}
}
