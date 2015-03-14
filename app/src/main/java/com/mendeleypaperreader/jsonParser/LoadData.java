package com.mendeleypaperreader.jsonParser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mendeleypaperreader.contentProvider.MyContentProvider;
import com.mendeleypaperreader.db.DatabaseOpenHelper;
import com.mendeleypaperreader.sessionManager.SessionManager;
import com.mendeleypaperreader.utl.DownloaderThread;
import com.mendeleypaperreader.utl.GetDataBaseInformation;
import com.mendeleypaperreader.utl.Globalconstant;
import com.mendeleypaperreader.utl.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */


public class LoadData {

    private Context context;
    private static String access_token;
    private GetDataBaseInformation getDataBaseInformation;
    RequestQueue queue;
    private Request.Priority mpriority = Request.Priority.HIGH;

    public LoadData(Context context) {
        this.context = context;

        SessionManager session = new SessionManager(this.context);
        access_token = session.LoadPreference("access_token");
        queue = Volley.newRequestQueue(this.context);
        getDataBaseInformation = new GetDataBaseInformation(this.context);

    }


    public void downloadFiles() {

        Thread downloaderThread;

        Cursor cursorFiles = getDataBaseInformation.getFile();

        while (cursorFiles.moveToNext()) {

            String fileId = cursorFiles.getString(cursorFiles.getColumnIndex(DatabaseOpenHelper._ID));
            String docId = cursorFiles.getString(cursorFiles.getColumnIndex(DatabaseOpenHelper.DOCUMENT_ID));
            String url = Globalconstant.get_files_by_doc_id.replace("file_id", fileId) + access_token;

            downloaderThread = new DownloaderThread(this.context, url, fileId, false, docId);
            downloaderThread.start();

        }
    }


    public void getGroupDocs() {

        Cursor groups = getGroups();

        while (groups.moveToNext()) {

            String url = Globalconstant.get_docs_in_groups.replace("#groupId#", groups.getString(groups.getColumnIndex(DatabaseOpenHelper._ID)));

            getUserLibrary(url + access_token);
        }

        groups.close();
    }


    private Cursor getDocId() {

        if (Globalconstant.LOG)
            Log.d(Globalconstant.TAG, "getGROUPS- LOAD DATA");

        String[] projection = new String[]{DatabaseOpenHelper._ID};
        Uri uri = MyContentProvider.CONTENT_URI_DOC_DETAILS;


        return this.context.getContentResolver().query(uri, projection, null, null, null);


    }


    


    public void getNotes() {

        ContentValues[] valuesArray;

        List<ContentValues> valueList = new ArrayList<ContentValues>();
        List<InputStream> link = new ArrayList<InputStream>();


        Cursor cursorDocId = getDocId();

        while (cursorDocId.moveToNext()) {

            String url = Globalconstant.get_docs_notes2 + cursorDocId.getString(cursorDocId.getColumnIndex(DatabaseOpenHelper._ID)) + "&limit=200&access_token=" + access_token;


            JSONParser jParser = new JSONParser();
            ObjectMapper mapper = new ObjectMapper();
            JsonFactory factory = mapper.getFactory();


            link = jParser.getJACKSONFromUrl(url, true);

            try {
                for (InputStream oneItem : link) {
                    JsonParser jp = factory.createParser(oneItem);

                    JsonNode rootNode = mapper.readTree(jp);

                    Iterator<JsonNode> ite = rootNode.iterator();

                    while (ite.hasNext()) {
                        JsonNode temp = ite.next();

                        ContentValues note_values = new ContentValues();

                        if (temp.has(DatabaseOpenHelper.TYPE)) {

                            String noteType = temp.get(DatabaseOpenHelper.TYPE).asText();

                            if (!noteType.equals("note")) {
                                break;
                            }

                            note_values.put(DatabaseOpenHelper.TYPE, noteType);
                        }


                        if (temp.has(DatabaseOpenHelper.NOTE_ID)) {

                            note_values.put(DatabaseOpenHelper.NOTE_ID, temp.get(DatabaseOpenHelper.NOTE_ID).asText());
                        }


                        if (temp.has(DatabaseOpenHelper.TEXT)) {

                            note_values.put(DatabaseOpenHelper.TEXT, temp.get(DatabaseOpenHelper.TEXT).asText().replace("<br/>", " "));
                        }
                        if (temp.has(DatabaseOpenHelper.DOCUMENT_ID)) {

                            note_values.put(DatabaseOpenHelper.DOCUMENT_ID, temp.get(DatabaseOpenHelper.DOCUMENT_ID).asText());
                        }

                        valueList.add(note_values);
                    }

                    jp.close();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //Insert data on table notes
        valuesArray = new ContentValues[valueList.size()];
        valueList.toArray(valuesArray);

        context.getContentResolver().bulkInsert(MyContentProvider.CONTENT_URI_DOC_NOTES, valuesArray);


        cursorDocId.close();

    }


    public void getUserGroups(String url) {


        List<ContentValues> valueList = new ArrayList<ContentValues>();

        ContentValues[] valuesArray;

        JSONParser jParser = new JSONParser();
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        List<InputStream> link = new ArrayList<InputStream>();
        link = jParser.getJACKSONFromUrl(url, true);

        try {
            for (InputStream oneItem : link) {
                JsonParser jp = factory.createParser(oneItem);
                JsonNode rootNode = mapper.readTree(jp);

                Iterator<JsonNode> ite = rootNode.iterator();

                while (ite.hasNext()) {
                    JsonNode temp = ite.next();

                    ContentValues values = new ContentValues();

                    if (temp.has(DatabaseOpenHelper.NAME)) {
                        values.put(DatabaseOpenHelper.GROUPS_NAME, temp.get(DatabaseOpenHelper.NAME).asText());
                    }

                    if (temp.has(DatabaseOpenHelper.ID)) {
                        values.put(DatabaseOpenHelper._ID, temp.get(DatabaseOpenHelper.ID).asText());
                    }
                    // Uri uri = this.context.getContentResolver().insert(MyContentProvider.CONTENT_URI_GROUPS, values);
                    valueList.add(values);
                }

                jp.close();
            }

            //Insert data on table groups
            valuesArray = new ContentValues[valueList.size()];
            valueList.toArray(valuesArray);
            context.getContentResolver().bulkInsert(MyContentProvider.CONTENT_URI_GROUPS, valuesArray);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private Cursor getGroups() {

        if (Globalconstant.LOG)
            Log.d(Globalconstant.TAG, "getGROUPS- LOAD DATA");

        String[] projection = new String[]{DatabaseOpenHelper._ID + " as _id"};
        Uri uri = MyContentProvider.CONTENT_URI_GROUPS;


        return this.context.getContentResolver().query(uri, projection, null, null, null);


    }


    public void getFiles(String url) {


        List<ContentValues> valueList = new ArrayList<ContentValues>();

        ContentValues[] valuesArray;

        JSONParser jParser = new JSONParser();
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        List<InputStream> link = new ArrayList<InputStream>();
        link = jParser.getJACKSONFromUrl(url, true);

        try {
            for (InputStream oneItem : link) {
                JsonParser jp = factory.createParser(oneItem);
                JsonNode rootNode = mapper.readTree(jp);

                Iterator<JsonNode> ite = rootNode.iterator();

                while (ite.hasNext()) {
                    JsonNode temp = ite.next();

                    ContentValues values = new ContentValues();

                    if (temp.has(DatabaseOpenHelper.ID)) {
                        values.put(DatabaseOpenHelper.FILE_ID, temp.get(DatabaseOpenHelper.ID).asText());
                    }

                    if (temp.has(DatabaseOpenHelper.FILE_DOC_ID)) {
                        values.put(DatabaseOpenHelper.FILE_DOC_ID, temp.get(DatabaseOpenHelper.FILE_DOC_ID).asText());
                    } else {
                        values.put(DatabaseOpenHelper.FILE_DOC_ID, "");
                    }

                    if (temp.has(DatabaseOpenHelper.FILE_NAME)) {
                        values.put(DatabaseOpenHelper.FILE_NAME, temp.get(DatabaseOpenHelper.FILE_NAME).asText());
                    } else {
                        values.put(DatabaseOpenHelper.FILE_NAME, "");
                    }

                    if (temp.has(DatabaseOpenHelper.FILE_MIME_TYPE)) {
                        values.put(DatabaseOpenHelper.FILE_MIME_TYPE, temp.get(DatabaseOpenHelper.FILE_MIME_TYPE).asText());
                    } else {
                        values.put(DatabaseOpenHelper.FILE_MIME_TYPE, "");
                    }
                    if (temp.has(DatabaseOpenHelper.FILE_FILEHASH)) {
                        values.put(DatabaseOpenHelper.FILE_FILEHASH, temp.get(DatabaseOpenHelper.FILE_FILEHASH).asText());
                    } else {
                        values.put(DatabaseOpenHelper.FILE_FILEHASH, "");
                    }

                    valueList.add(values);
                }

                jp.close();
            }

            //Insert data on table files
            valuesArray = new ContentValues[valueList.size()];
            valueList.toArray(valuesArray);
            context.getContentResolver().bulkInsert(MyContentProvider.CONTENT_URI_FILES, valuesArray);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public void getUserFolders(String url) {

        List<ContentValues> valueList = new ArrayList<ContentValues>();

        ContentValues[] valuesArray;

        JSONParser jParser = new JSONParser();
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        List<InputStream> link = new ArrayList<InputStream>();
        link = jParser.getJACKSONFromUrl(url, true);

        try {
            for (InputStream oneItem : link) {
                JsonParser jp = factory.createParser(oneItem);
                JsonNode rootNode = mapper.readTree(jp);

                Iterator<JsonNode> ite = rootNode.iterator();

                while (ite.hasNext()) {
                    JsonNode temp = ite.next();

                    ContentValues values = new ContentValues();

                    if (temp.has(DatabaseOpenHelper.ID)) {
                        values.put(DatabaseOpenHelper.FOLDER_ID, temp.get(DatabaseOpenHelper.ID).asText());
                    }

                    if (temp.has(DatabaseOpenHelper.NAME)) {
                        values.put(DatabaseOpenHelper.FOLDER_NAME, temp.get(DatabaseOpenHelper.NAME).asText());
                    } else {
                        values.put(DatabaseOpenHelper.FOLDER_NAME, "");
                    }

                    if (temp.has(DatabaseOpenHelper.PARENT_ID)) {
                        values.put(DatabaseOpenHelper.FOLDER_PARENT, temp.get(DatabaseOpenHelper.PARENT_ID).asText());
                    } else {
                        values.put(DatabaseOpenHelper.FOLDER_PARENT, "");
                    }

                    if (temp.has(DatabaseOpenHelper.ADDED)) {
                        values.put(DatabaseOpenHelper.FOLDER_ADDED, temp.get(DatabaseOpenHelper.ADDED).asText());
                    } else {
                        values.put(DatabaseOpenHelper.FOLDER_ADDED, "");
                    }
                    if (temp.has(DatabaseOpenHelper.GROUP)) {
                        values.put(DatabaseOpenHelper.FOLDER_GROUP, temp.get(DatabaseOpenHelper.GROUP).asText());
                    } else {
                        values.put(DatabaseOpenHelper.FOLDER_GROUP, "");
                    }

                    valueList.add(values);

                }

                jp.close();
            }

            //Insert data on table Folders
            valuesArray = new ContentValues[valueList.size()];
            valueList.toArray(valuesArray);
            context.getContentResolver().bulkInsert(MyContentProvider.CONTENT_URI_FOLDERS, valuesArray);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    public void getUserLibrary(String url) {

        JSONParser jParser = new JSONParser();
        String docTitle, docId;

        List<ContentValues> valueList = new ArrayList<ContentValues>();
        List<ContentValues> authorsValuesList = new ArrayList<ContentValues>();
        List<ContentValues> tagValueList = new ArrayList<ContentValues>();

        ContentValues[] mValueArray, authorsValuesArray, tagsValuesArray;

        List<InputStream> link = new ArrayList<InputStream>();

        ObjectMapper mapper = new ObjectMapper();

        JsonFactory factory = mapper.getFactory();
        link = jParser.getJACKSONFromUrl(url, true);

        try {

            for (InputStream oneItem : link) {

                JsonParser jp = factory.createParser(oneItem);
                JsonNode rootNode = mapper.readTree(jp);
                Iterator<JsonNode> ite = rootNode.iterator();
                while (ite.hasNext()) {
                    JsonNode temp = ite.next();


                    ContentValues values = new ContentValues();


                    if (temp.has(DatabaseOpenHelper.ID)) {


                        docId = temp.get(DatabaseOpenHelper.ID).asText();
                        values.put(DatabaseOpenHelper._ID, docId);
                    }

                    if (temp.has(DatabaseOpenHelper.TITLE)) {
                        docTitle = temp.get(DatabaseOpenHelper.TITLE).asText();
                        values.put(DatabaseOpenHelper.TITLE, docTitle);
                    } else {
                        docTitle = "";
                        values.put(DatabaseOpenHelper.TITLE, docTitle);
                    }

                    if (temp.has(DatabaseOpenHelper.TYPE)) {
                        values.put(DatabaseOpenHelper.TYPE, temp.get(DatabaseOpenHelper.TYPE).asText());
                    } else {
                        values.put(DatabaseOpenHelper.TYPE, "");
                    }


                    if (temp.has(DatabaseOpenHelper.YEAR)) {
                        values.put(DatabaseOpenHelper.YEAR, temp.get(DatabaseOpenHelper.YEAR).asText());
                    } else {
                        values.put(DatabaseOpenHelper.YEAR, "");
                    }
                    if (temp.has(DatabaseOpenHelper.LAST_MODIFIED)) {
                        values.put(DatabaseOpenHelper.LAST_MODIFIED, temp.get(DatabaseOpenHelper.LAST_MODIFIED).asText());
                    } else {
                        values.put(DatabaseOpenHelper.LAST_MODIFIED, "");
                    }

                    if (temp.has(DatabaseOpenHelper.TAGS)) {

                        Iterator<JsonNode> tagIterator = temp.get(DatabaseOpenHelper.TAGS).elements();
                        String tags = "";
                        while (tagIterator.hasNext()) {

                            ContentValues tagsValues = new ContentValues();

                            String tagName = tagIterator.next().asText();
                            tags += tagName + ",";
                            tagsValues.put(DatabaseOpenHelper._ID, temp.get(DatabaseOpenHelper.ID).asText());
                            tagsValues.put(DatabaseOpenHelper.TAG_NAME, tagName);
                            //Uri uriTag = context.getContentResolver().insert(MyContentProvider.CONTENT_URI_DOC_TAGS, tagsValues);
                            values.put(DatabaseOpenHelper.TAGS, tags.substring(0, tags.length() - 1));

                            tagValueList.add(tagsValues);
                        }

                    } else {
                        values.put(DatabaseOpenHelper.TAGS, "");
                    }

                    if (temp.has(DatabaseOpenHelper.CREATED)) {
                        values.put(DatabaseOpenHelper.ADDED, temp.get(DatabaseOpenHelper.CREATED).asText());
                    } else {
                        values.put(DatabaseOpenHelper.ADDED, "");
                    }

                    if (temp.has(DatabaseOpenHelper.GROUP_ID)) {
                        values.put(DatabaseOpenHelper.GROUP_ID, temp.get(DatabaseOpenHelper.GROUP_ID).asText());
                    } else {
                        values.put(DatabaseOpenHelper.GROUP_ID, "");
                    }
                    if (temp.has(DatabaseOpenHelper.SOURCE)) {
                        values.put(DatabaseOpenHelper.SOURCE, temp.get(DatabaseOpenHelper.SOURCE).asText());
                    } else {
                        values.put(DatabaseOpenHelper.SOURCE, "");
                    }

                    if (temp.has(DatabaseOpenHelper.PAGES)) {
                        values.put(DatabaseOpenHelper.PAGES, temp.get(DatabaseOpenHelper.PAGES).asText());
                    } else {
                        values.put(DatabaseOpenHelper.PAGES, "");
                    }

                    if (temp.has(DatabaseOpenHelper.VOLUME)) {
                        values.put(DatabaseOpenHelper.VOLUME, temp.get(DatabaseOpenHelper.VOLUME).asText());
                    } else {
                        values.put(DatabaseOpenHelper.VOLUME, "");
                    }
                    if (temp.has(DatabaseOpenHelper.ISSUE)) {
                        values.put(DatabaseOpenHelper.ISSUE, temp.get(DatabaseOpenHelper.ISSUE).asText());
                    } else {
                        values.put(DatabaseOpenHelper.ISSUE, "");
                    }

                    if (temp.has(DatabaseOpenHelper.STARRED)) {
                        values.put(DatabaseOpenHelper.STARRED, temp.get(DatabaseOpenHelper.STARRED).asText());
                    } else {
                        values.put(DatabaseOpenHelper.STARRED, "");
                    }
                    if (temp.has(DatabaseOpenHelper.AUTHORED)) {
                        values.put(DatabaseOpenHelper.AUTHORED, temp.get(DatabaseOpenHelper.AUTHORED).asText());

                    } else {
                        values.put(DatabaseOpenHelper.AUTHORED, "");
                    }

                    if (temp.has(DatabaseOpenHelper.ABSTRACT)) {
                        values.put(DatabaseOpenHelper.ABSTRACT, temp.get(DatabaseOpenHelper.ABSTRACT).asText());
                    } else {
                        values.put(DatabaseOpenHelper.ABSTRACT, "");
                    }

                    //Array
                    //authors":[{"first_name":"Asger","last_name":"Hobolth"},{"first_name":"Ole F","last_name":"Christensen"},{"first_name":"Thomas","last_name":"Mailund"},{"first_name":"Mikkel H","last_name":"Schierup"}]
                    if (temp.has(DatabaseOpenHelper.AUTHORS)) {
                        Iterator<JsonNode> authorsIterator = temp.get(DatabaseOpenHelper.AUTHORS).elements();
                        String authors = "";
                        String aux_surname, aux_forenamed;

                        while (authorsIterator.hasNext()) {

                            JsonNode author = authorsIterator.next();
                            ContentValues authors_values = new ContentValues();

                            if (author.has(DatabaseOpenHelper.PROFILE_FIRST_NAME)) {
                                aux_forenamed = author.get(DatabaseOpenHelper.PROFILE_FIRST_NAME).asText();

                            } else {
                                aux_forenamed = "";
                            }

                            if (author.has(DatabaseOpenHelper.PROFILE_LAST_NAME)) {
                                aux_surname = author.get(DatabaseOpenHelper.PROFILE_LAST_NAME).asText();

                            } else {
                                aux_surname = "";
                            }

                            author.get(DatabaseOpenHelper.PROFILE_LAST_NAME);

                            String author_name = aux_forenamed + " " + aux_surname;

                            authors += author_name + ",";
                            values.put(DatabaseOpenHelper.AUTHORS, authors.substring(0, authors.length() - 1));

                            
                            authors_values.put(DatabaseOpenHelper.DOC_DETAILS_ID, temp.get("id").asText());
                            authors_values.put(DatabaseOpenHelper.AUTHOR_NAME, author_name);

                            authorsValuesList.add(authors_values);

                        }


                    } else {
                        values.put(DatabaseOpenHelper.AUTHORS, "");
                    }


                    if (temp.has(DatabaseOpenHelper.IDENTIFIERS)) {

                        Iterator<Entry<String, JsonNode>> identifierIterator = temp.get(DatabaseOpenHelper.IDENTIFIERS).fields();

                        values.put(DatabaseOpenHelper.ISSN, "");
                        values.put(DatabaseOpenHelper.ISBN, "");
                        values.put(DatabaseOpenHelper.PMID, "");
                        values.put(DatabaseOpenHelper.SCOPUS, "");
                        values.put(DatabaseOpenHelper.SSN, "");
                        values.put(DatabaseOpenHelper.ARXIV, "");
                        values.put(DatabaseOpenHelper.DOI, "");

                        while (identifierIterator.hasNext()) {

                            Entry<String, JsonNode> entry = identifierIterator.next();

                            values.put(entry.getKey(), entry.getValue().asText());
                        }
                    } else {
                        values.put(DatabaseOpenHelper.ISSN, "");
                        values.put(DatabaseOpenHelper.ISBN, "");
                        values.put(DatabaseOpenHelper.PMID, "");
                        values.put(DatabaseOpenHelper.SCOPUS, "");
                        values.put(DatabaseOpenHelper.SSN, "");
                        values.put(DatabaseOpenHelper.ARXIV, "");
                        values.put(DatabaseOpenHelper.DOI, "");
                    }


                    valueList.add(values);

                }


                jp.close();
            }


            //Insert data on table Document_details
            mValueArray = new ContentValues[valueList.size()];
            valueList.toArray(mValueArray);
            this.context.getContentResolver().bulkInsert(MyContentProvider.CONTENT_URI_DOC_DETAILS, mValueArray);

            //Insert data on table Authors
            authorsValuesArray = new ContentValues[authorsValuesList.size()];
            authorsValuesList.toArray(authorsValuesArray);
            context.getContentResolver().bulkInsert(MyContentProvider.CONTENT_URI_AUTHORS, authorsValuesArray);

            //Insert data on table Tags
            tagsValuesArray = new ContentValues[tagValueList.size()];
            tagValueList.toArray(tagsValuesArray);
            context.getContentResolver().bulkInsert(MyContentProvider.CONTENT_URI_DOC_TAGS, tagsValuesArray);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private Cursor getFoldersId() {

        if (Globalconstant.LOG)
            Log.d(Globalconstant.TAG, "getGROUPS- LOAD DATA");


        String[] projection = new String[]{DatabaseOpenHelper.FOLDER_ID, DatabaseOpenHelper.FOLDER_NAME};
        Uri uri = MyContentProvider.CONTENT_URI_FOLDERS;


        return this.context.getContentResolver().query(uri, projection, null, null, null);


    }
    

    

    public void getUserDocsInFolders() {

        Cursor cursorFolderId = getFoldersId();

        while (cursorFolderId.moveToNext()) {
            
            getDocsFolder(cursorFolderId.getString(cursorFolderId.getColumnIndex(DatabaseOpenHelper.FOLDER_ID)));
        }

        cursorFolderId.close();

    }


    private void getDocsFolder(String folderId) {


        String auxurl = Globalconstant.get_docs_in_folders;
        String url = auxurl.replace("id", folderId) + access_token;


        List<ContentValues> valueList = new ArrayList<ContentValues>();

        ContentValues[] valuesArray;


        JSONParser jParser = new JSONParser();
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        List<InputStream> link = new ArrayList<InputStream>();


        link = jParser.getJACKSONFromUrl(url, true);


        try {

            for (InputStream oneItem : link) {
                JsonParser jp = factory.createParser(oneItem);
                JsonNode rootNode = mapper.readTree(jp);

                Iterator<JsonNode> ite = rootNode.iterator();
                while (ite.hasNext()) {
                    JsonNode temp = ite.next();


                    if (temp.has(DatabaseOpenHelper.ID)) {
                        ContentValues values = new ContentValues();

                        values.put(DatabaseOpenHelper.FOLDER_ID, folderId);
                        values.put(DatabaseOpenHelper.DOC_DETAILS_ID, temp.get(DatabaseOpenHelper.ID).asText());
                        valueList.add(values);

                    }
                }
                jp.close();
            }

            //Insert data on table Folders_doc
            valuesArray = new ContentValues[valueList.size()];
            valueList.toArray(valuesArray);
            context.getContentResolver().bulkInsert(MyContentProvider.CONTENT_URI_FOLDERS_DOCS, valuesArray);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Cursor getDocInfo() {


        String[] projection;
        String selection = null;
        String orderBy = null;
        Cursor query;

        projection = new String[]{DatabaseOpenHelper._ID + " as _id", DatabaseOpenHelper.PMID, DatabaseOpenHelper.DOI, DatabaseOpenHelper.ISSN, DatabaseOpenHelper.ISBN, DatabaseOpenHelper.SCOPUS, DatabaseOpenHelper.ARXIV};

        Uri uri = MyContentProvider.CONTENT_URI_DOC_DETAILS;
        query = this.context.getContentResolver().query(uri, projection, selection, null, orderBy);

        return query;

    }


    public void getCatalogId() {

        ContentValues values = new ContentValues();

        List<ContentValues> valueList = new ArrayList<ContentValues>();

        ContentValues[] valuesArray;

        Cursor cursorDocs;
        String urlfilter = null;
        boolean toProcess = false;
        Uri uri_ = Uri.parse(MyContentProvider.CONTENT_URI_DOC_DETAILS + "/id");

        cursorDocs = getDocInfo();

        while (cursorDocs.moveToNext()) {

            String docId = cursorDocs.getString(cursorDocs.getColumnIndex(DatabaseOpenHelper._ID));
            String auxPmid = cursorDocs.getString(cursorDocs.getColumnIndex(DatabaseOpenHelper.PMID));
            String auxDoi = cursorDocs.getString(cursorDocs.getColumnIndex(DatabaseOpenHelper.DOI));
            String auxIssn = cursorDocs.getString(cursorDocs.getColumnIndex(DatabaseOpenHelper.ISSN));
            String auxIsbn = cursorDocs.getString(cursorDocs.getColumnIndex(DatabaseOpenHelper.ISBN));
            String auxScopus = cursorDocs.getString(cursorDocs.getColumnIndex(DatabaseOpenHelper.SCOPUS));
            String auxArxiv = cursorDocs.getString(cursorDocs.getColumnIndex(DatabaseOpenHelper.ARXIV));
            String where = DatabaseOpenHelper._ID + " = '" + docId + "'";


            try {
                if (!auxPmid.isEmpty()) {
                    toProcess = true;
                    urlfilter = "pmid=" + URLEncoder.encode(auxPmid, "UTF-8");
                } else if (!auxDoi.isEmpty()) {
                    toProcess = true;
                    urlfilter = "doi=" + URLEncoder.encode(auxDoi, "UTF-8");
                } else if (!auxIssn.isEmpty()) {
                    toProcess = true;
                    urlfilter = "issn=" + URLEncoder.encode(auxIssn, "UTF-8");
                } else if (!auxIsbn.isEmpty()) {
                    toProcess = true;
                    urlfilter = "isbn=" + URLEncoder.encode(auxIsbn, "UTF-8");
                } else if (!auxScopus.isEmpty()) {
                    toProcess = true;
                    urlfilter = "scopus=" + URLEncoder.encode(auxScopus, "UTF-8");
                } else if (!auxArxiv.isEmpty()) {
                    toProcess = true;
                    urlfilter = "arxiv=" + URLEncoder.encode(auxArxiv, "UTF-8");
                }

            } catch (UnsupportedEncodingException uee) {
                Log.d(Globalconstant.TAG, "encode " + uee);
            }


            if (toProcess) {
                toProcess = false;

                String url = Globalconstant.get_catalog_url + urlfilter + "&view=stats&access_token=" + access_token;

                if (Globalconstant.LOG)
                    Log.d(Globalconstant.TAG, "getCatalogId url: " + url);

                JSONParser jParser = new JSONParser();
                ObjectMapper mapper = new ObjectMapper();
                JsonFactory factory = mapper.getFactory();
                List<InputStream> link = new ArrayList<InputStream>();
                link = jParser.getJACKSONFromUrl(url, false);

                try {

                    for (InputStream oneItem : link) {
                        JsonParser jp = factory.createParser(oneItem);
                        JsonNode rootNode = mapper.readTree(jp);


                        Iterator<JsonNode> ite = rootNode.iterator();

                        while (ite.hasNext()) {
                            JsonNode temp = ite.next();

                            if (temp.has("link")) {
                                values.put(DatabaseOpenHelper.WEBSITE, temp.get("link").asText());
                            }


                            if (temp.has("reader_count")) {
                                values.put(DatabaseOpenHelper.READER_COUNT, temp.get("reader_count").asText());
                            }

                            //update table
                            this.context.getContentResolver().update(uri_, values, where, null);

                            if (temp.has("reader_count_by_academic_status")) {

                                Iterator<Entry<String, JsonNode>> identifierIterator = temp.get("reader_count_by_academic_status").fields();

                                while (identifierIterator.hasNext()) {

                                    ContentValues academic_docs_values = new ContentValues();

                                    Map.Entry<String, JsonNode> entry = identifierIterator.next();

                                    academic_docs_values.put(DatabaseOpenHelper.DOC_DETAILS_ID, docId);
                                    academic_docs_values.put(DatabaseOpenHelper.STATUS, entry.getKey());
                                    academic_docs_values.put(DatabaseOpenHelper.COUNT, entry.getValue().asText());

                                    valueList.add(academic_docs_values);
                                }


                            }
                        }

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //Insert data on table Academic_status
        valuesArray = new ContentValues[valueList.size()];
        valueList.toArray(valuesArray);
        context.getContentResolver().bulkInsert(MyContentProvider.CONTENT_URI_ACADEMIC_DOCS, valuesArray);

        cursorDocs.close();
    }


    public void getProfileInfo(String url) {

        ContentValues values = new ContentValues();

        JSONParser jParser = new JSONParser();
        ObjectMapper mapper = new ObjectMapper();
        List<InputStream> link = new ArrayList<InputStream>();
        link = jParser.getJACKSONFromUrl(url, false);

        try {

            for (InputStream oneItem : link) {

                Map<String, Object> mapObject = mapper.readValue(oneItem, new TypeReference<Map<String, Object>>() {
                });

                values.put(DatabaseOpenHelper.PROFILE_ID, mapObject.get(DatabaseOpenHelper.ID).toString());
                values.put(DatabaseOpenHelper.PROFILE_FIRST_NAME, mapObject.get(DatabaseOpenHelper.PROFILE_FIRST_NAME).toString());
                values.put(DatabaseOpenHelper.PROFILE_LAST_NAME, mapObject.get(DatabaseOpenHelper.PROFILE_LAST_NAME).toString());
                values.put(DatabaseOpenHelper.PROFILE_DISPLAY_NAME, mapObject.get(DatabaseOpenHelper.PROFILE_DISPLAY_NAME).toString());
                values.put(DatabaseOpenHelper.PROFILE_LINK, mapObject.get(DatabaseOpenHelper.PROFILE_LINK).toString());

                Uri uri = this.context.getContentResolver().insert(MyContentProvider.CONTENT_URI_PROFILE, values);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
