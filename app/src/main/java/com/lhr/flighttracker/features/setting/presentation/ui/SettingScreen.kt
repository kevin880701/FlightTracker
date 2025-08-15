package com.lhr.flighttracker.features.setting.presentation.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lhr.flighttracker.R
import com.lhr.flighttracker.core.utils.dialog.DialogManager.dismissDialog
import com.lhr.flighttracker.core.utils.dialog.DialogPosition
import com.lhr.flighttracker.core.utils.dialog.showDialog
import com.lhr.flighttracker.features.main.presentation.widget.MainTitleBar
import com.lhr.flighttracker.features.setting.presentation.widget.dialog.ContactUsDialogContent
import com.lhr.flighttracker.features.setting.presentation.widget.dialog.LanguageDialogContent
import com.lhr.flighttracker.features.setting.presentation.widget.dialog.TermsOfServiceDialogContent
import com.lhr.flighttracker.features.setting.presentation.widget.dialog.ThemeDialogContent
import androidx.compose.ui.platform.LocalContext
import com.lhr.flighttracker.features.setting.presentation.widget.dialog.NotificationSettingsDialogContent

data class SettingItemData(
    val title: String,
    val onClick: () -> Unit,
    val leadingIcon: @Composable () -> Unit = {}
)

@Composable
fun SettingsScreen() {

    val context = LocalContext.current

    Scaffold(
        topBar = {
            MainTitleBar(
                title = stringResource(id = R.string.settings),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
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
                            }),
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
                            }),
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
                            })
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
                            }),
                        SettingItemData(
                            title = stringResource(id = R.string.terms_of_service),
                            onClick = {
                                showDialog(
                                    position = DialogPosition.BOTTOM,
                                    extendToNavigationBar = true,
                                    content = {
                                        TermsOfServiceDialogContent(onDismissRequest = {
                                            dismissDialog()
                                        })
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_article),
                                    contentDescription = stringResource(id = R.string.terms_of_service)
                                )
                            }),
                        SettingItemData(
                            title = stringResource(id = R.string.feedback),
                            onClick = {
                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:")
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf("support@flighttracker.com")) // 收件人
                                    putExtra(Intent.EXTRA_SUBJECT, "App 意見回饋") // 主旨
                                }
                                if (emailIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(emailIntent)
                                } },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_mail),
                                    contentDescription = stringResource(id = R.string.feedback)
                                )
                            })
                    )
                )
            }
        }
    }
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
                leadingIcon = itemData.leadingIcon
            )
        }
    }
}

/**
 * 設定項目
 */
@Composable
fun SettingItem(
    title: String,
    onClick: () -> Unit,
    leadingIcon: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leadingIcon()
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}