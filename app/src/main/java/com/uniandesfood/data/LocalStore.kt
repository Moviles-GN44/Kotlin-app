package com.uniandesfood.data.local

/**
 * Small key-value data stored in SharedPreferences.
 * It always uses apply() (asynchronous) and never commit(), so the main thread is not blocked.
 */
object LocalStore {

    private const val KEY_FAVORITES = "favorites"
    private const val KEY_DRAFT_RATING = "review_draft_rating_"
    private const val KEY_DRAFT_COMMENT = "review_draft_comment_"

    data class ReviewDraft(val rating: Int, val comment: String)

    // Returns null when nothing was saved yet (first time the app opens)
    fun getFavorites(): Set<String>? =
        AppStorage.prefs?.getStringSet(KEY_FAVORITES, null)?.toSet()

    fun saveFavorites(favorites: Set<String>) {
        AppStorage.prefs?.edit()?.putStringSet(KEY_FAVORITES, HashSet(favorites))?.apply()
    }

    fun getReviewDraft(restaurantId: String): ReviewDraft? {
        val prefs = AppStorage.prefs ?: return null
        val rating = prefs.getInt(KEY_DRAFT_RATING + restaurantId, 0)
        val comment = prefs.getString(KEY_DRAFT_COMMENT + restaurantId, "") ?: ""
        return if (rating == 0 && comment.isEmpty()) null else ReviewDraft(rating, comment)
    }

    fun saveReviewDraft(restaurantId: String, rating: Int, comment: String) {
        AppStorage.prefs?.edit()
            ?.putInt(KEY_DRAFT_RATING + restaurantId, rating)
            ?.putString(KEY_DRAFT_COMMENT + restaurantId, comment)
            ?.apply()
    }

    fun clearReviewDraft(restaurantId: String) {
        AppStorage.prefs?.edit()
            ?.remove(KEY_DRAFT_RATING + restaurantId)
            ?.remove(KEY_DRAFT_COMMENT + restaurantId)
            ?.apply()
    }
}