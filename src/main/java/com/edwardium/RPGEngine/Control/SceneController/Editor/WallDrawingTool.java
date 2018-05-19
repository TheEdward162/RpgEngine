package com.edwardium.RPGEngine.Control.SceneController.Editor;

import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Utility.Vector2D;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;

public class WallDrawingTool extends PointDrawingTool {
	private static TextureInfo ICON = new TextureInfo("editor", null, new Vector2D(128, 0), new Vector2D(64, 64));

	public WallDrawingTool() {
		super("Wall Drawing Tool", ICON);
	}

	@Override
	public boolean updateInput(EditorSceneController esc, Input gameInput, double unprocessedTime) {
		if (isDrawing) {
			if (gameInput.getKeyPressed(GLFW_KEY_ENTER)) {
				if (drawingPoints.size() >= 3) {
					Vector2D[] vecArray = drawingPoints.toArray(new Vector2D[drawingPoints.size()]);
					Vector2D center = Vector2D.center(vecArray);
					GameWall nWall = new GameWall(center, Vector2D.add(vecArray, Vector2D.inverse(center)));
					esc.getGameObjects().add(nWall);
				}

				drawingPoints.clear();
				isDrawing = false;
				closestDrawingPoint = null;
				selectedDrawingPoint = null;

				gameInput.lockKey(GLFW_KEY_ENTER);
				return true;
			}
		} else {
			esc.mainTransformTool.updateInput(esc, gameInput, unprocessedTime);

			if (gameInput.getMouseJustPressed(EditorSceneController.TOOLS_MOUSE_BUTTON, unprocessedTime)) {
				isDrawing = true;
				selectedDrawingPoint = new Vector2D(esc.cursorPos);
				drawingPoints.add(selectedDrawingPoint);
				gameInput.lockKey(EditorSceneController.TOOLS_MOUSE_BUTTON);
			}
		}

		return super.updateInput(esc, gameInput, unprocessedTime);
	}

	@Override
	public void render(EditorSceneController esc, Renderer renderer) {
		esc.mainTransformTool.render(esc, renderer);
		super.render(esc, renderer);
	}

	@Override
	public void renderUI(EditorSceneController esc, Renderer renderer, boolean drawIcon) {
		if (!isDrawing)
			esc.mainTransformTool.renderUI(esc, renderer, false);
		super.renderUI(esc, renderer, drawIcon);
	}
}
