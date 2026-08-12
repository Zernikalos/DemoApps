# Zernikalos Demo — Apple (iOS)

SwiftUI app that mirrors the Android and Web demos: **Fox**, **Soldier**, and **Stormtrooper**, using the shared `.zko` assets under [`DemoApps/assets/zko`](../assets/zko/).

## Prerequisites

1. **Generate the Kotlin framework** (first time, or after engine changes):

   ```bash
   cd Zernikalos
   ./gradlew :engine:generateDummyFramework
   ```

2. **Build shared assets** (if `assets/zko` is empty):

   ```bash
   cd DemoApps
   node scripts/build-assets.mjs
   ```

3. **Install CocoaPods dependencies**:

   ```bash
   cd DemoApps/apple/ZernikalosDemoApp
   pod install
   ```

## Run

Open **`ZernikalosDemoApp.xcworkspace`** (not the `.xcodeproj` alone), select an iOS simulator, and run.

The app bundles `../../assets/zko` as a folder reference (`zko/` at the bundle root). Loader paths in Swift use that prefix (same files as Android/Web):

- `zko/gltf/Fox.zko`
- `zko/gltf/soldier2.zko`
- `zko/collada/stormtrooper/stormtrooper.zko`

## Project layout

| Path | Role |
|------|------|
| `ZernikalosDemoApp/SamplePickerView.swift` | Demo menu |
| `ZernikalosDemoApp/ZernikalosMTKView.swift` | `MTKView` bridge + engine init/dispose |
| `ZernikalosDemoApp/EngineDemoHost.swift` | Full-screen render + controls overlay |
| `ZernikalosDemoApp/samples/*` | Per-demo scene handlers (ported from Android fragments) |

## Notes

- Engine dependency: local CocoaPods podspec at `Zernikalos/engine/Zernikalos.podspec`.
- v1 targets **iOS + simulator** only.
- Touch orbit is not wired on iOS (Metal limitation); use the rotation toolbar like Android.
