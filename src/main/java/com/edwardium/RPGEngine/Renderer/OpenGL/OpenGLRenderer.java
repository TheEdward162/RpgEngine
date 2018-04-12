package com.edwardium.RPGEngine.Renderer.OpenGL;

import com.edwardium.RPGEngine.Rectangle;
import com.edwardium.RPGEngine.Renderer.*;
import com.edwardium.RPGEngine.Vector2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.HashMap;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.NULL;

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

	private boolean vsyncStatus = true;

	// shader
	private OpenGLShaderBasic basicShader;

	// textures
	private HashMap<String, OpenGLTexture> gameTextures;

	public OpenGLRenderer(String title, int width, int height) {
		super(title, width, height);

		// DEBUG INFO
		System.err.println("GL_VENDOR: " + glGetString(GL_VENDOR));
		System.err.println("GL_RENDERER: " + glGetString(GL_RENDERER));
		System.err.println("GL_VERSION: " + glGetString(GL_VERSION));

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
		setVSync(vsyncStatus);

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

		gameTextures.put("sheet1", new OpenGLTexture("Assets/Textures/sheet1.png", GL_TEXTURE0));

		gameTextures.put("initsplash", new OpenGLTexture("Assets/splash.png", GL_TEXTURE0));

		glEnable(GL_TEXTURE_2D);
	}

	private void setupFonts() {
		String fontPath = "Assets/Fonts/NotoSans-Medium.ttf";
		basicFont = new OpenGLFont(fontPath, 30);

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
	public boolean getVSync() {
		return this.vsyncStatus;
	}

	@Override
	public void setVSync(boolean value) {
		this.vsyncStatus = value;

		glfwSwapInterval(value ? 1 : 0);
	}

	@Override
	public void beforeLoop() {
		// clear the framebuffer
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glViewport(0, 0, windowWidth, windowHeight);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		glOrtho(-windowWidth / 2, windowWidth / 2, windowHeight / 2, -windowHeight / 2, 0.0f, 1.0f);
	}

	@Override
	public void afterLoop() {
		// swap buffers
		glfwSwapBuffers(window);

		// Poll for window events. The key callback above will only be
		// invoked during this call.
		glfwPollEvents();
	}

	@Override
	public void pushTransformMatrix() {
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
	}
	@Override
	public void applyTransformMatrix(Vector2D scale, Float rotation, Vector2D translation) {
		if (translation != null)
			glTranslatef(translation.getX(), translation.getY(), 0f);

		if (rotation != null)
			glRotatef(rotation / (float)Math.PI * 180, 0f, 0f, 1f);

		if (scale != null)
			glScalef(scale.getX(), scale.getY(), 1f);
	}
	@Override
	public void popTransformMatrix() {
		glPopMatrix();
	}

	private void beginDraw(int vao, int vbo, int ibo, OpenGLShaderBasic.CircleInfoStruct circleInfo, TextureInfo textureInfo, boolean overrideTextureColor) {
		glBindVertexArray(vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);

		// default values on null
		if (circleInfo == null) {
			circleInfo = new OpenGLShaderBasic.CircleInfoStruct(0f, 1f, 4f);
		}

		OpenGLTexture currentTexture;
		if (textureInfo == null) {
			textureInfo = new TextureInfo("debug");
		}
		if (textureInfo.textureName != null && gameTextures.containsKey(textureInfo.textureName))
			currentTexture = gameTextures.get(textureInfo.textureName);
		else
			currentTexture = gameTextures.get(defaultTexture);

		OpenGLShaderBasic.TextureInfoStruct texInfoStruct = new OpenGLShaderBasic.TextureInfoStruct(currentTexture.getTextureUnit(), currentTexture.computeSubtexture(textureInfo.textureOffset, textureInfo.textureSize), overrideTextureColor);

		// use shader program
		glUseProgram(basicShader.getProgramID());

		glActiveTexture(currentTexture.getTextureUnit());
		glBindTexture(GL_TEXTURE_2D, currentTexture.getTextureID());
		basicShader.fillUniformData(textureInfo.textureColor.getAsArray(), circleInfo, texInfoStruct);

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
	public void drawLine(Vector2D from, Vector2D to, float width, Color color) {
		if (width <= 0)
			return;

		Vector2D directionVector = Vector2D.subtract(to, from);

		beginDraw(squareVAO, squareVBO, squareIBO, null, new TextureInfo("default", color), false);

		// transforms
		glTranslatef(from.getX() + directionVector.getX() / 2, from.getY() + directionVector.getY() / 2, 0); // translate
		glRotatef(directionVector.getAngle() / (float)Math.PI * 180, 0, 0, 1f); // rotate around z axis
		glScalef(directionVector.getMagnitude(), width , 1); // scale

		glDrawArrays(GL_QUADS, 0, 4);

		endDraw();
	}

	@Override
	public void drawRectangle(Vector2D center, Vector2D size, float rotationAngle, TextureInfo textureInfo) {
		beginDraw(squareVAO, squareVBO, squareIBO, null, textureInfo, false);

		// transforms
		applyTransformMatrix(size, rotationAngle, center);

		glDrawArrays(GL_QUADS, 0, 4);

		endDraw();
	}

	@Override
	public void drawRectangle(Rectangle rectangle, float rotationAngle, TextureInfo textureInfo) {
		drawRectangle(Vector2D.center(rectangle.topLeft, rectangle.bottomRight), Vector2D.subtract(rectangle.topLeft, rectangle.bottomRight).absolutize(), rotationAngle, textureInfo);
	}

	@Override
	public void drawCircle(float radius, Vector2D center, TextureInfo textureInfo) {
		beginDraw(squareVAO, squareVBO, squareIBO, new OpenGLShaderBasic.CircleInfoStruct(0f, 0.5f, 4f), textureInfo, false);

		// transforms
		applyTransformMatrix(new Vector2D(radius * 2, radius * 2), null, center);

		glDrawArrays(GL_QUADS, 0, 4);

		endDraw();
	}

	@Override
	public void drawCircle(float minRadius, float maxRadius, float maxAngle, Vector2D center, TextureInfo textureInfo) {
		float unitMinRadius = minRadius / (2 * maxRadius);
		beginDraw(squareVAO, squareVBO, squareIBO, new OpenGLShaderBasic.CircleInfoStruct(unitMinRadius, 0.5f, maxAngle), textureInfo, false);

		// transforms
		applyTransformMatrix(new Vector2D(maxRadius * 2, maxRadius * 2), null, center);

		glDrawArrays(GL_QUADS, 0, 4);

		endDraw();
	}

	@Override
	public void drawString(Font font, String text, Vector2D position, Vector2D scale, float rotation, Color color, StringAlignment alignment) {
		beginDraw(fontVAO, fontVBO, fontIBO, null, new TextureInfo(font.getTextureName(), color), true);

		if (scale == null)
			scale = new Vector2D(0.66f, 0.66f);

		Font.FontVertices fontVertices = font.generateVertices(text, scale);

		// do transforms
		// text alignment is funky
		// x position is the leftmost pixel of the text
		// y position is the text baseline
		Vector2D alignedPosition = new Vector2D(position);
		switch (alignment) {
			case CENTER:
				alignedPosition.subtract(new Vector2D(fontVertices.size.getX() / 2 * scale.getX(), (fontVertices.size.getY() / 2 - fontVertices.baseline) * scale.getY()));
				break;
		}
		applyTransformMatrix(scale, rotation, alignedPosition);

		Vertex[][] splitVertices = Vertex.splitArrayByLength(fontVertices.vertices, fontMaxVertices);
		for (Vertex[] subVertices : splitVertices) {
			FloatBuffer verticesBuffer = Vertex.verticesToBuffer(subVertices);

			// upload new data
			glBufferSubData(GL_ARRAY_BUFFER, 0, verticesBuffer);

			// draw
			glDrawArrays(GL_QUADS, 0, subVertices.length);
		}

		endDraw();
	}
	@Override
	public void drawString(Font font, String text, Vector2D position, Vector2D scale, float rotation, Color color) {
		drawString(font, text, position, scale, rotation, color, StringAlignment.TOPLEFT);
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
