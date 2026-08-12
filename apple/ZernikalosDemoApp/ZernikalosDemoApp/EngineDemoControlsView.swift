import SwiftUI

struct EngineDemoControlsView: View {
    @ObservedObject var viewModel: EngineDemoViewModel
    let onRotateNegative: () -> Void
    let onRotatePositive: () -> Void

    @State private var isRepeatingNegative = false
    @State private var isRepeatingPositive = false

    private let repeatInterval: TimeInterval = 0.04

    var body: some View {
        HStack(spacing: 12) {
            RepeatRotateButton(
                systemImage: "arrow.counterclockwise",
                isRepeating: $isRepeatingNegative,
                repeatInterval: repeatInterval,
                action: onRotateNegative
            )

            if !viewModel.actionLabels.isEmpty {
                Picker("Action", selection: Binding(
                    get: { viewModel.selectedActionIndex },
                    set: { viewModel.selectAction(at: $0) }
                )) {
                    ForEach(Array(viewModel.actionLabels.enumerated()), id: \.offset) { index, label in
                        Text(label).tag(index)
                    }
                }
                .pickerStyle(.menu)
                .frame(maxWidth: .infinity)
            }

            RepeatRotateButton(
                systemImage: "arrow.clockwise",
                isRepeating: $isRepeatingPositive,
                repeatInterval: repeatInterval,
                action: onRotatePositive
            )
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .padding()
    }
}

private struct RepeatRotateButton: View {
    let systemImage: String
    @Binding var isRepeating: Bool
    let repeatInterval: TimeInterval
    let action: () -> Void

    var body: some View {
        Image(systemName: systemImage)
            .font(.title2)
            .frame(width: 44, height: 44)
            .background(Color.accentColor.opacity(0.15))
            .clipShape(Circle())
            .gesture(
                DragGesture(minimumDistance: 0)
                    .onChanged { _ in
                        if !isRepeating {
                            isRepeating = true
                            action()
                        }
                    }
                    .onEnded { _ in
                        isRepeating = false
                    }
            )
            .onChange(of: isRepeating) { _, repeating in
                if repeating {
                    scheduleRepeat()
                }
            }
    }

    private func scheduleRepeat() {
        DispatchQueue.main.asyncAfter(deadline: .now() + repeatInterval) {
            guard isRepeating else { return }
            action()
            scheduleRepeat()
        }
    }
}
