package com.jewan.zipkr.analytics

interface AnalyticsTracker {
    fun trackSearchAddress(
        queryType: SearchQueryType,
        sidoSelected: Boolean,
    )

    fun trackCopyPostalCode()
}

enum class SearchQueryType(
    val analyticsValue: String,
) {
    Korean("korean"),
    English("english"),
    PostalCode("postal_code"),
}
