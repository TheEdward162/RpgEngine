package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile;

import com.edwardium.RPGEngine.Control.SceneController.PlaySceneController;
import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Utility.Rectangle;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;

public class PistolBullet extends GameProjectile {

	public PistolBullet(Vector2D position, Vector2D velocity) {
		super(position, "Pistol Bullet", velocity);

		this.maximumDistance = 4500f; // around 100 meters irl
		this.hitbox = new GameHitbox(new Rectangle(new Vector2D(-5f, -2.25f), new Vector2D(5f, 2.25f)));

		this.damage = 5f;
	}

	public PistolBullet(JsonObject sourceObj) {
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
	public void updateLights(PlaySceneController gsc) {
		//gsc.registerLight(new Light(this.position, new Color(0.4f, 0.4f, 0.4f, 1.0f), 50f));
		super.updateLights(gsc);
	}

	@Override
	public void render(Renderer gameRenderer) {
		if (isDrawn) {
			gameRenderer.drawRectangle(new Renderer.RenderInfo(this.position, new Vector2D(10f, 4.5f), this.velocity.getAngle(), Color.DARKGREY, true));
		}
		super.render(gameRenderer);
	}

	public JsonObject toJSON() {
		return super.toJSONBuilder().add_optional("damage", damage, 5f).build();
	}
}
