// passed from vertex
varying vec4 out_vertexColor;
varying vec4 out_shadowColor;
varying vec2 out_textureCoord;

// uniforms
uniform vec4 un_globalColor;

uniform bool un_useLights;
uniform int un_lightCount;

struct CircleInfo {
	float minRadius;
	float maxRadius;
	float maxAngle;
};
uniform CircleInfo un_circleInfo;

struct TextureInfo {
	sampler2D tex;
	// (x, y, width, height)
	vec4 textureSubspace;
	bool overrideColor;
};
uniform TextureInfo un_textureInfo;

struct LightInfo {
	vec4 position;
	vec4 color;

	float power;
	float cutoff;
};
const int MAX_LIGHTS = 64;
uniform LightInfo un_Lights[MAX_LIGHTS];

bool checkCircle() {
	vec2 circlePos = out_textureCoord - vec2(0.5, 0.5);
	float th = circlePos.x != 0.0 ? atan(circlePos.y, circlePos.x) : 3.1415;
	float distance = length(circlePos);
	if (th > un_circleInfo.maxAngle || !(distance >= un_circleInfo.minRadius && distance <= un_circleInfo.maxRadius)) {
		return false;
	}

	return true;
}

vec4 calcTextureColor() {
	vec4 currentColor = out_vertexColor * un_globalColor;
	vec2 textureCoord = vec2(un_textureInfo.textureSubspace.x + un_textureInfo.textureSubspace.z * out_textureCoord.s,
							un_textureInfo.textureSubspace.y + un_textureInfo.textureSubspace.w * out_textureCoord.t);
	vec4 finalColor = texture2D(un_textureInfo.tex, textureCoord);
	if (un_textureInfo.overrideColor) {
		finalColor = vec4(currentColor.xyz, finalColor.w * currentColor.w);
	} else {
		finalColor = finalColor * currentColor;
	}

	return finalColor;
}

vec4 calcLightColor() {
    if (!un_useLights)
        return vec4(1.0, 1.0, 1.0, 1.0);

	vec4 lightColor = vec4(0.0, 0.0, 0.0, 0.0);
	int numLights = min(un_lightCount, MAX_LIGHTS);

	for (int i = 0; i < numLights; i++) {
	    vec4 lColor = un_Lights[i].color;
	    // lights with power <= 0 are ambient
	    if (un_Lights[i].power > 0.0) {
	        float distance = length(un_Lights[i].position - gl_FragCoord);

	        if (un_Lights[i].cutoff <= 0.0 || distance <= un_Lights[i].cutoff) {
                float attenuation = un_Lights[i].power / distance;
                lColor *= vec4(attenuation, attenuation, attenuation, pow(attenuation, 3.0));
		    } else {
		        continue;
		    }
	    }

		lightColor += lColor;
	}

	return lightColor;
}

void main() {
	if (checkCircle()) {
		gl_FragColor = calcTextureColor() * calcLightColor();
	} else {
		gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
	}
}
