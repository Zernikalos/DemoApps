//
//  ZernikalosDemoAppApp.swift
//  ZernikalosDemoApp
//
//  Created by Aarón Negrín on 27/05/2026.
//

import SwiftUI

@main
struct ZernikalosDemoAppApp: App {
    var body: some Scene {
        WindowGroup {
            NavigationStack {
                SamplePickerView()
            }
        }
    }
}
