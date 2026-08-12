import Zernikalos

final class SoldierDemoSceneHandler: ZSceneStateHandler {
    private let viewModel: EngineDemoViewModel

    init(viewModel: EngineDemoViewModel) {
        self.viewModel = viewModel
    }

    func onReady(context: ZContext, done: () -> Void) {
        guard let loaded = DemoSceneSupport.loadZko(path: ZkoAssetPaths.soldier) else {
            viewModel.reportLoadFailure("Could not load \(ZkoAssetPaths.soldier).zko from bundle")
            done()
            return
        }

        let root = loaded.root
        viewModel.loadedRoot = root

        let scene = ZScene.companion.defaultScene()
        let camera = ZFinderKt.findFirstCamera(root: scene)
        scene.addChild(child: root)

        context.activeCamera = camera
        context.scene = scene

        let mainObj = ZFinderKt.findFirstModel(root: scene)
        context.activeCamera?.transform.rotateDegrees(angle: 180, x: 0, y: 1, z: 0)
        context.activeCamera?.transform.setPosition(x: 0, y: -1, z: -3)

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
