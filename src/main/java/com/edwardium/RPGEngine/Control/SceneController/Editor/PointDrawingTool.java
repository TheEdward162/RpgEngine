package com.edwardium.RPGEngine.Control.SceneController.Editor;

import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Renderer.Vertex;
import com.edwardium.RPGEngine.Utility.Vector2D;

import java.util.ArrayList;

public abstract class PointDrawingTool extends Tool {
	protected static final Color R_DRAWING_POINT_COLOR = Color.PINK;
	protected static final Color R_DRAWING_POINT_CLOSEST_COLOR = Color.YELLOW;
	protected static final Color R_DRAWING_POINT_SELECTED_COLOR = Color.CYAN;
	protected static final Color R_DRAWING_SHAPE_COLOR = Color.DARKGREY;

	protected ArrayList<Vector2D> drawingPoints;
	boolean isDrawing;

	protected Vector2D selectedDrawingPoint = null;
	protected Vector2D closestDrawingPoint = null;

	public PointDrawingTool(String name, TextureInfo icon) {
		super(name, icon);

		drawingPoints = new ArrayList<>();
	}

	@Override
	public boolean updateInput(EditorSceneController esc, Input gameInput, double unprocessedTime) {
		if (isDrawing) {
			closestDrawingPoint = null;
			for (Vector2D point : drawingPoints) {
				if (point.distance(esc.cursorPos) <= EditorSceneController.CURSOR_SELECTION_RADIUS) {
					closestDrawingPoint = point;
					break;
				}
			}

			if (gameInput.getMouseJustPressed(EditorSceneController.TRANSFORMS_MOUSE_BUTTON, unprocessedTime)) {
				if (closestDrawingPoint != null && closestDrawingPoint != selectedDrawingPoint) { // select closest object on click
					selectedDrawingPoint = closestDrawingPoint;
				} else if (closestDrawingPoint == null) { //place new point
					selectedDrawingPoint = new Vector2D(esc.cursorPos);
					drawingPoints.add(selectedDrawingPoint);
				}
			} else if (gameInput.getMousePressed(EditorSceneController.TRANSFORMS_MOUSE_BUTTON)) {
				if (selectedDrawingPoint != null) {
					selectedDrawingPoint.set(esc.cursorPos);
				}
			} else if (gameInput.getMouseJustPressed(EditorSceneController.TOOLS_MOUSE_BUTTON, unprocessedTime)) {
				if (closestDrawingPoint != null) {
					drawingPoints.remove(closestDrawingPoint);
					closestDrawingPoint = null;
				} else if (selectedDrawingPoint != null) {
					drawingPoints.remove(selectedDrawingPoint);
					selectedDrawingPoint = null;
				}
			}

			return false;
		} else {
			return true;
		}
	}

	@Override
	public void render(EditorSceneController esc, Renderer renderer) {
		if (drawingPoints.size() >= 3) {
			Vector2D[] vecArray = drawingPoints.toArray(new Vector2D[drawingPoints.size()]);
			Vertex[] shape = Vertex.shapeFromVector2D(vecArray);
			renderer.drawShape(shape, new Renderer.RenderInfo(null, 1f, 0f, R_DRAWING_SHAPE_COLOR, false));

			// center
			renderer.drawCircle(new Renderer.RenderInfo(Vector2D.center(vecArray), 3f, 0f, Color.RED, false));
		}

		for (Vector2D point : drawingPoints) {
			Color pointColor = R_DRAWING_POINT_COLOR;
			if (point == selectedDrawingPoint) {
				pointColor = R_DRAWING_POINT_SELECTED_COLOR;
			} else if (point == closestDrawingPoint) {
				pointColor = R_DRAWING_POINT_CLOSEST_COLOR;
			}

			renderer.drawCircle(new Renderer.RenderInfo(point, 5f, 0f, pointColor, false));
		}

		super.render(esc, renderer);
	}
}
