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
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.royasoft.component.album.R;
import com.royasoft.component.album.entity.AlbumImage;
import com.royasoft.component.album.impl.OnCompatCompoundCheckListener;
import com.royasoft.component.album.impl.OnCompatItemClickListener;
import com.royasoft.component.album.util.DisplayUtils;
import com.royasoft.component.album.util.SelectorUtils;

import java.util.List;


/***********************************************************************************************
 * 类名称:   <p>相册文件夹内容显示适配器。</p>
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

public class AlbumContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_BUTTON = 1;
    private static final int TYPE_IMAGE = 2;

    private LayoutInflater mLayoutInflater;

    private static Context ctx = null;

    private ColorStateList mColorStateList;

    private List<AlbumImage> mAlbumImages;

    private View.OnClickListener mAddPhotoClickListener;

    private OnCompatItemClickListener mItemClickListener;

    private OnCompatCompoundCheckListener mOnCompatCheckListener;

    private static int size = (DisplayUtils.screenWidth - 6);
    private static int itemsize = 4;

    public AlbumContentAdapter(int normalColor, int checkColor, int itemsize) {
        this.mColorStateList = SelectorUtils.createColorStateList(normalColor, checkColor);
        this.itemsize = itemsize;
    }

    private void initLayoutInflater(Context context) {
        if (ctx == null) {
            ctx = context;
        }
        if (mLayoutInflater == null)
            mLayoutInflater = LayoutInflater.from(context);
    }

    public void notifyDataSetChanged(List<AlbumImage> albumImages) {
        this.mAlbumImages = albumImages;
        super.notifyDataSetChanged();
    }

    public void notifyItemChangedCompat(int position) {
        super.notifyItemChanged(position);
    }

    public void setAddPhotoClickListener(View.OnClickListener addPhotoClickListener) {
        this.mAddPhotoClickListener = addPhotoClickListener;
    }

    public void setItemClickListener(OnCompatItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    @Override
    public int getItemCount() {
        return mAlbumImages == null ? 0 : mAlbumImages.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
//            case 0:
//                return TYPE_BUTTON;
            default:
                return TYPE_IMAGE;
        }
    }

    public void setOnCheckListener(OnCompatCompoundCheckListener checkListener) {
        this.mOnCompatCheckListener = checkListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        initLayoutInflater(parent.getContext());
        switch (viewType) {
            case TYPE_BUTTON:
                return new GalleryContentButtonHolder(mLayoutInflater.inflate(R.layout.album_item_album_content_button, parent, false));
            default:
                return new GalleryContentImageHolder(mLayoutInflater.inflate(R.layout.album_item_album_content_image, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_BUTTON: {
                GalleryContentButtonHolder imageHolder = (GalleryContentButtonHolder) holder;
                imageHolder.setAddPhotoClickListener(mAddPhotoClickListener);
                break;
            }
            default: {
                int adapterPosition = holder.getAdapterPosition();
                AlbumImage albumImage = mAlbumImages.get(adapterPosition);
                GalleryContentImageHolder imageHolder = (GalleryContentImageHolder) holder;
                imageHolder.setButtonTint(mColorStateList);
                imageHolder.setData(albumImage);
                imageHolder.setItemClickListener(mItemClickListener);
                imageHolder.setOnCompatCheckListener(mOnCompatCheckListener);
                break;
            }
        }
    }

    private static class GalleryContentImageHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        private ImageView mIvImage;
        private AppCompatCheckBox mCompatCheckBox;

        private OnCompatItemClickListener mItemClickListener;
        private OnCompatCompoundCheckListener mOnCompatCheckListener;

        public GalleryContentImageHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.getLayoutParams().width = size/itemsize;
            itemView.getLayoutParams().height = size/itemsize;
            itemView.requestLayout();

            mIvImage = (ImageView) itemView.findViewById(R.id.iv_album_content_image);
            mCompatCheckBox = (AppCompatCheckBox) itemView.findViewById(R.id.cb_album_check);
            mCompatCheckBox.setOnCheckedChangeListener(this);
        }

        public void setButtonTint(ColorStateList colorStateList) {
//            mCompatCheckBox.setSupportButtonTintList(colorStateList);
        }

        public void setData(AlbumImage albumImage) {
            Glide.with(ctx.getApplicationContext())
                    .load(albumImage.getPath())
                    .override(size/itemsize, size/itemsize)
                    .into(mIvImage);
            mCompatCheckBox.setChecked(albumImage.isChecked());
        }

        public void setItemClickListener(OnCompatItemClickListener mItemClickListener) {
            this.mItemClickListener = mItemClickListener;
        }

        public void setOnCompatCheckListener(OnCompatCompoundCheckListener mOnCompatCheckListener) {
            this.mOnCompatCheckListener = mOnCompatCheckListener;
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null)
                mItemClickListener.onItemClick(v, getAdapterPosition());
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mOnCompatCheckListener != null)
                mOnCompatCheckListener.onCheck(buttonView, getAdapterPosition(), isChecked);
        }
    }

    private static class GalleryContentButtonHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View.OnClickListener mClickListener;

        public GalleryContentButtonHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.getLayoutParams().width = size/itemsize;
            itemView.getLayoutParams().height = size/itemsize;
            itemView.requestLayout();
        }

        public void setAddPhotoClickListener(View.OnClickListener addPhotoClickListener) {
            this.mClickListener = addPhotoClickListener;
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null)
                mClickListener.onClick(v);
        }
    }
}
