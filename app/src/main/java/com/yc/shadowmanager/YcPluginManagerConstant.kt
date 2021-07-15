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
    const val MANAGER_NAME = "sample-manager"

    const val FROM_ID_START_ACTIVITY: Long = 233

    /**
     * cpu架构，宿主和插件必须一致
     */
    const val ABI = "armeabi-v7a"
    /**
     * 来至host的Key
     */
    object FromHostKey {
        /**
         * 压缩包路径
         */
        const val KEY_CHILD_ZIP_PATH = "key_child_zip_path"

        /**
         * 启动Activity的类名
         */
        const val KEY_START_ACTIVITY_CLASSNAME = "key_start_activity_classname"

        /**
         * 子插件
         */
        const val KEY_CHILD_PART_KEY = "key_child_part_key"

        /**
         * 启动Activity传入的bundle（用于发送数据）
         */
        const val KEY_TO_ACTIVITY_BUNDLE = "key_to_activity_bundle"
        /**
         * 主程序 服务名
         */
        const val KEY_TO_ACTIVITY_SERVICE_NAME = "key_to_activity_service_name"
    }
}