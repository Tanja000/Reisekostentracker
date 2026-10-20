import SwiftUI

struct ExpensesView: View {
    @ObservedObject var store: ExpenseStore
    @State private var searchText = ""
    @State private var selectedCategoryId: String? = nil
    @State private var activeQuickAdd: QuickAddPreset? = nil
    @State private var isShowingAddSheet = false
    
    var filteredExpenses: [Expense] {
        store.expenses.filter { exp in
            let matchesSearch = searchText.isEmpty || exp.title.localizedCaseInsensitiveContains(searchText) || exp.notes.localizedCaseInsensitiveContains(searchText)
            let matchesCat = selectedCategoryId == nil || exp.categoryId == selectedCategoryId
            return matchesSearch && matchesCat
        }
    }
    
    var totalAmount: Double {
        filteredExpenses.reduce(0) { $0 + $1.amount }
    }
    
    var body: some View {
        ZStack {
            NavigationView {
                ScrollView {
                    VStack(spacing: 14) {
                        // Summary Card
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                Text(store.isEnglish ? "Total Expenses" : "Gesamtausgaben")
                                    .font(.subheadline)
                                    .foregroundColor(.white.opacity(0.85))
                                Spacer()
                                Text("\(filteredExpenses.count) \(store.isEnglish ? "entries" : "Einträge")")
                                    .font(.caption)
                                    .bold()
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 4)
                                    .background(Color.white.opacity(0.2))
                                    .clipShape(Capsule())
                                    .foregroundColor(.white)
                            }
                            Text(store.formatCurrency(totalAmount))
                                .font(.system(size: 32, weight: .heavy))
                                .foregroundColor(.white)
                        }
                        .padding(18)
                        .background(
                            LinearGradient(gradient: Gradient(colors: [Color.blue, Color.purple]), startPoint: .topLeading, endPoint: .bottomTrailing)
                        )
                        .cornerRadius(18)
                        .padding(.horizontal)
                        
                        // Search bar
                        HStack {
                            Image(systemName: "magnifyingglass")
                                .foregroundColor(.secondary)
                            TextField(store.isEnglish ? "Search expenses..." : "Ausgabe durchsuchen...", text: $searchText)
                            if !searchText.isEmpty {
                                Button(action: { searchText = "" }) {
                                    Image(systemName: "xmark.circle.fill")
                                        .foregroundColor(.secondary)
                                }
                            }
                        }
                        .padding(10)
                        .background(Color(UIColor.secondarySystemBackground))
                        .cornerRadius(12)
                        .padding(.horizontal)
                        
                        // Quick Add Horizontal Row
                        VStack(alignment: .leading, spacing: 6) {
                            HStack {
                                Text(store.isEnglish ? "⚡ Quick Booking" : "⚡ Schnellbuchung")
                                    .font(.subheadline)
                                    .bold()
                                    .foregroundColor(.blue)
                                Spacer()
                                Text(store.isEnglish ? "Tap to customize" : "Klick zum Anpassen")
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            }
                            .padding(.horizontal)
                            
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 8) {
                                    ForEach(QuickAddPreset.items) { preset in
                                        Button(action: { activeQuickAdd = preset }) {
                                            HStack(spacing: 6) {
                                                Image(systemName: preset.iconName)
                                                    .foregroundColor(.blue)
                                                    .font(.caption)
                                                Text(preset.title(isEnglish: store.isEnglish))
                                                    .font(.caption)
                                                    .bold()
                                                    .foregroundColor(.primary)
                                                Text(store.formatCurrency(preset.defaultAmount))
                                                    .font(.caption)
                                                    .bold()
                                                    .foregroundColor(.blue)
                                            }
                                            .padding(.horizontal, 10)
                                            .padding(.vertical, 8)
                                            .background(Color.blue.opacity(0.1))
                                            .cornerRadius(10)
                                        }
                                    }
                                }
                                .padding(.horizontal)
                            }
                        }
                        
                        // Category Filter Chips
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                Button(action: { selectedCategoryId = nil }) {
                                    Text(store.isEnglish ? "All" : "Alle")
                                        .font(.caption)
                                        .bold()
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 7)
                                        .background(selectedCategoryId == nil ? Color.blue : Color(UIColor.secondarySystemBackground))
                                        .foregroundColor(selectedCategoryId == nil ? .white : .primary)
                                        .cornerRadius(16)
                                }
                                
                                ForEach(store.categories) { cat in
                                    Button(action: {
                                        selectedCategoryId = (selectedCategoryId == cat.id) ? nil : cat.id
                                    }) {
                                        HStack(spacing: 4) {
                                            Image(systemName: cat.iconName)
                                            Text(cat.name(isEnglish: store.isEnglish))
                                        }
                                        .font(.caption)
                                        .bold()
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 7)
                                        .background(selectedCategoryId == cat.id ? Color.blue : Color(UIColor.secondarySystemBackground))
                                        .foregroundColor(selectedCategoryId == cat.id ? .white : .primary)
                                        .cornerRadius(16)
                                    }
                                }
                            }
                            .padding(.horizontal)
                        }
                        
                        // Expenses List
                        if filteredExpenses.isEmpty {
                            VStack(spacing: 8) {
                                Image(systemName: "tray")
                                    .font(.system(size: 40))
                                    .foregroundColor(.secondary)
                                    .padding(.top, 30)
                                Text(store.isEnglish ? "No expenses found" : "Keine Ausgaben gefunden")
                                    .font(.headline)
                                    .foregroundColor(.secondary)
                            }
                        } else {
                            LazyVStack(spacing: 10) {
                                ForEach(filteredExpenses) { expense in
                                    let cat = store.category(for: expense.categoryId)
                                    HStack(spacing: 12) {
                                        Image(systemName: cat.iconName)
                                            .font(.system(size: 20))
                                            .foregroundColor(.blue)
                                            .frame(width: 42, height: 42)
                                            .background(Color.blue.opacity(0.12))
                                            .clipShape(Circle())
                                        
                                        VStack(alignment: .leading, spacing: 3) {
                                            Text(expense.title)
                                                .font(.headline)
                                                .lineLimit(1)
                                            HStack(spacing: 4) {
                                                Text(cat.name(isEnglish: store.isEnglish))
                                                    .font(.caption2)
                                                    .foregroundColor(.blue)
                                                Text("•")
                                                    .font(.caption2)
                                                    .foregroundColor(.secondary)
                                                Text(expense.paymentMethod)
                                                    .font(.caption2)
                                                    .bold()
                                                    .foregroundColor(.secondary)
                                                Text("•")
                                                    .font(.caption2)
                                                    .foregroundColor(.secondary)
                                                Text(expense.locationName)
                                                    .font(.caption2)
                                                    .foregroundColor(.secondary)
                                                    .lineLimit(1)
                                            }
                                        }
                                        
                                        Spacer()
                                        
                                        VStack(alignment: .trailing, spacing: 3) {
                                            Text(store.formatCurrency(expense.amount))
                                                .font(.headline)
                                                .bold()
                                            Button(action: { store.deleteExpense(id: expense.id) }) {
                                                Image(systemName: "trash")
                                                    .font(.caption)
                                                    .foregroundColor(.red.opacity(0.7))
                                            }
                                        }
                                    }
                                    .padding(12)
                                    .background(Color(UIColor.secondarySystemBackground))
                                    .cornerRadius(14)
                                    .padding(.horizontal)
                                }
                            }
                        }
                    }
                    .padding(.bottom, 80)
                }
                .navigationTitle("TrackMyCent")
                .toolbar {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        HStack(spacing: 6) {
                            Button(action: { store.isEnglish = false }) {
                                Text("🇩🇪")
                                    .font(.title3)
                                    .opacity(store.isEnglish ? 0.4 : 1.0)
                            }
                            Button(action: { store.isEnglish = true }) {
                                Text("🇺🇸")
                                    .font(.title3)
                                    .opacity(store.isEnglish ? 1.0 : 0.4)
                            }
                        }
                    }
                }
            }
            
            // Quick Add Dialog Overlay
            if let preset = activeQuickAdd {
                QuickAddDialog(store: store, preset: preset, onDismiss: { activeQuickAdd = nil })
            }
        }
    }
}
