import SwiftUI
import Zernikalos

struct EngineDemoHost: View {
    let title: String
    @StateObject private var viewModel = EngineDemoViewModel()
    @State private var engine: Zernikalos?
    @State private var stateHandler: DemoStateHandlerBox?

    let makeHandler: (EngineDemoViewModel) -> ZSceneStateHandler
    let onRotateNegative: (ZObject?) -> Void
    let onRotatePositive: (ZObject?) -> Void

    var body: some View {
        ZStack(alignment: .bottom) {
            if let handler = stateHandler {
                ZernikalosMTKView(stateHandler: handler.handler, engine: $engine)
                    .ignoresSafeArea()
            } else {
                Color.black.ignoresSafeArea()
            }

            EngineDemoControlsView(
                viewModel: viewModel,
                onRotateNegative: { onRotateNegative(viewModel.loadedRoot) },
                onRotatePositive: { onRotatePositive(viewModel.loadedRoot) }
            )
        }
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            if stateHandler == nil {
                stateHandler = DemoStateHandlerBox(handler: makeHandler(viewModel))
            }
        }
        .alert("Failed to load model", isPresented: Binding(
            get: { viewModel.loadError != nil },
            set: { if !$0 { viewModel.loadError = nil } }
        )) {
            Button("OK", role: .cancel) {}
        } message: {
            Text(viewModel.loadError ?? "")
        }
    }
}

/// Type-erased wrapper so SwiftUI can hold a stable handler reference.
final class DemoStateHandlerBox {
    let handler: ZSceneStateHandler

    init(handler: ZSceneStateHandler) {
        self.handler = handler
    }
}
