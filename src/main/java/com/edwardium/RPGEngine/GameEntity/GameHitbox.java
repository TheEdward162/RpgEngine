package com.edwardium.RPGEngine.GameEntity;

import com.edwardium.RPGEngine.Engine;
import com.edwardium.RPGEngine.Rectangle;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class GameHitbox {

	private Vector2D[] points;

	private Float radius = null;
	private float broadRadius;

	public GameHitbox(float radius) {
		this(radius, new Vector2D(0, 0));
	}

	public GameHitbox(float radius, Vector2D centerOffset) {
		this.radius = radius;
		this.broadRadius = radius;
		this.points = new Vector2D[] { centerOffset };
	}

	public GameHitbox(Vector2D[] points) {
		this.points = points;

		for (Vector2D point : points) {
			this.broadRadius = Math.max(this.broadRadius, point.getMagnitude());
		}
	}
	public GameHitbox(Rectangle rectangle) {
		this(new Vector2D[] {
				rectangle.topLeft,
				new Vector2D(rectangle.topLeft).scale(-1, 1),
				rectangle.bottomRight,
				new Vector2D(rectangle.bottomRight).scale(-1, 1)
		});
	}

	public boolean checkCollision(Vector2D myPosition, Vector2D myVelocity, float myRotation, GameHitbox other, Vector2D otherPosition, Vector2D otherVelocity, float otherRotation) {
		return checkBroad(myPosition, other, otherPosition) && checkNarrow(myPosition, myRotation, other, otherPosition, otherRotation);
	}
	public float calculateCrossSection(Vector2D normal) {
		float[] projection;
		if (this.radius != null) {
			projection = calculateMinMaxProjectionCircle(radius, points[0], normal.getNormal());
		} else {
			projection = calculateMinMaxProjectionConvex(points, normal.getNormal());
		}

		return (projection[1] - projection[0]) * Engine.PIXEL_TO_METER;
	}

	private boolean checkBroad(Vector2D myPosition, GameHitbox other, Vector2D otherPosition) {
		if (other == null)
			return false;
		return Vector2D.distance(myPosition, otherPosition) <= this.broadRadius + other.broadRadius;
	}
	private boolean checkBroadVelocity(Vector2D myPosition, Vector2D myVelocity, float myRotation, GameHitbox other, Vector2D otherPosition, Vector2D otherVelocity, float otherRotation) {
		// Math saves the day
		// A(u) = myPosition + u * myVelocity
		// B(u) = otherPosition + u * otherVelocity
		// D(u) = |A(u) - B(u)| = |myPosition + u * myVelocity - otherPosition - u * otherVelocity|
		// = |myPosition - otherPosition + u * (myVelocity - otherVelocity)|
		//
		// Solve D(u) <= this.broadRadius + other.broadRadius for u
		// |myPosition - otherPosition + u * (myVelocity - otherVelocity)| <= this.broadRadius + other.broadRadius
		// (myPosition - otherPosition + u * (myVelocity - otherVelocity))^2 <= (this.broadRadius + other.broadRadius)^2
		// sub myPosition - otherPosition = A, sub (myVelocity - otherVelocity) = B, sub this.broadRadius + other.broadRadius = C
		// (A + u * B)^2 <= C^2
		// A^2 + 2uAB + (uB)^2 <= c^2
		//
		// B^2 * u^2 + 2AB * u + A^2 - C^2 <= 0
		// B^2(u^2 + 2Au / B + (A^2 - C^2) / B^2) <= 0
		// B^2((u + A / B)^2 + (A^2 - C^2) / B^2 - A^2 / B^2) <= 0
		// B^2(u + A / B)^2 + (A^2 - C^2) - A^2 <= 0
		// B^2(u + A / B)^2 <= C^2
		// (u + A / B)^2 <= C^2 / B^2
		// (u + A / B) <= +- C / B
		// u <= +- C / B - A / B

		float A = Vector2D.subtract(myPosition, otherPosition).getMagnitude();
		float B = Vector2D.subtract(myVelocity, otherVelocity).getMagnitude();
		float C = this.broadRadius + other.broadRadius;

		if (B == 0)
			return checkBroad(myPosition, other, otherPosition) && checkNarrow(myPosition, myRotation, other, otherPosition, otherRotation);

		float uMin = -C / B - A / B;
		float uMax = C / B - A / B;

		if ((uMin >= 0 && uMin <= 1) || (uMax >= 0 && uMax <= 1)) {
			// TODO: Think
			return true;
		}

		return false;
	}
	private boolean checkNarrow(Vector2D myPosition, float myRotation, GameHitbox other, Vector2D otherPosition, float otherRotation) {
		if (other == null)
			return false;

		// collision checks done using SAT algorithm
		// more info: https://www.sevenson.com.au/actionscript/sat/
		if (this.radius != null) {
			if (other.radius != null) {
				// distance from center to center less than or equal to sum of radii
				return Vector2D.add(myPosition, points[0]).distance(Vector2D.add(otherPosition, other.points[0])) <= this.radius + other.radius;
			} else {
				return checkSATCircle(this.radius, Vector2D.add(myPosition, points[0]), other.points, otherPosition, otherRotation);
			}
		} else {
			if (other.radius != null) {
				return checkSATCircle(other.radius, Vector2D.add(otherPosition, other.points[0]), this.points, myPosition, myRotation);
			} else {
				return checkConvexConvex(this.points, myPosition, myRotation, other.points, otherPosition, otherRotation);
			}
		}
	}

	private static boolean checkSATCircle(float circleRadius, Vector2D circleCenter, Vector2D[] convexShape, Vector2D convexShift, float convexRotation) {
		if (convexShape.length < 2)
			return false;

		Vector2D[] convexShifted = Vector2D.add(Vector2D.rotatedBy(convexShape, convexRotation), convexShift);
		Vector2D closesPoint = null;
		for (Vector2D v : convexShifted) {
			if (closesPoint == null || circleCenter.distance(v) < circleCenter.distance(closesPoint)) {
				closesPoint = v;
			}
		}

		// circle center to closes point axis
		Vector2D normalAxis = Vector2D.subtract(closesPoint, circleCenter);

		float[] projectionMinMaxConvex = calculateMinMaxProjectionConvex(convexShifted, normalAxis);
		float[] projectionMinMaxCircle = calculateMinMaxProjectionCircle(circleRadius, circleCenter, normalAxis);

		if (projectionsDontIntersect(projectionMinMaxConvex, projectionMinMaxCircle))
			return false;

		for (int i = 0; i < convexShifted.length; i++) {
			// take a side
			int previousIndex = i - 1;
			if (previousIndex < 0)
				previousIndex += convexShifted.length;
			Vector2D sideA = convexShifted[previousIndex];
			Vector2D sideB = convexShifted[i];

			// normal axis for this side
			normalAxis = Vector2D.subtract(sideA, sideB).getNormal();

			projectionMinMaxCircle = calculateMinMaxProjectionCircle(circleRadius, circleCenter, normalAxis);

			// projection of a onto the normal axis
			projectionMinMaxConvex = calculateMinMaxProjectionConvex(convexShifted, normalAxis);
			if (projectionMinMaxConvex == null)
				return false;

			if (projectionsDontIntersect(projectionMinMaxConvex, projectionMinMaxCircle))
				return false;
		}

		return true;
	}

	private static boolean checkConvexConvex(Vector2D[] a, Vector2D aShift, float aRotation, Vector2D[] b, Vector2D bShift, float bRotation) {
		if (a.length < 2 || b.length < 2)
			return false;

		// first shift all vectors
		Vector2D[] aShifted = Vector2D.add(Vector2D.rotatedBy(a, aRotation), aShift);
		Vector2D[] bShifted = Vector2D.add(Vector2D.rotatedBy(b, bRotation), bShift);

		return checkSAT(aShifted, bShifted) && checkSAT(bShifted, aShifted);
	}
	private static boolean checkSAT(Vector2D[] shapeA, Vector2D[] shapeB) {
		// check all projection on normal axes of sides of shapeA against projections of shapeB
		for (int i = 0; i < shapeA.length; i++) {
			// take a side
			int previousIndex = i - 1;
			if (previousIndex < 0)
				previousIndex += shapeA.length;
			Vector2D sideA = shapeA[previousIndex];
			Vector2D sideB = shapeA[i];

			// normal axis for this side
			Vector2D normalAxis = Vector2D.subtract(sideA, sideB).getNormal();

			// projection of a onto the normal axis
			float[] projectionMinMaxA = calculateMinMaxProjectionConvex(shapeA, normalAxis);
			if (projectionMinMaxA == null)
				return false;

			// projection of b onto the normal axis
			float[] projectionMinMaxB = calculateMinMaxProjectionConvex(shapeB, normalAxis);
			if (projectionMinMaxB == null)
				return false;

			if (projectionsDontIntersect(projectionMinMaxA, projectionMinMaxB))
				return false;
		}

		return true;
	}

	private static float[] calculateMinMaxProjectionConvex(Vector2D[] points, Vector2D axis) {
		// projection of a onto the normal axis
		Float projectionAMin = null;
		Float projectionAMax = null;
		for (Vector2D point : points) {
			float projection = point.dot(axis) / axis.getMagnitude();
			if (projectionAMin == null || projection < projectionAMin)
				projectionAMin = projection;
			if (projectionAMax == null || projection > projectionAMax)
				projectionAMax = projection;
		}

		if (projectionAMin != null) {
			return new float[] { projectionAMin, projectionAMax };
		}

		return null;
	}
	private static float[] calculateMinMaxProjectionCircle(float radius, Vector2D center, Vector2D axis) {
		// circle projection is calculated by projection the center point of the circle onto the axis and then
		// adding and subtracting radius to obtain the edge points
		float projection = center.dot(axis) / axis.getMagnitude();
		return new float[] {
				Math.min(projection - radius, projection + radius),
				Math.max(projection + radius, projection - radius)
		};
	}
	private static boolean projectionsDontIntersect(float[] projA, float[] projB) {
		return (projA[1] < projB[0] || projB[1] < projA[0]);
	}

	public static void renderHitbox(Renderer renderer, Vector2D position, float rotation, GameHitbox hitbox) {
		if (hitbox.radius != null) {
			renderer.drawCircle(hitbox.radius - 2f, hitbox.radius, 4f, position, new float[]{1, 0, 0, 1}, new TextureInfo("default"));
		} else {
			if (hitbox.points.length >= 2) {
				for (int i = 0; i < hitbox.points.length; i++) {
					int previousIndex = i - 1;
					if (previousIndex < 0)
						previousIndex += hitbox.points.length;

					Vector2D sideA = new Vector2D(hitbox.points[previousIndex]).rotateBy(rotation).add(position);
					Vector2D sideB = new Vector2D(hitbox.points[i]).rotateBy(rotation).add(position);

					renderer.drawLine(sideA, sideB, 2f, new float[] { 1f, 0f, 0f, 1f });
				}
			}
		}
	}
}
