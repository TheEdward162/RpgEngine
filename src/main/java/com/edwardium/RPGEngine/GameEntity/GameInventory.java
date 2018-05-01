package com.edwardium.RPGEngine.GameEntity;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.IGameUsableItem;
import com.edwardium.RPGEngine.IO.JsonBuilder;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Utility.GameSerializable;
import com.edwardium.RPGEngine.Utility.Rectangle;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

public class GameInventory implements GameSerializable {

	private final GameItem[] items;

	private int activeIndex = 0;

	public GameInventory(int size) {
		items = new GameItem[size];
	}

	public GameItem getActiveItem() {
		return items.length > 0 ? items[activeIndex] : null;
	}
	public boolean isActiveEmpty() {
		return getActiveItem() == null;
	}

	public void setActiveIndex(int index) {
		if (!canSwitch())
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

	public boolean insertItem(GameItem item) {
		int firstEmpty = findFirstEmpty();
		if (firstEmpty >= 0) {

			items[firstEmpty] = item;
			item.isDrawn = false;
			item.doesCollide = false;

			return true;
		} else {
			return false;
		}
	}

	public GameItem insertActiveItem(GameItem item) {
		GameItem lastItem = removeActiveItem();

		items[activeIndex] = item;
		item.isDrawn = false;
		item.doesCollide = false;

		return lastItem;
	}
	public GameItem removeActiveItem() {
		GameItem item = getActiveItem();

		items[activeIndex] = null;
		item.isDrawn = true;
		item.doesCollide = true;

		return item;
	}

	private static final Vector2D r_inventoryItemSize = new Vector2D(200, 30);
	private static final float r_inventoryItemSpace = 5;

	private static final Color r_inventoryShadowColor = new Color(0, 0, 0, 0.5f);
	private static final Color r_inventoryNumberColor = new Color(1, 1, 1, 1f);
	private static final Color r_inventoryNumberActiveColor = new Color(1f, 0, 0, 1f);

	public static void renderInventory(GameInventory inventory, Renderer renderer, Vector2D basePosition, Vector2D scale) {
		for (int i = 0; i < inventory.items.length; i++) {
			Rectangle itemRectangle = new Rectangle(
					new Vector2D(basePosition).add(new Vector2D(0, i * (r_inventoryItemSize.getY() + r_inventoryItemSpace))),
					new Vector2D(basePosition).add(new Vector2D(0, i * (r_inventoryItemSize.getY() + r_inventoryItemSpace))).add(r_inventoryItemSize)
			);
			Vector2D centerPosition = itemRectangle.center();

			// shadow
			renderer.drawRectangle(centerPosition.scale(scale), Vector2D.scale(r_inventoryItemSize, scale), 0, new TextureInfo("default", r_inventoryShadowColor));

			// number
			Color textColor = r_inventoryNumberColor;
			if (inventory.activeIndex == i)
				textColor = r_inventoryNumberActiveColor;
//			renderer.drawString(renderer.basicFont, String.valueOf(i + 1) + ".", Vector2D.add(centerPosition, new Vector2D(37 - r_inventoryItemSize.getX() / 2, 6)), new Vector2D(1, 1), textColor);

			if (inventory.items[i] != null) {
				Rectangle imageRectangle = Rectangle.setWidth(itemRectangle, 32);
				renderer.drawRectangle(imageRectangle, 0, inventory.items[i].getInventoryTexture());
				renderer.drawString(renderer.basicFont, inventory.items[i].name, Vector2D.add(centerPosition, new Vector2D(37 - r_inventoryItemSize.getX() / 2, 6)), null, 0, textColor);
			} else {
				renderer.drawString(renderer.basicFont, "Empty", Vector2D.add(centerPosition, new Vector2D(5 - r_inventoryItemSize.getX() / 2, 6)), null, 0, textColor);
			}
		}
	}

	@Override
	public JsonObject toJSON() {
		JsonBuilder builder = new JsonBuilder().add_optional("activeIndex", activeIndex, 0);
		JsonArrayBuilder itemsArrayBuilder = Json.createArrayBuilder();
		// TODO: Inventory

		return builder.build();
	}

	public static GameInventory fromJSON(JsonObject sourceObj) {
		if (sourceObj == null)
			return new GameInventory(0);

		return new GameInventory(0);
	}
}
