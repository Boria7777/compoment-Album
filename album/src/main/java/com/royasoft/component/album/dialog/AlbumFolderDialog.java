/*
 * AUTHOR：Yan Zhenjie
 *
 * DESCRIPTION：create the File, and add the content.
 *
 * Copyright © ZhiMore. All Rights Reserved
 *
 */
package com.royasoft.component.album.dialog;

import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.royasoft.component.album.R;
import com.royasoft.component.album.adapter.DialogFolderAdapter;
import com.royasoft.component.album.entity.AlbumFolder;
import com.royasoft.component.album.impl.OnCompatItemClickListener;
import com.royasoft.component.album.task.Poster;
import com.royasoft.component.album.util.SelectorUtils;

import java.util.List;


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
public class AlbumFolderDialog extends BottomSheetDialog {

    private int checkPosition = 0;
    private BottomSheetBehavior bottomSheetBehavior;
    private OnCompatItemClickListener mItemClickListener;

    private boolean isOpen = true;

    public AlbumFolderDialog(@NonNull Context context, @ColorInt int toolbarColor, @Nullable List<AlbumFolder> albumFolders, @Nullable OnCompatItemClickListener itemClickListener) {
        super(context, R.style.AlbumDialogStyle_Folder);
        setContentView(R.layout.album_dialog_album_floder);
        mItemClickListener = itemClickListener;

        fixRestart();

        RecyclerView rvContentList = (RecyclerView) findViewById(R.id.rv_content_list);
        rvContentList.setHasFixedSize(true);
        rvContentList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvContentList.setAdapter(new DialogFolderAdapter(SelectorUtils.createColorStateList(ContextCompat.getColor(context, R.color.albumPrimaryBlack), toolbarColor), albumFolders, new OnCompatItemClickListener() {
            @Override
            public void onItemClick(final View view, final int position) {
                if (isOpen) { // 反应太快，按钮点击效果出不来，故加延迟。
                    isOpen = false;
                    Poster.getInstance().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            behaviorHide();
                            if (mItemClickListener != null && checkPosition != position) {
                                checkPosition = position;
                                mItemClickListener.onItemClick(view, position);
                            }
                            isOpen = true;
                        }
                    }, 200);
                }
            }
        }));
        setStatusBarColor(toolbarColor);
    }

    private void setStatusBarColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            final Window window = getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);
                window.setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.albumPrimaryBlack));
            }
        }
    }

    /**
     * 修复不能重新显示的bug。
     */
    private void fixRestart() {
        View view = findViewById(android.support.design.R.id.design_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(view);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }


    /**
     * 关闭dialog。
     */
    public void behaviorHide() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
}
