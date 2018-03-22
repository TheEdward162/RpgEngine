package com.edwardium.RPGEngine.Control.SceneController;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Rectangle;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

import static org.lwjgl.glfw.GLFW.*;

public class MenuSceneController extends SceneController {

	private static final Vector2D menuItemSize = new Vector2D(200, 50);
	private static final float menuItemSpace = 10;

	private static final Color menuItemBackgroundColor = new Color(0, 0, 0);
	private static final Color menuItemForegroundColor = new Color(1, 1, 1);

	private static final Color menuItemActiveBackgroundColor = new Color();
	private static final Color menuItemActiveForegroundColor = new Color(0, 0, 0);

	private String[] menuItems;
	private int currentMenuItemIndex = 0;

	public MenuSceneController(Input gameInput) {
		super(gameInput);

		menuItems = new String[] {
				"Start Game",
				"Settings",
				"Quit"
		};

		gameInput.watchKey(GLFW_KEY_UP);
		gameInput.watchKey(GLFW_KEY_DOWN);
		gameInput.watchKey(GLFW_KEY_ENTER);
	}

	@Override
	public void update(double unprocessedTime) {
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

		currentMenuItemIndex = (currentMenuItemIndex + menuItemShift) % menuItems.length;
		if (currentMenuItemIndex < 0)
			currentMenuItemIndex += menuItems.length;
	}

	private void enterPress() {
		String currentMenuValue = menuItems[currentMenuItemIndex];
		switch (currentMenuValue) {
			case "Start Game":
				menuItems[currentMenuItemIndex] = "Continue";
				Engine.gameEngine.changeSceneController(Engine.SceneControllerType.GAME);
				break;
			case "Continue":
				Engine.gameEngine.restoreLastSceneController();
				break;
			case "Settings":
				// Engine.gameEngine.changeSceneController(Engine.SceneControllerType.SETTINGS);
				break;
			case "Quit":
				Engine.gameEngine.changeSceneController(Engine.SceneControllerType.QUIT);
				break;
		}
	}

	@Override
	public void render(Renderer renderer) {
		Vector2D basePosition = new Vector2D().subtract(new Vector2D(menuItemSize).divide(2)).subtract(new Vector2D(0, (menuItemSize.getY() + menuItemSpace) * (menuItems.length - 1) / 2));

		for (int i = 0; i < menuItems.length; i++) {
			Vector2D currentPosition = Vector2D.add(basePosition, new Vector2D(0, menuItemSize.getY() + menuItemSpace).multiply(i));
			Rectangle currentRectangle = new Rectangle(currentPosition, Vector2D.add(currentPosition, menuItemSize));

			Color itemBGColor = menuItemBackgroundColor;
			if (i == currentMenuItemIndex)
				itemBGColor = menuItemActiveBackgroundColor;
			renderer.drawRectangle(currentRectangle, 0, new TextureInfo("default", itemBGColor));

			Color itemFGColor = menuItemForegroundColor;
			if (i == currentMenuItemIndex)
				itemFGColor = menuItemActiveForegroundColor;
			renderer.drawString(renderer.basicFont, menuItems[i], currentRectangle.center(), null, 0, itemFGColor, Renderer.StringAlignment.CENTER);
		}
	}

	@Override
	public void cleanup() {
		gameInput.unwatchKey(GLFW_KEY_UP);
		gameInput.unwatchKey(GLFW_KEY_DOWN);
		gameInput.unwatchKey(GLFW_KEY_ENTER);
	}
}
