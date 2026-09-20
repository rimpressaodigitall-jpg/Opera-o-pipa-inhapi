package com.example.operacaopipa.data.model

enum class TruckStatus {
    IDLE,
    IN_ROUTE,
    ARRIVED
}

enum class DeliveryStatus {
    PENDING,
    IN_ROUTE,
    DELIVERED
}

enum class SitioType {
    WATER,
    FUEL,
    SCHOOL,
    DAYCARE,
    RESIDENT,
    OTHER
}

enum class FuelType {
    DIESEL,
    GASOLINE,
    NONE
}

data class Truck(
    val id: String,
    val plate: String,
    val driverName: String,
    val lastLat: Double = -9.2155,
    val lastLng: Double = -36.3488,
    val lastUpdate: String = "",
    val status: TruckStatus = TruckStatus.IDLE
)

data class Delivery(
    val id: String,
    val truckId: String,
    val residentName: String,
    val residentId: String = "",
    val address: String,
    val neighborhood: String = "",
    val referencePoint: String = "",
    val phone: String = "",
    val councilman: String = "",
    val status: DeliveryStatus = DeliveryStatus.PENDING,
    val lat: Double = -9.2155,
    val lng: Double = -36.3488,
    val createdAt: String = "",
    val photo: String? = null // Base64 encoded or image URI
)

data class Sitio(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val description: String = "",
    val type: SitioType = SitioType.WATER,
    val fuelType: FuelType = FuelType.NONE,
    val referencePoint: String = ""
)

data class Resident(
    val id: String,
    val name: String,
    val address: String,
    val neighborhood: String = "",
    val phone: String = "",
    val lat: Double = -9.2155,
    val lng: Double = -36.3488,
    val referencePoint: String = "",
    val councilman: String = "",
    val category: String = "RESIDENT"
)

data class Driver(
    val id: String,
    val name: String,
    val phone: String
)

data class Councilman(
    val id: String,
    val name: String
)
