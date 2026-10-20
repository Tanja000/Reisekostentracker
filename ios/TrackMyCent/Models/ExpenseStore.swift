import Foundation
import CoreLocation
import Combine

class ExpenseStore: NSObject, ObservableObject, CLLocationManagerDelegate {
    @Published var expenses: [Expense] = [] {
        didSet { save() }
    }
    @Published var categories: [ExpenseCategory] = ExpenseCategory.defaults
    @Published var isEnglish: Bool = false
    @Published var monthlyBudget: Double = 1500.0
    @Published var currentCity: String = "Karlsruhe, Deutschland"
    @Published var currentCoordinate: CLLocationCoordinate2D = CLLocationCoordinate2D(latitude: 49.0069, longitude: 8.4037)
    
    private let locationManager = CLLocationManager()
    private let geocoder = CLGeocoder()
    private let expensesKey = "trackmycent_expenses"
    
    override init() {
        super.init()
        load()
        setupLocation()
    }
    
    func setupLocation() {
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
        locationManager.requestWhenInUseAuthorization()
        locationManager.startUpdatingLocation()
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        currentCoordinate = location.coordinate
        geocoder.reverseGeocodeLocation(location) { [weak self] placemarks, _ in
            if let p = placemarks?.first {
                let street = p.thoroughfare ?? ""
                let city = p.locality ?? p.subAdministrativeArea ?? ""
                let text = [street, city].filter { !$0.isEmpty }.joined(separator: ", ")
                DispatchQueue.main.async {
                    if !text.isEmpty {
                        self?.currentCity = text
                    }
                }
            }
        }
    }
    
    private let cityCoordinates: [String: CLLocationCoordinate2D] = [
        "karlsruhe": CLLocationCoordinate2D(latitude: 49.0069, longitude: 8.4037),
        "münchen": CLLocationCoordinate2D(latitude: 48.1371, longitude: 11.5754),
        "munchen": CLLocationCoordinate2D(latitude: 48.1371, longitude: 11.5754),
        "berlin": CLLocationCoordinate2D(latitude: 52.5200, longitude: 13.4050),
        "hamburg": CLLocationCoordinate2D(latitude: 53.5511, longitude: 9.9937),
        "köln": CLLocationCoordinate2D(latitude: 50.9375, longitude: 6.9603),
        "koln": CLLocationCoordinate2D(latitude: 50.9375, longitude: 6.9603),
        "frankfurt": CLLocationCoordinate2D(latitude: 50.1109, longitude: 8.6821),
        "stuttgart": CLLocationCoordinate2D(latitude: 48.7758, longitude: 9.1829),
        "wien": CLLocationCoordinate2D(latitude: 48.2082, longitude: 16.3738),
        "zürich": CLLocationCoordinate2D(latitude: 47.3769, longitude: 8.5417)
    ]
    
    func resolveLocation(name: String, completion: @escaping (CLLocationCoordinate2D) -> Void) {
        let clean = name.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        for (key, coord) in cityCoordinates {
            if clean.contains(key) || key.contains(clean) {
                completion(coord)
                return
            }
        }
        geocoder.geocodeAddressString(name) { placemarks, _ in
            if let loc = placemarks?.first?.location {
                completion(loc.coordinate)
            } else {
                completion(self.currentCoordinate)
            }
        }
    }
    
    func addExpense(_ expense: Expense) {
        expenses.insert(expense, at: 0)
    }
    
    func deleteExpense(id: String) {
        expenses.removeAll { $0.id == id }
    }
    
    func category(for id: String) -> ExpenseCategory {
        categories.first { $0.id == id } ?? ExpenseCategory(id: id, nameDe: "Sonstiges", nameEn: "Other", iconName: "ellipsis.circle.fill", hexColor: "#757575")
    }
    
    func formatCurrency(_ amount: Double) -> String {
        let f = NumberFormatter()
        f.numberStyle = .currency
        f.currencyCode = isEnglish ? "USD" : "EUR"
        f.locale = isEnglish ? Locale(identifier: "en_US") : Locale(identifier: "de_DE")
        return f.string(from: NSNumber(value: amount)) ?? String(format: "%.2f €", amount)
    }
    
    private func save() {
        if let data = try? JSONEncoder().encode(expenses) {
            UserDefaults.standard.set(data, forKey: expensesKey)
        }
    }
    
    private func load() {
        if let data = UserDefaults.standard.data(forKey: expensesKey),
           let list = try? JSONDecoder().decode([Expense].self, from: data) {
            expenses = list
        } else {
            // Demo data
            expenses = [
                Expense(title: "Supermarkt Einkauf", amount: 48.70, categoryId: "cat_lebensmittel", date: Date(), paymentMethod: "Karte", locationName: "Karlsruhe"),
                Expense(title: "Mittagessen Restaurant", amount: 16.50, categoryId: "cat_gastronomie", date: Date().addingTimeInterval(-3600 * 5), paymentMethod: "Karte", locationName: "Karlsruhe"),
                Expense(title: "Straßenbahn Ticket", amount: 3.20, categoryId: "cat_transport", date: Date().addingTimeInterval(-3600 * 24), paymentMethod: "Bar", locationName: "Karlsruhe"),
                Expense(title: "Kaffee & Brezel", amount: 4.50, categoryId: "cat_gastronomie", date: Date().addingTimeInterval(-3600 * 48), paymentMethod: "Bar", locationName: "Karlsruhe")
            ]
        }
    }
}
