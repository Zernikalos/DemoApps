import Zernikalos

final class FoxDemoSceneHandler: ZSceneStateHandler {
    private let viewModel: EngineDemoViewModel

    init(viewModel: EngineDemoViewModel) {
        self.viewModel = viewModel
    }

    func onReady(context: ZContext, done: () -> Void) {
        guard let loaded = DemoSceneSupport.loadZko(path: ZkoAssetPaths.fox) else {
            viewModel.reportLoadFailure("Could not load \(ZkoAssetPaths.fox).zko from bundle")
            done()
            return
        }

        let root = loaded.root
        viewModel.loadedRoot = root

        let scene = ZScene()
        let camera = ZCamera()
        let ambientLight = ZLight.companion.createAmbientLight()
        ambientLight.intensity = 0.1
        let light = ZLight()
        light.lamp = ZDirectionalLamp()

        scene.addChild(child: root)
        scene.addChild(child: ambientLight)
        scene.addChild(child: light)
        scene.addChild(child: camera)

        context.activeCamera = camera
        context.scene = scene

        let mainObj = ZFinderKt.findFirstModel(root: scene)
        mainObj?.transform.scale(s: 0.1)
        context.activeCamera?.transform.translate(x: 1, y: -5, z: -30)

        let actions = loaded.actions ?? []
        viewModel.bindSkeletalActions(model: mainObj, actions: actions)
        done()
    }

    func onUpdate(context: ZContext, done: @escaping () -> Void) {
        viewModel.actionPlayer.update()
        done()
    }

    func onRender(context: ZContext, done: () -> Void) {
        done()
    }

    func onResize(context: ZContext, width: Int32, height: Int32, done: () -> Void) {
        done()
    }
}
