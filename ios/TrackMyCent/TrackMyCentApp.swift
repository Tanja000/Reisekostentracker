import SwiftUI

@main
struct TrackMyCentApp: App {
    @StateObject private var store = ExpenseStore()
    
    var body: some Scene {
        WindowGroup {
            TabView {
                ExpensesView(store: store)
                    .tabItem {
                        Label(store.isEnglish ? "Expenses" : "Ausgaben", systemImage: "list.bullet.rectangle.portrait.fill")
                    }
                
                BudgetsView(store: store)
                    .tabItem {
                        Label(store.isEnglish ? "Budgets" : "Budgets", systemImage: "chart.pie.fill")
                    }
                
                MapView(store: store)
                    .tabItem {
                        Label(store.isEnglish ? "Map" : "Karte", systemImage: "map.fill")
                    }
                
                StatisticsView(store: store)
                    .tabItem {
                        Label(store.isEnglish ? "Stats" : "Statistik", systemImage: "chart.bar.xaxis")
                    }
            }
            .accentColor(.blue)
        }
    }
}
