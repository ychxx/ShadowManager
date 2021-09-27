package com.yc.shadowmanager

/**
 * Creator: yc
 * Date: 2021/5/19 15:12
 * UseDes:
 */
object YcPluginManagerConstant {
    const val LOG_TAG = "yc_manager_log"

    /**
     * PluginManager的名字
     * 用于和其他PluginManager区分持续化存储的名字
     */
    const val MANAGER_NAME = "yc_shadow_manager"

    const val FROM_ID_START_ACTIVITY: Long = 233
    /**
     * cpu架构，宿主和插件必须一致（由于shadow作者并没实现根据系统自动选择对应的abi，所以只能全部写一种，且删除其他abi）
     */
    const val ABI = "armeabi-v7a"
}