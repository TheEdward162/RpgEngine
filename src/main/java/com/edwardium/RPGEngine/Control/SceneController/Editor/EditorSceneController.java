package com.edwardium.RPGEngine.Control.SceneController.Editor;

import com.edwardium.RPGEngine.Control.Engine;
import com.edwardium.RPGEngine.Control.SceneController.GameSceneController;
import com.edwardium.RPGEngine.Control.UI;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Light;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Utility.Rectangle;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class EditorSceneController extends GameSceneController {
	// input abstraction
	static class TransformsInput {
		public static final int MOUSE_CLICK = 1; // mouse was clicked in this frame
		public static final int MOUSE_DRAG = 2; // mouse button is held and it has been some time since it was pressed

		public static final int POSITON_ABSOLUTE = 4;
		public static final int ROTATION_ABSOLUTE = 8;
		public static final int SCALE_ABSOLUTE = 16;

		public final Vector2D positionVector;
		public final float rotation;
		public final Vector2D scaleVector;

		public final int flags;

		public TransformsInput(Vector2D pos, float rot, Vector2D scale, int flags) {
			this.positionVector = pos;
			this.rotation = rot;
			this.scaleVector = scale;
			this.flags = flags;
		}

		public boolean isMouseClick() {
			return (flags & MOUSE_CLICK) != 0;
		}

		public boolean isMouseDrag() {
			return (flags & MOUSE_DRAG) != 0;
		}

		public boolean isPositionAbsolute() {
			return (flags & POSITON_ABSOLUTE) != 0;
		}

		public boolean isRotationAbsolute() {
			return (flags & ROTATION_ABSOLUTE) != 0;
		}

		public boolean isScaleAbsolute() {
			return (flags & SCALE_ABSOLUTE) != 0;
		}
	}

	// Input transforms base values
	static final int TRANSFORMS_MOUSE_BUTTON = GLFW_MOUSE_BUTTON_1;

	private static final float TRANSFORMS_SHIFT_DELTA = 10f;
	private static final float TRANSFORMS_SHIFT_MULT = 5f;

	private static final float TRANSFORMS_ROTATION_DELTA = 0.005f;
	private static final float TRANSFORMS_ROTATION_MULT = 5f;

	private static final float TRANSFORMS_SCALE_DELTA = 0.005f;
	private static final float TRANSFORMS_SCALE_MULT = 5f;

	// Colors
	static final Color R_SELECTION_COLOR = Color.CYAN;

	// other constants
	static final Float CURSOR_SELECTION_RADIUS = 25f;
	static final double DRAG_MINIMUM_TIME = 0.25 * 1e+9;

	// ambient light
	private static final Light ambientLight = new Light(new Vector2D(), Color.GREY, -1f, 0f);

	// tools
	static final int TOOLS_MOUSE_BUTTON = GLFW_MOUSE_BUTTON_2;
	private static final Tool[] TOOLS = new Tool[] {
		new MultiTool(),
		new DeleteTool(),
		new SpawnerTool(),
		new WallDrawingTool(),
	};
	private int toolIndex = 0;

	MultiTool mainTransformTool = (MultiTool) TOOLS[0];

	public EditorSceneController(Input gameInput) {
		super(gameInput);

		if (!loadState("Saves/editor-exitsave.json")) {
			player = new GameCharacter(new Vector2D(), "Player", 10);
			player.factionFlag = GameCharacter.CharacterFaction.addFaction(player.factionFlag, GameCharacter.CharacterFaction.PLAYER);

			gameObjects.add(new GameWall(new Vector2D(), new Rectangle(new Vector2D(-10f, -10f), new Vector2D(10f, 10f))));
		}

		gameObjects.add(player);

		gameInput.watchKey(GLFW_KEY_Q);
		gameInput.watchKey(GLFW_KEY_E);
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

		// tool selection
		int scrollDelta = 0;
		if (gameInput.getScrollUpJustNow(unprocessedTime)) {
			scrollDelta -= 1;
		}
		if (gameInput.getScrollDownJustNow(unprocessedTime)) {
			scrollDelta += 1;
		}
		toolIndex += scrollDelta;
		while (toolIndex >= TOOLS.length)
			toolIndex -= TOOLS.length;
		while (toolIndex < 0)
			toolIndex += TOOLS.length;

		// update main selection tool
		if (TOOLS[toolIndex].updateInput(this, gameInput, unprocessedTime)) {
			if (gameInput.getKeyPressed(GLFW_KEY_ENTER)) {
				cameraPos.set(0, 0);
			}
		}

		return true;
	}

	@Override
	protected void updateGame(float elapsedTime, int currentUpdateIndex, int maxUpdateIndex) {}

	TransformsInput updateInputObjectTransforms(double unprocessedTime, int mouseButton, double dragMinimumTime) {
		float frameTime = (float)(unprocessedTime * Engine.NANO_TIME_MULT);
		boolean shiftsPressed = gameInput.getKeyPressed(GLFW_KEY_LEFT_SHIFT) || gameInput.getKeyPressed(GLFW_KEY_RIGHT_SHIFT);

		int flags = 0;

		if (gameInput.getMouseJustPressed(mouseButton, unprocessedTime)) {
			flags = TransformsInput.MOUSE_CLICK;
		} else if (gameInput.getMousePressed(mouseButton) && gameInput.getMouseTimeSincePress(mouseButton) >= dragMinimumTime) {
			// This means that we are dragging, basically
			flags = TransformsInput.MOUSE_DRAG;
		}

		// selected object shift
		float shiftX = 0;
		float shiftY = 0;
		if (gameInput.getKeyPressed(GLFW_KEY_UP)) {
			shiftY -= TRANSFORMS_SHIFT_DELTA;
		}
		if (gameInput.getKeyPressed(GLFW_KEY_DOWN)) {
			shiftY += TRANSFORMS_SHIFT_DELTA;
		}
		if (gameInput.getKeyPressed(GLFW_KEY_LEFT)) {
			shiftX -= TRANSFORMS_SHIFT_DELTA;
		}
		if (gameInput.getKeyPressed(GLFW_KEY_RIGHT)) {
			shiftX += TRANSFORMS_SHIFT_DELTA;
		}
		Vector2D shiftVector = new Vector2D(shiftX, shiftY).scale(frameTime);
		if (shiftsPressed) {
			shiftVector.scale(TRANSFORMS_SHIFT_MULT);
		}

		float rotation = 0;
		if (gameInput.getKeyPressed(GLFW_KEY_KP_7)) {
			rotation -= TRANSFORMS_ROTATION_DELTA;
		}
		if (gameInput.getKeyPressed(GLFW_KEY_KP_9)) {
			rotation += TRANSFORMS_ROTATION_DELTA;
		}
		if (shiftsPressed) {
			rotation *= TRANSFORMS_ROTATION_MULT;
		}

		float scaleX = 0;
		float scaleY = 0;
		if (gameInput.getKeyPressed(GLFW_KEY_KP_ADD)) {
			scaleY += TRANSFORMS_SCALE_DELTA;
		}
		if (gameInput.getKeyPressed(GLFW_KEY_KP_SUBTRACT)) {
			scaleY -= TRANSFORMS_SCALE_DELTA;
		}
		if (gameInput.getKeyPressed(GLFW_KEY_KP_MULTIPLY)) {
			scaleX += TRANSFORMS_SCALE_DELTA;
		}
		if (gameInput.getKeyPressed(GLFW_KEY_KP_DIVIDE)) {
			scaleX -= TRANSFORMS_SCALE_DELTA;
		}
		Vector2D scaleVector = new Vector2D(scaleX, scaleY);
		if (shiftsPressed) {
			scaleVector.scale(TRANSFORMS_SCALE_MULT);
		}

		// selected object reset
		if (gameInput.getKeyPressed(GLFW_KEY_R)) {
			rotation = 0;
			scaleVector.set(1, 1);
			flags |= TransformsInput.ROTATION_ABSOLUTE | TransformsInput.SCALE_ABSOLUTE;

			if (shiftsPressed) {
				scaleVector.set(0, 0);
				flags |= TransformsInput.POSITON_ABSOLUTE;
			}
		}

		return new TransformsInput(shiftVector, rotation, scaleVector, flags);
	}

	// because tools need access to this
	ArrayList<GameObject> getGameObjects() {
		return gameObjects;
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
		renderer.setLight(0, ambientLight);

		super.renderBegin(renderer);
		super.render(renderer);

		TOOLS[toolIndex].render(this, renderer);

		super.renderEnd(renderer);

		Engine.gameEngine.drawDefaultCornerStrings();
		UI.drawCornerString(renderer, UI.Corner.BOTTOMLEFT, "Camera position: " + cameraPos, Color.WHITE, 5);
		UI.drawCornerString(renderer, UI.Corner.BOTTOMLEFT, "Cursor position: " + cursorPos);
		UI.drawCornerString(renderer, UI.Corner.TOPLEFT, "Tool: " + TOOLS[toolIndex].name, Color.YELLOW);

		TOOLS[toolIndex].renderUI(this, renderer);
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

		gameInput.unwatchKey(GLFW_KEY_Q);
		gameInput.unwatchKey(GLFW_KEY_E);

		super.cleanup();
	}
}
