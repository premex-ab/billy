# Product Price Access Examples

This document shows how to use the new price convenience properties and purchase methods in Billy Android Billing Library.

## Before (Verbose API)

```kotlin
when (val status = product.statusFlow.collectAsState().value) {
    is ProductStatus.Available -> {
        // Complex access to get the price
        val price = status.productDetails
            .subscriptionOfferDetails
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.firstOrNull()
            ?.formattedPrice
        
        if (price != null) {
            Text("Price: $price")
        }

        // Complex purchase flow
        status.productDetails.subscriptionOfferDetails?.firstOrNull()?.let { offer ->
            Button(onClick = { status.buy(offer) }) {
                Text("Buy")
            }
        }
    }
    // ... other states
}
```

## After (Simple API)

### Primary Price Access

```kotlin
when (val status = product.statusFlow.collectAsState().value) {
    is ProductStatus.Available -> {
        // Simple access to the primary price
        status.formattedPrice?.let { price ->
            Text("Price: $price")
        }

        // Simple purchase - automatically handles subscription vs in-app
        Button(onClick = { status.buy() }) {
            Text("Buy Now")
        }
    }
    // ... other states
}
```

### All Available Prices

```kotlin
when (val status = product.statusFlow.collectAsState().value) {
    is ProductStatus.Available -> {
        // Get all available prices (useful for subscriptions with multiple offers)
        val prices = status.allFormattedPrices
        if (prices.isNotEmpty()) {
            Text("Available prices: ${prices.joinToString(", ")}")
        }
    }
    // ... other states
}
```

## Product Types Support

### Subscription Products
- `formattedPrice`: Returns the price of the first offer's first pricing phase
- `allFormattedPrices`: Returns all prices from all offers and pricing phases
- `buy()`: Purchases using the first available offer
- `buy(offer)`: Purchases a specific subscription offer

### In-App Products
- `formattedPrice`: Returns the one-time purchase price
- `allFormattedPrices`: Returns a list with a single price
- `buy()`: Purchases the in-app product

## Benefits

1. **Simplified API**: No need to understand the complex Google Billing API structure
2. **Null Safety**: Properties handle null cases gracefully
3. **Type Safety**: Works correctly with both subscription and in-app products
4. **Backward Compatibility**: Original verbose API remains available
5. **Multiple Offers Support**: `allFormattedPrices` handles products with multiple pricing options
6. **Universal Purchase**: `buy()` method works for both product types automatically