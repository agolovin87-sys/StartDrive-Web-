package com.example.startdrive.shared.repository

import com.example.startdrive.shared.model.AppInfo
import com.example.startdrive.shared.platformName

/**
 * Репозиторий общей информации о приложении.
 * Реализация в commonMain — одна для всех платформ.
 */
interface AppInfoRepository {
    fun getAppInfo(): AppInfo
}

/**
 * Реализация по умолчанию: данные из констант + платформа через expect/actual.
 */
class DefaultAppInfoRepository : AppInfoRepository {
    override fun getAppInfo(): AppInfo = AppInfo(
        appName = com.example.startdrive.shared.Shared.appName,
        version = APP_VERSION,
        platform = platformName()
    )

    companion object {
        private const val APP_VERSION = "1.0.0"
    }
}
