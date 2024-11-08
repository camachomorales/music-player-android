package com.example.music.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.music.R

@Composable
fun Homepage(modifier: Modifier = Modifier) {

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Asegúrate de tener el drawable en res/drawable
        Image(
            painter = painterResource(id = R.drawable.background), // Reemplaza con tu drawable
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Ajusta cómo se escalará la imagen
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            // .background(Color(0xFF2196F3)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Home page",
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}