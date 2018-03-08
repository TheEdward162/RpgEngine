package com.edwardium.RPGEngine.Renderer.OpenGL;

import com.edwardium.RPGEngine.IO.IOUtil;
import com.edwardium.RPGEngine.Renderer.Font;
import com.edwardium.RPGEngine.Renderer.Vertex;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

import static org.lwjgl.opengl.GL11.*;
//import static org.lwjgl.stb.STBImageWrite.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class OpenGLFont extends Font {

	private final ByteBuffer ttf;
	private final STBTTFontinfo info;

	private STBTTBakedChar.Buffer bakedBuffer = null;
	private int bakedWidth;
	private int bakedHeight;

	private final int ascent;
	private final int descent;
	private final int lineGap;

	private final int fontHeight;

	private int textureID = -1;
	private String textureName = null;

	public OpenGLFont(String path, int fontHeight) {
		super();

		this.fontHeight = fontHeight;

		ttf = IOUtil.pathToByteBuffer(path, 512 * 1024);
		if (ttf == null) {
			throw new RuntimeException("Error: Could not load font from " + path + "!");
		}

		info = STBTTFontinfo.create();
		if (!stbtt_InitFont(info, ttf)) {
			throw new IllegalStateException("Failed to initialize font information.");
		}

		try (MemoryStack stack = stackPush()) {
			IntBuffer pAscent  = stack.mallocInt(1);
			IntBuffer pDescent = stack.mallocInt(1);
			IntBuffer pLineGap = stack.mallocInt(1);

			stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap);

			ascent = pAscent.get(0);
			descent = pDescent.get(0);
			lineGap = pLineGap.get(0);
		}
	}


	@Override
	public void bakeBuffer(int bitmapWidth, int bitmapHeight) {
		this.textureID = glGenTextures();
		this.bakedBuffer = STBTTBakedChar.malloc(96);

		this.bakedWidth = bitmapWidth;
		this.bakedHeight = bitmapHeight;

		ByteBuffer bitmap = BufferUtils.createByteBuffer(this.bakedWidth * this.bakedHeight);
		stbtt_BakeFontBitmap(ttf, fontHeight, bitmap, this.bakedWidth, this.bakedHeight, 32, this.bakedBuffer);

		glBindTexture(GL_TEXTURE_2D, textureID);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, this.bakedWidth, this.bakedHeight, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		glBindTexture(GL_TEXTURE_2D, 0);
	}

	public Vertex[] generateVertices(String text, float scaleX) {
		if (this.bakedBuffer == null)
			return null;

		Vector<Vertex> vertices = new Vector<>();
		try (MemoryStack stack = MemoryStack.stackPush()) {

			IntBuffer pCodePoint = stack.mallocInt(1);
			FloatBuffer x = stack.floats(0.0f);
			FloatBuffer y = stack.floats(0.0f);
			STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);

			float factorX = 1.0f / scaleX;
			int currentIndex = 0;
			int toIndex = text.length();
			while (currentIndex < toIndex) {
				currentIndex += OpenGLFont.getCodePoint(text, currentIndex, pCodePoint);

				int codePoint = pCodePoint.get(0);

				stbtt_GetBakedQuad(bakedBuffer, bakedWidth, bakedHeight, codePoint - 32, x, y, q, true);

				float posX = x.get(0);
				x.put(0, scaleDist(posX, x.get(0), factorX));

				float x0 = scaleDist(posX, q.x0(), factorX);
				float x1 = scaleDist(posX, q.x1(), factorX);
				float y0 = scaleDist(0, q.y0(), 1);
				float y1 = scaleDist(0, q.y1(), 1);

				vertices.add(new Vertex(x0, y0, 0, q.s0(), q.t0()));
				vertices.add(new Vertex(x1, y0, 0, q.s1(), q.t0()));
				vertices.add(new Vertex(x1, y1, 0, q.s1(), q.t1()));
				vertices.add(new Vertex(x0, y1, 0, q.s0(), q.t1()));
			}
		}

		Vertex[] vertexArray = new Vertex[vertices.size()];
		vertices.copyInto(vertexArray);

		return vertexArray;
	}
	private static float scaleDist(float center, float offset, float factor) {
		return (offset - center) * factor + center;
	}

	@Override
	public int getBakedTextureID() {
		return this.textureID;
	}

	@Override
	public void setTextureName(String name) {
		this.textureName = name;
	}

	@Override
	public String getTextureName() {
		return this.textureName;
	}

	@Override
	public void cleanup() {
		if (bakedBuffer != null) {
			bakedBuffer.free();
		}
	}
}
