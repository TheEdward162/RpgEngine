package com.edwardium.RPGEngine.Renderer.OpenGL;

import com.edwardium.RPGEngine.Engine;
import com.edwardium.RPGEngine.Renderer.Font;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Renderer.Vertex;
import com.edwardium.RPGEngine.Vector2D;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class OpenGLRenderer extends Renderer {
	private final float[] defaultColor = new float[] { 1f, 1f, 1f, 1f };
	private final String defaultTexture = "default";

	private Long window = null;

	// square buffers
	private int squareVAO;
	private int squareVBO;
	private int squareIBO;

	// font buffers
	private int fontVAO;
	private int fontVBO;
	private int fontIBO;

	// in case we get a string that is longer, we will either need to
	// split it and render is separately, or, even better, ignore it and
	// leave the only trace of the reason here in this comment
	private final int fontMaxCharacters = 10;
	private final int fontMaxVertices = fontMaxCharacters * 4;
	private final int fontMaxVBOSize = fontMaxVertices * Vertex.elementCount;

	// shader
	private OpenGLShaderBasic basicShader;

	// textures
	private HashMap<String, OpenGLTexture> gameTextures;

	public OpenGLRenderer(String title, int width, int height) {
		super(title, width, height);

		init();
	}

	@Override
	protected void init() {
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW!");

		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(windowWidth, windowHeight, windowTitle, NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window!");

		// window resize callback
		glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int w, int h) {
				if (w > 0 && h > 0) {
					windowWidth = w;
					windowHeight = h;
				}
			}
		});

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		// Set the clear color
		// Let there be midnight blue :3
		glClearColor(0.098f, 0.098f, 0.439f, 0.0f);

		setupPrimitives();
		setupShaders();
		setupTextures();
		setupFonts();
	}

	public long getWindowHandle() {
		return window;
	}

	@Override
	public Vector2D getWindowSize() {
		return new Vector2D(windowWidth, windowHeight);
	}

	private void setupPrimitives() {
		Vertex[] vertices = new Vertex[] {
				new Vertex(-0.5f, -0.5f, 0, 0, 0),
				new Vertex(0.5f, -0.5f, 0, 1, 0),
				new Vertex(0.5f, 0.5f, 0, 1, 1),
				new Vertex(-0.5f, 0.5f, 0, 0, 1),
		};

		// Select VAO
		squareVAO = glGenVertexArrays();
		glBindVertexArray(squareVAO);

		// Now init square shape VBO
		squareVBO = createVBO(vertices, GL_STATIC_DRAW);

		// Deselect the VAO
		glBindVertexArray(0);

		// square IBO
		squareIBO = createIBO(new int[] {0, 1, 2, 3}, GL_STATIC_DRAW);
	}

	private void setupShaders() {
		basicShader = new OpenGLShaderBasic("Assets/Shaders/vertex_shader.glsl", "Assets/Shaders/fragment_shader.glsl");
		glUseProgram(basicShader.getProgramID());

		// Enable transparency
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	private int createVBO(Vertex[] vertices, int vboFlags) {
		FloatBuffer verticesBuffer = Vertex.verticesToBuffer(vertices);

		int vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, vboFlags);

		glVertexAttribPointer(OpenGLShaderBasic.attribute_position, Vertex.positionElementCount, GL_FLOAT, false, Vertex.stride, Vertex.positionPointerOffset);
		glVertexAttribPointer(OpenGLShaderBasic.attribute_vertexColor, Vertex.colorElementCount, GL_FLOAT, false, Vertex.stride, Vertex.colorPointerOffset);
		glVertexAttribPointer(OpenGLShaderBasic.attribute_textureCoord, Vertex.textureCoordElementCount, GL_FLOAT, false, Vertex.stride, Vertex.texturePointerOffset);

		// Deselect VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		return vboID;
	}
	private int createVBO(Collection<Vertex> vertices, int vboFlags) {
		Vertex[] verticesArray = new Vertex[vertices.size()];
		int i = 0;
		for (Vertex vertex : vertices) {
			verticesArray[i] = vertex;
			i++;
		}

		return createVBO(verticesArray, vboFlags);
	}

	private int createIBO(int[] indices, int iboFlags) {
		int iboID = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, (IntBuffer) BufferUtils.createIntBuffer(indices.length).put(indices).flip(), iboFlags);

		return iboID;
	}
	private int createIBO(Collection<Integer> indices, int iboFlags) {
		int[] indicesArray = new int[indices.size()];

		int i = 0;
		for (Integer index : indices) {
			indicesArray[i] = index;
			i++;
		}

		return createIBO(indicesArray, iboFlags);
	}

	private void setupTextures() {
		gameTextures = new HashMap<>();
		gameTextures.put("default", new OpenGLTexture("Assets/Textures/default.png", GL_TEXTURE0));
		gameTextures.put("debug", new OpenGLTexture("Assets/Textures/debug1.png", GL_TEXTURE0));

		glEnable(GL_TEXTURE_2D);
	}

	private void setupFonts() {
		String fontPath = "Assets/Fonts/NotoSans-Medium.ttf";
		basicFont = new OpenGLFont(fontPath, 20);

		fontVAO = glGenVertexArrays();
		glBindVertexArray(fontVAO);

		fontVBO = createVBO(new Vertex[fontMaxVBOSize], GL_DYNAMIC_DRAW);

		glActiveTexture(GL_TEXTURE0);
		basicFont.bakeBuffer(512, 512);

		basicFont.setTextureName("basicFontTexture");
		gameTextures.put(basicFont.getTextureName(), new OpenGLTexture(GL_TEXTURE0, basicFont.getBakedTextureID(), 512, 512));
		glActiveTexture(0);

		glBindVertexArray(0);

		// we can create a static indices buffer, because the indices never change
		int[] indices = new int[fontMaxCharacters];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = i;
		}
		fontIBO = createIBO(indices, GL_STATIC_DRAW);
	}

	@Override
	public void show() {
		glfwShowWindow(window);
	}

	@Override
	public void hide() {
		glfwHideWindow(window);
	}

	@Override
	public void beforeLoop(Vector2D cameraPosition) {
		// clear the framebuffer
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glViewport(0, 0, windowWidth, windowHeight);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		glOrtho(-windowWidth / 2, windowWidth / 2, windowHeight / 2, -windowHeight / 2, 0.0f, 1.0f);

		glTranslatef(cameraPosition.getX(), cameraPosition.getY(), 0f);
	}

	@Override
	public void afterLoop() {
		// swap buffers
		glfwSwapBuffers(window);

		// Poll for window events. The key callback above will only be
		// invoked during this call.
		glfwPollEvents();
	}

	private void beginDraw(int vao, int vbo, int ibo, float[] color, Float circleRadius, TextureInfo textureInfo, boolean overrideTextureColor) {
		glBindVertexArray(vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);

		// default values on null
		if (color == null)
			color = defaultColor;
		if (circleRadius == null)
			circleRadius = 1f;

		OpenGLTexture currentTexture;
		if (textureInfo == null) {
			textureInfo = new TextureInfo("default", null, null);
		}
		if (textureInfo.textureName != null && gameTextures.containsKey(textureInfo.textureName))
			currentTexture = gameTextures.get(textureInfo.textureName);
		else
			currentTexture = gameTextures.get(defaultTexture);

		// use shader program
		glUseProgram(basicShader.getProgramID());

		glActiveTexture(currentTexture.getTextureUnit());
		glBindTexture(GL_TEXTURE_2D, currentTexture.getTextureID());
		basicShader.fillUniformData(color, circleRadius, currentTexture.getTextureUnit(), currentTexture.computeSubtexture(textureInfo.textureOffset, textureInfo.textureSize), overrideTextureColor);

		// push model matrix
		// a.k.a. only do transforms for this model
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
	}

	private void endDraw() {
		// pop model matrix
		glPopMatrix();

		// unbind stuff
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

		glDisableVertexAttribArray(2);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(0);
		glBindVertexArray(0);
	}

	@Override
	public void drawLine(Vector2D from, Vector2D to, float width, float[] color) {
		if (width <= 0)
			return;

		Vector2D directionVector = Vector2D.subtract(to, from);

		beginDraw(squareVAO, squareVBO, squareIBO, color, 1f, null, false);

		// transforms
		glTranslatef(from.getX() + directionVector.getX() / 2, from.getY() + directionVector.getY() / 2, 0); // translate
		glRotatef(directionVector.getAngle() / (float)Math.PI * 180, 0, 0, 1f); // rotate around z axis
		glScalef(directionVector.getMagnitude(), width , 1); // scale

		glDrawArrays(GL_QUADS, 0, 4);

		endDraw();
	}

	@Override
	public void drawRectangle(Vector2D center, Vector2D size, float rotationAngle, float[] color, TextureInfo textureInfo) {
		beginDraw(squareVAO, squareVBO, squareIBO, color, 1f, textureInfo, false);

		// transforms
		glTranslatef(center.getX(), center.getY(), 0); // translate
		glRotatef(rotationAngle / (float)Math.PI * 180, 0f, 0f, 1f); // rotate around z axis
		glScalef(size.getX(), size.getY(), 1); // scale

		glDrawArrays(GL_QUADS, 0, 4);

		endDraw();
	}

	@Override
	public void drawCircle(float radius, Vector2D center, float[] color, TextureInfo textureInfo) {
		beginDraw(squareVAO, squareVBO, squareIBO, color, 0.5f, textureInfo, false);

		// transforms
		glTranslatef(center.getX(), center.getY(), 0); // translate
		glScalef(radius, radius, 1); // scale

		glDrawArrays(GL_QUADS, 0, 4);

		endDraw();
	}

	@Override
	public void drawString(Font font, String text, Vector2D position, Vector2D scale, float[] color) {
		beginDraw(fontVAO, fontVBO, fontIBO, color, 1f, new TextureInfo(font.getTextureName()), true);

		// do transforms
		glScalef(1f, scale.getY(), 1f);
		glTranslatef(position.getX(), position.getY(), 0f);

		Vertex[] vertices = font.generateVertices(text, scale.getX());
		Vertex[][] splitVertices = Vertex.splitArrayByLength(vertices, fontMaxVertices);
		for (Vertex[] subVertices : splitVertices) {
			FloatBuffer verticesBuffer = Vertex.verticesToBuffer(subVertices);

			// upload new data
			glBufferSubData(GL_ARRAY_BUFFER, 0, verticesBuffer);

			// draw
			glDrawArrays(GL_QUADS, 0, vertices.length);
		}

		endDraw();
	}

	@Override
	public boolean shouldClose() {
		return glfwWindowShouldClose(window);
	}

	@Override
	public void cleanup() {
		if (window != null) {
			// Delete the shaders
			GL20.glUseProgram(0);

			basicShader.cleanup();
			basicFont.cleanup();

			// Free the window callbacks and destroy the window
			glfwFreeCallbacks(window);
			glfwDestroyWindow(window);

			// Terminate GLFW and free the error callback
			glfwTerminate();
			glfwSetErrorCallback(null).free();

			window = null;
		}
	}
}
