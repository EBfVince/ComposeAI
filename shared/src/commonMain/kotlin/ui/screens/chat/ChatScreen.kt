package ui.screens.chat

import analytics.AdMobButton
import analytics.TrackScreenViewEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.ebfstudio.appgpt.common.MainRes
import di.getScreenModel
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import ui.components.TypewriterText
import ui.screens.chat.components.Messages

internal object ChatScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel: ChatScreenModel = getScreenModel { parametersOf(null as String?) }
        val currentChatUiState by screenModel.currentChatUiState.collectAsState()
        val screenUiState by screenModel.screenUiState.collectAsState()
        val chatsUiState by screenModel.chatsUiState.collectAsState()
        val localClipboardManager = LocalClipboardManager.current

        ChatScreen(
            onSend = screenModel::onSendMessage,
            onNewChat = screenModel::onNewChat,
            onRetry = screenModel::onRetrySendMessage,
            onChatSelected = screenModel::onChatSelected,
            onTextChange = screenModel::onTextChange,
            screenUiState = screenUiState,
            currentChatUiState = currentChatUiState,
            chatsUiState = chatsUiState,
            onClickShare = screenModel::onMessageShared,
            onRewardEarned = screenModel::onRewardEarned,
            onClickCopy = { text ->
                localClipboardManager.setText(AnnotatedString(text))
                screenModel.onMessageCopied()
            },
        )
    }

    @Composable
    private fun ChatScreen(
        onSend: () -> Unit,
        onNewChat: () -> Unit,
        onRetry: () -> Unit,
        onChatSelected: (String) -> Unit,
        onTextChange: (String) -> Unit,
        onClickCopy: (String) -> Unit,
        onClickShare: (String) -> Unit,
        onRewardEarned: (Int) -> Unit,
        screenUiState: ChatScreenUiState,
        currentChatUiState: ChatMessagesUiState,
        chatsUiState: ChatsUiState,
    ) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        BoxWithConstraints {
            val maxWidth = maxWidth
            if (maxWidth < 600.dp) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            ChatDrawerContent(
                                showCreateChatButton = false,
                                chatsUiState = chatsUiState,
                                currentChatUiState = currentChatUiState,
                                onNewChat = onNewChat,
                                onChatSelected = onChatSelected,
                                onCloseDrawer = { scope.launch { drawerState.close() } }
                            )
                        }
                    },
                ) {
                    ChatScreenContent(
                        showTopBarActions = true,
                        onClickCopy = onClickCopy,
                        onClickShare = onClickShare,
                        onTextChange = onTextChange,
                        onSend = onSend,
                        onRetry = onRetry,
                        onNewChat = onNewChat,
                        screenUiState = screenUiState,
                        currentChatUiState = currentChatUiState,
                        onRewardEarned = onRewardEarned,
                        onMenuClick = { scope.launch { drawerState.open() } },
                    )
                }
            } else {
                PermanentNavigationDrawer(
                    drawerContent = {
                        PermanentDrawerSheet(Modifier.width(maxWidth / 5 * 2)) {
                            ChatDrawerContent(
                                showCreateChatButton = true,
                                chatsUiState = chatsUiState,
                                currentChatUiState = currentChatUiState,
                                onNewChat = onNewChat,
                                onChatSelected = onChatSelected,
                                onCloseDrawer = { scope.launch { drawerState.close() } },
                            )
                        }
                    }
                ) {
                    Row {
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            modifier = Modifier.width(1.dp).fillMaxHeight()
                        )
                        ChatScreenContent(
                            showTopBarActions = false,
                            onClickCopy = onClickCopy,
                            onClickShare = onClickShare,
                            onTextChange = onTextChange,
                            onSend = onSend,
                            onRetry = onRetry,
                            onNewChat = onNewChat,
                            screenUiState = screenUiState,
                            currentChatUiState = currentChatUiState,
                            onRewardEarned = onRewardEarned,
                            onMenuClick = { scope.launch { drawerState.open() } },
                        )
                    }
                }
            }
        }

        TrackScreenViewEvent(screenName = "Chat")
    }

    @Composable
    private fun ChatScreenContent(
        screenUiState: ChatScreenUiState,
        currentChatUiState: ChatMessagesUiState,
        showTopBarActions: Boolean,
        onSend: () -> Unit,
        onRetry: () -> Unit,
        onNewChat: () -> Unit,
        onMenuClick: () -> Unit,
        onClickCopy: (String) -> Unit,
        onClickShare: (String) -> Unit,
        onTextChange: (String) -> Unit,
        onRewardEarned: (Int) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val focusRequester = remember { FocusRequester() }

        Scaffold(
            topBar = {
                ChatTopBar(
                    chatTitle = currentChatUiState.chatOrNull?.title,
                    coins = screenUiState.coins,
                    showTopBarActions = showTopBarActions,
                    onNewChat = {
                        onNewChat()
                        focusRequester.requestFocus()
                    },
                    onMenuClick = onMenuClick,
                    onRewardEarned = onRewardEarned,
                )
            },
            bottomBar = {
                ChatBottomBar(
                    text = screenUiState.text,
                    isLoading = screenUiState.isSending,
                    focusRequester = focusRequester,
                    onTextChange = onTextChange,
                    onSend = onSend,
                )
            },
            modifier = modifier,
        ) { contentPadding ->
            Column(modifier = Modifier.padding(contentPadding)) {
                when (currentChatUiState) {
                    ChatMessagesUiState.Empty,
                    ChatMessagesUiState.Loading -> Unit

                    is ChatMessagesUiState.Success -> {
                        Messages(
                            messages = currentChatUiState.messages,
                            onClickCopy = onClickCopy,
                            onClickShare = onClickShare,
                            onRetry = onRetry,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ChatDrawerContent(
        chatsUiState: ChatsUiState,
        currentChatUiState: ChatMessagesUiState,
        showCreateChatButton: Boolean,
        onChatSelected: (String) -> Unit,
        onCloseDrawer: () -> Unit,
        onNewChat: () -> Unit,
    ) {
        Spacer(Modifier.height(12.dp))
        if (showCreateChatButton) {
            ExtendedFloatingActionButton(
                onClick = onNewChat,
                text = { Text(text = MainRes.string.chat_button_new) },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null
                    )
                },
            )
            Spacer(Modifier.height(12.dp))
        }
        when (chatsUiState) {
            ChatsUiState.Loading -> Unit
            is ChatsUiState.Success -> {
                chatsUiState.chats.forEach { chat ->
                    val isSelected = chat.id == currentChatUiState.chatOrNull?.id
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = chat.title ?: MainRes.string.chat_title_empty,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false
                            )
                        },
                        icon = {
                            Icon(
                                if (isSelected) Icons.Rounded.ChatBubble else Icons.Rounded.ChatBubbleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            onChatSelected(chat.id)
                            onCloseDrawer()
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    }

    @Composable
    private fun ChatTopBar(
        chatTitle: String?,
        showTopBarActions: Boolean,
        coins: Int?,
        onNewChat: () -> Unit,
        onMenuClick: () -> Unit,
        onRewardEarned: (Int) -> Unit,
    ) {
        CenterAlignedTopAppBar(
            title = {
                TypewriterText(
                    text = chatTitle ?: MainRes.string.app_name,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            },
            navigationIcon = {
                if (showTopBarActions.not()) return@CenterAlignedTopAppBar
                IconButton(
                    onClick = onMenuClick,
                ) {
                    Icon(
                        Icons.Rounded.Forum,
                        contentDescription = null,
                    )
                }
            },
            actions = {
                // Rewards button
                if (coins != null) {
                    AdMobButton(
                        coins = coins,
                        onRewardEarned = onRewardEarned,
                    )
                }

                // New chat button
                if (showTopBarActions) {
                    IconButton(
                        onClick = onNewChat,
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = null,
                        )
                    }
                }
            }
        )
    }

    @Composable
    private fun ChatBottomBar(
        text: String,
        isLoading: Boolean,
        focusRequester: FocusRequester,
        onTextChange: (String) -> Unit,
        onSend: () -> Unit,
    ) {
        val enableSend = text.isNotBlank() && !isLoading
        val transition = updateTransition(targetState = enableSend)
        val sendContainerColor by transition.animateColor { state ->
            when (state) {
                true -> MaterialTheme.colorScheme.primary
                false -> Color.Transparent
            }
        }
        val sendContentColor by transition.animateColor { state ->
            when (state) {
                true -> MaterialTheme.colorScheme.onPrimary
                false -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        }
        val sendIconRotation by transition.animateFloat { state ->
            when (state) {
                true -> -45f
                false -> -0f
            }
        }

        Surface {
            Column {
                AnimatedVisibility(
                    visible = isLoading,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                    )
                }
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.weight(1f),
                        ) {
                            BasicTextField(
                                value = text,
                                onValueChange = onTextChange,
                                maxLines = 3,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                                decorationBox = { innerTextField ->
                                    if (text.isBlank()) {
                                        Text(
                                            text = MainRes.string.chat_textfield_hint,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.alpha(0.6f)
                                        )
                                    }
                                    innerTextField()
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    capitalization = KeyboardCapitalization.Sentences,
                                ),
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .fillMaxWidth(1f)
                                    .focusRequester(focusRequester)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = onSend,
                        enabled = enableSend,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = sendContainerColor,
                            disabledContainerColor = sendContainerColor,
                            contentColor = sendContentColor,
                            disabledContentColor = sendContentColor,
                        )
                    ) {
                        Icon(
                            Icons.Rounded.Send,
                            contentDescription = null,
                            modifier = Modifier.rotate(sendIconRotation)
                        )
                    }
                }
            }
        }
    }
}

