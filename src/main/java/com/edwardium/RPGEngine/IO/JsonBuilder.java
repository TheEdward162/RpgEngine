package com.edwardium.RPGEngine.IO;

import com.edwardium.RPGEngine.Utility.GameSerializable;

import javax.json.*;
import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonBuilder {
	private JsonObjectBuilder builderInstance;
	public JsonBuilder() {
		this.builderInstance = Json.createObjectBuilder();
	}

	public JsonBuilder add(String s, JsonValue jsonValue) {
		builderInstance.add(s, jsonValue);
		return this;
	}

	public JsonBuilder add(String s, String s1) {
		builderInstance.add(s, s1);
		return this;
	}
	public JsonBuilder add_optional(String s, String s1, String opt) {
		if (!s1.equals(opt))
			return add(s, s1);
		return this;
	}
	
	public JsonBuilder add(String s, BigInteger bigInteger) {
		builderInstance.add(s, bigInteger);
		return this;
	}
	public JsonBuilder add_optional(String s, BigInteger bigInteger, BigInteger opt) {
		if (!bigInteger.equals(opt))
			return add(s, bigInteger);
		return this;
	}
	
	public JsonBuilder add(String s, BigDecimal bigDecimal) {
		builderInstance.add(s, bigDecimal);
		return this;
	}
	public JsonBuilder add_optional(String s, BigDecimal bigDecimal, BigDecimal opt) {
		if (!bigDecimal.equals(opt))
			return add(s, bigDecimal);
		return this;
	}

	public JsonBuilder add(String s, int i) {
		builderInstance.add(s, i);
		return this;
	}
	public JsonBuilder add_optional(String s, int i, int opt) {
		if (i != opt)
			return add(s, i);
		return this;
	}
	
	public JsonBuilder add(String s, long l) {
		builderInstance.add(s, l);
		return this;
	}
	public JsonBuilder add_optional(String s, long l, long opt) {
		if (l != opt)
			return add(s, l);
		return this;
	}

	public JsonBuilder add(String s, double v) {
		builderInstance.add(s, v);
		return this;
	}
	public JsonBuilder add_optional(String s, double v, double opt) {
		if (v != opt)
			return add(s, v);
		return this;
	}
	
	public JsonBuilder add(String s, boolean b) {
		builderInstance.add(s, b);
		return this;
	}
	public JsonBuilder add_optional(String s, boolean b, boolean opt) {
		if (b != opt)
			return add(s, b);
		return this;
	}

	public JsonBuilder addNull(String s) {
		builderInstance.add(s, s);
		return this;
	}

	public JsonBuilder add(String s, JsonBuilder jsonBuilder) {
		builderInstance.add(s, jsonBuilder.builderInstance);
		return this;
	}
	
	public JsonBuilder add(String s, JsonArrayBuilder jsonArrayBuilder) {
		builderInstance.add(s, jsonArrayBuilder);
		return this;
	}

	public JsonBuilder add(String s, GameSerializable obj) {
		builderInstance.add(s, obj.toJSON());
		return this;
	}
	public JsonBuilder add_optional(String s, GameSerializable obj, GameSerializable opt) {
		if (!obj.equals(opt))
			return add(s, obj.toJSON());
		return this;
	}
	public JsonBuilder add_optional(String s, GameSerializable obj, boolean opt) {
		if (!opt)
			return add(s, obj.toJSON());
		return this;
	}

	public JsonObject build() {
		return builderInstance.build();
	}
}
