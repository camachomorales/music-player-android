package com.example.music.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.music.SearchNavegacion.ArtistAlbumsViewModel
import com.example.music.pages.AlbumItem
import com.example.music.pages.RetrofitInstance
import com.example.music.pages.TrackDetail
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue



@Composable
fun ArtistAlbumsScreen(
    artistName: String,
    onSongClick: (String) -> Unit,
    navController: NavHostController,
    viewModel: ArtistAlbumsViewModel = viewModel() // Inyectar el ViewModel
) {
    Log.d("ArtistAlbumsScreen", "Iniciando la pantalla de álbumes para el artista: $artistName")
    // Llamar a la función para obtener álbumes solo cuando cambie el artistName
    LaunchedEffect(artistName) {
        Log.d("ArtistAlbumsScreen", "Llamando a fetchArtistAlbums para el artista: $artistName")
        viewModel.fetchArtistAlbums(artistName)
    }

    // Obtener el estado del ViewModel
    val albums = viewModel.albums.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Álbumes de: $artistName", fontWeight = FontWeight.Bold)

        if (isLoading) {
            Log.d("ArtistAlbumsScreen", "Cargando álbumes...")
            CircularProgressIndicator()
        } else if (errorMessage != null) {
            Log.e("ArtistAlbumsScreen", "Error al cargar álbumes: $errorMessage")
            Text(text = errorMessage!!, color = Color.Red)
        } else {
            Log.d("ArtistAlbumsScreen", "Álbumes cargados: ${albums.size}")
            LazyColumn {
                items(albums) { album ->
                    Log.d("ArtistAlbumsScreen", "Mostrando álbum: ${album.name} de ${album.artist}")
                    AlbumItem(album = album) {
                        Log.d("ArtistAlbumsScreen", "Navegando a detalles del álbum: ${album.name} de ${album.artist}")
                        navController.navigate("albumDetails/${album.name}/${album.artist}")
                    }
                }
            }
        }
    }
}
@Composable
fun AlbumDetailsScreen(
    albumName: String,
    artistName: String,
    onSongClick: (String) -> Unit
) {
    val tracks = remember { mutableStateOf<List<TrackDetail>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    Text(text = "Detalles del álbum: $albumName", color = Color.White)
    LaunchedEffect(albumName) {
        Log.d("AlbumDetailsScreen", "Cargando detalles del álbum: $albumName de $artistName")
        try {
            isLoading.value = true
            val albumInfo = RetrofitInstance.api.getAlbumInfo(album = albumName, artist = artistName)
            tracks.value = albumInfo.album.tracks.track
            Log.d("AlbumDetailsScreen", "Pistas cargadas: ${tracks.value.size}")
        } catch (e: Exception) {
            errorMessage.value = "Error al obtener pistas: ${e.message}"
            Log.e("AlbumDetailsScreen", "Error al cargar pistas: ${e.message}")
        } finally {
            isLoading.value = false
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (isLoading.value) {
            CircularProgressIndicator()
        } else if (errorMessage.value != null) {
            Text(text = errorMessage.value!!, color = Color.Red)
        } else {
            LazyColumn {
                items(tracks.value) { track ->
                    Text(
                        text = track.name,
                        modifier = Modifier
                            .clickable { /* Manejar clic en la canción */ }
                            .padding(16.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}


@Composable
fun SongDetailScreen(songName: String) {
    Log.d("SongDetailScreen", "Reproduciendo la canción: $songName")
    Text(text = "Reproduciendo: $songName", fontWeight = FontWeight.Bold)
}