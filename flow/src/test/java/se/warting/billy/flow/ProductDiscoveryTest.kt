package se.warting.billy.flow

import com.android.billingclient.api.ProductDetails
import org.junit.Test

class ProductDiscoveryTest {

    @Test
    fun `new interface methods exist`() {
        // This test verifies that the new methods exist in the interface
        val observer = object : PurchaseObserver {
            override fun getActiveSubscriptions() = kotlinx.coroutines.flow.flowOf(emptyList<com.android.billingclient.api.Purchase>())
            override fun getStatusFlow(product: Product) = kotlinx.coroutines.flow.flowOf(ProductStatus.Loading(product))
            override fun getPurchases() = kotlinx.coroutines.flow.flowOf(emptyList<com.android.billingclient.api.Purchase>())
            override fun getInAppPurchases() = kotlinx.coroutines.flow.flowOf(emptyList<com.android.billingclient.api.Purchase>())
            override fun getAvailableSubscriptions() = kotlinx.coroutines.flow.flowOf(emptyList<ProductDetails>())
            override fun getAvailableInAppProducts() = kotlinx.coroutines.flow.flowOf(emptyList<ProductDetails>())
            override fun refreshStatus() {}
            override fun connected(connected: Boolean) {}
            override fun configureSubscriptionProducts(productIds: List<String>) {}
            override fun configureInAppProducts(productIds: List<String>) {}
        }

        // Test that configuration methods exist and can be called
        observer.configureSubscriptionProducts(listOf("test_subscription"))
        observer.configureInAppProducts(listOf("test_inapp"))
        
        // Test that the new flow methods exist
        val subscriptionsFlow = observer.getAvailableSubscriptions()
        val inAppProductsFlow = observer.getAvailableInAppProducts()
        
        // If this compiles and runs without exception, the methods exist
        assert(subscriptionsFlow != null)
        assert(inAppProductsFlow != null)
    }
}