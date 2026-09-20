package com.example.operacaopipa.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.operacaopipa.data.model.Delivery
import com.example.operacaopipa.data.model.DeliveryStatus
import com.example.operacaopipa.data.model.SitioType
import com.example.operacaopipa.ui.theme.BrandNavy
import com.example.operacaopipa.ui.theme.BrandTeal
import com.example.operacaopipa.ui.theme.StatusDelivered
import com.example.operacaopipa.ui.theme.StatusInRoute
import com.example.operacaopipa.ui.theme.StatusPending
import com.example.operacaopipa.ui.theme.SurfaceWhite
import com.example.operacaopipa.ui.viewmodel.AppScreen
import com.example.operacaopipa.ui.viewmodel.PipaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(
    viewModel: PipaViewModel,
    modifier: Modifier = Modifier
) {
    val trucks by viewModel.trucks.collectAsState()
    val deliveries by viewModel.deliveries.collectAsState()
    val selectedTruckId by viewModel.selectedDriverTruckId.collectAsState()

    var showQuickPointDialog by remember { mutableStateOf(false) }
    var deliveryToConfirm by remember { mutableStateOf<Delivery?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }

    val activeTruck = trucks.find { it.id == selectedTruckId } ?: trucks.firstOrNull()
    val assignedDeliveries = deliveries.filter { it.truckId == (activeTruck?.id ?: "truck_1") }
    val pendingDeliveries = assignedDeliveries.filter { it.status != DeliveryStatus.DELIVERED }
    val completedDeliveries = assignedDeliveries.filter { it.status == DeliveryStatus.DELIVERED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Painel do Motorista",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SurfaceWhite
                        )
                        Text(
                            text = "Caminhão: ${activeTruck?.plate ?: "Nenhum"} (${activeTruck?.driverName ?: ""})",
                            fontSize = 12.sp,
                            color = SurfaceWhite.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.LOGIN) },
                        modifier = Modifier.testTag("driver_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = SurfaceWhite
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showQuickPointDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AddLocation,
                            contentDescription = "Cadastrar Ponto Rápido",
                            tint = SurfaceWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandNavy)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Truck Selection Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = BrandNavy
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = activeTruck?.plate ?: "Selecione o Caminhão",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(StatusInRoute)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "GPS Ativo: Inhapi/AL",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Truck Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedButton(
                            onClick = { expandedDropdown = true },
                            modifier = Modifier.menuAnchor()
                        ) {
                            Text("Trocar", fontSize = 12.sp)
                        }
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            trucks.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text("${t.plate} - ${t.driverName}") },
                                    onClick = {
                                        viewModel.setSelectedTruck(t.id)
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${pendingDeliveries.size}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusPending
                        )
                        Text(text = "Entregas Restantes", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${completedDeliveries.size}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusDelivered
                        )
                        Text(text = "Concluídas Hoje", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Roteiro de Entregas Prioritárias",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Dirija-se ao local e realize a confirmação fotográfica",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (pendingDeliveries.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusDelivered,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Roteiro concluído!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Todas as entregas deste caminhão foram realizadas.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(pendingDeliveries, key = { it.id }) { delivery ->
                        DriverDeliveryCard(
                            delivery = delivery,
                            onStartRoute = { viewModel.updateDeliveryStatus(delivery.id, DeliveryStatus.IN_ROUTE) },
                            onConfirmDelivery = { deliveryToConfirm = delivery }
                        )
                    }
                }
            }
        }
    }

    // Delivery Confirmation Dialog with Photo Simulation
    deliveryToConfirm?.let { delivery ->
        AlertDialog(
            onDismissRequest = { deliveryToConfirm = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = BrandNavy,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Confirmar Entrega de Água") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Morador: ${delivery.residentName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Local: ${delivery.address}",
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = BrandNavy
                            )
                            Text(
                                text = "Foto da Cisterna Abastecida Anexada",
                                fontSize = 11.sp,
                                color = BrandNavy,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.completeDelivery(delivery.id, "photo_sample_hash")
                        deliveryToConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDelivered)
                ) {
                    Text("Finalizar e Validar")
                }
            },
            dismissButton = {
                TextButton(onClick = { deliveryToConfirm = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Quick Point Dialog
    if (showQuickPointDialog) {
        var pointName by remember { mutableStateOf("") }
        var pointDesc by remember { mutableStateOf("") }
        var pointRef by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showQuickPointDialog = false },
            title = { Text("Cadastrar Ponto em Campo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Coordenadas GPS atuais capturadas automaticamente.",
                        fontSize = 12.sp,
                        color = BrandTeal
                    )
                    OutlinedTextField(
                        value = pointName,
                        onValueChange = { pointName = it },
                        label = { Text("Nome do Ponto / Sítio *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pointDesc,
                        onValueChange = { pointDesc = it },
                        label = { Text("Descrição / Tipo de Reservatório") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pointRef,
                        onValueChange = { pointRef = it },
                        label = { Text("Ponto de Referência") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pointName.isNotBlank()) {
                            viewModel.saveSitio(
                                name = pointName,
                                description = pointDesc,
                                type = SitioType.WATER,
                                referencePoint = pointRef
                            )
                            showQuickPointDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                ) {
                    Text("Salvar Ponto")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickPointDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DriverDeliveryCard(
    delivery: Delivery,
    onStartRoute: () -> Unit,
    onConfirmDelivery: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

                val statusText = if (delivery.status == DeliveryStatus.IN_ROUTE) "Em Rota" else "Pendente"
                val statusColor = if (delivery.status == DeliveryStatus.IN_ROUTE) StatusInRoute else StatusPending

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${delivery.address} ${if (delivery.neighborhood.isNotBlank()) "- ${delivery.neighborhood}" else ""}",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }

            if (delivery.referencePoint.isNotBlank()) {
                Text(
                    text = "Ref: ${delivery.referencePoint}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 20.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (delivery.status == DeliveryStatus.PENDING) {
                    OutlinedButton(
                        onClick = onStartRoute,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Iniciar Rota", fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = onConfirmDelivery,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDelivered),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Concluir Entrega", fontSize = 12.sp)
                }
            }
        }
    }
}
