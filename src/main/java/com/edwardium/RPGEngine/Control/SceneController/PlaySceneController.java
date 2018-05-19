package com.edwardium.RPGEngine.Control.SceneController;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.Control.UI;
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
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.IO.JsonBuilder;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Light;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Utility.Rectangle;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.EnumSet;

import static org.lwjgl.glfw.GLFW.*;

public class PlaySceneController extends GameSceneController {
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

	private Light[] currentLights;
	private int currentLightsSize = 0;
	private static final Light ambientLight = new Light(new Vector2D(), Color.GREY, -1f, 0f);

	// air density
	private float environmentDensity = 1.2f;
	private float timeFactor = 1f;

	// spawn limits
	private static final SpawnLimit characterLimit = new SpawnLimit(128);
	// maximum projectiles spawned at once
	private static final SpawnLimit projectileLimit = new SpawnLimit(512);
	// maximum items (that are not already in a different category) spawned at once
	private static final SpawnLimit itemLimit = new SpawnLimit(1024);

	public PlaySceneController(Input gameInput) {
		super(gameInput);

		currentLights = new Light[Renderer.MAX_LIGHTS - 1];
	}

	@Override
	public void freeze() {
		super.freeze();

		gameInput.unwatchKey(GLFW_KEY_UP);
		gameInput.unwatchKey(GLFW_KEY_DOWN);
		gameInput.unwatchKey(GLFW_KEY_KP_ADD);
		gameInput.unwatchKey(GLFW_KEY_KP_SUBTRACT);

		gameInput.unwatchKey(GLFW_KEY_H);
	}

	@Override
	public void restore() {
		super.restore();

		gameInput.watchKey(GLFW_KEY_UP);
		gameInput.watchKey(GLFW_KEY_DOWN);
		gameInput.watchKey(GLFW_KEY_KP_ADD);
		gameInput.watchKey(GLFW_KEY_KP_SUBTRACT);

		gameInput.watchKey(GLFW_KEY_H);
	}

	public void reloadScene() {
		gameObjects.clear();

		// init player
		player = new GameCharacter(new Vector2D(550, 0), "player", 10);
		player.factionFlag = GameCharacter.CharacterFaction.addFaction(player.factionFlag, GameCharacter.CharacterFaction.PLAYER);

		if (!loadState("Saves/editor-exitsave.json")) {
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
			GameItem secondSmg = new GunSMG(new Vector2D(143, 258));
			registerGameObject(secondSmg);

			//secondCharacter.inventory.insertItem(bouncyBallGun);
			secondCharacter.inventory.insertItem(smg);

			registerGameObject(new GameWall(new Vector2D(500, 0), new Rectangle(new Vector2D(-15, -50), new Vector2D(15, 50))));

			registerGameObject(new GameWall(new Vector2D(700, 0), new Rectangle(new Vector2D(-2, -30), new Vector2D(2, 30))));

			registerGameObject(new GameWall(new Vector2D(-50, 250), new Rectangle(new Vector2D(-5, -30), new Vector2D(5, 30))).rotateBy(-3.14f / 4));
			registerGameObject(new GameWall(new Vector2D(50, 250), new Rectangle(new Vector2D(-5, -30), new Vector2D(5, 30))).rotateBy(3.14f / 4));

			registerGameObject(new GameWall(new Vector2D(-150f, 0f), new Vector2D[] {
					new Vector2D(-15, -50),
					new Vector2D(15, -50),
					new Vector2D(25, 0f),
					new Vector2D(15, 50),
					new Vector2D(-15, 50)
			}));

			registerGameObject(new GameWall(new Vector2D(0, 500), new Rectangle(new Vector2D(-400, -60), new Vector2D(400, 60))));
		}

		// player is outside of spawn limits?
		gameObjects.add(player);
	}

	@Override
	protected boolean updateInput(double unprocessedTime) {
		if (!super.updateInput(unprocessedTime))
			return false;

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
			if (Math.round(timeFactor * 100f) / 100f < 0.1f)
				timeChange += 0.01f;
			else
				timeChange += 0.1f;
		}
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_KP_SUBTRACT, unprocessedTime) || gameInput.getScrollDownJustNow(unprocessedTime)) {
			if (timeFactor <= 0.1f)
				timeChange -= 0.01f;
			else
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

	@Override
	protected void updateGame(float elapsedTime, int currentUpdateIndex, int maxUpdateIndex) {
		elapsedTime *= timeFactor;
		boolean updateWalk = currentUpdateIndex == 0;
		boolean updateLights = currentUpdateIndex == maxUpdateIndex;

		ArrayList<GameObject> toRemove = new ArrayList<>();

		for (int i = 0; i < gameObjects.size(); i++) {
			GameObject currentObject = gameObjects.get(i);

			if (updateWalk && currentObject instanceof GameCharacter) {
				((GameCharacter) currentObject).updateWalk();
			}
			currentObject.updatePhysics(elapsedTime, environmentDensity);

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
			else if (updateLights)
				currentObject.updateLights(this);
		}

		for (GameObject gameObject : toRemove) {
			unregisterGameObject(gameObject);
		}
	}

	private void applyLights(Renderer renderer) {
		// LIGHTS ARE THE BEST THING EVEEER
		renderer.setLight(0, ambientLight);

		int numLights = Math.min(Renderer.MAX_LIGHTS - 1, currentLightsSize);
		int currentLightIndex = 1;

		Vector2D viewportSize = renderer.getWindowSize();
		Vector2D halfViewport = Vector2D.scale(viewportSize, 0.5f);
		Rectangle viewportRectangle = new Rectangle(Vector2D.scale(viewportSize, -0.5f).subtract(cameraPos), Vector2D.scale(viewportSize, 0.5f).subtract(cameraPos));

		for (int i = 0; i < numLights; i++) {
			Light light = currentLights[i];
			if (Rectangle.pointCollision(viewportRectangle, light.position, light.cutoff != 0 ? light.cutoff : light.power * Light.DEFAULT_CUTOFF_MULT)) {
				// shader coords are bottom-left based
				float xScreen = light.position.getX() + cameraPos.getX() + halfViewport.getX();
				float yScreen = -(light.position.getY() + cameraPos.getY() - halfViewport.getY());
				Vector2D newLightPosition = new Vector2D(xScreen, yScreen);

				renderer.setLight(currentLightIndex, new Light(newLightPosition, light.color, light.power, light.cutoff));
				currentLightIndex++;
			}
		}
		renderer.setLightCount(currentLightIndex);
		currentLightsSize = 0;

//		registerLight(new Light(new Vector2D((float)Math.sin(tmp * 2f) * 150f, (float)Math.cos(tmp * 2f) * 150f), new Color(0f, 0f, 1f), 20f, 0f));
//		registerLight(new Light(player.position, new Color(1f, 0f, 0f), 15f));
//		registerLight(new Light(cursorPos, new Color(0f, 1f, 0f), 15f));
//		registerLight(new Light(cursorPos, new Color(0f, 1f, 0f), 15f));
	}

	@Override
	public void render(Renderer renderer) {
		cameraPos = Vector2D.inverse(player.position);
		applyLights(renderer);

		super.renderBegin(renderer);
		super.render(renderer);

		// find the object to highlight (activable object in range of player closest to cursor
		// or pickupable object if player is empty handed)
		GameItem itemToHighlight = null;
		if (player.inventory.getActiveItem() == null) {
			itemToHighlight = getClosestItem(cursorPos, EnumSet.of(ItemFilter.PICKUPABLE), player.pickupRange, player.position);
		}
		if (itemToHighlight == null) {
			itemToHighlight = getClosestItem(cursorPos, EnumSet.of(ItemFilter.ACTIVABLE), player.pickupRange, player.position);
		}
		if (itemToHighlight != null) {
			itemToHighlight.renderHitbox(renderer, R_HIGHLIGHT_COLOR);
		}

		super.renderEnd(renderer);

		GameInventory.renderInventory(player.inventory, renderer, renderer.getWindowSize().divide(2).inverse(), new Vector2D(1, 1));

		Engine.gameEngine.drawDefaultCornerStrings();
		UI.drawCornerString(renderer, UI.Corner.BOTTOMLEFT, String.format("Time factor: %.2f", this.timeFactor));
	}

	@Override
	public void cleanup() {
		saveState("Saves/exitsave.json");

		gameInput.unwatchKey(GLFW_KEY_UP);
		gameInput.unwatchKey(GLFW_KEY_DOWN);
		gameInput.unwatchKey(GLFW_KEY_KP_ADD);
		gameInput.unwatchKey(GLFW_KEY_KP_SUBTRACT);

		gameInput.unwatchKey(GLFW_KEY_H);

		super.cleanup();
	}

	@Override
	public boolean loadState(String loadPath) {
		JsonObject root = loadStatePartial(loadPath);
		if (root == null)
			return false;

		try {
			timeFactor = (float)root.getJsonNumber("timeFactor").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		try {
			environmentDensity = (float)root.getJsonNumber("environmentDensity").doubleValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		cameraPos = Vector2D.fromJSON(root.getJsonObject("camera"));

		return true;
	}

	@Override
	public boolean saveState(String savePath) {
		JsonBuilder builder = new JsonBuilder();
		// time and environment density
		builder.add_optional("timeFactor", timeFactor, 1f);
		builder.add_optional("environmentDensity", environmentDensity, 1.2f);

		// camera pos
		builder.add_optional("camera", cameraPos, cameraPos.getMagnitude() != 0);

		// spawn limits
		// builder.add("characterLimit", characterLimit.toJSON());
		// builder.add("projectileLimit", projectileLimit.toJSON());
		// builder.add("itemLimit", itemLimit.toJSON());

		return saveState(savePath, builder);
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

	@Override
	public boolean registerGameObject(GameObject object) {
		if (super.registerGameObject(object)) {
			// bump up spawn limit
			updateSpawnLimits(object, 1);

			return true;
		}

		return false;
	}

	@Override
	public boolean unregisterGameObject(GameObject object) {
		if (super.unregisterGameObject(object)) {
			// bump down spawn limit
			updateSpawnLimits(object, -1);

			return true;
		}

		return false;
	}

	public boolean registerLight(Light light) {
		if (currentLightsSize < currentLights.length) {
			currentLights[currentLightsSize] = light;
			currentLightsSize++;
			return true;
		}

		return false;
	}
}
