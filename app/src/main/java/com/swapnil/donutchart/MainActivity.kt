package com.swapnil.donutchart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.swapnil.donutchart.presentation.DonutChart
import com.swapnil.donutchart.presentation.ui.theme.DonutChartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DonutChartTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    val sortedCoins= listOf<Coin>(
                        Coin("Bitcoin",50.0,"https://s2.coinmarketcap.com/static/img/coins/64x64/1.png"),
                        Coin("Etherium",150.0,"https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png"),
                        Coin("Tether USDt",250.0,"https://s2.coinmarketcap.com/static/img/coins/64x64/825.png"),
                        Coin("XRP",500.0,"https://s2.coinmarketcap.com/static/img/coins/64x64/52.png"),
                        Coin("BNB",80.0,"https://s2.coinmarketcap.com/static/img/coins/64x64/1839.png"),
                        Coin("Solana",170.0,"https://s2.coinmarketcap.com/static/img/coins/64x64/5426.png"),

                        )
                    val (pieChartData, imageUrls) = remember(sortedCoins) {
                        when {
                            sortedCoins.size <= 4 -> {
                                sortedCoins.associate { it.name to (it.quantity?.toInt() ?: 0) } to
                                        sortedCoins.map { it.logo.toString() }
                            }

                            else -> {
                                val topCoins = sortedCoins.take(4)
                                    .associate { it.name to (it.quantity?.toInt() ?: 0) }
                                val topImageUrls = sortedCoins.take(4).map { it.logo }
                                val othersQuantity = sortedCoins.drop(4).sumOf { it.quantity ?: 0.0 }.toInt()

                                val dummyUrl =
                                    "https://cdn-icons-png.freepik.com/256/17470/17470916.png?semt=ais_hybrid"
                                if (othersQuantity > 0) {
                                    topCoins + ("Others" to othersQuantity) to (topImageUrls + dummyUrl)
                                } else {
                                    topCoins to topImageUrls
                                }
                            }
                        }
                    }
                    val pieChartState = remember { mutableStateOf(pieChartData) }

                    DonutChart(
                        data = pieChartState.value,
                        ringBorderColor = MaterialTheme.colorScheme.tertiaryContainer,
                        ringBgColor = MaterialTheme.colorScheme.tertiary,
                        imageUrls = imageUrls // Pass the image URLs
                    )
                }

            }
        }
    }
}
data class Coin(
    val name: String,
    val quantity: Double,
    val logo: String
)

