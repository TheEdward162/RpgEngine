package com.edwardium.RPGEngine.Renderer.Animation;

import com.edwardium.RPGEngine.Renderer.Color;

import java.util.ArrayList;

public class ColorAnimation extends Animation {
	private class ColorStop {
		public final Color color;
		public final float u;

		public ColorStop(Color color, float u) {
			this.color = color;
			this.u = u;
		}
	}

	private ArrayList<ColorStop> colorStops;

	public ColorAnimation(float length) {
		super(length);

		this.colorStops = new ArrayList<>();
	}

	public ColorAnimation addColorStop(Color color, float u) {
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

		float relativeU = (currentTime - length * previousStop.u);
		float denominator = (nextStop.u * length - previousStop.u * length);
		if (denominator == 0)
			relativeU = 0;
		else
			relativeU /= denominator;

		return Color.interpolate(previousStop.color, nextStop.color, relativeU);
	}
}
