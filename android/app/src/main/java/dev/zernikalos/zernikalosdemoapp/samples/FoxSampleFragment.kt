package dev.zernikalos.zernikalosdemoapp.samples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zernikalos.Zernikalos
import zernikalos.action.ZActionPlayer
import zernikalos.components.light.ZDirectionalLamp
import zernikalos.context.ZContext
import zernikalos.loader.ZKo
import zernikalos.loader.loadFromProto
import zernikalos.logger.ZLogLevel
import zernikalos.action.ZSkeletalAction
import zernikalos.objects.ZCamera
import zernikalos.objects.ZLight
import zernikalos.objects.ZModel
import zernikalos.objects.ZObject
import zernikalos.objects.ZScene
import zernikalos.scenestatehandler.ZSceneStateHandler
import zernikalos.search.findFirstModel
import zernikalos.ui.ZernikalosView
import dev.zernikalos.zernikalosdemoapp.EngineDemoControlsBar
import dev.zernikalos.zernikalosdemoapp.R

/**
 * Standalone demo: loads the Fox skeletal model from app assets, builds a custom scene with ambient
 * plus directional light, exposes bundled skeletal actions in the spinner, and nudges the root
 * around Y with the toolbar buttons for a quick orbit preview.
 */
class FoxSampleFragment : Fragment() {

    private val engine = Zernikalos()
    private val actionPlayer = ZActionPlayer()

    private var loadedRoot: ZObject? = null

    private lateinit var zView: ZernikalosView
    private lateinit var demoControls: EngineDemoControlsBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_sample, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        zView = view.findViewById(R.id.zernikalos_view)
        demoControls = view.findViewById(R.id.demo_controls)

        // Verbose engine logs while wiring a new screen; turn down for production builds.
        engine.settings.logLevel = ZLogLevel.DEBUG

        val handler = object : ZSceneStateHandler {
            override fun onReady(context: ZContext, done: () -> Unit) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val loaded: ZKo = runCatching {
                        withContext(Dispatchers.IO) {
                            val bytes =
                                requireContext().assets.open("gltf/Fox.zko").use { it.readBytes() }
                            loadFromProto(bytes)
                        }
                    }.getOrElse { e ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.zko_load_failed, e.message ?: e.toString()),
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                        done()
                        return@launch
                    }

                    withContext(Dispatchers.Main) {
                        loadedRoot = loaded.root

                        // --- Scene graph: root model, lights, and camera are siblings under one ZScene. ---
                        val root = loaded.root
                        val scene = ZScene()
                        val camera = ZCamera()
                        val ambientLight = ZLight.createAmbientLight().apply { intensity = 0.1f }
                        val light = ZLight().apply { lamp = ZDirectionalLamp() }
                        scene.addChild(root)
                        scene.addChild(ambientLight)
                        scene.addChild(light)
                        scene.addChild(camera)
                        context.activeCamera = camera
                        context.scene = scene

                        // --- Framing: pick the first skinned mesh, scale it, then aim the camera. ---
                        val mainObj = findFirstModel(scene)
                        context.activeCamera?.transform?.rotate(180f, 1f, 0f, 0f)
                        context.activeCamera?.transform?.rotate(180f, 0f, 1f, 0f)
                        mainObj?.transform?.scale(0.1f)
                        context.activeCamera?.transform?.translate(-1f, -7f, -40f)
                        context.activeCamera?.transform?.rotate(-45f, 0f, 1f, 0f)

                        val actions = loaded.actions.orEmpty()
                        demoControls.bindSkeletalActions(mainObj, actions, ::playSkeletalClip)
                        val skinned = mainObj
                        if (skinned != null && actions.isNotEmpty()) {
                            playSkeletalClip(skinned, actions[0])
                        }
                        done()
                    }
                }
            }

            override fun onUpdate(context: ZContext, done: () -> Unit) {
                // Advance any playing skeletal clip by engine time delta.
                actionPlayer.update()
                done()
            }

            override fun onRender(context: ZContext, done: () -> Unit) {
                done()
            }
        }

        engine.initialize(zView, handler)

        // Toolbar rotates the loaded root around world Y for a small orbit each tap.
        demoControls.setOnRotateNegativeClick { rotateRootY(-2f) }
        demoControls.setOnRotatePositiveClick { rotateRootY(2f) }
    }

    private fun rotateRootY(degrees: Float) {
        val root = loadedRoot ?: return
        root.transform.rotate(degrees, 0f, 1f, 0f)
    }

    /** Stops the current clip, assigns [action] on [model], then starts playback (spinner + initial pose). */
    private fun playSkeletalClip(model: ZModel, action: ZSkeletalAction) {
        actionPlayer.stop()
        actionPlayer.setAction(model, action)
        actionPlayer.play(true)
    }

    override fun onDestroyView() {
        engine.dispose()
        super.onDestroyView()
    }
}
