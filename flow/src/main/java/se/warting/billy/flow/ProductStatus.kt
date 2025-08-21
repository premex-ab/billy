package se.warting.billy.flow

import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

/**
 * Defines the status of a given [Product]
 */
public sealed class ProductStatus {

    /**
     * The given [Product] from this status
     */
    public abstract val type: Product

    public data class Owned(override val type: Product, val purchase: List<Purchase>) : ProductStatus()

    public data class Loading(override val type: Product) : ProductStatus()

    /**
     * Indicates that the product is available for purchase.
     * This status provides access to pricing information through convenient properties:
     * - [formattedPrice]: Primary price for the product (first offer's first pricing phase for subscriptions)
     * - [allFormattedPrices]: List of all available prices across all offers and phases
     */
    public data class Available(
        override val type: Product,
        val productDetails: ProductDetails
    ) : ProductStatus() {
        
        /**
         * Gets the formatted price for the primary offer.
         * For subscriptions, this returns the price of the first offer's first pricing phase.
         * For in-app products, this returns the one-time purchase price.
         * Returns null if no price information is available.
         */
        public val formattedPrice: String?
            get() = when (type) {
                is Product.Subscription -> {
                    productDetails.subscriptionOfferDetails
                        ?.firstOrNull()
                        ?.pricingPhases
                        ?.pricingPhaseList
                        ?.firstOrNull()
                        ?.formattedPrice
                }
                is Product.InAppProduct -> {
                    productDetails.oneTimePurchaseOfferDetails?.formattedPrice
                }
            }

        /**
         * Gets all available formatted prices for this product.
         * For subscriptions, this includes all pricing phases from all offers.
         * For in-app products, this returns a list with a single price.
         */
        public val allFormattedPrices: List<String>
            get() = when (type) {
                is Product.Subscription -> {
                    productDetails.subscriptionOfferDetails
                        ?.flatMap { offer ->
                            offer.pricingPhases.pricingPhaseList.mapNotNull { phase ->
                                phase.formattedPrice
                            }
                        } ?: emptyList()
                }
                is Product.InAppProduct -> {
                    productDetails.oneTimePurchaseOfferDetails?.formattedPrice?.let { 
                        listOf(it) 
                    } ?: emptyList()
                }
            }

        /**
         * Initiates purchase flow for a subscription with a specific offer.
         * @param offer The subscription offer to purchase
         */
        public fun buy(offer: ProductDetails.SubscriptionOfferDetails) {

            val productDetailsParams: BillingFlowParams.ProductDetailsParams =
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setOfferToken(offer.offerToken)
                    .setProductDetails(productDetails)
                    .build()

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()
            buy(flowParams)
        }

        /**
         * Initiates purchase flow for this product.
         * For subscriptions, uses the first available offer.
         * For in-app products, uses the one-time purchase offer.
         */
        public fun buy() {
            when (type) {
                is Product.Subscription -> {
                    productDetails.subscriptionOfferDetails?.firstOrNull()?.let { offer ->
                        buy(offer)
                    }
                }
                is Product.InAppProduct -> {
                    val productDetailsParams: BillingFlowParams.ProductDetailsParams =
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()

                    val flowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(listOf(productDetailsParams))
                        .build()
                    buy(flowParams)
                }
            }
        }

        // maybe public?
        private fun buy(billingFlowParams: BillingFlowParams) {
            BillingProvider.instance.buy(billingFlowParams)
        }
    }

    public data class Unavailable(override val type: Product) : ProductStatus()
}
