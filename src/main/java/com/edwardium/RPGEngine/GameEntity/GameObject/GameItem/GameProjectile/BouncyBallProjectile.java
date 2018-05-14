package com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameProjectile;

import com.edwardium.RPGEngine.Control.SceneController.GameSceneController;
import com.edwardium.RPGEngine.GameEntity.GameHitbox;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter.GameCharacter;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Light;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Utility.Vector2D;

import javax.json.JsonObject;

public class BouncyBallProjectile extends GameProjectile {

	private int maximumBounces = 3;
	private int bounces = 0;

	private Color currentColor;

	public BouncyBallProjectile(Vector2D position, Vector2D velocity) {
		super(position, "Bouncy Ball of Death", velocity);

		this.hitbox = new GameHitbox(15f);
		this.maximumTime = 2f;

		this.damage = 20f;
	}

	public BouncyBallProjectile(JsonObject sourceObj) {
		this(Vector2D.fromJSON(sourceObj.getJsonObject("position")), Vector2D.fromJSON(sourceObj.getJsonObject("velocity")));
		super.membersFromJson(sourceObj);
	}

	@Override
	protected Vector2D calculateResistanceForce(float environmentDensity) {
		return new Vector2D();
	}

	@Override
	public void collideWith(GameObject other, Vector2D mySideNormal, Vector2D otherSideNormal) {
		if (other instanceof GameWall || other instanceof GameCharacter) {
			if (other instanceof GameCharacter) {
				((GameCharacter) other).damage(this.damage);
			}

			if (bounces == maximumBounces) {
				this.toDelete = true;
			} else {
				bounces++;

				if (otherSideNormal == null){
					this.velocity.inverse();
				} else {
					Vector2D collideSide = otherSideNormal.getNormal();
					Vector2D rejection = this.velocity.rejection(collideSide);
					this.velocity.subtract(rejection.multiply(2));
				}
			}
		}
	}

	@Override
	public void updateLights(GameSceneController gsc) {
		currentColor = new Color(0f, 1f, 0.502f, (maximumTime - timeTravelled) / maximumTime);
		gsc.greatestFunctionEVER(new Light(this.position, currentColor, currentColor.A() * 15f, 0f));
		super.updateLights(gsc);
	}

	@Override
	public void render(Renderer gameRenderer) {
		if (isDrawn) {
			gameRenderer.drawCircle(new Renderer.RenderInfo(this.position, 15f, 0f,  currentColor, true));
		}
		super.render(gameRenderer);
	}

	public JsonObject toJSON() {
		return super.toJSONBuilder().add_optional("damage", damage, 2f).build();
	}
}
