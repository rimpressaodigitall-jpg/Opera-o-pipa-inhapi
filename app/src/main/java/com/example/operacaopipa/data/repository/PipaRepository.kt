package com.example.operacaopipa.data.repository

import com.example.operacaopipa.data.local.DeliveryEntity
import com.example.operacaopipa.data.local.DriverEntity
import com.example.operacaopipa.data.local.PipaDao
import com.example.operacaopipa.data.local.ResidentEntity
import com.example.operacaopipa.data.local.SitioEntity
import com.example.operacaopipa.data.local.TruckEntity
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PipaRepository(private val dao: PipaDao) {

    val allTrucks: Flow<List<Truck>> = dao.getAllTrucks().map { list ->
        list.map { it.toModel() }
    }

    val allDeliveries: Flow<List<Delivery>> = dao.getAllDeliveries().map { list ->
        list.map { it.toModel() }
    }

    val allSitios: Flow<List<Sitio>> = dao.getAllSitios().map { list ->
        list.map { it.toModel() }
    }

    val allResidents: Flow<List<Resident>> = dao.getAllResidents().map { list ->
        list.map { it.toModel() }
    }

    val allDrivers: Flow<List<Driver>> = dao.getAllDrivers().map { list ->
        list.map { Driver(it.id, it.name, it.phone) }
    }

    val allCouncilmen: Flow<List<Councilman>> = dao.getAllCouncilmen().map { list ->
        list.map { Councilman(it.id, it.name) }
    }

    fun getDeliveriesForTruck(truckId: String): Flow<List<Delivery>> {
        return dao.getDeliveriesByTruck(truckId).map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun saveTruck(truck: Truck) {
        dao.insertTruck(
            TruckEntity(
                id = truck.id,
                plate = truck.plate,
                driverName = truck.driverName,
                lastLat = truck.lastLat,
                lastLng = truck.lastLng,
                lastUpdate = truck.lastUpdate,
                status = truck.status.name
            )
        )
    }

    suspend fun updateTruckLocation(truckId: String, lat: Double, lng: Double, time: String) {
        dao.updateTruckLocation(truckId, lat, lng, time)
    }

    suspend fun deleteTruck(id: String) {
        dao.deleteTruck(id)
    }

    suspend fun saveDelivery(delivery: Delivery) {
        dao.insertDelivery(
            DeliveryEntity(
                id = delivery.id,
                truckId = delivery.truckId,
                residentName = delivery.residentName,
                residentId = delivery.residentId,
                address = delivery.address,
                neighborhood = delivery.neighborhood,
                referencePoint = delivery.referencePoint,
                phone = delivery.phone,
                councilman = delivery.councilman,
                status = delivery.status.name,
                lat = delivery.lat,
                lng = delivery.lng,
                createdAt = delivery.createdAt,
                photo = delivery.photo
            )
        )
    }

    suspend fun completeDelivery(deliveryId: String, photo: String?) {
        dao.completeDelivery(deliveryId, DeliveryStatus.DELIVERED.name, photo)
    }

    suspend fun updateDeliveryStatus(deliveryId: String, status: DeliveryStatus) {
        dao.completeDelivery(deliveryId, status.name, null)
    }

    suspend fun deleteDelivery(id: String) {
        dao.deleteDelivery(id)
    }

    suspend fun clearCompletedDeliveries() {
        dao.clearCompletedDeliveries()
    }

    suspend fun saveSitio(sitio: Sitio) {
        dao.insertSitio(
            SitioEntity(
                id = sitio.id,
                name = sitio.name,
                lat = sitio.lat,
                lng = sitio.lng,
                description = sitio.description,
                type = sitio.type.name,
                fuelType = sitio.fuelType.name,
                referencePoint = sitio.referencePoint
            )
        )
    }

    suspend fun deleteSitio(id: String) {
        dao.deleteSitio(id)
    }

    suspend fun saveResident(resident: Resident) {
        dao.insertResident(
            ResidentEntity(
                id = resident.id,
                name = resident.name,
                address = resident.address,
                neighborhood = resident.neighborhood,
                phone = resident.phone,
                lat = resident.lat,
                lng = resident.lng,
                referencePoint = resident.referencePoint,
                councilman = resident.councilman,
                category = resident.category
            )
        )
    }

    suspend fun deleteResident(id: String) {
        dao.deleteResident(id)
    }

    suspend fun saveDriver(driver: Driver) {
        dao.insertDriver(DriverEntity(driver.id, driver.name, driver.phone))
    }

    suspend fun deleteDriver(id: String) {
        dao.deleteDriver(id)
    }

    private fun TruckEntity.toModel(): Truck {
        return Truck(
            id = id,
            plate = plate,
            driverName = driverName,
            lastLat = lastLat,
            lastLng = lastLng,
            lastUpdate = lastUpdate,
            status = runCatching { TruckStatus.valueOf(status) }.getOrDefault(TruckStatus.IDLE)
        )
    }

    private fun DeliveryEntity.toModel(): Delivery {
        return Delivery(
            id = id,
            truckId = truckId,
            residentName = residentName,
            residentId = residentId,
            address = address,
            neighborhood = neighborhood,
            referencePoint = referencePoint,
            phone = phone,
            councilman = councilman,
            status = runCatching { DeliveryStatus.valueOf(status) }.getOrDefault(DeliveryStatus.PENDING),
            lat = lat,
            lng = lng,
            createdAt = createdAt,
            photo = photo
        )
    }

    private fun SitioEntity.toModel(): Sitio {
        return Sitio(
            id = id,
            name = name,
            lat = lat,
            lng = lng,
            description = description,
            type = runCatching { SitioType.valueOf(type) }.getOrDefault(SitioType.WATER),
            fuelType = runCatching { FuelType.valueOf(fuelType) }.getOrDefault(FuelType.NONE),
            referencePoint = referencePoint
        )
    }

    private fun ResidentEntity.toModel(): Resident {
        return Resident(
            id = id,
            name = name,
            address = address,
            neighborhood = neighborhood,
            phone = phone,
            lat = lat,
            lng = lng,
            referencePoint = referencePoint,
            councilman = councilman,
            category = category
        )
    }
}
