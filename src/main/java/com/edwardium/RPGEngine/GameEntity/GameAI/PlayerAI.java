package com.edwardium.RPGEngine.GameEntity.GameAI;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.Control.SceneController.PlaySceneController;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;
import java.util.EnumSet;

import static org.lwjgl.glfw.GLFW.*;

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

	public void updateInput(Input gameInput, double unprocessedTime) {
		PlaySceneController gsc = Engine.gameEngine.getCurrentPlayController();
		if (gsc == null)
			return;

		float walkX = 0;
		float walkY = 0;
		if (gameInput.getKeyPressed(GLFW_KEY_W)) {
			walkY -= 1;
		}
		if (gameInput.getKeyPressed(GLFW_KEY_S)) {
			walkY += 1;
		}

		if (gameInput.getKeyPressed(GLFW_KEY_D)) {
			walkX += 1;
		} if (gameInput.getKeyPressed(GLFW_KEY_A)) {
			walkX -= 1;
		}
		if (walkX != 0 || walkY != 0)
			character.walkTowards(new Vector2D(walkX, walkY));

		// calculate cursor position relative to the center of the screen and camera position
		character.rotateToPoint(gsc.cursorPos);

		// inventory
		int inventoryShift = 0;
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_UP, unprocessedTime)) {
			inventoryShift -= 1;
		}
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_DOWN, unprocessedTime)) {
			inventoryShift += 1;
		}
		if (inventoryShift != 0)
			character.inventory.shiftActiveIndex(inventoryShift);

		int inventoryIndex = -1;
		for (int i = GLFW_KEY_1; i < GLFW_KEY_9; i++) {
			if (gameInput.getKeyPressed(i)) {
				inventoryIndex = i - GLFW_KEY_1;
			}
		}
		if (inventoryIndex != -1)
			character.inventory.setActiveIndex(inventoryIndex);

		if (gameInput.getMousePressed(GLFW_MOUSE_BUTTON_1)) {
			if (character.inventory.getActiveItem() != null) {
				character.useActiveItem(gsc.cursorPos, null);
			} else if (canPickupItem()) {
				gameInput.lockKey(GLFW_MOUSE_BUTTON_1);

				GameItem closestItem = gsc.getClosestItem(gsc.cursorPos, EnumSet.of(PlaySceneController.ItemFilter.PICKUPABLE), character.pickupRange, character.position);
				if (closestItem != null) {
					character.inventory.swapWithActiveItem(closestItem);
				}
			}
		} else if (gameInput.getMousePressed(GLFW_MOUSE_BUTTON_2)) {
			character.dropActiveItem(20f);
		}
	}

	@Override
	public JsonObject toJSON() {
		return super.toJSONBuilder().build();
	}
}
