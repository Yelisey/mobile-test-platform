package com.atiurin.atp.farmcore.entity


data class Config(
    val farmMode: FarmMode = FarmMode.LOCAL,
    var maxDevicesAmount: Int = 0,
    var maxDeviceCreationBatchSize: Int = 10,
    var keepAliveDevicesMap: MutableMap<String, Int> = mutableMapOf(),
    var busyDeviceTimeoutSec: Long = 30 * 60,
    var creatingDeviceTimeoutSec: Long = 10 * 60,
    var isMock: Boolean = false,
    var startPort: Int = 0,
    var endPort: Int = 65534,
    var devicePoolMonitorDelay: Long = 5_000L,
    var serverMonitorDelay: Long = 5_000L,
    var busyDevicesMonitorDelay: Long = 5_000L,
    var creatingDevicesMonitorDelay: Long = 5_000L,
    var deviceNeedToDeleteMonitorDelay: Long = 5_000L,
    var deviceNeedToCreateMonitorDelay: Long = 5_000L,
    var brokenDevicesMonitorDelay: Long = 30_000L,
    var androidContainerAdbPath: String = "/android/platform-tools",
    var serverAliveTimeoutSec: Long = 30,
    var emulatorParams: String? = null,
    var emulatorEnvironments: MutableMap<String, String> = mutableMapOf(),
)
