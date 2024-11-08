package com.example.music

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.music.pages.DefaultPage
import com.example.music.pages.Homepage
import com.example.music.pages.LibraryPage
import com.example.music.pages.UserPage
import androidx.compose.material3.Text
import com.example.music.SearchNavegacion.Navegacion
import com.example.music.pages.SearchPage

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    // Inicializa el controlador de navegación
    val navController = rememberNavController()

    // Llama a la función Navegacion aquí
    Navegacion(navController)
    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home, 0),
        NavItem("Search", Icons.Default.Search, 0),
        NavItem("Library", ImageVector.vectorResource(id = R.drawable.biblioteca), 0),
        NavItem("User", Icons.Default.Person, 5) // Added comma here
    )

    var selectedIndex = remember { mutableStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(containerColor = Color.Transparent) { // Establece el color de fondo como transparente
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        icon = {
                            BadgedBox(badge = {
                                if (navItem.badgeCount > 0) {
                                    Badge {
                                        Text(text = navItem.badgeCount.toString())
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = navItem.icon,
                                    contentDescription = "Icon" // Corrige el nombre del parámetro
                                )
                            }
                        },
                        label = { Text(navItem.label) },
                        selected = selectedIndex.value == index,
                        onClick = { selectedIndex.value = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White, // Cambia el color del icono seleccionado
                            unselectedIconColor = Color.White, // Cambia el color del icono no seleccionado
                            selectedTextColor = Color.White, // Cambia el color del texto seleccionado
                            unselectedTextColor = Color.White, // Cambia el color del texto no seleccionado
                            indicatorColor = Color.Transparent // Establece el color del indicador como transparente
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // Asegúrate de aplicar el padding al contenido
        ContentScreen(modifier = modifier.padding(innerPadding), selectedIndex.value, navController)
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, selectedIndex: Int, navController: NavHostController) {
    when (selectedIndex) {
        0 -> Homepage() // Pantalla principal
        1 -> SearchPage(navController) // Pantalla de búsqueda
        2 -> LibraryPage() // Pantalla de biblioteca
        3 -> UserPage() // Pantalla de configuración
        else -> DefaultPage() // Pantalla predeterminada o de error
    }
}