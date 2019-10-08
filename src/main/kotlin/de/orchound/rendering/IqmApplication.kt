package de.orchound.rendering

import de.orchound.engine.rendering.iqm.IqmLoader
import de.orchound.rendering.iqm.IqmSceneObject
import de.orchound.rendering.iqm.IqmShader
import org.joml.Matrix4f
import java.io.File


@ExperimentalUnsignedTypes
object IqmApplication {
	private val window = Window("IQM Demo", 1280, 720)
	private val camera = Camera(window.aspectRatio, 90f)

	private val iqmShader = IqmShader()
	private val sceneObject = IqmSceneObject(
		IqmLoader.loadIqm(File("data/mrfixit.iqm"))
	)

	private val modelViewProjection = Matrix4f()

	fun run() {
		while (!window.shouldClose()) {
			update()
			render()
		}

		window.destroy()
	}

	private fun update() {
		Time.update()

		sceneObject.rotateY(Time.deltaTime * 36f / 1000f)
		camera.getProjectionView(modelViewProjection)
			.mul(sceneObject.getTransformation(), modelViewProjection)
	}

	private fun render() {
		window.prepareFrame()

		iqmShader.bind()
		iqmShader.updateModelViewProjection(modelViewProjection)
		sceneObject.draw()
		iqmShader.unbind()

		window.finishFrame()
	}
}