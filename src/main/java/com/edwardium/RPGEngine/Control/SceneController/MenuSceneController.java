package com.edwardium.RPGEngine.Control.SceneController;

import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Rectangle;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class MenuSceneController extends SceneController {

	private static final Vector2D menuItemSize = new Vector2D(200, 50);
	private static final float menuItemSpace = 10;

	private static final Color menuItemBackgroundColor = new Color(0, 0, 0);
	private static final Color menuItemForegroundColor = new Color(1, 1, 1);

	private static final Color menuItemActiveBackgroundColor = new Color();
	private static final Color menuItemActiveForegroundColor = new Color(0, 0, 0);

	private String[] menuItems;

	public MenuSceneController(Input gameInput) {
		super(gameInput);

		menuItems = new String[] {
				"Start Game",
				"Settings",
				"Quit"
		};
	}

	@Override
	public void update(double unprocessedTime) {

	}

	@Override
	public void render(Renderer renderer) {
		Vector2D basePosition = new Vector2D().subtract(new Vector2D(menuItemSize).divide(2)).subtract(new Vector2D(0, (menuItemSize.getY() + menuItemSpace) * (menuItems.length - 1) / 2));

		for (int i = 0; i < menuItems.length; i++) {
			Vector2D currentPosition = Vector2D.add(basePosition, new Vector2D(0, menuItemSize.getY() + menuItemSpace).multiply(i));
			Rectangle currentRectangle = new Rectangle(currentPosition, Vector2D.add(currentPosition, menuItemSize));

			renderer.drawRectangle(currentRectangle, 0, new TextureInfo("default", menuItemBackgroundColor));

			renderer.drawString(renderer.basicFont, menuItems[i], currentRectangle.center(), null, 0, menuItemForegroundColor, Renderer.StringAlignment.CENTER);
		}
	}
}
