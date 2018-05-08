package com.edwardium.RPGEngine.Renderer;

import com.edwardium.RPGEngine.Utility.Rectangle;
import com.edwardium.RPGEngine.Utility.Vector2D;

// A Renderer takes care of all the window and graphics context initialization and cleanup.
// Also provides methods to render stuff.
public abstract class Renderer {
	public static class RenderInfo {
		public final Vector2D position;
		public final Vector2D scale;
		public final float rotation;
		public final TextureInfo textureInfo;
		public final boolean useLights;

		public RenderInfo(Vector2D position, Vector2D scale, Float rotation, TextureInfo textureInfo, Boolean useLights) {
			this.position = position != null ? position : new Vector2D();
			this.scale = scale != null ? scale : new Vector2D(1, 1);
			this.rotation = rotation != null ? rotation : 0f;
			this.textureInfo = textureInfo != null ? textureInfo : new TextureInfo("debug");
			this.useLights = useLights != null ? useLights : true;
		}
		public RenderInfo(Vector2D position, Float scale, Float rotation, TextureInfo textureInfo, Boolean useLights) {
			this(position, scale != null ? new Vector2D(scale, scale) : null, rotation, textureInfo, useLights);
		}

		public RenderInfo(Vector2D position, Vector2D scale, Float rotation, Color color, Boolean useLights) {
			this(position, scale, rotation, new TextureInfo("default", color), useLights);
		}
		public RenderInfo(Vector2D position, Float scale, Float rotation, Color color, Boolean useLights) {
			this(position, scale != null ? new Vector2D(scale, scale) : null, rotation, new TextureInfo("default", color), useLights);
		}

		public RenderInfo(Vector2D position) {
			this(position, (Vector2D) null, null, (TextureInfo) null, null);
		}
	}

	public enum StringAlignment { TOPLEFT, CENTER, BOTTOMRIGHT }

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

	public abstract boolean getVSync();
	public abstract void setVSync(boolean value);

	public abstract void beforeLoop();
	public abstract void afterLoop();

	public abstract void pushTransformMatrix();
	public abstract void applyTransformMatrix(Vector2D scale, Float rotation, Vector2D translation);
	public abstract void popTransformMatrix();

	public abstract void setCamera(Vector2D cameraPos);

	public abstract void drawLine(Vector2D destPoint, RenderInfo info);
	public abstract void drawRectangle(RenderInfo info);
	public abstract void drawRectangle(Rectangle rectangle, RenderInfo info);

	public abstract void drawCircle(RenderInfo info);
	public abstract void drawCircle(float minRadius, float maxRadius, float maxAngle, RenderInfo info);

	public abstract void drawString(Font font, String text, RenderInfo info, StringAlignment alignment);
	public abstract void drawString(Font font, String text, RenderInfo info);

	public abstract void drawShape(Vertex[] shape, RenderInfo info);

	public abstract void setLight(int index, Vector2D position, Color color, float power);
	public abstract void setLightCount(int count);

	public abstract boolean shouldClose();

	public abstract void cleanup();
}
