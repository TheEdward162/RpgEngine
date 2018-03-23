package com.edwardium.RPGEngine.Renderer.Animation;

public abstract class Animation {

	protected float currentTime = 0f;
	protected final float length;

	protected boolean running = false;
	public boolean jumpToZero = false;
	public boolean loops = false;

	protected Animation(float length) {
		this.length = length;
	}

	public void run() {
		this.currentTime = 0;
		this.running = true;
	}

	public void stop() {
		this.running = false;
	}

	public void jumpToStart() {
		this.currentTime = 0;
	}

	public void jumpToEnd() {
		this.currentTime = this.length;
	}

	public void update(float elapsedTime) {
		if (!running)
			return;

		float newTime = currentTime + elapsedTime;
		if (newTime >= length) {
			running = false;
			if (loops) {
				newTime -= length;
				running = true;
			} else if (jumpToZero) {
				newTime = 0;
			} else {
				newTime = length;
			}
		}
		this.currentTime = newTime;
	}

	public void reset() {
		this.stop();
		this.jumpToStart();
	}
}
