package com.example.operacaopipa.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.operacaopipa.data.model.Councilman
import com.example.operacaopipa.data.model.Delivery
import com.example.operacaopipa.data.model.DeliveryStatus
import com.example.operacaopipa.data.model.Driver
import com.example.operacaopipa.data.model.FuelType
import com.example.operacaopipa.data.model.Resident
import com.example.operacaopipa.data.model.Sitio
import com.example.operacaopipa.data.model.SitioType
import com.example.operacaopipa.data.model.Truck
import com.example.operacaopipa.data.model.TruckStatus
import com.example.operacaopipa.data.repository.PipaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    LOGIN,
    ADMIN_DASHBOARD,
    DRIVER_DASHBOARD
}

enum class AdminTab {
    MAP,
    DELIVERIES,
    REGISTRATIONS,
    PERFORMANCE,
    SETTINGS
}

data class CouncilmanPerformance(
    val name: String,
    val totalDeliveries: Int,
    val deliveredCount: Int,
    val pendingCount: Int
)

class PipaViewModel(private val repository: PipaRepository) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _adminTab = MutableStateFlow(AdminTab.MAP)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    private val _selectedDriverTruckId = MutableStateFlow<String?>("truck_1")
    val selectedDriverTruckId: StateFlow<String?> = _selectedDriverTruckId.asStateFlow()

    private val _adminPassword = MutableStateFlow("123456")
    val adminPassword: StateFlow<String> = _adminPassword.asStateFlow()

    private val _adminAuthError = MutableStateFlow<String?>(null)
    val adminAuthError: StateFlow<String?> = _adminAuthError.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val trucks = repository.allTrucks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val deliveries = repository.allDeliveries.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val sitios = repository.allSitios.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val residents = repository.allResidents.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val drivers = repository.allDrivers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val councilmen = repository.allCouncilmen.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val councilmanPerformance: StateFlow<List<CouncilmanPerformance>> = combine(
        councilmen,
        deliveries
    ) { councilList, deliveryList ->
        val map = mutableMapOf<String, Triple<Int, Int, Int>>() // total, delivered, pending
        councilList.forEach { c ->
            map[c.name] = Triple(0, 0, 0)
        }
        deliveryList.forEach { d ->
            val cName = if (d.councilman.isNotBlank()) d.councilman else "Não Especificado"
            val current = map[cName] ?: Triple(0, 0, 0)
            val isDelivered = if (d.status == DeliveryStatus.DELIVERED) 1 else 0
            val isPending = if (d.status != DeliveryStatus.DELIVERED) 1 else 0
            map[cName] = Triple(current.first + 1, current.second + isDelivered, current.third + isPending)
        }
        map.map { (name, stats) ->
            CouncilmanPerformance(
                name = name,
                totalDeliveries = stats.first,
                deliveredCount = stats.second,
                pendingCount = stats.third
            )
        }.sortedByDescending { it.totalDeliveries }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        _adminAuthError.value = null
    }

    fun setAdminTab(tab: AdminTab) {
        _adminTab.value = tab
    }

    fun setSelectedTruck(truckId: String) {
        _selectedDriverTruckId.value = truckId
    }

    fun loginAdmin(passInput: String): Boolean {
        return if (passInput == _adminPassword.value) {
            _adminAuthError.value = null
            _currentScreen.value = AppScreen.ADMIN_DASHBOARD
            true
        } else {
            _adminAuthError.value = "Senha incorreta. Padrão: 123456"
            false
        }
    }

    fun updateAdminPassword(newPass: String) {
        if (newPass.length >= 4) {
            _adminPassword.value = newPass
            _statusMessage.value = "Senha de administrador atualizada!"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun createDelivery(
        truckId: String,
        residentName: String,
        address: String,
        neighborhood: String,
        referencePoint: String,
        councilman: String,
        phone: String
    ) {
        viewModelScope.launch {
            val newDelivery = Delivery(
                id = UUID.randomUUID().toString(),
                truckId = truckId,
                residentName = residentName,
                address = address,
                neighborhood = neighborhood,
                referencePoint = referencePoint,
                councilman = councilman,
                phone = phone,
                status = DeliveryStatus.PENDING,
                createdAt = "Hoje, ${System.currentTimeMillis() % 86400000 / 3600000}:${(System.currentTimeMillis() % 3600000 / 60000).toString().padStart(2, '0')}",
                lat = -9.2155 + (Math.random() - 0.5) * 0.05,
                lng = -36.3488 + (Math.random() - 0.5) * 0.05
            )
            repository.saveDelivery(newDelivery)
            _statusMessage.value = "Entrega agendada com sucesso!"
        }
    }

    fun completeDelivery(deliveryId: String, photoData: String? = null) {
        viewModelScope.launch {
            repository.completeDelivery(deliveryId, photoData)
            _statusMessage.value = "Entrega confirmada com sucesso!"
        }
    }

    fun updateDeliveryStatus(deliveryId: String, status: DeliveryStatus) {
        viewModelScope.launch {
            repository.updateDeliveryStatus(deliveryId, status)
        }
    }

    fun deleteDelivery(deliveryId: String) {
        viewModelScope.launch {
            repository.deleteDelivery(deliveryId)
            _statusMessage.value = "Entrega removida."
        }
    }

    fun clearDelivered() {
        viewModelScope.launch {
            repository.clearCompletedDeliveries()
            _statusMessage.value = "Entregas concluídas limpas."
        }
    }

    fun saveTruck(plate: String, driverName: String) {
        viewModelScope.launch {
            val newTruck = Truck(
                id = UUID.randomUUID().toString(),
                plate = plate.uppercase(),
                driverName = driverName,
                status = TruckStatus.IDLE
            )
            repository.saveTruck(newTruck)
            _statusMessage.value = "Caminhão $plate cadastrado."
        }
    }

    fun deleteTruck(truckId: String) {
        viewModelScope.launch {
            repository.deleteTruck(truckId)
        }
    }

    fun saveSitio(
        name: String,
        description: String,
        type: SitioType,
        fuelType: FuelType = FuelType.NONE,
        referencePoint: String = "",
        lat: Double = -9.2155,
        lng: Double = -36.3488
    ) {
        viewModelScope.launch {
            val newSitio = Sitio(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                type = type,
                fuelType = fuelType,
                referencePoint = referencePoint,
                lat = lat,
                lng = lng
            )
            repository.saveSitio(newSitio)
            _statusMessage.value = "Ponto / Sítio $name cadastrado."
        }
    }

    fun deleteSitio(id: String) {
        viewModelScope.launch {
            repository.deleteSitio(id)
        }
    }

    fun saveResident(
        name: String,
        address: String,
        neighborhood: String,
        phone: String,
        councilman: String,
        referencePoint: String
    ) {
        viewModelScope.launch {
            val newResident = Resident(
                id = UUID.randomUUID().toString(),
                name = name,
                address = address,
                neighborhood = neighborhood,
                phone = phone,
                councilman = councilman,
                referencePoint = referencePoint
            )
            repository.saveResident(newResident)
            _statusMessage.value = "Morador $name cadastrado."
        }
    }

    fun deleteResident(id: String) {
        viewModelScope.launch {
            repository.deleteResident(id)
        }
    }

    fun updateTruckGps(truckId: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.updateTruckLocation(truckId, lat, lng, "Agora")
        }
    }
}

class PipaViewModelFactory(private val repository: PipaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PipaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PipaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
