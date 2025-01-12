#version 300 es

in vec3 a_Position;
out vec3 out_Color;

uniform vec3 a_Color;
uniform mat4 a_Model;
uniform mat4 a_View;
uniform mat4 a_Projection;

void main(){
    gl_Position = a_Projection * a_View * a_Model * vec4(a_Position, 1.0f);
    out_Color = a_Color;
}