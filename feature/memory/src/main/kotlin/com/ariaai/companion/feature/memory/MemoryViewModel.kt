package com.ariaai.companion.feature.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ariaai.companion.core.domain.model.MemoryEntry
import com.ariaai.companion.core.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository,
) : ViewModel() {

    val memoryItems: StateFlow<List<MemoryEntry>> = memoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addMemory(title: String, detail: String) {
        if (title.isBlank() && detail.isBlank()) return
        viewModelScope.launch {
            memoryRepository.insert(
                MemoryEntry(
                    title = title.ifBlank { "Untitled Memory" },
                    detail = detail.ifBlank { "No details provided." },
                    createdAtMs = System.currentTimeMillis(),
                ),
            )
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            memoryRepository.delete(id)
        }
    }
}
