package com.edwardium.RPGEngine.GameEntity.GameAI;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.Control.SceneController.PlaySceneController;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GameItemGun;

import javax.json.JsonObject;

public class SimpleEnemyAI extends GameAI {

	public SimpleEnemyAI(GameCharacter character) {
		super(character);
	}
	public SimpleEnemyAI(GameCharacter character, JsonObject sourceObj) {
		super(character, sourceObj);
	}

	@Override
	public void onUpdate(float elapsedTime) {
		PlaySceneController gsc = Engine.gameEngine.getCurrentPlayController();
		GameCharacter closestEnemy = gsc.getClosestCharacter(character, GameCharacter.CharacterRelationship.ENEMY);
		if (closestEnemy != null) {
			if (character.rotateToPoint(closestEnemy.position)) {
				if (character.inventory.getActiveItem() instanceof GameItemGun) {
					character.useActiveItem(closestEnemy.position, closestEnemy);
				}
			}
		}

		super.onUpdate(elapsedTime);
	}

	@Override
	public JsonObject toJSON() {
		return super.toJSONBuilder().build();
	}
}
