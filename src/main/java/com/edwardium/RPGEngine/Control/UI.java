package com.edwardium.RPGEngine.Control;

import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Utility.Vector2D;

/**
 * Static class for common UI functions
 */
public class UI {
	public enum Corner {
		TOPLEFT(0, -1, -1, Renderer.StringAlignment.TOPLEFT),
		TOPRIGHT(1, 1, -1, Renderer.StringAlignment.TOPRIGHT),
		BOTTOMLEFT(2, -1, 1, Renderer.StringAlignment.BOTTOMLEFT),
		BOTTOMRIGHT(3, 1, 1, Renderer.StringAlignment.BOTTOMRIGHT);

		public final int index;
		public final float xMult;
		public final float yMult;
		public final Renderer.StringAlignment stringAlignment;

		Corner(int index, float xViewportMult, float yViewportMult, Renderer.StringAlignment stringAlignment) {
			this.index = index;
			this.xMult = xViewportMult;
			this.yMult = yViewportMult;
			this.stringAlignment = stringAlignment;
		}
	}

	private static Vector2D[] cornerStringNextPos = new Vector2D[] {
		new Vector2D(), new Vector2D(), new Vector2D(), new Vector2D()
	};

	private UI() {}

	public static void resetCorners(Vector2D viewportSize) {
		Corner[] vals = Corner.values();
		for (Corner val : vals) {
			cornerStringNextPos[val.index] = Vector2D.scale(viewportSize, 0.5f * val.xMult, 0.5f * val.yMult)
					.add(-5 * val.xMult, -5 * val.yMult);
		}
	}

	/**
	 * Draws string in a corner of the screen, each consecutive call to this function in the same
	 * render frame draws the string in the next line in the respective corner.
	 *
	 * @param renderer Renderer to use.
	 * @param alignment Corner alignment.
	 * @param text Text to draw.
	 * @param color Color of the text.
	 * @param margin Margin between the last corner line and this one.
	 */
	public static void drawCornerString(Renderer renderer, Corner alignment, String text, Color color, float margin) {
		int signMult = (int)(-Math.signum(alignment.yMult));

		Vector2D posVector = cornerStringNextPos[alignment.index];
		posVector.add(0f, signMult * margin);

		renderer.drawString(renderer.basicFont, text, new Renderer.RenderInfo(posVector, 1f, 0f, color, false), alignment.stringAlignment);
		posVector.add(0f, signMult * 15f);
	}

	/**
	 * Equivalent to calling {@code drawCornerString(renderer, alignment, text, color, 0)}
	 * @see UI#drawCornerString(Renderer, Corner, String, Color, float)
	 *
	 * @param renderer Renderer to use.
	 * @param alignment Corner alignment.
	 * @param text Text to draw.
	 * @param color Color of the text.
	 */
	public static void drawCornerString(Renderer renderer, Corner alignment, String text, Color color) {
		drawCornerString(renderer, alignment, text, color, 0);
	}

	/**
	 * Equivalent to calling {@code drawCornerString(renderer, alignment, text, Color.WHITE, 0)}
	 * @see UI#drawCornerString(Renderer, Corner, String, Color)
	 *
	 * @param renderer Renderer to use.
	 * @param alignment Corner alignment.
	 * @param text Text to draw.
	 */
	public static void drawCornerString(Renderer renderer, Corner alignment, String text) {
		drawCornerString(renderer, alignment, text, Color.WHITE, 0);
	}
}
