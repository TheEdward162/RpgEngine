package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile;

import com.edwardium.RPGEngine.Control.SceneController.PlaySceneController;
import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Light;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Utility.Rectangle;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;

public class DestroyerProjectile extends GameProjectile {
	private static final Color color = new Color(1f, 0.502f, 0f);

	public DestroyerProjectile(Vector2D position, Vector2D velocity) {
		super(position, "Destroyer Projectile", velocity);

		this.hitbox = new GameHitbox(new Rectangle(new Vector2D(-13f, -4.5f), new Vector2D(13f, 4.5f)));
		this.maximumDistance = 9000f; // around 200 meters irl

		this.damage = 50f;
	}

	public DestroyerProjectile(JsonObject sourceObj) {
		this(Vector2D.fromJSON(sourceObj.getJsonObject("position")), Vector2D.fromJSON(sourceObj.getJsonObject("velocity")));
		super.membersFromJson(sourceObj);
	}

	@Override
	protected Vector2D calculateResistanceForce(float environmentDensity) {
		return new Vector2D();
	}

	@Override
	public void collideWith(GameObject other, Vector2D mySideNormal, Vector2D otherSideNormal) {
		super.collideWith(other, mySideNormal, otherSideNormal);

		if (other instanceof GameWall) {
			this.toDelete = true;
		}
	}

	@Override
	public void updatePhysics(float elapsedTime, float environmentDensity) {
		super.updatePhysics(elapsedTime, environmentDensity);
	}

	@Override
	public void updateLights(PlaySceneController gsc) {
		gsc.registerLight(new Light(this.position, color, 25f, 0f));
		super.updateLights(gsc);
	}

	@Override
	public void render(Renderer gameRenderer) {
		if (isDrawn) {
			gameRenderer.drawRectangle(new Renderer.RenderInfo(this.position, new Vector2D(23f, 9f), this.velocity.getAngle(),
					color, true));
		}
		super.render(gameRenderer);
	}

	public JsonObject toJSON() {
		return super.toJSONBuilder().add_optional("damage", damage, 50f).build();
	}
}
