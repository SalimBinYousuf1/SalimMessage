package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.core.telephony.TelephonyManagerHelper
import com.example.data.repository.SmsRepository
import com.example.ui.MainViewModel
import com.example.ui.ScreenState
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.ContactInfoScreen
import com.example.ui.screens.ConversationListScreen
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.NewMessageScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SpamBlockedScreen
import com.example.ui.theme.SalimTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var telephonyHelper: TelephonyManagerHelper

    private val defaultSmsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshStatus()
    }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        telephonyHelper = TelephonyManagerHelper(this)

        checkAndRequestPermissions()
        handleIncomingIntent(intent)

        setContent {
            val themePreference by viewModel.theme.collectAsState()
            val isDark = when (themePreference) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            SalimTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SalimAppNavigation(
                        viewModel = viewModel,
                        onRequestDefaultSms = {
                            val intent = telephonyHelper.createDefaultSmsIntent()
                            defaultSmsLauncher.launch(intent)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshStatus()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val ungranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (ungranted.isNotEmpty()) {
            permissionsLauncher.launch(ungranted.toTypedArray())
        }
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        // Direct notification open
        val threadId = intent.getLongExtra("thread_id", -1L)
        val address = intent.getStringExtra("address")
        if (threadId != -1L && address != null) {
            viewModel.selectOrCreateConversation(address, null)
            return
        }

        // SMS / MMS sendto intent
        if (Intent.ACTION_SENDTO == intent.action || Intent.ACTION_SEND == intent.action) {
            val uri: Uri? = intent.data
            val schemeRecipient = uri?.schemeSpecificPart
            val extraAddress = intent.getStringExtra("address")
            val targetRecipient = schemeRecipient ?: extraAddress

            if (!targetRecipient.isNullOrBlank()) {
                viewModel.selectOrCreateConversation(targetRecipient, null)
            } else {
                viewModel.openNewMessage()
            }
        }
    }
}

@Composable
fun SalimAppNavigation(
    viewModel: MainViewModel,
    onRequestDefaultSms: () -> Unit
) {
    val screenState by viewModel.currentScreen.collectAsState()
    val bottomDestination by viewModel.currentBottomDestination.collectAsState()
    val isDefault by viewModel.isDefaultSmsApp.collectAsState()
    val activeSims by viewModel.activeSims.collectAsState()

    val conversations by viewModel.conversations.collectAsState()
    val activeConversation by viewModel.activeConversation.collectAsState()
    val activeMessages by viewModel.activeMessages.collectAsState()
    val contacts by viewModel.contacts.collectAsState()

    val defaultSimId by viewModel.defaultSimId.collectAsState()
    val askSim by viewModel.askSimPerMessage.collectAsState()
    val deliveryReports by viewModel.deliveryReports.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val enterIsSend by viewModel.enterIsSend.collectAsState()
    val appLockEnabled by viewModel.appLockEnabled.collectAsState()
    val hideInRecents by viewModel.hideInRecents.collectAsState()

    val archivedConversations by viewModel.archivedConversations.collectAsState()
    val favouriteConversations by viewModel.favouriteConversations.collectAsState()
    val spamConversations by viewModel.spamConversations.collectAsState()
    val blockedNumbers by viewModel.blockedNumbers.collectAsState()

    when (screenState) {
        ScreenState.CONVERSATION_LIST -> {
            ConversationListScreen(
                conversations = conversations,
                isDefaultSmsApp = isDefault,
                currentDestination = bottomDestination,
                onNavigateDestination = { viewModel.navigateBottom(it) },
                onConversationClick = { viewModel.openConversation(it) },
                onComposeClick = { viewModel.openNewMessage() },
                onCameraClick = { viewModel.openNewMessage() },
                onRequestDefaultSms = onRequestDefaultSms,
                onPinConversation = { id, pin -> viewModel.pinConversation(id, pin) },
                onArchiveConversation = { id, archive -> viewModel.archiveConversation(id, archive) },
                onDeleteConversation = { id -> viewModel.deleteConversation(id) },
                onMarkAsRead = { id -> viewModel.markAsRead(id) },
                onFavouriteConversation = { id, fav -> viewModel.favouriteConversation(id, fav) }
            )
        }

        ScreenState.CONVERSATION -> {
            if (activeConversation != null) {
                ConversationScreen(
                    conversation = activeConversation!!,
                    messages = activeMessages,
                    activeSims = activeSims,
                    onBackClick = { viewModel.navigateBack() },
                    onTitleClick = { viewModel.openContactInfo() },
                    onSendMessage = { text, subId, attach -> viewModel.sendMessage(text, subId, attach) },
                    onRetryMessage = { viewModel.retryMessage(it) },
                    onDeleteMessage = { viewModel.deleteMessage(it) },
                    onStarMessage = { id, star -> viewModel.starMessage(id, star) },
                    onScheduleClick = { /* Schedule dialog */ }
                )
            } else {
                viewModel.navigateBack()
            }
        }

        ScreenState.NEW_MESSAGE -> {
            NewMessageScreen(
                contacts = contacts,
                onBackClick = { viewModel.navigateBack() },
                onContactSelect = { number, name ->
                    viewModel.selectOrCreateConversation(number, name)
                }
            )
        }

        ScreenState.SETTINGS -> {
            SettingsScreen(
                isDefaultSmsApp = isDefault,
                onRequestDefaultSms = onRequestDefaultSms,
                activeSims = activeSims,
                defaultSimId = defaultSimId,
                askSimPerMessage = askSim,
                deliveryReports = deliveryReports,
                notificationsEnabled = notificationsEnabled,
                otpCopyEnabled = true,
                theme = theme,
                enterIsSend = enterIsSend,
                appLockEnabled = appLockEnabled,
                hideInRecents = hideInRecents,
                onUpdateDefaultSim = { viewModel.updateDefaultSim(it) },
                onUpdateAskSim = { viewModel.updateAskSim(it) },
                onUpdateDeliveryReports = { viewModel.updateDeliveryReports(it) },
                onUpdateNotifications = { viewModel.updateNotifications(it) },
                onUpdateTheme = { viewModel.updateTheme(it) },
                onUpdateEnterIsSend = { viewModel.updateEnterIsSend(it) },
                onUpdateAppLock = { viewModel.updateAppLock(it) },
                onUpdateHideInRecents = { viewModel.updateHideInRecents(it) },
                onOpenBlocked = { viewModel.openSpamBlocked() },
                onOpenSpam = { viewModel.openSpamBlocked() },
                onBackClick = { viewModel.navigateBack() }
            )
        }

        ScreenState.FAVOURITES -> {
            FavoritesScreen(
                conversations = favouriteConversations,
                onConversationClick = { viewModel.openConversation(it) },
                onBackClick = { viewModel.navigateBack() }
            )
        }

        ScreenState.ARCHIVE -> {
            ArchiveScreen(
                conversations = archivedConversations,
                onConversationClick = { viewModel.openConversation(it) },
                onBackClick = { viewModel.navigateBack() }
            )
        }

        ScreenState.SPAM_BLOCKED -> {
            SpamBlockedScreen(
                spamConversations = spamConversations,
                blockedNumbers = blockedNumbers,
                onReportNotSpam = { viewModel.reportNotSpam(it) },
                onUnblockNumber = { viewModel.unblockNumber(it) },
                onBackClick = { viewModel.navigateBack() }
            )
        }

        ScreenState.CONTACT_INFO -> {
            if (activeConversation != null) {
                ContactInfoScreen(
                    conversation = activeConversation!!,
                    onBackClick = { viewModel.navigateBack() },
                    onTogglePin = { viewModel.pinConversation(activeConversation!!.threadId, it) },
                    onToggleMute = { viewModel.muteConversation(activeConversation!!.threadId, it) },
                    onToggleFavourite = { viewModel.favouriteConversation(activeConversation!!.threadId, it) },
                    onBlockContact = { viewModel.blockContact(it) },
                    onDeleteConversation = { viewModel.deleteConversation(activeConversation!!.threadId) }
                )
            } else {
                viewModel.navigateBack()
            }
        }
    }
}
