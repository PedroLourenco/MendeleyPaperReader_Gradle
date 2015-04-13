package com.mendeleypaperreader.util;

/**
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */


public class Globalconstant {

    public static String TAG = "PaperReader";
    public static final boolean LOG = false;

    public static String shared_file_name = "share_pref";
    public static boolean isTaskRunning;
    public static String firstLoad;

    public static boolean isFirstLoad = true;

    //Login information
    public static String CLIENT_ID = "177";
    // Use your own client id
    public static String CLIENT_SECRET = "V!yw8[5_0ZliXK$0";
    // Use your own client secret
    public static String REDIRECT_URI = "http://localhost";
    public static String GRANT_TYPE = "refresh_token";
    public static String TOKEN_URL = "https://api-oauth2.mendeley.com/oauth/token";



    public static final String mBroadcastStringAction = "com.truiton.broadcast.string";
    public static final String mBroadcastIntegerAction = "com.truiton.broadcast.integer";
    public static final String mBroadcastArrayListAction = "com.truiton.broadcast.arraylist";


    //API URLs

    public final static String get_docs_notes = "https://mix.mendeley.com/annotations?document_id=#docId#&limit=200&access_token=";
    public final static String get_docs_notes2 = "https://mix.mendeley.com/annotations?document_id=";
    public final static String get_docs_in_groups = "https://mix.mendeley.com/documents?group_id=#groupId#&view=all&limit=400&access_token=";
    public static String get_groups_url = "https://mix.mendeley.com/groups?limit=400&access_token=";
    public static String get_catalog_url = "https://mix.mendeley.com/catalog?";
    public static String get_metadata_url = "https://mix.mendeley.com/metadata";
    public static String get_catalod_id_url = "https://mix.mendeley.com/catalog/doc_id?view=all&access_token=";
    public static String get_user_library_url = "https://mix.mendeley.com/documents?view=all&limit=400&access_token=";
    public static String get_docs_in_folders = "https://mix.mendeley.com/folders/id/documents?access_token=";
    public static String get_docs_ann = "https://mix.mendeley.com/annotations/?document_id=doc_id&access_token=";
    public static String get_user_folders_url = "https://mix.mendeley.com/folders?limit=400&access_token=";
    public static String get_files = "https://mix.mendeley.com/files?limit=400&access_token=";
    public static String get_files_by_doc_id = "https://mix.mendeley.com/files/file_id?access_token=";
    public static String get_profile = "https://mix.mendeley.com/profiles/me?access_token=";


    //URLs
    public static String ISSN_URL = "http://www.worldcat.org/issn/";
    public static String DOI_URL = "http://dx.doi.org/";
    public static String PMID_URL = "http://www.ncbi.nlm.nih.gov/m/pubmed/";


    public static final String[] MYLIBRARY =
            {
                    "All Documents",
                    "Recently Added",
                    "Favorites",
                    "My Publications",
                    "Trash",
            };

}