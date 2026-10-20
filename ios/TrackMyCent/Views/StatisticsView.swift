import SwiftUI

struct StatisticsView: View {
    @ObservedObject var store: ExpenseStore
    
    var totalAmount: Double {
        store.expenses.reduce(0) { $0 + $1.amount }
    }
    
    var averageAmount: Double {
        store.expenses.isEmpty ? 0 : totalAmount / Double(store.expenses.count)
    }
    
    var maxExpense: Double {
        store.expenses.map { $0.amount }.max() ?? 0
    }
    
    var categoryBreakdown: [(ExpenseCategory, Double, Double)] {
        guard totalAmount > 0 else { return [] }
        return store.categories.compactMap { cat in
            let sum = store.expenses.filter { $0.categoryId == cat.id }.reduce(0) { $0 + $1.amount }
            if sum > 0 {
                return (cat, sum, sum / totalAmount)
            }
            return nil
        }.sorted { $0.1 > $1.1 }
    }
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 16) {
                    // KPI Row
                    HStack(spacing: 10) {
                        KPICard(
                            title: store.isEnglish ? "Total" : "Gesamt",
                            value: store.formatCurrency(totalAmount),
                            icon: "chart.pie.fill",
                            color: .blue
                        )
                        KPICard(
                            title: store.isEnglish ? "Average" : "Durchschnitt",
                            value: store.formatCurrency(averageAmount),
                            icon: "divide.circle.fill",
                            color: .purple
                        )
                        KPICard(
                            title: store.isEnglish ? "Max" : "Höchste",
                            value: store.formatCurrency(maxExpense),
                            icon: "arrow.up.circle.fill",
                            color: .orange
                        )
                    }
                    .padding(.horizontal)
                    
                    // Category Breakdown
                    VStack(alignment: .leading, spacing: 14) {
                        Text(store.isEnglish ? "Expenses by Category" : "Ausgaben nach Kategorie")
                            .font(.headline)
                            .bold()
                        
                        if categoryBreakdown.isEmpty {
                            Text(store.isEnglish ? "No expenses recorded yet." : "Noch keine Ausgaben erfasst.")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        } else {
                            ForEach(categoryBreakdown, id: \.0.id) { cat, sum, pct in
                                VStack(spacing: 6) {
                                    HStack {
                                        Image(systemName: cat.iconName)
                                            .foregroundColor(.blue)
                                        Text(cat.name(isEnglish: store.isEnglish))
                                            .font(.subheadline)
                                        Spacer()
                                        Text(store.formatCurrency(sum))
                                            .font(.subheadline)
                                            .bold()
                                        Text(String(format: "(%.1f%%)", pct * 100))
                                            .font(.caption2)
                                            .foregroundColor(.secondary)
                                    }
                                    ProgressView(value: pct)
                                        .accentColor(.blue)
                                }
                                .padding(.vertical, 2)
                            }
                        }
                    }
                    .padding(16)
                    .background(Color(UIColor.secondarySystemBackground))
                    .cornerRadius(16)
                    .padding(.horizontal)
                }
                .padding(.bottom, 80)
            }
            .navigationTitle(store.isEnglish ? "Statistics" : "Statistik")
        }
    }
}

struct KPICard: View {
    let title: String
    let value: String
    let icon: String
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Image(systemName: icon)
                .font(.subheadline)
                .foregroundColor(color)
            Text(title)
                .font(.caption2)
                .foregroundColor(.secondary)
            Text(value)
                .font(.headline)
                .bold()
                .minimumScaleFactor(0.7)
                .lineLimit(1)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }
}
