package com.edwardium.RPGEngine.Control.SceneController;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.GameEntity.GameAI.PlayerAI;
import com.edwardium.RPGEngine.GameEntity.GameAI.SimpleEnemyAI;
import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.GameEntity.GameInventory;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GunBouncyBall;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GunDestroyer;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GunPistol;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItemGun.GunSMG;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile.GameProjectile;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.IGameActivableItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.IGameUsableItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.IO.JsonBuilder;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Utility.Rectangle;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;

import static com.edwardium.RPGEngine.Control.Engine.NANO_TIME_MULT;
import static org.lwjgl.glfw.GLFW.*;

public class GameSceneController extends SceneController {

	public enum ItemFilter { PICKUPABLE, ACTIVABLE, USABLE }
	public enum SpawnType { CHARACTER, PROJECTILE, ITEM }

	private static class SpawnLimit {
		public final int maximum;
		public int current;

		public SpawnLimit(int maximum) {
			this.maximum = maximum;
			this.current = 0;
		}

		public SpawnLimit(JsonObject sourceObj) {
			int maxTemp = 0;
			try {
				maxTemp = sourceObj.getJsonNumber("maximum").intValue();
			} catch (NullPointerException | ClassCastException ignored) {}
			maximum = maxTemp;

			current = 0;
			try {
				current = sourceObj.getJsonNumber("current").intValue();
			} catch (NullPointerException | ClassCastException ignored) {}
		}

		public boolean canSpawnMore() {
			return maximum > 0 && current < maximum;
		}

		public JsonObject toJSON() {
			return new JsonBuilder().add("maximum", maximum).add_optional("current", current, 0).build();
		}
	}

	private static final float UPDATE_STEP_TIME = 1 / 600f;

	public Vector2D cameraPos;
	public Vector2D cursorPos;

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
	private static Color highlightColor = new Color(255, 255, 0);

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
	}

	public void reloadScene() {
		gameObjects.clear();

		// init player
		player = new GameCharacter(new Vector2D(550, 20), "player", 10);
		player.factionFlag = GameCharacter.CharacterFaction.addFaction(player.factionFlag, GameCharacter.CharacterFaction.PLAYER);

		if (!loadState("test.json")) {
			GameItem pistol = new GunPistol(new Vector2D(player.position));
			registerGameObject(pistol);
			GameItem destroyerGun = new GunDestroyer(new Vector2D(player.position));
			registerGameObject(destroyerGun);
			GameItem bouncyBallGun = new GunBouncyBall(new Vector2D(player.position));
			registerGameObject(bouncyBallGun);
			GameItem smg = new GunSMG(new Vector2D(player.position));
			registerGameObject(smg);

			player.inventory.insertItem(pistol);
			player.inventory.insertItem(destroyerGun);
			player.inventory.insertItem(bouncyBallGun);
			//player.inventory.insertItem(smg);

			GameCharacter secondCharacter = new GameCharacter(new Vector2D(-100, -100), "Enemy Trianglehead", 3);
			secondCharacter.ai = new SimpleEnemyAI(secondCharacter);
			secondCharacter.factionFlag = GameCharacter.CharacterFaction.addFaction(secondCharacter.factionFlag, GameCharacter.CharacterFaction.TRIANGLEHEADS);
			secondCharacter.maxRotationSpeed = .1f;
			registerGameObject(secondCharacter);

			GameItem secondPistol = new GunPistol(new Vector2D(50, 50).add(secondCharacter.position));
			registerGameObject(secondPistol);

			//secondCharacter.inventory.insertItem(bouncyBallGun);
			secondCharacter.inventory.insertItem(smg);

			registerGameObject(new GameWall(new Vector2D(500, 0), new Rectangle(new Vector2D(-15, -50), new Vector2D(15, 50))));

			registerGameObject(new GameWall(new Vector2D(700, 0), new Rectangle(new Vector2D(-2, -30), new Vector2D(2, 30))));

			registerGameObject(new GameWall(new Vector2D(-50, 250), new Rectangle(new Vector2D(-5, -30), new Vector2D(5, 30))).rotateBy(-3.14f / 4));
			registerGameObject(new GameWall(new Vector2D(50, 250), new Rectangle(new Vector2D(-5, -30), new Vector2D(5, 30))).rotateBy(3.14f / 4));
		}

		// player is outside of spawn limits?
		gameObjects.add(player);
	}

	@Override
	public void update(double unprocessedTime) {
		if (updateInput(unprocessedTime)) {
			updateGame(UPDATE_STEP_TIME * timeFactor, true);

			float remainingTime = (float)(unprocessedTime * NANO_TIME_MULT) - UPDATE_STEP_TIME;
			while (remainingTime > 0) {
				updateGame(UPDATE_STEP_TIME * timeFactor, false);
				remainingTime -= UPDATE_STEP_TIME;
			}
		}
	}
	private boolean updateInput(double unprocessedTime) {
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_ESCAPE, unprocessedTime)) {
			if (!Engine.gameEngine.restoreLastSceneController()) {
				Engine.gameEngine.changeSceneController(Engine.SceneControllerType.MENU);
			}
			return false;
		}

		// DEBUG
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_H, unprocessedTime)) {
			d_drawHitboxes = !d_drawHitboxes;
		}
		// end DEBUG

		// cursor pos
		cursorPos = gameInput.getGameCursorPos().subtract(cameraPos);

		// time
		float timeChange = 0;
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_KP_ADD, unprocessedTime) || gameInput.getScrollUpJustNow(unprocessedTime)) {
			timeChange += 0.1f;
		}
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_KP_SUBTRACT, unprocessedTime) || gameInput.getScrollDownJustNow(unprocessedTime)) {
			timeChange -= 0.1f;
		}
		this.shiftTimeFactor(timeChange);

		for (int i = 0; i < gameObjects.size(); i++) {
			GameObject currentObject = gameObjects.get(i);
			if (currentObject instanceof GameCharacter && ((GameCharacter) currentObject).ai instanceof PlayerAI) {
				((PlayerAI) ((GameCharacter) currentObject).ai).updateInput(gameInput, unprocessedTime);
			}
		}

		return true;
	}

	private void updateGame(float elapsedTime, boolean updateWalk) {
		ArrayList<GameObject> toRemove = new ArrayList<>();

		for (int i = 0; i < gameObjects.size(); i++) {
			GameObject currentObject = gameObjects.get(i);

			if (updateWalk && currentObject instanceof GameCharacter) {
				((GameCharacter) currentObject).updateWalk();
			}
			currentObject.update(elapsedTime, environmentDensity);

			// collisions
			for (int j = i + 1; j < gameObjects.size(); j++) {
				GameObject currentObjectCollision = gameObjects.get(j);

				GameHitbox.CollisionInfo collisionInfo = currentObject.checkCollision(currentObjectCollision);
				if (collisionInfo != null && collisionInfo.doesCollide) {
					currentObject.collideWith(currentObjectCollision, collisionInfo.ASurfaceNormal, collisionInfo.BSurfaceNormal);
					currentObjectCollision.collideWith(currentObject, collisionInfo.BSurfaceNormal, collisionInfo.ASurfaceNormal);
				}
			}

			if (currentObject.toDelete)
				toRemove.add(currentObject);
		}

		for (GameObject gameObject : toRemove) {
			unregisterGameObject(gameObject);
		}
	}

	@Override
	public void render(Renderer renderer) {
		// find the object to highlight (activable object in range of player closest to cursor
		// or pickupable object if player is empty handed)
		GameItem itemToHighlight = null;
		if (player.inventory.getActiveItem() == null) {
			itemToHighlight = getClosestItem(cursorPos, EnumSet.of(ItemFilter.PICKUPABLE), player.pickupRange, player.position);
		}
		if (itemToHighlight == null) {
			itemToHighlight = getClosestItem(cursorPos, EnumSet.of(ItemFilter.ACTIVABLE), player.pickupRange, player.position);
		}

		cameraPos = Vector2D.inverse(player.position);

		renderer.pushTransformMatrix();
		renderer.applyTransformMatrix(null, null, cameraPos);
		for (GameObject gameObject : gameObjects) {
			gameObject.render(renderer);

			if (itemToHighlight == gameObject) {
				gameObject.renderHitbox(renderer, highlightColor);
			} else if (d_drawHitboxes) {
				gameObject.renderHitbox(renderer);
			}
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

	public boolean loadState(String loadPath) {
		JsonObject root = null;
		try (JsonReader reader = Json.createReader(new FileReader(loadPath))) {
			root = reader.readObject();
		} catch (IOException e) {
			return false;
		}

		if (root == null)
			return false;

		try {
			timeFactor = (float)root.getJsonNumber("timeFactor").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			environmentDensity = (float)root.getJsonNumber("environmentDensity").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		cameraPos = Vector2D.fromJSON(root.getJsonObject("camera"));

		JsonObject playerObj = root.getJsonObject("player");
		if (playerObj != null)
			player = new GameCharacter(playerObj);

		try {
			JsonArray objectArray = root.getJsonArray("objects");
			for (JsonValue value : objectArray) {
				try {
					JsonObject object = value.asJsonObject();
					registerGameObject(GameObject.fromJSON(object));
				} catch (NullPointerException | ClassCastException ignored) { }
			}
		} catch (NullPointerException | ClassCastException ignored) { }

		return true;
	}

	public boolean saveState(String savePath) {
		JsonBuilder builder = new JsonBuilder();
		// time and environment density
		builder.add_optional("timeFactor", timeFactor, 1f);
		builder.add_optional("environmentDensity", environmentDensity, 1.2f);

		// camera pos
		builder.add_optional("camera", cameraPos, cameraPos.getMagnitude() != 0);

		// player object
		builder.add("player", player.toJSON());

		// spawn limits
		// builder.add("characterLimit", characterLimit.toJSON());
		// builder.add("projectileLimit", projectileLimit.toJSON());
		// builder.add("itemLimit", itemLimit.toJSON());

		// object array
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (GameObject object : gameObjects) {
			if (object == player)
				continue;
			arrayBuilder.add(object.toJSON());
		}
		builder.add("objects", arrayBuilder);

		JsonObject root = builder.build();

		try (JsonWriter writer = Json.createWriter(new FileWriter(savePath))) {
			writer.writeObject(root);

			return true;
		} catch (IOException e) {
			return false;
		}
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
	private void updateSpawnLimits(GameObject object, int delta) {
		if (object instanceof GameCharacter)
			characterLimit.current += delta;
		else if (object instanceof GameProjectile)
			projectileLimit.current += delta;
		else if (object instanceof GameItem)
			itemLimit.current += delta;
	}

	public boolean registerGameObject(GameObject object) {
		if (object == null || gameObjects.contains(object))
			return false;

		gameObjects.add(object);

		// bump up spawn limit
		updateSpawnLimits(object, 1);

		return true;
	}
	public boolean unregisterGameObject(GameObject object) {
		boolean returnVal = gameObjects.remove(object);

		if (returnVal) {
			// bump down spawn limit
			updateSpawnLimits(object, -1);
		}

		return returnVal;
	}

	public GameItem getClosestItem(Vector2D position, EnumSet<ItemFilter> filters, Float maxDistance) {
		return getClosestItem(position, filters, maxDistance, position);
	}
	public GameItem getClosestItem(Vector2D position, EnumSet<ItemFilter> filters, Float maxDistance, Vector2D maxDistanceSource) {
		GameItem closest = null;
		for (GameObject object : gameObjects) {
			if (object instanceof GameItem) {
				if (closest == null || object.position.distance(position) < closest.position.distance(position)) {
					boolean passedFilter = true;
					if (maxDistance != null)
						passedFilter = object.position.distance(maxDistanceSource) <= maxDistance;

					if (filters.contains(ItemFilter.USABLE))
						passedFilter &= object instanceof IGameUsableItem;

					if (filters.contains(ItemFilter.PICKUPABLE))
						passedFilter &= ((GameItem) object).canPickup;

					if (filters.contains(ItemFilter.ACTIVABLE))
						passedFilter &= object instanceof IGameActivableItem;

					if (passedFilter)
						closest = (GameItem) object;
				}
			}
		}

		return closest;
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
