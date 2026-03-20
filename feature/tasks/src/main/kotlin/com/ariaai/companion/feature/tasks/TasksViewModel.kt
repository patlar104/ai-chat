package com.ariaai.companion.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ariaai.companion.core.automation.reminder.ReminderScheduler
import com.ariaai.companion.core.domain.model.ReminderStatus
import com.ariaai.companion.core.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val pendingReminders = reminderRepository.observePending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun cancelReminder(id: Long) {
        viewModelScope.launch {
            reminderScheduler.cancel(id)
            reminderRepository.updateStatus(id, ReminderStatus.CANCELLED)
        }
    }
}
