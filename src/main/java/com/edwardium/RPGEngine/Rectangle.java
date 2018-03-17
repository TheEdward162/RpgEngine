package com.edwardium.RPGEngine;

public class Rectangle {

	public static Rectangle setWidth(Rectangle a, float w) {
		return new Rectangle(a).setWidth(w);
	}
	public static Rectangle setHeight(Rectangle a, float h) {
		return new Rectangle(a).setHeight(h);
	}

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

	public float getWidth() {
		return bottomRight.getX() - topLeft.getX();
	}
	public Rectangle setWidth(float w) {
		this.bottomRight.setX(this.topLeft.getX() + w);

		return this;
	}

	public float getHeight() {
		return bottomRight.getY() - topLeft.getY();
	}
	public Rectangle setHeight(float h) {
		this.bottomRight.setY(this.topLeft.getY() + h);

		return this;
	}

	public Rectangle shiftBy(Vector2D shift) {
		this.topLeft.add(shift);
		this.bottomRight.add(shift);

		return this;
	}

	public Vector2D center() {
		return topLeft.center(bottomRight);
	}
}
