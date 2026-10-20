import Foundation
import CoreLocation

enum PaymentMethod: String, CaseIterable, Identifiable, Codable {
    case card = "Karte"
    case cash = "Bar"
    case online = "Online"
    
    var id: String { rawValue }
    
    var iconName: String {
        switch self {
        case .card: return "creditcard.fill"
        case .cash: return "banknote.fill"
        case .online: return "globe"
        }
    }
}

struct ExpenseCategory: Identifiable, Hashable, Codable {
    let id: String
    let nameDe: String
    let nameEn: String
    let iconName: String
    let hexColor: String
    
    func name(isEnglish: Bool) -> String {
        isEnglish ? nameEn : nameDe
    }
    
    static let defaults: [ExpenseCategory] = [
        ExpenseCategory(id: "cat_gastronomie", nameDe: "Gastronomie", nameEn: "Dining", iconName: "fork.knife", hexColor: "#FF5722"),
        ExpenseCategory(id: "cat_lebensmittel", nameDe: "Lebensmittel", nameEn: "Groceries", iconName: "cart.fill", hexColor: "#4CAF50"),
        ExpenseCategory(id: "cat_transport", nameDe: "Transport", nameEn: "Transit", iconName: "car.fill", hexColor: "#2196F3"),
        ExpenseCategory(id: "cat_wohnen", nameDe: "Wohnen & Fix", nameEn: "Housing", iconName: "house.fill", hexColor: "#9C27B0"),
        ExpenseCategory(id: "cat_freizeit", nameDe: "Freizeit", nameEn: "Leisure", iconName: "gamecontroller.fill", hexColor: "#E91E63"),
        ExpenseCategory(id: "cat_shopping", nameDe: "Shopping", nameEn: "Shopping", iconName: "bag.fill", hexColor: "#FF9800"),
        ExpenseCategory(id: "cat_gesundheit", nameDe: "Gesundheit", nameEn: "Health", iconName: "cross.case.fill", hexColor: "#00BCD4"),
        ExpenseCategory(id: "cat_sonstiges", nameDe: "Sonstiges", nameEn: "Other", iconName: "ellipsis.circle.fill", hexColor: "#757575")
    ]
}

struct Expense: Identifiable, Codable {
    var id: String = UUID().uuidString
    var title: String
    var amount: Double
    var categoryId: String
    var date: Date = Date()
    var paymentMethod: String = "Karte"
    var locationName: String = "Karlsruhe, Deutschland"
    var latitude: Double? = 49.0069
    var longitude: Double? = 8.4037
    var notes: String = ""
}

struct QuickAddPreset: Identifiable {
    let id = UUID()
    let titleDe: String
    let titleEn: String
    let defaultAmount: Double
    let categoryId: String
    let defaultPaymentMethod: String
    let iconName: String
    
    func title(isEnglish: Bool) -> String {
        isEnglish ? titleEn : titleDe
    }
    
    static let items: [QuickAddPreset] = [
        QuickAddPreset(titleDe: "Kaffee & Snack", titleEn: "Coffee & Snack", defaultAmount: 3.80, categoryId: "cat_gastronomie", defaultPaymentMethod: "Bar", iconName: "cup.and.saucer.fill"),
        QuickAddPreset(titleDe: "Mittagessen", titleEn: "Lunch", defaultAmount: 12.50, categoryId: "cat_gastronomie", defaultPaymentMethod: "Karte", iconName: "fork.knife"),
        QuickAddPreset(titleDe: "Supermarkt", titleEn: "Groceries", defaultAmount: 35.00, categoryId: "cat_lebensmittel", defaultPaymentMethod: "Karte", iconName: "cart.fill"),
        QuickAddPreset(titleDe: "Tanken / Ticket", titleEn: "Transit / Fuel", defaultAmount: 25.00, categoryId: "cat_transport", defaultPaymentMethod: "Karte", iconName: "car.fill")
    ]
}
