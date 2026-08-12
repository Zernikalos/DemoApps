import SwiftUI

struct SamplePickerView: View {
    var body: some View {
        List {
            Section("Engine demos") {
                NavigationLink("Fox") {
                    FoxDemoView()
                }
                NavigationLink("Soldier") {
                    SoldierDemoView()
                }
                NavigationLink("Stormtrooper") {
                    StormtrooperDemoView()
                }
            }
        }
        .navigationTitle("Zernikalos")
    }
}
