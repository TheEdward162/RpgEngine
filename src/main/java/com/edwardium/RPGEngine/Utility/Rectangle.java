package com.edwardium.RPGEngine.Utility;

import com.edwardium.RPGEngine.IO.JsonBuilder;

import javax.json.JsonObject;

public class Rectangle implements GameSerializable {

	public static Rectangle setWidth(Rectangle a, float w) {
		return new Rectangle(a).setWidth(w);
	}
	public static Rectangle setHeight(Rectangle a, float h) {
		return new Rectangle(a).setHeight(h);
	}

	public static Rectangle shiftBy(Rectangle a, Vector2D shift) {
		return new Rectangle(a).shiftBy(shift);
	}

	public static boolean pointCollision(Rectangle r, Vector2D point, float marginX, float marginY) {
		return point.getX() >= r.topLeft.getX() - marginX && point.getX() <= r.bottomRight.getX() + marginX
				&& point.getY() >= r.topLeft.getY() - marginY && point.getY() <= r.bottomRight.getY() + marginY;
	}
	public static boolean pointCollision(Rectangle r, Vector2D point, float margin) {
		return pointCollision(r, point, margin, margin);
	}
	public static boolean pointCollision(Rectangle r, Vector2D point) {
		return pointCollision(r, point, 0);
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

	public Vector2D getTopRight() {
		return new Vector2D(topLeft).setX(bottomRight.getX());
	}
	public Vector2D getBottomLeft() {
		return new Vector2D(topLeft).setY(bottomRight.getY());
	}

	public Rectangle shiftBy(Vector2D shift) {
		this.topLeft.add(shift);
		this.bottomRight.add(shift);

		return this;
	}

	public Vector2D center() {
		return topLeft.center(bottomRight);
	}

	public Vector2D[] toShape() {
		Vector2D topRight = new Vector2D(topLeft).setX(bottomRight.getX());
		Vector2D bottomLeft = new Vector2D(topLeft).setY(bottomRight.getY());

		return new Vector2D[] {topLeft, topRight, bottomRight, bottomLeft};
	}

	public JsonObject toJSON() {
		return new JsonBuilder().add("topLeft", this.topLeft.toJSON()).add("bottomRight", this.bottomRight.toJSON()).build();
	}

	public static Rectangle fromJSON(JsonObject sourceObj) {
		if (sourceObj == null)
			return new Rectangle(new Vector2D(), new Vector2D());

		return new Rectangle(Vector2D.fromJSON(sourceObj.getJsonObject("topLeft")), Vector2D.fromJSON(sourceObj.getJsonObject("bottomRight")));
	}
}
