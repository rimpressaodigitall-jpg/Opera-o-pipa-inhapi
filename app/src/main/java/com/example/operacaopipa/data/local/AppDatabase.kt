package com.example.operacaopipa.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TruckEntity::class,
        DeliveryEntity::class,
        SitioEntity::class,
        ResidentEntity::class,
        DriverEntity::class,
        CouncilmanEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pipaDao(): PipaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "operacao_pipa_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.pipaDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: PipaDao) {
                // Seed Trucks
                dao.insertTrucks(
                    listOf(
                        TruckEntity(
                            id = "truck_1",
                            plate = "QTT-1234",
                            driverName = "Manoel Bezerra",
                            lastLat = -9.2155,
                            lastLng = -36.3488,
                            lastUpdate = "Hoje, 09:30",
                            status = "IN_ROUTE"
                        ),
                        TruckEntity(
                            id = "truck_2",
                            plate = "KLS-5678",
                            driverName = "Zé da Pipa",
                            lastLat = -9.2210,
                            lastLng = -36.3550,
                            lastUpdate = "Hoje, 08:45",
                            status = "IDLE"
                        ),
                        TruckEntity(
                            id = "truck_3",
                            plate = "MNX-9012",
                            driverName = "Antônio Silva",
                            lastLat = -9.2080,
                            lastLng = -36.3420,
                            lastUpdate = "Hoje, 10:15",
                            status = "ARRIVED"
                        )
                    )
                )

                // Seed Sitios (Pontos de Abastecimento & Comunidades)
                dao.insertSitios(
                    listOf(
                        SitioEntity(
                            id = "sitio_1",
                            name = "Ponto Central de Captação (Inhapi)",
                            lat = -9.2155,
                            lng = -36.3488,
                            description = "Caixa d'água principal da CASAL / Ponto Central",
                            type = "WATER",
                            fuelType = "NONE",
                            referencePoint = "Centro da Cidade"
                        ),
                        SitioEntity(
                            id = "sitio_2",
                            name = "Sítio Leão",
                            lat = -9.2310,
                            lng = -36.3680,
                            description = "Comunidade do Leão, reservatório coletivo",
                            type = "WATER",
                            fuelType = "NONE",
                            referencePoint = "Entrada da fazenda de Zé Preto"
                        ),
                        SitioEntity(
                            id = "sitio_3",
                            name = "Escola Mun. Manoel Pereira",
                            lat = -9.2050,
                            lng = -36.3390,
                            description = "Cisterna escolar (16.000 litros)",
                            type = "SCHOOL",
                            fuelType = "NONE",
                            referencePoint = "Próximo à igreja"
                        ),
                        SitioEntity(
                            id = "sitio_4",
                            name = "Posto Combustível Municipal",
                            lat = -9.2180,
                            lng = -36.3510,
                            description = "Abastecimento de diesel para frotas",
                            type = "FUEL",
                            fuelType = "DIESEL",
                            referencePoint = "Saída para Mata Grande"
                        )
                    )
                )

                // Seed Councilmen
                dao.insertCouncilmen(
                    listOf(
                        CouncilmanEntity(id = "c_1", name = "Vereador Beto"),
                        CouncilmanEntity(id = "c_2", name = "Vereador Cícero de Inhapi"),
                        CouncilmanEntity(id = "c_3", name = "Vereadora Maria"),
                        CouncilmanEntity(id = "c_4", name = "Secretaria de Obras")
                    )
                )

                // Seed Residents
                dao.insertResidents(
                    listOf(
                        ResidentEntity(
                            id = "res_1",
                            name = "Dona Josefa Souza",
                            address = "Sítio Leão, Lote 14",
                            neighborhood = "Zona Rural",
                            phone = "(82) 99881-2233",
                            lat = -9.2315,
                            lng = -36.3685,
                            referencePoint = "Casa amarela após a cancela",
                            councilman = "Vereador Beto",
                            category = "RESIDENT"
                        ),
                        ResidentEntity(
                            id = "res_2",
                            name = "Severino Rodrigues",
                            address = "Povoado Promissão, Casa 08",
                            neighborhood = "Promissão",
                            phone = "(82) 99611-3344",
                            lat = -9.2085,
                            lng = -36.3425,
                            referencePoint = "Em frente à mercearia do Pedro",
                            councilman = "Vereador Cícero de Inhapi",
                            category = "RESIDENT"
                        )
                    )
                )

                // Seed Drivers
                dao.insertDrivers(
                    listOf(
                        DriverEntity(id = "drv_1", name = "Manoel Bezerra", phone = "(82) 99911-0001"),
                        DriverEntity(id = "drv_2", name = "Zé da Pipa", phone = "(82) 99911-0002"),
                        DriverEntity(id = "drv_3", name = "Antônio Silva", phone = "(82) 99911-0003")
                    )
                )

                // Seed Deliveries
                dao.insertDeliveries(
                    listOf(
                        DeliveryEntity(
                            id = "del_1",
                            truckId = "truck_1",
                            residentName = "Dona Josefa Souza",
                            residentId = "res_1",
                            address = "Sítio Leão, Lote 14",
                            neighborhood = "Zona Rural",
                            referencePoint = "Casa amarela após a cancela",
                            phone = "(82) 99881-2233",
                            councilman = "Vereador Beto",
                            status = "IN_ROUTE",
                            lat = -9.2315,
                            lng = -36.3685,
                            createdAt = "Hoje, 09:00",
                            photo = null
                        ),
                        DeliveryEntity(
                            id = "del_2",
                            truckId = "truck_1",
                            residentName = "Severino Rodrigues",
                            residentId = "res_2",
                            address = "Povoado Promissão, Casa 08",
                            neighborhood = "Promissão",
                            referencePoint = "Em frente à mercearia do Pedro",
                            phone = "(82) 99611-3344",
                            councilman = "Vereador Cícero de Inhapi",
                            status = "PENDING",
                            lat = -9.2085,
                            lng = -36.3425,
                            createdAt = "Hoje, 09:15",
                            photo = null
                        ),
                        DeliveryEntity(
                            id = "del_3",
                            truckId = "truck_3",
                            residentName = "Escola Mun. Manoel Pereira",
                            residentId = "sitio_3",
                            address = "Povoado Promissão",
                            neighborhood = "Zona Rural",
                            referencePoint = "Ao lado da igreja",
                            phone = "(82) 99922-1100",
                            councilman = "Secretaria de Obras",
                            status = "DELIVERED",
                            lat = -9.2050,
                            lng = -36.3390,
                            createdAt = "Hoje, 08:00",
                            photo = null
                        )
                    )
                )
            }
        }
    }
}
