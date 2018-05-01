package com.edwardium.RPGEngine.GameEntity.GameObject;

import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Utility.Rectangle;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;

public class GameWall extends GameObject {

	private Rectangle shape;
	public boolean penetrable = false;

	public GameWall(Vector2D position, Rectangle shape) {
		super(position, "Wall");

		this.shape = shape;
		this.hitbox = new GameHitbox(shape);

		this.mass = Float.POSITIVE_INFINITY;
	}

	public GameWall(JsonObject sourceObj) {
		this(Vector2D.fromJSON(sourceObj.getJsonObject("position")), Rectangle.fromJSON(sourceObj.getJsonObject("shape")));
		this.membersFromJson(sourceObj);
	}

	@Override
	public void update(float elapsedTime, float environmentDensity) {
		super.update(elapsedTime, environmentDensity);
	}

	@Override
	public void render(Renderer gameRenderer, boolean drawHitbox) {
		if (isDrawn) {
			gameRenderer.drawRectangle(Rectangle.shiftBy(this.shape, this.position), this.rotation, new TextureInfo("default", new Color(0.3f, 0.3f, 0.3f, 1f)));
		}
		super.render(gameRenderer, drawHitbox);
	}

	@Override
	public void collideWith(GameObject other, Vector2D otherSideNormal) {

	}

	public JsonObject toJSON() {
		return super.toJSONBuilder().add("shape", shape.toJSON()).add_optional("mass", mass, Float.POSITIVE_INFINITY).build();

		// TODO: Penetrable
	}
}
