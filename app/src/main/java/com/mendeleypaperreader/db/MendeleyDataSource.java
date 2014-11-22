package com.mendeleypaperreader.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */

public class MendeleyDataSource {


    private SQLiteDatabase db;
    private DatabaseOpenHelper mendeley_library;


    public MendeleyDataSource(Context context) {
        mendeley_library = new DatabaseOpenHelper(context);
    }


    public void open() throws SQLException {

        db = mendeley_library.getWritableDatabase();
    }

    public void close() {


        mendeley_library.close();
    }
}