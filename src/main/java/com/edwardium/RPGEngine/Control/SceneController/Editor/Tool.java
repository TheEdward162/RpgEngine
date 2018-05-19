package com.edwardium.RPGEngine.Control.SceneController.Editor;

import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Utility.Vector2D;

public abstract class Tool {
	public final String name;
	private TextureInfo icon;

	protected Tool(String name, TextureInfo icon) {
		this.name = name;
		this.icon = icon != null ? icon : new TextureInfo("debug");
	}

	public abstract boolean updateInput(EditorSceneController esc, Input gameInput, double unprocessedTime);

	public void render(EditorSceneController esc, Renderer renderer) {}

	public void renderUI(EditorSceneController esc, Renderer renderer, boolean drawIcon) {
		if (drawIcon && icon != null) {
			Vector2D topLeft = renderer.getWindowSize().scale(-0.5f).add(10, 35);
			Vector2D bottomRight = new Vector2D(64, 64).add(topLeft);

			renderer.drawRectangle(new Renderer.RenderInfo(Vector2D.center(topLeft, bottomRight).add(0f, 0),
					Vector2D.subtract(bottomRight, topLeft).absolutize().add(5f, 5f), 0f, new TextureInfo("default", Color.BLACKGREY), false));
			renderer.drawRectangle(new Renderer.RenderInfo(Vector2D.center(topLeft, bottomRight),
					Vector2D.subtract(bottomRight, topLeft).absolutize(), 0f, icon, false));
		}
	}
	public void renderUI(EditorSceneController esc, Renderer renderer) {
		renderUI(esc, renderer, true);
	}
}
