package com.lhr.flighttracker.features.setting.presentation.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lhr.flighttracker.R
import com.lhr.flighttracker.core.dialog.DialogManager.dismissDialog
import com.lhr.flighttracker.core.dialog.DialogPosition
import com.lhr.flighttracker.core.dialog.showDialog
import com.lhr.flighttracker.features.main.presentation.widget.MainTitleBar
import com.lhr.flighttracker.features.setting.presentation.widget.dialog.ContactUsDialogContent
import com.lhr.flighttracker.features.setting.presentation.widget.dialog.LanguageDialogContent
import com.lhr.flighttracker.features.setting.presentation.widget.dialog.TermsOfServiceDialogContent
import com.lhr.flighttracker.features.setting.presentation.widget.dialog.ThemeDialogContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavController
import com.lhr.flighttracker.LocalNavController
import com.lhr.flighttracker.features.setting.presentation.widget.SettingItem
import com.lhr.flighttracker.features.setting.presentation.widget.UserWidget
import com.lhr.flighttracker.features.setting.presentation.widget.dialog.NotificationSettingsDialogContent

data class SettingItemData(
    val title: String,
    val onClick: () -> Unit,
    val leadingIcon: @Composable () -> Unit = {},
    val modifier: Modifier
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(navController: NavController) {

    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        content = { _ ->
            Column {
                MainTitleBar(
                    title = stringResource(id = R.string.settings),
                    testTag = "setting_screen_title_bar"
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        UserWidget(
                            userName = "LHR",
                            userEmail = "lhr@example.com",
                            avatarUrl = "https://p3-pc-sign.douyinpic.com/tos-cn-i-0813c001/oEAAN9iIEAgzHUjfF8iNakAWUAgHTAAvBCXeA7~tplv-dy-aweme-images:q75.webp?biz_tag=aweme_images&from=327834062&lk3s=138a59ce&s=PackSourceEnum_SEARCH&sc=image&se=false&x-expires=1757782800&x-signature=ZYQv%2B8hqxacVrdwozko9glAVGnY%3D",
                            onClick = {
                                navController.navigate("UserProfile")
                            }
                        )
                    }

                    item {
                        SettingsSection(
                            title = stringResource(id = R.string.preferences_setting),
                            items = listOf(
                                SettingItemData(
                                    title = stringResource(id = R.string.notifications_setting),
                                    onClick = {
                                        showDialog(
                                            position = DialogPosition.BOTTOM,
                                            extendToNavigationBar = true,
                                            content = {
                                                NotificationSettingsDialogContent(onDismissRequest = {
                                                    dismissDialog()
                                                })
                                            }
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_notifications),
                                            contentDescription = stringResource(id = R.string.notifications_setting)
                                        )
                                    },
                                    modifier = Modifier
                                        .testTag("notifications_item")
                                        .semantics {
                                            testTagsAsResourceId = true
                                        },
                                ),
                                SettingItemData(
                                    title = stringResource(id = R.string.language),
                                    onClick = {
                                        showDialog(
                                            position = DialogPosition.BOTTOM,
                                            extendToNavigationBar = true,
                                            content = {
                                                LanguageDialogContent(onDismissRequest = {
                                                    dismissDialog()
                                                })
                                            }
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_language),
                                            contentDescription = stringResource(id = R.string.language)
                                        )
                                    },
                                    modifier = Modifier
                                        .testTag("language_item")
                                        .semantics {
                                            testTagsAsResourceId = true
                                        },
                                ),
                                SettingItemData(
                                    title = stringResource(id = R.string.theme),
                                    onClick = {
                                        showDialog(
                                            position = DialogPosition.BOTTOM,
                                            extendToNavigationBar = true,
                                            content = {
                                                ThemeDialogContent(onDismissRequest = {
                                                    dismissDialog()
                                                })
                                            }
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_theme),
                                            contentDescription = stringResource(id = R.string.theme)
                                        )
                                    },
                                    modifier = Modifier
                                        .testTag("theme_item")
                                        .semantics {
                                            testTagsAsResourceId = true
                                        },
                                ),
                            )
                        )
                    }

                    item {
                        SettingsSection(
                            title = stringResource(id = R.string.more_info),
                            items = listOf(
                                SettingItemData(
                                    title = stringResource(id = R.string.contact_us),
                                    onClick = {
                                        showDialog(
                                            position = DialogPosition.BOTTOM,
                                            extendToNavigationBar = true,
                                            content = {
                                                ContactUsDialogContent(onDismissRequest = {
                                                    dismissDialog()
                                                })
                                            }
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_contact),
                                            contentDescription = stringResource(id = R.string.contact_us)
                                        )
                                    },
                                    modifier = Modifier
                                        .testTag("contact_item")
                                        .semantics {
                                            testTagsAsResourceId = true
                                        },
                                ),
                                SettingItemData(
                                    title = stringResource(id = R.string.terms_of_service),
                                    onClick = {
                                        navController.navigate("TermsOfService")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_article),
                                            contentDescription = stringResource(id = R.string.terms_of_service)
                                        )
                                    },
                                    modifier = Modifier
                                        .testTag("terms_of_service_item")
                                        .semantics {
                                            testTagsAsResourceId = true
                                        },
                                ),
                                SettingItemData(
                                    title = stringResource(id = R.string.feedback),
                                    onClick = {
                                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:")
                                            putExtra(
                                                Intent.EXTRA_EMAIL,
                                                arrayOf("support@flighttracker.com")
                                            ) // 收件人
                                            putExtra(Intent.EXTRA_SUBJECT, "App 意見回饋") // 主旨
                                        }
                                        if (emailIntent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(emailIntent)
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_mail),
                                            contentDescription = stringResource(id = R.string.feedback)
                                        )
                                    },
                                    modifier = Modifier
                                        .testTag("feedback_item")
                                        .semantics {
                                            testTagsAsResourceId = true
                                        },
                                ),
                            )
                        )
                    }
                }
            }
        }
    )
}

/**
 * 設定區塊
 */
@Composable
fun SettingsSection(
    title: String,
    items: List<SettingItemData>
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        items.forEachIndexed { index, itemData ->
            SettingItem(
                title = itemData.title,
                onClick = itemData.onClick,
                leadingContent = itemData.leadingIcon,
                modifier = itemData.modifier
            )
        }
    }
}
