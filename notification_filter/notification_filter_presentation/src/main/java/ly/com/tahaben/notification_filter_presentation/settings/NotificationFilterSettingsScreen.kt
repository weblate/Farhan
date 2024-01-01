package ly.com.tahaben.notification_filter_presentation.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import ly.com.tahaben.core.R
import ly.com.tahaben.core_ui.LocalSpacing
import ly.com.tahaben.core_ui.components.PermissionDialog
import ly.com.tahaben.core_ui.components.PostNotificationPermissionTextProvider
import ly.com.tahaben.core_ui.components.ScheduleExactAlarmPermissionTextProvider
import ly.com.tahaben.core_ui.mirror
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationFilterSettingsScreen(
    onNavigateUp: () -> Unit,
    onNavigateToExceptions: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
    shouldShowRational: (String) -> Boolean,
    snackbarHostState: SnackbarHostState
) {
    val spacing = LocalSpacing.current
    val context = LocalContext.current
    val state = viewModel.state
    val dialogQueue = state.visiblePermissionDialogQueue
    val scope = rememberCoroutineScope()
    var checkKey by remember {
        mutableStateOf(false)
    }

    val postNotificationPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                viewModel.onPermissionResult(
                    permission = Manifest.permission.POST_NOTIFICATIONS,
                    isGranted = isGranted
                )
            }
        }
    )

    val postNotificationPermissionSilentCheck = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isGranted) {
                viewModel.onEvent(NotificationSettingsEvent.DeclinedPermission(Manifest.permission.POST_NOTIFICATIONS))
            }
            checkKey = !checkKey
        }
    )

    LaunchedEffect(key1 = true) {
        viewModel.event.collect { event ->
            when (event) {
                is UiEventNotificationSettings.NotifyMeEnabled -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        viewModel.checkExactAlarmsPermissionGranted()
                        postNotificationPermissionResultLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                }

                is UiEventNotificationSettings.PerformSilentChecks -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && state.isNotifyMeEnabled) {
                        postNotificationPermissionSilentCheck.launch(Manifest.permission.POST_NOTIFICATIONS)
                        viewModel.exactAlarmsSilentCheck()
                    }
                }
            }
        }
    }
    LaunchedEffect(key1 = checkKey) {
        if (state.declinedPermissions.isNotEmpty()) {
            if (dialogQueue.isEmpty()) {
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        context.getString(R.string.notify_me_permissions_needed_not_granted),
                        actionLabel = context.getString(
                            R.string.grant_access
                        ),
                        duration = SnackbarDuration.Indefinite
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            Timber.d("action performed: declined permissions: ${state.declinedPermissions.toList()} size: ${state.declinedPermissions.size}")
                            dialogQueue.addAll(state.declinedPermissions.toList())
                            state.declinedPermissions.clear()
                        }

                        else -> Unit
                    }
                }
            }
        } else {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.notifications_filter_settings))
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            navigationIcon = {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        modifier = Modifier.mirror(),
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            }
        )
        Column(
            modifier = Modifier
                .padding(horizontal = spacing.spaceMedium)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.spaceMedium)
                    .selectable(
                        selected = state.isServiceEnabled,
                        onClick = {
                            viewModel.setServiceStats(!state.isServiceEnabled)
                        },
                        role = Role.Switch
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,

                ) {
                Text(text = stringResource(R.string.notifications_filter))
                Switch(
                    checked = state.isServiceEnabled,
                    onCheckedChange = { checked ->
                        viewModel.setServiceStats(checked)
                    }
                )
            }
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.spaceMedium)
                    .clickable {
                        onNavigateToExceptions()
                    },
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.exceptions),
                    textAlign = TextAlign.Start
                )
            }
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.spaceMedium)
                    .selectable(
                        selected = state.isNotifyMeEnabled,
                        onClick = {
                            if (state.isNotifyMeEnabled) {
                                viewModel.onEvent(NotificationSettingsEvent.CancelNotifyMe)
                            } else {
                                viewModel.onEvent(NotificationSettingsEvent.ShowNotifyMeTimePicker)
                            }
                        },
                        role = Role.Switch
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(text = stringResource(R.string.notify_me))
                    Text(
                        text = stringResource(R.string.notify_me_description),
                        textAlign = TextAlign.Start
                    )
                }
                Switch(
                    checked = state.isNotifyMeEnabled,
                    onCheckedChange = { checked ->
                        if (!checked) {
                            viewModel.setNotifyMeTime(-1, -1)
                        } else {
                            viewModel.onEvent(NotificationSettingsEvent.ShowNotifyMeTimePicker)
                        }
                    }
                )
            }
            val timePickerState = rememberTimePickerState()
            if (state.isTimePickerVisible) {
                Dialog(onDismissRequest = {
                    viewModel.onEvent(NotificationSettingsEvent.DismissNotifyMeTimePicker)
                }) {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        tonalElevation = AlertDialogDefaults.TonalElevation
                    ) {
                        Column(
                            modifier = Modifier.padding(spacing.spaceLarge)
                        ) {
                            TimePicker(
                                state = timePickerState,
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    viewModel.onEvent(NotificationSettingsEvent.DismissNotifyMeTimePicker)
                                }) {
                                    Text(
                                        text = stringResource(id = R.string.cancel),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                                Spacer(modifier = Modifier.width(spacing.spaceSmall))
                                TextButton(onClick = {
                                    viewModel.onEvent(
                                        NotificationSettingsEvent.SaveNotifyMeTime(
                                            timePickerState.hour,
                                            timePickerState.minute
                                        )
                                    )
                                }) {
                                    Text(
                                        text = stringResource(id = R.string.ok),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }

                            }
                        }
                    }
                }
            }
            AnimatedVisibility(visible = state.isNotifyMeEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.spaceSmall)
                        .clickable {
                            viewModel.onEvent(NotificationSettingsEvent.ShowNotifyMeTimePicker)
                        },
                ) {
                    if (state.notifyMeHour != -1) {
                        Text(text = "${state.notifyMeHour}:${state.notifyMeMinute}")
                    }
                    Spacer(modifier = Modifier.height(spacing.spaceExtraSmall))
                    Text(text = stringResource(R.string.tap_to_set_new_time))
                }
            }
            Divider()
        }

    }

    dialogQueue
        .reversed()
        .forEach { permission ->
            PermissionDialog(
                permissionTextProvider = when (permission) {
                    Manifest.permission.POST_NOTIFICATIONS -> PostNotificationPermissionTextProvider()
                    Manifest.permission.SCHEDULE_EXACT_ALARM -> ScheduleExactAlarmPermissionTextProvider()
                    else -> return@forEach
                },
                isPermanentlyDeclined = !shouldShowRational(permission),
                onDismiss = { viewModel.onEvent(NotificationSettingsEvent.DismissPermissionDialog) },
                onOkClick = {
                    viewModel.onEvent(NotificationSettingsEvent.DismissPermissionDialog)
                    if (permission == Manifest.permission.SCHEDULE_EXACT_ALARM) {
                        viewModel.openExactAlarmsPermissionScreen()
                    } else {
                        postNotificationPermissionResultLauncher.launch(
                            permission
                        )
                    }
                },
                onGoToAppSettingsClick = {
                    if (permission == Manifest.permission.SCHEDULE_EXACT_ALARM) {
                        viewModel.openExactAlarmsPermissionScreen()
                    } else {
                        viewModel.openAppSettings()
                    }
                    viewModel.onEvent(NotificationSettingsEvent.DismissPermissionDialog)
                }
            )
        }
}


