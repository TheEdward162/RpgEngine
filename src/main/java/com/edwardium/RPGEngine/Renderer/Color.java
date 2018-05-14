package com.edwardium.RPGEngine.Renderer;

public class Color {

	public static Color add(Color a, Color b) {
		return new Color(a).add(b);
	}

	public static Color multiply(Color a, float u) {
		return new Color(a).multiply(u);
	}

	public static Color interpolate(Color a, Color b, float u) {
		return multiply(a, 1 - u).add(multiply(b, u));
	}

	private float[] rgba;

	public Color(float r, float g, float b, float a) {
		this.rgba = new float[] { r, g, b, a };
		checkBounds();
	}

	public Color(float r, float g, float b) {
		this(r, g, b, 1);
	}

	public Color(Color copy) {
		this(copy.R(), copy.G(), copy.B(), copy.A());
	}

	public Color() {
		this(1, 1, 1);
	}

	public float[] getAsArray() {
		return new float[] { R(), G(), B(), A() };
	}

	public float R() {
		return rgba[0];
	}
	public Color setR(float value) {
		this.rgba[0] = value;
		checkBounds();

		return this;
	}

	public float G() {
		return rgba[1];
	}
	public Color setG(float value) {
		this.rgba[1] = value;
		checkBounds();

		return this;
	}

	public float B() {
		return rgba[2];
	}
	public Color setB(float value) {
		this.rgba[2] = value;
		checkBounds();

		return this;
	}

	public float A() {
		return rgba[3];
	}
	public Color setA(float value) {
		this.rgba[3] = value;
		checkBounds();

		return this;
	}

	public Color add(Color other) {
		this.setR(this.R() + other.R());
		this.setG(this.G() + other.G());
		this.setB(this.B() + other.B());
		this.setA(this.A() + other.A());

		return this;
	}

	public Color multiply(float u) {
		this.setR(this.R() * u);
		this.setG(this.G() * u);
		this.setB(this.B() * u);
		this.setA(this.A() * u);

		return this;
	}

	private void checkBounds() {
		if (this.R() < 0)
			this.setR(0f);
		else if (this.R() > 1f)
			this.setR(1f);

		if (this.G() < 0)
			this.setG(0f);
		else if (this.G() > 1f)
			this.setG(1f);

		if (this.B() < 0)
			this.setB(0f);
		else if (this.B() > 1f)
			this.setB(1f);

		if (this.A() < 0)
			this.setA(0f);
		else if (this.A() > 1f)
			this.setA(1f);
	}

	@Override
	public String toString() {
		return "rgba(" + R() + ", " + G() + ", " + B() + ", " + A() + ")";
	}
}
