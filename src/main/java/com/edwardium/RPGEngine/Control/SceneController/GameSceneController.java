package com.edwardium.RPGEngine.Control.SceneController;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.GameEntity.GameAI.SimpleEnemyAI;
import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.GameEntity.GameInventory;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GunBouncyBall;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GunDestroyer;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GunPistol;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile.GameProjectile;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Rectangle;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Vector2D;

import java.util.ArrayList;

import static com.edwardium.RPGEngine.Control.Engine.NANO_TIME_MULT;
import static org.lwjgl.glfw.GLFW.*;

public class GameSceneController extends SceneController {

	public enum SpawnType { CHARACTER, PROJECTILE, ITEM };

	private static class SpawnLimit {
		public final int maximum;
		public int current;

		public SpawnLimit(int maximum) {
			this.maximum = maximum;
			this.current = 0;
		}

		public boolean canSpawnMore() {
			return maximum > 0 && current < maximum;
		}
	}

	private Vector2D cameraPos;

	private ArrayList<GameObject> gameObjects;
	private GameCharacter player;

	// air density
	private float environmentDensity = 1.2f;
	private float timeFactor = 1f;

	// spawn limits
	private static final SpawnLimit characterLimit = new SpawnLimit(128);
	// maximum projectiles spawned at once
	private static final SpawnLimit projectileLimit = new SpawnLimit(512);
	// maximum items (that are not already in a different category) spawned at once
	private static final SpawnLimit itemLimit = new SpawnLimit(1024);

	private static boolean d_drawHitboxes = false;

	public GameSceneController(Input gameInput) {
		super(gameInput);

		cameraPos = new Vector2D();
		gameObjects = new ArrayList<>();

		gameInput.watchKey(GLFW_KEY_UP);
		gameInput.watchKey(GLFW_KEY_DOWN);
		gameInput.watchKey(GLFW_KEY_KP_ADD);
		gameInput.watchKey(GLFW_KEY_KP_SUBTRACT);

		gameInput.watchKey(GLFW_KEY_H);

		gameInput.watchKey(GLFW_KEY_ESCAPE);

		init();
	}

	private void init() {
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
	}

	@Override
	public void update(double unprocessedTime) {
		if (updateInput(unprocessedTime))
			updateGame((float)(unprocessedTime * NANO_TIME_MULT * timeFactor));
	}
	private boolean updateInput(double unprocessedTime) {
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_ESCAPE, unprocessedTime)) {
			Engine.gameEngine.restoreLastSceneController();
			return false;
		}

		// DEBUG
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_G, unprocessedTime)) {
			Engine.gameEngine.toggleVSync();
		}

		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_H, unprocessedTime)) {
			d_drawHitboxes = !d_drawHitboxes;
		}
		// end DEBUG

		float timeChange = 0;
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_KP_ADD, unprocessedTime) || gameInput.getScrollUpJustNow(unprocessedTime)) {
			timeChange += 0.1f;
		}
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_KP_SUBTRACT, unprocessedTime) || gameInput.getScrollDownJustNow(unprocessedTime)) {
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
		Vector2D cursorPos = gameInput.getGameCursorPos().subtract(cameraPos);
		player.rotateToPoint(cursorPos);

		// inventory
		int inventoryShift = 0;
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_UP, unprocessedTime)) {
			inventoryShift -= 1;
		}
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_DOWN, unprocessedTime)) {
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

		return true;
	}

	private void updateGame(float elapsedTime) {
		ArrayList<GameObject> toRemove = new ArrayList<>();

		for (int i = 0; i < gameObjects.size(); i++) {
			GameObject currentObject = gameObjects.get(i);

			currentObject.update(elapsedTime, environmentDensity);

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

	@Override
	public void render(Renderer renderer) {
		cameraPos = Vector2D.inverse(player.position);

		renderer.pushTransformMatrix();
		renderer.applyTransformMatrix(null, null, cameraPos);
		for (GameObject gameObject : gameObjects) {
			gameObject.render(renderer, d_drawHitboxes);
		}
		renderer.popTransformMatrix();

		GameInventory.renderInventory(player.inventory, renderer, renderer.getWindowSize().divide(2).inverse(), new Vector2D(1, 1));

		renderer.drawString(renderer.basicFont, "Time factor: " + String.format("%.2f", this.timeFactor), renderer.getWindowSize().divide(2).scale(-1, 1).add(new Vector2D(5, -35)), null, 0, new Color());
	}

	@Override
	public void cleanup() {
		gameInput.unwatchKey(GLFW_KEY_UP);
		gameInput.unwatchKey(GLFW_KEY_DOWN);
		gameInput.unwatchKey(GLFW_KEY_KP_ADD);
		gameInput.unwatchKey(GLFW_KEY_KP_SUBTRACT);

		gameInput.unwatchKey(GLFW_KEY_H);

		gameInput.unwatchKey(GLFW_KEY_ESCAPE);
	}

	private void setTimeFactor(Float value) {
		if (value == null) {
			this.timeFactor = 1f;
		} else {
			this.timeFactor = Math.max(0, value);
		}
	}
	private void shiftTimeFactor(float value) {
		setTimeFactor(this.timeFactor + value);
	}

	public boolean canSpawnType(SpawnType type) {
		switch (type) {
			case CHARACTER:
				return characterLimit.canSpawnMore();
			case PROJECTILE:
				return projectileLimit.canSpawnMore();
			case ITEM:
				return itemLimit.canSpawnMore();
			default:
				return false;
		}
	}
	public boolean registerGameObject(GameObject object) {
		if (gameObjects.contains(object))
			return false;

		gameObjects.add(object);

		// bump up spawn limit
		if (object instanceof GameCharacter)
			characterLimit.current++;
		else if (object instanceof GameProjectile)
			projectileLimit.current++;
		else if (object instanceof GameItem)
			itemLimit.current++;

		return true;
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
}
