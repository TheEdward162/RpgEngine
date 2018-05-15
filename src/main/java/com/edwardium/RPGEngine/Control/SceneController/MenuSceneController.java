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

	private interface MenuItemNameGetter {
		String getName();
	}

	private interface MenuItemCallback {
		void invoke();
	}

	private class MenuItem {
		public final MenuItemNameGetter nameGetter;
		public final MenuItemCallback callback;

		public MenuItem(MenuItemNameGetter nameGetter, MenuItemCallback callback) {
			this.nameGetter = nameGetter;
			this.callback = callback;
		}
	}

	private static final Vector2D menuItemSize = new Vector2D(200, 50);
	private static final float menuItemSpace = 10;

	private static final Color menuItemBackgroundColor = new Color(0, 0, 0);
	private static final Color menuItemForegroundColor = new Color(1, 1, 1);

	private static final Color menuItemActiveBackgroundColor = new Color();
	private static final Color menuItemActiveForegroundColor = new Color(0, 0, 0);

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
						() -> { switchMenu(MenuType.PAUSE); Engine.gameEngine.changeSceneController(Engine.SceneControllerType.GAME);}),
				new MenuItem(() -> "Settings", () -> this.switchMenu(MenuType.SETTINGS)),
				new MenuItem(() -> "Quit", () -> Engine.gameEngine.changeSceneController(Engine.SceneControllerType.QUIT))
		};
		menus.put(MenuType.MAIN, mainMenu);

		MenuItem[] settingsMenu = {
				new MenuItem(() -> "VSYNC: " + (Engine.gameEngine.getVSync() ? "ON" : "OFF"),
						() -> Engine.gameEngine.toggleVSync()),
				new MenuItem(() -> "Back", this::restoreMenu)
		};
		menus.put(MenuType.SETTINGS, settingsMenu);

		MenuItem[] pauseMenu = {
				new MenuItem(() -> "Continue", () -> Engine.gameEngine.restoreLastSceneController()),
				new MenuItem(() -> "Settings", () -> this.switchMenu(MenuType.SETTINGS)),
				new MenuItem(() -> "Quit to menu", () -> switchMenu(MenuType.MAIN)),
				new MenuItem(() -> "Quit", () -> Engine.gameEngine.changeSceneController(Engine.SceneControllerType.QUIT))
		};
		menus.put(MenuType.PAUSE, pauseMenu);

		gameInput.watchKey(GLFW_KEY_UP);
		gameInput.watchKey(GLFW_KEY_DOWN);
		gameInput.watchKey(GLFW_KEY_ENTER);
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
	}

	private void enterPress() {
		MenuItem[] currentMenu = menus.get(currentMenuType);
		MenuItem currentMenuItem = currentMenu[currentMenuItemIndex];
		currentMenuItem.callback.invoke();
	}

	@Override
	public void render(Renderer renderer) {
		MenuItem[] currentMenu = menus.get(currentMenuType);

		Vector2D basePosition = new Vector2D().subtract(new Vector2D(menuItemSize).divide(2)).subtract(new Vector2D(0, (menuItemSize.getY() + menuItemSpace) * (currentMenu.length - 1) / 2));

		for (int i = 0; i < currentMenu.length; i++) {
			Vector2D currentPosition = Vector2D.add(basePosition, new Vector2D(0, menuItemSize.getY() + menuItemSpace).scale(i));
			Rectangle currentRectangle = new Rectangle(currentPosition, Vector2D.add(currentPosition, menuItemSize));

			Color itemBGColor = menuItemBackgroundColor;
			if (i == currentMenuItemIndex)
				itemBGColor = menuItemActiveBackgroundColor;
			renderer.drawRectangle(currentRectangle, new Renderer.RenderInfo(null, 1f, 0f, itemBGColor, false));

			Color itemFGColor = menuItemForegroundColor;
			if (i == currentMenuItemIndex)
				itemFGColor = menuItemActiveForegroundColor;
			renderer.drawString(renderer.basicFont, currentMenu[i].nameGetter.getName(), new Renderer.RenderInfo(currentRectangle.center(), 1f, 0f, itemFGColor, false), Renderer.StringAlignment.CENTER);
		}
	}

	@Override
	public void cleanup() {
		gameInput.unwatchKey(GLFW_KEY_UP);
		gameInput.unwatchKey(GLFW_KEY_DOWN);
		gameInput.unwatchKey(GLFW_KEY_ENTER);
	}
}
