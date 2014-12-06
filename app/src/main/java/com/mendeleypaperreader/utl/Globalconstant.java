package com.mendeleypaperreader.utl;

/**
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */


public class Globalconstant {

    public static String TAG = "PaperReader";
    public static final boolean LOG = false;

    public static String shared_file_name = "share_pref";


    //Login information
    public static String CLIENT_ID = "177";
    // Use your own client id
    public static String CLIENT_SECRET = "V!yw8[5_0ZliXK$0";
    // Use your own client secret
    public static String REDIRECT_URI = "http://localhost";
    public static String GRANT_TYPE = "refresh_token";
    public static String TOKEN_URL = "https://api-oauth2.mendeley.com/oauth/token";

    //API URLs

    public final static String get_docs_notes = "https://mix.mendeley.com/annotations?document_id=#docId#&limit=200&access_token=";
    public final static String get_docs_in_groups = "https://mix.mendeley.com/documents?group_id=#groupId#&view=all&limit=400&access_token=";
    public static String get_groups_url = "https://mix.mendeley.com/groups?access_token=";
    public static String get_catalog_url = "https://mix.mendeley.com/catalog?";
    public static String get_metadata_url = "https://mix.mendeley.com/metadata";
    public static String get_catalod_id_url = "https://mix.mendeley.com/catalog/doc_id?view=all&access_token=";
    public static String get_user_library_url = "https://mix.mendeley.com/documents?view=all&limit=400&access_token=";
    public static String get_docs_in_folders = "https://mix.mendeley.com/folders/id/documents?access_token=";
    public static String get_docs_ann = "https://mix.mendeley.com/annotations/?document_id=doc_id&access_token=";
    public static String get_user_folders_url = "https://mix.mendeley.com/folders?access_token=";
    public static String get_files = "https://mix.mendeley.com/files?access_token=";
    public static String get_files_by_doc_id = "https://mix.mendeley.com/files/file_id?access_token=";
    public static String get_profile = "https://mix.mendeley.com/profiles/me?access_token=";


    //URLs
    public static String ISSN_URL = "http://www.worldcat.org/issn/";
    public static String DOI_URL = "http://dx.doi.org/";
    public static String PMID_URL = "http://www.ncbi.nlm.nih.gov/m/pubmed/";


    //Json variables
    public static String ID = "id";
    public static String TYPE = "type";
    public static String MONTH = "month";
    public static String YEAR = "year";
    public static String LAST_MODIFIED = "last_modified";
    public static String DAY = "day";
    public static String GROUP_ID = "group_id";
    public static String SOURCE = "source";
    public static String TITLE = "title";
    public static String REVISION = "revision";
    public static String IDENTIFIERS = "identifiers";
    public static String PMID = "pmid";
    public static String DOI = "doi";
    public static String ISSN = "issn";
    public static String ABSTRACT = "abstract";
    public static String PROFILE_ID = "profile_id";
    public static String AUTHORS = "authors";
    public static String FORENAME = "first_name";
    public static String SURNAME = "last_name";
    public static String ADDED = "added";
    public static String CREATED = "created";
    public static String PAGES = "pages";
    public static String VOLUME = "volume";
    public static String ISSUE = "issue";
    public static String WEBSITE = "website";
    public static String WEBSITES = "websites";
    public static String PUBLISHER = "publisher";
    public static String CITY = "city";
    public static String EDITION = "edition";
    public static String INSTITUTION = "institution";
    public static String SERIES = "series";
    public static String EDITORS = "editors";
    public static String READ = "read";
    public static String STARRED = "starred";
    public static String AUTHORED = "authored";
    public static String TAGS = "tags";
    public static String CONFIRMED = "confirmed";
    public static String HIDDEN = "hidden";
    public static String NAME = "name";
    public static String PARENT = "parent";
    public static String PARENT_ID = "parent_id";
    public static String GROUP = "group";
    public static String DOCUMENTS_ID = "document_ids";
    public static String DOCUMENTS = "documents";
    public static String FILE_ID = "id";
    public static String FILE_NAME = "file_name";
    public static String FILE_MIME_TYPE = "mime_type";
    public static String FILE_DOC_ID = "document_id";
    public static String FILE_FILEHASH = "filehash";
    public static String PROFILE_DISPLAY_NAME = "display_name";
    public static String PROFILE_LINK = "link";
    public static String FOLDERS_DOCS_ID = "doc_id";
    public static String CATALOG_ID = "catalog_id";
    public static String GROUP_NAME = "name";

    public static final String[] MYLIBRARY =
            {
                    "All Documents",
                    "Recently Added",
                    "Favorites",
                    "My Publications",
                    "Trash",
            };

}
