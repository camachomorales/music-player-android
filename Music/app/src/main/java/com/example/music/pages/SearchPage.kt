package com.example.music.pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.music.R
import com.example.music.ui.theme.MusicTheme
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@Composable
fun SearchPage(navController: NavHostController) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchResults by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var results by remember { mutableStateOf<List<String>>(emptyList()) }
    var artistName by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var currentlyPlayingTrack by remember { mutableStateOf<Track?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        currentlyPlayingTrack?.let { track ->
            Text(
                text = "Reproduciendo: ${track.name} - ${track.artist}",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Usar Column para organizar la barra de búsqueda y los resultados
        Column(modifier = Modifier.fillMaxSize()) {
            // Barra de búsqueda
            TransparentSearchBar(
                onSearch = { query ->
                    Log.d("SearchPage", "Consulta de búsqueda: $query")
                    artistName = query
                    isSearchActive = true

                    // Use the coroutine scope to launch the search
                    coroutineScope.launch {
                        Log.d("SearchPage", "Iniciando búsqueda...")
                        isLoading = true
                        try {
                            searchResults = search(query) // Call your suspend function
                            Log.d("SearchPage", "Resultados de búsqueda obtenidos: $searchResults")
                        } catch (e: Exception) {
                            errorMessage = "Error al buscar: ${e.message}"
                            Log.e("SearchPage", "Error en búsqueda: ${e.message}")
                        } finally {
                            isLoading = false
                            Log.d("SearchPage", "Búsqueda finalizada.")
                        }
                    }
                    // Ejecutar la búsqueda en un coroutine
                    /*
                    scope.launch {
                        try {
                            searchResults = search(query) // Llama a la función de búsqueda
                        } catch (e: Exception) {
                            errorMessage = "Error al buscar: ${e.message}"
                        } finally {
                            isLoading = false // Finalizar la carga
                        }
                    }*/

                },
                onClose = {
                    Log.d("SearchPage", "Cerrando barra de búsqueda")
                    artistName = ""
                    //results = emptyList()
                    searchResults = emptyList()
                    isSearchActive = false
                    // albums = emptyList()
                    //tracks = emptyList()
                    //currentlyPlayingTrack = null
                }
            )
            // Realiza la búsqueda solo si la búsqueda está activa

            // Realiza la búsqueda
            if (isSearchActive) {
                SearchArtist(
                    query = artistName,
                    searchType = SearchType.ARTIST,
                    onResultsUpdated = { newResults ->
                        Log.d("SearchPage", "Resultados actualizados: $newResults")
                        results = newResults
                    },
                    onArtistClick = { selectedArtist ->
                        Log.d("SearchPage", "Artista seleccionado: $selectedArtist")
                        // Aquí puedes buscar álbumes y pistas relacionadas
                        navController.navigate("artistAlbums/$selectedArtist")

                    },
                    onTrackClick = { track ->
                        Log.d("SearchPage", "Pista seleccionada: ${track.name}")
                        // Maneja la reproducción de la canción
                        currentlyPlayingTrack = track

                    }
                )
            }
// Carga de fondo y mensajes
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center // Centra el indicador de carga
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Log.d("SearchPage", "Cargando resultados...")
                }
            } else {
                errorMessage?.let {
                    Log.e("SearchPage", "Mensaje de error: $it")
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp) // Agrega un padding para el mensaje de error
                    )
                }

                // Mostrar resultados en una lista deslizante solo si la búsqueda está activa
                if (isSearchActive) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize() // O el tamaño que necesites
                            .background(Color.Transparent) // Asegúrate de que el fondo de la LazyColumn sea transparente
                            .padding(bottom = 65     .dp) // Ajusta el padding inferior para evitar superposición con la barra de navegación
                    ) {
                        if (searchResults.isNotEmpty()) {
                            // Encabezado de resultados de búsqueda
                            item {
                                Text(
                                    text = "Resultados de búsqueda",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color.White
                                )
                            }
                        }

                        // Mostrar resultados
                        items(searchResults) { result ->
                            when (result) {
                                is SearchResult.ArtistResult -> {
                                    ArtistItem(artistName = result.artist.name) {
                                        Log.d(
                                            "SearchPage",
                                            "Navegando a álbumes del artista: ${result.artist.name}"
                                        )
                                        navController.navigate("artistAlbums/${result.artist.name}")
                                    }
                                }

                                is SearchResult.AlbumResult -> {
                                    AlbumItem(album = result.album) {
                                        Log.d(
                                            "SearchPage",
                                            "Navegando a detalles del álbum: ${result.album.name}"
                                        )
                                        navController.navigate("albumDetails/${result.album.name}/${result.album.artist}")
                                    }
                                }

                                is SearchResult.TrackResult -> {
                                    TrackItem(track = result.track) {
                                        Log.d(
                                            "SearchPage",
                                            "Reproduciendo la pista: ${result.track.name}"
                                        )
                                        currentlyPlayingTrack = result.track
                                    }
                                }
                            }
                        }

                        if (searchResults.isEmpty()) {
                            // Mensaje si no hay resultados
                            item {
                                Log.d("SearchPage", "No se encontraron resultados")
                                Text(
                                    "No se encontraron resultados",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class SearchType {
    ARTIST,
    ALBUM,
    TRACK
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransparentSearchBar(onSearch: (String) -> Unit, onClose: () -> Unit) {
    val textFieldState = remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Log.d("TransparentSearchBarExample", "Estado inicial de la barra de búsqueda: expandido = $expanded") // Log del estado inicial

    SearchBar(
        modifier = Modifier.fillMaxWidth(),
        inputField = {
            SearchBarDefaults.InputField(
                query = textFieldState.value,
                onQueryChange = { newQuery ->
                    textFieldState.value = newQuery
                    Log.d("TransparentSearchBarExample", "Consulta actualizada: $newQuery") // Log de cambio de consulta
                },
                onSearch = {
                    // Llama a onSearch y cierra las sugerencias
                    onSearch(textFieldState.value)
                    expanded = false // Cierra las sugerencias
                    Log.d("TransparentSearchBarExample", "Búsqueda realizada: ${textFieldState.value}") // Log de búsqueda
                },
                expanded = expanded,
                onExpandedChange = { newExpanded ->
                    expanded = newExpanded
                    Log.d("TransparentSearchBarExample", "Estado de expansión cambiado: $expanded") // Log de cambio de expansión
                },
                placeholder = { Text("Buscar...", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        textFieldState.value = "" // Limpiar el texto
                        expanded = false // Colapsar la barra de búsqueda
                        onClose()
                        Log.d("TransparentSearchBarExample", "Barra de búsqueda cerrada y texto limpiado") // Log de cierre
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                )
            )
        },
        expanded = expanded,
        onExpandedChange = { newExpanded ->
            expanded = newExpanded
            Log.d("TransparentSearchBarExample", "Estado de expansión cambiado: $expanded") // Log de cambio de expansión
        },
        colors = SearchBarDefaults.colors(
            containerColor = Color.Transparent
        )
    ) {
        // Muestra sugerencias si es necesario
        Column {
            repeat(5) { index ->
                ListItem(
                    headlineContent = { Text("Sugerencia $index") },
                    modifier = Modifier.clickable {
                        textFieldState.value = "Sugerencia $index"
                        expanded = false // Cierra las sugerencias al seleccionar
                        onSearch(textFieldState.value) // Realiza la búsqueda
                        Log.d("TransparentSearchBarExample", "Sugerencia seleccionada: Sugerencia $index") // Log de sugerencia seleccionada
                    }
                )
            }
        }
    }
}
@Composable
fun SearchArtist(
    query: String,
    searchType: SearchType,
    onResultsUpdated: (List<String>) -> Unit,
    onArtistClick: (String) -> Unit,
    onTrackClick: (Track) -> Unit
) {
    val scope = rememberCoroutineScope()
    var artistResults by remember { mutableStateOf<List<Artist>>(emptyList()) }
    var trackResults by remember { mutableStateOf<List<Track>>(emptyList()) }
    var albumResults by remember { mutableStateOf<List<Album>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            isLoading = true
            errorMessage = null
            try {
                when (searchType) {
                    SearchType.ARTIST -> {
                        artistResults =
                            RetrofitInstance.api.searchArtist(artist = query).results.artistmatches.artist
                        onResultsUpdated(artistResults.map { it.name })
                    }

                    SearchType.TRACK -> {
                        trackResults =
                            RetrofitInstance.api.searchTrack(track = query).results.trackmatches.track
                        onResultsUpdated(trackResults.map { it.name })
                    }

                    SearchType.ALBUM -> {
                        albumResults =
                            RetrofitInstance.api.searchAlbum(album = query).results.albummatches.album // Asegúrate de que esto esté aquí
                        onResultsUpdated(albumResults.map { it.name }) // Actualiza los resultados para álbumes
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error al buscar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}

@Composable
fun SearchResults(results: List<String>, onArtistClick: (String) -> Unit) {
    Log.d("SearchResults", "Resultados a mostrar: $results") // Log de resultados a mostrar

    if (results.isEmpty()) {
        Log.d("SearchResults", "No se encontraron resultados") // Log si no hay resultados
        Text("No se encontraron resultados", color = Color.White)
    } else {
        LazyColumn {
            items(results) { artistName ->
                ArtistItem(artistName = artistName) {
                    Log.d("SearchResults", "Artista seleccionado: $artistName") // Log de artista seleccionado
                    onArtistClick(artistName) // Llama a la función cuando se selecciona un artista
                }
            }
        }
    }
}
@Composable
fun ArtistItem(artistName: String, onArtistClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onArtistClick)
            .background(Color.Transparent) // Fondo transparente
            .padding(16.dp) // Espaciado
    ) {
        Text(
            text = artistName,
            color = Color.White,
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun TrackItem(track: Track, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(Color.Transparent) // Fondo transparente
            .padding(16.dp) // Espaciado
    ) {
        Column {
            Text(track.name, color = Color.White) // Texto blanco
            Text("Artista: ${track.artist}", color = Color.White) // Texto blanco
        }
    }
}

@Composable
fun AlbumItem(album: Album, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(Color.Transparent) // Fondo transparente
            .padding(16.dp) // Espaciado
    ) {
        Column {
            Text(album.name, color = Color.White) // Texto blanco
            Text("Artista: ${album.artist}", color = Color.White) // Texto blanco
        }
    }
}
suspend fun search(query: String): List<SearchResult> {
    val musicRepository = MusicRepository(RetrofitInstance.api)

    val artistResults = musicRepository.searchArtists(query)
    val trackResults = musicRepository.searchTracks(query)
    val albumResults = musicRepository.searchAlbums(query)

    val combinedResults = mutableListOf<SearchResult>()
    combinedResults.addAll(artistResults.map { SearchResult.ArtistResult(it) })
    combinedResults.addAll(trackResults.map { SearchResult.TrackResult(it) })
    combinedResults.addAll(albumResults.map { SearchResult.AlbumResult(it) })

    return combinedResults
}

class MusicRepository(private val api: LastFmApi) {

    suspend fun searchArtists(query: String): List<Artist> {
        return api.searchArtist(artist = query).results.artistmatches.artist
    }

    suspend fun searchTracks(query: String): List<Track> {
        return api.searchTrack(track = query).results.trackmatches.track
    }

    suspend fun searchAlbums(query: String): List<Album> {
        return api.searchAlbum(album = query).results.albummatches.album
    }
    /*
        suspend fun getArtistAlbums(artist: String): List<Album> {
            return api.getArtistAlbums(artist = artist).results.albummatches.album
        }*/
    suspend fun getArtistAlbums(artist: String): List<Album> {
        return api.getArtistAlbums(artist = artist).artist.albums.album
    }
}

// Clases para la respuesta JSON de la API de Last.fm

// Respuesta de álbumes de un artista
data class ArtistInfoResponse(
    val artist: ArtistInfo
)

data class ArtistInfo(
    val name: String,
    val url: String,
    val albums: AlbumMatches
)

// Respuesta de álbumes
data class AlbumInfoResponse(
    val album: AlbumDetail
)

data class AlbumDetail(
    val name: String,
    val artist: String,
    val tracks: TrackList,
    val image: List<Image>
)

// Listado de pistas
data class TrackList(val track: List<TrackDetail>)

data class TrackDetail(
    val name: String,
    val url: String
)

// Respuesta de búsqueda de artista
data class ArtistSearchResponse(val results: Results)

data class Results(val artistmatches: ArtistMatches)

data class ArtistMatches(val artist: List<Artist>)

data class Artist(
    val name: String,
    val url: String
)

// Respuesta de búsqueda de pistas
data class TrackSearchResponse(val results: TrackResults)

data class TrackResults(val trackmatches: TrackMatches)

data class TrackMatches(val track: List<Track>)

data class Track(
    val name: String,
    val url: String,
    val artist: String
)

// Respuesta de búsqueda de álbumes
data class AlbumSearchResponse(val results: AlbumResults)

data class AlbumResults(val albummatches: AlbumMatches)

data class AlbumMatches(val album: List<Album>)

data class Album(
    val name: String,
    val url: String,
    val artist: String
)

// Clase para la respuesta de los álbumes más populares de un artista
data class ArtistAlbumsResponse(
    val topalbums: TopAlbums
)

data class TopAlbums(
    val album: List<AlbumDetail>
)

// Clase para manejar resultados de búsqueda
sealed class SearchResult {
    data class ArtistResult(val artist: Artist) : SearchResult()
    data class AlbumResult(val album: Album) : SearchResult()
    data class TrackResult(val track: Track) : SearchResult()
}

// Clase para las imágenes
data class Image(
    val text: String,
    val size: String
)

// Interfaz para las solicitudes de la API
interface LastFmApi {
    @GET("2.0/")
    suspend fun searchArtist(
        //@Query("method") method: String = "artist.getInfo",
        @Query("method") method: String = "artist.search",
        @Query("artist") artist: String,
        @Query("api_key") apiKey: String = "5a21bfe4c62ad0b6dc214546e23f2698",
        @Query("format") format: String = "json"
    ): ArtistSearchResponse

    @GET("2.0/")
    suspend fun searchTrack(
        @Query("method") method: String = "track.search",
        @Query("track") track: String,
        @Query("api_key") apiKey: String = "5a21bfe4c62ad0b6dc214546e23f2698",
        @Query("format") format: String = "json"
    ): TrackSearchResponse

    @GET("2.0/")
    suspend fun searchAlbum(
        @Query("method") method: String = "album.search",
        @Query("album") album: String,
        @Query("api_key") apiKey: String = "5a21bfe4c62ad0b6dc214546e23f2698",
        @Query("format") format: String = "json"
    ): AlbumSearchResponse
    /*
        @GET("2.0/")
        suspend fun getArtistAlbums(
            @Query("method") method: String = "artist.getTopAlbums",
            @Query("artist") artist: String,
            @Query("api_key") apiKey: String = "5a21bfe4c62ad0b6dc214546e23f2698",
            @Query("format") format: String = "json"
        ): ArtistInfoResponse*/




    @GET("2.0/")
    suspend fun getArtistAlbums(
        @Query("method") method: String = "artist.getinfo",
        @Query("artist") artist: String,
        @Query("api_key") apiKey: String = "5a21bfe4c62ad0b6dc214546e23f2698",
        @Query("format") format: String = "json"
    ): ArtistInfoResponse


    @GET("2.0/")
    suspend fun getAlbumInfo(
        @Query("method") method: String = "album.getinfo",
        @Query("api_key") apiKey: String = "5a21bfe4c62ad0b6dc214546e23f2698",
        @Query("album") album: String,
        @Query("artist") artist: String,
        @Query("format") format: String = "json"
    ): AlbumInfoResponse

}



// Configuración de Retrofit
object RetrofitInstance {
    private const val BASE_URL = "https://ws.audioscrobbler.com/"

    // Configura el interceptor
    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY) // Cambia el nivel según lo que necesites
    }

    // Crea el cliente OkHttp y añade el interceptor
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val api: LastFmApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(LastFmApi::class.java)
}

@Preview(showBackground = true)
@Composable
fun PreviewSearchPage() {
    val navController = rememberNavController() // Crea un NavHostController simulado
    MusicTheme {
        SearchPage(navController = navController) // Pasa el navController a SearchPage
    }
}








