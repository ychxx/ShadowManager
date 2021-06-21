package com.yc.shadowmanager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.util.Log
import com.tencent.shadow.core.manager.installplugin.InstalledPlugin
import com.tencent.shadow.core.manager.installplugin.InstalledType
import com.tencent.shadow.dynamic.host.EnterCallback
import com.tencent.shadow.dynamic.host.FailedException
import com.tencent.shadow.dynamic.manager.PluginManagerThatUseDynamicLoader
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Creator: yc
 * Date: 2021/5/12 19:49
 * UseDes:
 */
public class YcPluginManager(context: Context?) : PluginManagerThatUseDynamicLoader(context) {

    private val mSingleThread = Executors.newSingleThreadExecutor()
    private val mFixedPool = Executors.newFixedThreadPool(4)

    /**
     * PluginManager的名字
     * 用于和其他PluginManager区分持续化存储的名字
     */
    override fun getName(): String {
        return YcPluginManagerConstant.MANAGER_NAME
    }

    override fun enter(context: Context, formId: Long, bundle: Bundle, callback: EnterCallback) {
        Log.e(YcPluginManagerConstant.LOG_TAG, "enter-到了manager的enter")
        when (formId) {
            YcPluginManagerConstant.FROM_ID_START_ACTIVITY -> {
                Log.e(YcPluginManagerConstant.LOG_TAG, "enter-formId:$formId 开始页面跳转")
                startActivity(context, bundle, callback)
            }
            else -> {
                Log.e(YcPluginManagerConstant.LOG_TAG, "enter-找不到对应的formId")
            }
        }
    }

    /**
     * 启动子模块Activity
     * @param context Context
     * @param bundle Bundle
     * @param callback EnterCallback
     */
    private fun startActivity(context: Context, bundle: Bundle, callback: EnterCallback?) {
        Log.e(YcPluginManagerConstant.LOG_TAG, "startActivity1-start")
        val childZipPath = bundle.getString(YcPluginManagerConstant.FromHostKey.KEY_CHILD_ZIP_PATH)
        val childKey = bundle.getString(YcPluginManagerConstant.FromHostKey.KEY_CHILD_PART_KEY)
        val activityClassName = bundle.getString(YcPluginManagerConstant.FromHostKey.KEY_START_ACTIVITY_CLASSNAME)
        val dataBundle = bundle.getBundle(YcPluginManagerConstant.FromHostKey.KEY_TO_ACTIVITY_BUNDLE)
        val serviceName = bundle.getString(YcPluginManagerConstant.FromHostKey.KEY_TO_ACTIVITY_SERVICE_NAME) ?: "服务名为空"
        Log.e(YcPluginManagerConstant.LOG_TAG, "startActivity1-childZipPath:$childZipPath--childKey:$childKey--activityClassName:$activityClassName--")
        Log.e(YcPluginManagerConstant.LOG_TAG, "startActivity1-serviceName:$serviceName")
        callback?.onShowLoadingView(null)//调用host，让其显示加载中动画处理
        if (childZipPath == null || activityClassName == null) {
            Log.e(YcPluginManagerConstant.LOG_TAG, "startActivity1-插件路径或者启动Activity类名为空")
            throw Throwable("插件路径或者启动Activity类名为空")
        } else {
            mSingleThread.execute {
                try {
                    val installedPlugin: InstalledPlugin = installPlugin(childZipPath, null, true) //这个调用是阻塞的
                    val pluginIntent = Intent()
                    pluginIntent.setClassName(context.packageName, activityClassName)
                    if (dataBundle != null) {
                        pluginIntent.replaceExtras(dataBundle)
                    }
                    startPluginActivity(context, installedPlugin, childKey, pluginIntent, serviceName)
                } catch (e: Exception) {
                    throw  RuntimeException(e)
                }
                if (callback != null) {
                    val uiHandler = Handler(Looper.getMainLooper())
                    uiHandler.post {
                        callback.onCloseLoadingView()
                        callback.onEnterComplete()
                    }
                }
            }
        }
        Log.e(YcPluginManagerConstant.LOG_TAG, "startActivity1-end")
    }

    /**
     *
     * @param zipPath String    压缩包路径
     * @param hash String?      压缩包hash
     * @param odex Boolean
     * @return InstalledPlugin
     */
    private fun installPlugin(zipPath: String, hash: String?, odex: Boolean): InstalledPlugin {
        Log.e(YcPluginManagerConstant.LOG_TAG, "installPlugin-start")
        val pluginConfig = installPluginFromZip(File(zipPath), hash)
        val uuid = pluginConfig.UUID
        val futures: MutableList<Future<*>> = LinkedList()
        if (pluginConfig.runTime != null && pluginConfig.pluginLoader != null) {
            val odexRuntime: Future<*> = mFixedPool.submit<Any> {
                oDexPluginLoaderOrRunTime(
                    uuid, InstalledType.TYPE_PLUGIN_RUNTIME,
                    pluginConfig.runTime.file
                )
                null
            }
            futures.add(odexRuntime)
            val odexLoader: Future<*> = mFixedPool.submit<Any> {
                oDexPluginLoaderOrRunTime(
                    uuid, InstalledType.TYPE_PLUGIN_LOADER,
                    pluginConfig.pluginLoader.file
                )
                null
            }
            futures.add(odexLoader)
        }
        for ((partKey, value) in pluginConfig.plugins) {
            val apkFile = value.file
            val extractSo: Future<*> = mFixedPool.submit<Any> {
                extractSo(uuid, partKey, apkFile)
                null
            }
            futures.add(extractSo)
            if (odex) {
                val odexPlugin: Future<*> = mFixedPool.submit<Any> {
                    oDexPlugin(uuid, partKey, apkFile)
                    null
                }
                futures.add(odexPlugin)
            }
        }
        for (future in futures) {
            future.get()
        }
        onInstallCompleted(pluginConfig)
        Log.e(YcPluginManagerConstant.LOG_TAG, "installPlugin-end")
        return getInstalledPlugins(1)[0]
    }

    @Throws(RemoteException::class, TimeoutException::class, FailedException::class)
    fun startPluginActivity(context: Context, installedPlugin: InstalledPlugin, partKey: String?, pluginIntent: Intent?, serviceName: String) {
        Log.e(YcPluginManagerConstant.LOG_TAG, "startPluginActivity-start")
        val intent: Intent = convertActivityIntent(installedPlugin, partKey, pluginIntent, serviceName)
        if (context !is Activity) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        Log.e(YcPluginManagerConstant.LOG_TAG, "startPluginActivity-end")
    }

    @Throws(RemoteException::class, TimeoutException::class, FailedException::class)
    fun convertActivityIntent(installedPlugin: InstalledPlugin, partKey: String?, pluginIntent: Intent?, serviceName: String): Intent {
        Log.e(YcPluginManagerConstant.LOG_TAG, "convertActivityIntent-start")
        loadPlugin(installedPlugin.UUID, partKey, serviceName)
        Log.e(YcPluginManagerConstant.LOG_TAG, "convertActivityIntent-end")
        return mPluginLoader.convertActivityIntent(pluginIntent)
    }

    @Throws(RemoteException::class, TimeoutException::class, FailedException::class)
    protected fun loadPlugin(uuid: String, partKey: String?, serviceName: String) {
        Log.e(YcPluginManagerConstant.LOG_TAG, "loadPlugin-start")
        loadPluginLoaderAndRuntime(uuid, serviceName)
        val map = mPluginLoader.loadedPlugin
        if (!map.containsKey(partKey)) {
            mPluginLoader.loadPlugin(partKey)
        }
        val isCall = map[partKey] as Boolean?
        if (isCall == null || !isCall) {
            mPluginLoader.callApplicationOnCreate(partKey)
        }
        Log.e(YcPluginManagerConstant.LOG_TAG, "loadPlugin-end")
    }

    @Throws(RemoteException::class, TimeoutException::class, FailedException::class)
    private fun loadPluginLoaderAndRuntime(uuid: String, serviceName: String) {
        Log.e(YcPluginManagerConstant.LOG_TAG, "loadPluginLoaderAndRuntime-start")
        if (mPpsController == null) {
            Log.e(YcPluginManagerConstant.LOG_TAG, "bindPluginProcessService-start")
            bindPluginProcessService(serviceName)
            Log.e(YcPluginManagerConstant.LOG_TAG, "bindPluginProcessService-end")
            Log.e(YcPluginManagerConstant.LOG_TAG, "waitServiceConnected-start")
            waitServiceConnected(10, TimeUnit.SECONDS)
            Log.e(YcPluginManagerConstant.LOG_TAG, "waitServiceConnected-end")
        }
        Log.e(YcPluginManagerConstant.LOG_TAG, "loadRunTime-start")
        loadRunTime(uuid)
        Log.e(YcPluginManagerConstant.LOG_TAG, "loadPluginLoader-start")
        loadPluginLoader(uuid)
        Log.e(YcPluginManagerConstant.LOG_TAG, "loadPluginLoaderAndRuntime-end")
    }
}