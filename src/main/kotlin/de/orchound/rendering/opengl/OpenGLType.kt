package de.orchound.rendering.opengl

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.GL_HALF_FLOAT


enum class OpenGLType(val value: Int, val size: Int, val isIntegral: Boolean) {
	BYTE(GL_BYTE, 1, true),
	UNSIGNED_BYTE(GL_UNSIGNED_BYTE, 1, true),
	SHORT(GL_SHORT, 2, true),
	UNSIGNED_SHORT(GL_UNSIGNED_SHORT, 2, true),
	INT(GL_INT, 4, true),
	FLOAT(GL_FLOAT, 4, false),
	HALF_FLOAT(GL_HALF_FLOAT, 2, false),
	DOUBLE(GL_DOUBLE, 8, false);
}
