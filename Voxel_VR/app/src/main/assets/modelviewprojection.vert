#version 300 es

in vec3 a_Position;

out vec2 out_Texture;
out float shadow_value;

uniform mat4 a_Model;
uniform mat4 a_View;
uniform mat4 a_Projection;

vec2 texCoords[6] = vec2[6](
    vec2(0.0,1.0),
    vec2(1.0,1.0),
    vec2(0.0,0.0),
    vec2(0.0,0.0),
    vec2(1.0,1.0),
    vec2(1.0,0.0)
);

float shadows[6] = float[6](
0.2, // Face du dessous
1.2, // Face du dessus
0.8, // Face arrière
0.8, // Face de devant
0.5, // Face de gauche
0.5 // Face de droite
);

void main(){
    gl_Position = a_Projection * a_View * a_Model * vec4(a_Position, 1.0f);
    out_Texture = vec2(texCoords[gl_VertexID%6].x, texCoords[gl_VertexID%6].y);
    shadow_value = shadows[(gl_VertexID/6)%6];
}