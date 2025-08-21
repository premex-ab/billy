// Example: Using Billy with In-App Products
// This demonstrates how the new price API simplifies in-app product handling

@Composable
fun InAppProductExample() {
    // Create an in-app product
    val coinPackProduct = remember { Product.InAppProduct("coin_pack_100") }
    
    val productStatus by coinPackProduct.statusFlow.collectAsState(
        initial = ProductStatus.Loading(coinPackProduct)
    )
    
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("100 Gold Coins", style = MaterialTheme.typography.h6)
        
        when (val status = productStatus) {
            is ProductStatus.Available -> {
                // Show price using the new simple API
                status.formattedPrice?.let { price ->
                    Text("Price: $price", style = MaterialTheme.typography.body1)
                }
                
                // Purchase using the new simplified buy method
                Button(
                    onClick = { 
                        status.buy() // No need to handle offers for in-app products
                    }
                ) {
                    Text("Purchase Coins")
                }
            }
            
            is ProductStatus.Loading -> {
                CircularProgressIndicator()
                Text("Loading price...")
            }
            
            is ProductStatus.Unavailable -> {
                Text("Product not available", color = MaterialTheme.colors.error)
            }
            
            is ProductStatus.Owned -> {
                Text("Already purchased", color = MaterialTheme.colors.primary)
                // Note: For consumable in-app products, you would typically
                // consume the purchase and show "Available" again
            }
        }
    }
}

/*
 * Key Benefits Demonstrated:
 * 
 * 1. Simple price access: status.formattedPrice
 * 2. Universal buy method: status.buy() works for both subscriptions and in-app
 * 3. No need to understand Google Billing API complexity
 * 4. Null-safe operations
 * 5. Type-aware handling (automatically uses oneTimePurchaseOfferDetails for in-app)
 * 
 * Compare this to the old way:
 * 
 * val price = status.productDetails.oneTimePurchaseOfferDetails?.formattedPrice
 * 
 * val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
 *     .setProductDetails(status.productDetails)
 *     .build()
 * val flowParams = BillingFlowParams.newBuilder()
 *     .setProductDetailsParamsList(listOf(productDetailsParams))
 *     .build()
 * BillingProvider.instance.buy(flowParams)
 */