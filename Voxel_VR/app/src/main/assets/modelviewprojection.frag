#version 300 es

precision mediump float;
in vec2 out_Texture;
in float shadow_value;

uniform sampler2D texture1;

out vec4 fragmentColor;

void main(){
    fragmentColor = shadow_value * texture(texture1, out_Texture);
}