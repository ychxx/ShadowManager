package com.tencent.shadow.dynamic.impl;

import android.content.Context;
import android.util.Log;

import com.tencent.shadow.dynamic.host.ManagerFactory;
import com.tencent.shadow.dynamic.host.PluginManagerImpl;
import com.yc.shadowmanager.YcPluginManager;

/**
 * Creator: yc
 * Date: 2021/5/12 20:07
 * UseDes:
 * 此类包名及类名固定
 */

public final class  ManagerFactoryImpl implements ManagerFactory {
    @Override
    public PluginManagerImpl buildManager(Context context) {
        Log.e("manager", "ManagerFactoryImpl");
        return new YcPluginManager(context);
    }
}
