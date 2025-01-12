#version 300 es

precision mediump float;
in vec3 out_Color;
out vec4 fragmentColor;

void main(){
    fragmentColor = vec4(out_Color,1.0f);
}