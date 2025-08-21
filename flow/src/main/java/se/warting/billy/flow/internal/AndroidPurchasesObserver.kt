package se.warting.billy.flow.internal

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import se.warting.billy.flow.Product
import se.warting.billy.flow.ProductStatus
import se.warting.billy.flow.PurchaseObserver

internal data class CombinedPurchaseData(
    val itemPurchases: List<Purchase>,
    val subPurchases: List<Purchase>
) {
    val purchases: List<Purchase>
        get() = itemPurchases + subPurchases
}

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions")
internal class AndroidPurchasesObserver(
    private val billingClient: BillingClient,
) : PurchaseObserver, LifecycleEventObserver {

    private val _subPurchasesStateFlow: MutableStateFlow<List<Purchase>> by lazy {
        MutableStateFlow(listOf())
    }
    private val _itemPurchasesStateFlow: MutableStateFlow<List<Purchase>> by lazy {
        MutableStateFlow(listOf())
    }

    // Configuration for product discovery
    private val _subscriptionProductIds: MutableStateFlow<List<String>> by lazy {
        MutableStateFlow(listOf())
    }
    private val _inAppProductIds: MutableStateFlow<List<String>> by lazy {
        MutableStateFlow(listOf())
    }

    // Available product details flows
    private val _availableSubscriptions: MutableStateFlow<List<ProductDetails>> by lazy {
        MutableStateFlow(listOf())
    }
    private val _availableInAppProducts: MutableStateFlow<List<ProductDetails>> by lazy {
        MutableStateFlow(listOf())
    }

    override fun configureSubscriptionProducts(productIds: List<String>) {
        _subscriptionProductIds.value = productIds
        refreshAvailableProducts()
    }

    override fun configureInAppProducts(productIds: List<String>) {
        _inAppProductIds.value = productIds
        refreshAvailableProducts()
    }

    override fun getActiveSubscriptions(): Flow<List<Purchase>> {
        return _subPurchasesStateFlow
    }

    override fun getInAppPurchases(): Flow<List<Purchase>> {
        return _itemPurchasesStateFlow
    }

    override fun getPurchases(): Flow<List<Purchase>> {
        return combine(
            _subPurchasesStateFlow, _itemPurchasesStateFlow
        ) { subs, inapps ->
            subs + inapps
        }
    }

    override fun getAvailableSubscriptions(): Flow<List<ProductDetails>> {
        return _availableSubscriptions
    }

    override fun getAvailableInAppProducts(): Flow<List<ProductDetails>> {
        return _availableInAppProducts
    }

    private suspend fun queryProductDetails(productIds: List<String>, productType: String): List<ProductDetails> {
        val productList = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        }

        val productDetailsParams = QueryProductDetailsParams
            .newBuilder()
            .setProductList(productList)
            .build()

        val productDetailsResult = billingClient.queryProductDetails(productDetailsParams)
        return productDetailsResult.productDetailsList ?: listOf()
    }

    private fun getCombinedPurchasesFlow() =
        combine(_subPurchasesStateFlow, _itemPurchasesStateFlow) { subPurchases, itemPurchases ->
            CombinedPurchaseData(
                subPurchases = subPurchases, itemPurchases = itemPurchases
            )
        }

    override fun getStatusFlow(product: Product): Flow<ProductStatus> {
        return combine(getCombinedPurchasesFlow(), product.detailsFlow) { alles, skuDetailsList ->
            val skuList = ArrayList<String>()
            skuList.add(product.name)

            skuStatusResolver(
                product, skuDetailsList, alles.purchases
            )
        }
    }

    private fun skuStatusResolver(
        product: Product,
        skuDetailsList: List<ProductDetails>,
        ownedItemsList: List<Purchase>,
    ): ProductStatus {

        val ownedItem = ownedItemsList.filter { it.products.contains(product.name) }
        if (ownedItem.isNotEmpty()) {
            return ProductStatus.Owned(product, ownedItem)
        }

        val skuDetails: ProductDetails? =
            skuDetailsList.firstOrNull { it.productId == product.name }
        return if (skuDetails != null) {
            ProductStatus.Available(product, skuDetails)
        } else {
            ProductStatus.Unavailable(product)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> refreshStatus()
            ON_RESUME -> refreshStatus()
            else -> return
        }
    }

    private val subsPurchasesObserver = PurchasesResponseListener { billingResult, purchasesList ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _subPurchasesStateFlow.value = purchasesList
        }
    }

    private val inAppPurchasesObserver = PurchasesResponseListener { billingResult, purchasesList ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _itemPurchasesStateFlow.value = purchasesList.map {
                it
            }
        }
    }

    override fun refreshStatus() {
        if (isConnected) {
            val queryPurchasesSubsParams =
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                    .build()

            val queryPurchasesInAppParams =
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                    .build()

            billingClient.queryPurchasesAsync(queryPurchasesSubsParams, subsPurchasesObserver)
            billingClient.queryPurchasesAsync(queryPurchasesInAppParams, inAppPurchasesObserver)

            refreshAvailableProducts()
        }
    }

    private fun refreshAvailableProducts() {
        if (isConnected) {
            // Refresh subscriptions
            val subscriptionIds = _subscriptionProductIds.value
            if (subscriptionIds.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val subscriptionDetails = queryProductDetails(subscriptionIds, BillingClient.ProductType.SUBS)
                    _availableSubscriptions.value = subscriptionDetails
                }
            }

            // Refresh in-app products
            val inAppIds = _inAppProductIds.value
            if (inAppIds.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val inAppDetails = queryProductDetails(inAppIds, BillingClient.ProductType.INAPP)
                    _availableInAppProducts.value = inAppDetails
                }
            }
        }
    }

    private var isConnected = false
    override fun connected(connected: Boolean) {
        isConnected = connected
        if (connected) {
            refreshAvailableProducts()
        }
    }
}
