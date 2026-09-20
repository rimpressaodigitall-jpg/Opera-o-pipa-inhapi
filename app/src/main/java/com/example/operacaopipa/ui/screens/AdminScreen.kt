package com.example.operacaopipa.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.operacaopipa.data.model.Delivery
import com.example.operacaopipa.data.model.DeliveryStatus
import com.example.operacaopipa.data.model.Sitio
import com.example.operacaopipa.data.model.SitioType
import com.example.operacaopipa.data.model.Truck
import com.example.operacaopipa.data.model.TruckStatus
import com.example.operacaopipa.ui.theme.BrandNavy
import com.example.operacaopipa.ui.theme.BrandNavyDark
import com.example.operacaopipa.ui.theme.BrandTeal
import com.example.operacaopipa.ui.theme.StatusDelivered
import com.example.operacaopipa.ui.theme.StatusInRoute
import com.example.operacaopipa.ui.theme.StatusPending
import com.example.operacaopipa.ui.theme.SurfaceWhite
import com.example.operacaopipa.ui.viewmodel.AdminTab
import com.example.operacaopipa.ui.viewmodel.AppScreen
import com.example.operacaopipa.ui.viewmodel.PipaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: PipaViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.adminTab.collectAsState()
    val deliveries by viewModel.deliveries.collectAsState()
    val trucks by viewModel.trucks.collectAsState()
    val sitios by viewModel.sitios.collectAsState()
    val residents by viewModel.residents.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    val councilPerformance by viewModel.councilmanPerformance.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var showNewDeliveryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Operação Pipa",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SurfaceWhite
                        )
                        Text(
                            text = "Painel da Administração • Inhapi - AL",
                            fontSize = 12.sp,
                            color = SurfaceWhite.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.LOGIN) },
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar ao início",
                            tint = SurfaceWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandNavy)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceWhite,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AdminTab.MAP,
                    onClick = { viewModel.setAdminTab(AdminTab.MAP) },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Mapa") },
                    label = { Text("Mapa") },
                    modifier = Modifier.testTag("nav_tab_map")
                )
                NavigationBarItem(
                    selected = currentTab == AdminTab.DELIVERIES,
                    onClick = { viewModel.setAdminTab(AdminTab.DELIVERIES) },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Entregas") },
                    label = { Text("Entregas") },
                    modifier = Modifier.testTag("nav_tab_deliveries")
                )
                NavigationBarItem(
                    selected = currentTab == AdminTab.REGISTRATIONS,
                    onClick = { viewModel.setAdminTab(AdminTab.REGISTRATIONS) },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Cadastros") },
                    label = { Text("Cadastros") },
                    modifier = Modifier.testTag("nav_tab_registrations")
                )
                NavigationBarItem(
                    selected = currentTab == AdminTab.PERFORMANCE,
                    onClick = { viewModel.setAdminTab(AdminTab.PERFORMANCE) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Desempenho") },
                    label = { Text("Desempenho") },
                    modifier = Modifier.testTag("nav_tab_performance")
                )
                NavigationBarItem(
                    selected = currentTab == AdminTab.SETTINGS,
                    onClick = { viewModel.setAdminTab(AdminTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                    label = { Text("Ajustes") },
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == AdminTab.DELIVERIES) {
                FloatingActionButton(
                    onClick = { showNewDeliveryDialog = true },
                    containerColor = BrandNavy,
                    contentColor = SurfaceWhite,
                    modifier = Modifier.testTag("fab_add_delivery")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nova Entrega")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                AdminTab.MAP -> AdminMapView(trucks = trucks, sitios = sitios, deliveries = deliveries)
                AdminTab.DELIVERIES -> DeliveriesView(
                    deliveries = deliveries,
                    onDelete = { viewModel.deleteDelivery(it) },
                    onStatusChange = { id, status -> viewModel.updateDeliveryStatus(id, status) }
                )
                AdminTab.REGISTRATIONS -> RegistrationsView(
                    sitios = sitios,
                    residents = residents,
                    trucks = trucks,
                    drivers = drivers,
                    viewModel = viewModel
                )
                AdminTab.PERFORMANCE -> PerformanceView(councilPerformance = councilPerformance)
                AdminTab.SETTINGS -> SettingsView(
                    viewModel = viewModel,
                    deliveries = deliveries
                )
            }
        }
    }

    if (showNewDeliveryDialog) {
        NewDeliveryDialog(
            trucks = trucks,
            residents = residents,
            onDismiss = { showNewDeliveryDialog = false },
            onConfirm = { truckId, resName, addr, neigh, ref, council, phone ->
                viewModel.createDelivery(truckId, resName, addr, neigh, ref, council, phone)
                showNewDeliveryDialog = false
            }
        )
    }
}

@Composable
fun AdminMapView(
    trucks: List<Truck>,
    sitios: List<Sitio>,
    deliveries: List<Delivery>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Fleet Status Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${trucks.size}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                    Text(text = "Caminhões", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${deliveries.count { it.status == DeliveryStatus.PENDING || it.status == DeliveryStatus.IN_ROUTE }}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusPending
                    )
                    Text(text = "Pendentes", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${deliveries.count { it.status == DeliveryStatus.DELIVERED }}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusDelivered
                    )
                    Text(text = "Concluídas", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Mapa Territorial - Inhapi / AL",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Visualização de rotas, pontos de captação e caminhões",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Canvas Map View
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE5EEF7))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw map grid lines
                val stepX = canvasWidth / 6
                val stepY = canvasHeight / 8
                for (i in 0..6) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(i * stepX, 0f),
                        end = Offset(i * stepX, canvasHeight),
                        strokeWidth = 1f
                    )
                }
                for (j in 0..8) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(0f, j * stepY),
                        end = Offset(canvasWidth, j * stepY),
                        strokeWidth = 1f
                    )
                }

                // Central Water Point (Inhapi)
                val centerPt = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f)
                drawCircle(
                    color = Color(0xFF1B4382),
                    radius = 18f,
                    center = centerPt
                )
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = centerPt
                )

                // Draw Sitios / Points
                sitios.forEachIndexed { index, s ->
                    val posX = canvasWidth * (0.25f + (index * 0.18f) % 0.6f)
                    val posY = canvasHeight * (0.2f + (index * 0.22f) % 0.65f)
                    drawCircle(
                        color = if (s.type == SitioType.SCHOOL) Color(0xFFE11D48) else Color(0xFF0D9488),
                        radius = 12f,
                        center = Offset(posX, posY)
                    )
                }

                // Draw Trucks on route
                trucks.forEachIndexed { index, truck ->
                    val tX = canvasWidth * (0.35f + (index * 0.25f) % 0.55f)
                    val tY = canvasHeight * (0.35f + (index * 0.2f) % 0.5f)
                    drawCircle(
                        color = when (truck.status) {
                            TruckStatus.IN_ROUTE -> StatusInRoute
                            TruckStatus.ARRIVED -> StatusDelivered
                            TruckStatus.IDLE -> Color.Gray
                        },
                        radius = 15f,
                        center = Offset(tX, tY)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(BrandNavy))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ponto Central", fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(StatusInRoute))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Caminhão Rota", fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF0D9488)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sítio/Cisterna", fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE11D48)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Escola", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun DeliveriesView(
    deliveries: List<Delivery>,
    onDelete: (String) -> Unit,
    onStatusChange: (String, DeliveryStatus) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredDeliveries = remember(deliveries, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> deliveries.filter { it.status == DeliveryStatus.PENDING }
            "IN_ROUTE" -> deliveries.filter { it.status == DeliveryStatus.IN_ROUTE }
            "DELIVERED" -> deliveries.filter { it.status == DeliveryStatus.DELIVERED }
            else -> deliveries
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterButton("Todas (${deliveries.size})", selectedFilter == "ALL") { selectedFilter = "ALL" }
            FilterButton("Pendentes (${deliveries.count { it.status == DeliveryStatus.PENDING }})", selectedFilter == "PENDING") { selectedFilter = "PENDING" }
            FilterButton("Em Rota (${deliveries.count { it.status == DeliveryStatus.IN_ROUTE }})", selectedFilter == "IN_ROUTE") { selectedFilter = "IN_ROUTE" }
            FilterButton("Concluídas (${deliveries.count { it.status == DeliveryStatus.DELIVERED }})", selectedFilter == "DELIVERED") { selectedFilter = "DELIVERED" }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredDeliveries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma entrega encontrada para este filtro.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredDeliveries, key = { it.id }) { delivery ->
                    DeliveryItemCard(
                        delivery = delivery,
                        onDelete = { onDelete(delivery.id) },
                        onStatusChange = { newStatus -> onStatusChange(delivery.id, newStatus) }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) BrandNavy else SurfaceWhite)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) SurfaceWhite else Color.DarkGray
        )
    }
}

@Composable
fun DeliveryItemCard(
    delivery: Delivery,
    onDelete: () -> Unit,
    onStatusChange: (DeliveryStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = delivery.residentName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BrandNavy
                )

                // Status Badge
                val (badgeBg, badgeText) = when (delivery.status) {
                    DeliveryStatus.PENDING -> StatusPending.copy(alpha = 0.15f) to "Pendente"
                    DeliveryStatus.IN_ROUTE -> StatusInRoute.copy(alpha = 0.15f) to "Em Rota"
                    DeliveryStatus.DELIVERED -> StatusDelivered.copy(alpha = 0.15f) to "Entregue"
                }
                val badgeColor = when (delivery.status) {
                    DeliveryStatus.PENDING -> StatusPending
                    DeliveryStatus.IN_ROUTE -> StatusInRoute
                    DeliveryStatus.DELIVERED -> StatusDelivered
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Endereço: ${delivery.address} ${if (delivery.neighborhood.isNotBlank()) "(${delivery.neighborhood})" else ""}",
                fontSize = 13.sp,
                color = Color.DarkGray
            )

            if (delivery.referencePoint.isNotBlank()) {
                Text(
                    text = "Ref: ${delivery.referencePoint}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (delivery.councilman.isNotBlank()) {
                Text(
                    text = "Indicação: ${delivery.councilman}",
                    fontSize = 12.sp,
                    color = BrandTeal,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = delivery.createdAt, fontSize = 11.sp, color = Color.Gray)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (delivery.status != DeliveryStatus.DELIVERED) {
                        TextButton(onClick = { onStatusChange(DeliveryStatus.DELIVERED) }) {
                            Text("Concluir", fontSize = 12.sp, color = StatusDelivered)
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remover",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RegistrationsView(
    sitios: List<Sitio>,
    residents: List<com.example.operacaopipa.data.model.Resident>,
    trucks: List<Truck>,
    drivers: List<com.example.operacaopipa.data.model.Driver>,
    viewModel: PipaViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Sítios", "Moradores", "Caminhões", "Motoristas")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sitios) { s ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(s.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(s.description, fontSize = 12.sp, color = Color.Gray)
                                    Text("Tipo: ${s.type}", fontSize = 11.sp, color = BrandTeal)
                                }
                                IconButton(onClick = { viewModel.deleteSitio(s.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(residents) { r ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(r.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(r.address, fontSize = 12.sp, color = Color.Gray)
                                    if (r.councilman.isNotBlank()) Text("Vereador: ${r.councilman}", fontSize = 11.sp, color = BrandTeal)
                                }
                                IconButton(onClick = { viewModel.deleteResident(r.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(trucks) { t ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Placa: ${t.plate}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Motorista: ${t.driverName}", fontSize = 12.sp, color = Color.Gray)
                                    Text("Status: ${t.status}", fontSize = 11.sp, color = StatusInRoute)
                                }
                                IconButton(onClick = { viewModel.deleteTruck(t.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(drivers) { d ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(d.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Tel: ${d.phone}", fontSize = 12.sp, color = Color.Gray)
                                }
                                IconButton(onClick = { viewModel.deleteDriver(d.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceView(
    councilPerformance: List<com.example.operacaopipa.ui.viewmodel.CouncilmanPerformance>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Desempenho por Indicação Parlamentar",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Total de abastecimentos direcionados e entregues por vereador",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        councilPerformance.forEach { perf ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = perf.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BrandNavy
                        )
                        Text(
                            text = "${perf.totalDeliveries} atendimentos",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = BrandTeal
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar
                    val total = if (perf.totalDeliveries > 0) perf.totalDeliveries else 1
                    val deliveredRatio = perf.deliveredCount.toFloat() / total

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(deliveredRatio)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(StatusDelivered)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Entregues: ${perf.deliveredCount}",
                            fontSize = 12.sp,
                            color = StatusDelivered,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Pendentes: ${perf.pendingCount}",
                            fontSize = 12.sp,
                            color = StatusPending,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsView(
    viewModel: PipaViewModel,
    deliveries: List<Delivery>
) {
    var newPassword by remember { mutableStateOf("") }
    var showConfirmClear by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ajustes do Sistema",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        // Password change card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Segurança do Painel", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nova Senha de Administrador") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (newPassword.isNotBlank()) {
                            viewModel.updateAdminPassword(newPassword)
                            newPassword = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                ) {
                    Text("Salvar Nova Senha")
                }
            }
        }

        // Maintenance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Manutenção e Limpeza", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Limpar histórico de entregas que já foram concluídas para otimizar o banco de dados.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showConfirmClear = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Limpar Entregas Concluídas")
                }
            }
        }
    }

    if (showConfirmClear) {
        AlertDialog(
            onDismissRequest = { showConfirmClear = false },
            title = { Text("Limpar Entregas Concluídas?") },
            text = { Text("Todas as entregas com status 'Entregue' serão permanentemente removidas do histórico.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearDelivered()
                        showConfirmClear = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Confirmar Exclusão")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClear = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun NewDeliveryDialog(
    trucks: List<Truck>,
    residents: List<com.example.operacaopipa.data.model.Resident>,
    onDismiss: () -> Unit,
    onConfirm: (truckId: String, residentName: String, address: String, neighborhood: String, ref: String, council: String, phone: String) -> Unit
) {
    var selectedTruckId by remember { mutableStateOf(trucks.firstOrNull()?.id ?: "") }
    var residentName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var neighborhood by remember { mutableStateOf("") }
    var referencePoint by remember { mutableStateOf("") }
    var councilman by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agendar Nova Entrega de Água") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = residentName,
                    onValueChange = { residentName = it },
                    label = { Text("Nome do Morador / Escola *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Endereço / Sítio *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = neighborhood,
                    onValueChange = { neighborhood = it },
                    label = { Text("Povoado / Bairro") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = referencePoint,
                    onValueChange = { referencePoint = it },
                    label = { Text("Ponto de Referência") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = councilman,
                    onValueChange = { councilman = it },
                    label = { Text("Indicação do Vereador") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone / WhatsApp") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (residentName.isNotBlank() && address.isNotBlank()) {
                        onConfirm(
                            selectedTruckId.ifBlank { "truck_1" },
                            residentName,
                            address,
                            neighborhood,
                            referencePoint,
                            councilman,
                            phone
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
            ) {
                Text("Agendar Entrega")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
