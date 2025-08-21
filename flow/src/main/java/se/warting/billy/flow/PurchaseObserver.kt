package se.warting.billy.flow

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.Flow

/**
 * Observes purchase status changes
 */
public interface PurchaseObserver {

    /**
     * @return a flow with all purchases made
     */
    public fun getActiveSubscriptions(): Flow<List<Purchase>>

    public fun getStatusFlow(product: Product): Flow<ProductStatus>

    /**
     * @return a flow with the full purchase history (INAPP + SUBS)
     */
    public fun getPurchases(): Flow<List<Purchase>>

    /**
     * @return a flow with all in-app purchases made
     */
    public fun getInAppPurchases(): Flow<List<Purchase>>

    /**
     * @return a flow with all available subscription product details
     */
    public fun getAvailableSubscriptions(): Flow<List<ProductDetails>>

    /**
     * @return a flow with all available in-app product details
     */
    public fun getAvailableInAppProducts(): Flow<List<ProductDetails>>

    /**
     * Request the observer to refresh the status of the purchases
     *
     * Note: this is called on lifecycle changes
     */
    public fun refreshStatus()

    /**
     * Set the connected state of the observer
     *
     * @param connected true if the observer is connected to the billing service
     */
    public fun connected(connected: Boolean)

    /**
     * Configure the product IDs for subscriptions that should be queried
     *
     * @param productIds list of subscription product IDs
     */
    public fun configureSubscriptionProducts(productIds: List<String>)

    /**
     * Configure the product IDs for in-app products that should be queried
     *
     * @param productIds list of in-app product IDs
     */
    public fun configureInAppProducts(productIds: List<String>)
}
