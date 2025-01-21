package com.example.datarecoverynew.views.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.android.billingclient.api.ProductDetails
import com.example.datarecoverynew.base.BaseActivity
import com.example.datarecoverynew.databinding.ActivityPremiumBinding
import com.example.datarecoverynew.utils.AppPreferences
import com.example.recoverydata.utils.collectLatestLifeCycleFlow
import com.subscription.ads.billing.SubscriptionViewModel
import com.subscription.ads.billing.SubscriptionsConstants


class PremiumActivity : BaseActivity() {
    lateinit var binding: ActivityPremiumBinding
    private var productDetail: ProductDetails? = null
    val TAG = PremiumActivity::class.java.simpleName

    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clickListener()
        observerViewModel()
    }

    private fun clickListener() {
        binding.cancel.setOnClickListener {
            finish()
        }
        binding.planTV.setOnClickListener {
            productDetail?.let {
                subscriptionViewModel.buy(
                    it,
                    null,
                    this,
                    it.subscriptionOfferDetails?.get(0)?.offerToken ?: ""
                )
            }
        }
    }

    private fun observerViewModel() {
        subscriptionViewModel.billingConnectionState.observe(this) {
            Log.d(TAG, "observerViewModel: billing client status $it")
//            showHideProgress(it)
        }

        collectLatestLifeCycleFlow(subscriptionViewModel.productsForSaleFlows) {
            Log.d(TAG, "observerViewModel: productsForSaleFlows $it")
            setUpSubscriptionPrice(it)
        }

        collectLatestLifeCycleFlow(subscriptionViewModel.currentPurchasesForPurchaseFlow) {
            Log.d(TAG, "observerViewModel: currentPurchase $it")

        }

        subscriptionViewModel.isUserSubscribed.observe(this) {
            AppPreferences.getInstance(this).setAppPurchased(it)
            SubscriptionsConstants.isUserSubscribed = it
        }
        collectLatestLifeCycleFlow(subscriptionViewModel.isNewPurchaseAcknowledged) {
            if (it) {
                startActivity(Intent(this@PremiumActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun setUpSubscriptionPrice(it: List<ProductDetails>) {
        var price = ""
        it.firstOrNull()?.let { item ->
            productDetail = item
            item.subscriptionOfferDetails?.get(0).let { sod ->
                price = sod?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice ?: ""
            }
        }
        binding.tvPrice.text = "$price/Month"

    }
}