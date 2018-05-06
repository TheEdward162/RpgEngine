package com.edwardium.RPGEngine.Renderer;

import com.edwardium.RPGEngine.Utility.Vector2D;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Arrays;

public class Vertex {

	public static final int positionElementCount = 4;
	public static final int colorElementCount = 4;
	public static final int textureCoordElementCount = 2;

	public static final int elementCount = positionElementCount + colorElementCount + textureCoordElementCount;

	// 4 bytes in one float
	public static final int stride = elementCount * 4;

	// nothing before position
	public static final int positionPointerOffset = 0;
	public static final int colorPointerOffset = positionElementCount * 4;
	public static final int texturePointerOffset = colorPointerOffset + colorElementCount * 4;

	private float[] position;
	private float[] color;
	private float[] textureCoord;

	public Vertex() {
		this(0, 0, 0, 0, 0);
	}

	public Vertex(float x, float y, float z, float s, float t) {
		this(x, y, z, 1, 1, 1, 1, s, t);
	}

	public Vertex(float x, float y, float z, float r, float g, float b, float a, float s, float t) {
		position = new float[] {x, y, z, 1.0f};
		this.color = new float[] {r, g, b, a};
		textureCoord = new float[] {s, t};
	}

	public float[] getPosition() {
		return new float[] { position[0], position[1], position[2], position[3] };
	}

	public float[] getElements() {
		return new float[] {
				position[0], position[1], position[2], position[3],
				color[0], color[1], color[2], color[3],
				textureCoord[0], textureCoord[1]};
	}

	public static FloatBuffer verticesToBuffer(Vertex[] vertices) {
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length * Vertex.elementCount);
		for (Vertex vertex : vertices) {
			if (vertex != null)
				verticesBuffer.put(vertex.getElements());
			else
				verticesBuffer.put(new Vertex().getElements());
		}
		verticesBuffer.flip();

		return verticesBuffer;
	}

	public static Vertex[][] splitArrayByLength(Vertex[] array, int size) {
		int splitCount = (int)Math.ceil((float)array.length / size);

		Vertex[][] result = new Vertex[splitCount][];

		int lastOffset = 0;
		for (int i = 0;  i < result.length; i++) {
			int newEnd = Math.min(lastOffset + size, array.length);
			result[i] = Arrays.copyOfRange(array, lastOffset, newEnd);
			lastOffset = newEnd;
		}

		return result;
	}

	public static Vertex[] arrayFromVector2D(Vector2D[] sourceArray) {
		Vector2D topCorner = new Vector2D();
		Vector2D bottomCorner = new Vector2D();

		for (Vector2D vec : sourceArray) {
			if (vec.getX() < topCorner.getX())
				topCorner.setX(vec.getX());
			else if (vec.getX() > bottomCorner.getX())
				bottomCorner.setX(vec.getX());

			if (vec.getY() < topCorner.getY())
				topCorner.setY(vec.getY());
			else if (vec.getY() > bottomCorner.getY())
				bottomCorner.setY(vec.getY());
		}

		float width = bottomCorner.getX() - topCorner.getX();
		float height = bottomCorner.getY() - topCorner.getY();

		Vertex[] resultArray = new Vertex[sourceArray.length];
		for (int i = 0; i < sourceArray.length; i++) {
			float s = (sourceArray[i].getX() - topCorner.getX()) / width;
			float t = (sourceArray[i].getY() - topCorner.getY()) / height;

			resultArray[i] = new Vertex(sourceArray[i].getX(), sourceArray[i].getY(), 0, s, t);
		}

		return resultArray;
	}
}
