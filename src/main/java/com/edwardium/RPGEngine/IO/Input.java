package com.edwardium.RPGEngine.IO;

import com.edwardium.RPGEngine.Vector2D;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class Input {
	private final long window;

	private Vector2D lastMousePos;

	public Input(long window) {
		this.window = window;
		lastMousePos = new Vector2D();

		// setup callbacks
		// window key callback
		glfwSetKeyCallback(window, new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
			// Bind the release of Ctrl+Q to window close
			if (key == GLFW_KEY_Q && ((mods & GLFW_MOD_CONTROL) != 0) && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(window, true);
			}
			}
		});

		// cursor pos callback
		glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double x, double y) {
				lastMousePos.set((float)x, (float)y);
			}
		});
	}

	public int getKeyState(int code) {
		return glfwGetKey(window, code);
	}
	public boolean getKeyPressed(int code) {
		return getKeyState(code) == GLFW_PRESS;
	}

	public int getMouseState(int code) {
		return glfwGetMouseButton(window, code);
	}
	public boolean getMousePressed(int code) {
		return getMouseState(code) == GLFW_PRESS;
	}

	public Vector2D getCursorPos() {
		return new Vector2D(lastMousePos);
	}
}
