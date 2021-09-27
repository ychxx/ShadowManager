package com.tencent.shadow.core.common;

/**
 * Creator: yc
 * Date: 2021/9/14 14:49
 * UseDes:合并插件AndroidManifest.xml发生冲突时处理方式
 */
public class MergeType {
    /**
     * 冲突时抛异常
     */
    public final static String THROW_EXCEPTION = "throw_exception";
    /**
     * 冲突时使用之前添加的
     */
    public final static String USER_BEFORE = "user_before";
    /**
     * 冲突时使用现在添加的
     */
    public final static String USER_AFTER = "user_after";
}
