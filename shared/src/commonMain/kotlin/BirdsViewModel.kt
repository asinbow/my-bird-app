import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.BirdImage

data class BirdsUiState (
    val images: List<BirdImage> = emptyList(),
    val selectedCategory: String? = null,
) {
    val categories: Set<String> = images.map { it.category }.toSet()
    val selectedImages: List<BirdImage> = images.filter { it.category == selectedCategory }
}

class BirdsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BirdsUiState())
    val uiState = _uiState.asStateFlow()

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    init {
        updateImage()
    }

    override fun onCleared() {
        httpClient.close()
    }

    fun updateImage() {
        viewModelScope.launch {
            val images = getImages()
            _uiState.update { it.copy(images = images) }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = if (it.selectedCategory != category) category else null) }
    }

    suspend fun getImages(): List<BirdImage> =
        httpClient.get("$imageBaseUrl/pictures.json")
            .body()
}