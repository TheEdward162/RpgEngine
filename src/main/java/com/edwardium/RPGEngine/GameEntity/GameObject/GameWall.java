package com.edwardium.RPGEngine.GameEntity.GameObject;

import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Renderer.Vertex;
import com.edwardium.RPGEngine.Utility.Rectangle;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class GameWall extends GameObject {

	private Vector2D[] shape;
	public boolean penetrable = false;

	private final Vertex[] vertices;
	private final TextureInfo tInfo = new TextureInfo("default", new Color(0.3f, 0.3f, 0.3f, 1f));

	public GameWall(Vector2D position, Rectangle rect) {
		this(position, rect.toShape());
	}
	public GameWall(Vector2D position, Vector2D[] points) {
		super(position, "Wall");

		this.shape = points;
		this.hitbox = new GameHitbox(points);
		this.vertices = Vertex.arrayFromVector2D(points);

		this.mass = Float.POSITIVE_INFINITY;
	}

	public GameWall(JsonObject sourceObj) {
		super(new Vector2D(), "Wall");
		this.membersFromJson(sourceObj);

		this.hitbox = new GameHitbox(shape);
		this.vertices = Vertex.arrayFromVector2D(shape);

		this.mass = Float.POSITIVE_INFINITY;
	}

	@Override
	public void update(float elapsedTime, float environmentDensity) {
		super.update(elapsedTime, environmentDensity);
	}

	@Override
	public void render(Renderer gameRenderer) {
		if (isDrawn) {
			gameRenderer.drawShape(vertices, this.position, new Vector2D(1, 1), this.rotation, tInfo);
		}
		super.render(gameRenderer);
	}

	@Override
	public void collideWith(GameObject other, Vector2D mnySideNormal, Vector2D otherSideNormal) {
		other.position.subtract(mnySideNormal);

//		Vector2D rejection = other.velocity.rejection(mnySideNormal.getNormal());
//		if (rejection.angleBetween(mnySideNormal) == 0) {
//			other.velocity.subtract(rejection);
//		}
	}

	@Override
	protected GameObject membersFromJson(JsonObject sourceObj) {
		super.membersFromJson(sourceObj);

		JsonArray shapeArray = sourceObj.getJsonArray("shape");
		if (shapeArray != null) {
			Vector2D[] shape = new Vector2D[shapeArray.size()];
			for (int i = 0; i < shapeArray.size(); i++) {
				shape[i] = Vector2D.fromJSON(shapeArray.getJsonObject(i));
			}
			this.shape = shape;
		} else {
			this.shape = new Vector2D[] {};
		}

		return this;
	}

	public JsonObject toJSON() {
		return super.toJSONBuilder().add("shape", shape).add_optional("mass", mass, Float.POSITIVE_INFINITY).build();

		// TODO: Penetrable
	}
}
