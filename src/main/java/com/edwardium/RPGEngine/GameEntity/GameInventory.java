package com.edwardium.RPGEngine.GameEntity;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.Control.SceneController.PlaySceneController;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.IGameUsableItem;
import com.edwardium.RPGEngine.IO.JsonBuilder;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Utility.GameSerializable;
import com.edwardium.RPGEngine.Utility.Rectangle;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class GameInventory implements GameSerializable {

	private final GameItem[] items;

	private int activeIndex = 0;

	public GameInventory(int size) {
		items = new GameItem[size];
	}
	public GameInventory(JsonObject sourceObj) {
		if (sourceObj == null) {
			items = new GameItem[0];
			return;
		}

		try {
			activeIndex = sourceObj.getJsonNumber("activeIndex").intValue();
		} catch (NullPointerException | ClassCastException ignored) { }

		JsonArray itemsArray = null;
		try {
			itemsArray = sourceObj.getJsonArray("items");
		} catch (NullPointerException | ClassCastException ignored) {}

		if (itemsArray == null) {
			items = new GameItem[0];
		} else {
			items = new GameItem[itemsArray.size()];

			for (int i = 0; i < items.length; i++) {
				try {
					if (itemsArray.isNull(i))
						items[i] = null;
					else
						items[i] = (GameItem) GameItem.fromJSON(itemsArray.getJsonObject(i));
				} catch (ClassCastException ignored) {}
			}
		}
	}

	public GameItem getActiveItem() {
		return items.length > 0 ? items[activeIndex] : null;
	}
	public boolean isActiveEmpty() {
		return getActiveItem() == null;
	}

	public void setActiveIndex(int index) {
		if (!canSwitch() || items.length == 0)
			return;

		this.activeIndex = index % items.length;
		while (this.activeIndex < 0)
			this.activeIndex += items.length;
	}
	public void shiftActiveIndex(int shift) {
		setActiveIndex(this.activeIndex + shift);
	}

	public boolean canSwitch() {
		GameItem activeItem = getActiveItem();
		return activeItem == null || !(activeItem instanceof IGameUsableItem) || ((IGameUsableItem) activeItem).canUse(null, null, null);
	}

	public int getSize() {
		return items.length;
	}
	public int getFreeSpace() {
		int free = 0;
		for (GameItem item : items) {
			if (item == null)
				free++;
		}

		return free;
	}

	private int findFirstEmpty() {
		for (int i = 0; i < items.length; i++) {
			if (items[i] == null)
				return i;
		}

		return -1;
	}

	public int findFirstItem() {
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null)
				return i;
		}

		return -1;
	}

	public boolean insertItem(GameItem item) {
		if (item == null)
			return false;

		int firstEmpty = findFirstEmpty();
		if (firstEmpty >= 0) {
			PlaySceneController gsc = Engine.gameEngine.getCurrentPlayController();
			if (gsc != null)
				gsc.unregisterGameObject(item);

			items[firstEmpty] = item;
			item.isDrawn = false;
			item.doesCollide = false;

			return true;
		} else {
			return false;
		}
	}

	public void onUpdate(GameCharacter owner, float elapsedTime, float environmentDensity, boolean updateLights, PlaySceneController gsc) {
		for (GameItem item : items) {
			if (item != null) {
				if (owner != null) {
					item.position = owner.getFacingDirection().setMagnitude(item.getHeldSize().getX() / 2).add(owner.position);
					item.rotateTo(owner.getRotation(), true);
				}
				if (elapsedTime > 0)
					item.updatePhysics(elapsedTime, environmentDensity);
				if (updateLights)
					item.updateLights(gsc);
			}
		}
	}

	public GameItem swapWithActiveItem(GameItem item) {
		if (item == null)
			return removeActiveItem();

		GameItem lastItem = removeActiveItem();

		PlaySceneController gsc = Engine.gameEngine.getCurrentPlayController();
		if (gsc != null)
			gsc.unregisterGameObject(item);

		items[activeIndex] = item;
		item.isDrawn = false;
		item.doesCollide = false;

		return lastItem;
	}
	public GameItem removeActiveItem() {
		GameItem item = getActiveItem();
		if (item == null)
			return null;

		items[activeIndex] = null;
		item.isDrawn = true;
		item.doesCollide = true;

		PlaySceneController gsc = Engine.gameEngine.getCurrentPlayController();
		if (gsc != null)
			gsc.registerGameObject(item);
		return item;
	}

	private static final Vector2D r_inventoryItemSize = new Vector2D(200, 30);
	private static final float r_inventoryItemSpace = 5;

	private static final Color r_inventoryShadowColor = new Color(0, 0, 0, 0.5f);
	private static final Color r_inventoryShadowChargingColor = new Color(1f, 0, 0, 0.2f);
	private static final Color r_inventoryShadowCooldownColor = new Color(0, 0, 1f, 0.2f);

	private static final Color r_inventoryNumberColor = Color.WHITE;
	private static final Color r_inventoryNumberActiveColor = Color.RED;

	public static void renderInventory(GameInventory inventory, Renderer renderer, Vector2D basePosition, Vector2D scale) {
		for (int i = 0; i < inventory.items.length; i++) {
			GameItem currentItem = inventory.items[i];
			boolean isActive = inventory.activeIndex == i;

			Rectangle itemRectangle = new Rectangle(
					new Vector2D(basePosition).add(new Vector2D(0, i * (r_inventoryItemSize.getY() + r_inventoryItemSpace)).scale(scale)),
					new Vector2D(basePosition).add(new Vector2D(0, i * (r_inventoryItemSize.getY() + r_inventoryItemSpace)).scale(scale)).add(Vector2D.scale(r_inventoryItemSize, scale))
			);
			Vector2D centerPosition = itemRectangle.center();

			// shadow
//			renderer.drawRectangle(new Renderer.RenderInfo(centerPosition.scale(scale), Vector2D.scale(r_inventoryItemSize, scale), 0f, r_inventoryShadowColor, false));
			renderer.drawRectangle(itemRectangle, new Renderer.RenderInfo(null, 1f, 0f, r_inventoryShadowColor, false));

			if (isActive && currentItem != null) {
				if (currentItem instanceof IGameUsableItem) {
					IGameUsableItem usableItem = (IGameUsableItem) currentItem;

					float chargeupPercent = usableItem.getMaxChargeup() != 0 ? usableItem.getChargeup() / usableItem.getMaxChargeup() : 0;
					if (chargeupPercent > 0) {
						Rectangle chargeupShadowRectangle = new Rectangle(itemRectangle).scale(chargeupPercent, 1f);
						renderer.drawRectangle(chargeupShadowRectangle, new Renderer.RenderInfo(null, 1f, 0f, r_inventoryShadowChargingColor, false));
					} else {
						float cooldownPercent = usableItem.getMaxCooldown() != 0 ? usableItem.getCooldown() / usableItem.getMaxCooldown() : 0;
						if (cooldownPercent > 0) {
							Rectangle cooldownShadowRectangle = new Rectangle(itemRectangle).scale(cooldownPercent, 1f);
							renderer.drawRectangle(cooldownShadowRectangle, new Renderer.RenderInfo(null, 1f, 0f, r_inventoryShadowCooldownColor, false));
						}
					}
				}
			}

			// number
			Color textColor = r_inventoryNumberColor;
			if (isActive)
				textColor = r_inventoryNumberActiveColor;
//			renderer.drawString(renderer.basicFont, String.valueOf(i + 1) + ".", Vector2D.add(centerPosition, new Vector2D(37 - r_inventoryItemSize.getX() / 2, 6)), new Vector2D(1, 1), textColor);

			if (currentItem != null) {
				Rectangle imageRectangle = Rectangle.setWidth(itemRectangle, 32);
				renderer.drawRectangle(imageRectangle, new Renderer.RenderInfo(null, 1f, 0f, currentItem.getInventoryTexture(), false));
				renderer.drawString(renderer.basicFont, currentItem.name, new Renderer.RenderInfo(Vector2D.add(centerPosition, 37 - r_inventoryItemSize.getX() / 2, 6), 1f, 0f, textColor, false));
			} else {
				renderer.drawString(renderer.basicFont, "Empty", new Renderer.RenderInfo(Vector2D.add(centerPosition, 5 - r_inventoryItemSize.getX() / 2, 6), 1f, 0f, textColor, false));
			}
		}
	}

	@Override
	public JsonObject toJSON() {
		JsonBuilder builder = new JsonBuilder().add_optional("activeIndex", activeIndex, 0);

		builder.add("items", items);

		return builder.build();
	}
}
