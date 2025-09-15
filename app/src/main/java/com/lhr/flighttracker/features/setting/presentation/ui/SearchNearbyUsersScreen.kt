package com.lhr.flighttracker.features.setting.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.lhr.flighttracker.R
import com.lhr.flighttracker.core.dialog.DialogManager
import com.lhr.flighttracker.core.dialog.DialogState
import com.lhr.flighttracker.core.dialog.ui.GeneralDialogContent
import com.lhr.flighttracker.core.ui.BaseScreen
import com.lhr.flighttracker.core.toast.ToastManager
import com.lhr.flighttracker.features.main.presentation.widget.MainTitleBar
import com.lhr.flighttracker.features.setting.domain.entity.FriendshipStatus
import com.lhr.flighttracker.features.setting.domain.entity.UserProfile
import com.lhr.flighttracker.features.setting.presentation.viewmodel.SearchNearbyUsersViewModel
import com.lhr.flighttracker.features.setting.presentation.widget.dialog.UserProfileDialogContent

@Composable
fun SearchNearbyUsersScreen(
    navController: NavController,
    viewModel: SearchNearbyUsersViewModel = hiltViewModel()
) {
    val isSearching by viewModel.isSearching.collectAsState()
    val foundUsers by viewModel.foundUsers.collectAsState()
    val context = LocalContext.current

    // 當畫面首次進入時，自動開始搜尋
    LaunchedEffect(Unit) {
        viewModel.startSearching()
    }

    BaseScreen(
        content = {
            Column(Modifier.fillMaxSize()) {
                MainTitleBar(
                    title = stringResource(id = R.string.share_with_nearby_users),
                    onBackPress = { navController.popBackStack() }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSearching) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(16.dp))
                            Text(text = stringResource(id = R.string.searching_nearby_users))
                        }
                    } else {
                        if (foundUsers.isEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Spacer(Modifier.height(16.dp))
                                Text(text = stringResource(id = R.string.no_users_found))
                                Spacer(Modifier.height(24.dp))
                                Button(onClick = { viewModel.startSearching() }) {
                                    Text(text = stringResource(id = R.string.research))
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(foundUsers, key = { it.id }) { user ->
                                    UserListItem(
                                        user = user,
                                        onItemClick = {
                                            DialogManager.showDialog(
                                                DialogState(
                                                    content = {
                                                        UserProfileDialogContent(
                                                            user = user,
                                                            onDismissRequest = { DialogManager.dismissDialog() },
                                                            onAddFriendClick = { userToAdd ->
                                                                viewModel.addFriend(userToAdd.id)
                                                                ToastManager.showToast(context.getString(R.string.friend_request_sent, userToAdd.name))
                                                                DialogManager.dismissDialog()
                                                            }
                                                        )
                                                    }
                                                )
                                            )
                                        },
                                        onAddFriend = {
                                            // 列表右側按鈕的邀請邏輯
                                            viewModel.addFriend(user.id)
                                            ToastManager.showToast(
                                                context.getString(
                                                    R.string.friend_request_sent,
                                                    user.name
                                                )
                                            )
                                        },
                                        onCancelFriendRequest = {
                                            // 取消邀請的邏輯
                                            DialogManager.showDialog(
                                                DialogState(
                                                    content = {
                                                        GeneralDialogContent(
                                                            title = stringResource(id = R.string.cancel_friend_request_title),
                                                            confirmButtonText = stringResource(id = R.string.confirm_cancel),
                                                            onConfirmClick = {
                                                                viewModel.cancelFriendRequest(user.id)
                                                                ToastManager.showToast(
                                                                    context.getString(R.string.friend_request_cancelled)
                                                                )
                                                                DialogManager.dismissDialog()
                                                            },
                                                            dismissButtonText = stringResource(id = R.string.cancel),
                                                            onDismissRequest = { DialogManager.dismissDialog() }
                                                        )
                                                    }
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun UserListItem(
    user: UserProfile,
    onItemClick: () -> Unit,
    onAddFriend: () -> Unit,
    onCancelFriendRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onItemClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = stringResource(id = R.string.user_avatar),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            AddFriendButton(
                status = user.friendshipStatus,
                onAddFriendClick = onAddFriend,
                onCancelRequestClick = onCancelFriendRequest
            )
        }
    }
}

@Composable
private fun AddFriendButton(
    status: FriendshipStatus,
    onAddFriendClick: () -> Unit,
    onCancelRequestClick: () -> Unit
) {
    when (status) {
        FriendshipStatus.NOT_FRIEND -> {
            Button(onClick = onAddFriendClick) {
                Text(text = stringResource(id = R.string.add_friend))
            }
        }

        FriendshipStatus.PENDING -> {
            OutlinedButton(onClick = onCancelRequestClick) {
                Text(text = stringResource(id = R.string.pending_friend_request))
            }
        }

        FriendshipStatus.ALREADY_FRIEND -> {
            TextButton(onClick = {}, enabled = false) {
                Text(text = stringResource(id = R.string.already_friend))
            }
        }
    }
}