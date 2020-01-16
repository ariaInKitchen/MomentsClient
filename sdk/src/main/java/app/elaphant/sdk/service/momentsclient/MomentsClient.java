package app.elaphant.sdk.service.momentsclient;

import android.util.Log;

import org.elastos.sdk.elephantwallet.contact.Contact;
import org.elastos.sdk.elephantwallet.contact.internal.ContactInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import app.elaphant.sdk.peernode.Connector;
import app.elaphant.sdk.peernode.PeerNodeListener;

public class MomentsClient {
    private static String TAG = MomentsClient.class.getName();
    private static String SERVICE_NAME = "moments";

    private Connector mConnector;
    private String mMyDid;
    private Listener mListener;

    public MomentsClient() {
        mConnector = new Connector(SERVICE_NAME);
        mConnector.setMessageListener(new MessageListener());
        Contact.UserInfo userInfo = mConnector.getUserInfo();
        mMyDid = userInfo.did;
    }

    class MessageListener implements PeerNodeListener.MessageListener {

        @Override
        public void onEvent(Contact.Listener.EventArgs event) {
            switch (event.type) {
                case FriendRequest:
                    Contact.Listener.RequestEvent requestEvent = (Contact.Listener.RequestEvent) event;
                    Log.d(TAG, "Friend request " + event.humanCode + " " + requestEvent.summary);
                    break;
                case StatusChanged:
                    Contact.Listener.StatusEvent statusEvent = (Contact.Listener.StatusEvent)event;
                    Log.d(TAG, statusEvent.humanCode + " status changed " + statusEvent.status);
                    if (mListener != null) {
                        mListener.onStatusChanged(statusEvent.humanCode, statusEvent.status);
                    }
                    break;
                case HumanInfoChanged:
                    Contact.Listener.InfoEvent infoEvent = (Contact.Listener.InfoEvent) event;
                    Log.d(TAG, event.humanCode + " info changed: " + infoEvent.toString());
                    break;
                default:
                    Log.w(TAG, "Unprocessed event: " + event);
            }
        }

        @Override
        public void onReceivedMessage(String s, ContactInterface.Channel channel, ContactInterface.Message message) {
            Log.d(TAG, "Receive message from " + s);
            Log.d(TAG, message.data.toString());
            if (mListener == null) {
                Log.w(TAG, "Listener not set");
                return;
            }

            try {
                JSONObject content = new JSONObject(message.data.toString());
                String command = content.getString("command");
                if (command.equals("pushData")) {
                    mListener.onPushData(s, content.getInt("type"), content.getJSONArray("content"));
                } else if (command.equals("publish")) {
                    mListener.onPublishResult(s, content.getLong("time"), content.getInt("result"));
                } else if (command.equals("setting")) {
                    mListener.onSettingResult(s, content.getString("type"),
                            Boolean.toString(content.getBoolean("value")), content.getInt("result"));
                } else if (command.equals("getData")) {
                    mListener.onGetData(s, content.getJSONObject("content"));
                } else if (command.equals("getDataList")) {
                    mListener.onGetDataList(s, content.getJSONArray("content"));
                } else if (command.equals("getSetting")) {
                    mListener.onGetSetting(s, content.getString("type"), Boolean.toString(content.getBoolean("value")));
                } else if (command.equals("delete")) {
                    mListener.onDeleteResult(s, content.getInt("id"), content.getInt("result"));
                } else if (command.equals("clear")) {
                    mListener.onClearResult(s, content.getInt("result"));
                } else if (command.equals("friendRequest")) {
                    mListener.onFriendRequest(s, content.getString("friendCode"), content.getString("summary"));
                } else if (command.equals("getFollowList")) {
                    mListener.onGetFollowList(s, content.getJSONArray("content"));
                }  else if (command.equals("newFollow")) {
                    mListener.onNewFollow(s, content.getString("friendCode"));
                } else {
                    Log.e(TAG, "not support command: " + command);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public int create(String did) {
        return addFriend(did,"create");
    }

    public int follow(String did) {
        return addFriend(did, "follow");
    }

    private int addFriend(String did, String command) {
        ContactInterface.Status status = mConnector.getStatus();
        if (status != ContactInterface.Status.Online) {
            Log.e(TAG, "node is not online");
            return -1;
        }

        try {
            JSONObject content = new JSONObject();
            content.put("command", command);
            return mConnector.addFriend(did, content.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -2;
    }

    public int setPrivate(String did, boolean priv) {
        JSONObject content = new JSONObject();
        try {
            content.put("command", "setting");
            content.put("type", "access");
            content.put("value", priv);
            return sendCommand(did, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -2;
    }

    public int publishText(String did, String content, long time, String access) {
        JSONObject contentObj = new JSONObject();
        try {
            contentObj.put("command", "publish");
            contentObj.put("type", 1);
            contentObj.put("content", content);
            contentObj.put("time", time);
            contentObj.put("access", access);
            return sendCommand(did, contentObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -2;
    }

    public int delete(String did, int id) {
        JSONObject content = new JSONObject();
        try {
            content.put("command", "delete");
            content.put("id", id);
            return sendCommand(did, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -2;
    }

    public int clear(String did) {
        JSONObject content = new JSONObject();
        try {
            content.put("command", "clear");
            return sendCommand(did, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -2;
    }

    public int getSetting(String did, String name) {
        JSONObject content = new JSONObject();
        try {
            content.put("command", "clear");
            return sendCommand(did, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -2;
    }

    public int acceptFriend(String did, String friendCode) {
        JSONObject content = new JSONObject();
        try {
            content.put("command", "acceptFriend");
            content.put("friendCode", friendCode);
            return sendCommand(did, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -2;
    }

    public int getFollowList(String did) {
        JSONObject content = new JSONObject();
        try {
            content.put("command", "getFollowList");
            return sendCommand(did, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -2;
    }

    public int getDataList(String did, long time) {
        JSONObject content = new JSONObject();
        try {
            content.put("command", "getDataList");
            content.put("time", time);
            return sendCommand(did, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -2;
    }

    private int sendCommand(String friendCode, JSONObject content) throws JSONException {
        ContactInterface.Status status = mConnector.getStatus();
        if (status != ContactInterface.Status.Online) {
            Log.e(TAG, "node is not online");
            return -1;
        }

        return mConnector.sendMessage(friendCode, content.toString());
    }

    public interface Listener {
        void onPushData(String did, int type, JSONArray array);
        void onPublishResult(String did, long time, int result);
        void onSettingResult(String did, String type, String value, int result);
        void onGetData(String did, JSONObject content);
        void onGetDataList(String did, JSONArray array);
        void onGetSetting(String did, String type, String value);
        void onDeleteResult(String did, int id, int result);
        void onClearResult(String did, int result);
        void onFriendRequest(String did, String friendCode, String summary);
        void onGetFollowList(String did, JSONArray array);
        void onNewFollow(String did, String friendCode);

        void onStatusChanged(String did, Contact.Status status);
    }
}
