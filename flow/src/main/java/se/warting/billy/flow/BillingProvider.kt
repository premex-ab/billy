package se.warting.billy.flow

import android.app.Application
import android.content.Context
import android.os.Handler
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import se.warting.billy.flow.internal.AndroidActivityProvider
import se.warting.billy.flow.internal.AndroidPurchaseLauncher
import se.warting.billy.flow.internal.AndroidPurchasesObserver
import se.warting.billy.flow.internal.AndroidPurchasesUpdatedListener

/**
 * Used to hold the reference to check and observe application billing status.
 *
 * @see BillingClient
 * @see PurchaseLauncher
 * @see PurchaseObserver
 */
public class BillingProvider constructor(
    public val billingClient: BillingClient,
    private val purchaseLauncher: PurchaseLauncher,
    public val observer: PurchaseObserver,
) : PurchaseObserver by observer {

    public fun buy(billingFlowParams: BillingFlowParams): BillingResult? {
        return purchaseLauncher.buy(billingFlowParams)
    }

    public companion object {

        private var _instance: BillingProvider? = null

        @JvmStatic
        public val instance: BillingProvider
            get() = checkNotNull(_instance) {
                "BillingProvider was not initialized. If you have disabled auto-init ensure you" +
                        " are calling init(context) before using it."
            }

        /**
         * When BillingProvider self-initialization is disabled, BillingProvider.init(context)
         * must be called to manually initialize the BillingProvider instance. Depending on
         * implementation if init() is called more than once (multiple times in app OR in app + test),
         * an IllegalStateException is thrown.
         *
         * Provide an easy way to check if BillingProvider instance is already
         * initialized, before calling BillingProvider.init(context).
         */
        public fun isInitialized(): Boolean = _instance != null

        /**
         * Init method that allows to provide custom parameters for testing purposes.
         */
        public fun init(
            context: Context
        ): BillingProvider {

            val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
                .enablePrepaidPlans()
                .enableOneTimeProducts()
                .build()

            val billingClient = BillingClient.newBuilder(context)
                .setListener(AndroidPurchasesUpdatedListener())
                .enablePendingPurchases(pendingPurchasesParams)
                .build()

            val purchaseObserver = AndroidPurchasesObserver(billingClient).also {
                // Wire to lifecycle ensuring its done in the main thread
                Handler(context.mainLooper).post {
                    ProcessLifecycleOwner.get().lifecycle.addObserver(it)
                }
            }

            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        purchaseObserver.connected(true)
                        purchaseObserver.refreshStatus()
                    }
                }

                override fun onBillingServiceDisconnected() {
                    purchaseObserver.connected(false)
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })

            val billingLauncher = AndroidPurchaseLauncher(
                billingClient,
                AndroidActivityProvider().also {
                    (context.applicationContext as Application).registerActivityLifecycleCallbacks(
                        it
                    )
                })

            _instance =
                BillingProvider(billingClient, billingLauncher, purchaseObserver)

            return _instance!!
        }
    }
}
