package app.elaphant.sdk.service.momentsclient.test;

import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.TextView;

import org.elastos.sdk.elephantwallet.contact.Contact;
import org.elastos.sdk.elephantwallet.contact.Utils;
import org.elastos.sdk.keypair.ElastosKeypair;

import app.elaphant.sdk.peernode.PeerNode;
import app.elaphant.sdk.peernode.PeerNodeListener;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MomentsTest";

    private static final String mnemonic = "ability cloth cannon buddy together theme uniform erase fossil meadow top pumpkin";
    private static final String mPrivateKey = "b8e923f4e5c5a3c704bcc02a90ee0e4fa34a5b8f0dd1de1be4eb2c37ffe8e3ea";
    private static final String mPublicKey = "021e53dc2b8af1548175cba357ae321096065f8d49e3935607bc8844c157bb0859";


    private PeerNode mPeerNode;

    private String mMyMoments;
    private String mMyMomentsStatus;

    private TextView mMomentsText;
    private TextView mStatusText;

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
                create();
            }
        });

        InitPeerNode();
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
            follow();
            return true;
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

    private void create() {
        showDailog("create");
    }

    private void follow() {
        showDailog("follow");
    }

    private void showDailog(final String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("输入DID");

        final EditText edit = new EditText(this);
        builder.setView(edit);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = edit.getText().toString();
                if (text.isEmpty()) return;
                dialog.dismiss();
                if (type.equals("create")) {
                    createMoments(text);
                } else {
                    followMoments(text);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void createMoments(String did) {

    }

    private void followMoments(String did) {

    }
}
