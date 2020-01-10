package app.elaphant.sdk.service.momentsclient.test;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getName();
    private static final String DATABASE_NAME = "moments.db";
    private static final int VERSION = 1;

    private static DatabaseHelper sInstance;

    private SQLiteDatabase mDb;

    private static final String MOMENTS_TABLE = "create table " + MomentsTable.NAME + "("
            + MomentsTable.ID + " integer primary key autoincrement,"
            + MomentsTable.DID + " text not null,"
            + MomentsTable.STATUS + " text not null,"
            + MomentsTable.OWNER + " text,"
            + MomentsTable.PRIVATE + " text);";

    private static final String MOMENTS_LIST_TABLE = "create table " + MomentsListTable.NAME + "("
            + MomentsListTable.ID + " integer primary key autoincrement,"
            + MomentsListTable.DID + " text not null,"
            + MomentsListTable.UID + " integer not null,"
            + MomentsListTable.TYPE + " integer not null,"
            + MomentsListTable.CONTENT + " text not null,"
            + MomentsListTable.TIME + " integer not null,"
            + MomentsListTable.FILES + " text,"
            + MomentsListTable.ACCESS + " text);";


    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context);
        }

        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "on create");
        Log.d(TAG, MOMENTS_TABLE);
        db.execSQL(MOMENTS_TABLE);
        db.execSQL(MOMENTS_LIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertMoments(MomentsItem item) {
        ContentValues values = new ContentValues();
        values.put(MomentsTable.DID, item.mDid);
        values.put(MomentsTable.STATUS, item.mStatus);
        values.put(MomentsTable.OWNER, item.mIsOwner.toString());
        values.put(MomentsTable.PRIVATE, item.mIsPrivate.toString());

        return getDb().insertWithOnConflict(MomentsTable.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public MomentsItem getMyMoments() {
        String sql = "select * from " + MomentsTable.NAME + " where " + MomentsTable.OWNER + " = \"true\"";
        SQLiteDatabase db = getDb();
        Cursor cursor = db.rawQuery(sql, null);
        if (!cursor.moveToFirst()) return null;

        MomentsItem item = getItemFromCursor(cursor);
        cursor.close();
        return item;
    }

    public List<MomentsItem> getFollowList() {
        String sql = "select * from " + MomentsTable.NAME + " where " + MomentsTable.OWNER + "=\"false\"";
        Cursor cursor = getDb().rawQuery(sql, null);
        if (cursor.getCount() == 0) return new ArrayList<>();

        ArrayList<MomentsItem> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(getItemFromCursor(cursor));
        }

        cursor.close();
        return list;
    }

    private MomentsItem getItemFromCursor(Cursor cursor) {
        MomentsItem item = new MomentsItem();
        item.mDid = cursor.getString(cursor.getColumnIndex(MomentsTable.DID));
        item.mStatus = cursor.getString(cursor.getColumnIndex(MomentsTable.STATUS));
        item.mIsOwner = Boolean.valueOf(cursor.getString(cursor.getColumnIndex(MomentsTable.OWNER)));
        item.mIsPrivate = Boolean.valueOf(cursor.getString(cursor.getColumnIndex(MomentsTable.PRIVATE)));
        return item;
    }

    public void removeMoments(String did) {
        String sql = "delete from " + MomentsTable.NAME + " where " + MomentsTable.DID + "=\"" + did + "\";";
        getDb().execSQL(sql);
    }

    public void updateMomentsStatus(String did, String status) {
        String sql = "update " + MomentsTable.NAME + " set " + MomentsTable.STATUS
                    + "=\"" + status + "\" where " + MomentsTable.DID + "=\"" + did + "\";";
        getDb().execSQL(sql);
    }

    private SQLiteDatabase getDb() {
        if (mDb == null || !mDb.isOpen()) {
            Log.d(TAG, "open database");
            mDb = getWritableDatabase();
        }

        return mDb;
    }

    private final class MomentsTable {
        static final String NAME = "moments";

        static final String ID = "id";
        static final String DID = "did";
        static final String STATUS = "status";
        static final String OWNER = "isOwner";
        static final String PRIVATE = "isPrivate";
    }

    private final class MomentsListTable {
        static final String NAME = "moments_list";

        static final String ID = "id";
        static final String DID = "did";
        static final String UID = "uid";
        static final String TYPE = "type";
        static final String CONTENT = "content";
        static final String TIME = "time";
        static final String FILES = "files";
        static final String ACCESS = "access";
    }
}
