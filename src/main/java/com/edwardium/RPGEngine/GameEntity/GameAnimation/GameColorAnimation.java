package com.edwardium.RPGEngine.GameEntity.GameAnimation;

import com.edwardium.RPGEngine.Renderer.Color;

import java.util.ArrayList;

public class GameColorAnimation extends GameAnimation {
	private class ColorStop {
		public final Color color;
		public final float u;

		public ColorStop(Color color, float u) {
			this.color = color;
			this.u = u;
		}
	}

	private ArrayList<ColorStop> colorStops;

	public GameColorAnimation(float length) {
		super(length);

		this.colorStops = new ArrayList<>();
	}

	public GameColorAnimation addColorStop(Color color, float u) {
		this.colorStops.add(new ColorStop(color, u));

		return this;
	}

	public Color getCurrentColor() {
		ColorStop previousStop = null;
		ColorStop nextStop = null;

		for (ColorStop stop : colorStops) {
			if (previousStop == null || stop.u * length <= currentTime)
				previousStop = stop;

			if (nextStop == null) {
				nextStop = stop;
			}

			if (stop.u * length >= currentTime) {
				nextStop = stop;
				break;
			}
		}

		if (previousStop == null)
			return new Color();

		float relativeU = (currentTime - length * previousStop.u) / (nextStop.u * length - previousStop.u * length);
		return Color.interpolate(previousStop.color, nextStop.color, relativeU);
	}
}
