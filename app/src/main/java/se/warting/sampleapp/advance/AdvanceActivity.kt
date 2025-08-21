package se.warting.sampleapp.advance

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.ProductDetails
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.runBlocking
import se.warting.billy.flow.ProductStatus
import se.warting.sampleapp.R

class AdvanceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.advance_activity)

        val locationText = findViewById<TextView>(R.id.locationText)
        val actionButton = findViewById<FloatingActionButton>(R.id.locationFab)

        var buyableProduct: ProductStatus.Available? = null

        actionButton.setOnClickListener {
            val offer: ProductDetails.SubscriptionOfferDetails? = runBlocking {
                buyableProduct?.productDetails?.subscriptionOfferDetails?.firstOrNull()
            }

            buyableProduct?.buy(offer!!)
        }

        // Update view based on ViewModel output
        viewModel.getViewData().observe(this) { viewData ->

            buyableProduct = when (val s = viewData.earlyBirdStatus) {
                is ProductStatus.Available -> s
                is ProductStatus.Loading -> null
                is ProductStatus.Owned -> null
                is ProductStatus.Unavailable -> null
            }
            locationText.text =
                getString(
                    R.string.products, when (val status = viewData.earlyBirdStatus) {
                        is ProductStatus.Available -> {
                            val priceText = status.formattedPrice?.let { " - $it" } ?: ""
                            "Available$priceText"
                        }
                        is ProductStatus.Loading -> "Loading"
                        is ProductStatus.Owned -> "Owned"
                        is ProductStatus.Unavailable -> "Unavailable"
                    }
                )
        }
    }

    private val viewModel: AdvanceViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdvanceViewModel() as T
            }
        }
    }
}
