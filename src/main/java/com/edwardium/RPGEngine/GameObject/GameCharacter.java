package com.edwardium.RPGEngine.GameObject;

import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class GameCharacter extends GameObject {
	protected float maxWalkSpeed = 500f;
	private Vector2D walkVector = new Vector2D(0, 0);

	private String name = "";

	public GameCharacter() {
		super();
	}
	public GameCharacter(Vector2D position) {
		super(position);
	}
	public GameCharacter(Vector2D position, String name) {
		this(position);

		this.name = name;
	}

	public void walkTo(Vector2D target) {
		this.walkVector = Vector2D.subtract(target, this.position).limit(maxWalkSpeed);
	}

	public void walkTowards(Vector2D direction) {
		this.walkVector = new Vector2D(direction).setMagnitude(maxWalkSpeed);
	}

	@Override
	public void update(float elapsedTime, float velocityDiminishFactor) {
		// if we aren't walking anywhere, we might want to. like, you know, stop
		// if we can... TODO: AI
		if (this.walkVector.getMagnitude() == 0) {
			this.walkVector = new Vector2D(this.velocity).inverse().limit(maxWalkSpeed);
		}
		this.velocity.add(this.walkVector);
		this.walkVector.set(0, 0);

		this.position.add(Vector2D.multiply(this.velocity, elapsedTime));
		this.velocity.multiply(velocityDiminishFactor * elapsedTime);
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
