#version 300 es

in vec3 a_Position;
uniform vec3 a_Color;
out vec3 out_Color;

void main(){
    gl_Position = vec4(a_Position, 1.0f);
    out_Color = a_Color;
}