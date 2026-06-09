import Combine
import Foundation
import Zernikalos

@MainActor
final class EngineDemoViewModel: ObservableObject {
    @Published var actionLabels: [String] = []
    @Published var selectedActionIndex: Int = 0
    @Published var loadError: String?

    let actionPlayer = ZActionPlayer()
    var loadedRoot: ZObject?
    var mainModel: ZModel?
    private var skeletalActions: [ZSkeletalAction] = []

    func bindSkeletalActions(model: ZModel?, actions: [ZSkeletalAction]) {
        mainModel = model
        skeletalActions = actions
        actionLabels = actions.map(\.name)
        selectedActionIndex = 0

        if let model, let first = actions.first {
            playSkeletalClip(model: model, action: first)
        }
    }

    func selectAction(at index: Int) {
        guard index >= 0, index < skeletalActions.count,
              let model = mainModel else { return }
        selectedActionIndex = index
        playSkeletalClip(model: model, action: skeletalActions[index])
    }

    func playSkeletalClip(model: ZModel, action: ZSkeletalAction) {
        guard let skeleton = model.skeleton else { return }
        actionPlayer.stop()
        actionPlayer.setAction(skeleton: skeleton, action: action)
        actionPlayer.play(loop: true)
    }

    func reportLoadFailure(_ message: String) {
        loadError = message
    }
}

enum DemoSceneSupport {
    static func loadZko(path: String) -> ZKo? {
        ZkoLoader.companion.loadFromMainBundlePathSync(fileName: path)
    }
}
