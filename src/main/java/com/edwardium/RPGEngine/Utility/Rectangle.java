package com.edwardium.RPGEngine.Utility;

import com.edwardium.RPGEngine.IO.JsonBuilder;

import javax.json.JsonObject;

/**
 * Class representing a rectangle in 2D space.
 */
public class Rectangle implements GameSerializable {

	/**
	 * @param a Rectangle a.
	 * @param w New width.
	 * @return New rectangle which is a copy of a with width width.
	 */
	public static Rectangle setWidth(Rectangle a, float w) {
		return new Rectangle(a).setWidth(w);
	}

	/**
	 * @param a Rectangle a.
	 * @param h New height.
	 * @return New rectangle which is a copy of a with height height.
	 */
	public static Rectangle setHeight(Rectangle a, float h) {
		return new Rectangle(a).setHeight(h);
	}

	/**
	 * @param a Rectangle a.
	 * @param shift Vector by which to shift.
	 * @return New rectangle that is a copy of a shifted by shift.
	 */
	public static Rectangle shiftBy(Rectangle a, Vector2D shift) {
		return new Rectangle(a).shiftBy(shift);
	}

	/**
	 * @param r Rectangle r.
	 * @param point Vector representing a point.
	 * @param marginX Margin on the x axis.
	 * @param marginY Margin on the y axis.
	 * @return Whether the point lies inside this rectangle and the margins.
	 */
	public static boolean pointCollision(Rectangle r, Vector2D point, float marginX, float marginY) {
		return point.getX() >= r.getTopLeft().getX() - marginX && point.getX() <= r.getBottomRight().getX() + marginX
				&& point.getY() >= r.getTopLeft().getY() - marginY && point.getY() <= r.getBottomRight().getY() + marginY;
	}

	/**
	 * @param r Rectangle r.
	 * @param point Vector representing a point.
	 * @param margin Margin on both axes.
	 * @return Whether the point lies inside this rectangle and the margin.
	 *
	 * Equivaled to calling pointCollision(r, point, margin, margin)
	 * @see Rectangle#pointCollision(Rectangle, Vector2D, float, float)
	 */
	public static boolean pointCollision(Rectangle r, Vector2D point, float margin) {
		return pointCollision(r, point, margin, margin);
	}

	/**
	 * @param r Rectangle r.
	 * @param point Vector representing a point.
	 * @return Whether the point lies inside this rectangle.
	 *
	 * Equivaled to calling pointCollision(r, point, 0)
	 * @see Rectangle#pointCollision(Rectangle, Vector2D, float)
	 */
	public static boolean pointCollision(Rectangle r, Vector2D point) {
		return pointCollision(r, point, 0);
	}

	private Vector2D topLeft;
	private Vector2D bottomRight;

	/**
	 * @param topLeft Top left point of the rectangle.
	 * @param bottomRight Bottom right point of the rectangle.
	 */
	public Rectangle(Vector2D topLeft, Vector2D bottomRight) {
		this.setTopLeft(topLeft);
		this.setBottomRight(bottomRight);
	}

	/**
	 * @param copy Rectangle to copy.
	 *
	 * Copy constructor.
	 */
	public Rectangle(Rectangle copy) {
		this.setTopLeft(new Vector2D(copy.getTopLeft()));
		this.setBottomRight(new Vector2D(copy.getBottomRight()));
	}

	/**
	 * @return Width of this rectangle.
	 */
	public float getWidth() {
		return getBottomRight().getX() - getTopLeft().getX();
	}

	/**
	 * @param w New width.
	 * @return This reference.
	 */
	public Rectangle setWidth(float w) {
		this.getBottomRight().setX(this.getTopLeft().getX() + w);

		return this;
	}

	/**
	 * @return Height of this rectangle.
	 */
	public float getHeight() {
		return getBottomRight().getY() - getTopLeft().getY();
	}

	/**
	 * @param h New height.
	 * @return This reference.
	 */
	public Rectangle setHeight(float h) {
		this.getBottomRight().setY(this.getTopLeft().getY() + h);

		return this;
	}

	/**
	 * @return Calculates and returns a new vector representing the top right corner of this rectangle.
	 */
	public Vector2D getTopRight() {
		return new Vector2D(getTopLeft()).setX(getBottomRight().getX());
	}

	/**
	 * @return Calculates and returns a new vector representing the bottom left corner of this rectangle.
	 */
	public Vector2D getBottomLeft() {
		return new Vector2D(getTopLeft()).setY(getBottomRight().getY());
	}

	/**
	 * @param shift Vector to shift by.
	 * @return This reference.
	 *
	 * Shifts this rectangle by shift.
	 */
	public Rectangle shiftBy(Vector2D shift) {
		this.getTopLeft().add(shift);
		this.getBottomRight().add(shift);

		return this;
	}

	/**
	 * @param s Factor to scale by.
	 * @return This reference.
	 *
	 * Equivalent to calling scale(s, s)
	 * @see Rectangle#scale(float, float)
	 */
	public Rectangle scale(float s) {
		return scale(s, s);
	}

	/**
	 * @param x Factor by which to scale width.
	 * @param y Factor by which to scale height;
	 * @return This reference.
	 */
	public Rectangle scale(float x, float y) {
		setWidth(getWidth() * x);
		setHeight(getHeight() * y);

		return this;
	}

	/**
	 * @return New vector representing the center of this rectangle.
	 */
	public Vector2D center() {
		return getTopLeft().center(getBottomRight());
	}

	/**
	 * @return Arrayf of vectors representing points of this polygon.
	 */
	public Vector2D[] toShape() {
		Vector2D topRight = new Vector2D(getTopLeft()).setX(getBottomRight().getX());
		Vector2D bottomLeft = new Vector2D(getTopLeft()).setY(getBottomRight().getY());

		return new Vector2D[] {getTopLeft(), getTopRight(), getBottomRight(), getBottomLeft()};
	}

	/**
	 * @return JsonObject representing this rectangle.
	 */
	public JsonObject toJSON() {
		return new JsonBuilder().add("topLeft", this.getTopLeft().toJSON()).add("bottomRight", this.getBottomRight().toJSON()).build();
	}

	/**
	 * @param sourceObj Object to construct the new rectangle from.
	 * @return New rectangle constructed from sourceObj.
	 */
	public static Rectangle fromJSON(JsonObject sourceObj) {
		if (sourceObj == null)
			return new Rectangle(new Vector2D(), new Vector2D());

		return new Rectangle(Vector2D.fromJSON(sourceObj.getJsonObject("topLeft")), Vector2D.fromJSON(sourceObj.getJsonObject("bottomRight")));
	}

	/**
	 * @return Vector representing the top left point.
	 */
	public Vector2D getTopLeft() {
		return topLeft;
	}

	/**
	 * @param topLeft New top left point.
	 */
	public void setTopLeft(Vector2D topLeft) {
		this.topLeft = topLeft;
	}

	/**
	 * @return Vector representing the bottom right point.
	 */
	public Vector2D getBottomRight() {
		return bottomRight;
	}

	/**
	 * @param bottomRight New bottom right point.
	 */
	public void setBottomRight(Vector2D bottomRight) {
		this.bottomRight = bottomRight;
	}
}
