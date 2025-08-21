package se.warting.sampleapp.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.warting.billy.flow.Product
import se.warting.billy.flow.ProductStatus

@Composable
fun ComposeBillingScreen() {

    val earlyBirdProduct: Product.Subscription = remember { Product.Subscription("early_bird") }

    val earlyBirdProductStatus by earlyBirdProduct.statusFlow.collectAsState(
        initial = ProductStatus.Loading(earlyBirdProduct)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(earlyBirdProduct.name)

        when (val status = earlyBirdProductStatus) {
            is ProductStatus.Available -> {

                Text("Available!")
                
                // Show the primary price using the new convenient property
                status.formattedPrice?.let { price ->
                    Text("Primary Price: $price")
                }
                
                // Show all available prices
                if (status.allFormattedPrices.isNotEmpty()) {
                    Text("All Prices: ${status.allFormattedPrices.joinToString(", ")}")
                }

                // Simple buy button using the new convenience method
                Button(onClick = {
                    status.buy() // Simplified API - automatically handles subscription vs in-app
                }) {
                    Text(text = "Buy Now")
                }

                LazyColumn {
                    items(status.productDetails.subscriptionOfferDetails ?: listOf()) { offer ->
                        Button(onClick = {
                            status.buy(offer) // Specific offer purchase (existing API)
                        }) {
                            // Use the original verbose way for comparison
                            Text(text = "Buy Specific Offer ${offer.pricingPhases.pricingPhaseList.firstOrNull()!!.formattedPrice}")
                        }
                    }
                }
            }

            is ProductStatus.Loading -> Text("Loading....")
            is ProductStatus.Unavailable -> Text("Unavailable")
            is ProductStatus.Owned -> Text("Owned")
        }
    }
}
