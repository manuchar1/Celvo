package com.mtislab.celvo.feature.store.data.repository


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mtislab.celvo.feature.store.domain.model.ClaimedPromo
import com.mtislab.celvo.feature.store.domain.repository.PromoClaimRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Persists claimed promo codes in DataStore Preferences as a single
 * JSON-serialized list under one key.
 *
 * Design rationale:
 * - One key (not key-per-banner) avoids key sprawl and makes `clearAll()` trivial.
 * - kotlinx.serialization avoids manual string parsing.
 * - Max realistic size: ~5-10 claimed promos × ~80 bytes = <1 KB.
 *   DataStore handles this efficiently.
 *
 * Thread safety: DataStore is inherently thread-safe via its
 * coroutine-based transaction model. No additional synchronization needed.
 */
class PromoClaimRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : PromoClaimRepository {

    private companion object {
        val KEY_CLAIMED_PROMOS = stringPreferencesKey("claimed_promos_json")

        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }



    @Serializable
    private data class ClaimedPromoEntry(
        val code: String,
        val bannerId: String,
        val claimedAtMillis: Long,
    )

    // ── Reads ──

    override fun observeClaims(): Flow<List<ClaimedPromo>> {
        return dataStore.data.map { prefs ->
            parseEntries(prefs).map { it.toDomain() }
        }
    }

    override fun observeClaimedBannerIds(): Flow<Set<String>> {
        return dataStore.data.map { prefs ->
            parseEntries(prefs).map { it.bannerId }.toSet()
        }
    }

    override suspend fun getActivePromoCode(): String? {
        val entries = parseEntries(dataStore.data.first())
        return entries.maxByOrNull { it.claimedAtMillis }?.code
    }

    // ── Writes ──

    override suspend fun claimPromo(bannerId: String, code: String) {
        dataStore.edit { prefs ->
            val current = parseEntries(prefs).toMutableList()

            current.removeAll { it.bannerId == bannerId }
            current.add(
                ClaimedPromoEntry(
                    code = code,
                    bannerId = bannerId,
                    claimedAtMillis = kotlin.time.Clock.System.now().toEpochMilliseconds(),

                    )
            )

            prefs[KEY_CLAIMED_PROMOS] = json.encodeToString(current)
        }
    }

    override suspend fun removeClaim(bannerId: String) {
        dataStore.edit { prefs ->
            val current = parseEntries(prefs).toMutableList()
            current.removeAll { it.bannerId == bannerId }

            if (current.isEmpty()) {
                prefs.remove(KEY_CLAIMED_PROMOS)
            } else {
                prefs[KEY_CLAIMED_PROMOS] = json.encodeToString(current)
            }
        }
    }

    override suspend fun clearAll() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_CLAIMED_PROMOS)
        }
    }

    // ── Internal helpers ──

    private fun parseEntries(prefs: Preferences): List<ClaimedPromoEntry> {
        val raw = prefs[KEY_CLAIMED_PROMOS] ?: return emptyList()
        return try {
            json.decodeFromString<List<ClaimedPromoEntry>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun ClaimedPromoEntry.toDomain() = ClaimedPromo(
        code = code,
        bannerId = bannerId,
        claimedAtMillis = claimedAtMillis,
    )
}