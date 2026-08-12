import SwiftUI
import Zernikalos

struct StormtrooperDemoView: View {
    var body: some View {
        EngineDemoHost(
            title: "Stormtrooper",
            makeHandler: { viewModel in StormtrooperDemoSceneHandler(viewModel: viewModel) },
            onRotateNegative: { root in
                root?.transform.rotateDegrees(angle: -2, x: 0, y: 0, z: 1)
            },
            onRotatePositive: { root in
                root?.transform.rotateDegrees(angle: 2, x: 0, y: 0, z: 1)
            }
        )
    }
}
