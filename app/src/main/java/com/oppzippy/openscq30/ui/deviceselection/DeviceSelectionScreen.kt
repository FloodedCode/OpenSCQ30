@file:OptIn(ExperimentalMaterial3Api::class)

package com.oppzippy.openscq30.ui.deviceselection

import android.Manifest
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.compose.ui.res.stringResource
import com.oppzippy.openscq30.R
import com.oppzippy.openscq30.lib.wrapper.ConnectionDescriptor
import com.oppzippy.openscq30.lib.wrapper.PairedDevice
import com.oppzippy.openscq30.ui.deviceselection.screens.AppInfoScreen
import com.oppzippy.openscq30.ui.deviceselection.screens.DeviceListingScreen
import com.oppzippy.openscq30.ui.deviceselection.screens.SelectDeviceForPairingScreen
import com.oppzippy.openscq30.ui.deviceselection.screens.SelectModelForPairingScreen
import com.oppzippy.openscq30.ui.settings.SettingsPage
import com.oppzippy.openscq30.ui.utils.PermissionCheck
import kotlinx.serialization.Serializable

@Composable
fun DeviceSelectionScreen(
    onDeviceSelected: (device: PairedDevice) -> Unit,
    viewModel: DeviceSelectionViewModel = viewModel { DeviceSelectionViewModel() },
) {
    val bluetoothPermission = if (Build.VERSION.SDK_INT >= 31) {
        Manifest.permission.BLUETOOTH_CONNECT
    } else {
        Manifest.permission.BLUETOOTH
    }

    val navController = rememberNavController()

    PermissionCheck(
        permission = bluetoothPermission,
        prompt = stringResource(R.string.bluetooth_permission_is_required),
        onPermissionGranted = { viewModel.refreshPairedDevices() },
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Connect,
            enterTransition = {
                slideInHorizontally { width -> width / 2 } + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally { width -> -width / 2 } + fadeOut()
            },
            popEnterTransition = {
                slideInHorizontally { width -> -width / 2 } + fadeIn()
            },
            popExitTransition = {
                slideOutHorizontally { width -> width / 2 } + fadeOut()
            },
        ) {
            composable<Screen.Connect> {
                val context = androidx.compose.ui.platform.LocalContext.current
                val activity = context as? android.app.Activity

                DeviceListingScreen(
                    devices = viewModel.pairedDevices.collectAsState().value,
                    onDeviceClick = { onDeviceSelected(it) },
                    onUnpair = { device -> activity?.let { viewModel.unpair(it, device) } },
                    onAddDeviceClick = { navController.navigate(Screen.SelectModelForPairing) },
                    onRefreshClick = { viewModel.refreshPairedDevices() },
                    onSettingsClick = { navController.navigate(Screen.Settings) },
                    onInfoClick = { navController.navigate(Screen.Info) },
                )
            }

            composable<Screen.SelectDeviceForPairing> { backStackEntry ->
                val screen = backStackEntry.toRoute<Screen.SelectDeviceForPairing>()
                val context = androidx.compose.ui.platform.LocalContext.current
                val activity = context as? android.app.Activity

                var isDemoMode by remember { mutableStateOf(false) }
                val devices = remember { mutableStateOf<List<ConnectionDescriptor>?>(null) }
                LaunchedEffect(screen.model, isDemoMode) {
                    devices.value = viewModel.listDevices(screen.model, isDemoMode)
                }

                val devicesValue = devices.value
                SelectDeviceForPairingScreen(
                    model = screen.model,
                    isDemoMode = isDemoMode,
                    devices = devicesValue,
                    onDemoModeChange = { isDemoMode = it },
                    onDescriptorSelected = { descriptor ->
                        activity?.let { act ->
                            viewModel.pair(
                                activity = act,
                                pairedDevice = PairedDevice(
                                    macAddress = descriptor.macAddress,
                                    model = screen.model,
                                    isDemo = isDemoMode,
                                ),
                                onPaired = {
                                    navController.popBackStack<Screen.Connect>(false)
                                },
                            )
                        }
                    },
                    onBackClick = { navController.popBackStack() },
                )
            }

            composable<Screen.SelectModelForPairing> {
                SelectModelForPairingScreen(
                    onModelSelected = { navController.navigate(Screen.SelectDeviceForPairing(it)) },
                    onBackClick = { navController.popBackStack() },
                )
            }

            composable<Screen.Info> {
                AppInfoScreen(onBackClick = { navController.popBackStack() })
            }

            composable<Screen.Settings> {
                SettingsPage(onBackClick = { navController.popBackStack() })
            }
        }
    }
}

@Serializable
private sealed class Screen {
    @Serializable
    data object Connect : Screen()

    @Serializable
    data object SelectModelForPairing : Screen()

    @Serializable
    data class SelectDeviceForPairing(val model: String) : Screen()

    @Serializable
    data object Info : Screen()

    @Serializable
    data object Settings : Screen()
}
