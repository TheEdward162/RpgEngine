package com.edwardium.RPGEngine.Control.SceneController;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Utility.Rectangle;
import com.edwardium.RPGEngine.Utility.Vector2D;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class MenuSceneController extends SceneController {

	private enum MenuType { MAIN, SETTINGS, PAUSE }

	private Vector2D cursorPos;

	private interface MenuItemNameGetter {
		String getName();
	}

	private interface MenuItemCallback {
		void invoke();
	}

	private static class MenuItem {
		public static final Rectangle defaultRectangle = new Rectangle(new Vector2D(-100, -25), new Vector2D(100, 25));

		public final MenuItemNameGetter nameGetter;
		public final MenuItemCallback callback;

		public Color background = Color.BLACK;
		public Color foreground = Color.WHITE;

		public Color selectedBackground = Color.WHITE;
		public Color selectedForeground = Color.BLACK;

		public Rectangle rectangle = new Rectangle(defaultRectangle);

		public MenuItem(MenuItemNameGetter nameGetter, MenuItemCallback callback) {
			this.nameGetter = nameGetter;
			this.callback = callback;
		}

		public MenuItem setBackground(Color background) {
			this.background = background;

			return this;
		}
		public MenuItem setForeround(Color foreground) {
			this.foreground = foreground;

			return this;
		}

		public MenuItem setBackgroundSelected(Color background) {
			this.selectedBackground = background;

			return this;
		}
		public MenuItem setForeroundSelected(Color foreground) {
			this.selectedForeground = foreground;

			return this;
		}

		public MenuItem setRectangle(Rectangle rectangle) {
			this.rectangle = rectangle;

			return this;
		}
	}

	private HashMap<MenuType, MenuItem[]> menus;
	private MenuType currentMenuType = MenuType.MAIN;
	private MenuType lastMenuType = null;

	private int currentMenuItemIndex = 0;

	public MenuSceneController(Input gameInput) {
		super(gameInput);

		menus = new HashMap<>(3);

		// build menus
		MenuItem[] mainMenu = {
				new MenuItem(() -> "Start Game",
						() -> { switchMenu(MenuType.PAUSE); Engine.gameEngine.changeSceneController(Engine.SceneControllerType.GAME); })
				.setRectangle(Rectangle.shiftBy(MenuItem.defaultRectangle, new Vector2D(0f, -30 - 60))),

				new MenuItem(() -> "Level editor",
						() -> { switchMenu(MenuType.PAUSE); Engine.gameEngine.changeSceneController(Engine.SceneControllerType.EDITOR); })
				.setRectangle(Rectangle.shiftBy(MenuItem.defaultRectangle, new Vector2D(0f, -30))),

				new MenuItem(() -> "Settings", () -> switchMenu(MenuType.SETTINGS))
				.setRectangle(Rectangle.shiftBy(MenuItem.defaultRectangle, new Vector2D(0f, 30))),

				new MenuItem(() -> "Quit", () -> Engine.gameEngine.changeSceneController(Engine.SceneControllerType.QUIT))
				.setRectangle(Rectangle.shiftBy(MenuItem.defaultRectangle, new Vector2D(0f, 30 + 60))),
		};
		menus.put(MenuType.MAIN, mainMenu);

		MenuItem[] settingsMenu = {
				new MenuItem(() -> "VSYNC: " + (Engine.gameEngine.getVSync() ? "ON" : "OFF"),
						() -> Engine.gameEngine.toggleVSync())
				.setRectangle(Rectangle.shiftBy(MenuItem.defaultRectangle, new Vector2D(0f, -30))),

				new MenuItem(() -> "Back", this::restoreMenu)
				.setRectangle(Rectangle.shiftBy(MenuItem.defaultRectangle, new Vector2D(0f, 30))),
		};
		menus.put(MenuType.SETTINGS, settingsMenu);

		MenuItem[] pauseMenu = {
				new MenuItem(() -> "Continue", () -> Engine.gameEngine.restoreLastSceneController())
				.setRectangle(Rectangle.shiftBy(MenuItem.defaultRectangle, new Vector2D(0f, -30 - 60))),

				new MenuItem(() -> "Settings", () -> switchMenu(MenuType.SETTINGS))
				.setRectangle(Rectangle.shiftBy(MenuItem.defaultRectangle, new Vector2D(0f, -30))),

				new MenuItem(() -> "Quit to menu", () -> switchMenu(MenuType.MAIN))
				.setRectangle(Rectangle.shiftBy(MenuItem.defaultRectangle, new Vector2D(0f, 30))),

				new MenuItem(() -> "Quit", () -> Engine.gameEngine.changeSceneController(Engine.SceneControllerType.QUIT))
				.setRectangle(Rectangle.shiftBy(MenuItem.defaultRectangle, new Vector2D(0f, 30 + 60))),
		};
		menus.put(MenuType.PAUSE, pauseMenu);

		restore();
	}

	private void switchMenu(MenuType to) {
		lastMenuType = currentMenuType;
		currentMenuType = to;
		currentMenuItemIndex = 0;
	}
	private void restoreMenu() {
		if (lastMenuType != null) {
			MenuType temp = currentMenuType;
			currentMenuType = lastMenuType;
			lastMenuType = temp;
			currentMenuItemIndex = 0;
		}
	}

	@Override
	public void update(double unprocessedTime) {
		MenuItem[] currentMenu = menus.get(currentMenuType);

		Vector2D currentCursorPos = gameInput.getGameCursorPos();
		boolean cursorPosChanged = !currentCursorPos.equals(cursorPos);
		cursorPos = currentCursorPos;

		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_ENTER, unprocessedTime)) {
			enterPress();
			return;
		}

		int menuItemShift = 0;
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_DOWN, unprocessedTime)) {
			menuItemShift++;
		}
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_UP, unprocessedTime)) {
			menuItemShift--;
		}

		currentMenuItemIndex = (currentMenuItemIndex + menuItemShift) % currentMenu.length;
		if (currentMenuItemIndex < 0)
			currentMenuItemIndex += currentMenu.length;

		if (cursorPosChanged) {
			for (int i = 0; i < currentMenu.length; i++) {
				if (Rectangle.pointCollision(currentMenu[i].rectangle, cursorPos)) {
					currentMenuItemIndex = i;
					break;
				}
			}
		}
		if (gameInput.getMousePressed(GLFW_MOUSE_BUTTON_1)) {
			for (int i = 0; i < currentMenu.length; i++) {
				if (Rectangle.pointCollision(currentMenu[i].rectangle, cursorPos)) {
					gameInput.lockKey(GLFW_MOUSE_BUTTON_1);
					enterPress();
					break;
				}
			}
		}
	}

	private void enterPress() {
		cursorPos = null;

		MenuItem[] currentMenu = menus.get(currentMenuType);
		MenuItem currentMenuItem = currentMenu[currentMenuItemIndex];
		currentMenuItem.callback.invoke();
	}

	@Override
	public void render(Renderer renderer) {
		MenuItem[] currentMenu = menus.get(currentMenuType);

		for (int i = 0; i < currentMenu.length; i++) {
			Color background = currentMenu[i].background;
			Color foreground = currentMenu[i].foreground;
			if (i == currentMenuItemIndex) {
				background = currentMenu[i].selectedBackground;
				foreground = currentMenu[i].selectedForeground;
			}

			renderer.drawRectangle(currentMenu[i].rectangle, new Renderer.RenderInfo(null, 1f, 0f, background, false));
			renderer.drawString(renderer.basicFont, currentMenu[i].nameGetter.getName(),
					new Renderer.RenderInfo(currentMenu[i].rectangle.center(), 1f, 0f, foreground, false),
					Renderer.StringAlignment.CENTER);
		}

		Engine.gameEngine.drawDefaultCornerStrings();
	}

	@Override
	public void freeze() {
		cleanup();
	}

	@Override
	public void restore() {
		gameInput.watchKey(GLFW_KEY_UP);
		gameInput.watchKey(GLFW_KEY_DOWN);
		gameInput.watchKey(GLFW_KEY_ENTER);
	}

	@Override
	public void cleanup() {
		gameInput.unwatchKey(GLFW_KEY_UP);
		gameInput.unwatchKey(GLFW_KEY_DOWN);
		gameInput.unwatchKey(GLFW_KEY_ENTER);
	}
}
