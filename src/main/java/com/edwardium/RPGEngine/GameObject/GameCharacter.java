package com.edwardium.RPGEngine.GameObject;

import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class GameCharacter extends GameObject {
	protected float maxWalkSpeed = 5f;

	public GameCharacter() {
		super();
	}
	public GameCharacter(Vector2D position) {
		super(position);
	}

	public void walkTo(Vector2D target) {
		Vector2D walkVector = Vector2D.subtract(target, this.position).limit(maxWalkSpeed);
		this.applyForce(walkVector);
	}

	public void walkTowards(Vector2D direction) {
		Vector2D walkVector = new Vector2D(direction).setMagnitude(maxWalkSpeed);
		this.applyForce(walkVector);
	}

	@Override
	public void update(float elapsedTime) {
		this.position.add(this.velocity);
		this.velocity.set(0, 0);
	}

	@Override
	public void render(Renderer gameRenderer) {
		// shadow
		gameRenderer.drawCircle(50f, this.position, new float[] {0f, 0f, 0f, 0.3f}, null);

		// body
		gameRenderer.drawRectangle(this.position, new Vector2D(15, 25), this.getFacingDirection().getAngle(), new float[] {1f, 1f, 0f, 1f}, null);

		// facing direction
		gameRenderer.drawLine(this.position, Vector2D.add(this.position, this.getFacingDirection().setMagnitude(30)), 2f, new float[] { 1f, 0f, 0f, 1f });

		gameRenderer.drawString(gameRenderer.basicFont, "Hello, world!", new Vector2D(0, 100), new Vector2D(1, 1), new float[] { 0f, 1f, 0f, 1f });
	}
}
