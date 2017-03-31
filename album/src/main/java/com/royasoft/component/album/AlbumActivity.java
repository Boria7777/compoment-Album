/*
 * AUTHOR：Yan Zhenjie
 *
 * DESCRIPTION：create the File, and add the content.
 *
 * Copyright © ZhiMore. All Rights Reserved
 *
 */
package com.royasoft.component.album;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.royasoft.component.album.adapter.AlbumContentAdapter;
import com.royasoft.component.album.dialog.AlbumFolderDialog;
import com.royasoft.component.album.dialog.AlbumFolderDialogFragment;
import com.royasoft.component.album.dialog.AlbumPreviewDialog;
import com.royasoft.component.album.entity.AlbumFolder;
import com.royasoft.component.album.entity.AlbumImage;
import com.royasoft.component.album.impl.OnCompatCompoundCheckListener;
import com.royasoft.component.album.impl.OnCompatItemClickListener;
import com.royasoft.component.album.task.AlbumScanner;
import com.royasoft.component.album.task.Poster;
import com.royasoft.component.album.util.AlbumUtils;
import com.royasoft.component.album.util.DisplayUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>全局相册，选择图片入口。</p>
 * Created by Yan Zhenjie on 2016/10/17.
 */
public class AlbumActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_STORAGE = 200;
    private static final int PERMISSION_REQUEST_CAMERA = 201;

    private static final int ACTIVITY_REQUEST_CAMERA = 200;

    private static final String INSTANCE_CAMERA_FILE_PATH = "INSTANCE_CAMERA_FILE_PATH";

    private static ExecutorService sRunnableExecutor = Executors.newSingleThreadExecutor();

    private Toolbar mToolbar;
    private TextView ioscancelTxt;
    private TextView andcompleteTxt;
    private TextView androidpreviewTxt;
    private TextView previewTxt;
    private TextView sendTxt;
    private RelativeLayout ioscompleteLayout;
    private RelativeLayout andcompleteLayout;
    private RelativeLayout sendLayout;

    private Button mBtnSwitchFolder;
    private RecyclerView mRvContentList;
    private GridLayoutManager mGridLayoutManager;
    private AlbumContentAdapter mAlbumContentAdapter;

    private int mToolBarColor;
    private int statusColor;
    private int mAllowSelectCount;
    private int mCheckFolderIndex;
    private int showType;
    private int itemssize;
    public int originalType = 1;
    private ArrayList<AlbumFolder> mAlbumFolders;
    private ArrayList<AlbumImage> mCheckedImages = new ArrayList<>(1);
    private ArrayList<AlbumImage> mTempCheckedImages;

    private AlbumFolderDialog mAlbumFolderSelectedDialog;
    private AlbumPreviewDialog mAlbumPreviewDialog;
    private AlbumFolderDialogFragment mAlbumFolderFragment;
    private String mCameraFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtils.initScreen(this);
        setContentView(R.layout.album_activity_album);

        if (savedInstanceState != null) {
            mCameraFilePath = savedInstanceState.getString(INSTANCE_CAMERA_FILE_PATH);
        }

        Intent intent = getIntent();
        mToolBarColor = intent.getIntExtra(Album.KEY_INPUT_TOOLBAR_COLOR, ResourcesCompat.getColor(getResources(), R.color.albumColorPrimary, null));
        statusColor = intent.getIntExtra(Album.KEY_INPUT_STATUS_COLOR, ResourcesCompat.getColor(getResources(), R.color.albumColorPrimaryDark, null));
        mAllowSelectCount = intent.getIntExtra(Album.KEY_INPUT_LIMIT_COUNT, Integer.MAX_VALUE);
        showType = intent.getIntExtra(Album.KEY_INPUT_SHOW_TYPE, Album.IOSTYPE);
        itemssize = intent.getIntExtra(Album.KEY_INPUT_ITEM_SIZE, Album.ITEMSIZE);
        int normalColor = ContextCompat.getColor(this, R.color.albumWhiteGray);

        initializeMain(statusColor);
        initializeContent(normalColor);
        setPreviewCount(0);
        scanImages();
        if (showType == Album.IOSTYPE) {
            findViewById(R.id.iosbottom).setVisibility(View.VISIBLE);
            findViewById(R.id.ioscomplete_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.androidbottom).setVisibility(View.GONE);
            findViewById(R.id.andcomplete_layout).setVisibility(View.GONE);
        } else if (showType == Album.ANDROIDTYPE) {
            findViewById(R.id.iosbottom).setVisibility(View.GONE);
            findViewById(R.id.ioscomplete_layout).setVisibility(View.GONE);
            findViewById(R.id.androidbottom).setVisibility(View.VISIBLE);
            findViewById(R.id.andcomplete_layout).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Initialize up.
     */
    private void initializeMain(int statusColor) {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        ioscancelTxt = (TextView) findViewById(R.id.activitycancel);
        ioscancelTxt.setOnClickListener(this);
        andcompleteTxt = (TextView) findViewById(R.id.andcomplete);
        previewTxt = (TextView) findViewById(R.id.previewtextview);
        sendTxt = (TextView) findViewById(R.id.sendtext);
        ioscompleteLayout = (RelativeLayout) findViewById(R.id.ioscomplete_layout);
        andcompleteLayout = (RelativeLayout) findViewById(R.id.andcomplete_layout);
        andcompleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toResult(false);
            }
        });
        sendLayout = (RelativeLayout) findViewById(R.id.sendLayout);
        androidpreviewTxt = (TextView) findViewById(R.id.androidpreview);
        mBtnSwitchFolder = (Button) findViewById(R.id.btn_switch_dir);
        mRvContentList = (RecyclerView) findViewById(R.id.rv_content_list);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        previewTxt.setOnClickListener(mPreviewClick);
        androidpreviewTxt.setOnClickListener(mPreviewClick);
        mBtnSwitchFolder.setOnClickListener(androidSwitchDirClick);

        setStatusBarColor(statusColor);
        mToolbar.setBackgroundColor(mToolBarColor);
    }

    private void setStatusBarColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            final Window window = getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);
                window.setNavigationBarColor(ContextCompat.getColor(this, R.color.albumPrimaryBlack));
            }
        }
    }

    /**
     * Initialize content.
     */
    private void initializeContent(int normalColor) {
        mRvContentList.setHasFixedSize(true);
        mGridLayoutManager = new GridLayoutManager(this, itemssize);
        mRvContentList.setLayoutManager(mGridLayoutManager);
        mRvContentList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                if (position % 2 == 0)
                    outRect.set(2, 2, 2, 0);
                else
                    outRect.set(0, 2, 2, 0);
            }
        });

        mAlbumContentAdapter = new AlbumContentAdapter(normalColor, mToolBarColor, itemssize);
        mAlbumContentAdapter.setAddPhotoClickListener(mAddPhotoListener);
        mAlbumContentAdapter.setItemClickListener(new OnCompatItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ArrayList<AlbumImage> albumImages = mAlbumFolders.get(mCheckFolderIndex).getPhotos();
                dismissPreviewDialog();
                mAlbumPreviewDialog = new AlbumPreviewDialog(AlbumActivity.this, mToolBarColor, albumImages, mPreviewFolderCheckListener, position, contentHeight, showType);
                mAlbumPreviewDialog.show();
            }
        });
        mAlbumContentAdapter.setOnCheckListener(mContentCheckListener);
        mRvContentList.setAdapter(mAlbumContentAdapter);
    }

    /**
     * 扫描有照片的文件夹。
     */
    private void scanImages() {
        if (Build.VERSION.SDK_INT >= 23) {
            int permissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionResult == PackageManager.PERMISSION_GRANTED) {
                sRunnableExecutor.execute(scanner);
            } else if (permissionResult == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
            }
        } else {
            sRunnableExecutor.execute(scanner);
        }
    }

    /**
     * 拍照点击监听。
     */
    private View.OnClickListener mAddPhotoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= 23) {
                int permissionResult = ContextCompat.checkSelfPermission(AlbumActivity.this, Manifest.permission.CAMERA);
                if (permissionResult == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else if (permissionResult == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(AlbumActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
                }
            } else {
                startCamera();
            }
        }
    };

    /**
     * 启动相机拍照。
     */
    private void startCamera() {
        String outFileFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        String outFilePath = AlbumUtils.getNowDateTime("yyyyMMdd_HHmmssSSS") + ".jpg";
        File file = new File(outFileFolder, outFilePath);
        mCameraFilePath = file.getAbsolutePath();
        AlbumUtils.startCamera(this, ACTIVITY_REQUEST_CAMERA, file);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putString(INSTANCE_CAMERA_FILE_PATH, mCameraFilePath);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_REQUEST_CAMERA: {
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent();
                    ArrayList<String> pathList = new ArrayList<>();
                    pathList.add(mCameraFilePath);
                    intent.putStringArrayListExtra(Album.KEY_OUTPUT_IMAGE_PATH_LIST, pathList);
                    setResult(RESULT_OK, intent);
                    super.finish();
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_STORAGE: {
                int permissionResult = grantResults[0];
                if (permissionResult == PackageManager.PERMISSION_GRANTED) {
                    sRunnableExecutor.execute(scanner);
                } else {
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle(R.string.album_dialog_permission_failed)
                            .setMessage(R.string.album_permission_storage_failed_hint)
                            .setPositiveButton(R.string.album_dialog_sure, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    toResult(true);
                                }
                            })
                            .show();
                }
                break;
            }
            case PERMISSION_REQUEST_CAMERA: {
                int permissionResult = grantResults[0];
                if (permissionResult == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.album_dialog_permission_failed)
                            .setMessage(R.string.album_permission_camera_failed_hint)
                            .setPositiveButton(R.string.album_dialog_sure, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    /**
     * 预览文件夹时某个item的选中监听。
     */
    private OnCompatCompoundCheckListener mPreviewFolderCheckListener = new OnCompatCompoundCheckListener() {
        @Override
        public void onCheck(CompoundButton compoundButton, int position, boolean isChecked) {
            mContentCheckListener.onCheck(compoundButton, position, isChecked);
            mAlbumContentAdapter.notifyItemChangedCompat(position);
        }
    };

    /**
     * 预览已选中的大图的监听。
     */
    private OnCompatCompoundCheckListener mPreviewCheckedImageCheckListener = new OnCompatCompoundCheckListener() {
        @Override
        public void onCheck(CompoundButton compoundButton, int position, boolean isChecked) {
            AlbumImage albumImage = mTempCheckedImages.get(position);
            albumImage.setChecked(isChecked);
            int i = mAlbumFolders.get(mCheckFolderIndex).getPhotos().indexOf(albumImage);
            if (i != -1) mAlbumContentAdapter.notifyItemChangedCompat(i);
            if (!isChecked)
                mCheckedImages.remove(albumImage);
        }
    };

    /**
     * 选中监听。
     */
    private OnCompatCompoundCheckListener mContentCheckListener = new OnCompatCompoundCheckListener() {
        @Override
        public void onCheck(CompoundButton compoundButton, int position, boolean isChecked) {
            AlbumImage albumImage = mAlbumFolders.get(mCheckFolderIndex).getPhotos().get(position);
            albumImage.setChecked(isChecked);
            if (isChecked) {
                if (!mCheckedImages.contains(albumImage))
                    mCheckedImages.add(albumImage);
            } else {
                mCheckedImages.remove(albumImage);
            }

            int hasCheckSize = mCheckedImages.size();
            if (hasCheckSize > mAllowSelectCount) {
                String hint = getString(R.string.album_check_limit);
                Toast.makeText(AlbumActivity.this, String.format(Locale.getDefault(), hint, mAllowSelectCount), Toast.LENGTH_LONG).show();
                mCheckedImages.remove(albumImage);
                compoundButton.setChecked(false);
                albumImage.setChecked(false);
            } else {
                setPreviewCount(hasCheckSize);
            }
        }
    };

    /**
     * 设置选中的图片数。
     *
     * @param count 数字。
     */
    public void setPreviewCount(int count) {
        if (showType == Album.IOSTYPE) {
            if (count == 0) {
                previewTxt.setText("预览");
                findViewById(R.id.sendNum).setVisibility(View.INVISIBLE);
            } else if (count > 0) {
                previewTxt.setText("预览" + " (" + count + ")");
                androidpreviewTxt.setText("预览" + " (" + count + ")");
                sendTxt.setText("" + count + "");
                findViewById(R.id.sendNum).setVisibility(View.VISIBLE);
            }
        } else if (showType == Album.ANDROIDTYPE) {
            if (count == 0) {
                androidpreviewTxt.setText("预览");
                andcompleteTxt.setText("完成");
                andcompleteLayout.setBackgroundResource(R.drawable.album_btn_unpressed);
            } else if (count > 0) {
                androidpreviewTxt.setText("预览" + " (" + count + ")");
                andcompleteTxt.setText("完成" + "(" + count + "/" + mAllowSelectCount + ")");
                andcompleteLayout.setBackgroundResource(R.drawable.album_btn_pressed);
            }
        }
    }

    /**
     * 切换文件夹按钮被点击。
     */
    private View.OnClickListener mSwitchDirClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mAlbumFolderFragment == null) {
                mAlbumFolderFragment = AlbumFolderDialogFragment.getInstance(mAlbumFolders, mToolBarColor, statusColor);
                mAlbumFolderFragment.setItemclickListener(new OnCompatItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        showAlbum(position);
                    }
                });
            }
            mAlbumFolderFragment.show(getFragmentManager(), null);
        }
    };

    /**
     * 切换文件夹按钮被点击。
     */
    private View.OnClickListener androidSwitchDirClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mAlbumFolderSelectedDialog == null) {
                mAlbumFolderSelectedDialog = new AlbumFolderDialog(AlbumActivity.this, mToolBarColor, mAlbumFolders, new OnCompatItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        showAlbum(position);
                    }
                });
            }
            if (!mAlbumFolderSelectedDialog.isShowing())
                mAlbumFolderSelectedDialog.show();
        }
    };


    /**
     * 预览按钮被点击。
     */
    private View.OnClickListener mPreviewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCheckedImages.size() == 0)
                return;
            mTempCheckedImages = new ArrayList<>(mCheckedImages);
            Collections.copy(mTempCheckedImages, mCheckedImages);

            dismissPreviewDialog();

            mAlbumPreviewDialog = new AlbumPreviewDialog(AlbumActivity.this, mToolBarColor, mTempCheckedImages, mPreviewCheckedImageCheckListener, 0, contentHeight, showType);
            mAlbumPreviewDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mTempCheckedImages != null) {
                        mTempCheckedImages.clear();
                        mTempCheckedImages = null;
                    }
                }
            });
            mAlbumPreviewDialog.show();
        }
    };

    /**
     * 关闭预览窗口。
     */

    private void dismissPreviewDialog() {
        if (mAlbumPreviewDialog != null && mAlbumPreviewDialog.isShowing())
            mAlbumPreviewDialog.dismiss();
    }

    /**
     * 显示某个文件夹的图片。
     *
     * @param index 选中的文件夹的item。
     */
    private void showAlbum(int index) {
        mCheckFolderIndex = index;
        AlbumFolder albumFolder = mAlbumFolders.get(index);
        mBtnSwitchFolder.setText(albumFolder.getName());
        mToolbar.setTitle(albumFolder.getName());
        mAlbumContentAdapter.notifyDataSetChanged(albumFolder.getPhotos());
        if (showType==Album.IOSTYPE){
            mGridLayoutManager.scrollToPosition(albumFolder.getPhotos().size() - 1);
        }else if (showType==Album.ANDROIDTYPE){
            mGridLayoutManager.scrollToPosition(0);
        }
    }

    /**
     * 选择完成或者取消。
     */
    public void toResult(boolean cancel) {
        if (cancel) {
            setResult(RESULT_CANCELED);
            super.finish();
        } else {
            int allSize = mAlbumFolders.get(0).getPhotos().size();
            int checkSize = mCheckedImages.size();
            if (allSize > 0 && checkSize == 0) {
                Toast.makeText(this, R.string.album_check_little, Toast.LENGTH_LONG).show();
            } else if (checkSize == 0) {
                setResult(RESULT_CANCELED);
                super.finish();
            } else {
                Intent intent = new Intent();
                ArrayList<String> pathList = new ArrayList<>();
                for (AlbumImage albumImage : mCheckedImages) {
                    pathList.add(albumImage.getPath());
                }
                intent.putStringArrayListExtra(Album.KEY_OUTPUT_IMAGE_PATH_LIST, pathList);
                setResult(RESULT_OK, intent);
                super.finish();
            }
        }
    }

    /**
     * 拿到已经选择的大小。
     *
     * @return 大小。
     */
    public int getCheckedImagesSize() {
        return mCheckedImages.size();
    }

    /**
     * 拿到允许选择的数量。
     *
     * @return 数量int。
     */
    public int getAllowCheckCount() {
        return mAllowSelectCount;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (showType == Album.IOSTYPE) {

                if (mAlbumFolderFragment == null) {
                    mAlbumFolderFragment = AlbumFolderDialogFragment.getInstance(mAlbumFolders, mToolBarColor, statusColor);
                    mAlbumFolderFragment.setItemclickListener(new OnCompatItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            showAlbum(position);
                        }
                    });
                }
                mAlbumFolderFragment.show(getFragmentManager(), null);
            } else if (showType == Album.ANDROIDTYPE) {
                toResult(true);
            }

        } else if (itemId == R.id.menu_gallery_finish) {
            toResult(false);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        toResult(true);
    }

    /**
     * Init ui.
     */
    private Runnable initialize = new Runnable() {
        @Override
        public void run() {
            if (!AlbumActivity.this.isFinishing()) {
                showAlbum(0);
            } else {
                mAlbumFolders.clear();
                mAlbumFolders = null;
            }
        }
    };

    /**
     * Scan image.
     */
    private Runnable scanner = new Runnable() {
        @Override
        public void run() {
            mAlbumFolders = AlbumScanner.getInstance().getPhotoAlbum(AlbumActivity.this,showType);
            Poster.getInstance().post(initialize);
        }
    };

    private boolean initializeHeight;
    private int contentHeight;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !initializeHeight) {
            initializeHeight = true;
            View view = findViewById(R.id.coordinator_layout);
            contentHeight = view.getMeasuredHeight();
        }
    }

    @Override
    protected void onDestroy() {
        if (mAlbumFolderSelectedDialog != null && mAlbumFolderSelectedDialog.isShowing())
            mAlbumFolderSelectedDialog.behaviorHide();
        dismissPreviewDialog();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.sendLayout) {
            toResult(false);
        } else if (id == R.id.activitycancel) {
            toResult(true);
        }
    }
}