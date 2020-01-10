package app.elaphant.sdk.service.momentsclient.test;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import app.elaphant.sdk.service.momentsclient.MomentsClient;


public class SettingActivity extends AppCompatActivity {
    private static final String TAG = SettingActivity.class.getName();

    public static final String MY_MOMENTS = "my_moments";

    private TextView mPrivateText;
    private LinearLayout mPrivateLable;
    private MomentsItem mMyMoments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        String data = intent.getStringExtra(MY_MOMENTS);
        mMyMoments = new Gson().fromJson(data, MomentsItem.class);

        mPrivateText = findViewById(R.id.setting_text_priv);
        mPrivateText.setText(mMyMoments.mIsPrivate.toString());
        mPrivateLable = findViewById(R.id.setting_label_priv);
        mPrivateLable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPrivate();
            }
        });
    }


    private void setPrivate() {
        String text;
        if (mMyMoments.mIsPrivate) {
            text = "设置为公开？";
        } else {
            text = "设置为私有？";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int ret = new MomentsClient().setPrivate(mMyMoments.mDid, !mMyMoments.mIsPrivate);
                if (ret == 0) {
                    mPrivateText.setText("修改中");
                }

            }
        });
        builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
