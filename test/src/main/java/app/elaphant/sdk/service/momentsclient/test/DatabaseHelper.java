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
    public static String DATABASE_NAME = "moments.db";
    private static final int VERSION = 2;

    private static DatabaseHelper sInstance;

    private SQLiteDatabase mDb;

    private static final String MOMENTS_TABLE = "create table " + MomentsTable.NAME + "("
            + MomentsTable.ID + " integer primary key autoincrement,"
            + MomentsTable.DID + " text not null,"
            + MomentsTable.STATUS + " text not null,"
            + MomentsTable.OWNER + " text,"
            + MomentsTable.PRIVATE + " text,"
            + MomentsTable.RECORD + " integer);";

    private static final String MOMENTS_LIST_TABLE = "create table " + MomentsListTable.NAME + "("
            + MomentsListTable.ID + " integer primary key autoincrement,"
            + MomentsListTable.DID + " text not null,"
            + MomentsListTable.UID + " integer not null,"
            + MomentsListTable.TYPE + " integer,"
            + MomentsListTable.CONTENT + " text,"
            + MomentsListTable.TIME + " integer,"
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
        db.execSQL(MOMENTS_TABLE);
        db.execSQL(MOMENTS_LIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "old version " + oldVersion);
        Log.d(TAG, "new version " + newVersion);
        if (oldVersion == 1) {
            db.execSQL("alter table " + MomentsTable.NAME + " add " + MomentsTable.RECORD + " integer");
        }
    }

    public long insertMoments(MomentsItem item) {
        ContentValues values = new ContentValues();
        values.put(MomentsTable.DID, item.mDid);
        values.put(MomentsTable.STATUS, item.mStatus);
        values.put(MomentsTable.OWNER, item.mIsOwner.toString());
        values.put(MomentsTable.PRIVATE, item.mIsPrivate.toString());
        values.put(MomentsTable.RECORD, item.mRecord);

        return getDb().insertWithOnConflict(MomentsTable.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public MomentsItem getMyMoments() {
        String sql = "select * from " + MomentsTable.NAME + " where " + MomentsTable.OWNER + " = \"true\"";
        Log.d(TAG, sql);
        SQLiteDatabase db = getDb();
        Cursor cursor = db.rawQuery(sql, null);
        if (!cursor.moveToFirst()) return null;

        MomentsItem item = getItemFromCursor(cursor);
        cursor.close();
        return item;
    }

    public List<MomentsItem> getFollowList() {
        String sql = "select * from " + MomentsTable.NAME + " where " + MomentsTable.OWNER + "=\"false\"";
        Log.d(TAG, sql);
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
        item.mRecord = cursor.getLong(cursor.getColumnIndex(MomentsTable.RECORD));
        return item;
    }

    public void removeMoments(String did) {
        String sql = "delete from " + MomentsTable.NAME + " where " + MomentsTable.DID + "=\"" + did + "\";";
        Log.d(TAG, sql);
        getDb().execSQL(sql);
    }

    public void updateMomentsStatus(String did, String status) {
        String sql = "update " + MomentsTable.NAME + " set " + MomentsTable.STATUS
                    + "=\"" + status + "\" where " + MomentsTable.DID + "=\"" + did + "\";";
        Log.d(TAG, sql);
        getDb().execSQL(sql);
    }

    public void updateMomentsRecord(String did, long time) {
        String sql = "update " + MomentsTable.NAME + " set " + MomentsTable.RECORD
                + "=" + time + " where " + MomentsTable.DID + "=\"" + did + "\";";
        Log.d(TAG, sql);
        getDb().execSQL(sql);
    }

    public long insertRecord(Record record) {
        ContentValues values = new ContentValues();
        values.put(MomentsListTable.DID, record.did);
        values.put(MomentsListTable.UID, record.uid);
        values.put(MomentsListTable.TYPE, record.type);
        values.put(MomentsListTable.TIME, record.time);
        values.put(MomentsListTable.CONTENT, record.content);
        values.put(MomentsListTable.FILES, record.files);
        values.put(MomentsListTable.ACCESS, record.access);

        return getDb().insertWithOnConflict(MomentsListTable.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void updateRecord(Record record) {
//        String sql = "update " + MomentsListTable.NAME + " set " + MomentsListTable.TYPE
//                + "=" + record.type + "," + MomentsListTable.CONTENT + "=\"" + record.content
//                + "\"," + MomentsListTable.FILES + "=\"" + record.files + "\","
//                + "\"," + MomentsListTable.ACCESS + "=\"" + record.access
//                + "\" where " + MomentsListTable.DID + "=\"" + record.did + "\" and "
//                + MomentsListTable.UID + "=" + record.uid + " and "
//                + MomentsListTable.TIME + "=" + record.time;

        String sql = "insert or replace into " + MomentsListTable.NAME + " ("
                + MomentsListTable.ID + "," + MomentsListTable.DID + "," + MomentsListTable.UID
                + "," + MomentsListTable.TYPE + "," + MomentsListTable.CONTENT + ","
                + MomentsListTable.TIME + "," + MomentsListTable.FILES + ","
                + MomentsListTable.ACCESS + ") values ("
                + "(select id from " + MomentsListTable.NAME + " where " + MomentsListTable.DID
                + "=\"" + record.did + "\" and " + MomentsListTable.TIME + "=" + record.time
                + "),\"" + record.did + "\"," + record.uid + "," + record.type + ",\""
                + record.content + "\"," + record.time + ",\"" + record.files + "\",\""
                + record.access + "\")";
        Log.d(TAG, sql);
        getDb().execSQL(sql);
    }

    public void updateRecordList(List<Record> list) {
        SQLiteDatabase db = getDb();
        db.beginTransaction();
        for (Record record : list) {
            updateRecord(record);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void updateRecordUid(String did, long time, int uid) {
        String sql = "update " + MomentsListTable.NAME + " set " + MomentsListTable.UID
                + "=" + uid + " where " + MomentsListTable.DID + "=\"" + did + "\" and "
                + MomentsListTable.TIME + "=" +time;
        Log.d(TAG, sql);
        getDb().execSQL(sql);
    }

    public void removeRecord(String did, long time) {
        String sql = "delete from " + MomentsListTable.NAME +
                " where " + MomentsListTable.DID + "=\"" + did + "\" and "
                + MomentsListTable.TIME + "=" + time;
        Log.d(TAG, sql);
        getDb().execSQL(sql);
    }

    public void removeRecordByUid(String did, int uid) {
        String sql = "delete from " + MomentsListTable.NAME +
                " where " + MomentsListTable.DID + "=\"" + did + "\" and "
                + MomentsListTable.UID + "=" + uid;
        Log.d(TAG, sql);
        getDb().execSQL(sql);
    }

    public void removeRecords(String did) {
        String sql = "delete from " + MomentsListTable.NAME +
                " where " + MomentsListTable.DID + "=\"" + did + "\"";
        Log.d(TAG, sql);
        getDb().execSQL(sql);
    }

    public List<Record> getRecordList(String did) {
        String sql = "select * from " + MomentsListTable.NAME
                + " where " + MomentsListTable.DID + "=\"" + did + "\" order by "
                + MomentsListTable.TIME + " desc";
        Log.d(TAG, sql);

        Cursor cursor = getDb().rawQuery(sql, null);
        if (cursor.getCount() == 0) return new ArrayList<>();

        ArrayList<Record> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(getRecordFromCursor(cursor));
        }

        cursor.close();
        return list;
    }

    private Record getRecordFromCursor(Cursor cursor) {
        Record record = new Record();

        record.id = cursor.getInt(cursor.getColumnIndex(MomentsListTable.ID));
        record.did = cursor.getString(cursor.getColumnIndex(MomentsListTable.DID));
        record.type = cursor.getInt(cursor.getColumnIndex(MomentsListTable.TYPE));
        record.uid = cursor.getInt(cursor.getColumnIndex(MomentsListTable.UID));
        record.content = cursor.getString(cursor.getColumnIndex(MomentsListTable.CONTENT));
        record.time = cursor.getLong(cursor.getColumnIndex(MomentsListTable.TIME));
        record.files = cursor.getString(cursor.getColumnIndex(MomentsListTable.FILES));
        record.access = cursor.getString(cursor.getColumnIndex(MomentsListTable.ACCESS));

        return record;
    }

    private SQLiteDatabase getDb() {
        if (mDb == null || !mDb.isOpen()) {
            Log.d(TAG, "open database");
            mDb = getWritableDatabase();
        }

        return mDb;
    }

    public void Close() {
        getDb().close();
    }

    public void clear() {
        SQLiteDatabase db = getDb();
        db.execSQL("delete from " + MomentsTable.NAME);
        db.execSQL("delete from " + MomentsListTable.NAME);
    }

    private final class MomentsTable {
        static final String NAME = "moments";

        static final String ID = "id";
        static final String DID = "did";
        static final String STATUS = "status";
        static final String OWNER = "isOwner";
        static final String PRIVATE = "isPrivate";
        static final String RECORD = "record";
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
