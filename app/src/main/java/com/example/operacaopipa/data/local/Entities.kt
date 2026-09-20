package com.example.operacaopipa.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trucks")
data class TruckEntity(
    @PrimaryKey val id: String,
    val plate: String,
    val driverName: String,
    val lastLat: Double,
    val lastLng: Double,
    val lastUpdate: String,
    val status: String
)

@Entity(tableName = "deliveries")
data class DeliveryEntity(
    @PrimaryKey val id: String,
    val truckId: String,
    val residentName: String,
    val residentId: String,
    val address: String,
    val neighborhood: String,
    val referencePoint: String,
    val phone: String,
    val councilman: String,
    val status: String,
    val lat: Double,
    val lng: Double,
    val createdAt: String,
    val photo: String?
)

@Entity(tableName = "sitios")
data class SitioEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val description: String,
    val type: String,
    val fuelType: String,
    val referencePoint: String
)

@Entity(tableName = "residents")
data class ResidentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String,
    val neighborhood: String,
    val phone: String,
    val lat: Double,
    val lng: Double,
    val referencePoint: String,
    val councilman: String,
    val category: String
)

@Entity(tableName = "drivers")
data class DriverEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String
)

@Entity(tableName = "councilmen")
data class CouncilmanEntity(
    @PrimaryKey val id: String,
    val name: String
)
