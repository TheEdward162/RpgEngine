package com.edwardium.RPGEngine;

public class Rectangle {

	public static Rectangle shiftBy(Rectangle a, Vector2D shift) {
		return new Rectangle(a).shiftBy(shift);
	}

	public Vector2D topLeft;
	public Vector2D bottomRight;

	public Rectangle(Vector2D topLeft, Vector2D bottomRight) {
		this.topLeft = topLeft;
		this.bottomRight = bottomRight;
	}

	public Rectangle(Rectangle copy) {
		this.topLeft = new Vector2D(copy.topLeft);
		this.bottomRight = new Vector2D(copy.bottomRight);
	}

	public Rectangle shiftBy(Vector2D shift) {
		this.topLeft.add(shift);
		this.bottomRight.add(shift);

		return this;
	}
}
