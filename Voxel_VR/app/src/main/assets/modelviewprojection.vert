#version 300 es

in vec3 a_Position;
in vec2 a_Texture;

out vec2 out_Texture;

uniform mat4 a_Model;
uniform mat4 a_View;
uniform mat4 a_Projection;

void main(){
    gl_Position = a_Projection * a_View * a_Model * vec4(a_Position, 1.0f);
    out_Texture = vec2(a_Texture.x, a_Texture.y);
}