package com.edwardium.RPGEngine.Control.SceneController.Editor;

import com.edwardium.RPGEngine.Control.UI;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Utility.Vector2D;

import static com.edwardium.RPGEngine.Control.SceneController.Editor.EditorSceneController.TRANSFORMS_MOUSE_BUTTON;

public class MultiTool extends Tool {
	private static TextureInfo ICON = new TextureInfo("editor", null, new Vector2D(), new Vector2D(64, 64));

	public GameObject closestObject = null;
	public GameObject selectedObject = null;

	public MultiTool() {
		super("Multi Tool", ICON);
	}

	@Override
	public boolean updateInput(EditorSceneController esc, Input gameInput, double unprocessedTime) {
		closestObject = esc.getClosestObject(esc.cursorPos, EditorSceneController.CURSOR_SELECTION_RADIUS);

		// object transforms
		EditorSceneController.TransformsInput transforms = esc.updateInputObjectTransforms(unprocessedTime, TRANSFORMS_MOUSE_BUTTON, EditorSceneController.DRAG_MINIMUM_TIME);
		if (transforms.isMouseClick() && closestObject != null && closestObject != selectedObject) {
			// select a new object
			selectedObject = closestObject;
			gameInput.lockKey(TRANSFORMS_MOUSE_BUTTON);
		} else if (selectedObject != null) {
			if (transforms.isMouseClick()) {
				// reposition selectedObject on click, if we didn't select a new object
				selectedObject.position = new Vector2D(esc.cursorPos);
			} else if (transforms.isPositionAbsolute()) {
				selectedObject.position.set(transforms.positionVector);
			} else {
				selectedObject.position.add(transforms.positionVector);
			}

			if (transforms.isRotationAbsolute()) {
				selectedObject.rotateTo(transforms.rotation, true);
			} else if (transforms.isMouseDrag()) {
				selectedObject.rotateToPoint(esc.cursorPos, true);
			} else {
				selectedObject.rotateBy(transforms.rotation, true);
			}

			// Scale not implemented for anything, but it's good for future to have it here
		}

		if (gameInput.getMouseJustPressed(EditorSceneController.TOOLS_MOUSE_BUTTON, unprocessedTime)) {
			if (selectedObject != null && closestObject != null) {
				if (selectedObject instanceof GameCharacter && ((GameCharacter) selectedObject).inventory.getFreeSpace() > 0
					&& closestObject instanceof GameItem && ((GameItem) closestObject).canPickup) {
					((GameCharacter) selectedObject).inventory.insertItem((GameItem) closestObject);

					// TODO: Abstraction over inventory insertion in editor vs in play
					esc.getGameObjects().remove(closestObject);
				}
			}
		}

		return true;
	}

	@Override
	public void render(EditorSceneController esc, Renderer renderer) {
		if (closestObject != null && closestObject != selectedObject) {
			closestObject.renderHitbox(renderer, EditorSceneController.R_HIGHLIGHT_COLOR);
		}
		if (selectedObject != null) {
			selectedObject.renderHitbox(renderer, EditorSceneController.R_SELECTION_COLOR);
		}

		super.render(esc, renderer);
	}

	@Override
	public void renderUI(EditorSceneController esc, Renderer renderer, boolean drawIcon) {
		if (selectedObject != null) {
			UI.drawCornerString(renderer, UI.Corner.TOPRIGHT, "Object position: " + selectedObject.position);
			UI.drawCornerString(renderer, UI.Corner.TOPRIGHT, String.format("Object rotation: %.2f", selectedObject.getRotation()));
		}

		super.renderUI(esc, renderer, drawIcon);
	}
}
