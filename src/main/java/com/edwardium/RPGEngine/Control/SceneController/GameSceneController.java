package com.edwardium.RPGEngine.Control.SceneController;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.IGameActivableItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.IGameUsableItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.IO.JsonBuilder;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;

import static com.edwardium.RPGEngine.Control.Engine.NANO_TIME_MULT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public abstract class GameSceneController extends SceneController {

	public enum ItemFilter { PICKUPABLE, ACTIVABLE, USABLE }

	protected static final float UPDATE_STEP_TIME = 1 / 600f;
	protected static boolean d_drawHitboxes = false;
	protected static Color highlightColor = new Color(255, 255, 0);

	public Vector2D cameraPos;
	public Vector2D cursorPos;

	protected ArrayList<GameObject> gameObjects;
	protected GameCharacter player;

	protected GameSceneController(Input gameInput) {
		super(gameInput);

		cameraPos = new Vector2D();
		cursorPos = new Vector2D();

		gameObjects = new ArrayList<>();

		restore();
	}

	@Override
	public void update(double unprocessedTime) {
		if (updateInput(unprocessedTime)) {
			float remainingTime = (float)(unprocessedTime * NANO_TIME_MULT);
			int numUpdates = (int)Math.ceil(remainingTime / UPDATE_STEP_TIME);
			for (int i = 0; i < numUpdates; i++) {
				updateGame(UPDATE_STEP_TIME, i, numUpdates - 1);
			}
		}
	}

	@Override
	public void freeze() {
		gameInput.unwatchKey(GLFW_KEY_ESCAPE);
	}

	@Override
	public void restore() {
		gameInput.watchKey(GLFW_KEY_ESCAPE);
	}

	protected boolean updateInput(double unprocessedTime) {
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_ESCAPE, unprocessedTime)) {
			if (!Engine.gameEngine.restoreLastSceneController()) {
				Engine.gameEngine.changeSceneController(Engine.SceneControllerType.MENU);
			}
			return false;
		}

		return true;
	}
	protected abstract void updateGame(float elapsedTime, int currentUpdateIndex, int maxUpdateIndex);

	protected void render(Renderer renderer, GameObject highlightObject) {
		renderer.pushTransformMatrix();
		renderer.applyTransformMatrix(null, null, cameraPos);

		// objects
		for (GameObject gameObject : gameObjects) {
			gameObject.render(renderer);

			if (highlightObject == gameObject) {
				gameObject.renderHitbox(renderer, highlightColor);
			} else if (d_drawHitboxes) {
				gameObject.renderHitbox(renderer);
			}
		}
		renderer.popTransformMatrix();
	}

	@Override
	public void cleanup() {
		freeze();
	}

	/**
	 * @param loadPath Path to load from.
	 * @return Whether loading was successful.
	 */
	public abstract boolean loadState(String loadPath);

	protected JsonObject loadStatePartial(String loadPath) {
		JsonObject root;
		try (JsonReader reader = Json.createReader(new FileReader(loadPath))) {
			root = reader.readObject();
		} catch (IOException e) {
			return null;
		}

		if (root == null)
			return null;

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

		return root;
	}

	/**
	 * @param savePath Path to save to.
	 * @return Whether saving was successful.
	 */
	public abstract boolean saveState(String savePath);

	protected boolean saveState(String savePath, JsonBuilder partialObject) {
		if (partialObject == null)
			partialObject = new JsonBuilder();

		// player object
		partialObject.add("player", player.toJSON());

		// object array
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (GameObject object : gameObjects) {
			if (object == player)
				continue;
			arrayBuilder.add(object.toJSON());
		}
		partialObject.add("objects", arrayBuilder);

		JsonObject root = partialObject.build();

		try (JsonWriter writer = Json.createWriter(new FileWriter(savePath))) {
			writer.writeObject(root);

			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * @param object Object to register.
	 * @return Whether the object was registered. Can be false if the object is already registered or if it is null.
	 */
	public boolean registerGameObject(GameObject object) {
		if (object == null || gameObjects.contains(object))
			return false;

		gameObjects.add(object);

		return true;
	}

	/**
	 * @param object Object to unregister.
	 * @return Whether the object was unregistered. Can be false if the object was not registered or if it is null.
	 */
	public boolean unregisterGameObject(GameObject object) {
		return gameObjects.remove(object);
	}

	/**
	 * @param position Position.
	 * @param maxDistance Max distance to allow the object to be from position. Not used if null.
	 * @return Object closest to position, optionally no further than maxDistance. Can be null.
	 */
	public GameObject getClosestObject(Vector2D position, Float maxDistance) {
		GameObject closest = null;
		for (GameObject object : gameObjects) {
			if (closest == null || object.position.distance(position) < closest.position.distance(position)) {
				if (maxDistance == null || object.position.distance(position) <= maxDistance) {
					closest = object;
				}
			}
		}

		return closest;
	}

	/**
	 * @param position Position.
	 * @param filters Item filters.
	 * @param maxDistance Max distance to allow the object to be from position. Not used if null.
	 * @return Item closest to position that passes item filters, optionally no further than maxDistance. Can be null.
	 *
	 * Equivalent to calling getClosestItem(position, filters, maxDistance, position)
	 * @see GameSceneController#getClosestItem(Vector2D, EnumSet, Float, Vector2D)
	 */
	public GameItem getClosestItem(Vector2D position, EnumSet<PlaySceneController.ItemFilter> filters, Float maxDistance) {
		return getClosestItem(position, filters, maxDistance, position);
	}

	/**
	 * @param position Position.
	 * @param filters Item filters.
	 * @param maxDistance Max distance to allow the object to be from maxDistanceSource. Not used if null.
	 * @param maxDistanceSource Point from which to calculate distance for maxDistance.
	 * @return Item closest to position that passed item filters. This item also also has to be in maxDistance radius around maxDistanceSource. Can be null.
	 *
	 * This method is usefull if you need to select the closest item to a point, but it also needs to be in close proximity to mouse cursor.
	 * Setting maxDistanceSource to cursor position and maxDistance to some radius allows you to select items closest to some point with mouse.
	 */
	public GameItem getClosestItem(Vector2D position, EnumSet<PlaySceneController.ItemFilter> filters, Float maxDistance, Vector2D maxDistanceSource) {
		GameItem closest = null;
		for (GameObject object : gameObjects) {
			if (object instanceof GameItem) {
				if (closest == null || object.position.distance(position) < closest.position.distance(position)) {
					boolean passedFilter = true;
					if (maxDistance != null)
						passedFilter = object.position.distance(maxDistanceSource) <= maxDistance;

					if (filters.contains(PlaySceneController.ItemFilter.USABLE))
						passedFilter &= object instanceof IGameUsableItem;

					if (filters.contains(PlaySceneController.ItemFilter.PICKUPABLE))
						passedFilter &= ((GameItem) object).canPickup;

					if (filters.contains(PlaySceneController.ItemFilter.ACTIVABLE))
						passedFilter &= object instanceof IGameActivableItem;

					if (passedFilter)
						closest = (GameItem) object;
				}
			}
		}

		return closest;
	}

	/**
	 * @param position Position.
	 * @return Character closest to position. Can be null.
	 */
	public GameCharacter getClosestCharacter(Vector2D position) {
		GameCharacter closest = null;
		for (GameObject object : gameObjects) {
			if (object instanceof GameCharacter) {
				if (closest == null || object.position.distance(position) < closest.position.distance(position)) {
					closest = (GameCharacter) object;
				}
			}
		}

		return closest;
	}

	/**
	 * @param me Character to which to find the closest character.
	 * @param filters Character relationship filter.
	 * @return Character that is closest to me and passes filters. Can be null.
	 */
	public GameCharacter getClosestCharacter(GameCharacter me, GameCharacter.CharacterRelationship filters) {
		GameCharacter closest = null;
		for (GameObject object : gameObjects) {
			if (object instanceof GameCharacter) {
				if (object != me) {
					if (closest == null || object.position.distance(me.position) < closest.position.distance(me.position)) {
						if (filters == null || GameCharacter.CharacterFaction.getRelationship(((GameCharacter)object).factionFlag, me.factionFlag) == filters) {
							closest = (GameCharacter) object;
						}
					}
				}
			}
		}

		return closest;
	}
}
