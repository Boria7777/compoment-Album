package com.yanzhenjie.album.dialog;

import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.yanzhenjie.album.R;
import com.yanzhenjie.album.adapter.DialogFolderAdapter;
import com.yanzhenjie.album.entity.AlbumFolder;
import com.yanzhenjie.album.impl.OnCompatItemClickListener;
import com.yanzhenjie.album.task.Poster;
import com.yanzhenjie.album.util.SelectorUtils;

import java.util.ArrayList;

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


public class AlbumFolderDialogFragment extends DialogFragment {
    private RecyclerView rvContentList;
    private ArrayList<AlbumFolder> albumFolders;
    private RelativeLayout cancel_layout;
    private int mToolBarColor;
    private int statusColor;
    private Toolbar mToolbar;
    private OnCompatItemClickListener mItemClickListener;
    private boolean isOpen = true;
    private int checkPosition = 0;
    private DialogFolderAdapter dialogFolderAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
        albumFolders = getArguments().getParcelableArrayList("albumFolders");
        mToolBarColor = getArguments().getInt("mToolBarColor");
        statusColor = getArguments().getInt("statusColor");
    }

    public void setItemclickListener(OnCompatItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public static AlbumFolderDialogFragment getInstance(ArrayList<AlbumFolder> albumFolders, int mToolBarColor, int statusColor) {
        AlbumFolderDialogFragment f = new AlbumFolderDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("albumFolders", albumFolders);
        args.putInt("mToolBarColor", mToolBarColor);
        args.putInt("statusColor", statusColor);
        f.setArguments(args);
        return f;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.album_folder_dialog, container, false);

        mToolbar = (Toolbar) view.findViewById(R.id.dialogtoolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        cancel_layout = (RelativeLayout) view.findViewById(R.id.cancel_layout);
        cancel_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        mToolbar.setBackgroundColor(mToolBarColor);
        setStatusBarColor(statusColor);
        rvContentList = (RecyclerView) view.findViewById(R.id.content_list);
        rvContentList.setHasFixedSize(true);
        rvContentList.setLayoutManager(new LinearLayoutManager(getContext()));
        if (dialogFolderAdapter == null) {
            dialogFolderAdapter = new DialogFolderAdapter(SelectorUtils.createColorStateList(ContextCompat.getColor(getContext(), R.color.albumPrimaryBlack), mToolBarColor), albumFolders, new OnCompatItemClickListener() {
                @Override
                public void onItemClick(final View view, final int position) {
                    if (isOpen) { // 反应太快，按钮点击效果出不来，故加延迟。
                        isOpen = false;
                        Poster.getInstance().post(new Runnable() {
                            @Override
                            public void run() {

                                if (mItemClickListener != null && checkPosition != position) {
                                    checkPosition = position;
                                    mItemClickListener.onItemClick(view, position);
                                }
                                isOpen = true;
                            }
                        });
                        Poster.getInstance().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                behaviorHide();
                            }
                        }, 400);
                    }
                }
            });
        }
        rvContentList.setAdapter(dialogFolderAdapter);

        return view;
    }


    public void behaviorHide() {
        this.dismiss();
    }

    private void setStatusBarColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            final Window window = getDialog().getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);
                window.setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.albumPrimaryBlack));
            }
        }
    }

}
