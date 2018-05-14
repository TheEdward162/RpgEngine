package com.edwardium.RPGEngine.Renderer;

import com.edwardium.RPGEngine.Utility.Vector2D;

public class Light {
	public static final float DEFAULT_CUTOFF_MULT = 10f;

	public Vector2D position;
	public final Color color;

	public final float power;
	public final float cutoff;

	public Light(Vector2D position, Color color, Float power) {
		this(position, color, power, null);
	}
	public Light(Vector2D position, Color color, Float power, Float cutoff) {
		this.position = position == null ? new Vector2D() : position;
		this.color = color == null ? new Color() : color;
		this.power = power == null ? 0f : power;
		this.cutoff = cutoff == null ? 0f : cutoff;
	}
}
