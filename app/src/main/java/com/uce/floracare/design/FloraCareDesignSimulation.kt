package com.uce.floracare.design

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// FloraCare Official Colors
val ForestGreen = Color(0xFF2D5A27)
val ForestDark = Color(0xFF1B331E)
val AlertRed = Color(0xFFE05345)
val AlertRedSoft = Color(0xFFFFF5F5)
val SuccessGreen = Color(0xFF4CAF50)
val SuccessGreenSoft = Color(0xFFF0FFF4)
val CreamBg = Color(0xFFFFFCF2)
val TextSlate = Color(0xFF203622)
val GrayLine = Color(0xFFD9D9D9)

@Composable
fun FloraCareTopBar() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = com.uce.floracare.R.drawable.ic_logo),
                    contentDescription = "Logo",
                    tint = ForestGreen,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "FloraCare",
                    color = ForestGreen,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = ForestGreen)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir", tint = ForestGreen)
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GrayLine)
                ) {
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = "Perfil", 
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun PlantCard(
    name: String,
    species: String,
    needsWater: Boolean = false,
    watered: Boolean = false,
    humidity: String = "45%"
) {
    val bgColor = when {
        watered -> SuccessGreenSoft
        needsWater -> AlertRedSoft
        else -> Color.White
    }
    val borderColor = when {
        watered -> SuccessGreen
        needsWater -> AlertRed
        else -> Color(0xFFEEF2EC)
    }

    Card(
        modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray)
                ) {
                    Text(
                        "Imagen Planta", 
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 10.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = species.uppercase(),
                    color = Color(0xFF8C9C8A),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = name,
                    color = ForestDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                if (needsWater && !watered) {
                    Text(
                        text = "Humedad: $humidity",
                        color = AlertRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (watered) {
                    Text(
                        text = "¡Regada!",
                        color = SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(modifier = Modifier.padding(top = 8.dp)) {
                    Icon(
                        Icons.Default.Info, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp), 
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Star, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp), 
                        tint = Color.Gray
                    )
                }
            }

            if (needsWater && !watered) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AlertRed)
                ) {
                    Icon(
                        Icons.Default.PriorityHigh,
                        contentDescription = "Alerta",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp).align(Alignment.Center)
                    )
                }
            }
            
            if (watered) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "OK",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp).align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavBar() {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = false,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Park, contentDescription = "Mi Jardín") },
            label = { Text("Mi Jardín") },
            selected = true,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Explore, contentDescription = "Explorar") },
            label = { Text("Explorar") },
            selected = false,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            selected = false,
            onClick = {}
        )
    }
}

// --- CASE A: GENERAL VIEW ---
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun MyGardenGeneralPreview() {
    Scaffold(
        topBar = { FloraCareTopBar() },
        bottomBar = { BottomNavBar() },
        containerColor = CreamBg
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(20.dp)) {
            Text(
                "Mi Jardín",
                color = ForestGreen,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize()
            ) {
                items(4) { index ->
                    PlantCard(
                        name = "Planta $index",
                        species = "Species Name",
                        needsWater = index == 0
                    )
                }
            }
        }
    }
}

// --- CASE B: ALERT VIEW ---
@Preview(showBackground = true)
@Composable
fun PlantAlertPreview() {
    Box(modifier = Modifier.padding(16.dp).width(180.dp)) {
        Column {
            Text("CASO B: Alerta Roja", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            PlantCard(
                name = "Adansonii",
                species = "Monstera Adansonii",
                needsWater = true,
                humidity = "20%"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "¡NECESITA AGUA!",
                color = AlertRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

// --- CASE C: CONFIRMATION VIEW ---
@Preview(showBackground = true)
@Composable
fun WateringConfirmationPreview() {
    Box(modifier = Modifier.padding(16.dp).width(180.dp)) {
        Column {
            Text("CASO C: Confirmación", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            PlantCard(
                name = "Adansonii",
                species = "Monstera Adansonii",
                watered = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Regada")
            }
        }
    }
}
