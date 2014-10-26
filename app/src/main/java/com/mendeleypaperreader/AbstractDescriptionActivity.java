package com.mendeleypaperreader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.mendeleypaperreader.utl.Globalconstant;

/**
 * classname: AbstractDescriptionActivity 
 * 	Class to display full abstract of pdf articles.  
 * 
 * @date July 8, 2014
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
		String v_abstract = bundle.getString("abstract");

		return v_abstract;
	}
}
