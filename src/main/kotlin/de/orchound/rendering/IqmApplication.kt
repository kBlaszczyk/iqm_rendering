package de.orchound.rendering

import de.orchound.engine.rendering.iqm.IqmLoader
import de.orchound.rendering.iqm.IqmSceneObject
import de.orchound.rendering.iqm.IqmShader
import org.joml.Matrix4f
import org.joml.Vector3f
import java.io.File


@ExperimentalUnsignedTypes
object IqmApplication {
	private val window = Window("IQM Demo", 1280, 720)
	private val camera = Camera(window.aspectRatio, 90f)

	private val shader = IqmShader()
	private val sceneObject = IqmSceneObject(
		IqmLoader.loadIqm(File("data/mrfixit.iqm")), shader
	)

	private val lightDirection = Vector3f(-10f)
	private val csLightDirection = Vector3f()
	private val view = Matrix4f()

	fun run() {
		while (!window.shouldClose()) {
			update()
			render()
		}

		window.destroy()
	}

	private fun update() {
		Time.update()

		sceneObject.rotateY(Time.deltaTime * 15f / 1000f)
		sceneObject.update(camera)

		camera.getView(view)
		view.transformDirection(lightDirection, csLightDirection).normalize()
	}

	private fun render() {
		window.prepareFrame()

		shader.bind()
		shader.setCsLightDirection(csLightDirection)
		sceneObject.draw()
		shader.unbind()

		window.finishFrame()
	}
}