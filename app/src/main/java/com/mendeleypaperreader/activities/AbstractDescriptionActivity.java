package com.mendeleypaperreader.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.mendeleypaperreader.R;
import com.mendeleypaperreader.utl.Globalconstant;

/**
 * 	Class to display full abstract of pdf articles.  
 *
 * @author PedroLourenco (pdrolourenco@gmail.com)
 */



public class AbstractDescriptionActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_abstract_description);

		if(Globalconstant.LOG)
			Log.d(Globalconstant.TAG, "DOC_DETAILS - Abstract: " + getAbstract());

		TextView v_abstract = (TextView) findViewById(R.id.abstractDescription);
		v_abstract.setText(getAbstract());

	}


	private String getAbstract(){		

		Bundle bundle = getIntent().getExtras();

		return bundle.getString("abstract");
	}
}
