package it.infn.ct.dchrpSGmobile;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PoCActivity extends ListActivity {

	private static String[] Pocs;

//	private static final String[] Pocs = new String[] { "BELSPO PoC",
//			"ICCU PoC", "INDICATE Use Case" };

	private AppPreferences _appPrefs;
	// private ListView lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Pocs = getResources().getStringArray(R.array.def_PoCs);
		// setContentView(R.layout.activity_proofs_of_concept);
		// lv = getListView();
		_appPrefs = new AppPreferences(getApplicationContext());
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, Pocs));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.po_c, menu);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
//		if (position != 0) {
			_appPrefs.saveDefaultRepo("");
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("poc", Pocs[position]);
			startActivity(intent);
//		} else
//			Toast.makeText(getBaseContext(), "There aren't still repository for this PoC", Toast.LENGTH_LONG).show();

		super.onListItemClick(l, v, position, id);
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
		// .setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Closing Activity")
		.setMessage("Are you sure you want to close this activity?")
		.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						_appPrefs.saveCookie("");
						finish();
					}

				}).setNegativeButton("No", null).show();
	}

}
