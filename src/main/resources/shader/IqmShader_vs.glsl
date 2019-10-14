#version 330

const int MAX_JOINTS = 80;
const int BLEND_WEIGHTS_COUNT = 4;

layout(location = 0) in vec3 position_ms;
layout(location = 1) in vec3 normal_ms;
layout(location = 2) in vec4 tangent_ms;
layout(location = 3) in vec2 texcoords;
layout(location = 4) in vec3 color;
layout(location = 5) in uvec4 blend_index;
layout(location = 6) in vec4 blend_weight;

out vec2 uv;
out vec3 normal_cs;
out vec3 position_cs;

uniform mat4 model_view;
uniform mat4 model_view_projection;
uniform mat4 joints[MAX_JOINTS];

void main(void) {
	vec4 final_position_ms = vec4(0.0);
	vec4 final_normal_ms = vec4(0.0);

	for(int i = 0; i < BLEND_WEIGHTS_COUNT; i++) {
		mat4 joint = joints[blend_index[i]];
		vec4 joint_position = joint * vec4(position_ms, 1.0);
		final_position_ms += joint_position * blend_weight[i];

		vec4 joint_normal = joint * vec4(normal_ms, 0.0);
		final_normal_ms += joint_normal * blend_weight[i];
	}

	gl_Position = model_view_projection * final_position_ms;
	uv = texcoords;
	normal_cs = normalize((model_view * vec4(normal_ms, 0)).xyz);
	position_cs = (model_view * vec4(position_ms, 1)).xyz;
}
