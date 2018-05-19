package com.edwardium.RPGEngine.Control.SceneController.Editor;

import com.edwardium.RPGEngine.Control.UI;
import com.edwardium.RPGEngine.GameEntity.GameAI.SimpleEnemyAI;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GunBouncyBall;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GunDestroyer;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GunPistol;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GunSMG;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Utility.Vector2D;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;

public class SpawnerTool extends Tool {
	private static TextureInfo ICON = new TextureInfo("editor", null, new Vector2D(192, 0), new Vector2D(64, 64));

	private interface SpawnActionFn {
		GameObject invoke(Vector2D pos);
	}

	private static class SpawnAction {
		private final SpawnActionFn spawnActionFn;
		public final String name;

		public SpawnAction(String name, SpawnActionFn spawnActionFn) {
			this.name = name;
			this.spawnActionFn = spawnActionFn;
		}
	}

	private static final SpawnAction[] ACTIONS = new SpawnAction[] {
		new SpawnAction("Pistol", GunPistol::new),
		new SpawnAction("SMG", GunSMG::new),
		new SpawnAction("Bouncy Ball Gun", GunBouncyBall::new),
		new SpawnAction("Destroyer Gun", GunDestroyer::new),
		new SpawnAction("Enemy Trianglehead", (pos) -> {
			GameCharacter character = new GameCharacter(pos, "Enemy Trianglehead", 3);
			character.ai = new SimpleEnemyAI(character);
			character.factionFlag = GameCharacter.CharacterFaction.addFaction(character.factionFlag, GameCharacter.CharacterFaction.TRIANGLEHEADS);
			character.maxRotationSpeed = .1f;

			return character;
		}),
		new SpawnAction("Enemy Bubblenose", (pos) -> {
			GameCharacter character = new GameCharacter(pos, "Enemy Bubblenose", 1);
			character.ai = new SimpleEnemyAI(character);
			character.factionFlag = GameCharacter.CharacterFaction.addFaction(character.factionFlag, GameCharacter.CharacterFaction.BUBBLENOSES);
			character.maxRotationSpeed = .2f;

			return character;
		}),
	};
	private int spawnActionIndex = 0;

	public SpawnerTool() {
		super("Spawner Tool", ICON);
	}

	@Override
	public boolean updateInput(EditorSceneController esc, Input gameInput, double unprocessedTime) {
		esc.mainTransformTool.updateInput(esc, gameInput, unprocessedTime);

		if (gameInput.getMouseJustPressed(EditorSceneController.TOOLS_MOUSE_BUTTON, unprocessedTime)) {

			// Spawn currently selected spawn object
			Vector2D spawnPos = new Vector2D(esc.cursorPos);
			GameObject newObject = ACTIONS[spawnActionIndex].spawnActionFn.invoke(spawnPos);
			esc.getGameObjects().add(newObject);

			gameInput.lockKey(EditorSceneController.TOOLS_MOUSE_BUTTON);
		}

		int indexChange = 0;
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_Q, unprocessedTime)) {
			indexChange--;
		}
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_E, unprocessedTime)) {
			indexChange++;
		}
		if (indexChange != 0) {
			spawnActionIndex += indexChange;
			while (spawnActionIndex < 0)
				spawnActionIndex += ACTIONS.length;
			while (spawnActionIndex >= ACTIONS.length)
				spawnActionIndex -= ACTIONS.length;
		}

		return true;
	}

	@Override
	public void render(EditorSceneController esc, Renderer renderer) {
		esc.mainTransformTool.render(esc, renderer);
		super.render(esc, renderer);
	}

	@Override
	public void renderUI(EditorSceneController esc, Renderer renderer, boolean drawIcon) {
		esc.mainTransformTool.renderUI(esc, renderer, false);
		UI.drawCornerString(renderer, UI.Corner.TOPRIGHT, "Spawn: " + ACTIONS[spawnActionIndex].name);

		super.renderUI(esc, renderer, drawIcon);
	}
}
