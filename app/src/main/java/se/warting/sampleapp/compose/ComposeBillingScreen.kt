package se.warting.sampleapp.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.ProductDetails
import se.warting.billy.flow.Product
import se.warting.billy.flow.ProductStatus
import se.warting.billy.flow.BillingProvider

@Composable
fun ComposeBillingScreen() {
    val earlyBirdProduct: Product.Subscription = remember { Product.Subscription("early_bird") }

    val earlyBirdProductStatus by earlyBirdProduct.statusFlow.collectAsState(
        initial = ProductStatus.Loading(earlyBirdProduct)
    )

    // Configure available products for discovery
    LaunchedEffect(Unit) {
        BillingProvider.instance.configureSubscriptionProducts(
            listOf("early_bird", "premium_monthly", "premium_yearly")
        )
        BillingProvider.instance.configureInAppProducts(
            listOf("remove_ads", "extra_features")
        )
    }

    // Collect all available products
    val availableSubscriptions by BillingProvider.instance.getAvailableSubscriptions()
        .collectAsState(initial = emptyList())
    val availableInAppProducts by BillingProvider.instance.getAvailableInAppProducts()
        .collectAsState(initial = emptyList())

    // Collect all purchase history
    val allHistory by BillingProvider.instance.getPurchases().collectAsState(initial = listOf())
    val historyForProduct = remember(allHistory) {
        allHistory.sortedByDescending { it.purchaseTime }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SampleProductSection(earlyBirdProduct, earlyBirdProductStatus)
        AllAvailableProductsSection(availableSubscriptions, availableInAppProducts)
        PurchaseHistorySection(historyForProduct)
    }
}

@Composable
private fun SampleProductSection(
    earlyBirdProduct: Product.Subscription,
    earlyBirdProductStatus: ProductStatus
) {
    Text("Sample Product: ${earlyBirdProduct.name}")

    when (val status = earlyBirdProductStatus) {
        is ProductStatus.Available -> {
            Text("Available!")
            LazyColumn {
                items(status.productDetails.subscriptionOfferDetails ?: listOf()) { offer ->
                    Button(onClick = { status.buy(offer) }) {
                        val price = offer.pricingPhases.pricingPhaseList.firstOrNull()?.formattedPrice
                        Text(text = "Buy $price")
                    }
                }
            }
        }

        is ProductStatus.Loading -> Text("Loading....")
        is ProductStatus.Unavailable -> Text("Unavailable")
        is ProductStatus.Owned -> Text("Owned")
    }
}

@Composable
private fun AllAvailableProductsSection(
    availableSubscriptions: List<ProductDetails>,
    availableInAppProducts: List<ProductDetails>
) {
    // Show all available subscriptions discovered
    if (availableSubscriptions.isNotEmpty()) {
        Text("\nAll Available Subscriptions:")
        LazyColumn {
            items(availableSubscriptions) { productDetails ->
                ProductDetailsCard(productDetails = productDetails)
            }
        }
    }

    // Show all available in-app products discovered
    if (availableInAppProducts.isNotEmpty()) {
        Text("\nAll Available In-App Products:")
        LazyColumn {
            items(availableInAppProducts) { productDetails ->
                ProductDetailsCard(productDetails = productDetails)
            }
        }
    }
}

@Composable
private fun PurchaseHistorySection(historyForProduct: List<com.android.billingclient.api.Purchase>) {
    // Show previous purchases (including expired/canceled)
    if (historyForProduct.isNotEmpty()) {
        Text("Previous purchases:")
        LazyColumn {
            items(historyForProduct) { record ->
                Text("• ${record.purchaseTime}")
            }
        }
    }
}

@Composable
private fun ProductDetailsCard(productDetails: ProductDetails) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text("Product: ${productDetails.productId}")
        Text("Name: ${productDetails.name}")
        Text("Description: ${productDetails.description}")

        // Show pricing for subscriptions
        productDetails.subscriptionOfferDetails?.firstOrNull()?.let { offer ->
            offer.pricingPhases.pricingPhaseList.firstOrNull()?.let { phase ->
                Text("Price: ${phase.formattedPrice}")
            }
        }

        // Show pricing for one-time products
        productDetails.oneTimePurchaseOfferDetails?.let { offer ->
            Text("Price: ${offer.formattedPrice}")
        }
    }
}
