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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.royasoft.component.album.entity.AlbumImage;
import com.royasoft.component.album.util.DisplayUtils;
import com.royasoft.component.album.widget.PhotoViewAttacher;

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
public class IosPreviewAdapter extends PagerAdapter {

    private List<AlbumImage> mAlbumImages;
    private int contentHeight;
    private Context ctx;

    public IosPreviewAdapter(List<AlbumImage> mAlbumImages, int contentHeight, Context context) {
        this.mAlbumImages = mAlbumImages;
        this.contentHeight = contentHeight;
        this.ctx = context;
    }

    @Override
    public int getCount() {
        return mAlbumImages == null ? 0 : mAlbumImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final ImageView imageView = new ImageView(container.getContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        container.addView(imageView);
        final PhotoViewAttacher attacher = new PhotoViewAttacher(imageView);
        Glide.with(ctx.getApplicationContext())
                .load(mAlbumImages.get(position).getPath())
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        Bitmap bitmap = Bitmap
                                .createBitmap(
                                        resource.getIntrinsicWidth(),
                                        resource.getIntrinsicHeight(),
                                        resource.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                                : Bitmap.Config.RGB_565);
                        Canvas canvas = new Canvas(bitmap);
                        resource.setBounds(0, 0, resource.getIntrinsicWidth(),
                                resource.getIntrinsicHeight());
                        resource.draw(canvas);

                        imageView.setImageBitmap(bitmap);
                        attacher.update();

                        int height = bitmap.getHeight();
                        int width = bitmap.getWidth();
                        int bitmapSize = height / width;
                        int contentSize = contentHeight / DisplayUtils.screenWidth;

                        if (height > width && bitmapSize >= contentSize) {
                            attacher.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        } else {
                            attacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        }

                        return true;
                    }
                })
                .override(DisplayUtils.screenWidth, contentHeight)
                .into(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(((View) object));
    }
}
