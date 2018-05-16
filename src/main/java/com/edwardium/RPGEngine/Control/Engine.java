package com.edwardium.RPGEngine.Control;

import com.edwardium.RPGEngine.Control.SceneController.*;
import com.edwardium.RPGEngine.IO.Config;
import com.edwardium.RPGEngine.IO.Input;
import com.edwardium.RPGEngine.Renderer.Animation.ColorAnimation;
import com.edwardium.RPGEngine.Renderer.Animation.FusedAnimation;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.OpenGL.OpenGLRenderer;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Utility.FPSCounter;

import java.util.Random;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_G;

public class Engine implements Runnable {
	public enum SceneControllerType { INITSPLASH, GAME, EDITOR, MENU, QUIT }

	// in seconds
	public static final float UPDATE_CAP = 1.0f / 60.0f;
	// nanoseconds * NANO_TIME_MULT = seconds
	public static final double NANO_TIME_MULT = 1e-9;

	public static final float PIXEL_TO_METER = 1.0f / 50.0f;

	public static Engine gameEngine = new Engine();

	public Random randomGenerator;

	// Game objects
	private Thread gameThread;
	private Renderer gameRenderer;
	private Input gameInput;
	private Config gameConfig;

	private boolean running = false;
	private SceneController lastSceneController = null;
	private SceneController currentSceneController;

	private Engine() {
		if (gameEngine != null)
			gameEngine.cleanup();
		gameEngine = this;

		randomGenerator = new Random();
	}

	public void start() {
		if (gameThread != null) {
			return;
		}

		gameThread = new Thread(this);
		gameThread.run();
	}

	public void run() {
		if (running) {
			return;
		}

		FPSCounter.init(50);

		// load config
		gameConfig = new Config("config.ini");

		// initialize game renderer based on config values
		switch (gameConfig.getString("renderer", "opengl").toLowerCase()) {
			default: // OpenGL
				gameRenderer = new OpenGLRenderer(gameConfig.getString("window-title", "RPGEngine"),
						gameConfig.getInt("window-width", 800),
						gameConfig.getInt("window-height", 600));
				break;
		}

		gameInput = new Input(gameRenderer.getWindowHandle());
		gameInput.watchKey(GLFW_KEY_G);

		gameInput.setGameCursorCenter(gameRenderer.getWindowSize().divide(2));

		changeSceneController(SceneControllerType.GAME);

//		changeSceneController(SceneControllerType.MENU);
//		changeSceneController(SceneControllerType.INITSPLASH);

		gameRenderer.setVSync(true);
		gameRenderer.show();

		// start game loop
		running = true;
		loopUnsynced();

//		if (currentSceneController instanceof PlaySceneController) {
//			((PlaySceneController) currentSceneController).saveState("save.json");
//		}

		// Game is over
		cleanup();
	}

	// TODO: Integrate FPS cap
//	private void loop() {
//		// whether to render this tick or skip rendering because nothing has updated
//		boolean doRender = false;
//
//		// time that is now
//		double nowTime;
//		// last time the loop was run
//		double lastTime = System.nanoTime();
//		// how much time has elapsed since last time
//		// divide by UPDATE_CAP to see how many updates we have missed
//		double unprocessedTime = 0;
//
//		while (running) {
//			if (gameRenderer.shouldClose()) {
//				running = false;
//				break;
//			}
//
//			nowTime = System.nanoTime();
//
//			double timeSinceLastFrame = nowTime - lastTime;
//			unprocessedTime += timeSinceLastFrame;
//
//			lastTime = nowTime;
//
//			// only updatePhysics input once, even if we do more game updates
//			if (unprocessedTime >= UPDATE_CAP / NANO_TIME_MULT) {
//				FPSCounter.updatePhysics(unprocessedTime);
//				updateInput(unprocessedTime / 2);
//				System.err.println(unprocessedTime);
//			}
//
//			// updatePhysics while we are all caught up
//			while (unprocessedTime >= UPDATE_CAP / NANO_TIME_MULT) {
//				if (gameRenderer.shouldClose())
//					break;
//
//				updatePhysics(UPDATE_CAP * timeFactor);
//				unprocessedTime -= UPDATE_CAP / NANO_TIME_MULT;
//
//				// Since we updated, we also want to render
//				doRender = true;
//			}
//
//			if (doRender) {
//				render();
//			} else {
//				// sleep for a millisecond, catch interrupts and do literally nothing with them just 'cause
//				try {
//					Thread.sleep(1);
//				} catch (InterruptedException ignored) {
//
//				}
//			}
//		}
//	}

	private void loopUnsynced() {
		// time that is now
		double nowTime;
		// last time the loop was run
		double lastTime = System.nanoTime();
		// how much time has elapsed since last time
		double unprocessedTime;

		while (running) {
			if (gameRenderer.shouldClose()) {
				running = false;
				break;
			}

			nowTime = System.nanoTime();
			unprocessedTime = nowTime - lastTime;
			lastTime = nowTime;

			FPSCounter.update(unprocessedTime);
			// System.err.println(FPSCounter.getFPS());

			gameInput.setGameCursorCenter(gameRenderer.getWindowSize().divide(2));

			update(unprocessedTime);
			render();
		}
	}

	private void update(double unprocessedTime) {
		if (gameInput.getWatchedKeyJustPressed(GLFW_KEY_G, unprocessedTime)) {
			Engine.gameEngine.toggleVSync();
		}

		currentSceneController.update(unprocessedTime);
	}

	private void render() {
		gameRenderer.beforeLoop();

		currentSceneController.render(gameRenderer);

		gameRenderer.drawString(gameRenderer.basicFont, "VSYNC: " + (gameRenderer.getVSync() ? "ON" : "OFF"),
				new Renderer.RenderInfo(gameRenderer.getWindowSize().divide(2).scale(-1, 1).add(5, -20),1f, 0f, new Color(), false));

		gameRenderer.drawString(gameRenderer.basicFont, "FPS: " + String.format("%.1f", FPSCounter.getFPS()),
				new Renderer.RenderInfo(gameRenderer.getWindowSize().divide(2).scale(-1, 1).add(5, -5), 1f, 0f, new Color(1f, 0f, 0f), false));

		gameRenderer.afterLoop();
	}

	public boolean getVSync() {
		return gameRenderer.getVSync();
	}
	public void toggleVSync() {
		boolean currentVSync = gameRenderer.getVSync();
		gameRenderer.setVSync(!currentVSync);
	}

	public PlaySceneController getCurrentPlayController() {
		if (currentSceneController instanceof PlaySceneController)
			return (PlaySceneController) currentSceneController;
		else
			return null;
	}

	public void changeSceneController(SceneControllerType type) {
		if (lastSceneController != null)
			lastSceneController.cleanup();

		if (currentSceneController != null) {
			currentSceneController.freeze();
			lastSceneController = currentSceneController;
		}

		switch (type) {
			case INITSPLASH:
				ColorAnimation colorAnim = new ColorAnimation(2f);
				colorAnim.addColorStop(new Color(0, 0, 0), 0f);
				colorAnim.addColorStop(new Color(), 0.5f);
				colorAnim.addColorStop(new Color(0, 0, 0), 1f);

				FusedAnimation anim = new FusedAnimation(5, 0, new TextureInfo("initsplash"), null, colorAnim);
				currentSceneController = new SplashSceneController(gameInput, anim, 2f);
				break;
			case GAME:
				currentSceneController = new PlaySceneController(gameInput);
				((PlaySceneController) currentSceneController).reloadScene();
				break;
			case EDITOR:
				currentSceneController = new EditorSceneController(gameInput);
				break;
			case MENU:
				currentSceneController = new MenuSceneController(gameInput);
				break;
			case QUIT:
				running = false;
				break;
		}
	}
	public boolean restoreLastSceneController() {
		if (lastSceneController == null)
			return false;

		SceneController temp = currentSceneController;
		currentSceneController = lastSceneController;
		lastSceneController = temp;

		currentSceneController.restore();
		return true;
	}

	// dispose of all created instances and stuff
	private void cleanup() {
		currentSceneController.cleanup();
		if (lastSceneController != null)
			lastSceneController.cleanup();

		gameRenderer.cleanup();

		gameConfig.saveConfig(true);
	}
}
