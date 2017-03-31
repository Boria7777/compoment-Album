/*
 * AUTHOR：Yan Zhenjie
 *
 * DESCRIPTION：create the File, and add the content.
 *
 * Copyright © ZhiMore. All Rights Reserved
 *
 */
package com.royasoft.component.album.task;

import android.os.Handler;
import android.os.Looper;


/***********************************************************************************************
 * 类名称:
 * 类描述:
 * 创建人:   包勇 2017/3/29.
 * 创建时间:   2017/3/29.
 * 创建备注：
 * 创建版本:
 * 修改人:
 * 修改时间:
 * 修改备注:
 *
 ************************************************************************************************/
public class Poster extends Handler {

    private static Poster instance;

    /**
     * Get single object.
     *
     * @return {@link Poster}.
     */
    public static Poster getInstance() {
        if (instance == null)
            synchronized (Poster.class) {
                if (instance == null)
                    instance = new Poster();
            }
        return instance;
    }

    private Poster() {
        super(Looper.getMainLooper());
    }
}
