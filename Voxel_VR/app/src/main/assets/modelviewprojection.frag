#version 300 es

precision mediump float;
in vec3 out_Color;
in vec2 out_Texture;

uniform sampler2D texture1;

out vec4 fragmentColor;

void main(){
    fragmentColor = texture(texture1, out_Texture) * vec4(out_Color, 1.0F);
}