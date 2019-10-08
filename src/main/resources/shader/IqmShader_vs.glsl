#version 330

layout(location = 0) in vec3 position_ms;
layout(location = 1) in vec3 normal_ms;
layout(location = 2) in vec4 tangent_ms;
layout(location = 3) in vec2 texcoords;
layout(location = 4) in vec3 color;
layout(location = 5) in vec4 blend_index;
layout(location = 6) in vec4 blend_weight;

out vec2 uv;

uniform mat4 model_view_projection;

void main(void) {
	gl_Position = model_view_projection * vec4(position_ms, 1);
	uv = texcoords;
}
