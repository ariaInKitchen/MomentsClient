package app.elaphant.sdk.service.momentsclient.test;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blikoon.qrcodescanner.QrCodeActivity;
import com.google.gson.Gson;

import org.elastos.sdk.elephantwallet.contact.Contact;
import org.elastos.sdk.elephantwallet.contact.Utils;
import org.elastos.sdk.keypair.ElastosKeypair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import app.elaphant.sdk.peernode.PeerNode;
import app.elaphant.sdk.peernode.PeerNodeListener;
import app.elaphant.sdk.service.momentsclient.MomentsClient;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MomentsTest";

    private static final int REQUEST_CODE_QR_SCAN = 101;

    private static final String mnemonic = "ability cloth cannon buddy together theme uniform erase fossil meadow top pumpkin";
    private static final String mPrivateKey = "b8e923f4e5c5a3c704bcc02a90ee0e4fa34a5b8f0dd1de1be4eb2c37ffe8e3ea";
    private static final String mPublicKey = "021e53dc2b8af1548175cba357ae321096065f8d49e3935607bc8844c157bb0859";


    private PeerNode mPeerNode;
    private MomentsClient mMomentsClient;

    private DatabaseHelper mDbHelper = DatabaseHelper.getInstance(this);

    private MomentsItem mMyMoments;
    private List<MomentsItem> mFollowList;

    private TextView mMomentsText;
    private TextView mStatusText;
    private ListView mFollowListView;
    private Adapter mAdapter;

    private String mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scan("create");
            }
        });

        InitPeerNode();
        InitMoments();
        InitView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPeerNode.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_follow) {
            scan("follow");
            return true;
        } else if (id == R.id.action_setting) {
            if (mMyMoments != null) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                intent.putExtra(SettingActivity.MY_MOMENTS, new Gson().toJson(mMyMoments));
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "还未创建朋友圈", Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void InitPeerNode() {
        mPeerNode = PeerNode.getInstance(getFilesDir().getAbsolutePath(),
                Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

        mPeerNode.setListener(new PeerNodeListener.Listener() {
            @Override
            public byte[] onAcquire(Contact.Listener.AcquireArgs request) {
                byte[] response = null;
                switch (request.type) {
                    case PublicKey:
                        response = mPublicKey.getBytes();
                        break;
                    case EncryptData:
                        response = request.data;
                        break;
                    case DecryptData:
                        response = request.data;
                        break;
                    case DidPropAppId:
                        break;
                    case DidAgentAuthHeader:
                        response = getAgentAuthHeader();
                        break;
                    case SignData:
                        response = signData(request.data);
                        break;
                    default:
                        throw new RuntimeException("Unprocessed request: " + request);
                }
                return response;
            }

            @Override
            public void onError(int errCode, String errStr, String ext) {
                Log.e(TAG, "Contact error: " + errCode + " " + errStr);
            }
        });

        mPeerNode.start();
    }

    private byte[] getAgentAuthHeader() {
        String appid = "org.elastos.debug.didplugin";
        String appkey = "b2gvzUM79yLhCbbGNWCuhSsGdqYhA7sS";
        long timestamp = System.currentTimeMillis();
        String auth = Utils.getMd5Sum(appkey + timestamp);
        String headerValue = "id=" + appid + ";time=" + timestamp + ";auth=" + auth;
        Log.i(TAG, "getAgentAuthHeader() headerValue=" + headerValue);

        return headerValue.getBytes();
    }

    private byte[] signData(byte[] data) {

        ElastosKeypair.Data originData = new ElastosKeypair.Data();
        originData.buf = data;

        ElastosKeypair.Data signedData = new ElastosKeypair.Data();

        int signedSize = ElastosKeypair.sign(mPrivateKey, originData, originData.buf.length, signedData);
        if(signedSize <= 0) {
            return null;
        }

        return signedData.buf;
    }

    private void InitMoments() {
        mMomentsClient = new MomentsClient();
        mMomentsClient.setListener(new MomentsClient.Listener() {
            @Override
            public void onPushData(String did, int type, JSONArray array) {

            }

            @Override
            public void onPublishResult(String did, long time, int result) {

            }

            @Override
            public void onSettingResult(String did, String type, String value, int result) {
                handleSettingResult(did, type, value, result);
            }

            @Override
            public void onGetData(String did, JSONObject content) {

            }

            @Override
            public void onGetDataList(String did, JSONArray array) {

            }

            @Override
            public void onGetSetting(String did, String type, String value) {

            }

            @Override
            public void onDeleteResult(String did, int id, int result) {

            }

            @Override
            public void onClearResult(String did, int result) {

            }

            @Override
            public void onFriendRequest(String did, String friendCode, String summary) {
                handleRequest(did, friendCode, summary);
            }

            @Override
            public void onGetFollowList(String did, JSONArray array) {

            }

            @Override
            public void onNewFollow(String did, String friendCode) {
                handleNewFollow(did, friendCode);
            }

            @Override
            public void onStatusChanged(String did, Contact.Status status) {
                handleStatusChanged(did, status);
            }
        });
    }

    private void InitView() {
        mMomentsText = findViewById(R.id.my_moments);
        mStatusText = findViewById(R.id.my_moments_status);

        mMyMoments = mDbHelper.getMyMoments();
        if (mMyMoments != null) {
            mMomentsText.setText(mMyMoments.mDid);
            mStatusText.setText(mMyMoments.mStatus);
        } else {
            mMomentsText.setText("none");
        }

        mFollowListView = findViewById(R.id.follow_list);
        mFollowList = mDbHelper.getFollowList();
        mAdapter = new Adapter(this, R.layout.moments_item, mFollowList);
        mFollowListView.setAdapter(mAdapter);

    }

    private void scan(String type) {
        mType = type;
        Intent i = new Intent(MainActivity.this, QrCodeActivity.class);
        startActivityForResult(i, REQUEST_CODE_QR_SCAN);
    }

    private void createMoments(String did) {
        Log.d(TAG, "create moments " + did);
        int ret = mMomentsClient.create(did);
        if (ret != 0) {
            Log.e(TAG, "create moments failed " + ret);
            return;
        }

        if (mMyMoments != null) {
            mDbHelper.removeMoments(mMyMoments.mDid);
        }

        mMyMoments = new MomentsItem();
        mMyMoments.mDid = did;
        mMyMoments.mStatus = "waiting";
        mMyMoments.mIsOwner = true;
        mMyMoments.mIsPrivate = false;

        mDbHelper.insertMoments(mMyMoments);
    }

    private void followMoments(String did) {
        int ret = mMomentsClient.follow(did);
        if (ret != 0) {
            Log.e(TAG, "follow moments failed " + ret);
            return;
        }

        MomentsItem item = new MomentsItem();
        item.mDid = did;
        item.mStatus = "waiting";
        item.mIsOwner = false;
        item.mIsPrivate = false;

        mDbHelper.insertMoments(item);
        mAdapter.add(item);
        mAdapter.notifyDataSetChanged();
    }

    private void handleStatusChanged(final String did, final Contact.Status status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mMyMoments != null && mMyMoments.mDid.equals(did)) {
                    if (mMyMoments.mStatus.equals("waiting")) {
                        mDbHelper.updateMomentsStatus(did, status.toString());
                    }
                    mMyMoments.mStatus = status.toString();
                    mStatusText.setText(mMyMoments.mStatus);
                } else {
                    for (MomentsItem item : mFollowList) {
                        if (!item.mDid.equals(did)) continue;
                        if (item.mStatus.equals("waiting")) {
                            mDbHelper.updateMomentsStatus(did, status.toString());
                        }
                        item.mStatus = status.toString();
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
    }

    private void handleRequest(final String did, final String friendCode, final String summary) {
        if (mMyMoments == null || !did.equals(mMyMoments.mDid)) {
            Log.e(TAG, "Request from moments " + did + " not mine");
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("请求关注朋友圈");
                builder.setMessage(friendCode + " " + summary);
                builder.setNegativeButton("忽略", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("接受", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mMomentsClient.acceptFriend(did, friendCode);
                    }
                });
                builder.create().show();
            }
        });
    }

    private void handleSettingResult(String did, String type, String value, int result) {
        if (mMyMoments == null || !did.equals(mMyMoments.mDid)) {
            Log.e(TAG, "Request from moments " + did + " not mine");
            return;
        }

        if (result != 0) {
            Log.e(TAG, "set " + type + " " + value + " failed " + result);
            return;
        }

        if (!type.equals("access")) return;
        mMyMoments.mIsPrivate = Boolean.valueOf(value);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "set private " + mMyMoments.mIsPrivate, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleNewFollow(String did, final String friendCode) {
        if (mMyMoments == null || !did.equals(mMyMoments.mDid)) {
            Log.e(TAG, "Request from moments " + did + " not mine");
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "新的朋友 " + friendCode + " 关注了你", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            Log.d(TAG,"COULD NOT GET A GOOD RESULT.");
            if(data == null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
            if (result != null) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Scan Error");
                alertDialog.setMessage("QR Code could not be scanned");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            return;
        }

        if (requestCode == REQUEST_CODE_QR_SCAN) {
            if(data==null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
            Log.d(TAG,"Have scan result in your app activity :"+ result);
            if (result.isEmpty()) return;
            if (mType.equals("create")) {
                createMoments(result);
            } else if (mType.equals("follow")) {
                followMoments(result);
            }
        }
    }
}
