package com.mtislab.celvo.infrastructure.esim.esimgo.adapter

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.mtislab.celvo.domain.esim.models.*
import com.mtislab.celvo.domain.esim.ports.EsimProvider
import com.mtislab.celvo.infrastructure.esim.esimgo.client.EsimGoClient
import com.mtislab.celvo.infrastructure.esim.esimgo.persistence.EsimBundleEntity
import com.mtislab.celvo.infrastructure.esim.esimgo.persistence.EsimBundleRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@Service
open class EsimGoAdapter(
    private val apiClient: EsimGoClient,
    private val mapper: EsimGoMapper,
    private val repository: EsimBundleRepository,
    private val objectMapper: ObjectMapper
) : EsimProvider {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val memoryCache = AtomicReference<List<EsimBundle>>(emptyList())
    private val isSyncing = AtomicBoolean(false)



    @EventListener(ApplicationReadyEvent::class)
    fun onStartup() {
        logger.info("📢 Application started. Checking persistent cache in Supabase...")
        try {
            val dbData = repository.findAll()
            if (dbData.isNotEmpty()) {
                logger.info("✅ Loaded ${dbData.size} bundles from Supabase.")
                memoryCache.set(mapEntitiesToDomain(dbData))
            } else {
                logger.warn("⚠️ Cache empty! Triggering background sync...")
                CompletableFuture.runAsync { syncProcess() }
            }
        } catch (e: Exception) {
            logger.error("Error connecting to DB on startup", e)
            CompletableFuture.runAsync { syncProcess() }
        }
    }

    override fun getCatalogue(): List<EsimBundle> = memoryCache.get()

    @Scheduled(fixedRate = 3600000, initialDelay = 60000)
    fun scheduledSync() {
        logger.info("⏰ Scheduled sync triggered...")
        syncProcess()
    }

    private fun syncProcess() {
        if (isSyncing.getAndSet(true)) {
            logger.warn("⚠️ Sync already in progress. Skipping.")
            return
        }
        try {
            val apiBundles = fetchAllPagesFromApi()
            if (apiBundles.isNotEmpty()) {
                val uniqueBundles = apiBundles.distinctBy { it.id }
                logger.info("🔍 Found ${uniqueBundles.size} bundles. Updating RAM immediately...")
                memoryCache.set(uniqueBundles)

                val entities = mapDomainToEntities(uniqueBundles)
                saveToDatabaseTransactional(entities)
            }
        } catch (e: Exception) {
            logger.error("❌ Critical error during sync process", e)
        } finally {
            isSyncing.set(false)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected fun saveToDatabaseTransactional(entities: List<EsimBundleEntity>) {
        repository.deleteAllInBatch()
        repository.flush()

        entities.chunked(50).forEach { batch ->
            repository.saveAll(batch)
            repository.flush()
        }
        logger.info("✅ DB Sync Complete.")
    }

    private fun fetchAllPagesFromApi(): List<EsimBundle> {
        val allBundles = mutableListOf<EsimBundle>()
        var page = 1
        val perPage = 100
        var hasMore = true

        while (hasMore) {
            try {
                val response = apiClient.getCatalogue(page = page, perPage = perPage)
                val dtos = response.bundles ?: emptyList()

                if (dtos.isNotEmpty()) {
                    val domainBatch = dtos.map { mapper.toDomain(it) }
                    allBundles.addAll(domainBatch)
                    page++
                    if (page % 5 == 0) logger.info("... fetched page $page")
                }
                if (dtos.size < perPage || page > 100) hasMore = false
            } catch (e: Exception) {
                hasMore = false
            }
        }
        return allBundles
    }

    // --- MAPPERS (JSON Logic) ---

    private fun mapEntitiesToDomain(entities: List<EsimBundleEntity>): List<EsimBundle> {
        return entities.map { entity ->

            val networksMap: Map<String, List<NetworkInfo>> = try {
                if (entity.roamingDataJson.isNullOrBlank()) emptyMap()
                else objectMapper.readValue(entity.roamingDataJson, object : TypeReference<Map<String, List<NetworkInfo>>>() {})
            } catch (e: Exception) {
                emptyMap()
            }

            val countriesList = entity.supportedCountriesIso.split(",")
                .filter { it.isNotBlank() }
                .map { iso ->
                    val name = Locale("", iso).displayCountry.ifBlank { iso }

                    CountryInfo(
                        iso,
                        name,
                        "https://flagcdn.com/h240/${iso.lowercase()}.png",
                        networks = networksMap[iso] ?: emptyList()
                    )
                }

            val networks = if (entity.has5g) listOf("4G", "5G") else listOf("4G")

            EsimBundle(
                id = entity.id,
                name = entity.id,
                description = entity.description,
                dataAmount = DataAmount(entity.dataAmountMb, DataUnit.MB),
                validity = ValidityPeriod(entity.durationDays, TimeUnit.DAYS),
                price = Price(entity.price, entity.currency),
                coverage = Coverage(countriesList, emptyList()),
                imageUrl = entity.imageUrl,
                type = BundleType.NEW,
                category = BundleCategory.REGIONAL,
                networkTypes = networks
            )
        }
    }

    private fun mapDomainToEntities(bundles: List<EsimBundle>): List<EsimBundleEntity> {
        return bundles.map { bundle ->
            val isoString = bundle.coverage.countries.joinToString(",") { it.isoCode }
            val is5gIncluded = bundle.networkTypes.contains("5G")


            val roamingData = bundle.coverage.countries.associate { it.isoCode to it.networks }
            val jsonString = try {
                objectMapper.writeValueAsString(roamingData)
            } catch (e: Exception) {
                null
            }

            EsimBundleEntity(
                id = bundle.id,
                description = bundle.description,
                dataAmountMb = bundle.dataAmount.value,
                durationDays = bundle.validity.value,
                price = bundle.price.amount,
                currency = bundle.price.currency,
                imageUrl = bundle.imageUrl,
                supportedCountriesIso = isoString,
                has5g = is5gIncluded,
                roamingDataJson = jsonString
            )
        }
    }


}