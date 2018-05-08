// passed from vertex
varying vec4 out_vertexColor;
varying vec4 out_shadowColor;
varying vec2 out_textureCoord;

// uniforms
uniform vec2 un_viewportSize;
uniform vec2 un_cameraPos;
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
	float cutOff;
};
uniform LightInfo un_Lights[8];

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

    vec4 fragCoord = vec4(gl_FragCoord.x, un_viewportSize.y - gl_FragCoord.y, gl_FragCoord.zw);
    vec4 fragCoordWorld = fragCoord - vec4(un_cameraPos.xy + un_viewportSize.xy / 2.0, 0.0, 1.0);

	vec4 lightColor = vec4(0.0, 0.0, 0.0, 1.0);
	for (int i = 0; i < un_lightCount; i++) {
	    vec4 lColor = un_Lights[i].color;
	    if (un_Lights[i].power >= 0.0) {
		    float attenuation = un_Lights[i].power / length(un_Lights[i].position - fragCoordWorld);
		    lColor *= vec4(attenuation, attenuation, attenuation, pow(attenuation, 3.0));
	    }

		lightColor += lColor;
	}

	//return vec4(fragCoordWorld.x / 1920.0, fragCoordWorld.y / 1080.0, 0.0, 1.0);
	return lightColor;
}

void main() {
	if (checkCircle()) {
		gl_FragColor = calcTextureColor() * calcLightColor();
	} else {
		gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
	}
}
