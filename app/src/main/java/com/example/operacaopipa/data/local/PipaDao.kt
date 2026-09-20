package com.example.operacaopipa.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PipaDao {

    // Trucks
    @Query("SELECT * FROM trucks ORDER BY plate ASC")
    fun getAllTrucks(): Flow<List<TruckEntity>>

    @Query("SELECT * FROM trucks WHERE id = :id LIMIT 1")
    suspend fun getTruckById(id: String): TruckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTruck(truck: TruckEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrucks(trucks: List<TruckEntity>)

    @Update
    suspend fun updateTruck(truck: TruckEntity)

    @Query("UPDATE trucks SET lastLat = :lat, lastLng = :lng, lastUpdate = :updateTime WHERE id = :id")
    suspend fun updateTruckLocation(id: String, lat: Double, lng: Double, updateTime: String)

    @Query("DELETE FROM trucks WHERE id = :id")
    suspend fun deleteTruck(id: String)

    // Deliveries
    @Query("SELECT * FROM deliveries ORDER BY createdAt DESC")
    fun getAllDeliveries(): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE truckId = :truckId ORDER BY createdAt DESC")
    fun getDeliveriesByTruck(truckId: String): Flow<List<DeliveryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: DeliveryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveries(deliveries: List<DeliveryEntity>)

    @Update
    suspend fun updateDelivery(delivery: DeliveryEntity)

    @Query("UPDATE deliveries SET status = :status, photo = :photo WHERE id = :id")
    suspend fun completeDelivery(id: String, status: String, photo: String?)

    @Query("DELETE FROM deliveries WHERE id = :id")
    suspend fun deleteDelivery(id: String)

    @Query("DELETE FROM deliveries WHERE status = 'DELIVERED'")
    suspend fun clearCompletedDeliveries()

    // Sitios
    @Query("SELECT * FROM sitios ORDER BY name ASC")
    fun getAllSitios(): Flow<List<SitioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSitio(sitio: SitioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSitios(sitios: List<SitioEntity>)

    @Query("DELETE FROM sitios WHERE id = :id")
    suspend fun deleteSitio(id: String)

    // Residents
    @Query("SELECT * FROM residents ORDER BY name ASC")
    fun getAllResidents(): Flow<List<ResidentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResident(resident: ResidentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResidents(residents: List<ResidentEntity>)

    @Query("DELETE FROM residents WHERE id = :id")
    suspend fun deleteResident(id: String)

    // Drivers
    @Query("SELECT * FROM drivers ORDER BY name ASC")
    fun getAllDrivers(): Flow<List<DriverEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrivers(drivers: List<DriverEntity>)

    @Query("DELETE FROM drivers WHERE id = :id")
    suspend fun deleteDriver(id: String)

    // Councilmen
    @Query("SELECT * FROM councilmen ORDER BY name ASC")
    fun getAllCouncilmen(): Flow<List<CouncilmanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCouncilman(councilman: CouncilmanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCouncilmen(councilmen: List<CouncilmanEntity>)

    @Query("DELETE FROM councilmen WHERE id = :id")
    suspend fun deleteCouncilman(id: String)
}
