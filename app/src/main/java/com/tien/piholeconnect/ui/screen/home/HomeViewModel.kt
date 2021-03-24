package com.tien.piholeconnect.ui.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.tien.piholeconnect.model.RefreshableViewModel
import com.tien.piholeconnect.repository.IPiHoleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val piHoleRepository: IPiHoleRepository) :
    RefreshableViewModel() {
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

    var queriesOverTime by mutableStateOf(mapOf<Int, Int>())
        private set
    var adsOverTime by mutableStateOf(mapOf<Int, Int>())
        private set

    override suspend fun queueRefresh() {
        joinAll(
            viewModelScope.launch {
                val summary = piHoleRepository.getStatusSummary()

                isAdsBlockingEnabled = summary.status == "enabled"
                totalQueries = summary.dnsQueriesToday
                totalBlockedQueries = summary.adsBlockedToday
                queryBlockingPercentage = summary.adsPercentageToday
                blockedDomainListCount = summary.domainsBeingBlocked
            },
            viewModelScope.launch {
                val overTimeData = piHoleRepository.getOverTimeData10Minutes()

                queriesOverTime = overTimeData.domainsOverTime
                adsOverTime = overTimeData.adsOverTime
            }
        )
    }
}