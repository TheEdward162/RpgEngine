package com.edwardium.RPGEngine.GameObject;

import com.edwardium.RPGEngine.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Vector2D;

public class GameInventory {

	private final GameItem[] items;

	private int activeIndex = 0;

	public GameInventory(int size) {
		items = new GameItem[size];
	}

	public GameItem getActiveItem() {
		return items[activeIndex];
	}
	public boolean isActiveEmpty() {
		return getActiveItem() == null;
	}

	public void setActiveIndex(int index) {
		this.activeIndex = index % items.length;
		while (this.activeIndex < 0)
			this.activeIndex += items.length;
	}
	public void shiftActiveIndex(int shift) {
		setActiveIndex(this.activeIndex + shift);
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
			return true;
		} else {
			return false;
		}
	}

	public GameItem insertActiveItem(GameItem item) {
		GameItem lastItem = removeActiveItem();

		items[activeIndex] = item;

		return lastItem;
	}
	public GameItem removeActiveItem() {
		GameItem item = getActiveItem();

		items[activeIndex] = null;

		return item;
	}

	private static final Vector2D r_inventoryItemSize = new Vector2D(70, 30);
	private static final float r_inventoryItemSpace = 5;

	private static final float[] r_inventoryShadowColor = new float[] { 0, 0, 0, 0.5f };
	private static final float[] r_inventoryNumberColor = new float[] { 1, 1, 1, 1f };
	private static final float[] r_inventoryNumberActiveColor = new float[] { 1f, 0, 0, 1f };

	public static void renderInventory(GameInventory inventory, Renderer renderer, Vector2D basePosition, Vector2D scale) {
		for (int i = 0; i < inventory.items.length; i++) {
			Vector2D centerPosition = new Vector2D(basePosition).add(new Vector2D(0, i * (r_inventoryItemSize.getY() + r_inventoryItemSpace))).add(Vector2D.divide(r_inventoryItemSize, 2));

			// shadow
			renderer.drawRectangle(centerPosition.scale(scale), Vector2D.scale(r_inventoryItemSize, scale), 0, r_inventoryShadowColor, null);

			// number
			float[] numberColor = r_inventoryNumberColor;
			if (inventory.activeIndex == i)
				numberColor = r_inventoryNumberActiveColor;
			renderer.drawString(renderer.basicFont, String.valueOf(i + 1) + ".", Vector2D.add(centerPosition, new Vector2D(5 - r_inventoryItemSize.getX() / 2, 6)), new Vector2D(1, 1), numberColor);

			// TODO: Render item image and item name
		}
	}
}
