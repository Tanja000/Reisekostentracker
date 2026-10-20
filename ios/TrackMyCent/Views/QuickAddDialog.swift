import SwiftUI
import CoreLocation

struct QuickAddDialog: View {
    @ObservedObject var store: ExpenseStore
    let preset: QuickAddPreset
    let onDismiss: () -> Void
    
    @State private var amountText: String = ""
    @State private var locationText: String = "Karlsruhe"
    @State private var paymentMethod: String = "Karte"
    
    private let quickCities = ["Karlsruhe", "München", "Berlin", "Hamburg", "Köln", "Frankfurt", "Stuttgart", "Wien", "Zürich"]
    
    init(store: ExpenseStore, preset: QuickAddPreset, onDismiss: @escaping () -> Void) {
        self.store = store
        self.preset = preset
        self.onDismiss = onDismiss
        _amountText = State(initialValue: String(format: "%.2f", preset.defaultAmount).replacingOccurrences(of: ".", with: ","))
        _locationText = State(initialValue: store.currentCity.isEmpty ? "Karlsruhe" : store.currentCity)
        _paymentMethod = State(initialValue: preset.defaultPaymentMethod)
    }
    
    var body: some View {
        ZStack {
            Color.black.opacity(0.4).ignoresSafeArea()
                .onTapGesture { onDismiss() }
            
            VStack(spacing: 14) {
                // Header
                HStack(spacing: 12) {
                    Image(systemName: preset.iconName)
                        .font(.system(size: 24))
                        .foregroundColor(.blue)
                        .frame(width: 44, height: 44)
                        .background(Color.blue.opacity(0.12))
                        .clipShape(Circle())
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text(store.isEnglish ? "⚡ Quick Booking" : "⚡ Schnellbuchung")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Text(preset.title(isEnglish: store.isEnglish))
                            .font(.headline)
                            .bold()
                    }
                    Spacer()
                }
                
                Divider()
                
                // Amount Input
                VStack(alignment: .leading, spacing: 4) {
                    Text(store.isEnglish ? "Amount" : "Betrag")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    HStack {
                        TextField("0,00", text: $amountText)
                            .font(.system(size: 26, weight: .bold))
                            .keyboardType(.decimalPad)
                        
                        Text(store.isEnglish ? "$" : "€")
                            .font(.title2)
                            .bold()
                            .foregroundColor(.secondary)
                    }
                    .padding(8)
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(10)
                }
                
                // Location Input (Editable + Geocoding Library)
                VStack(alignment: .leading, spacing: 4) {
                    Text(store.isEnglish ? "Location / City (places pin on map)" : "Ort / Stadt (wird auf Karte platziert)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    HStack {
                        Image(systemName: "location.fill")
                            .foregroundColor(.blue)
                            .font(.subheadline)
                        TextField(store.isEnglish ? "e.g. Berlin, Munich..." : "z.B. Berlin, München...", text: $locationText)
                            .font(.subheadline)
                    }
                    .padding(8)
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(10)
                    
                    // Quick city chips
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 6) {
                            ForEach(quickCities, id: \.self) { city in
                                Button(action: { locationText = city }) {
                                    Text(city)
                                        .font(.caption2)
                                        .bold()
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 4)
                                        .background(locationText == city ? Color.blue : Color(UIColor.systemGray5))
                                        .foregroundColor(locationText == city ? .white : .primary)
                                        .cornerRadius(8)
                                }
                            }
                        }
                    }
                }
                
                // Payment Method Picker
                VStack(alignment: .leading, spacing: 4) {
                    Text(store.isEnglish ? "Payment Method" : "Zahlungsart")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    Picker("Payment", selection: $paymentMethod) {
                        Text(store.isEnglish ? "Card" : "Karte").tag("Karte")
                        Text(store.isEnglish ? "Cash" : "Bar").tag("Bar")
                        Text("Online").tag("Online")
                    }
                    .pickerStyle(.segmented)
                }
                
                // Action Buttons
                HStack(spacing: 12) {
                    Button(action: onDismiss) {
                        Text(store.isEnglish ? "Cancel" : "Abbrechen")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(Color(UIColor.systemGray5))
                            .foregroundColor(.primary)
                            .cornerRadius(10)
                    }
                    
                    Button(action: confirmBooking) {
                        Text(store.isEnglish ? "✓ Book Now" : "✓ Buchen")
                            .bold()
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(10)
                    }
                }
            }
            .padding(20)
            .background(Color(UIColor.systemBackground))
            .cornerRadius(18)
            .shadow(radius: 20)
            .padding(.horizontal, 24)
        }
    }
    
    private func confirmBooking() {
        let clean = amountText.replacingOccurrences(of: ",", with: ".")
        let amount = Double(clean) ?? preset.defaultAmount
        let locName = locationText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Karlsruhe" : locationText
        
        store.resolveLocation(name: locName) { coord in
            DispatchQueue.main.async {
                let exp = Expense(
                    title: preset.title(isEnglish: store.isEnglish),
                    amount: amount,
                    categoryId: preset.categoryId,
                    date: Date(),
                    paymentMethod: paymentMethod,
                    locationName: locName,
                    latitude: coord.latitude,
                    longitude: coord.longitude,
                    notes: "⚡ Schnellbuchung"
                )
                store.addExpense(exp)
                onDismiss()
            }
        }
    }
}
