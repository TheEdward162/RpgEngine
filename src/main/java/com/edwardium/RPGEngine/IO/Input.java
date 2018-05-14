package com.edwardium.RPGEngine.IO;

import com.edwardium.RPGEngine.Utility.Vector2D;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import static org.lwjgl.glfw.GLFW.*;

public class Input {
	public class KeyState {
		public int key = 0;
		public int scancode = 0;
		public int action = GLFW_RELEASE;
		public int mods = 0;

		public double actionTime = 0;

		public KeyState(int key) {
			this.key = key;
		}

		public KeyState(KeyState copy) {
			set(copy);
		}

		public void set(KeyState copy) {
			set(copy.key, copy.scancode, copy.action, copy.mods, copy.actionTime);
		}

		public void set(int key, int scancode, int action, int mods, double actionTime) {
			this.key = key;
			this.scancode = scancode;
			this.action = action;
			this.mods = mods;
			this.actionTime = actionTime;
		}
	}

	public class ScrollState {
		public double offsetX = 0;
		public double offsetY = 0;

		public double actionTime = 0;

		public ScrollState() {

		}

		public void set(double x, double y, double time) {
			this.offsetX = x;
			this.offsetY = y;
			this.actionTime = time;
		}
	}

	private final long window;

	//private Vector2D lastMousePos;
	private Vector2D gameCursorCenter = new Vector2D();

	private HashMap<Integer, KeyState> watchedKeys;
	private ScrollState scrollState = new ScrollState();

	private Set<Integer> keyLock;

	public Input(long window) {
		this.window = window;
		//lastMousePos = new Vector2D();
		watchedKeys = new HashMap<>();
		keyLock = new TreeSet<>();

		// setup callbacks
		// window key callback
		glfwSetKeyCallback(window, new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
			// Bind the release of Ctrl+Q to window close
				if (key == GLFW_KEY_Q && ((mods & GLFW_MOD_CONTROL) != 0) && action == GLFW_RELEASE) {
					glfwSetWindowShouldClose(window, true);
				} else if ((action == GLFW_PRESS || action == GLFW_RELEASE) && watchedKeys.containsKey(key)) {
					watchedKeys.get(key).set(key, scancode, action, mods, System.nanoTime());
				}
			}
		});

		// cursor pos callback
//		glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
//			@Override
//			public void invoke(long window, double x, double y) {
//				lastMousePos.set((float)x, (float)y);
//			}
//		});

		glfwSetScrollCallback(window, new GLFWScrollCallback() {
			@Override
			public void invoke(long window, double offsetX, double offsetY) {
				scrollState.set(offsetX, offsetY, System.nanoTime());
			}
		});
	}

	public void watchKey(int key) {
		if (!watchedKeys.containsKey(key)) {
			watchedKeys.put(key, new KeyState(key));
		}
	}
	public void unwatchKey(int key) {
		watchedKeys.remove(key);
	}

	private int getKeyState(int code) {
		return glfwGetKey(window, code);
	}
	public boolean getKeyPressed(int code) {
		return checkLock(code, getKeyState(code) == GLFW_PRESS);
	}

	private int getMouseState(int code) {
		return glfwGetMouseButton(window, code);
	}
	public boolean getMousePressed(int code) {
		return checkLock(code, getMouseState(code) == GLFW_PRESS);
	}

	public void lockKey(int code) {
		keyLock.add(code);
	}
	private boolean checkLock(int code, boolean actual) {
		if (keyLock.contains(code)) {
			if (!actual)
				keyLock.remove(code);

			return false;
		}

		return actual;
	}

	public KeyState getWatchedKeyState(int code) {
		KeyState state = watchedKeys.getOrDefault(code, null);
		if (state != null)
			state = new KeyState(state);

		return state;
	}
	public boolean getWatchedKeyJustPressed(int code, double timeThreshold) {
		KeyState state = watchedKeys.getOrDefault(code, null);
		if (state == null)
			return false;

		double timeDiff = System.nanoTime() - state.actionTime;
		return state.action == GLFW_PRESS && (timeDiff <= timeThreshold);
	}
	public boolean getWatchedKeyJustReleased(int code, double timeThreshold) {
		KeyState state = watchedKeys.getOrDefault(code, null);
		if (state == null)
			return false;

		double timeDiff = System.nanoTime() - state.actionTime;
		return state.action == GLFW_RELEASE && (timeDiff <= timeThreshold);
	}

	public boolean getScrollUpJustNow(double timeThreshold) {
		double timeDiff = System.nanoTime()- scrollState.actionTime;
		return scrollState.offsetY > 0 && (timeDiff <= timeThreshold);
	}
	public boolean getScrollDownJustNow(double timeThreshold) {
		double timeDiff = System.nanoTime() - scrollState.actionTime;
		return scrollState.offsetY < 0 && (timeDiff <= timeThreshold);
	}

	private Vector2D getCursorPos() {
		double[] xpos = {0};
		double[] ypos = {0};
		glfwGetCursorPos(window, xpos, ypos);

		return new Vector2D((float)xpos[0], (float)ypos[0]);
	}

	public void setGameCursorCenter(Vector2D center) {
		this.gameCursorCenter = center;
	}
	public Vector2D getGameCursorPos() {
		return getCursorPos().subtract(gameCursorCenter);
	}
}
