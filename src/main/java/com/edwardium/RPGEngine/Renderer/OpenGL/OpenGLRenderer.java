package com.edwardium.RPGEngine.Renderer.OpenGL;

import com.edwardium.RPGEngine.Renderer.*;
import com.edwardium.RPGEngine.Utility.Rectangle;
import com.edwardium.RPGEngine.Utility.Vector2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
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

	// shape buffers
	private int shapeVAO;
	private int shapeVBO;
	private int shapeIBO;

	// frame buffer for shadow pass
	private int shadowFBO;

	// in case we get a string that is longer, we will either need to
	// split it and render is separately, or, even better, ignore it and
	// leave the only trace of the reason here in this comment
	private final int fontMaxCharacters = 10;
	private final int fontMaxVertices = fontMaxCharacters * 4;

	// after this, we will need to split
	private final int shapeMaxVertices = 16;

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
		//glClearColor(0.098f, 0.098f, 0.439f, 0.0f);
		glClearColor(Color.DARKGREY.R(), Color.DARKGREY.G(), Color.DARKGREY.B(), 1f);

		setupPrimitives();
		setupShaders();
		setupTextures();
		setupFonts();
		setupShapes();
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

		//glEnable(GL_STENCIL_TEST);
	}

	private void setupTextures() {
		gameTextures = new HashMap<>();
		gameTextures.put("default", new OpenGLTexture("Assets/Textures/default.png", GL_TEXTURE0));
		gameTextures.put("debug", new OpenGLTexture("Assets/Textures/debug1.png", GL_TEXTURE0));

		gameTextures.put("editor", new OpenGLTexture("Assets/Textures/editor.png", GL_TEXTURE0));
		gameTextures.put("sheet1", new OpenGLTexture("Assets/Textures/sheet1.png", GL_TEXTURE0));

		gameTextures.put("initsplash", new OpenGLTexture("Assets/splash.png", GL_TEXTURE0));

		glEnable(GL_TEXTURE_2D);
	}

	private void setupFonts() {
		String fontPath = "Assets/Fonts/NotoSans-Medium.ttf";
		basicFont = new OpenGLFont(fontPath, 30);

		fontVAO = glGenVertexArrays();
		glBindVertexArray(fontVAO);

		fontVBO = createVBO(new Vertex[fontMaxVertices], GL_DYNAMIC_DRAW);

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

	private void setupShapes() {
		shapeVAO = glGenVertexArrays();
		glBindVertexArray(shapeVAO);

		shapeVBO = createVBO(new Vertex[shapeMaxVertices], GL_DYNAMIC_DRAW);

		glBindVertexArray(0);

		// we can create a static indices buffer, because the indices never change, same as in fonts
		int[] indices = new int[fontMaxCharacters];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = i;
		}
		shapeIBO = createIBO(indices, GL_STATIC_DRAW);
	}

	private void setupStages() {
		shadowFBO = glGenFramebuffers();
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
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

		glViewport(0, 0, windowWidth, windowHeight);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		Rectangle viewport = new Rectangle(new Vector2D(-windowWidth / 2f, - windowHeight / 2f), new Vector2D(windowWidth / 2f, windowHeight / 2f));

		glOrtho(viewport.getTopLeft().getX(), viewport.getBottomRight().getX(), viewport.getBottomRight().getY(), viewport.getTopLeft().getY(), 0.0f, 1.0f);

		// for lights
		drawShape(Vertex.shapeFromVector2D(new Vector2D[] {
				viewport.getTopLeft(),
				viewport.getBottomLeft(),
				viewport.getBottomRight(),
				viewport.getTopRight()
		}), new RenderInfo(null, 1f, 0f, new TextureInfo("default", Color.GREY), true));
	}

	@Override
	public void afterLoop() {
		// swap buffers
		glfwSwapBuffers(window);

		// Poll for window events.
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

	private void beginDraw(int vao, int vbo, int ibo, RenderInfo info, OpenGLShaderBasic.CircleInfoStruct circleInfo, boolean overrideTextureColor) {
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
		if (info.textureInfo.textureName != null && gameTextures.containsKey(info.textureInfo.textureName))
			currentTexture = gameTextures.get(info.textureInfo.textureName);
		else
			currentTexture = gameTextures.get(defaultTexture);

		OpenGLShaderBasic.TextureInfoStruct texInfoStruct = new OpenGLShaderBasic.TextureInfoStruct(currentTexture.getTextureUnit(),
				currentTexture.computeSubtexture(info.textureInfo.textureOffset, info.textureInfo.textureSize), overrideTextureColor);

		// use shader program
		glUseProgram(basicShader.getProgramID());

		glActiveTexture(currentTexture.getTextureUnit());
		glBindTexture(GL_TEXTURE_2D, currentTexture.getTextureID());
		basicShader.fillUniformGlobalColor(info.textureInfo.textureColor);
		basicShader.fillUniformCircleInfo(circleInfo);
		basicShader.fillUnitformTextureInfo(texInfoStruct);
		basicShader.fillUniformUseLights(info.useLights);

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
	public void drawLine(Vector2D to, RenderInfo info) {
		if (info.scale.getX() <= 0)
			return;

		Vector2D directionVector = Vector2D.subtract(to, info.position);

		beginDraw(squareVAO, squareVBO, squareIBO, info, null, false);

		// transforms
		glTranslatef(info.position.getX() + directionVector.getX() / 2, info.position.getY() + directionVector.getY() / 2, 0); // translate
		glRotatef(directionVector.getAngle() / (float)Math.PI * 180, 0, 0, 1f); // rotate around z axis
		glScalef(directionVector.getMagnitude(), info.scale.getX() , 1); // scale

		glDrawArrays(GL_QUADS, 0, 4);

		endDraw();
	}

	@Override
	public void drawRectangle(RenderInfo info) {
		beginDraw(squareVAO, squareVBO, squareIBO, info, null, false);

		// transforms
		applyTransformMatrix(info.scale, info.rotation, info.position);

		glDrawArrays(GL_QUADS, 0, 4);

		endDraw();
	}

	@Override
	public void drawRectangle(Rectangle rectangle, RenderInfo info) {
		drawRectangle(new RenderInfo(Vector2D.center(rectangle.getTopLeft(), rectangle.getBottomRight()),
				Vector2D.subtract(rectangle.getTopLeft(), rectangle.getBottomRight()).absolutize(),
				info.rotation, info.textureInfo, info.useLights));
	}

	@Override
	public void drawCircle(RenderInfo info) {
		beginDraw(squareVAO, squareVBO, squareIBO, info, new OpenGLShaderBasic.CircleInfoStruct(0f, 0.5f, 4f), false);

		// transforms
		applyTransformMatrix(new Vector2D(info.scale.getX() * 2, info.scale.getY() * 2), null, info.position);

		glDrawArrays(GL_QUADS, 0, 4);

		endDraw();
	}

	@Override
	public void drawCircle(float minRadius, float maxRadius, float maxAngle, RenderInfo info) {
		float unitMinRadius = minRadius / (2 * maxRadius);
		beginDraw(squareVAO, squareVBO, squareIBO, info, new OpenGLShaderBasic.CircleInfoStruct(unitMinRadius, 0.5f, maxAngle), false);

		// transforms
		applyTransformMatrix(new Vector2D(maxRadius * 2, maxRadius * 2), null, info.position);

		glDrawArrays(GL_QUADS, 0, 4);

		endDraw();
	}

	@Override
	public void drawString(Font font, String text, RenderInfo info, StringAlignment alignment) {
		if (text.isEmpty())
			return;

		beginDraw(fontVAO, fontVBO, fontIBO, new RenderInfo(info.position, info.scale, info.rotation, new TextureInfo(font.getTextureName(), info.textureInfo.textureColor), info.useLights), null, true);

		Font.FontVertices fontVertices = font.generateVertices(text, info.scale.scale(0.66f));

		// do transforms
		// text alignment is funky
		// x position is the leftmost pixel of the text
		// y position is the text baseline
		Vector2D alignedPosition = new Vector2D(info.position);
		switch (alignment) {
			case TOPLEFT:
				alignedPosition.subtract(0, -fontVertices.baseline);
				break;
			case TOPRIGHT:
				alignedPosition.subtract(fontVertices.size.getX() * info.scale.getX(), -fontVertices.baseline);
				break;
			case CENTER:
				alignedPosition.subtract(fontVertices.size.getX() / 2 * info.scale.getX(),
						(fontVertices.size.getY() / 2 - fontVertices.baseline) * info.scale.getY());
				break;
			case BOTTOMLEFT:
				break;
			case BOTTOMRIGHT:
				alignedPosition.subtract(fontVertices.size.getX() * info.scale.getX(), 0);
				break;
		}
		applyTransformMatrix(info.scale, info.rotation, alignedPosition);

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
	public void drawString(Font font, String text, RenderInfo info) {
		drawString(font, text, info, StringAlignment.BOTTOMLEFT);
	}

	@Override
	public void drawShape(Vertex[] shape, RenderInfo info) {
		beginDraw(shapeVAO, shapeVBO, shapeIBO, info, null, true);

		applyTransformMatrix(null, info.rotation, info.position);

		Vertex[][] splitVertices = Vertex.splitArrayByLength(shape, shapeMaxVertices);
		for (Vertex[] subVertices : splitVertices) {
			FloatBuffer verticesBuffer = Vertex.verticesToBuffer(subVertices);

			// upload new data
			glBufferSubData(GL_ARRAY_BUFFER, 0, verticesBuffer);

			// draw
			glDrawArrays(GL_TRIANGLE_FAN, 0, subVertices.length);
		}

		endDraw();
	}

	@Override
	public void setLight(int index, Light light) {
		basicShader.fillUniformLightInfo(index, light);
	}

	@Override
	public void setLightCount(int count) {
		basicShader.fillUniformLightCount(count);
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

	private static int createVBO(Vertex[] vertices, int vboFlags) {
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

	private static int createIBO(int[] indices, int iboFlags) {
		int iboID = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, (IntBuffer) BufferUtils.createIntBuffer(indices.length).put(indices).flip(), iboFlags);

		return iboID;
	}
}
