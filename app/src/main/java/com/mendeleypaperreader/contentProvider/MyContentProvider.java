package com.mendeleypaperreader.contentProvider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.utl.Globalconstant;

public class MyContentProvider extends ContentProvider {

	private DatabaseOpenHelper db_helper;

	private static final String AUTHORITY = 
			"com.android.mendeleypaperreader.utl.MyContentProvider";


	public static final Uri CONTENT_URI_DOC_DETAILS = Uri.parse("content://" + AUTHORITY + "/" + DatabaseOpenHelper.TABLE_DOCUMENT_DETAILS);
	public static final Uri CONTENT_URI_AUTHORS = Uri.parse("content://" + AUTHORITY + "/" + DatabaseOpenHelper.TABLE_AUTHORS);
	public static final Uri CONTENT_URI_FOLDERS = Uri.parse("content://" + AUTHORITY + "/" + DatabaseOpenHelper.TABLE_FOLDERS);
	public static final Uri CONTENT_URI_FILES = Uri.parse("content://" + AUTHORITY + "/" + DatabaseOpenHelper.TABLE_FILES);
	public static final Uri CONTENT_URI_PROFILE = Uri.parse("content://" + AUTHORITY + "/" + DatabaseOpenHelper.TABLE_PROFILE);
	public static final Uri CONTENT_URI_DELETE_DATA_BASE= Uri.parse("content://" + AUTHORITY + "/" + "DUMMY");
	public static final Uri CONTENT_URI_FOLDERS_DOCS= Uri.parse("content://" + AUTHORITY + "/" + DatabaseOpenHelper.TABLE_FOLDERS_DOCS);
	public static final Uri CONTENT_URI_CATALOG_DOCS= Uri.parse("content://" + AUTHORITY + "/" + DatabaseOpenHelper.TABLE_CATALOG_DOCS);
	public static final Uri CONTENT_URI_ACADEMIC_DOCS= Uri.parse("content://" + AUTHORITY + "/" + DatabaseOpenHelper.TABLE_ACADEMIC_STATUS_DOCS);
	public static final Uri CONTENT_URI_COUNTRY_DOCS= Uri.parse("content://" + AUTHORITY + "/" + DatabaseOpenHelper.TABLE_COUNTRY_STATUS_DOCS);
    public static final Uri CONTENT_URI_GROUPS= Uri.parse("content://" + AUTHORITY + "/" + DatabaseOpenHelper.TABLE_GROUPS);


    public static final int ALLDOCS = 1;
	public static final int ALL_DOCS_ID = 2;  
	public static final int ALL_DOC_AUTHORS = 3;
	public static final int DOC_AUTHORS_ID = 4;
	public static final int ALL_FOLDERS = 5;
	public static final int FOLDERS_ID = 6;
	public static final int ALL_FILES = 7;
	public static final int ALL_FILES_ID = 8;
	public static final int ALL_PROFILE = 9;
	public static final int ALL_PROFILE_ID = 10;
	public static final int DELETE_DATA_BASE = 11;
	public static final int ALL_FOLDERS_DOCS = 12;
	public static final int ALL_CATALOG_DOCS = 13;
	public static final int ALL_ACADEMIC_DOCS = 14;
	public static final int ALL_ACADEMIC_DOCS_ID = 15;
	public static final int ALL_COUNTRY_DOCS = 16;
	public static final int ALL_COUNTRY_DOCS_ID = 17;
    public static final int ALL_GROUPS = 18;


	private static final UriMatcher sURIMatcher = 
			new UriMatcher(UriMatcher.NO_MATCH);

	static {
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_DOCUMENT_DETAILS, ALLDOCS);
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_DOCUMENT_DETAILS + "/id", ALL_DOCS_ID);
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_AUTHORS, ALL_DOC_AUTHORS);
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_AUTHORS + "/#", DOC_AUTHORS_ID);
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_FOLDERS, ALL_FOLDERS);
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_FOLDERS + "/#", FOLDERS_ID);
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_FILES, ALL_FILES);
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_FILES + "/id", ALL_FILES_ID);
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_PROFILE, ALL_PROFILE);
		sURIMatcher.addURI(AUTHORITY, "DUMMY", DELETE_DATA_BASE);
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_FOLDERS_DOCS, ALL_FOLDERS_DOCS);
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_CATALOG_DOCS, ALL_CATALOG_DOCS);
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_ACADEMIC_STATUS_DOCS, ALL_ACADEMIC_DOCS);
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_ACADEMIC_STATUS_DOCS + "/id", ALL_ACADEMIC_DOCS_ID);
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_COUNTRY_STATUS_DOCS, ALL_COUNTRY_DOCS);
		sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_COUNTRY_STATUS_DOCS + "/id", ALL_COUNTRY_DOCS_ID);
        sURIMatcher.addURI(AUTHORITY, DatabaseOpenHelper.TABLE_GROUPS, ALL_GROUPS);
	}



	// system calls onCreate() when it starts up the provider.
	@Override
	public boolean onCreate() {
		// get access to the database helper
		db_helper = new DatabaseOpenHelper(getContext());
		return false;
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		SQLiteDatabase db = db_helper.getWritableDatabase();
		int count = 0;
		switch (sURIMatcher.match(uri)) {
		case DELETE_DATA_BASE:

			count = deleteDatabase(db, selection, selectionArgs);

			break;

		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null, false);
		return count;

	}



	@Override
	public Uri insert(Uri uri, ContentValues values) {

		SQLiteDatabase db = db_helper.getWritableDatabase();

		
		switch (sURIMatcher.match(uri)){
		case ALLDOCS:

			long row = db.insert(DatabaseOpenHelper.TABLE_DOCUMENT_DETAILS, null, values);

			// If record is added successfully		 
			if(row > 0) {
				Uri newUri = ContentUris.withAppendedId(CONTENT_URI_DOC_DETAILS, row);		 
				getContext().getContentResolver().notifyChange(newUri, null);		 
				return newUri;	
			}

			break;

		case ALL_DOC_AUTHORS:

			long authors_row = db.insert(DatabaseOpenHelper.TABLE_AUTHORS, null, values);

			// If record is added successfully		 
			if(authors_row > 0) {

				Uri newUri = ContentUris.withAppendedId(CONTENT_URI_AUTHORS, authors_row);		 
				getContext().getContentResolver().notifyChange(newUri, null);		 
				return newUri;	
			}
			break; 

		case ALL_FOLDERS:

			long folders_row = db.insert(DatabaseOpenHelper.TABLE_FOLDERS, null, values);

			// If record is added successfully		 
			if(folders_row > 0) {

				Uri newUri = ContentUris.withAppendedId(CONTENT_URI_FOLDERS, folders_row);		 
				getContext().getContentResolver().notifyChange(newUri, null);		 
				return newUri;	
			}
			break;
		case ALL_FILES:

			long files_row = db.insert(DatabaseOpenHelper.TABLE_FILES, null, values);

			// If record is added successfully		 
			if(files_row > 0) {	

				Uri newUri = ContentUris.withAppendedId(CONTENT_URI_FILES, files_row);		 
				getContext().getContentResolver().notifyChange(newUri, null);		 
				return newUri;	
			}
			break;

		case ALL_PROFILE:

			long profile_row = db.insert(DatabaseOpenHelper.TABLE_PROFILE, null, values);

			// If record is added successfully		 
			if(profile_row > 0) {

				Uri newUri = ContentUris.withAppendedId(CONTENT_URI_PROFILE, profile_row);		 
				getContext().getContentResolver().notifyChange(newUri, null);		 
				return newUri;	
			}
			break;

		case ALL_FOLDERS_DOCS:

			long folders_docs_row = db.insert(DatabaseOpenHelper.TABLE_FOLDERS_DOCS, null, values);

			// If record is added successfully		 
			if(folders_docs_row > 0) {

				Uri newUri = ContentUris.withAppendedId(CONTENT_URI_FOLDERS_DOCS, folders_docs_row);		 
				getContext().getContentResolver().notifyChange(newUri, null);		 
				return newUri;	
			}
			break;
			
		case ALL_CATALOG_DOCS:

			long catalog_docs_row = db.insert(DatabaseOpenHelper.TABLE_CATALOG_DOCS, null, values);

			// If record is added successfully		 
			if(catalog_docs_row > 0) {

				Uri newUri = ContentUris.withAppendedId(CONTENT_URI_CATALOG_DOCS, catalog_docs_row);		 
				getContext().getContentResolver().notifyChange(newUri, null);		 
				return newUri;	
			}
			break;
		case ALL_ACADEMIC_DOCS:

			long academic_docs_row = db.insert(DatabaseOpenHelper.TABLE_ACADEMIC_STATUS_DOCS, null, values);

			// If record is added successfully		 
			if(academic_docs_row > 0) {

				Uri newUri = ContentUris.withAppendedId(CONTENT_URI_ACADEMIC_DOCS, academic_docs_row);		 
				getContext().getContentResolver().notifyChange(newUri, null);		 
				return newUri;	
			}
			break;
		case ALL_COUNTRY_DOCS:

			long country_docs_row = db.insert(DatabaseOpenHelper.TABLE_COUNTRY_STATUS_DOCS, null, values);

			// If record is added successfully		 
			if(country_docs_row > 0) {

				Uri newUri = ContentUris.withAppendedId(CONTENT_URI_COUNTRY_DOCS, country_docs_row);		 
				getContext().getContentResolver().notifyChange(newUri, null);		 
				return newUri;	
			}
			break;
            case ALL_GROUPS:

                long group_row = db.insert(DatabaseOpenHelper.TABLE_GROUPS, null, values);

                // If record is added successfully
                if(group_row > 0) {

                    Uri newUri = ContentUris.withAppendedId(CONTENT_URI_GROUPS, group_row);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
                break;
		default: throw new SQLException("Failed to insert row into " + uri);
		}
		return uri;

	}


	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteDatabase db = db_helper.getWritableDatabase();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();


		switch (sURIMatcher.match(uri)) {
		case ALLDOCS:
			queryBuilder.setTables(DatabaseOpenHelper.TABLE_DOCUMENT_DETAILS);
			break;
		case ALL_DOCS_ID:		   

			queryBuilder.setTables(DatabaseOpenHelper.TABLE_DOCUMENT_DETAILS);
			queryBuilder.appendWhere(selection);
			break;

		case ALL_FOLDERS:

			queryBuilder.setTables(DatabaseOpenHelper.TABLE_FOLDERS);
			break;

		case ALL_PROFILE:		   

			queryBuilder.setTables(DatabaseOpenHelper.TABLE_PROFILE);
			break;
			
		case ALL_ACADEMIC_DOCS_ID:		   

			queryBuilder.setTables(DatabaseOpenHelper.TABLE_ACADEMIC_STATUS_DOCS);
			queryBuilder.appendWhere(selection);
			break;	
		case ALL_COUNTRY_DOCS_ID:		   

			queryBuilder.setTables(DatabaseOpenHelper.TABLE_COUNTRY_STATUS_DOCS);
			queryBuilder.appendWhere(selection);
			break;	
			
		case ALL_FILES_ID:		   

			queryBuilder.setTables(DatabaseOpenHelper.TABLE_FILES);
			queryBuilder.appendWhere(selection);
			break;

        case ALL_GROUPS:

                queryBuilder.setTables(DatabaseOpenHelper.TABLE_GROUPS);

                break;


            default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}


		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;


	} 

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs){

		SQLiteDatabase db = db_helper.getWritableDatabase();
		int rowsUpdated = 0;


		switch (sURIMatcher.match(uri)) {
		case ALL_DOCS_ID:

			if(!TextUtils.isEmpty(selection)){

				rowsUpdated = 
						db.update(DatabaseOpenHelper.TABLE_DOCUMENT_DETAILS, 
								values,
								selection, 
								selectionArgs); 
			}

			break;
		case ALL_FILES_ID:

			if(!TextUtils.isEmpty(selection)){

				rowsUpdated = 
						db.update(DatabaseOpenHelper.TABLE_FILES, 
								values,
								selection, 
								selectionArgs); 
			}

			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}





	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}


	private int deleteDatabase(SQLiteDatabase db, String selection, String[] selectionArgs) {

        if (Globalconstant.LOG)
            Log.e(Globalconstant.TAG, "DATABASE CREATE!!!!!!!");

        int count = 0;

		count = db.delete(DatabaseOpenHelper.TABLE_DOCUMENT_DETAILS, selection, selectionArgs);
		count = count + db.delete(DatabaseOpenHelper.TABLE_AUTHORS, selection, selectionArgs);
		count = count + db.delete(DatabaseOpenHelper.TABLE_FOLDERS, selection, selectionArgs);
		count = count + db.delete(DatabaseOpenHelper.TABLE_FILES, selection, selectionArgs);
		count = count + db.delete(DatabaseOpenHelper.TABLE_PROFILE, selection, selectionArgs);
		count = count + db.delete(DatabaseOpenHelper.TABLE_ACADEMIC_STATUS_DOCS, selection, selectionArgs);
        count = count + db.delete(DatabaseOpenHelper.TABLE_GROUPS, selection, selectionArgs);
		return count;
	}




}
