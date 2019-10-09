#version 330

in vec2 uv;
in vec3 normal_cs;
in vec3 position_cs;

out vec4 color;

uniform sampler2D texture_sampler;
uniform vec3 light_direction_cs;

float ambient_intensity = 0.2;
float diffuse_intensity = 0.8;
float specular_intensity = 0.5;
float specular_power = 2;

void main(void) {
	vec4 texture_color = texture(texture_sampler, uv);
	vec4 ambient_color = texture_color * ambient_intensity;

	float diffuse_factor = -min(dot(normal_cs, light_direction_cs), 0);
	vec4 diffuse_color = texture_color * diffuse_intensity * diffuse_factor;

	vec3 reflected_light = normalize(reflect(light_direction_cs, normal_cs));
	float specular_factor = max(dot(normalize(-position_cs), reflected_light), 0);
	vec4 specular_color = texture_color * pow(specular_factor, specular_power) * specular_intensity;

	color = ambient_color + diffuse_color + specular_color;
}
