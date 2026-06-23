package com.jewan.zipkr.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsTracker
    @Inject
    constructor(
        private val firebaseAnalytics: FirebaseAnalytics,
    ) : AnalyticsTracker {
        override fun trackSearchAddress(
            queryType: SearchQueryType,
            sidoSelected: Boolean,
        ) {
            val params =
                Bundle().apply {
                    putString(PARAM_QUERY_TYPE, queryType.analyticsValue)
                    putLong(PARAM_SIDO_SELECTED, if (sidoSelected) 1L else 0L)
                }
            firebaseAnalytics.logEvent(EVENT_SEARCH_ADDRESS, params)
        }

        override fun trackCopyPostalCode() {
            firebaseAnalytics.logEvent(EVENT_COPY_POSTAL_CODE, null)
        }

        private companion object {
            const val EVENT_SEARCH_ADDRESS = "search_address"
            const val EVENT_COPY_POSTAL_CODE = "copy_postal_code"
            const val PARAM_QUERY_TYPE = "query_type"
            const val PARAM_SIDO_SELECTED = "sido_selected"
        }
    }
