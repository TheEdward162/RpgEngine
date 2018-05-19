package com.edwardium.RPGEngine.Renderer;

public class Color {

	public static final Color BLACK = new Color(0, 0, 0);
	public static final Color WHITE = new Color(1, 1, 1);
	public static final Color GREY = new Color(0.5f, 0.5f, 0.5f);
	public static final Color DARKGREY = new Color(0.3f, 0.3f, 0.3f);
	public static final Color BLACKGREY = new Color(0.2f, 0.2f, 0.2f);

	public static final Color RED = new Color(1, 0, 0);
	public static final Color GREEN = new Color(0, 1, 0);
	public static final Color BLUE = new Color(0, 0, 1);

	public static final Color YELLOW = new Color(1, 1, 0);
	public static final Color CYAN = new Color(0, 1, 1);
	public static final Color PURPLE = new Color(1, 0, 1);

	public static final Color PINK = Color.fromRGB(230, 0, 58);

	/**
	 * This is a convenience function to convert from 0-255 format into float format.
	 *
	 * @param r Red component. From 0 to 255.
	 * @param g Green component. From 0 to 255.
	 * @param b Blue component. From 0 to 255.
	 * @param a Alpha component. From 0 to 255.
	 * @return New color from r, g, b and a components divided by 255.
	 */
	public static Color fromRGBA(int r, int g, int b, int a) {
		return new Color((float)r / 255f, (float)g / 255f, (float)b / 255f, (float)a / 255f);
	}

	/**
	 * This is a convenience function to convert from 0-255 format into float format.
	 * Equivalent to calling {@code Color.fromRGBA(r, g, b, 255)}
	 * @see Color#fromRGBA(int, int, int, int)
	 *
	 * @param r Red component. From 0 to 255.
	 * @param g Green component. From 0 to 255.
	 * @param b Blue component. From 0 to 255.
	 * @return New color from r, g and b components divided by 255.
	 */
	public static Color fromRGB(int r, int g, int b) {
		return fromRGBA(r, g, b, 255);
	}

	/**
	 * @param a Color a.
	 * @param b Color b.
	 * @return New color that is an addition of a to b.
	 */
	public static Color add(Color a, Color b) {
		return new Color(a).add(b);
	}

	/**
	 * @param a Color.
	 * @param m Multiplication factor.
	 * @return New color that is a multiplication of a by m.
	 */
	public static Color multiply(Color a, float m) {
		return new Color(a).multiply(m);
	}

	/**
	 * @param a Color a.
	 * @param b Color b.
	 * @param u Factor of interpolation.
	 * @return New color that is a linear interpolation between a and b with factor f.
	 */
	public static Color interpolate(Color a, Color b, float u) {
		return multiply(a, 1 - u).add(multiply(b, u));
	}

	private float[] rgba;

	public Color(float r, float g, float b, float a) {
		this.rgba = new float[] { r, g, b, a };
		checkBounds();
	}

	/**
	 * Equivalent to calling {@code new Color(r, g, b, 1)}
	 *
	 * @param r Red component.
	 * @param g Green component.
	 * @param b Blue component.
	 */
	public Color(float r, float g, float b) {
		this(r, g, b, 1);
	}

	/**
	 * @param copy Color to copy.
	 */
	public Color(Color copy) {
		this(copy.R(), copy.G(), copy.B(), copy.A());
	}

	/**
	 * Empty constructor.
	 * Equivalent to calling {@code new Color(1, 1, 1)}
	 */
	public Color() {
		this(1, 1, 1);
	}

	/**
	 * Equivalent to calling new {@code float[] { R(), G(), B(), A() }}
	 *
	 * @return Float array representing this color.
	 */
	public float[] getAsArray() {
		return new float[] { R(), G(), B(), A() };
	}

	/**
	 * @return The red component. From 0 to 1.
	 */
	public float R() {
		return rgba[0];
	}

	/**
	 * @param value New red component. From 0 to 1.
	 * @return This reference.
	 */
	public Color setR(float value) {
		this.rgba[0] = value;
		checkBounds();

		return this;
	}

	/**
	 * @return The green component. From 0 to 1.
	 */
	public float G() {
		return rgba[1];
	}

	/**
	 * @param value New green component. From 0 to 1.
	 * @return This reference.
	 */
	public Color setG(float value) {
		this.rgba[1] = value;
		checkBounds();

		return this;
	}

	/**
	 * @return The blue component. From 0 to 1.
	 */
	public float B() {
		return rgba[2];
	}

	/**
	 * @param value New blue component. From 0 to 1.
	 * @return This reference.
	 */
	public Color setB(float value) {
		this.rgba[2] = value;
		checkBounds();

		return this;
	}

	/**
	 * @return The alpha component. From 0 to 1.
	 */
	public float A() {
		return rgba[3];
	}

	/**
	 * @param value New alpha component. From 0 to 1.
	 * @return This reference.
	 */
	public Color setA(float value) {
		this.rgba[3] = value;
		checkBounds();

		return this;
	}

	/**
	 * @param other Color to add.
	 * @return This reference.
	 */
	public Color add(Color other) {
		this.setR(this.R() + other.R());
		this.setG(this.G() + other.G());
		this.setB(this.B() + other.B());
		this.setA(this.A() + other.A());

		return this;
	}

	/**
	 * Multiplies all color components by m.
	 *
	 * @param m Multiplication factor.
	 * @return This reference.
	 */
	public Color multiply(float m) {
		this.setR(this.R() * m);
		this.setG(this.G() * m);
		this.setB(this.B() * m);
		this.setA(this.A() * m);

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
