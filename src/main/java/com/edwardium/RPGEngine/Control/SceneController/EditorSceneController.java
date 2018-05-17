package com.edwardium.RPGEngine.Control.SceneController;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.Vertex;
import com.edwardium.RPGEngine.Utility.Rectangle;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class EditorSceneController extends GameSceneController {
	private static final Color r_selectionColor = Color.CYAN;
	private static final Color r_drawingPointColor = Color.PINK;
	private static final Color r_drawingPointClosestColor = Color.YELLOW;
	private static final Color r_drawingPointSelectedColor = Color.CYAN;

	private static final Color r_drawingShapeColor = Color.DARKGREY;

	private static final Float cursorSelectionRadius = 25f;
	private static final double dragMinimumTime = 0.25 * 1e+9;

	private static final String[] tools = new String[] {
			"Delete",
			"Draw wall"
	};
	private int toolIndex = 0;

	private GameObject selectedObject;
	private GameObject closestObject;

	private ArrayList<Vector2D> drawingPoints;
	private Vector2D closestDrawingPoint;
	private Vector2D selectedDrawingPoint;
	private boolean isDrawing = false;

	public EditorSceneController(Input gameInput) {
		super(gameInput);

		drawingPoints = new ArrayList<>();

		if (!loadState("Saves/editor-exitsave.json")) {
			player = new GameCharacter(new Vector2D(), "Player", 10);
			gameObjects.add(new GameWall(new Vector2D(), new Rectangle(new Vector2D(-10f, -10f), new Vector2D(10f, 10f))));
		}

		gameObjects.add(player);

		selectedObject = null;
	}

	@Override
	public void update(double unprocessedTime) {
		updateInput(unprocessedTime);
	}

	@Override
	protected boolean updateInput(double unprocessedTime) {
		if (!super.updateInput(unprocessedTime))
			return false;

		float frameTime = (float)(unprocessedTime * Engine.NANO_TIME_MULT);
		boolean shiftsPressed = gameInput.getKeyPressed(GLFW_KEY_LEFT_SHIFT) || gameInput.getKeyPressed(GLFW_KEY_RIGHT_SHIFT);

		float cameraShiftX = 0;
		float cameraShiftY = 0;
		if (gameInput.getKeyPressed(GLFW_KEY_W)) {
			cameraShiftY += 200;
		}
		if (gameInput.getKeyPressed(GLFW_KEY_S)) {
			cameraShiftY -= 200;
		}
		if (gameInput.getKeyPressed(GLFW_KEY_D)) {
			cameraShiftX -= 200;
		}
		if (gameInput.getKeyPressed(GLFW_KEY_A)) {
			cameraShiftX += 200;
		}
		Vector2D cameraShiftVector = new Vector2D(cameraShiftX, cameraShiftY).scale(frameTime);
		if (shiftsPressed) {
			cameraShiftVector.scale(5f);
		}
		cameraPos.add(cameraShiftVector);

		cursorPos = gameInput.getGameCursorPos().subtract(cameraPos);
		closestObject = getClosestObject(cursorPos, cursorSelectionRadius);

		if (isDrawing) {
			return updateInputDrawing(unprocessedTime);
		} else {
			return updateInputNormal(unprocessedTime);
		}
	}

	private boolean updateInputNormal(double unprocessedTime) {
		float frameTime = (float)(unprocessedTime * Engine.NANO_TIME_MULT);
		boolean shiftsPressed = gameInput.getKeyPressed(GLFW_KEY_LEFT_SHIFT) || gameInput.getKeyPressed(GLFW_KEY_RIGHT_SHIFT);

		if (gameInput.getKeyPressed(GLFW_KEY_ENTER)) {
			cameraPos.set(0, 0);
		}

		if (gameInput.getMouseJustPressed(GLFW_MOUSE_BUTTON_2, unprocessedTime)) {
			if (closestObject != null && closestObject != selectedObject) { // select closest object on click
				selectedObject = closestObject;
				gameInput.lockKey(GLFW_MOUSE_BUTTON_2);
			} else if (selectedObject != null) { // reposition selectedObject on click, if we didn't select a new object
				selectedObject.position = new Vector2D(cursorPos);
			}
		} else if (gameInput.getMousePressed(GLFW_MOUSE_BUTTON_2) && gameInput.getMouseTimeSincePress(GLFW_MOUSE_BUTTON_2) >= dragMinimumTime) {
			// This means that we are dragging, basically
			if (selectedObject != null) {
				selectedObject.rotateToPoint(cursorPos, true);
			}
		}

		if (selectedObject != null) {
			// selected object shift
			float objectShiftX = 0;
			float objectShiftY = 0;
			if (gameInput.getKeyPressed(GLFW_KEY_UP)) {
				objectShiftY -= 10;
			}
			if (gameInput.getKeyPressed(GLFW_KEY_DOWN)) {
				objectShiftY += 10;
			}
			if (gameInput.getKeyPressed(GLFW_KEY_LEFT)) {
				objectShiftX -= 10;
			}
			if (gameInput.getKeyPressed(GLFW_KEY_RIGHT)) {
				objectShiftX += 10;
			}


			Vector2D objectShiftVector = new Vector2D(objectShiftX, objectShiftY).scale(frameTime);
			if (shiftsPressed) {
				objectShiftVector.scale(5f);
			}
			selectedObject.position.add(objectShiftVector);

			// selected object reset
			if (gameInput.getKeyPressed(GLFW_KEY_R)) {
				selectedObject.rotateTo(0, true);
				if (shiftsPressed) {
					selectedObject.position = new Vector2D();
				}
			}
		}

		int scrollDelta = 0;
		if (gameInput.getScrollUpJustNow(unprocessedTime)) {
			scrollDelta -= 1;
		}
		if (gameInput.getScrollDownJustNow(unprocessedTime)) {
			scrollDelta += 1;
		}
		toolIndex += scrollDelta;
		while (toolIndex >= tools.length)
			toolIndex -= tools.length;
		while (toolIndex < 0)
			toolIndex += tools.length;

		switch (toolIndex) {
			case 0: // Delete
				//noinspection Duplicates
				if (gameInput.getMouseJustPressed(GLFW_MOUSE_BUTTON_1, unprocessedTime)) {
					if (closestObject != null) {
						gameObjects.remove(closestObject);
						closestObject = null;
					} else if (selectedObject != null) {
						gameObjects.remove(selectedObject);
						selectedObject = null;
					}
				}
				break;
			case 1: // Draw wall
				if (gameInput.getMouseJustPressed(GLFW_MOUSE_BUTTON_1, unprocessedTime)) {
					isDrawing = true;
					selectedDrawingPoint = new Vector2D(cursorPos);
					drawingPoints.add(selectedDrawingPoint);
				}
				break;
		}

		return true;
	}

	private boolean updateInputDrawing(double unprocessedTime) {
		if (gameInput.getKeyPressed(GLFW_KEY_ENTER)) {
			if (drawingPoints.size() >= 3) {
				Vector2D[] vecArray = drawingPoints.toArray(new Vector2D[drawingPoints.size()]);
				Vector2D center = Vector2D.center(vecArray);
				GameWall nWall = new GameWall(center, Vector2D.add(vecArray, Vector2D.inverse(center)));
				gameObjects.add(nWall);
			}

			drawingPoints.clear();
			isDrawing = false;
			closestDrawingPoint = null;
			selectedDrawingPoint = null;

			gameInput.lockKey(GLFW_KEY_ENTER);
			return true;
		}

		closestDrawingPoint = null;
		for (Vector2D point : drawingPoints) {
			if (point.distance(cursorPos) <= cursorSelectionRadius) {
				closestDrawingPoint = point;
				break;
			}
		}

		if (gameInput.getMouseJustPressed(GLFW_MOUSE_BUTTON_1, unprocessedTime)) {
			if (closestDrawingPoint != null && closestDrawingPoint != selectedDrawingPoint) { // select closest object on click
				selectedDrawingPoint = closestDrawingPoint;
			} else if (closestDrawingPoint == null) { //place new point
				selectedDrawingPoint = new Vector2D(cursorPos);
				drawingPoints.add(selectedDrawingPoint);
			}
		} else if (gameInput.getMousePressed(GLFW_MOUSE_BUTTON_1)) {
			if (selectedDrawingPoint != null) {
				selectedDrawingPoint.set(cursorPos);
			}
		} else //noinspection Duplicates
			if (gameInput.getMouseJustPressed(GLFW_MOUSE_BUTTON_2, unprocessedTime)) {
			if (closestDrawingPoint != null) {
				drawingPoints.remove(closestDrawingPoint);
				closestDrawingPoint = null;
			} else if (selectedDrawingPoint != null) {
				drawingPoints.remove(selectedDrawingPoint);
				selectedDrawingPoint = null;
			}
		}

		return true;
	}

	@Override
	protected void updateGame(float elapsedTime, int currentUpdateIndex, int maxUpdateIndex) {

	}

	@Override
	public boolean loadState(String loadPath) {
		JsonObject root = loadStatePartial(loadPath);
		return root != null;
	}

	@Override
	public boolean saveState(String savePath) {
		return saveState(savePath, null);
	}

	@Override
	public void render(Renderer renderer) {
		renderer.setLightCount(1);
		renderer.setLight(0, PlaySceneController.ambientLight);

		super.renderBegin(renderer);
		super.render(renderer);

		if (isDrawing) {
			if (drawingPoints.size() >= 3) {
				Vector2D[] vecArray = drawingPoints.toArray(new Vector2D[drawingPoints.size()]);
				Vertex[] shape = Vertex.shapeFromVector2D(vecArray);
				renderer.drawShape(shape, new Renderer.RenderInfo(null, 1f, 0f, r_drawingShapeColor, false));

				renderer.drawCircle(new Renderer.RenderInfo(Vector2D.center(vecArray), 3f, 0f, Color.RED, false));
			}

			for (Vector2D point : drawingPoints) {
				Color pointColor = r_drawingPointColor;
				if (point == selectedDrawingPoint) {
					pointColor = r_drawingPointSelectedColor;
				} else if (point == closestDrawingPoint) {
					pointColor = r_drawingPointClosestColor;
				}

				renderer.drawCircle(new Renderer.RenderInfo(point, 5f, 0f, pointColor, false));
			}
		} else {
			if (closestObject != null && closestObject != selectedObject) {
				closestObject.renderHitbox(renderer, r_highlightColor);
			}
			if (selectedObject != null) {
				selectedObject.renderHitbox(renderer, r_selectionColor);
			}
		}

		super.renderEnd(renderer);

		Engine.gameEngine.drawDefaultCornerStrings();
		Engine.gameEngine.drawCornerString("Camera position: " + cameraPos, new Color(), 5);
		Engine.gameEngine.drawCornerString("Cursor position: " + cursorPos);
		Engine.gameEngine.drawCornerString("Tool: " + tools[toolIndex]);

		if (selectedObject != null) {
			Engine.gameEngine.drawCornerString("Object position: " + selectedObject.position);
			Engine.gameEngine.drawCornerString(String.format("Object rotation: %.2f", selectedObject.getRotation()));
		}
	}

	@Override
	public void freeze() {
		super.freeze();
	}

	@Override
	public void restore() {
		super.restore();
	}

	@Override
	public void cleanup() {
		saveState("Saves/editor-exitsave.json");

		super.cleanup();
	}
}
