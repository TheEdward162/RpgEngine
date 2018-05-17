package com.edwardium.RPGEngine.IO;

import com.edwardium.RPGEngine.Utility.Vector2D;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Class that handles input from existing window.
 */
public class Input {
	private abstract class ActionState {
		public double actionTime;

		public ActionState() {
			actionTime = 0;
		}

		public ActionState(double actionTime) {
			this.actionTime = actionTime;
		}

		public double timeSince() {
			return System.nanoTime() - actionTime;
		}

		public boolean inTime(double threshold) {
			return timeSince() <= threshold;
		}
	}

	private class KeyState extends ActionState {
		public int key = 0;
		public int scancode = 0;
		public int action = GLFW_RELEASE;
		public int mods = 0;

		public KeyState() {
			super();
		}

		public KeyState(int key) {
			super(0);
			this.key = key;
		}

		public KeyState(KeyState copy) {
			super(0);
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

	private class ScrollState extends ActionState {
		public double offsetX = 0;
		public double offsetY = 0;

		public ScrollState() {
			super();
		}

		public void set(double x, double y, double time) {
			this.offsetX = x;
			this.offsetY = y;
			this.actionTime = time;
		}
	}

	private class MouseState extends ActionState {
		public int key = 0;
		public int action = GLFW_RELEASE;
		public int mods = 0;

		public Vector2D position = null;

		public MouseState() {
			super();
		}

		public MouseState(Vector2D position, double time) {
			super(time);
			this.position = position;
		}

		public void set(int key, int action, int mods, Vector2D position, double time) {
			this.key = key;
			this.action = action;
			this.mods = mods;
			this.position = position;
			this.actionTime = time;
		}
	}

	private class ActionHistory<T extends ActionState> {
		public T lastPress;
		public T lastRelease;

		public ActionHistory(T press, T release) {
			lastPress = press;
			lastRelease = release;
		}
	}

	/**
	 * Data class representing mouse drag event.
	 */
	public class MouseDrag {
		public final Vector2D start;
		public final Vector2D end;

		public MouseDrag(Vector2D start, Vector2D end) {
			this.start = start;
			this.end = end;
		}
	}

	private final long window;

	private Vector2D gameCursorCenter = new Vector2D();

	private HashMap<Integer, ActionHistory<KeyState>> watchedKeys;
	private ScrollState scrollState = new ScrollState();

	private ActionHistory<MouseState> mouse1History;
	private ActionHistory<MouseState> mouse2History;

	private Set<Integer> keyLock;

	/**
	 * @param window Window handle to collect events from.
	 */
	public Input(long window) {
		this.window = window;
		watchedKeys = new HashMap<>();
		keyLock = new TreeSet<>();

		mouse1History = new ActionHistory<>(new MouseState(), new MouseState());
		mouse2History = new ActionHistory<>(new MouseState(), new MouseState());

		// setup callbacks
		// window key callback
		glfwSetKeyCallback(window, new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
			// Bind the release of Ctrl+Q to window close
				if (key == GLFW_KEY_Q && ((mods & GLFW_MOD_CONTROL) != 0) && action == GLFW_RELEASE) {
					glfwSetWindowShouldClose(window, true);
				} else if (watchedKeys.containsKey(key)) {
					if (action == GLFW_RELEASE) {
						checkLock(key, false);
						watchedKeys.get(key).lastRelease.set(key, scancode, action, mods, System.nanoTime());
					} else if (action == GLFW_PRESS) {
						watchedKeys.get(key).lastPress.set(key, scancode, action, mods, System.nanoTime());
					}
				}
			}
		});

		glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				if (button == GLFW_MOUSE_BUTTON_1) {
					if (action == GLFW_RELEASE) {
						checkLock(button, false);
						mouse1History.lastRelease.set(button, action, mods, getCursorPos(), System.nanoTime());
					} else if (action == GLFW_PRESS) {
						mouse1History.lastPress.set(button, action, mods, getCursorPos(), System.nanoTime());
					}
				} else if (button == GLFW_MOUSE_BUTTON_2) {
					if (action == GLFW_RELEASE) {
						checkLock(button, false);
						mouse2History.lastRelease.set(button, action, mods, getCursorPos(), System.nanoTime());
					} else if (action == GLFW_PRESS) {
						mouse2History.lastPress.set(button, action, mods, getCursorPos(), System.nanoTime());
					}
				}
			}
		});

		// replaced in favor of glfwGetCursorPos
		// cursor pos callback
//		glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
//			@Override
//			public void invoke(long window, double x, double y) {
//				//System.err.println(x + "; " + y);
//			}
//		});

		glfwSetScrollCallback(window, new GLFWScrollCallback() {
			@Override
			public void invoke(long window, double offsetX, double offsetY) {
				scrollState.set(offsetX, offsetY, System.nanoTime());
			}
		});
	}

	/**
	 * Starts storing last press and last release for given key.
	 * Watching a key is useful if you need to check the last time the key was pressed, to detect double taps or long presses.
	 *
	 * @param key Key to watch.
	 */
	public void watchKey(int key) {
		if (!watchedKeys.containsKey(key)) {
			watchedKeys.put(key, new ActionHistory<>(new KeyState(key), new KeyState(key)));
		}
	}

	/**
	 * Stops storing last press and last release for given key.
	 *
	 * @param key Key to unwatch.
	 */
	public void unwatchKey(int key) {
		watchedKeys.remove(key);
	}

	private int getKeyState(int code) {
		return glfwGetKey(window, code);
	}
	private int getMouseState(int code) {
		return glfwGetMouseButton(window, code);
	}

	/**
	 * @param code Key code. Doesn't have to be a watched key.
	 * @return Whether the key is pressed right now.
	 */
	public boolean getKeyPressed(int code) {
		return checkLock(code, getKeyState(code) == GLFW_PRESS);
	}

	/**
	 * Locking a key makes functions that return pressed/released status to return false until
	 * the key status really becomes GLFW_RELEASE, at which point the lock releases and these
	 * functions will return the true state until locked again.
	 *
	 * @param code Key code to lock.
	 */
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

	/**
	 * @param code Key code.
	 * @param timeThreshold Time threshold.
	 * @return Whether the key was pressed sometime in the interval [now - timeThreshold; now]. If the key is not watched, returns false.
	 */
	public boolean getWatchedKeyJustPressed(int code, double timeThreshold) {
		ActionHistory<KeyState> history = watchedKeys.getOrDefault(code, null);
		if (history == null || history.lastPress == null)
			return false;

		return checkLock(code, true) && history.lastPress.inTime(timeThreshold);
	}

	/**
	 * @param code Key code.
	 * @param timeThreshold Time threshold.
	 * @return Whether the key was released sometime in the interval [now - timeThreshold; now]. If the key is not watched, returns false.
	 */
	public boolean getWatchedKeyJustReleased(int code, double timeThreshold) {
		ActionHistory<KeyState> history = watchedKeys.getOrDefault(code, null);
		if (history == null || history.lastRelease == null)
			return false;

		return checkLock(code, true) && history.lastRelease.inTime(timeThreshold);
	}

	/**
	 * @param timeThreshold Time threshold.
	 * @return Whether there was an scroll up event sometime in the interval [now - timeThreshold; now].
	 */
	public boolean getScrollUpJustNow(double timeThreshold) {
		return scrollState.offsetY > 0 && scrollState.inTime(timeThreshold);
	}

	/**
	 * @param timeThreshold Time threshold.
	 * @return Whether there was an scroll down event sometime in the interval [now - timeThreshold; now].
	 */
	public boolean getScrollDownJustNow(double timeThreshold) {
		return scrollState.offsetY < 0 && scrollState.inTime(timeThreshold);
	}

	/**
	 * @param code Mouse button code.
	 * @return Whether code mouse button is pressed right now.
	 */
	public boolean getMousePressed(int code) {
		return checkLock(code, getMouseState(code) == GLFW_PRESS);
	}

	/**
	 * @param code Mouse button code.
	 * @param timeThreshold Time threshold.
	 * @return Whether the mouse button 1 was pressed sometime in the interval [now - timeThreshold; now].
	 */
	public boolean getMouseJustPressed(int code, double timeThreshold) {
		ActionHistory<MouseState> history;
		switch (code) {
			case GLFW_MOUSE_BUTTON_1:
				history = mouse1History;
				break;
			case GLFW_MOUSE_BUTTON_2:
				history = mouse2History;
				break;
			default:
				return false;
		}

		return checkLock(code, true) && history.lastPress.inTime(timeThreshold);
	}

	/**
	 * @param code Mouse button code.
	 * @param timeThreshold Time threshold.
	 * @return Whether the mouse button 1 was released sometime in the interval [now - timeThreshold; now].
	 */
	public boolean getMouseJustReleased(int code, double timeThreshold) {
		ActionHistory<MouseState> history;
		switch (code) {
			case GLFW_MOUSE_BUTTON_1:
				history = mouse1History;
				break;
			case GLFW_MOUSE_BUTTON_2:
				history = mouse2History;
				break;
			default:
				return false;
		}

		return checkLock(code, true) && history.lastRelease.inTime(timeThreshold);
	}

	/**
	 * @param code Mouse button code.
	 * @return Nanoseconds since the last time this button has been pressed.
	 */
	public double getMouseTimeSincePress(int code) {
		switch (code) {
			case GLFW_MOUSE_BUTTON_1:
				return mouse1History.lastPress.timeSince();
			case GLFW_MOUSE_BUTTON_2:
				return mouse2History.lastPress.timeSince();
			default:
				return 0;
		}
	}

	/**
	 * @param code Mouse button code. Currently supported only for button 1 and 2.
	 * @param timeThreshold Time threshold for drag end.
	 * @return Mouse drag event that is still ongoing or ended no less than timeThreshold nanoseconds ago.
	 */
	public MouseDrag getMouseDrag(int code, double timeThreshold) {
		ActionHistory<MouseState> history;
		switch (code) {
			case GLFW_MOUSE_BUTTON_1:
				history = mouse1History;
				break;
			case GLFW_MOUSE_BUTTON_2:
				history = mouse2History;
				break;
			default:
				return null;
		}

		if (history.lastPress.actionTime > history.lastRelease.actionTime) {
			// this means that we are still dragging
			return new MouseDrag(history.lastPress.position, getCursorPos());
		} else if (history.lastRelease.inTime(timeThreshold)) {
			// button release happened in threshold
			return new MouseDrag(history.lastPress.position, history.lastRelease.position );
		} else {
			// no drag
			return null;
		}
	}

	private Vector2D getCursorPos() {
		double[] xpos = {0};
		double[] ypos = {0};
		glfwGetCursorPos(window, xpos, ypos);

		return new Vector2D((float)xpos[0], (float)ypos[0]);
	}

	/**
	 * Sets the center point for calculating cursor position in getGameCursorPos.
	 * @see Input#getGameCursorPos()
	 *
	 * @param center Center point,
	 */
	public void setGameCursorCenter(Vector2D center) {
		this.gameCursorCenter = center;
	}

	/**
	 * @return Cursor position relative to gameCursorCenter
	 */
	public Vector2D getGameCursorPos() {
		return getGameCursorPos(getCursorPos());
	}

	/**
	 * @param pos Position to calculate cursor coordinates from.
	 * @return Coordinates of pos relative to gameCursorCenter.
	 */
	public Vector2D getGameCursorPos(Vector2D pos) {
		return Vector2D.subtract(pos, gameCursorCenter);
	}
}
