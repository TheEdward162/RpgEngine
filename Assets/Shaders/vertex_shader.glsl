// passed from vertex data
attribute vec4 in_Position;
attribute vec4 in_VertexColor;
attribute vec2 in_TextureCoord;

// passed to frag
varying vec4 out_vertexColor;
varying vec2 out_textureCoord;

void main() {
    // transformed position
    gl_Position = gl_ModelViewProjectionMatrix * in_Position;

    // for fragment
    out_vertexColor = in_VertexColor;
    out_textureCoord = in_TextureCoord;
}