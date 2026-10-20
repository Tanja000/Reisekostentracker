import SwiftUI

struct BudgetsView: View {
    @ObservedObject var store: ExpenseStore
    @State private var activeQuickAdd: QuickAddPreset? = nil
    @State private var isEditingBudget = false
    @State private var budgetInput = ""
    
    var spentThisMonth: Double {
        store.expenses.reduce(0) { $0 + $1.amount }
    }
    
    var remainingBudget: Double {
        store.monthlyBudget - spentThisMonth
    }
    
    var progress: Double {
        store.monthlyBudget > 0 ? min(1.0, spentThisMonth / store.monthlyBudget) : 0
    }
    
    var dailyAllowance: Double {
        let cal = Calendar.current
        let range = cal.range(of: .day, in: .month, for: Date())
        let totalDays = range?.count ?? 30
        let currentDay = cal.component(.day, from: Date())
        let daysLeft = max(1, totalDays - currentDay + 1)
        return remainingBudget > 0 ? remainingBudget / Double(daysLeft) : 0
    }
    
    var body: some View {
        ZStack {
            NavigationView {
                ScrollView {
                    VStack(spacing: 16) {
                        // Monthly Budget Card
                        VStack(alignment: .leading, spacing: 14) {
                            HStack {
                                HStack(spacing: 8) {
                                    Image(systemName: "wallet.pass.fill")
                                        .foregroundColor(.blue)
                                    Text(store.isEnglish ? "Monthly Budget" : "Monatsbudget")
                                        .font(.headline)
                                        .bold()
                                }
                                Spacer()
                                Button(action: {
                                    budgetInput = String(format: "%.0f", store.monthlyBudget)
                                    isEditingBudget = true
                                }) {
                                    Image(systemName: "pencil.circle.fill")
                                        .font(.title3)
                                        .foregroundColor(.blue)
                                }
                            }
                            
                            HStack(alignment: .lastTextBaseline) {
                                Text(store.formatCurrency(store.monthlyBudget))
                                    .font(.system(size: 28, weight: .bold))
                                Spacer()
                                Text("\(Int(progress * 100))% \(store.isEnglish ? "spent" : "ausgegeben")")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            
                            ProgressView(value: progress)
                                .accentColor(progress > 0.9 ? .red : (progress > 0.75 ? .orange : .blue))
                            
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(store.isEnglish ? "Spent" : "Ausgegeben")
                                        .font(.caption2)
                                        .foregroundColor(.secondary)
                                    Text(store.formatCurrency(spentThisMonth))
                                        .font(.subheadline)
                                        .bold()
                                }
                                Spacer()
                                VStack(alignment: .trailing, spacing: 2) {
                                    Text(store.isEnglish ? "Remaining" : "Restbudget")
                                        .font(.caption2)
                                        .foregroundColor(remainingBudget >= 0 ? .secondary : .red)
                                    Text(store.formatCurrency(remainingBudget))
                                        .font(.subheadline)
                                        .bold()
                                        .foregroundColor(remainingBudget >= 0 ? .blue : .red)
                                }
                            }
                            
                            // Daily allowance chip
                            HStack {
                                Text(store.isEnglish ? "Daily budget:" : "Tagesbudget:")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                Spacer()
                                Text("~ \(store.formatCurrency(dailyAllowance)) / \(store.isEnglish ? "day" : "Tag")")
                                    .font(.caption)
                                    .bold()
                                    .foregroundColor(.blue)
                            }
                            .padding(8)
                            .background(Color(UIColor.systemBackground))
                            .cornerRadius(8)
                        }
                        .padding(18)
                        .background(Color(UIColor.secondarySystemBackground))
                        .cornerRadius(18)
                        .padding(.horizontal)
                        
                        // Quick Add Section
                        VStack(alignment: .leading, spacing: 10) {
                            Text(store.isEnglish ? "⚡ Quick Booking (1-Click)" : "⚡ Schnellbuchung (1-Klick)")
                                .font(.headline)
                                .bold()
                            Text(store.isEnglish ? "Book frequent daily expenses immediately" : "Häufige Alltagsausgaben sofort eintragen")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            
                            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                                ForEach(QuickAddPreset.items) { preset in
                                    Button(action: { activeQuickAdd = preset }) {
                                        HStack(spacing: 10) {
                                            Image(systemName: preset.iconName)
                                                .foregroundColor(.blue)
                                                .frame(width: 32, height: 32)
                                                .background(Color.blue.opacity(0.12))
                                                .clipShape(Circle())
                                            
                                            VStack(alignment: .leading, spacing: 2) {
                                                Text(preset.title(isEnglish: store.isEnglish))
                                                    .font(.caption)
                                                    .bold()
                                                    .foregroundColor(.primary)
                                                Text(store.formatCurrency(preset.defaultAmount))
                                                    .font(.caption2)
                                                    .bold()
                                                    .foregroundColor(.blue)
                                            }
                                            Spacer()
                                        }
                                        .padding(10)
                                        .background(Color(UIColor.secondarySystemBackground))
                                        .cornerRadius(12)
                                    }
                                }
                            }
                        }
                        .padding(.horizontal)
                        
                        // Wallets & Payment Breakdown
                        VStack(alignment: .leading, spacing: 10) {
                            Text(store.isEnglish ? "Payment Methods & Wallets" : "Zahlungsarten & Wallets")
                                .font(.headline)
                                .bold()
                            
                            VStack(spacing: 8) {
                                ForEach(["Karte", "Bar", "Online"], id: \.self) { method in
                                    let sum = store.expenses.filter { $0.paymentMethod == method }.reduce(0) { $0 + $1.amount }
                                    HStack {
                                        Text(method)
                                            .font(.subheadline)
                                        Spacer()
                                        Text(store.formatCurrency(sum))
                                            .font(.subheadline)
                                            .bold()
                                    }
                                    .padding(.vertical, 4)
                                    if method != "Online" { Divider() }
                                }
                            }
                            .padding(14)
                            .background(Color(UIColor.secondarySystemBackground))
                            .cornerRadius(14)
                        }
                        .padding(.horizontal)
                        
                        // Export CSV Button
                        Button(action: exportCSV) {
                            HStack {
                                Image(systemName: "square.and.arrow.up")
                                Text(store.isEnglish ? "Export All Expenses as CSV" : "Alle Finanzen als CSV exportieren")
                            }
                            .font(.subheadline)
                            .bold()
                            .frame(maxWidth: .infinity)
                            .padding(14)
                            .background(Color(UIColor.secondarySystemBackground))
                            .cornerRadius(12)
                        }
                        .padding(.horizontal)
                    }
                    .padding(.bottom, 80)
                }
                .navigationTitle(store.isEnglish ? "Budgets & Finance" : "Budgets & Finanzen")
                .alert(store.isEnglish ? "Set Monthly Budget" : "Monatsbudget festlegen", isPresented: $isEditingBudget) {
                    TextField(store.isEnglish ? "Budget in €" : "Budget in €", text: $budgetInput)
                        .keyboardType(.numberPad)
                    Button(store.isEnglish ? "Save" : "Speichern") {
                        if let b = Double(budgetInput) { store.monthlyBudget = b }
                    }
                    Button(store.isEnglish ? "Cancel" : "Abbrechen", role: .cancel) {}
                }
            }
            
            if let preset = activeQuickAdd {
                QuickAddDialog(store: store, preset: preset, onDismiss: { activeQuickAdd = nil })
            }
        }
    }
    
    private func exportCSV() {
        var csv = "Titel;Betrag;Kategorie;Zahlungsart;Ort;Datum\n"
        for exp in store.expenses {
            csv += "\(exp.title);\(exp.amount);\(exp.categoryId);\(exp.paymentMethod);\(exp.locationName);\(exp.date)\n"
        }
        let av = UIActivityViewController(activityItems: [csv], applicationActivities: nil)
        if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let root = scene.windows.first?.rootViewController {
            root.present(av, animated: true)
        }
    }
}
