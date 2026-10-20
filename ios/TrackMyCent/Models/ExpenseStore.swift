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
