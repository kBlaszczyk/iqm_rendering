package de.orchound.rendering.opengl

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.GL_HALF_FLOAT


enum class OpenGLType(val value: Int, val size: Int) {
	BYTE(GL_BYTE, 1),
	UNSIGNED_BYTE(GL_UNSIGNED_BYTE, 1),
	SHORT(GL_SHORT, 2),
	UNSIGNED_SHORT(GL_UNSIGNED_SHORT, 2),
	INT(GL_INT, 4),
	FLOAT(GL_FLOAT, 4),
	HALF_FLOAT(GL_HALF_FLOAT, 2),
	DOUBLE(GL_DOUBLE, 8);
}
