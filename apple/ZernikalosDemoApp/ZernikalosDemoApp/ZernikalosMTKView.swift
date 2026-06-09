import MetalKit
import SwiftUI
import Zernikalos

struct ZernikalosMTKView: UIViewRepresentable {
    let stateHandler: ZSceneStateHandler
    @Binding var engine: Zernikalos?

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    func makeUIView(context: Context) -> MTKView {
        let mtkView = MTKView()
        mtkView.preferredFramesPerSecond = 60
        mtkView.enableSetNeedsDisplay = false
        mtkView.isPaused = false
        mtkView.framebufferOnly = false

        guard let device = MTLCreateSystemDefaultDevice() else {
            fatalError("Metal is not supported on this device")
        }

        mtkView.device = device
        mtkView.backgroundColor = UIColor(red: 0.15, green: 0.15, blue: 0.15, alpha: 1)

        let zernikalos = Zernikalos()
        zernikalos.settings.logLevel = ZLogLevel.debug
        zernikalos.initialize(view: mtkView, stateHandler: stateHandler)

        context.coordinator.engine = zernikalos
        DispatchQueue.main.async {
            engine = zernikalos
        }

        return mtkView
    }

    func updateUIView(_ uiView: MTKView, context: Context) {}

    static func dismantleUIView(_ uiView: MTKView, coordinator: Coordinator) {
        coordinator.engine?.dispose()
        coordinator.engine = nil
    }

    final class Coordinator {
        var engine: Zernikalos?
    }
}
