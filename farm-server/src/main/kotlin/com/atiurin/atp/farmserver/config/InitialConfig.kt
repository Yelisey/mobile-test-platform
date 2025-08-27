package com.atiurin.atp.farmserver.config

import com.atiurin.atp.farmcore.entity.Config
import com.atiurin.atp.farmcore.entity.FarmMode

data class InitialConfig(
    val maxDevicesAmount: Int = 0,
    val maxDeviceCreationBatchSize: Int = 10,
    val keepAliveDevicesMap: Map<String, Int> = mapOf(),
    val deviceBusyTimeoutSec: Long = 30 * 60,
    val isMock: Boolean = false,
    val startPort: Int = 0,
    val farmMode: String? = "MULTIPLE",
    val endPort: Int = 65534,
    val imagesMap: Map<String, String> = mapOf(),
    val androidContainerAdbPath: String? = null,
    val emulatorParams: String? = null,
    val emulatorEnvironments: Map<String, String> = mapOf()
)

fun InitialConfig.toConfig(farmMode: FarmMode = FarmMode.MULTIPLE) = Config(
    maxDevicesAmount = this.maxDevicesAmount,
    maxDeviceCreationBatchSize = this.maxDeviceCreationBatchSize,
    keepAliveDevicesMap = this.keepAliveDevicesMap.toMutableMap(),
    busyDeviceTimeoutSec = this.deviceBusyTimeoutSec,
    isMock = this.isMock,
    startPort = this.startPort,
    endPort = this.endPort,
    farmMode = farmMode,
    androidContainerAdbPath = this.androidContainerAdbPath ?: "/android/sdk/platform-tools",
    emulatorParams = this.emulatorParams,
    emulatorEnvironments = this.emulatorEnvironments.toMutableMap()
)

