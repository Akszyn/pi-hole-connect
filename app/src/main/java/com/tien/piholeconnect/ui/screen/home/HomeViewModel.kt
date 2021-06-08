package com.tien.piholeconnect.ui.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tien.piholeconnect.model.PiHoleConnectionAwareViewModel
import com.tien.piholeconnect.repository.PiHoleRepository
import com.tien.piholeconnect.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val piHoleRepository: PiHoleRepository,
    userPreferencesRepository: UserPreferencesRepository
) : PiHoleConnectionAwareViewModel(userPreferencesRepository) {
    var isPiHoleSwitchLoading by mutableStateOf(false)
        private set
    var isAdsBlockingEnabled by mutableStateOf(true)
        private set
    var totalQueries by mutableStateOf(0)
        private set
    var totalBlockedQueries by mutableStateOf(0)
        private set
    var queryBlockingPercentage by mutableStateOf(.0)
        private set
    var blockedDomainListCount by mutableStateOf(0)
        private set
    var uniqueClients by mutableStateOf(0)

    var queriesOverTime by mutableStateOf(mapOf<Int, Int>())
        private set
    var adsOverTime by mutableStateOf(mapOf<Int, Int>())
        private set

    override suspend fun queueRefresh() = coroutineScope {
        val deferredSummary = async { piHoleRepository.getStatusSummary() }
        val deferredOverTimeData = async { piHoleRepository.getOverTimeData10Minutes() }

        awaitAll(deferredSummary, deferredOverTimeData)

        deferredSummary.await().let { summary ->
            isAdsBlockingEnabled = summary.status == "enabled"
            totalQueries = summary.dnsQueriesToday
            totalBlockedQueries = summary.adsBlockedToday
            queryBlockingPercentage = summary.adsPercentageToday
            blockedDomainListCount = summary.domainsBeingBlocked
            uniqueClients = summary.uniqueClients
        }
        deferredOverTimeData.await().let { overTimeData ->
            queriesOverTime = overTimeData.domainsOverTime
            adsOverTime = overTimeData.adsOverTime
        }
    }

    suspend fun disable(duration: Duration) {
        runCatching {
            isPiHoleSwitchLoading = true
            piHoleRepository.disable(duration)
            refresh()
        }.onFailure { error = it }
        isPiHoleSwitchLoading = false
    }

    suspend fun enable() {
        runCatching {
            isPiHoleSwitchLoading = true
            piHoleRepository.enable()
            refresh()
        }.onFailure { error = it }
        isPiHoleSwitchLoading = false
    }
}