import SwiftUI
import Zernikalos

struct FoxDemoView: View {
    var body: some View {
        EngineDemoHost(
            title: "Fox",
            makeHandler: { viewModel in FoxDemoSceneHandler(viewModel: viewModel) },
            onRotateNegative: { root in
                root?.transform.rotateDegrees(angle: -2, x: 0, y: 1, z: 0)
            },
            onRotatePositive: { root in
                root?.transform.rotateDegrees(angle: 2, x: 0, y: 1, z: 0)
            }
        )
    }
}
