package com.edwardium.RPGEngine.GameEntity.GameObject;

import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.Vertex;
import com.edwardium.RPGEngine.Utility.Rectangle;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class GameWall extends GameObject {

	private Vector2D[] shape;

	private final Vertex[] vertices;

	public GameWall(Vector2D position, Rectangle rect) {
		this(position, rect.toShape());
	}
	public GameWall(Vector2D position, Vector2D[] points) {
		super(position, "Wall");

		this.shape = points;
		this.hitbox = new GameHitbox(points);
		this.vertices = Vertex.shapeFromVector2D(points);

		this.mass = Float.POSITIVE_INFINITY;
	}

	public GameWall(JsonObject sourceObj) {
		super(new Vector2D(), "Wall");
		this.membersFromJson(sourceObj);

		this.hitbox = new GameHitbox(shape);
		this.vertices = Vertex.shapeFromVector2D(shape);

		this.mass = Float.POSITIVE_INFINITY;
	}

	@Override
	public void render(Renderer gameRenderer) {
		if (isDrawn) {
			gameRenderer.drawShape(vertices, new Renderer.RenderInfo(this.position, 1f, this.rotation, new Color(0.3f, 0.3f, 0.3f, 1f), true));
		}
		super.render(gameRenderer);
	}

	@Override
	public void collideWith(GameObject other, Vector2D mySideNormal, Vector2D otherSideNormal) {
		if (other.mass > 0 && other.mass < Float.POSITIVE_INFINITY)
			other.position.subtract(mySideNormal);

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
	}
}
