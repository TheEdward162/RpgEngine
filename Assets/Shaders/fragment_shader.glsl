// passed from vertex
varying vec4 out_vertexColor;
varying vec2 out_textureCoord;

uniform vec4 un_globalColor;

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


void main() {
    // circle code
    bool draw = true;
    vec2 circlePos = out_textureCoord - vec2(0.5, 0.5);
    float th = circlePos.x != 0.0 ? atan(circlePos.y, circlePos.x) : 3.1415;
    float distance = length(circlePos);
    if (th > un_circleInfo.maxAngle || !(distance >= un_circleInfo.minRadius && distance <= un_circleInfo.maxRadius)) {
        draw = false;
    }
    if (draw) {
        vec4 currentColor = out_vertexColor * un_globalColor;

        // texture code
        vec2 textureCoord = vec2(un_textureInfo.textureSubspace.x + un_textureInfo.textureSubspace.z * out_textureCoord.s,
                                un_textureInfo.textureSubspace.y + un_textureInfo.textureSubspace.w * out_textureCoord.t);
        vec4 finalColor = texture2D(un_textureInfo.tex, textureCoord);
        if (un_textureInfo.overrideColor) {
            finalColor = vec4(currentColor.xyz, finalColor.w * currentColor.w);
        } else {
            finalColor = finalColor * currentColor;
        }
    
        gl_FragColor = finalColor;
    } else {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    }
}
