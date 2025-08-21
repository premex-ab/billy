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
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import se.warting.billy.flow.PurchaseObserver
import se.warting.billy.flow.Product
import se.warting.billy.flow.ProductStatus

internal data class CombinedPurchaseData(
    val itemPurchases: List<Purchase>,
    val subPurchases: List<Purchase>
) {
    val purchases: List<Purchase>
        get() = itemPurchases + subPurchases
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class AndroidPurchasesObserver(
    private val billingClient: BillingClient,
) : PurchaseObserver, LifecycleEventObserver {

    private val _subPurchasesHistoryStateFlow: MutableStateFlow<List<Purchase>> by lazy {
        MutableStateFlow(listOf())
    }
    private val _itemPurchasesHistoryStateFlow: MutableStateFlow<List<Purchase>> by lazy {
        MutableStateFlow(listOf())
    }

    private val _subPurchasesStateFlow: MutableStateFlow<List<Purchase>> by lazy {
        MutableStateFlow(listOf())
    }
    private val _itemPurchasesStateFlow: MutableStateFlow<List<Purchase>> by lazy {
        MutableStateFlow(listOf())
    }

    override fun getActiveSubscriptions(): Flow<List<Purchase>> {
        return _subPurchasesStateFlow
    }

    private fun getCombinedPurchasesFlow() =
        combine(_subPurchasesStateFlow, _itemPurchasesStateFlow) { subPurchases, itemPurchases ->
            CombinedPurchaseData(
                subPurchases = subPurchases,
                itemPurchases = itemPurchases
            )
        }

    override fun getStatusFlow(product: Product): Flow<ProductStatus> {
        return combine(getCombinedPurchasesFlow(), product.detailsFlow) { alles, skuDetailsList ->
            val skuList = ArrayList<String>()
            skuList.add(product.name)

            skuStatusResolver(
                product,
                skuDetailsList,
                alles.purchases
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

        val skuDetails: ProductDetails? = skuDetailsList.firstOrNull { it.productId == product.name }
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

    private val subHistoryObserver =
        PurchasesResponseListener { billingResult, purchaseHistoryRecordList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _subPurchasesHistoryStateFlow.value = purchaseHistoryRecordList
            }
        }

    private val inappHistoryObserver =
        PurchasesResponseListener { billingResult, purchaseHistoryRecordList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _itemPurchasesHistoryStateFlow.value = purchaseHistoryRecordList
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

            val queryPurchasesSubsParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val queryPurchasesInAppParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            billingClient.queryPurchasesAsync(
                queryPurchasesSubsParams,
                subHistoryObserver
            )
            billingClient.queryPurchasesAsync(
                queryPurchasesInAppParams,
                inappHistoryObserver
            )

            billingClient.queryPurchasesAsync(queryPurchasesSubsParams, subsPurchasesObserver)
            billingClient.queryPurchasesAsync(queryPurchasesInAppParams, inAppPurchasesObserver)
        }
    }

    private var isConnected = false
    override fun connected(connected: Boolean) {
        isConnected = true
    }
}
