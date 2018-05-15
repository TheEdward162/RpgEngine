package com.edwardium.RPGEngine.Utility;

import com.edwardium.RPGEngine.IO.JsonBuilder;

import javax.json.JsonObject;
import java.util.Objects;

/**
 * Class representing a coordinate in 2D space.
 */
public class Vector2D implements GameSerializable {
	private float posX;
	private float posY;

	/**
	 * @param angle Angle.
	 * @param magnitude Magnitude.
	 * @return New vector with angle and magnitude.
	 */
	public static Vector2D fromAM(float angle, float magnitude) {
		return new Vector2D(1, 0).setAngle(angle).setMagnitude(magnitude);
	}

	/**
	 * @param a Vector to rotate.
	 * @param angle Angle.
	 * @return New vector that is a rotation of a by angle.
	 */
	public static Vector2D rotatedBy(Vector2D a, float angle) {
		return new Vector2D(a).rotateBy(angle);
	}

	/**
	 * @param array Vectors to rotate
	 * @param angle Angle.
	 * @return Array of new vectors that are rotations of vectors in array.
	 */
	public static Vector2D[] rotatedBy(Vector2D[] array, float angle) {
		Vector2D[] newArray = new Vector2D[array.length];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = rotatedBy(array[i], angle);
		}

		return newArray;
	}

	/**
	 * @param a Vector to inverse.
	 * @return New vector that is an inversion of a.
	 */
	// static methods for arithmetic
	public static Vector2D inverse(Vector2D a) {
		return new Vector2D(a).inverse();
	}

	/**
	 * @param a Vector to absolutize.
	 * @return New vectors that is the absolutization fo a.
	 */
	public static Vector2D absolutize(Vector2D a) {
		return new Vector2D(a).absolutize();
	}

	/**
	 * @param a Vector to add.
	 * @param b Vector to add.
	 * @return New vector that is an addition of a and b.
	 */
	public static Vector2D add(Vector2D a, Vector2D b) {
		return new Vector2D(a).add(b);
	}

	/**
	 * @param array Vectors to add to.
	 * @param shift Vector to add.
	 * @return Array of new vectors that are translations of vectors in array by shift.
	 */
	public static Vector2D[] add(Vector2D[] array, Vector2D shift) {
		Vector2D[] newArray = new Vector2D[array.length];
		for (int i = 0; i < array.length; i++) {
			newArray[i] = add(array[i], shift);
		}

		return newArray;
	}

	/**
	 * @param a Vectors to subtract from.
	 * @param b Vector to subtract.
	 * @return New vector that is a subtraction of b from a.
	 */
	public static Vector2D subtract(Vector2D a, Vector2D b) {
		return new Vector2D(a).subtract(b);
	}

	/**
	 * @param a Vector to divide.
	 * @param d Factor.
	 * @return New vector that is a copy of a divided by d.
	 */
	public static Vector2D divide(Vector2D a, float d) {
		return new Vector2D(a).divide(d);
	}

	/**
	 * @param a Vector to limit.
	 * @param limit Limit.
	 * @return New vector that is a copy of a limited by limit.
	 */
	public static Vector2D limit(Vector2D a, float limit) {
		return new Vector2D(a).limit(limit);
	}

	/**
	 * @param a Vector to normalize.
	 * @return New vector that is a normalization of a.
	 */
	public static Vector2D normalize(Vector2D a) {
		return new Vector2D(a).normalize();
	}

	/**
	 * @param a Vector to scale.
	 * @param s Factor by which to scale.
	 * @return New vector that is a copy of a scaled by s.
	 *
	 * @see Vector2D#scale(float)
	 */
	public static Vector2D scale(Vector2D a, float s) { return new Vector2D(a).scale(s); }

	/**
	 * @param a Vector to scale.
	 * @param x Factor by which to scale x coordinate.
	 * @param y Factor by which to scale y coordinate.
	 * @return New vector that is a copy of a but scaled by x and y.
	 *
	 * @see Vector2D#scale(float, float)
	 */
	public static Vector2D scale(Vector2D a, float x, float y) { return new Vector2D(a).scale(x, y); }

	/**
	 * @param a Vector to scale.
	 * @param scaleVector Vector by which's coordinates to scale x coordinate.
	 * @return New vector that is a copy of a but scaled by scaleVector.getX() and scaleVector.getY().
	 *
	 * @see Vector2D#scale(Vector2D)
	 */
	public static Vector2D scale(Vector2D a, Vector2D scaleVector) { return new Vector2D(a).scale(scaleVector); }

	/**
	 * @param a Vector a.
	 * @param b Vector b.
	 * @return Distance from vector a to vector b.
	 *
	 * Equivalent to calling a.distance(b)
	 * @see Vector2D#distance(Vector2D)
	 */
	public static float distance(Vector2D a, Vector2D b) { return a.distance(b); }

	/**
	 * @param a Vector a.
	 * @param b Vector b.
	 * @return New vector that points to the center between a and b.
	 *
	 * Equivalent to calling a.center(b)
	 * @see Vector2D#center(Vector2D)
	 */
	public static Vector2D center(Vector2D a, Vector2D b) { return a.center(b); }

	/**
	 * @param a Vector a.
	 * @param b Vector b.
	 * @return The dot product of a and b.
	 *
	 * Equivalent to calling a.dot(b)
	 * @see Vector2D#dot(Vector2D)
	 */
	public static float dot(Vector2D a, Vector2D b) {
		return a.dot(b);
	}

	/**
	 * @param a Vector a.
	 * @param b Vector b.
	 * @return Angle (in radians) between and and b.
	 *
	 * Equivalent to calling a.angleBetween(b)
	 * @see Vector2D#angleBetween(Vector2D)
	 */
	public static float angleBetween(Vector2D a, Vector2D b) { return a.angleBetween(b); }

	/**
	 * @param a Vector a.
	 * @param axis Axis vector.
	 * @return New vector that is a projection of a onto axis.
	 *
	 * Equivalent to calling a.projection(axis)
	 * @see Vector2D#projection(Vector2D)
	 */
	public static Vector2D projection(Vector2D a, Vector2D axis) {
		return a.projection(axis);
	}

	/**
	 * @param a Vector a.
	 * @param axis Axis vector.
	 * @return New vector that is a rejection of a from axis.
	 *
	 * Equivalent to calling a.rejection(axis)
	 * @see Vector2D#rejection(Vector2D)
	 */
	public static Vector2D rejection(Vector2D a, Vector2D axis) {
		return a.rejection(axis);
	}

	/**
	 * Empty constructor. Same as Vector2D(0, 0);
	 */
	public Vector2D() {
		this(0, 0);
	}

	/**
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 */
	public Vector2D(float x, float y) {
		posX = x;
		posY = y;
	}


	/**
	 * Copy constructor
	 * @param copy Vector2D object to copy
	 */
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

	/**
	 * @return The x coordinate.
	 */
	public float getX() {
		return posX;
	}

	/**
	 * @param x The x coordinate to set.
	 * @return This reference.
	 */
	public Vector2D setX(float x) {
		this.posX = x;

		return this;
	}

	/**
	 * @return The y coordinate.
	 */
	public float getY() {
		return posY;
	}

	/**
	 * @param y The y coordinate to set.
	 * @return This reference.
	 */
	public Vector2D setY(float y) {
		this.posY = y;

		return this;
	}

	/**
	 * Sets both x and y coordinate.
	 * @param x The x coordinate to set.
	 * @param y The y coordinate to set.
	 * @return This reference.
	 */
	public Vector2D set(float x, float y) {
		this.setX(x);
		this.setY(y);

		return this;
	}

	/**
	 * @return Atan y / x.
	 */
	public float getAngle() {
		return (float)Math.atan2(posY, posX);
	}

	/**
	 * @param angle Angle to be set (in radians).
	 * @return This reference.
	 */
	public Vector2D setAngle(float angle) {
		float newX = (float)(Math.cos(angle) * getMagnitude());
		float newY = (float)(Math.sin(angle) * getMagnitude());

		this.posX = newX;
		this.posY = newY;

		return this;
	}

	/**
	 * @param angle Angle to add to the current angle.
	 * @return This reference.
	 */
	public Vector2D rotateBy(float angle) {
		return this.setAngle(this.getAngle() + angle);
	}

	/**
	 * @return sqrt(x^2 + y^2).
	 */
	public float getMagnitude() {
		return (float)Math.sqrt(posX * posX + posY * posY);
	}

	/**
	 * @param magnitude The magnitude to set.
	 * @return This reference.
	 */
	public Vector2D setMagnitude(float magnitude) {
		float newX = (float)(Math.cos(getAngle()) * magnitude);
		float newY = (float)(Math.sin(getAngle()) * magnitude);

		this.posX = newX;
		this.posY = newY;

		return this;
	}

	/**
	 * @return New Vector2D orthogonal to this vector.
	 */
	@SuppressWarnings("SuspiciousNameCombination")
	public Vector2D getNormal() {
		return new Vector2D(this.posY, -this.posX);
	}

	/**
	 * Multiplies this by -1.
	 * @return This reference.
	 */
	// Unary arithmetics
	public Vector2D inverse() {
		return this.scale(-1);
	}

	/**
	 * @return This reference.
	 *
	 * Sets x and y coordinates to absolute values.
	 */
	public Vector2D absolutize() {
		this.posX = Math.abs(this.posX);
		this.posY = Math.abs(this.posY);

		return this;
	}

	/**
	 * @param other Vector2D to add to this.
	 * @return This reference.
	 */
	// Binary arithmetics
	public Vector2D add(Vector2D other) {
		this.posX += other.posX;
		this.posY += other.posY;

		return this;
	}

	/**
	 * @param other Vector2D to subtract from this.
	 * @return This reference.
	 */
	public Vector2D subtract(Vector2D other) {
		this.posX -= other.posX;
		this.posY -= other.posY;

		return this;
	}

	/**
	 * @param d The factor to divide by. Must not be zero.
	 * @return This reference.
	 *
	 * Same as calling scale(1 / d)
	 * @see Vector2D#scale(float) scale
	 */
	public Vector2D divide(float d) {
		return this.scale(1 / d);
	}

	/**
	 * @param limit Maximum magnitude to allow.
	 * @return This reference.
	 */
	public Vector2D limit(float limit) {
		if (this.getMagnitude() > limit) {
			this.setMagnitude(limit);
		}

		return this;
	}

	/**
	 * @return This reference.
	 *
	 * Same as calling limit(1)
	 * @see Vector2D#limit(float)
	 */
	public Vector2D normalize() {
		return this.setMagnitude(1);
	}

	/**
	 * @param s Factor to scale by. Same as calling scale(s, s)
	 * @return This reference.
	 *
	 * @see Vector2D#scale(float, float)
	 */
	public Vector2D scale(float s) { return scale(s, s); }

	/**
	 * @param x The factor by which to scale x coordinate.
	 * @param y The factor by which to scale y coordinate.
	 * @return This reference.
	 */
	public Vector2D scale(float x, float y) {
		this.posX *= x;
		this.posY *= y;

		return this;
	}

	/**
	 * @param scaleVector The vector by which's coordinates to scale this vector.
	 * @return This reference.
	 *
	 * Same as calling scale(scaleVector.getX(), scaleVector.getY())
	 * @see Vector2D#scale(float, float)
	 */
	public Vector2D scale(Vector2D scaleVector) {
		return scale(scaleVector.getX(), scaleVector.getY());
	}

	/**
	 * @param other Vector to which to measure distance.
	 * @return Distance to other.
	 *
	 * The distance is calculated by constructing a new vector that is a subtraction of other from this and then computing magnitude.
	 */
	public float distance(Vector2D other) {
		return Vector2D.subtract(this, other).getMagnitude();
	}

	/**
	 * @param other Other vector.
	 * @return New vector that points to the center between this and other.
	 */
	public Vector2D center(Vector2D other) {
		return new Vector2D(this).add(other).divide(2);
	}

	/**
	 * @param other Other vector.
	 * @return Dot product of this and other.
	 */
	public float dot(Vector2D other) {
		return this.posX * other.posX + this.posY * other.posY;
	}

	/**
	 * @param other Other vector.
	 * @return Signed angle (in radians) between this and other.
	 */
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

	/**
	 * @param axis Vector to project onto.
	 * @return New vector that is a projection of this onto axis.
	 */
	public Vector2D projection(Vector2D axis) {
		float projectionFactor = dot(axis) / axis.getMagnitude();

		return new Vector2D(axis).setMagnitude(projectionFactor);
	}

	/**
	 * @param axis Vector to reject from.
	 * @return New vector that is a rejection of this onto axis.
	 *
	 * This is equal to calling this.projection(axis).inverse().add(this)
	 * @see Vector2D#projection(Vector2D)
	 */
	public Vector2D rejection(Vector2D axis) {
		Vector2D projectionVector = projection(axis);
		return projectionVector.inverse().add(this);
	}

	/**
	 * @return String representation of this vector.
	 */
	@Override
	public String toString() {
		return getX() + "; " + getY();
	}

	/**
	 * @return JsonObject representing this vector.
	 */
	public JsonObject toJSON() {
		return new JsonBuilder().add("x", this.getX()).add("y", this.getY()).build();
	}

	/**
	 * @param sourceObj Object to construct the vector from.
	 * @return New vector constructed from JSON, or, in case of failure, new Vector().
	 */
	public static Vector2D fromJSON(JsonObject sourceObj) {
		return fromJSON(sourceObj, new Vector2D());
	}

	/**
	 * @param sourceObj Object to construct the vector from.
	 * @param def Default value to return if the construction fails.
	 * @return New vector constructed from JSON, or, in case of failure, def.
	 */
	public static Vector2D fromJSON(JsonObject sourceObj, Vector2D def) {
		// already protected from NullPointerException, no need to check for null here
		try {
			float x = (float)sourceObj.getJsonNumber("x").doubleValue();
			float y = (float)sourceObj.getJsonNumber("y").doubleValue();

			return new Vector2D(x, y);
		} catch (NullPointerException | ClassCastException e) {
			return def;
		}
	}
}
