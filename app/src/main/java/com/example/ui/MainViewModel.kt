package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.DashboardMetrics
import com.example.data.model.Expense
import com.example.data.model.Invoice
import com.example.data.model.NavigationTab
import com.example.data.model.PaymentRecord
import com.example.data.model.ServiceItem
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AppRepository(database.appDao())

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(NavigationTab.OVERVIEW)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    // Domain Data Flows
    val customers: StateFlow<List<Customer>> = repository.customers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val services: StateFlow<List<ServiceItem>> = repository.services
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<Invoice>> = repository.invoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentRecord>> = repository.payments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<BusinessSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessSettings())

    // Computed Dashboard Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        invoices,
        expenses,
        payments
    ) { invList, expList, payList ->
        val revenue = invList.sumOf { it.total }
        val cash = invList.sumOf { it.paidAmount }
        val dues = invList.sumOf { it.dueAmount }
        val exp = expList.sumOf { it.amount }
        val net = cash - exp
        val pendingCount = invList.count { it.dueAmount > 0.01 }

        val methods = mutableMapOf<String, Double>()
        for (pay in payList) {
            methods[pay.method] = (methods[pay.method] ?: 0.0) + pay.amount
        }

        DashboardMetrics(
            invoicedRevenue = revenue,
            cashReceived = cash,
            outstandingDues = dues,
            operatingExpenses = exp,
            netCash = net,
            totalInvoicesCount = invList.size,
            pendingDuesCount = pendingCount,
            methodBreakdown = methods
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Search and Filters
    private val _invoiceSearch = MutableStateFlow("")
    val invoiceSearch: StateFlow<String> = _invoiceSearch.asStateFlow()
    fun setInvoiceSearch(q: String) { _invoiceSearch.value = q }

    private val _invoiceStatusFilter = MutableStateFlow("All")
    val invoiceStatusFilter: StateFlow<String> = _invoiceStatusFilter.asStateFlow()
    fun setInvoiceStatusFilter(f: String) { _invoiceStatusFilter.value = f }

    private val _serviceSearch = MutableStateFlow("")
    val serviceSearch: StateFlow<String> = _serviceSearch.asStateFlow()
    fun setServiceSearch(q: String) { _serviceSearch.value = q }

    private val _serviceCategoryFilter = MutableStateFlow("All")
    val serviceCategoryFilter: StateFlow<String> = _serviceCategoryFilter.asStateFlow()
    fun setServiceCategoryFilter(c: String) { _serviceCategoryFilter.value = c }

    private val _customerSearch = MutableStateFlow("")
    val customerSearch: StateFlow<String> = _customerSearch.asStateFlow()
    fun setCustomerSearch(q: String) { _customerSearch.value = q }

    private val _expenseSearch = MutableStateFlow("")
    val expenseSearch: StateFlow<String> = _expenseSearch.asStateFlow()
    fun setExpenseSearch(q: String) { _expenseSearch.value = q }

    // Dialog & Sheet States
    private val _isCreateInvoiceOpen = MutableStateFlow(false)
    val isCreateInvoiceOpen: StateFlow<Boolean> = _isCreateInvoiceOpen.asStateFlow()
    fun setCreateInvoiceOpen(open: Boolean) { _isCreateInvoiceOpen.value = open }

    private val _selectedInvoiceForReceipt = MutableStateFlow<Invoice?>(null)
    val selectedInvoiceForReceipt: StateFlow<Invoice?> = _selectedInvoiceForReceipt.asStateFlow()
    fun openReceiptForInvoice(invoice: Invoice?) { _selectedInvoiceForReceipt.value = invoice }

    private val _selectedInvoiceForPayment = MutableStateFlow<Invoice?>(null)
    val selectedInvoiceForPayment: StateFlow<Invoice?> = _selectedInvoiceForPayment.asStateFlow()
    fun openPaymentForInvoice(invoice: Invoice?) { _selectedInvoiceForPayment.value = invoice }

    private val _selectedCustomerForStatement = MutableStateFlow<Customer?>(null)
    val selectedCustomerForStatement: StateFlow<Customer?> = _selectedCustomerForStatement.asStateFlow()
    fun openCustomerStatement(customer: Customer?) { _selectedCustomerForStatement.value = customer }

    private val _isAddCustomerOpen = MutableStateFlow(false)
    val isAddCustomerOpen: StateFlow<Boolean> = _isAddCustomerOpen.asStateFlow()
    private val _editingCustomer = MutableStateFlow<Customer?>(null)
    val editingCustomer: StateFlow<Customer?> = _editingCustomer.asStateFlow()
    fun openAddCustomer(customer: Customer? = null) {
        _editingCustomer.value = customer
        _isAddCustomerOpen.value = true
    }
    fun closeAddCustomer() {
        _isAddCustomerOpen.value = false
        _editingCustomer.value = null
    }

    private val _isAddServiceOpen = MutableStateFlow(false)
    val isAddServiceOpen: StateFlow<Boolean> = _isAddServiceOpen.asStateFlow()
    private val _editingService = MutableStateFlow<ServiceItem?>(null)
    val editingService: StateFlow<ServiceItem?> = _editingService.asStateFlow()
    fun openAddService(service: ServiceItem? = null) {
        _editingService.value = service
        _isAddServiceOpen.value = true
    }
    fun closeAddService() {
        _isAddServiceOpen.value = false
        _editingService.value = null
    }

    private val _isManageCategoriesOpen = MutableStateFlow(false)
    val isManageCategoriesOpen: StateFlow<Boolean> = _isManageCategoriesOpen.asStateFlow()
    fun setManageCategoriesOpen(open: Boolean) { _isManageCategoriesOpen.value = open }

    private val _isAddExpenseOpen = MutableStateFlow(false)
    val isAddExpenseOpen: StateFlow<Boolean> = _isAddExpenseOpen.asStateFlow()
    fun setAddExpenseOpen(open: Boolean) { _isAddExpenseOpen.value = open }

    // Mutations
    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            closeAddCustomer()
        }
    }

    fun deleteCustomer(id: String) {
        viewModelScope.launch {
            repository.deleteCustomer(id)
        }
    }

    fun saveService(service: ServiceItem) {
        viewModelScope.launch {
            repository.saveService(service)
            closeAddService()
        }
    }

    fun deleteService(id: String) {
        viewModelScope.launch {
            repository.deleteService(id)
        }
    }

    fun addCategory(category: String) {
        viewModelScope.launch {
            repository.saveCategory(category)
        }
    }

    fun deleteCategory(category: String) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun createInvoice(invoice: Invoice, onCreated: (Invoice) -> Unit) {
        viewModelScope.launch {
            val nextNum = settings.value.nextInvoiceNumber
            val created = repository.createInvoice(invoice, nextNum)
            _isCreateInvoiceOpen.value = false
            onCreated(created)
        }
    }

    fun recordPayment(
        invoice: Invoice,
        amount: Double,
        method: String,
        date: String,
        reference: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.recordPayment(invoice, amount, method, date, reference, note)
            _selectedInvoiceForPayment.value = null
        }
    }

    fun deleteInvoice(id: String) {
        viewModelScope.launch {
            repository.deleteInvoice(id)
            if (_selectedInvoiceForReceipt.value?.id == id) {
                _selectedInvoiceForReceipt.value = null
            }
        }
    }

    fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            repository.saveExpense(expense)
            _isAddExpenseOpen.value = false
        }
    }

    fun deleteExpense(id: String) {
        viewModelScope.launch {
            repository.deleteExpense(id)
        }
    }

    fun updateSettings(settings: BusinessSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }

    fun clearTransactions() {
        viewModelScope.launch {
            repository.clearTransactions()
        }
    }
}
