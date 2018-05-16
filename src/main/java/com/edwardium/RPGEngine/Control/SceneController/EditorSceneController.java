package com.edwardium.RPGEngine.Control.SceneController;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;

import static org.lwjgl.glfw.GLFW.*;

public class EditorSceneController extends GameSceneController {

	private static final Float cursorSelectionRadius = 25f;

	public EditorSceneController(Input gameInput) {
		super(gameInput);

		if (!loadState("Saves/editor-exitsave.json")) {
			player = new GameCharacter(new Vector2D(), "Player", 10);
		}

		gameObjects.add(player);
	}

	@Override
	public void update(double unprocessedTime) {
		updateInput(unprocessedTime);
	}

	@Override
	protected boolean updateInput(double unprocessedTime) {
		if (!super.updateInput(unprocessedTime))
			return false;

		float cameraShiftX = 0;
		float cameraShiftY = 0;
		if (gameInput.getKeyPressed(GLFW_KEY_W)) {
			cameraShiftY -= 1;
		}
		if (gameInput.getKeyPressed(GLFW_KEY_S)) {
			cameraShiftY += 1;
		}

		if (gameInput.getKeyPressed(GLFW_KEY_D)) {
			cameraShiftX += 1;
		} if (gameInput.getKeyPressed(GLFW_KEY_A)) {
			cameraShiftX -= 1;
		}
		cameraPos.add(cameraShiftX, cameraShiftY);

		cursorPos = gameInput.getGameCursorPos().subtract(cameraPos);

		if (gameInput.getMousePressed(GLFW_MOUSE_BUTTON_1)) {
			player.position = new Vector2D(cursorPos);
		}

		return true;
	}

	@Override
	protected void updateGame(float elapsedTime, int currentUpdateIndex, int maxUpdateIndex) {

	}

	@Override
	public boolean loadState(String loadPath) {
		JsonObject root = loadStatePartial(loadPath);
		return root != null;
	}

	@Override
	public boolean saveState(String savePath) {
		return saveState(savePath, null);
	}

	@Override
	public void render(Renderer renderer) {
		renderer.setLightCount(1);
		renderer.setLight(0, PlaySceneController.ambientLight);

		GameObject closestObject = getClosestObject(cursorPos, cursorSelectionRadius);

		super.render(renderer, closestObject);
	}

	@Override
	public void freeze() {
		super.freeze();
	}

	@Override
	public void restore() {
		super.restore();
	}

	@Override
	public void cleanup() {
		saveState("Saves/editor-exitsave.json");

		super.cleanup();
	}
}
