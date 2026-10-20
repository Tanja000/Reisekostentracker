import SwiftUI
import MapKit

struct MapExpenseItem: Identifiable {
    let id: String
    let title: String
    let amount: Double
    let coordinate: CLLocationCoordinate2D
    let categoryName: String
}

struct MapView: View {
    @ObservedObject var store: ExpenseStore
    @State private var region = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 49.0069, longitude: 8.4037),
        span: MKCoordinateSpan(latitudeDelta: 0.15, longitudeDelta: 0.15)
    )
    @State private var selectedExpense: Expense? = nil
    
    var annotations: [MapExpenseItem] {
        store.expenses.compactMap { exp in
            guard let lat = exp.latitude, let lon = exp.longitude else { return nil }
            let cat = store.category(for: exp.categoryId)
            return MapExpenseItem(
                id: exp.id,
                title: exp.title,
                amount: exp.amount,
                coordinate: CLLocationCoordinate2D(latitude: lat, longitude: lon),
                categoryName: cat.name(isEnglish: store.isEnglish)
            )
        }
    }
    
    var body: some View {
        NavigationView {
            ZStack {
                Map(coordinateRegion: $region, annotationItems: annotations) { item in
                    MapAnnotation(coordinate: item.coordinate) {
                        let size = min(54.0, max(24.0, 20.0 + sqrt(max(1.0, item.amount)) * 2.0))
                        ZStack {
                            Circle()
                                .fill(Color.red.opacity(0.85))
                                .frame(width: size, height: size)
                                .overlay(Circle().stroke(Color.white, lineWidth: 2.5))
                                .shadow(color: Color.red.opacity(0.4), radius: 4, x: 0, y: 2)
                            
                            if size >= 28 {
                                Text("\(Int(round(item.amount)))€")
                                    .font(.system(size: max(9, min(12, size * 0.35)), weight: .heavy))
                                    .foregroundColor(.white)
                            }
                        }
                    }
                }
                .edgesIgnoringSafeArea(.all)
                
                // Top Info Bar
                VStack {
                    HStack {
                        HStack(spacing: 8) {
                            Image(systemName: "map.fill")
                                .foregroundColor(.blue)
                            Text("\(annotations.count) \(store.isEnglish ? "locations on map" : "Ausgaben auf der Karte")")
                                .font(.caption)
                                .bold()
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(Color(UIColor.systemBackground).opacity(0.92))
                        .cornerRadius(20)
                        .shadow(radius: 4)
                        
                        Spacer()
                        
                        Button(action: {
                            withAnimation {
                                region.center = store.currentCoordinate
                            }
                        }) {
                            Image(systemName: "location.circle.fill")
                                .font(.system(size: 32))
                                .foregroundColor(.blue)
                                .background(Color.white.clipShape(Circle()))
                                .shadow(radius: 4)
                        }
                    }
                    .padding()
                    Spacer()
                }
            }
            .navigationTitle(store.isEnglish ? "Expense Map" : "Ausgaben-Karte")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
