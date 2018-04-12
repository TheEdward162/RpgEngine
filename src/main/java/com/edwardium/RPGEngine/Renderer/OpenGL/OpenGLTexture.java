package com.edwardium.RPGEngine.Renderer.OpenGL;

import com.edwardium.RPGEngine.Renderer.Texture;
import com.edwardium.RPGEngine.Vector2D;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load;

public class OpenGLTexture extends Texture {

	private final boolean isLoaded;
	private final int textureID;
	private final int textureUnit;

	private final int textureWidth;
	private final int textureHeight;

	public OpenGLTexture(String path, int textureUnit) {
		this.textureUnit = textureUnit;

		IntBuffer pWidth = BufferUtils.createIntBuffer(1);
		IntBuffer pHeight = BufferUtils.createIntBuffer(1);
		IntBuffer pChannels = BufferUtils.createIntBuffer(1);

		ByteBuffer data = stbi_load(path, pWidth, pHeight, pChannels, 4);
		if (data == null) {
			isLoaded = false;
			textureID = -1;

			this.textureWidth = 0;
			this.textureHeight = 0;

			return;
		} else {
			isLoaded = true;
		}

		this.textureWidth = pWidth.get();
		this.textureHeight = pHeight.get();

		// init texture
		glActiveTexture(textureUnit);

		textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureID);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureWidth, textureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);

		// unbind
		glBindTexture(GL_TEXTURE_2D, 0);
		glActiveTexture(0);

		// free local buffer
		stbi_image_free(data);
	}

	public OpenGLTexture(int textureUnit, int textureID, int textureWidth, int textureHeight) {
		this.textureUnit = textureUnit;
		this.textureID = textureID;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;

		this.isLoaded = true;
	}

	@Override
	public boolean isLoaded() {
		return this.isLoaded;
	}

	@Override
	public int getTextureID() {
		return this.textureID;
	}

	@Override
	public int getTextureUnit() {
		return this.textureUnit;
	}

	@Override
	public Vector2D getSize() {
		return new Vector2D(textureWidth, textureHeight);
	}

	@Override
	public float[] computeSubtexture(Vector2D offset, Vector2D size) {
		float offsetX = offset != null ? offset.getX() / textureWidth : 0;
		float offsetY = offset != null ? offset.getY() / textureHeight : 0;

		float width = size != null ? size.getX() / textureWidth : (1 - offsetX);
		float height = size != null ? size.getY() / textureHeight : (1 - offsetY);

		return new float[] { offsetX, offsetY, width, height };
	}


}
