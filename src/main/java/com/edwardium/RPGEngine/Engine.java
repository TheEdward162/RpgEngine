package com.edwardium.RPGEngine;

import com.edwardium.RPGEngine.GameEntity.GameAI.SimpleEnemyAI;
import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.GameEntity.GameInventory;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GunBouncyBall;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GunDestroyer;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GunPistol;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.IO.Config;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.OpenGL.OpenGLRenderer;
import com.edwardium.RPGEngine.Renderer.Renderer;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class Engine implements Runnable {
	private enum GameStage { MAIN_MENU, PAUSED_MENU, GAME }

	// in seconds
	public static final float UPDATE_CAP = 1.0f / 60.1f;
	// nanoseconds * NANO_TIME_MULT = seconds
	public static final double NANO_TIME_MULT = 1e-9;

	public static final float PIXEL_TO_METER = 1.0f / 50.0f;

	public static boolean d_drawHitboxes = false;

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

	// air density
	private float enviromentDensity = 1.2f;
	private float timeFactor = 1f;

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

		FPSCounter.init(50);

		// load config
		gameConfig = new Config("config.ini");

		// init variables
		cameraPos = new Vector2D();

		gameObjects = new ArrayList<>();

		// init player
		player = new GameCharacter(new Vector2D(550, 20), "player", 10);
		player.factionFlag = GameCharacter.CharacterFaction.addFaction(player.factionFlag, GameCharacter.CharacterFaction.PLAYER);

		GameItem pistol = new GunPistol(new Vector2D(player.position));
		GameItem destroyerGun = new GunDestroyer(new Vector2D(player.position));
		GameItem bouncyBallGun = new GunBouncyBall(new Vector2D(player.position));
		player.inventory.insertItem(pistol);
		player.inventory.insertItem(destroyerGun);
		player.inventory.insertItem(bouncyBallGun);

		registerGameObject(pistol);
		registerGameObject(destroyerGun);
		registerGameObject(bouncyBallGun);

		gameObjects.add(player);

		GameCharacter secondCharacter = new GameCharacter(new Vector2D(-100, -100), "Enemy Trianglehead", 3);
		secondCharacter.ai = new SimpleEnemyAI(secondCharacter);
		secondCharacter.factionFlag = GameCharacter.CharacterFaction.addFaction(secondCharacter.factionFlag, GameCharacter.CharacterFaction.TRIANGLEHEADS);

		GameItem secondPistol = new GunPistol(new Vector2D(secondCharacter.position));
		//secondCharacter.inventory.insertItem(secondPistol);

		registerGameObject(secondPistol);
		gameObjects.add(secondCharacter);

		gameObjects.add(new GameWall(new Vector2D(500, 0), new Rectangle(new Vector2D(-15, -50), new Vector2D(15, 50))));

		gameObjects.add(new GameWall(new Vector2D(700, 0), new Rectangle(new Vector2D(-2, -30), new Vector2D(2, 30))));

		gameObjects.add(new GameWall(new Vector2D(-50, 250), new Rectangle(new Vector2D(-5, -30), new Vector2D(5, 30))).rotateBy(-3.14f / 4));
		gameObjects.add(new GameWall(new Vector2D(50, 250), new Rectangle(new Vector2D(-5, -30), new Vector2D(5, 30))).rotateBy(3.14f / 4));

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
		gameInput.watchKey(GLFW_KEY_KP_ADD);
		gameInput.watchKey(GLFW_KEY_KP_SUBTRACT);

		gameInput.watchKey(GLFW_KEY_H);
		gameInput.watchKey(GLFW_KEY_G);

		gameRenderer.setVSync(true);
		gameRenderer.show();

		// start game loop
		running = true;
		loopUnsynced();

		// Game is over
		cleanup();
	}

	private void loop() {
		// whether to render this tick or skip rendering because nothing has updated
		boolean doRender = false;

		// time that is now
		double nowTime;
		// last time the loop was run
		double lastTime = System.nanoTime();
		// how much time has elapsed since last time
		// divide by UPDATE_CAP to see how many updates we have missed
		double unprocessedTime = 0;

		while (running) {
			if (gameRenderer.shouldClose()) {
				running = false;
				break;
			}

			nowTime = System.nanoTime();

			double timeSinceLastFrame = nowTime - lastTime;
			unprocessedTime += timeSinceLastFrame;

			lastTime = nowTime;

			// only update input once, even if we do more game updates
			if (unprocessedTime >= UPDATE_CAP / NANO_TIME_MULT) {
				FPSCounter.update(unprocessedTime);
				System.err.println(FPSCounter.getFPS());
				updateInput(UPDATE_CAP);
			}

			// update while we are all caught up
			while (unprocessedTime >= UPDATE_CAP / NANO_TIME_MULT) {
				if (gameRenderer.shouldClose())
					break;

				update(UPDATE_CAP * timeFactor);
				unprocessedTime -= UPDATE_CAP / NANO_TIME_MULT;

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
	}

	private void loopUnsynced() {
		// time that is now
		double nowTime;
		// last time the loop was run
		double lastTime = System.nanoTime();
		// how much time has elapsed since last time
		double unprocessedTime;

		while (running) {
			if (gameRenderer.shouldClose()) {
				running = false;
				break;
			}

			nowTime = System.nanoTime();
			unprocessedTime = nowTime - lastTime;
			lastTime = nowTime;

			// only update input once, even if we do more game updates
			FPSCounter.update(unprocessedTime);
			// System.err.println(FPSCounter.getFPS());

			updateInput(unprocessedTime);
			if (gameRenderer.shouldClose())
				break;

			update((float)(unprocessedTime * NANO_TIME_MULT * timeFactor));
			render();
		}
	}

	private void updateInput(double elapsedTime) {
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_G, elapsedTime)) {
			boolean currentVSync = gameRenderer.getVSync();
			gameRenderer.setVSync(!currentVSync);
		}

		if (gameStage == GameStage.GAME) {
			// DEBUG
			if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_H, elapsedTime)) {
				d_drawHitboxes = !d_drawHitboxes;
			}
			// end DEBUG

			float timeChange = 0;
			if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_KP_ADD, elapsedTime) || gameInput.getScrollUpJustNow(elapsedTime)) {
				timeChange += 0.1f;
			}
			if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_KP_SUBTRACT, elapsedTime) || gameInput.getScrollDownJustNow(elapsedTime)) {
				timeChange -= 0.1f;
			}
			this.shiftTimeFactor(timeChange);

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
			if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_UP, elapsedTime)) {
				inventoryShift -= 1;
			}
			if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_DOWN, elapsedTime)) {
				inventoryShift += 1;
			}
			if (inventoryShift != 0)
				player.inventory.shiftActiveIndex(inventoryShift);

			int inventoryIndex = -1;
			for (int i = GLFW_KEY_1; i < GLFW_KEY_9; i++) {
				if (gameInput.getKeyPressed(i)) {
					inventoryIndex = i - GLFW_KEY_1;
				}
			}
			if (inventoryIndex != -1)
				player.inventory.setActiveIndex(inventoryIndex);

			if (gameInput.getMousePressed(GLFW_MOUSE_BUTTON_1)) {
				player.useActiveItem(cursorPos, null);
			}
		}
	}

	private void update(float elapsedTime) {
		ArrayList<GameObject> toRemove = new ArrayList<>();

		for (int i = 0; i < gameObjects.size(); i++) {
			GameObject currentObject = gameObjects.get(i);

			currentObject.update(elapsedTime, enviromentDensity);

			// collisions
			for (int j = i + 1; j < gameObjects.size(); j++) {
				GameObject currentObjectCollision = gameObjects.get(j);

				GameHitbox.CollisionInfo collisionInfo = currentObject.checkCollision(currentObjectCollision);
				if (collisionInfo != null && collisionInfo.doesCollide) {
					currentObject.collideWith(currentObjectCollision, collisionInfo.BSurfaceNormal);
					currentObjectCollision.collideWith(currentObject, collisionInfo.ASurfaceNormal);
				}
			}

			if (currentObject.toDelete)
				toRemove.add(currentObject);
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

		gameRenderer.drawString(gameRenderer.basicFont, "FPS: " + String.format("%.2f", FPSCounter.getFPS()), gameRenderer.getWindowSize().divide(2).scale(-1, 1).add(new Vector2D(5, -20)), null, new Color());
		gameRenderer.drawString(gameRenderer.basicFont, "Time factor: " + String.format("%.2f", this.timeFactor), gameRenderer.getWindowSize().divide(2).scale(-1, 1).add(new Vector2D(5, -5)), null, new Color());

		gameRenderer.afterLoop();
	}

	public boolean registerGameObject(GameObject newObject) {
		if (gameObjects.contains(newObject))
			return false;

		gameObjects.add(newObject);
		return true;
	}
	public void setTimeFactor(Float value) {
		if (value == null) {
			this.timeFactor = 1f;
		} else {
			this.timeFactor = Math.max(0, value);
		}
	}
	public void shiftTimeFactor(float value) {
		setTimeFactor(this.timeFactor + value);
	}

	public GameCharacter getClosestCharacter(GameCharacter me, GameCharacter.CharacterRelationship filter) {
		GameCharacter closest = null;
		for (GameObject object : gameObjects) {
			if (object instanceof GameCharacter) {
				if (object != me) {
					if (closest == null || object.position.distance(me.position) < closest.position.distance(me.position)) {
						if (filter == null || GameCharacter.CharacterFaction.getRelationship(((GameCharacter)object).factionFlag, me.factionFlag) == filter) {
							closest = (GameCharacter) object;
						}
					}
				}
			}
		}

		return closest;
	}

	// dispose of all created instances and stuff
	private void cleanup() {
		gameRenderer.cleanup();

		gameConfig.saveConfig(true);
	}
}
