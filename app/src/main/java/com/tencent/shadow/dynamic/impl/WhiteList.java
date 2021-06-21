package com.tencent.shadow.dynamic.impl;

/**
 * Creator: yc
 * Date: 2021/5/12 20:08
 * UseDes:
 * 此类包名及类名固定
 * classLoader的白名单
 * PluginManager可以加载宿主中位于白名单内的类
 */
public interface WhiteList {
    String[] sWhiteList = new String[]{
            "com.yc.commonlib.**", "com.yc.commonlib","okhttp3","okhttp3.**","retrofit2","retrofit2.**","com.google.gson","com.google.gson.**"
    };
}