package com.example.aicompanion.feature.homecontrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aicompanion.core.domain.model.Alias
import com.example.aicompanion.core.domain.model.AuditLogEntry
import com.example.aicompanion.core.domain.repository.AliasRepository
import com.example.aicompanion.core.domain.repository.AuditLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeControlViewModel @Inject constructor(
    private val auditLogRepository: AuditLogRepository,
    private val aliasRepository: AliasRepository,
) : ViewModel() {

    val auditLog: StateFlow<List<AuditLogEntry>> = auditLogRepository.observeRecent(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val aliases: StateFlow<List<Alias>> = aliasRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addAlias(alias: String, entityId: String, domain: String) {
        viewModelScope.launch {
            aliasRepository.insert(Alias(alias = alias, entityId = entityId, domain = domain))
        }
    }

    fun updateAlias(alias: Alias) {
        viewModelScope.launch {
            aliasRepository.update(alias)
        }
    }

    fun deleteAlias(id: Long) {
        viewModelScope.launch {
            aliasRepository.delete(id)
        }
    }
}
