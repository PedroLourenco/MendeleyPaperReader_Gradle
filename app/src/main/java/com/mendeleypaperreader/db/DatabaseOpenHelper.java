package com.mendeleypaperreader.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mendeleypaperreader.sessionManager.SessionManager;
import com.mendeleypaperreader.utl.Globalconstant;

/**
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    public final static String TABLE_DOCUMENT_DETAILS = "document_details";
    public final static String TABLE_AUTHORS = "authors";
    public final static String TABLE_FOLDERS = "folders";
    public final static String TABLE_FILES = "files";
    public final static String TABLE_PROFILE = "profile";
    public final static String TABLE_FOLDERS_DOCS = "folders_docs";
    public final static String TABLE_CATALOG_DOCS = "catalog_docs";
    public final static String TABLE_ACADEMIC_STATUS_DOCS = "academic_status_docs";
    public final static String TABLE_COUNTRY_STATUS_DOCS = "country_status_docs";
    public final static String TABLE_GROUPS = "groups";
    public final static String TABLE_DOC_TAGS = "documents_tags";
    public final static String TABLE_DOC_NOTES = "documents_notes";

    public final static String _ID = "_id";
    public final static String ID = "id";
    public final static String TYPE = "type";
    public final static String MONTH = "month";
    public final static String YEAR = "year";
    public final static String LAST_MODIFIED = "last_modified";
    public final static String DAY = "day";
    public final static String GROUP_ID = "group_id";
    public final static String SOURCE = "source";
    public final static String TITLE = "title";
    public final static String REVISION = "revision";
    public final static String IDENTIFIERS = "identifiers";   //MAP
    public final static String PMID = "pmid";
    public final static String DOI = "doi";
    public final static String ISSN = "issn";
    public final static String ARXIV = "arxiv";
    public final static String ISBN = "isbn";
    public final static String SCOPUS = "scopus";
    public final static String SSN = "ssn";
    public final static String ABSTRACT = "abstract";
    public final static String PROFILE_ID = "profile_id";
    public final static String AUTHORS = "authors";        //ARRAY
    public final static String ADDED = "added";
    public final static String CREATED = "created";
    public final static String PAGES = "pages";
    public final static String VOLUME = "volume";
    public final static String ISSUE = "issue";
    public final static String WEBSITE = "website";
    public final static String PUBLISHER = "publisher";
    public final static String CITY = "city";
    public final static String EDITION = "edition";
    public final static String INSTITUTION = "institution";
    public final static String SERIES = "series";
    public final static String CHAPTER = "chapter";
    public final static String EDITORS = "editors";  // array
    public final static String READ = "read";
    public final static String STARRED = "starred";
    public final static String AUTHORED = "authored";
    public final static String CONFIRMED = "confirmed";
    public final static String HIDDEN = "hidden";
    public final static String IS_DOWNLOAD = "is_download";
    public final static String TAGS = "tags";
    public final static String DOC_DETAILS_ID = "doc_details_id";
    public final static String AUTHOR_NAME = "author_name";
    public final static String READER_COUNT = "reader_count";
    public final static String FOLDER_ID = "folder_id";
    public final static String FOLDER_NAME = "folder_name";
    public final static String NAME = "name";
    public final static String FOLDER_ADDED = "folder_added";
    public final static String FOLDER_PARENT = "folder_parent";
    public final static String PARENT_ID = "parent_id";
    public final static String FOLDER_GROUP = "folder_group";
    public final static String GROUP = "group";
    public final static String FILE_ID = "file_id";
    public final static String FILE_NAME = "file_name";
    public final static String FILE_MIME_TYPE = "mime_type";
    public final static String FILE_DOC_ID = "document_id";
    public final static String FILE_FILEHASH = "filehash";
    //Table Profile colunms
    public final static String PROFILE_FIRST_NAME = "first_name";
    public final static String PROFILE_LAST_NAME = "last_name";
    public final static String PROFILE_DISPLAY_NAME = "display_name";
    public final static String PROFILE_LINK = "link";

    public final static String CATALOG_ID = "catalaog_id";
    public final static String SCORE = "score";
    public final static String STATUS = "status";
    public final static String COUNT = "count";
    public final static String COUNTRY = "country";
    //Table Groups columns
    public final static String GROUPS_NAME = "group_name";
    public final static String TAG_NAME = "tag_name";

    //Table NOTES columns
    public final static String NOTE_ID = "id";
    public final static String PREVIOUS_ID = "previous_id";
    public final static String COLOR = "color";
    public final static String TEXT = "text";
    public final static String POSITIONS = "position";
    public final static String PRIVACY_LEVEL = "provacy_level";
    public final static String FILEHASH = "filehash";
    public final static String DOCUMENT_ID = "document_id";


    final static String[] document_details_columns = {_ID, TYPE, MONTH, YEAR, LAST_MODIFIED, DAY, GROUP_ID, SOURCE, TITLE, REVISION, IDENTIFIERS, ABSTRACT, PROFILE_ID, AUTHORS, ADDED, PAGES, VOLUME, ISSUE, WEBSITE, PUBLISHER, CITY, EDITION, INSTITUTION, SERIES, CHAPTER, EDITORS, READ, STARRED, AUTHORED, CONFIRMED, HIDDEN};
    final static String[] document_titles_columns = {TITLE, AUTHORS, SOURCE, YEAR};

    final private static String CREATE_TABLE_AUTHORS = "CREATE TABLE authors (" + DOC_DETAILS_ID + " TEXT, "
            + AUTHOR_NAME + " TEXT, PRIMARY KEY (" + DOC_DETAILS_ID + "," + AUTHOR_NAME + " ) ) ";

    final private static String CREATE_TABLE_FILE = "CREATE TABLE files (" + FILE_ID + " TEXT, "
            + FILE_NAME + " TEXT, " + FILE_MIME_TYPE + " TEXT, " + FILE_DOC_ID + " TEXT, " + FILE_FILEHASH + " TEXT, PRIMARY KEY (" + FILE_ID + ") ) ";

    final private static String CREATE_TABLE_PROFILE = "CREATE TABLE profile (" + PROFILE_ID + " TEXT, "
            + PROFILE_FIRST_NAME + " TEXT, " + PROFILE_LAST_NAME + " TEXT, " + PROFILE_DISPLAY_NAME + " TEXT, " + PROFILE_LINK + " TEXT, PRIMARY KEY (" + PROFILE_ID + ") ) ";

    final private static String CREATE_TABLE_FOLDERS = "CREATE TABLE folders (" + FOLDER_ID + " TEXT, " + FOLDER_ADDED + " TEXT, " + FOLDER_PARENT + " TEXT, " + FOLDER_GROUP + " TEXT, "
            + FOLDER_NAME + " TEXT, PRIMARY KEY (" + FOLDER_ID + ") ) ";

    final private static String CREATE_TABLE_FOLDERS_DOCS = "CREATE TABLE folders_docs (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FOLDER_ID + " TEXT, " + DOC_DETAILS_ID + " TEXT) ";

    final private static String CREATE_TABLE_ACADEMIC_STATUS_DOCS = "CREATE TABLE academic_status_docs (" + DOC_DETAILS_ID + " TEXT, " + STATUS + " TEXT, " + COUNT + " TEXT) ";

    final private static String CREATE_TABLE_GROUPS = "CREATE TABLE groups (" + _ID + " TEXT PRIMARY KEY, " + GROUPS_NAME + " TEXT ) ";

    private static String CREATE_TABLE_DOC_TAGS = "CREATE TABLE documents_tags (" + _ID + " TEXT, " + TAG_NAME + " TEXT ) ";

    private static String CREATE_TABLE_DOC_NOTES = "CREATE TABLE documents_notes (" + NOTE_ID + " TEXT, " + TYPE + " TEXT, " + PREVIOUS_ID + " TEXT, "+ COLOR + " TEXT, "+ TEXT + " TEXT, " + POSITIONS + " TEXT, "+ PRIVACY_LEVEL + " TEXT, "+ FILEHASH + " TEXT, " +  DOCUMENT_ID + "  TEXT ) ";

    final private static String CREATE_TABLE_DOCUMENT_DETAILS =

            "CREATE TABLE document_details (" + _ID + " TEXT PRIMARY KEY, "
                    + TYPE + " TEXT, "
                    + YEAR + " TEXT, "
                    + LAST_MODIFIED + " TEXT, "
                    + GROUP_ID + " TEXT, "
                    + SOURCE + " TEXT, "
                    + TITLE + " TEXT, "
                    + PMID + " TEXT, "
                    + DOI + " TEXT, "
                    + ISSN + " TEXT, "
                    + ARXIV + " TEXT, "
                    + ISBN + " TEXT, "
                    + SCOPUS + " TEXT, "
                    + SSN + " TEXT, "
                    + ABSTRACT + " TEXT, "
                    + AUTHORS + " TEXT, "
                    + ADDED + " TEXT, "
                    + PAGES + " TEXT, "
                    + VOLUME + " TEXT, "
                    + ISSUE + " TEXT, "
                    + WEBSITE + " TEXT, "
                    + EDITION + " TEXT, "           //not used
                    + STARRED + " TEXT, "
                    + AUTHORED + " TEXT, "
                    + CONFIRMED + " TEXT, "
                    + IS_DOWNLOAD + " TEXT, "
                    + READER_COUNT + " TEXT, "
                    + TAGS + " Text )";


    final private static String DATABASE_NAME = "Mendeley_library.db";
    final private static Integer VERSION = 4;
    final private Context mContext;

    public DatabaseOpenHelper(Context context, String name,
                              CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, VERSION);
        this.mContext = context;

    }


    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        if (Globalconstant.LOG)
            Log.e(Globalconstant.TAG, "DATABASE CREATE!!!!!!!");

        db.execSQL(CREATE_TABLE_DOCUMENT_DETAILS);
        db.execSQL(CREATE_TABLE_AUTHORS);
        db.execSQL(CREATE_TABLE_FOLDERS);
        db.execSQL(CREATE_TABLE_FILE);
        db.execSQL(CREATE_TABLE_PROFILE);
        db.execSQL(CREATE_TABLE_FOLDERS_DOCS);
        db.execSQL(CREATE_TABLE_ACADEMIC_STATUS_DOCS);
        db.execSQL(CREATE_TABLE_GROUPS);
        db.execSQL(CREATE_TABLE_DOC_TAGS);
        db.execSQL(CREATE_TABLE_DOC_NOTES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        SessionManager session;
        session = new SessionManager(this.mContext);
        session.savePreferences("versionCode", "05");


        //db.execSQL(CREATE_TABLE_GROUPS);

        //Release_2 version 0.2.0 - Add column reader_count to table documents_details
        //Database verson 2
        //db.execSQL("ALTER TABLE document_details ADD COLUMN reader_count TEXT ;");
        //db.execSQL("ALTER TABLE document_details ADD COLUMN is_download TEXT ;");
        //db.execSQL(CREATE_TABLE_ACADEMIC_STATUS_DOCS);
        //db.execSQL(CREATE_TABLE_COUNTRY_STATUS_DOCS);

        //Release_4 delete old columns of table documents_details

        //Create temporary table base on documents_details

        db.execSQL("CREATE TABLE IF NOT EXISTS TEMP_DOCUMENTS_DETAILS (" + _ID + " TEXT PRIMARY KEY, " + TYPE + " TEXT, " + YEAR + " TEXT, " + LAST_MODIFIED + " TEXT, " + GROUP_ID + " TEXT, " + SOURCE + " TEXT, " + TITLE + " TEXT, " + PMID + " TEXT, "
                + DOI + " TEXT, " + ISSN + " TEXT, " + ARXIV + " TEXT, " + ISBN + " TEXT, " + SCOPUS + " TEXT, " + SSN + " TEXT, " + ABSTRACT + " TEXT, " + AUTHORS + " TEXT, " + ADDED + " TEXT, " + PAGES + " TEXT, " + VOLUME + " TEXT, " + ISSUE + " TEXT, " + AUTHORED + " TEXT, "
                + WEBSITE + " TEXT, " + STARRED + " TEXT, " + TAGS + " TEXT, " + IS_DOWNLOAD + " TEXT, " + READER_COUNT + " TEXT )");

        db.execSQL("INSERT INTO TEMP_DOCUMENTS_DETAILS (" + _ID + ", " + TYPE + ", " + YEAR + ", " + LAST_MODIFIED + ", " + GROUP_ID + ", " + SOURCE + ", " + TITLE + ", " + PMID + ", "
                + DOI + ",  " + ISSN + ",  " + ARXIV + ",  " + ISBN + ",  " + SCOPUS + ",  " + SSN + ",  " + ABSTRACT + ",  " + AUTHORS + ",  " + ADDED + ",  " + PAGES + ",  " + VOLUME + ",  " + ISSUE + ",  " + WEBSITE + ", " + STARRED + ", " + IS_DOWNLOAD + ", " + READER_COUNT + ", " + AUTHORED + ") " +
                "SELECT " + _ID + ", " + TYPE + ", " + YEAR + ", " + LAST_MODIFIED + ", " + GROUP_ID + ", " + SOURCE + ", " + TITLE + ", " + PMID + ", "
                + DOI + ",  " + ISSN + ",  " + ARXIV + ",  " + ISBN + ",  " + SCOPUS + ",  " + SSN + ",  " + ABSTRACT + ",  " + AUTHORS + ",  " + ADDED + ",  " + PAGES + ",  " + VOLUME + ",  " + ISSUE + ",  " + WEBSITE + ", " + STARRED + ", " + IS_DOWNLOAD + ", " + READER_COUNT + ", " + AUTHORED + " FROM document_details");

        db.execSQL("DROP TABLE document_details");

        db.execSQL("ALTER TABLE TEMP_DOCUMENTS_DETAILS RENAME TO document_details");


        db.execSQL(CREATE_TABLE_DOC_TAGS);
        db.execSQL(CREATE_TABLE_DOC_NOTES);


    }

    void deleteDatabase() {
        mContext.deleteDatabase(DATABASE_NAME);
    }


}

