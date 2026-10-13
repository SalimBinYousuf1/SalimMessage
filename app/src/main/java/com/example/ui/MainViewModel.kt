package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.model.BlockedNumber
import com.example.core.model.Contact
import com.example.core.model.Conversation
import com.example.core.model.Message
import com.example.core.model.SimInfo
import com.example.core.preferences.PreferencesManager
import com.example.data.repository.SmsRepository
import com.example.ui.components.BottomBarDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenState {
    CONVERSATION_LIST,
    CONVERSATION,
    NEW_MESSAGE,
    SETTINGS,
    FAVOURITES,
    ARCHIVE,
    SPAM_BLOCKED,
    CONTACT_INFO,
    SCHEDULED
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmsRepository.getInstance(application)
    private val prefs = PreferencesManager(application)

    val currentScreen = MutableStateFlow(ScreenState.CONVERSATION_LIST)
    val currentBottomDestination = MutableStateFlow(BottomBarDestination.MESSAGE)
    val activeConversation = MutableStateFlow<Conversation?>(null)

    val activeMessages = MutableStateFlow<List<Message>>(emptyList())

    val conversations: StateFlow<List<Conversation>> = repository.conversationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedConversations: StateFlow<List<Conversation>> = repository.archivedConversationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spamConversations: StateFlow<List<Conversation>> = repository.spamConversationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favouriteConversations: StateFlow<List<Conversation>> = repository.favouriteConversationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedNumbers: StateFlow<List<BlockedNumber>> = repository.blockedNumbersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<Contact>> = repository.contactsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val defaultSimId = prefs.defaultSimFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)
    val askSimPerMessage = prefs.askSimFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val deliveryReports = prefs.deliveryReportsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val notificationsEnabled = prefs.notificationsEnabledFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val theme = prefs.themeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "LIGHT")
    val enterIsSend = prefs.enterIsSendFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val appLockEnabled = prefs.appLockEnabledFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val hideInRecents = prefs.hideInRecentsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val activeSims = MutableStateFlow<List<SimInfo>>(emptyList())
    val isDefaultSmsApp = MutableStateFlow(false)

    init {
        refreshStatus()
    }

    private var messagesCollectionJob: kotlinx.coroutines.Job? = null

    fun refreshStatus() {
        isDefaultSmsApp.value = repository.isDefaultSmsApp()
        activeSims.value = repository.getActiveSimCards()
    }

    val scheduledMessages: StateFlow<List<com.example.core.model.ScheduledMessage>> = repository.scheduledMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun openConversation(conversation: Conversation) {
        activeConversation.value = conversation
        currentScreen.value = ScreenState.CONVERSATION
        messagesCollectionJob?.cancel()
        messagesCollectionJob = viewModelScope.launch {
            repository.markThreadAsRead(conversation.threadId)
            repository.getMessagesForThread(conversation.threadId).collect {
                activeMessages.value = it
            }
        }
    }

    fun openNewMessage() {
        currentScreen.value = ScreenState.NEW_MESSAGE
    }

    fun openContactInfo() {
        currentScreen.value = ScreenState.CONTACT_INFO
    }

    fun openSpamBlocked() {
        currentScreen.value = ScreenState.SPAM_BLOCKED
    }

    fun openScheduledMessages() {
        currentScreen.value = ScreenState.SCHEDULED
    }

    fun navigateBottom(destination: BottomBarDestination) {
        currentBottomDestination.value = destination
        when (destination) {
            BottomBarDestination.MESSAGE -> currentScreen.value = ScreenState.CONVERSATION_LIST
            BottomBarDestination.SCHEDULED -> currentScreen.value = ScreenState.SCHEDULED
            BottomBarDestination.CONTACTS -> currentScreen.value = ScreenState.NEW_MESSAGE
            BottomBarDestination.FAVOURITE -> currentScreen.value = ScreenState.FAVOURITES
            BottomBarDestination.SETTINGS -> currentScreen.value = ScreenState.SETTINGS
        }
    }

    fun navigateBack() {
        when (currentScreen.value) {
            ScreenState.CONVERSATION_LIST -> {
                // system default
            }
            ScreenState.CONTACT_INFO -> {
                currentScreen.value = ScreenState.CONVERSATION
            }
            ScreenState.SPAM_BLOCKED -> {
                currentScreen.value = ScreenState.SETTINGS
            }
            else -> {
                currentScreen.value = ScreenState.CONVERSATION_LIST
                currentBottomDestination.value = BottomBarDestination.MESSAGE
            }
        }
    }

    fun sendMessage(body: String, subId: Int, attachmentUri: String?) {
        val conv = activeConversation.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                recipient = conv.address,
                body = body,
                subId = subId,
                attachmentUri = attachmentUri,
                targetThreadId = conv.threadId
            )
        }
    }

    fun scheduleMessage(recipient: String, body: String, scheduledTime: Long, subId: Int = -1) {
        viewModelScope.launch {
            repository.scheduleMessage(recipient, body, scheduledTime, subId)
        }
    }

    fun deleteScheduledMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteScheduledMessage(id)
        }
    }

    fun retryMessage(message: Message) {
        viewModelScope.launch {
            repository.sendMessage(
                recipient = message.address,
                body = message.body,
                subId = message.subscriptionId,
                attachmentUri = message.attachmentUri
            )
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun starMessage(id: Long, star: Boolean) {
        viewModelScope.launch {
            repository.starMessage(id, star)
        }
    }

    fun pinConversation(threadId: Long, pin: Boolean) {
        viewModelScope.launch {
            repository.pinConversation(threadId, pin)
        }
    }

    fun archiveConversation(threadId: Long, archive: Boolean) {
        viewModelScope.launch {
            repository.archiveConversation(threadId, archive)
            if (activeConversation.value?.threadId == threadId) {
                navigateBack()
            }
        }
    }

    fun muteConversation(threadId: Long, mute: Boolean) {
        viewModelScope.launch {
            repository.muteConversation(threadId, mute)
        }
    }

    fun favouriteConversation(threadId: Long, fav: Boolean) {
        viewModelScope.launch {
            repository.favouriteConversation(threadId, fav)
        }
    }

    fun deleteConversation(threadId: Long) {
        viewModelScope.launch {
            repository.deleteConversation(threadId)
            if (activeConversation.value?.threadId == threadId) {
                navigateBack()
            }
        }
    }

    fun reportNotSpam(threadId: Long) {
        viewModelScope.launch {
            repository.reportNotSpam(threadId)
        }
    }

    fun blockContact(address: String) {
        viewModelScope.launch {
            repository.blockNumber(address)
            navigateBack()
        }
    }

    fun unblockNumber(number: String) {
        viewModelScope.launch {
            repository.unblockNumber(number)
        }
    }

    fun markAsRead(threadId: Long) {
        viewModelScope.launch {
            repository.markThreadAsRead(threadId)
        }
    }

    fun selectOrCreateConversation(address: String, name: String?) {
        val norm = com.example.core.util.PhoneNumberHelper.normalize(address)
        val existing = conversations.value.find { 
            it.address == address || (norm.isNotBlank() && com.example.core.util.PhoneNumberHelper.normalize(it.address) == norm)
        }
        if (existing != null) {
            openConversation(existing)
        } else {
            val newConv = Conversation(
                threadId = System.currentTimeMillis(),
                address = address,
                contactName = name
            )
            openConversation(newConv)
        }
    }

    fun updateDefaultSim(simId: Int) = viewModelScope.launch { prefs.setDefaultSim(simId) }
    fun updateAskSim(ask: Boolean) = viewModelScope.launch { prefs.setAskSim(ask) }
    fun updateDeliveryReports(enabled: Boolean) = viewModelScope.launch { prefs.setDeliveryReports(enabled) }
    fun updateNotifications(enabled: Boolean) = viewModelScope.launch { prefs.setNotificationsEnabled(enabled) }
    fun updateTheme(themeStr: String) = viewModelScope.launch { prefs.setTheme(themeStr) }
    fun updateEnterIsSend(enabled: Boolean) = viewModelScope.launch { prefs.setEnterIsSend(enabled) }
    fun updateAppLock(enabled: Boolean) = viewModelScope.launch { prefs.setAppLockEnabled(enabled) }
    fun updateHideInRecents(enabled: Boolean) = viewModelScope.launch { prefs.setHideInRecents(enabled) }
}
