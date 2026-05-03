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
import zernikalos.action.ZSkeletalAction
import zernikalos.context.ZContext
import zernikalos.loader.ZKo
import zernikalos.loader.loadFromAssets
import zernikalos.logger.ZLogLevel
import zernikalos.objects.ZModel
import zernikalos.objects.ZObject
import zernikalos.objects.ZScene
import zernikalos.scenestatehandler.ZSceneStateHandler
import zernikalos.search.findFirstCamera
import zernikalos.search.findFirstModel
import zernikalos.ui.ZernikalosView
import dev.zernikalos.zernikalosdemoapp.EngineDemoControlsBar
import dev.zernikalos.zernikalosdemoapp.R

/**
 * Standalone demo: loads the Soldier model, uses the engine default scene (includes a camera),
 * parents the loaded root under that scene, wires actions to the spinner, and rotates the root
 * around Y from the toolbar.
 */
class SoldierSampleFragment : Fragment() {

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

        engine.settings.logLevel = ZLogLevel.DEBUG

        val handler = object : ZSceneStateHandler {
            override fun onReady(context: ZContext, done: () -> Unit) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val loaded: ZKo = runCatching {
                        withContext(Dispatchers.IO) {
                            loadFromAssets(requireContext(), "gltf/soldier2.zko")
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

                        // --- defaultScene() ships a ready-made camera and lights; we only add the asset root. ---
                        val root = loaded.root
                        val scene = ZScene.defaultScene()
                        val camera = findFirstCamera(scene)!!
                        scene.addChild(root)
                        context.activeCamera = camera
                        context.scene = scene

                        val mainObj = findFirstModel(scene)

                        // --- Camera pose tuned for this asset (eye height and distance along -Z). ---
                        context.activeCamera?.transform?.rotate(180f, 1f, 0f, 0f)
                        context.activeCamera?.transform?.translate(0f, -1f, -3f)

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
                actionPlayer.update()
                actionPlayer.applyCurrentPose()
                done()
            }

            override fun onRender(context: ZContext, done: () -> Unit) {
                done()
            }
        }

        engine.initialize(zView, handler)

        demoControls.setOnRotateNegativeClick { rotateRootY(-2f) }
        demoControls.setOnRotatePositiveClick { rotateRootY(2f) }
    }

    private fun rotateRootY(degrees: Float) {
        val root = loadedRoot ?: return
        root.transform.rotate(degrees, 0f, 1f, 0f)
    }

    /** Stops the current clip, binds [action] to [model]'s skeleton, then starts playback. */
    private fun playSkeletalClip(model: ZModel, action: ZSkeletalAction) {
        actionPlayer.stop()
        actionPlayer.setAction(model.skeleton!!, action)
        actionPlayer.play(true)
    }

    override fun onDestroyView() {
        engine.dispose()
        super.onDestroyView()
    }
}
