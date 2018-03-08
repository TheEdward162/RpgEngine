package com.edwardium.RPGEngine.Renderer;

import java.nio.IntBuffer;

public abstract class Font {
	protected Font() {

	}

	public abstract void bakeBuffer(int bitmap_width, int bitmap_height);
	public abstract int getBakedTextureID();

	public abstract void setTextureName(String name);
	public abstract String getTextureName();

	public abstract Vertex[] generateVertices(String text, float scaleX);

	public abstract void cleanup();

	public static int getCodePoint(String text, int i, IntBuffer cpOut) {
		char c1 = text.charAt(i);
		if (Character.isHighSurrogate(c1) && i + 1 < text.length()) {
			char c2 = text.charAt(i + 1);
			if (Character.isLowSurrogate(c2)) {
				cpOut.put(0, Character.toCodePoint(c1, c2));
				return 2;
			}
		}
		cpOut.put(0, c1);
		return 1;
	}

	public static String[] splitStringByLength(String string, int length) {
		int splitCount = (int)Math.ceil((float)string.length() / length);
		String[] result = new String[splitCount];

		int lastOffset = 0;
		for (int i = 0;  i < result.length; i++) {
			int newEnd = Math.min(lastOffset + length, string.length());
			result[i] = string.substring(lastOffset, newEnd);
			lastOffset = newEnd;
		}

		return result;
	}
}
