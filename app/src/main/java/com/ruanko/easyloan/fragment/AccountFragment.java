/**
 * Created by deserts on 17/7/25.
 */
package com.ruanko.easyloan.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.CountCallback;
import com.avos.avoscloud.SaveCallback;
import com.ruanko.easyloan.R;
import com.ruanko.easyloan.activity.MainActivity;
import com.ruanko.easyloan.activity.UserInfoActivity;
import com.ruanko.easyloan.data.OrderContract;
import com.ruanko.easyloan.data.UserContract;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.Arrays;

import static com.ruanko.easyloan.utilities.FileUtils.getFileBytes;
import static com.ruanko.easyloan.utilities.ImageUtils.getRoundedTransformation;

public class AccountFragment extends Fragment {
    private static final String TAG = "***Account fragment***";
    private NestedScrollView mRootView;
    private ImageView mUserHeadImage;
    private TextView mNameTextView;
    private TextView mInfoTextView;
    private TextView mRealNameTextView;
    private TextView mPhoneTextView;
    private TextView mSchoolTextView;
    private TextView mMailTextView;

    private TextView mLevelTextView;
    private TextView mInfoIntegrityTextView;

    private FloatingActionButton mFloatingActionButton;

    private Context context;

    private byte[] mImageBytes = null;

    private UserInfoChangedListener mUserInfoChangedListener;
    public final static String USER_INFO_CHANGED_LISTENER_KEY = "user_info_changed_key";

    public interface UserInfoChangedListener {
        public void OnUserInfoChanged();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        mUserInfoChangedListener = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.mRootView = (NestedScrollView) inflater.inflate(R.layout.fragment_account, container, false);
        initView();
        loadData();
        updateLevel();
        return mRootView;
    }

    private void initView() {
        mFloatingActionButton = (FloatingActionButton) getActivity().findViewById(R.id.fab_main_modify_info);
        mUserHeadImage = (ImageView) mRootView.findViewById(R.id.img_user_head);
        mNameTextView = (TextView) mRootView.findViewById(R.id.tv_real_name);
        mInfoTextView = (TextView) mRootView.findViewById(R.id.tv_account_mail);
        mRealNameTextView = (TextView) mRootView.findViewById(R.id.tv_name_account_center);
        mPhoneTextView = (TextView) mRootView.findViewById(R.id.tv_phone_account_center);
        mSchoolTextView = (TextView) mRootView.findViewById(R.id.tv_school_account_center);
        mMailTextView = (TextView) mRootView.findViewById(R.id.tv_mail_account_center);

        mLevelTextView = (TextView) mRootView.findViewById(R.id.tv_credit_account_center);
        mInfoIntegrityTextView = (TextView) mRootView.findViewById(R.id.tv_info_level_account_center);
        mUserHeadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle(getString(R.string.crop_image_title))
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setRequestedSize(400, 400)
                        .setAspectRatio(1, 1)
                        .start(context, AccountFragment.this);
            }
        });

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getContext(), UserInfoActivity.class),
                        UserInfoActivity.USER_INFO_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    private void loadData() {
        AVUser user = AVUser.getCurrentUser();
        if (user != null) {
            int account_score = 100;
            if (user.getEmail() == null || user.getEmail().length() == 0) {
                account_score -= 10;
            }
            if (user.getString(UserContract.UserEntry.COLUMN_REAL_NAME) == null || user.getString(UserContract.UserEntry.COLUMN_REAL_NAME).length() == 0) {
                account_score -= 10;
            }
            if (user.getString(UserContract.UserEntry.COLUMN_REAL_NAME) == null || user.getString(UserContract.UserEntry.COLUMN_HOME).length() == 0) {
                account_score -= 10;
            }
            if (user.getString(UserContract.UserEntry.COLUMN_REAL_NAME) == null || user.getString(UserContract.UserEntry.COLUMN_RELATIVE_NAME).length() == 0) {
                account_score -= 10;
            }
            if (user.getString(UserContract.UserEntry.COLUMN_REAL_NAME) == null || user.getString(UserContract.UserEntry.COLUMN_SCHOOL).length() == 0) {
                account_score -= 10;
            }

            if (user.getString(UserContract.UserEntry.COLUMN_REAL_NAME) == null || user.getString(UserContract.UserEntry.COLUMN_ID_CARD).length() == 0) {
                account_score -= 10;
            }

            mNameTextView.setText(user.getUsername());
            mInfoTextView.setText(user.getMobilePhoneNumber());
            mPhoneTextView.setText(user.getMobilePhoneNumber());
            mMailTextView.setText(user.getEmail());
            mSchoolTextView.setText(user.getString(UserContract.UserEntry.COLUMN_SCHOOL));
            mRealNameTextView.setText(user.getString(UserContract.UserEntry.COLUMN_REAL_NAME));

            AVFile imageFile = user.getAVFile(UserContract.UserEntry.COLUMN_AVATAR);
            if (imageFile != null) {
                Log.d(TAG, "loadData: get user image url: " + imageFile.getUrl());
                Picasso.with(context)
                        .load(imageFile.getUrl())
                        .fit()
                        .transform(getRoundedTransformation())
                        .into(mUserHeadImage);
            } else {
                Picasso.with(context)
                        .load(R.drawable.default_header)
                        .fit()
                        .transform(getRoundedTransformation())
                        .into(mUserHeadImage);
            }

            // update level
            int level = user.getInt(UserContract.UserEntry.COLUMN_LEVEL);
            if (user.getInt(UserContract.UserEntry.COLUMN_INFO_LEVEL) != account_score) {
                user.put(UserContract.UserEntry.COLUMN_INFO_LEVEL, account_score);
                level = (int) (account_score * 0.5 + 0.5 * user.getInt(UserContract.UserEntry.COLUMN_TRADE_LEVEL));
                user.put(UserContract.UserEntry.COLUMN_LEVEL, level);
                user.saveInBackground();
            }
            mInfoIntegrityTextView.setText(account_score + "%");
            String[] level_list = getResources().getStringArray(R.array.credit_levels);
            if (level < 50) {
                mLevelTextView.setText(level_list[0]);
            } else if (level < 100){
                mLevelTextView.setText(level_list[(int) ((level - 50) / 10)]);
            }
            else {
                mLevelTextView.setText(level_list[-1]);
            }

        } else {
            resetView();
        }
    }

    private void updateLevel(){
        AVUser user = AVUser.getCurrentUser();
        AVQuery<AVObject> avQuery1 = new AVQuery<>(OrderContract.OrderEntry.TABLE_NAME);
        avQuery1.whereEqualTo(OrderContract.OrderEntry.COLUMN_STATUS, OrderContract.Status.DONE);
        AVQuery<AVObject> avQuery2 = new AVQuery<>(OrderContract.OrderEntry.TABLE_NAME);
        avQuery2.whereEqualTo(OrderContract.OrderEntry.COLUMN_OWNER, user);
        AVQuery<AVObject> avQuery = AVQuery.and(Arrays.asList(avQuery2, avQuery1));
        avQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int i, AVException e) {
                if (e == null) {
                    final int finishedCount = i;
                    AVQuery<AVObject> avQuery1 = new AVQuery<>(OrderContract.OrderEntry.TABLE_NAME);
                    avQuery1.whereEqualTo(OrderContract.OrderEntry.COLUMN_STATUS, OrderContract.Status.OVERDUE);
                    AVQuery<AVObject> avQuery2 = new AVQuery<>(OrderContract.OrderEntry.TABLE_NAME);
                    avQuery2.whereEqualTo(OrderContract.OrderEntry.COLUMN_OWNER, AVUser.getCurrentUser());
                    AVQuery<AVObject> avQuery = AVQuery.and(Arrays.asList(avQuery2, avQuery1));
                    avQuery.countInBackground(new CountCallback() {
                        @Override
                        public void done(int i, AVException e) {
                            int overdueCount = i;
                            AVUser user1 = AVUser.getCurrentUser();
                            int trade_score = user1.getInt(UserContract.UserEntry.COLUMN_TRADE_LEVEL);
                            int new_trade_score = 50;
                            if (finishedCount < 10){
                                new_trade_score += finishedCount * 4;
                            }
                            else if (finishedCount < 20) {
                                new_trade_score += (finishedCount - 10) * 0.8 + 40;
                            }
                            else if (finishedCount < 30) {
                                new_trade_score += (finishedCount - 20) * 0.2 + 48;
                            }
                            else {
                                new_trade_score += 50;
                            }
                            new_trade_score = new_trade_score - overdueCount * 8;
                            if (new_trade_score != trade_score) {
                                user1.put(UserContract.UserEntry.COLUMN_TRADE_LEVEL, (int)new_trade_score);
                                user1.put(UserContract.UserEntry.COLUMN_LEVEL,
                                        (int)(new_trade_score * 0.5 + 0.5 * user1.getInt(UserContract.UserEntry.COLUMN_INFO_LEVEL)));
                            }
                        }

                    });
                }
            }
        });
    }

    private void resetView() {
        mNameTextView.setText("");
        mInfoTextView.setText("");
        mRealNameTextView.setText("");
        mPhoneTextView.setText("");
        mSchoolTextView.setText("");
        mMailTextView.setText("");
        mLevelTextView.setText("");
        mInfoIntegrityTextView.setText("");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            try {
                Log.d(TAG, "onActivityResult: user image uri: " + result.getUri());
                mImageBytes = getFileBytes(context.getContentResolver()
                        .openInputStream(result.getUri()));
                AVUser user = AVUser.getCurrentUser();
                if (user != null) {
                    user.put(UserContract.UserEntry.COLUMN_AVATAR, new AVFile(UserContract.UserEntry.COLUMN_AVATAR + ".jpg", mImageBytes));
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e == null) {
                                mUserInfoChangedListener.OnUserInfoChanged();
                                loadData();
                                Toast.makeText(context, getString(R.string.upload_done), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, getString(R.string.upload_fail), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == UserInfoActivity.USER_INFO_ACTIVITY_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            loadData();
        }
    }
}
