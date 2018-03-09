package com.edwardium.RPGEngine.Renderer;

import com.edwardium.RPGEngine.Rectangle;
import com.edwardium.RPGEngine.Vector2D;

// A Renderer takes care of all the window and graphics context initialization and cleanup.
// Also provides methods to render stuff.
public abstract class Renderer {
	protected String windowTitle;
	protected int windowWidth;
	protected int windowHeight;

	public Font basicFont;

	protected Renderer() {
		this("Window");
	}

	protected Renderer(String title) {
		this(title, 800, 600);
	}

	protected Renderer(String title, int width, int height) {
		this.windowTitle = title;
		this.windowWidth = width;
		this.windowHeight = height;

		init();
	}

	protected abstract void init();
	public abstract long getWindowHandle();
	public abstract Vector2D getWindowSize();

	public abstract void show();
	public abstract void hide();

	public abstract void beforeLoop(Vector2D cameraPosition);
	public abstract void afterLoop();

	public abstract void drawLine(Vector2D from, Vector2D to, float width, float[] color);
	public abstract void drawRectangle(Vector2D center, Vector2D size, float rotationAngle, float[] color, TextureInfo textureInfo);
	public abstract void drawRectangle(Rectangle rectangle, float rotationAngle, float[] color, TextureInfo textureInfo);
	public abstract void drawCircle(float radius, Vector2D center, float[] color, TextureInfo textureInfo);
	public abstract void drawCircle(float minRadius, float maxRadius, float maxAngle, Vector2D center, float[] color, TextureInfo textureInfo);
	public abstract void drawString(Font font, String text, Vector2D position, Vector2D scale, float[] color);

	public abstract boolean shouldClose();

	public abstract void cleanup();
}
