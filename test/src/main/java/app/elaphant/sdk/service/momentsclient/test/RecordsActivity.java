package app.elaphant.sdk.service.momentsclient.test;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;


import java.util.ArrayList;
import java.util.List;

import app.elaphant.sdk.service.momentsclient.MomentsClient;

public class RecordsActivity extends AppCompatActivity {
    private static final String TAG = RecordsActivity.class.getName();

    public static final String MOMENTS_ITEM = "moments_item";

    public static final int REQUEST_RECORD_LIST = 1001;
    public static final int RESULT_UNFOLLOW = 200;
    public static final String UNFOLLOW_DID = "unFollow_did";

    private MomentsItem mItem;
    private DatabaseHelper mDbHelper = DatabaseHelper.getInstance(this);

    private RecordAdapter mAdapter;
    private MomentsClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
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
        String itemStr = intent.getStringExtra(MOMENTS_ITEM);
        if (!itemStr.isEmpty()) {
            mItem = new Gson().fromJson(itemStr, MomentsItem.class);
        }

        mClient = new MomentsClient();

        initView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mItem == null) return false;

        if (mItem.mIsOwner) {
            getMenuInflater().inflate(R.menu.menu_record, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_follow_record, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            publish();
            return true;
        } else if (id == R.id.action_clear) {
            clear();
        } else if (id == R.id.action_unFollow) {
            unFollow();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        List<Record> list = new ArrayList<>();
        if (mItem != null) {
            list = mDbHelper.getRecordList(mItem.mDid);
            for (Record r : list) {
                Log.d(TAG, new Gson().toJson(r, Record.class));
            }
        }

        ListView listView = findViewById(R.id.record_list);
        mAdapter = new RecordAdapter(this, R.layout.record_item, list);
        listView.setAdapter(mAdapter);

        if (mItem != null && mItem.mIsOwner) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    itemClick(position);
                }
            });
        }
    }

    private void publish() {
        final EditText edit = new EditText(this);
        edit.setBackgroundResource(R.drawable.edit_border);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请输入要发布的内容");
        builder.setView(edit);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                publishContent(edit.getText().toString());
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

    private void publishContent(String content) {
        if (content.isEmpty()) return;

        Record record = new Record();
        record.did = mItem.mDid;
        record.type = 1;
        record.content = content;
        record.time = System.currentTimeMillis();
        record.files = "";
        record.access = "";
        int ret = mClient.publishText(mItem.mDid, content, record.time, "");
        if (ret != 0) {
            Log.e(TAG, "public moment failed " + ret);
            return;
        }


        long id = mDbHelper.insertRecord(record);
        if (id < 0) {
            Log.e(TAG, "insert into database failed " + id);
            return;
        }

        mAdapter.insert(record, 0);
        mAdapter.notifyDataSetChanged();
    }

    private void itemClick(int position) {
        final Record record = mAdapter.getItem(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除朋友圈");
        builder.setMessage(record.content);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                delete(record);
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

    private void delete(Record record) {
        int ret = mClient.delete(record.did, record.uid);
        if (ret != 0) {
            Log.e(TAG, "delete moment failed " + ret);
            return;
        }

        mAdapter.remove(record);
        mAdapter.notifyDataSetChanged();
    }

    private void clear() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确定要清空所有数据？");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                doClear();
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

    private void doClear() {
        int ret = mClient.clear(mItem.mDid);
        if (ret != 0) {
            Log.e(TAG, "clear moments failed " + ret);
            return;
        }

        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    private void unFollow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("取消关注？");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                doUnFollow();
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

    private void doUnFollow() {
        int ret = mClient.unFollow(mItem.mDid);
        if (ret < 0) {
            Log.e(TAG, "unFollow " + mItem.mDid + " failed " + ret);
            Toast.makeText(this, "取消关注失败", Toast.LENGTH_LONG).show();
            return;
        }

        mDbHelper.removeRecords(mItem.mDid);
        Intent intent = new Intent();
        intent.putExtra(UNFOLLOW_DID, mItem.mDid);
        setResult(RESULT_UNFOLLOW, intent);
        finish();
    }
}
