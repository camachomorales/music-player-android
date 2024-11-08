package com.example.music.SearchNavegacion

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.music.pages.SearchPage
import com.example.music.screens.AlbumDetailsScreen
import com.example.music.screens.ArtistAlbumsScreen
import com.example.music.screens.SongDetailScreen

@Composable
fun Navegacion(navController: NavHostController) {
    NavHost(navController, startDestination = "search") {
        //composable("search") { SearchPage(navController) }
        composable("search") {
            Log.d("Navegacion", "Navegando a la página de búsqueda")
            SearchPage(navController) }
        composable("artistAlbums/{artistName}") { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
            Log.d("Navegacion", "Navegando a los álbumes del artista: $artistName")
            ArtistAlbumsScreen(artistName, onSongClick = { songName ->
                Log.d("Navegacion", "Navegando a detalles de la canción: $songName")
                navController.navigate("songDetail/$songName") // Navegar a detalles de la canción
            }, navController = navController) // Asegúrate de pasar el navController
        }

        composable("albumDetails/{albumName}/{artistName}") { backStackEntry ->
            val albumName = backStackEntry.arguments?.getString("albumName") ?: ""
            val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
            Log.d("Navegacion", "Navegando a los detalles del álbum: $albumName de $artistName")

            // Pasar la función onSongClick que navega a SongDetailScreen
            AlbumDetailsScreen(albumName, artistName) { songName ->
                Log.d("Navegacion", "Navegando a detalles de la canción: $songName")
                navController.navigate("songDetail/$songName") // Navegar a detalles de la canción
            }
        }
        composable("songDetail/{songName}") { backStackEntry ->
            val songName = backStackEntry.arguments?.getString("songName")
            if (songName != null) {
                Log.d("Navegacion", "Navegando a los detalles de la canción: $songName")
                SongDetailScreen(songName)
            }else {
                Log.e("Navegacion", "No se encontró el nombre de la canción")
            }
        }
    }
}
