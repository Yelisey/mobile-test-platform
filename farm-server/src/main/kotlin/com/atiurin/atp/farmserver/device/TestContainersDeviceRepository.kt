package com.atiurin.atp.farmserver.device

import com.atiurin.atp.farmcore.entity.DeviceState
import com.atiurin.atp.farmcore.util.waitForWithDelay
import com.atiurin.atp.farmserver.config.FarmConfig
import com.atiurin.atp.farmserver.images.AndroidImagesConfiguration
import com.atiurin.atp.farmserver.logging.log
import com.atiurin.atp.farmserver.util.NetUtil
import com.atiurin.atp.farmserver.util.nowSec
import com.farm.cli.command.DockerExecAdbBootAnimationCompletedCommand
import com.farm.cli.command.DockerExecAdbBootCompletedCommand
import com.github.dockerjava.api.model.Device
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Repository
import org.testcontainers.utility.DockerImageName


@Lazy
@Repository
class TestContainersDeviceRepository @Autowired constructor(
    private val farmConfig: FarmConfig,
    private val androidImages: AndroidImagesConfiguration,
) : DeviceRepository {
    private val containerMap: MutableMap<String, FarmDevice> = mutableMapOf()

    override suspend fun createDevice(farmDevice: FarmDevice): FarmDevice {
        log.info { "Start device creation $farmDevice" }
        val image = androidImages.get(farmDevice.deviceInfo.groupId)
        val container = AndroidContainer<Nothing>(DockerImageName.parse(image)).apply {
            withCreateContainerCmdModifier { cmd ->
                cmd.hostConfig?.withDevices(Device("rwm", "/dev/kvm", "/dev/kvm"))
                farmConfig.get().apply {
                    emulatorParams?.let { params -> cmd.withEnv("EMULATOR_PARAMS", params) }
                    emulatorEnvironments.forEach { (key, value) ->
                        cmd.withEnv(key, value)
                    }
                }
            }
        }

        startContainer(container)
        val containerInfo = ContainerInfo(
            adbPort = container.getHostAdbPort(),
            ip = NetUtil.localhostName,
            gRpcPort = container.getHostGrpcPort(),
            dockerImage = image
        )
        farmDevice.containerInfo = containerInfo
        farmDevice.container = container
        containerMap[farmDevice.id] = farmDevice
        log.info { "Device ${farmDevice.id} is booting. Created container $containerInfo, " }
        val bootTimeout = farmConfig.get().creatingDeviceTimeoutSec
        val isDeviceCreated = waitForWithDelay(timeoutMs = bootTimeout * 1000, intervalMs = 1000){
            isDeviceAlive(farmDevice.id)
        }
        val state = if (isDeviceCreated){
            log.info { "Change device ${farmDevice.id} state to ${DeviceState.READY} as it's booted." }
            DeviceState.READY
        } else {
            log.info { "Change device ${farmDevice.id} state to ${DeviceState.BROKEN} as it's not booted during $bootTimeout sec. $farmDevice" }
            DeviceState.BROKEN
        }
        farmDevice.stateTimestampSec = nowSec()
        farmDevice.state = state
        return farmDevice
    }

    override suspend fun deleteDevice(deviceId: String) {
        val device = containerMap[deviceId]?.let { farmDevice ->
            farmDevice.container?.stop()
        }
        containerMap.remove(deviceId)
    }

    override fun isDeviceAlive(deviceId: String): Boolean {
        // Получаем FarmDevice, а из него уже containerId, если он там есть, или сам контейнер.
        // В вашем текущем коде isDeviceAlive вы берете container.containerId.
        // Предположим, что FarmDevice.container это объект AndroidContainer, у которого есть containerId
        val deviceFromMap = containerMap[deviceId]
        val containerId = deviceFromMap?.container?.containerId ?: run {
            // Если у вас containerId хранится в другом месте в FarmDevice, используйте его.
            // Например, если FarmDevice.containerInfo.containerId существует, или если
            // сам deviceFromMap.container является строкой containerId.
            // В вашем оригинальном isDeviceAlive было containerMap[deviceId]?.container?.containerId
            // что предполагает, что `containerMap[deviceId]?.container` - это объект, имеющий `containerId`.
            // Если `deviceFromMap.container` - это `AndroidContainer`, то у него есть `containerId`.
            log.debug { "Device or container not found for id $deviceId in isDeviceAlive" }
            return false
        }

        // Выполняем команду для проверки содержимого sdcard/Android
        val sdcardCheckResult = runBlocking {
            DockerExecAdbBootAnimationCompletedCommand(
                containerId = containerId, // Используем containerId
                adbContainerPath = "${farmConfig.get().androidContainerAdbPath}/adb"
            ).execute() // Возвращает CliCommandResult(success: Boolean, message: String)
        }

        // Важно: sdcardCheckResult.success будет почти всегда false из-за анализатора "stopped".
        // Поэтому мы анализируем sdcardCheckResult.message (необработанный вывод команды).
        val output = sdcardCheckResult.message

        // Проверяем, содержит ли вывод ожидаемые директории.
        // Это также неявно проверяет, что команда 'ls' вообще смогла что-то вывести.
        // Если 'ls' завершится с ошибкой, ее вывод (если он попадет в .message)
        // вряд ли будет содержать эти строки.
        val hasExpectedContent = output.contains("data") &&
                output.contains("media") &&
                output.contains("obb")

        if (!hasExpectedContent) {
            log.debug { "Device $deviceId sdcard content check failed. Output: $output" }
        }

        return hasExpectedContent
    }

    override fun getDevices(): List<FarmDevice> = containerMap.values.toList()

    private fun startContainer(container: AndroidContainer<Nothing>) {
        container.apply {
            log.info { "Start container $container" }
            withPrivilegedMode(true)
            container.exposeAdbPort(farmConfig.getPortInRange())
            container.exposeGrpcPort(farmConfig.getPortInRange())
            start()
        }
    }
}