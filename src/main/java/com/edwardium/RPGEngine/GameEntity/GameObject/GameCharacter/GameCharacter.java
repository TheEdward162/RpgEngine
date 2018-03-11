package com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter;

import com.edwardium.RPGEngine.GameEntity.GameInventory;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class GameCharacter extends GameObject {
	public float maxWalkSpeed = 350f;
	private Vector2D walkVector = new Vector2D(0, 0);

	public GameInventory inventory;

	public GameCharacter() {
		this(new Vector2D(0, 0));
	}
	public GameCharacter(Vector2D position) {
		this(position, "", 0);
	}
	public GameCharacter(Vector2D position, String name, int inventorySize) {
		super(position);

		this.name = name;
		this.inventory = new GameInventory(inventorySize);
	}

	public void walkTo(Vector2D target) {
		this.walkVector = Vector2D.subtract(target, this.position).limit(maxWalkSpeed);
	}

	public void walkTowards(Vector2D direction) {
		this.walkVector = new Vector2D(direction).setMagnitude(maxWalkSpeed);
	}

	@Override
	public void update(float elapsedTime, float environmentDensity) {
		// if we aren't walking anywhere, we might want to. like, you know, stop
		// if we can... TODO: AI
		if (this.walkVector.getMagnitude() == 0) {
			this.walkVector = new Vector2D(this.velocity).inverse().limit(maxWalkSpeed);
		}
		this.velocity.add(this.walkVector);
		this.walkVector.set(0, 0);

		super.update(elapsedTime, environmentDensity);
	}

	@Override
	public void render(Renderer gameRenderer) {
		if (true || isDrawn) {
			// shadow
			gameRenderer.drawCircle(50f, this.position, new float[]{0f, 0f, 0f, 0.3f}, new TextureInfo("default"));

			// body
			gameRenderer.drawRectangle(this.position, new Vector2D(15, 25), this.getFacingDirection().getAngle(), new float[]{1f, 1f, 0f, 1f}, new TextureInfo("default"));

			// facing direction
			gameRenderer.drawLine(this.position, Vector2D.add(this.position, this.getFacingDirection().setMagnitude(30)), 2f, new float[]{1f, 0f, 0f, 1f});

			gameRenderer.drawString(gameRenderer.basicFont, this.name, new Vector2D(30, -30).add(this.position), new Vector2D(1, 1), new float[]{0f, 1f, 0f, 1f});
		}

		super.render(gameRenderer);
	}
}
