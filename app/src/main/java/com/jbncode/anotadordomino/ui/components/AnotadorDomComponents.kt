package com.jbncode.anotadordomino.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jbncode.anotadordomino.ui.theme.NeonCyan
import com.jbncode.anotadordomino.ui.theme.NeonGreen
import com.jbncode.anotadordomino.ui.theme.SurfaceDark
import com.jbncode.anotadordomino.ui.theme.TextGray
import com.jbncode.anotadordomino.ui.theme.TextWhite

@Composable
fun GameModeSelector(isPairsSelected: Boolean, onModeChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(SurfaceDark)
    ) {
        // Botón Parejas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(28.dp))
                .background(if (isPairsSelected) NeonCyan else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👥 PAIRS",
                color = if (isPairsSelected) Color.Black else TextGray,
                fontWeight = FontWeight.Bold
            )
        }

        // Botón Individual
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "👤 INDIVIDUAL", color = TextGray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PlayerCard(name: String, wins: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Placeholder (Círculo)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF2A2E38)),
            contentAlignment = Alignment.Center
        ) {
            Text("👤", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text = name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "WINS: $wins", color = TextGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun NeonPrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text = text, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}