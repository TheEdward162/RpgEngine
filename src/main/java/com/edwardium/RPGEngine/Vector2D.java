package com.edwardium.RPGEngine;

import java.util.Objects;

public class Vector2D {
	private float posX;
	private float posY;

	public static Vector2D fromAM(float angle, float magnitude) {
		return new Vector2D().setAngle(angle).setMagnitude(magnitude);
	}

	public static Vector2D rotatedBy(Vector2D a, float angle) {
		return new Vector2D(a).rotateBy(angle);
	}

	// static methods for arithmetic
	public static Vector2D inverse(Vector2D a) {
		return new Vector2D(a).inverse();
	}

	public static Vector2D normalize(Vector2D a) {
		return new Vector2D(a).normalize();
	}

	public static Vector2D limit(Vector2D a, float limit) {
		return new Vector2D(a).limit(limit);
	}

	public static Vector2D add(Vector2D a, Vector2D b) {
		return new Vector2D(a).add(b);
	}
	public static Vector2D[] add(Vector2D shift, Vector2D[] array) {
		Vector2D[] newArray = new Vector2D[array.length];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = add(array[i], shift);
		}

		return newArray;
	}

	public static Vector2D subtract(Vector2D a, Vector2D b) {
		return new Vector2D(a).subtract(b);
	}

	public static Vector2D multiply(Vector2D a, float m) {
		return new Vector2D(a).multiply(m);
	}

	public static Vector2D divide(Vector2D a, float d) {
		return new Vector2D(a).divide(d);
	}

	public static Vector2D scale(Vector2D a, float x, float y) { return new Vector2D(a).scale(x, y); }
	public static Vector2D scale(Vector2D a, Vector2D scaleVector) { return new Vector2D(a).scale(scaleVector); }

	public static float distance(Vector2D a, Vector2D b) { return a.distance(b); }

	public static float dot(Vector2D a, Vector2D b) {
		return a.dot(b);
	}

	public static float angleBetween(Vector2D a, Vector2D b) { return a.angleBetween(b); }

	// Constructors
	public Vector2D() {
		this(1, 0);
	}

	public Vector2D(float x, float y) {
		posX = x;
		posY = y;
	}

	public Vector2D(Vector2D copy) {
		this.posX = copy.posX;
		this.posY = copy.posY;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vector2D vector2D = (Vector2D) o;
		return Float.compare(vector2D.posX, posX) == 0 &&
				Float.compare(vector2D.posY, posY) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(posX, posY);
	}

	public float getX() {
		return posX;
	}

	public Vector2D setX(float x) {
		this.posX = x;

		return this;
	}

	public float getY() {
		return posY;
	}

	public Vector2D setY(float y) {
		this.posY = y;

		return this;
	}

	public Vector2D set(float x, float y) {
		this.setX(x);
		this.setY(y);

		return this;
	}

	public float getAngle() {
		return (float)Math.atan2(posY, posX);
	}

	public Vector2D setAngle(float angle) {
		float newX = (float)(Math.cos(angle) * getMagnitude());
		float newY = (float)(Math.sin(angle) * getMagnitude());

		this.posX = newX;
		this.posY = newY;

		return this;
	}

	public Vector2D rotateBy(float angle) {
		return this.setAngle(this.getAngle() + angle);
	}

	public float getMagnitude() {
		return (float)Math.sqrt(posX * posX + posY * posY);
	}

	public Vector2D setMagnitude(float magnitude) {
		float newX = (float)(Math.cos(getAngle()) * magnitude);
		float newY = (float)(Math.sin(getAngle()) * magnitude);

		this.posX = newX;
		this.posY = newY;

		return this;
	}

	public Vector2D getNormal() {
		return new Vector2D(this.posY, -this.posX);
	}

	// Unary arithmetics
	public Vector2D inverse() {
		return this.multiply(-1);
	}

	public Vector2D normalize() {
		return this.setMagnitude(1);
	}

	public Vector2D limit(float limit) {
		if (this.getMagnitude() > limit) {
			this.setMagnitude(limit);
		}

		return this;
	}

	// Binary arithmetics
	public Vector2D add(Vector2D other) {
		this.posX += other.posX;
		this.posY += other.posY;

		return this;
	}
	public void addTo(Vector2D[] array) {
		for (Vector2D v : array) {
			v.add(this);
		}
	}

	public Vector2D subtract(Vector2D other) {
		this.posX -= other.posX;
		this.posY -= other.posY;

		return this;
	}

	// Scalar
	public Vector2D multiply(float m) {
		this.posX *= m;
		this.posY *= m;

		return this;
	}

	public Vector2D divide(float d) {
		return this.multiply(1 / d);
	}

	public Vector2D scale(float x, float y) {
		this.posX *= x;
		this.posY *= y;

		return this;
	}
	public Vector2D scale(Vector2D scaleVector) {
		return scale(scaleVector.getX(), scaleVector.getY());
	}

	public float distance(Vector2D other) {
		return Vector2D.subtract(this, other).getMagnitude();
	}

	public float dot(Vector2D other) {
		return this.posX * other.posX + this.posY * other.posY;
	}

	public float angleBetween(Vector2D other) {
		// How this works is
		// Dot product can be written as a . b = |a| * |b| * cos t, where t is the angle between a and b
		// Signed magnitude of a cross product in 3D is equal to |a| * |b| * sin t
		// We pretend that the z coord of both vectors is 0, which gives us |a x b| = ax * by - ay * bx
		// So (a x b) / (a . b) = (|a||b|sin t) / (|a||b|cos t) = sin t / cos t = tan t = (ax * by - ay * bx) / (ax * bx + ay * by)

		float dot = this.dot(other);

		float cross = posX * other.posY - posY * other.posX;
		// this is also equal to the determinant of a 2 * 2 matrix with vectors a and b as columns

		return (float)Math.atan2(-cross, dot);
	}


	@Override
	public String toString() {
		return getX() + "; " + getY();
	}
}
