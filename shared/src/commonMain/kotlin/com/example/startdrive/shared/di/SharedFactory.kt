package com.example.startdrive.shared.di

import com.example.startdrive.shared.repository.AppInfoRepository
import com.example.startdrive.shared.repository.DefaultAppInfoRepository

/**
 * Простая фабрика общих зависимостей (без Koin/Kodein).
 * И app, и webApp могут создавать репозитории через этот объект.
 */
object SharedFactory {
    private var _appInfoRepository: AppInfoRepository? = null
    private fun appInfoRepository(): AppInfoRepository {
        if (_appInfoRepository == null) _appInfoRepository = DefaultAppInfoRepository()
        return _appInfoRepository!!
    }

    fun getAppInfoRepository(): AppInfoRepository = appInfoRepository()
}
