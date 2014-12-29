package com.mendeleypaperreader.jsonParser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

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

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */


public class LoadData {

    private Context context;
    private static String access_token;
    private GetDataBaseInformation getDataBaseInformation;

    public LoadData(Context context) {
        this.context = context;

        SessionManager session = new SessionManager(this.context);
        access_token = session.LoadPreference("access_token");

        getDataBaseInformation = new GetDataBaseInformation(this.context);

    }
    
    
    
    public void downloadFiles(){

        
        Thread downloaderThread;

        Log.d(Globalconstant.TAG, "downloadFiles: ");

       Cursor cursorFiles = getDataBaseInformation.getFile();
       
        while (cursorFiles.moveToNext()) {
            
            String fileId = cursorFiles.getString(cursorFiles.getColumnIndex(DatabaseOpenHelper._ID));
            String docId = cursorFiles.getString(cursorFiles.getColumnIndex(DatabaseOpenHelper.DOCUMENT_ID));
            String url = Globalconstant.get_files_by_doc_id.replace("file_id", fileId) + access_token;

            Log.d(Globalconstant.TAG, "URL: " + url);
            Log.d(Globalconstant.TAG, "fileId: " + fileId);

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


    }


    private Cursor getDocId() {

        if (Globalconstant.LOG)
            Log.d(Globalconstant.TAG, "getGROUPS- LOAD DATA");

        String[] projection = new String[]{DatabaseOpenHelper._ID};
        Uri uri = MyContentProvider.CONTENT_URI_DOC_DETAILS;


        return this.context.getContentResolver().query(uri, projection, null, null, null);


    }


    public void getDocNotes() {

        Cursor cursorDocId = getDocId();
        ObjectMapper mapper = new ObjectMapper();
        while (cursorDocId.moveToNext()) {

            String url = Globalconstant.get_docs_notes.replace("#docId#", cursorDocId.getString(cursorDocId.getColumnIndex(DatabaseOpenHelper._ID)));

            getNotes(url + access_token);
        }

    }


    private void getNotes(String url) {

        ContentValues note_values = new ContentValues();

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


                    if (temp.has(DatabaseOpenHelper.NOTE_ID)) {

                        note_values.put(DatabaseOpenHelper.NOTE_ID, temp.get(DatabaseOpenHelper.NOTE_ID).asText());
                    }

                    if (temp.has(DatabaseOpenHelper.TYPE)) {

                        String noteType = temp.get(DatabaseOpenHelper.TYPE).asText();

                        if (!noteType.equals("note")) {
                            break;
                        }

                        note_values.put(DatabaseOpenHelper.TYPE, noteType);
                    }
                    if (temp.has(DatabaseOpenHelper.TEXT)) {

                        note_values.put(DatabaseOpenHelper.TEXT, temp.get(DatabaseOpenHelper.TEXT).asText().replace("<br/>", " "));
                    }
                    if (temp.has(DatabaseOpenHelper.DOCUMENT_ID)) {

                        note_values.put(DatabaseOpenHelper.DOCUMENT_ID, temp.get(DatabaseOpenHelper.DOCUMENT_ID).asText());
                    }
                    Uri uri = this.context.getContentResolver().insert(MyContentProvider.CONTENT_URI_DOC_NOTES, note_values);

                }

                jp.close();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void getGroups(String url) {

        ContentValues values = new ContentValues();

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

                    if (temp.has(DatabaseOpenHelper.NAME)) {
                        values.put(DatabaseOpenHelper.GROUPS_NAME, temp.get(DatabaseOpenHelper.NAME).asText());
                    }

                    if (temp.has(DatabaseOpenHelper.ID)) {
                        values.put(DatabaseOpenHelper._ID, temp.get(DatabaseOpenHelper.ID).asText());
                    }
                    Uri uri = this.context.getContentResolver().insert(MyContentProvider.CONTENT_URI_GROUPS, values);

                }

                jp.close();
            }


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

        ContentValues values = new ContentValues();

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

                    Uri uri = this.context.getContentResolver().insert(MyContentProvider.CONTENT_URI_FILES, values);

                }

                jp.close();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void getFolders(String url) {

        ContentValues values = new ContentValues();

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

                    Uri uri = this.context.getContentResolver().insert(MyContentProvider.CONTENT_URI_FOLDERS, values);
                    getDocsInFolder(temp.get(DatabaseOpenHelper.ID).asText());
                }

                jp.close();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void getUserLibrary(String url) {

        ContentValues values = new ContentValues();
        ContentValues authors_values = new ContentValues();
        ContentValues tagsValues = new ContentValues();
        JSONParser jParser = new JSONParser();
        String docTitle, docId;

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

                            String tagName = tagIterator.next().asText();
                            tags += tagName + ",";
                            tagsValues.put(DatabaseOpenHelper._ID, temp.get(DatabaseOpenHelper.ID).asText());
                            tagsValues.put(DatabaseOpenHelper.TAG_NAME, tagName);
                            Uri uriTag = context.getContentResolver().insert(MyContentProvider.CONTENT_URI_DOC_TAGS, tagsValues);
                            values.put(DatabaseOpenHelper.TAGS, tags.substring(0, tags.length() - 1));
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

                            Uri uri_authors = context.getContentResolver().insert(MyContentProvider.CONTENT_URI_AUTHORS, authors_values);

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

                            Map.Entry<String, JsonNode> entry = identifierIterator.next();

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

                    Uri uri = this.context.getContentResolver().insert(MyContentProvider.CONTENT_URI_DOC_DETAILS, values);

                }
                jp.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void getDocsInFolder(String folderId) {

        ContentValues values = new ContentValues();

        String auxurl = Globalconstant.get_docs_in_folders;
        String url = auxurl.replace("id", folderId) + access_token;

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

                        values.put(DatabaseOpenHelper.FOLDER_ID, folderId);
                        values.put(DatabaseOpenHelper.DOC_DETAILS_ID, temp.get(DatabaseOpenHelper.ID).asText());

                    } else {
                        values.put(DatabaseOpenHelper.FOLDER_ID, "");
                        values.put(DatabaseOpenHelper.DOC_DETAILS_ID, "");
                    }
                    Uri uri = this.context.getContentResolver().insert(MyContentProvider.CONTENT_URI_FOLDERS_DOCS, values);

                }
                jp.close();
            }

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
        ContentValues values1 = new ContentValues();
        ContentValues academic_docs_values = new ContentValues();
        Cursor cursorDocs;
        String urlfilter = null;
        boolean toProcess = true;
        Uri uri_ = Uri.parse(MyContentProvider.CONTENT_URI_DOC_DETAILS + "/id");

        cursorDocs = getDocInfo();

        while (cursorDocs.moveToNext()) {

            String docId = cursorDocs.getString(cursorDocs.getColumnIndex(DatabaseOpenHelper._ID));
            String auxPmid = cursorDocs.getString(cursorDocs.getColumnIndex(DatabaseOpenHelper.PMID));
            String pmid = URLEncoder.encode(auxPmid);
            String auxDoi = cursorDocs.getString(cursorDocs.getColumnIndex(DatabaseOpenHelper.DOI));
            String doi = URLEncoder.encode(auxDoi);
            String auxIssn = cursorDocs.getString(cursorDocs.getColumnIndex(DatabaseOpenHelper.ISSN));
            String issn = URLEncoder.encode(auxIssn);
            String auxIsbn = cursorDocs.getString(cursorDocs.getColumnIndex(DatabaseOpenHelper.ISBN));
            String isbn = URLEncoder.encode(auxIsbn);
            String auxScopus = cursorDocs.getString(cursorDocs.getColumnIndex(DatabaseOpenHelper.SCOPUS));
            String scopus = URLEncoder.encode(auxScopus);
            String auxArxiv = cursorDocs.getString(cursorDocs.getColumnIndex(DatabaseOpenHelper.ARXIV));
            String arxiv = URLEncoder.encode(auxArxiv);

            String where = DatabaseOpenHelper._ID + " = '" + docId + "'";
            String where2 = DatabaseOpenHelper._ID + " = '" + docId + "' and " + DatabaseOpenHelper.READER_COUNT + " IS NULL";
            String where3 = DatabaseOpenHelper._ID + " = '" + docId + "' and " + DatabaseOpenHelper.WEBSITE + " IS NULL";


            if (!pmid.isEmpty()) {
                toProcess = true;
                urlfilter = "pmid=" + pmid;
            } else if (!doi.isEmpty()) {
                toProcess = true;
                urlfilter = "doi=" + doi;
            } else if (!issn.isEmpty()) {
                toProcess = true;
                urlfilter = "issn=" + issn;
            } else if (!isbn.isEmpty()) {
                toProcess = true;
                urlfilter = "isbn=" + isbn;
            } else if (!scopus.isEmpty()) {
                toProcess = true;
                urlfilter = "scopus=" + scopus;
            } else if (!arxiv.isEmpty()) {
                toProcess = true;
                urlfilter = "arxiv=" + arxiv;
            } else {
                toProcess = false;
                //update table
                values.put(DatabaseOpenHelper.READER_COUNT, "0");
                this.context.getContentResolver().update(uri_, values, where, null);
            }


            if (toProcess) {

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

                                    Map.Entry<String, JsonNode> entry = identifierIterator.next();

                                    academic_docs_values.put(DatabaseOpenHelper.DOC_DETAILS_ID, docId);
                                    academic_docs_values.put(DatabaseOpenHelper.STATUS, entry.getKey());
                                    academic_docs_values.put(DatabaseOpenHelper.COUNT, entry.getValue().asText());
                                    Uri uri = this.context.getContentResolver().insert(MyContentProvider.CONTENT_URI_ACADEMIC_DOCS, academic_docs_values);
                                }
                            }
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            values.put(DatabaseOpenHelper.READER_COUNT, "0");
            values1.put(DatabaseOpenHelper.WEBSITE, "");
            this.context.getContentResolver().update(uri_, values, where2, null);
            this.context.getContentResolver().update(uri_, values1, where3, null);

        }
    }


    public void getProfileInfo(String url) {

        ContentValues values = new ContentValues();

        JSONParser jParser = new JSONParser();
        ObjectMapper mapper = new ObjectMapper();
        List<InputStream> link = new ArrayList<InputStream>();
        link = jParser.getJACKSONFromUrl(url, true);

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