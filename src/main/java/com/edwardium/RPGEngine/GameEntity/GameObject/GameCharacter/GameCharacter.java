package com.edwardium.RPGEngine.GameEntity.GameObject.GameCharacter;

import com.edwardium.RPGEngine.GameEntity.GameAI.GameAI;
import com.edwardium.RPGEngine.GameEntity.GameAI.PlayerAI;
import com.edwardium.RPGEngine.GameEntity.GameInventory;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.GameItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameItem.IGameUsableItem;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameObject;
import com.edwardium.RPGEngine.GameEntity.GameObject.GameWall;
import com.edwardium.RPGEngine.Renderer.Color;
import com.edwardium.RPGEngine.Renderer.Renderer;
import com.edwardium.RPGEngine.Renderer.TextureInfo;
import com.edwardium.RPGEngine.Vector2D;

public class GameCharacter extends GameObject {
	public enum CharacterRelationship { NONE, FRIENDLY, NEUTRAL, ENEMY }

	public enum CharacterFaction {
		NONE(0b0), PLAYER(0b1), NEUTRAL(0b10), TRIANGLEHEADS(0b100), BUBBLENOSES(0b1000);

		private final int factionID;
		CharacterFaction(int factionID) {
			this.factionID = factionID;
		}

		public int getFactionID() {
			return this.factionID;
		}

		public static boolean hasFaction(int factionFlag, CharacterFaction faction) {
			if (faction == NONE)
				return factionFlag == 0;

			return (factionFlag & faction.factionID) != 0;
		}

		public static int addFaction(int factionFlag, CharacterFaction faction) {
			return factionFlag | faction.factionID;
		}

		public static int removeFaction(int factionFlag, CharacterFaction faction) {
			return factionFlag & (~faction.factionID);
		}

		public static int toggleFaction(int factionFlag, CharacterFaction faction) {
			return factionFlag ^ faction.factionID;
		}

		public static CharacterRelationship getRelationship(int factionFlagA, int factionFlagB) {
			// Table of relationships: F - friendly, N - neutral, E - enemy, D - dynamic
			// 		 			PLAYER	NEUTRAL	TRIANGLEHEADS	BUBBLENOSES
			// PLAYER			  X		   F		 D				 D
			// NEUTRAL			  F		   F		 N				 N
			// TRIANGLEHEADS	  D		   N		 F				 E
			// BUBBLENOSES		  D		   N		 E				 F
			//
			// Also enemy status is stronger than friend status
			// so being in both aggressive factions at once makes you
			// an enemy of both

			if (hasFaction(factionFlagA, NONE)) {
				return CharacterRelationship.NONE;
			}

			if (hasFaction(factionFlagA, NEUTRAL)) {
				if (hasFaction(factionFlagB, NEUTRAL) || hasFaction(factionFlagB, PLAYER))
					return CharacterRelationship.FRIENDLY;
				else
					return CharacterRelationship.NEUTRAL;
			}

			if (hasFaction(factionFlagA, PLAYER)) {
				boolean hasTriang = hasFaction(factionFlagB, TRIANGLEHEADS);
				boolean hasBubble = hasFaction(factionFlagB, BUBBLENOSES);

				if (hasTriang && hasBubble) {
					// forced ally :D
					return CharacterRelationship.FRIENDLY;
				} else if (hasTriang) {
					// TODO: based on dynamic status
					return CharacterRelationship.ENEMY;
				} else if (hasBubble) {
					// TODO: based on dynamic status
					return CharacterRelationship.ENEMY;
				}
			}

			if (hasFaction(factionFlagA, TRIANGLEHEADS)) {
				if (hasFaction(factionFlagB, TRIANGLEHEADS)) {
					return CharacterRelationship.FRIENDLY;
				} else if (hasFaction(factionFlagB, BUBBLENOSES)) {
					return CharacterRelationship.ENEMY;
				}
			}

			if (hasFaction(factionFlagA, BUBBLENOSES)) {
				if (hasFaction(factionFlagB, BUBBLENOSES)){
					return CharacterRelationship.FRIENDLY;
				}
			}

			// relationships are symmetrical
			return getRelationship(factionFlagB, factionFlagA);
		}
	}

	public float maxWalkSpeed = 350f;
	private Vector2D walkVector = new Vector2D(0, 0);

	public GameInventory inventory;
	public GameAI ai;
	public int factionFlag = 0;

	public float maxHeath = 100f;
	public float health = 100f;

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
		this.ai = new PlayerAI(this);
	}

	@Override
	public GameObject rotateBy(float angle) {
		if (this.ai.canRotate())
			super.rotateBy(angle);

		return this;
	}

	@Override
	public GameObject rotateTo(float angle) {
		if (this.ai.canRotate())
			super.rotateTo(angle);

		return this;
	}

	@Override
	public GameObject rotateToward(boolean left) {
		if (this.ai.canRotate())
			super.rotateToward(left);

		return this;
	}

	@Override
	public boolean rotateToPoint(Vector2D target) {
		return this.ai.canRotate() && super.rotateToPoint(target);
	}

	public void walkTo(Vector2D target) {
		this.walkVector = Vector2D.subtract(target, this.position).limit(maxWalkSpeed);
	}

	public void walkTowards(Vector2D direction) {
		this.walkVector = new Vector2D(direction).setMagnitude(maxWalkSpeed);
	}

	public boolean useActiveItem(Vector2D to, GameCharacter at) {
		GameItem activeItem = inventory.getActiveItem() ;
		if (activeItem != null && activeItem instanceof IGameUsableItem) {
			return ((IGameUsableItem)activeItem).use(this, to, at);
		}

		return false;
	}

	@Override
	public void update(float elapsedTime, float environmentDensity) {
		// if we aren't walking anywhere, we might want to. like, you know, stop
		// if we can...
		if (this.walkVector.getMagnitude() == 0 || (!ai.canWalk() && ai.canWalkStop())) {
			this.walkVector = new Vector2D(this.velocity).inverse().limit(maxWalkSpeed);

			if (ai.canWalkStop())
				this.applyForce(this.walkVector);
		} else {
			// when walking, we want to first cancel out all the velocity we have that is not in the right direction
			// before adding force in that direction
			float remainingWalkSpeed = this.maxWalkSpeed;
			Vector2D walkVectorRejection = this.velocity.rejection(this.walkVector);

			walkVectorRejection.limit(remainingWalkSpeed).inverse();
			remainingWalkSpeed -= walkVectorRejection.getMagnitude();

			if (ai.canWalk()) {
				this.applyForce(walkVectorRejection);
			}

			// also we can't walk faster than maxWalkSpeed globally
			float currentSpeedInWalkDirection = this.velocity.projection(this.walkVector).getMagnitude();
			float maxAllowedWalkSpeed = Math.max(0, remainingWalkSpeed - currentSpeedInWalkDirection);

			this.walkVector.limit(maxAllowedWalkSpeed);

			if (ai.canWalk())
				this.applyForce(this.walkVector);
		}
		this.walkVector.set(0, 0);

		this.ai.onUpdate(elapsedTime);

		super.update(elapsedTime, environmentDensity);
	}

	@Override
	public void render(Renderer gameRenderer, boolean drawHitbox) {
		if (isDrawn) {
			// shadow
			gameRenderer.drawCircle(25f, this.position, new TextureInfo("default", new Color(0f, 0f, 0f, 0.3f)));

			// body
			gameRenderer.drawRectangle(this.position, new Vector2D(15, 25), this.getFacingDirection().getAngle(), new TextureInfo("default", new Color(1f, 1f, 0f, 1f)));

			// facing direction
			gameRenderer.drawLine(this.position, Vector2D.add(this.position, this.getFacingDirection().setMagnitude(30)), 2f, new Color(1f, 0f, 0f, 1f));

			// draw held item
			GameItem activeItem = this.inventory.getActiveItem();
			if (activeItem != null) {
				TextureInfo activeItemTexture = activeItem.getHeldTexture();
				if (activeItemTexture != null) {
					gameRenderer.drawRectangle(Vector2D.add(this.position, this.getFacingDirection().setMagnitude(30)), activeItem.getHeldSize(), this.rotation, activeItemTexture);
				}
			}

			// name and HP
			gameRenderer.drawString(gameRenderer.basicFont, this.name, new Vector2D(30, -30).add(this.position), null, 0, new Color(0f, 1f, 0f, 1f));

			String healthString = Math.round(health) + " / " + Math.round(maxHeath);
			gameRenderer.drawString(gameRenderer.basicFont, healthString, new Vector2D(50, -10).add(this.position), null, 0, new Color(0f, 1f, 0f, 1f));
		}

		super.render(gameRenderer, drawHitbox);
	}

	@Override
	public void collideWith(GameObject other, Vector2D otherSideNormal) {
		if (other instanceof GameWall) {
			// push out of the wall
			this.position.subtract(otherSideNormal);

			// cancel out velocity in that direction
			Vector2D wallRejection = this.velocity.rejection(otherSideNormal.getNormal());
			if (wallRejection.angleBetween(otherSideNormal) == 0) {
				this.velocity.subtract(wallRejection);
				this.walkVector.set(0, 0);
			}
		}
	}

	public void damage(float damage) {
		this.health = Math.max(0f, this.health - damage);

		if (this.health <= 0)
			this.ai.currentState = GameAI.CharacterState.LOCKED;
	}
}
