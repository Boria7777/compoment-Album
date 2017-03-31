/*
 * AUTHOR：Yan Zhenjie
 *
 * DESCRIPTION：create the File, and add the content.
 *
 * Copyright © ZhiMore. All Rights Reserved
 *
 */
package com.royasoft.component.album.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.royasoft.component.album.R;
import com.royasoft.component.album.entity.AlbumFolder;
import com.royasoft.component.album.entity.AlbumImage;
import com.royasoft.component.album.impl.OnCompatItemClickListener;
import com.royasoft.component.album.util.DisplayUtils;

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
public class IosDialogFolderAdapter extends RecyclerView.Adapter<IosDialogFolderAdapter.FolderViewHolder> {

    private ColorStateList mButtonTint;

    private static Context ctx = null;

    private List<AlbumFolder> mAlbumFolders;

    private OnCompatItemClickListener mItemClickListener;

    private int checkPosition = 0;

    private static int size = DisplayUtils.dip2px(100);

    public IosDialogFolderAdapter(ColorStateList buttonTint, List<AlbumFolder> mAlbumFolders, OnCompatItemClickListener mItemClickListener) {
        this.mButtonTint = buttonTint;
        this.mAlbumFolders = mAlbumFolders;
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (ctx == null) {
            ctx = parent.getContext();
        }
        return new FolderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.album_item_ios_folder, parent, false));
    }

    @Override
    public void onBindViewHolder(FolderViewHolder holder, int position) {
        final int newPosition = holder.getAdapterPosition();
        holder.setButtonTint(mButtonTint);
        holder.setData(mAlbumFolders.get(newPosition));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlbumFolder albumFolder = mAlbumFolders.get(newPosition);

                if (mItemClickListener != null)
                    mItemClickListener.onItemClick(v, newPosition);

                if (!albumFolder.isChecked()) {
                    albumFolder.setChecked(true);
                    mAlbumFolders.get(checkPosition).setChecked(false);
                    notifyItemChanged(checkPosition);
                    notifyItemChanged(newPosition);
                    checkPosition = newPosition;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAlbumFolders == null ? 0 : mAlbumFolders.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIvImage;
        private TextView forderName;
        private TextView fordersize;
        private AppCompatRadioButton mRbCheck;

        public FolderViewHolder(View itemView) {
            super(itemView);

            mIvImage = (ImageView) itemView.findViewById(R.id.iv_gallery_preview_image);
            forderName = (TextView) itemView.findViewById(R.id.forderName);
            fordersize = (TextView) itemView.findViewById(R.id.fordersize);
            mRbCheck = (AppCompatRadioButton) itemView.findViewById(R.id.rb_gallery_preview_check);
        }

        public void setButtonTint(ColorStateList colorStateList) {
//            mRbCheck.setSupportButtonTintList(colorStateList);
        }

        public void setData(AlbumFolder albumFolder) {
            List<AlbumImage> albumImages = albumFolder.getPhotos();
            forderName.setText(albumFolder.getName());
            fordersize.setText("(" + albumImages.size() + ") ");
            mRbCheck.setChecked(albumFolder.isChecked());

            if (albumImages.size() > 0) {
                Glide.with(ctx.getApplicationContext())
                        .load(albumImages.get(0).getPath())
                        .override(size, size)
                        .into(mIvImage);
            } else {
                mIvImage.setImageDrawable(new ColorDrawable(Color.parseColor("#FF2B2B2B")));
            }
        }

    }

}
