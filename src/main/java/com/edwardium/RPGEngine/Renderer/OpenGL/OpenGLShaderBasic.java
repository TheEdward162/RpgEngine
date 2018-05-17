package com.edwardium.RPGEngine.Renderer.OpenGL;

import com.edwardium.RPGEngine.IO.IOUtil;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Light;
import com.edwardium.RPGEngine.Utility.Vector2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.memUTF8;

public class OpenGLShaderBasic {

	public static class CircleInfoStruct {
		public final Float minRadius;
		public final Float maxRadius;
		public final Float maxAngle;

		public CircleInfoStruct(Float minRadius, Float maxRadius, Float maxAngle) {
			this.minRadius = minRadius;
			this.maxRadius = maxRadius;
			this.maxAngle = maxAngle;
		}
	}

	public static class TextureInfoStruct {
		public final Integer textureUnit;
		public final float[] textureSubs;
		public final Boolean overrideColor;

		public TextureInfoStruct(Integer textureUnit, float[] textureSubs, Boolean overrideColor) {
			this.textureUnit = textureUnit;
			this.textureSubs = textureSubs;
			this.overrideColor = overrideColor;
		}
	}

	public static final int attribute_position = 0;
	public static final int attribute_vertexColor = 1;
	public static final int attribute_textureCoord = 2;

	private Integer program_id;
	private int shader_vertex;
	private int shader_fragment;

	public OpenGLShaderBasic(String vertexPath, String fragmentPath) {
		program_id = createProgram(vertexPath, fragmentPath);
	}

	/**
	 * Create a shader object from the given classpath resource.
	 *
	 * @param resource The class path
	 * @param type The shader type
	 * @return the shader object id
	 */
	private Integer createShader(String resource, int type)  {
		return createShader(resource, type, null);
	}

	/**
	 * Create a shader object from the given classpath resource.
	 *
	 * @param path The class path
	 * @param type The shader type
	 * @param version The GLSL version to prepend to the shader source, or null
	 * @return the shader object id
	 */
	private Integer createShader(String path, int type, String version) {
		int shader = glCreateShader(type);

		ByteBuffer source = IOUtil.pathToByteBuffer(path, 8192);
		if (source == null)
			return null;

		if (version == null) {
			PointerBuffer strings = BufferUtils.createPointerBuffer(1);
			IntBuffer lengths = BufferUtils.createIntBuffer(1);

			strings.put(0, source);
			lengths.put(0, source.remaining());

			glShaderSource(shader, strings, lengths);
		} else {
			PointerBuffer strings = BufferUtils.createPointerBuffer(2);
			IntBuffer lengths = BufferUtils.createIntBuffer(2);

			ByteBuffer preamble = memUTF8("#version " + version + "\n", false);

			strings.put(0, preamble);
			lengths.put(0, preamble.remaining());

			strings.put(1, source);
			lengths.put(1, source.remaining());

			glShaderSource(shader, strings, lengths);
		}

		glCompileShader(shader);
		int compiled = glGetShaderi(shader, GL_COMPILE_STATUS);
		String shaderLog = glGetShaderInfoLog(shader);
		if (shaderLog != null && shaderLog.trim().length() > 0) {
			System.err.println(shaderLog);
		}
		if (compiled == 0) {
			throw new AssertionError("Error: Could not compile shader!");
		}
		return shader;
	}

	private Integer createProgram(String vertexPath, String fragmentPath) {
		program_id = glCreateProgram();

		Integer vshader = createShader(vertexPath, GL_VERTEX_SHADER);
		Integer fshader = createShader(fragmentPath, GL_FRAGMENT_SHADER);
		if (vshader == null || fshader == null) {
			return null;
		}

		shader_vertex = vshader;
		shader_fragment = fshader;

		glAttachShader(program_id, shader_vertex);
		glAttachShader(program_id, shader_fragment);

		glBindAttribLocation(program_id, attribute_position, "in_Position");
		glBindAttribLocation(program_id, attribute_vertexColor, "in_VertexColor");
		glBindAttribLocation(program_id, attribute_textureCoord, "in_TextureCoord");

		glLinkProgram(program_id);

		int linked = glGetProgrami(program_id, GL_LINK_STATUS);
		String programLog = glGetProgramInfoLog(program_id);
		if (programLog != null && programLog.trim().length() > 0) {
			System.err.println(programLog);
		}
		if (linked == 0) {
			throw new AssertionError("Could not link program");
		}

		return program_id;
	}

	public Integer getProgramID() {
		return program_id;
	}

	public void fillUniformGlobalColor(Color color) {
		int globalColorLoc = glGetUniformLocation(getProgramID(), "un_globalColor");
		glUniform4fv(globalColorLoc, color.getAsArray());
	}

	public void fillUniformCircleInfo(CircleInfoStruct circleInfo) {
		if (circleInfo.minRadius != null) {
			int circleMinRadiusLoc = glGetUniformLocation(getProgramID(), "un_circleInfo.minRadius");
			glUniform1f(circleMinRadiusLoc, circleInfo.minRadius);
		}
		if (circleInfo.maxRadius != null) {
			int circleMaxRadiusLoc = glGetUniformLocation(getProgramID(), "un_circleInfo.maxRadius");
			glUniform1f(circleMaxRadiusLoc, circleInfo.maxRadius);
		}
		if (circleInfo.maxAngle != null) {
			int circleMaxAngleLoc = glGetUniformLocation(getProgramID(), "un_circleInfo.maxAngle");
			glUniform1f(circleMaxAngleLoc, circleInfo.maxAngle);
		}
	}

	public void fillUnitformTextureInfo(TextureInfoStruct textureInfo) {
		if (textureInfo.textureUnit != null) {
			int textureIDLoc = glGetUniformLocation(getProgramID(), "un_textureInfo.tex");
			glUniform1i(textureIDLoc, textureInfo.textureUnit);
		}

		if (textureInfo.textureSubs != null) {
			int textureSubsLoc = glGetUniformLocation(getProgramID(), "un_textureInfo.textureSubspace");
			glUniform4fv(textureSubsLoc, textureInfo.textureSubs);
		}

		if (textureInfo.overrideColor != null) {
			int overrideColorLoc = glGetUniformLocation(getProgramID(), "un_textureInfo.overrideColor");
			glUniform1i(overrideColorLoc, textureInfo.overrideColor ? 1 : 0);
		}
	}

	public void fillUniformLightInfo(int index, Light light) {
		String arrBase = "un_Lights[" + index + "]";

		int positionLoc = glGetUniformLocation(getProgramID(), arrBase + ".position");
		glUniform4f(positionLoc, light.position.getX(), light.position.getY(), 0f, 1f);

		int colorLoc = glGetUniformLocation(getProgramID(), arrBase + ".color");
		glUniform4fv(colorLoc, light.color.getAsArray());

		int powerLoc = glGetUniformLocation(getProgramID(), arrBase + ".power");
		glUniform1f(powerLoc, light.power);

		int cutoffLoc = glGetUniformLocation(getProgramID(), arrBase + ".cutoff");
		glUniform1f(cutoffLoc, light.cutoff);
	}

	public void fillViewportSize(Vector2D size) {
		int viewportSizeLoc = glGetUniformLocation(getProgramID(), "un_viewportSize");
		glUniform2f(viewportSizeLoc, size.getX(), size.getY());
	}

	public void fillUniformLightCount(int count) {
		int countLoc = glGetUniformLocation(getProgramID(), "un_lightCount");
		glUniform1i(countLoc, count);
	}

	public void fillUniformUseLights(boolean useLights) {
		int boolLoc = glGetUniformLocation(getProgramID(), "un_useLights");
		glUniform1i(boolLoc, useLights ? 1 : 0);
	}

	public void cleanup() {
		if (program_id != null) {
			GL20.glDetachShader(program_id, shader_vertex);
			GL20.glDetachShader(program_id, shader_fragment);

			GL20.glDeleteShader(shader_vertex);
			GL20.glDeleteShader(shader_fragment);
			GL20.glDeleteProgram(program_id);
		}
	}
}
