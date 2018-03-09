package com.edwardium.RPGEngine;

import com.edwardium.RPGEngine.GameObject.*;
import com.edwardium.RPGEngine.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameObject.GameItem.GameItemPistol;
import com.edwardium.RPGEngine.GameObject.GameItem.IGameUsableItem;
import com.edwardium.RPGEngine.IO.Config;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.*;
import com.edwardium.RPGEngine.Renderer.OpenGL.OpenGLRenderer;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class Engine implements Runnable {
	private enum GameStage { MAIN_MENU, PAUSED_MENU, GAME };

	private static final float UPDATE_CAP = 1.0f / 60.0f;
	public static final float NANO_TIME_MULT = 10e-9f;

	public static Engine gameEngine;

	// Game objects
	private Thread gameThread;
	private Renderer gameRenderer;
	private Input gameInput;
	private Config gameConfig;

	private Vector2D cameraPos;

	private ArrayList<GameObject> gameObjects;
	private GameCharacter player;

	private boolean running = false;
	private GameStage gameStage = GameStage.GAME;

	private float velocityDiminishFactor = 0.95f;

	public Engine() {
		if (gameEngine != null)
			gameEngine.cleanup();
		gameEngine = this;
	}

	public void start() {
		if (gameThread != null) {
			return;
		}

		gameThread = new Thread(this);
		gameThread.run();
	}

	public void run() {
		if (running) {
			return;
		}

		// load config
		gameConfig = new Config("config.ini");

		// init variables
		cameraPos = new Vector2D();

		gameObjects = new ArrayList<>();

		// init player
		player = new GameCharacter(new Vector2D(5, 7), "player", 10);

		GameItem pistol = new GameItemPistol(new Vector2D(player.position));
		player.inventory.insertItem(pistol);
		registerGameObject(pistol);

		gameObjects.add(player);

		gameObjects.add(new GameCharacter());

		// whether to render this tick or skip rendering because nothing has updated
		boolean doRender = false;

		// time that is now
		double nowTime;
		// last time the loop was run
		double lastTime = System.nanoTime() * NANO_TIME_MULT;
		// how much time has elapsed since last time
		// divide by UPDATE_CAP to see how many updates we have missed
		double unprocessedTime = 0;

		// initialize game renderer based on config values
		switch (gameConfig.getString("renderer", "opengl").toLowerCase()) {
			default: // OpenGL
				gameRenderer = new OpenGLRenderer(gameConfig.getString("window-title", "RPGEngine"),
						gameConfig.getInt("window-width", 800),
						gameConfig.getInt("window-height", 600));
				break;
		}

		gameInput = new Input(gameRenderer.getWindowHandle());
		gameInput.watchKey(GLFW_KEY_UP);
		gameInput.watchKey(GLFW_KEY_DOWN);

		gameRenderer.show();

		running = true;
		while (running) {
			if (gameRenderer.shouldClose()) {
				running = false;
				break;
			}

			nowTime = System.nanoTime() * NANO_TIME_MULT;
			unprocessedTime += nowTime - lastTime;
			lastTime = nowTime;

			// only update input once, even if we do more game updates
			if (unprocessedTime >= UPDATE_CAP) {
				updateInput();
			}

			// update while we are all caught up
			while (unprocessedTime >= UPDATE_CAP) {
				if (gameRenderer.shouldClose())
					break;

				update(UPDATE_CAP);
				unprocessedTime -= UPDATE_CAP;

				// Since we updated, we also want to render
				doRender = true;
			}

			if (doRender) {
				render();
			} else {
				// sleep for a millisecond, catch interrupts and do literally nothing with them just 'cause
				try {
					Thread.sleep(1);
				} catch (InterruptedException ignored) {

				}
			}
		}

		// Game is over
		cleanup();
	}

	private void updateInput() {
		if (gameStage == GameStage.GAME) {
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
				player.walkTowards(new Vector2D(walkX, walkY));

			// calculate cursor position relative to the center of the screen and camera position
			Vector2D cursorPos = gameInput.getCursorPos().subtract(gameRenderer.getWindowSize().divide(2)).add(cameraPos);
			player.rotateToPoint(cursorPos);

			// inventory
			int inventoryShift = 0;
			if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_UP, UPDATE_CAP)) {
				inventoryShift -= 1;
			}
			if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_DOWN, UPDATE_CAP * 2)) {
				inventoryShift += 1;
			}
			if (inventoryShift != 0)
				player.inventory.shiftActiveIndex(inventoryShift);

			if (gameInput.getMousePressed(GLFW_MOUSE_BUTTON_1)) {
				if (player.inventory.getActiveItem() != null && player.inventory.getActiveItem().isUsable()) {
					((IGameUsableItem)player.inventory.getActiveItem()).use(player, cursorPos, null);
				}
			}
		}
	}

	private void update(float elapsedTime) {
		ArrayList<GameObject> toRemove = new ArrayList<>();

		for (GameObject gameObject : gameObjects) {
			gameObject.update(elapsedTime, velocityDiminishFactor);

			if (gameObject.toDelete)
				toRemove.add(gameObject);
		}

		for (GameObject gameObject : toRemove) {
			gameObjects.remove(gameObject);
		}
	}

	private void render() {
		gameRenderer.beforeLoop(cameraPos);

		for (GameObject gameObject : gameObjects) {
			gameObject.render(gameRenderer);
		}

		if (gameStage == GameStage.GAME) {
			GameInventory.renderInventory(player.inventory, gameRenderer, gameRenderer.getWindowSize().divide(2).inverse(), new Vector2D(1, 1));
		}

		gameRenderer.afterLoop();
	}

	public boolean registerGameObject(GameObject newObject) {
		if (gameObjects.contains(newObject))
			return false;

		gameObjects.add(newObject);
		return true;
	}

	// dispose of all created instances and stuff
	private void cleanup() {
		gameRenderer.cleanup();

		gameConfig.saveConfig(true);
	}
}
