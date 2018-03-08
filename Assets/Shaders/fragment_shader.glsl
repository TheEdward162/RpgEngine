// passed from vertex
varying vec4 out_vertexColor;
varying vec2 out_textureCoord;

uniform vec4 un_globalColor;
uniform float un_circleRadius;

struct TextureInfo {
    sampler2D tex;
    // (x, y, width, height)
    vec4 textureSubspace;
    bool overrideColor;
};
uniform TextureInfo un_textureInfo;


void main() {
    // color code
    vec4 currentColor = out_vertexColor * un_globalColor;

    // circle code
    float alpha = 1.0;
    float distance = length(out_textureCoord - vec2(0.5, 0.5));
    if (distance > un_circleRadius)
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    else {
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
    }
}
