package com.example.lab5_part3_a206163

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.lab5_part3_a206163.ui.theme.Lab5_part3_a206163Theme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

// 假设后端返回格式：
// {
//   "temperature": 22.5,
//   "description": "Sunny"
// }
@Serializable
data class WeatherInfo(
    val temperature: Double,
    val description: String
)

interface WeatherApiService {
    @GET("weather")
    suspend fun getCurrentWeather(): WeatherInfo
}

// 替换为实际可用的后端地址
private const val BASE_URL = "https://api.example.com/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

object WeatherApi {
    val retrofitService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }
}

sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(val temperature: Double, val description: String) : WeatherUiState
    object Error : WeatherUiState
}

class WeatherViewModel : ViewModel() {
    var uiState: WeatherUiState by mutableStateOf(WeatherUiState.Loading)
        private set

    fun getWeather() {
        viewModelScope.launch {
            uiState = try {
                val weather = WeatherApi.retrofitService.getCurrentWeather()
                WeatherUiState.Success(weather.temperature, weather.description)
            } catch (e: Exception) {
                WeatherUiState.Error
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel = WeatherViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 启动请求天气数据
        viewModel.getWeather()

        setContent {
            Lab5_part3_a206163Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherScreen(
                        uiState = viewModel.uiState,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherScreen(uiState: WeatherUiState, modifier: Modifier = Modifier) {
    when (uiState) {
        WeatherUiState.Loading -> Text("Loading weather...", modifier = modifier)
        is WeatherUiState.Success -> Text(
            "Temperature: ${uiState.temperature}°C\nDescription: ${uiState.description}",
            modifier = modifier
        )
        WeatherUiState.Error -> Text("Failed to load weather data", modifier = modifier)
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherPreview() {
    Lab5_part3_a206163Theme {
        // 预览中使用一个模拟的成功状态
        WeatherScreen(
            uiState = WeatherUiState.Success(22.5, "Sunny")
        )
    }
}
