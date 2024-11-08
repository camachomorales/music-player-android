package com.example.music.SearchNavegacion

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music.pages.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.music.pages.Album

class ArtistAlbumsViewModel : ViewModel() {
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchArtistAlbums(artistName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("ArtistAlbumsViewModel", "Buscando álbumes para el artista: $artistName")
            try {
                // Primero, buscar el artista
                Log.d("ArtistAlbumsViewModel", "Llamando a searchArtist con el nombre: $artistName")
                val artistResponse = RetrofitInstance.api.searchArtist(artist = artistName)
                Log.d("ArtistAlbumsViewModel", "Respuesta de searchArtist: $artistResponse")

                val artistMatches = artistResponse.results.artistmatches.artist
                Log.d("ArtistAlbumsViewModel", "Coincidencias de artistas encontradas: ${artistMatches.size}")

                if (artistMatches.isNotEmpty()) {
                    // Si se encuentra el artista, obtener su nombre
                    val foundArtistName = artistMatches[0].name
                    Log.d("ArtistAlbumsViewModel", "Artista encontrado: $foundArtistName")

                    // Ahora, obtener los álbumes del artista
                    Log.d("ArtistAlbumsViewModel", "Llamando a getArtistAlbums con el artista: $foundArtistName")
                    val albumsResponse = RetrofitInstance.api.getArtistAlbums   (artist = foundArtistName)
                    Log.d("ArtistAlbumsViewModel", "Respuesta de getArtistAlbums: $albumsResponse")

                    // Verifica si albummatches no es nulo y tiene álbumes
                    if (albumsResponse.artist.albums != null && albumsResponse.artist.albums.album.isNotEmpty()) {
                        _albums.value = albumsResponse.artist.albums.album
                        Log.d("ArtistAlbumsViewModel", "Álbumes cargados: ${_albums.value.size}")
                        _errorMessage.value = null // Limpiar el mensaje de error
                    } else {
                        _errorMessage.value = "No se encontraron álbumes para el artista."
                        Log.e("ArtistAlbumsViewModel", "No se encontraron álbumes: $albumsResponse")
                    }
                } else {
                    _errorMessage.value = "Artista no encontrado"
                    Log.d("ArtistAlbumsViewModel", "No se encontraron coincidencias para el artista: $artistName")
                }
            } catch (e: HttpException) {
                _errorMessage.value = "Error al obtener álbumes: ${e.message()}"
                val errorBody = e.response()?.errorBody()?.string() ?: "No error body"
                Log.e("ArtistAlbumsViewModel", "HttpException: ${e.code()} - $errorBody")
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                Log.e("ArtistAlbumsViewModel", "Unexpected error: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
                Log.d("ArtistAlbumsViewModel", "Carga finalizada, isLoading: ${_isLoading.value}")
            }
        }
    }
}