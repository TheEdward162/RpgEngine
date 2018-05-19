package com.edwardium.RPGEngine.Control.SceneController.Editor;

import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Utility.Vector2D;

public class DeleteTool extends Tool {
	private static TextureInfo ICON = new TextureInfo("editor", null, new Vector2D(64, 0), new Vector2D(64, 64));

	public DeleteTool() {
		super("Delete Tool", ICON);
	}

	@Override
	public boolean updateInput(EditorSceneController esc, Input gameInput, double unprocessedTime) {
		esc.mainTransformTool.updateInput(esc, gameInput, unprocessedTime);

		if (gameInput.getMouseJustPressed(EditorSceneController.TOOLS_MOUSE_BUTTON, unprocessedTime)) {
			if (esc.mainTransformTool.closestObject != null) {
				esc.getGameObjects().remove(esc.mainTransformTool.closestObject);
				esc.mainTransformTool.closestObject = null;
			} else if (esc.mainTransformTool.selectedObject != null) {
				esc.getGameObjects().remove(esc.mainTransformTool.selectedObject);
				esc.mainTransformTool.selectedObject = null;
			}
		}

		return true;
	}

	@Override
	public void render(EditorSceneController esc, Renderer renderer) {
		esc.mainTransformTool.render(esc, renderer);
		super.render(esc, renderer);
	}

	@Override
	public void renderUI(EditorSceneController esc, Renderer renderer, boolean drawIcon) {
		esc.mainTransformTool.renderUI(esc, renderer, false);
		super.renderUI(esc, renderer, drawIcon);
	}
}
