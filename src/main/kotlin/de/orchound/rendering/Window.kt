package de.orchound.rendering

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWFramebufferSizeCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*


class Window(title: String, width: Int, height: Int) {

	val aspectRatio = width.toFloat() / height

	var title: String = title
		set(value) {
			glfwSetWindowTitle(handle, value)
			field = value
		}

	private var handle: Long = 0

	private var errorCallback = GLFWErrorCallback.createPrint(System.err)
	private var framebufferSizeCallback = GLFWFramebufferSizeCallback.create { window, newWidth, newHeight ->
		if (window == handle)
			glViewport(0, 0, newWidth, newHeight)
	}

	init {
		if (!glfwInit())
			throw RuntimeException("Failed to initialize GLFW")

		glfwSetErrorCallback(errorCallback)

		glfwDefaultWindowHints()
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

		handle = glfwCreateWindow(width, height, title, 0, 0)
		if (handle == 0L)
			throw Exception("Failed to create window")

		glfwSetFramebufferSizeCallback(handle, framebufferSizeCallback)
		glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

		glfwMakeContextCurrent(handle)
		GL.createCapabilities()
		initRendering()
	}

	fun shouldClose() = glfwWindowShouldClose(handle)

	fun prepareFrame() {
		glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
	}

	fun finishFrame() {
		glfwSwapBuffers(handle)
		glfwPollEvents()
	}

	fun destroy() {
		errorCallback.close()
		framebufferSizeCallback.close()

		glfwDestroyWindow(handle)
		glfwTerminate()
	}

	private fun initRendering() {
		glClearColor(0f, 0f, 0f, 1f)
		glEnable(GL_DEPTH_TEST)
		glEnable(GL_CULL_FACE)
		glCullFace(GL_FRONT)
	}
}
