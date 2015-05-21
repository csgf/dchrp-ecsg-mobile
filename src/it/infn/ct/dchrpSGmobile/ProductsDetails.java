package it.infn.ct.dchrpSGmobile;

import it.infn.ct.dchrpSGmobile.net.RESTLoader;
import it.infn.ct.dchrpSGmobile.net.RESTLoader.RESTResponse;
import it.infn.ct.dchrpSGmobile.pojos.Column;
import it.infn.ct.dchrpSGmobile.pojos.Field;
import it.infn.ct.dchrpSGmobile.pojos.Metadata;
import it.infn.ct.dchrpSGmobile.pojos.NewProduct;
import it.infn.ct.dchrpSGmobile.pojos.Replica;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ProductsDetails extends FragmentActivity implements
		LoaderCallbacks<RESTLoader.RESTResponse> {

	private static final String TAG = ProductsDetails.class.getSimpleName();
	private String shibCookie;
	private AppPreferences _appPrefs;
	// private static final String SERVER_URL =
	// "https://earthserver-sg.consorzio-cometa.it/glibrary/links2/";
	private static final String SERVER_URL = "/glibrary/links2/";

	Metadata metadata;
	NewProduct product = new NewProduct();
	ArrayList<Column> columns;
	MyAdapter adapter;
	private ArrayList<Replica> replicas;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_products_details);

		_appPrefs = new AppPreferences(this.getBaseContext());
		Intent intent = getIntent();
		product = (NewProduct) intent.getSerializableExtra("product");
		columns = intent.getParcelableArrayListExtra("columns");
		metadata = (Metadata) intent.getSerializableExtra("metadata");

		ListView lv = (ListView) findViewById(R.id.listView1);
		adapter = new MyAdapter(this, R.layout.activity_product_detail_lv_item);
		ImageView imgView = (ImageView) findViewById(R.id.imageView1);
		ArrayList<String[]> attributes = new ArrayList<String[]>();
		for (Column c : columns) {
			Object[] obj = product.getEntry(c.getDataIndex());
			if (c.getId() != null && c.getId().contains("thumb")) {
				byte[] tmp = Base64.decode((String) obj[1], Base64.DEFAULT);
				imgView.setImageBitmap(BitmapFactory.decodeByteArray(tmp, 0,
						tmp.length));
			} else {
				attributes
						.add(new String[] { c.getDataIndex(), (String) obj[1] });
			}

		}
		adapter.setData(attributes);
		lv.setAdapter(adapter);

		shibCookie = _appPrefs.getCookie();
		if (!shibCookie.equals("")) {
			String productId = "";
			ArrayList<Field> fields = metadata.getFields();
			for (Field field : fields) {
				String k = field.getField().get("name");
				if (k.contains("FILE"))
					productId = product.getEntry(k)[1].toString();
			}
			if (!productId.equals("")) {
				Uri uri = Uri.parse(_appPrefs.getGateway() + SERVER_URL
						+ _appPrefs.getDefaultRepository() + "/" + productId);
				Log.i(TAG, uri.toString());

				Bundle args1 = new Bundle();
				args1.putParcelable("ARGS_URI", uri);

				Bundle params = new Bundle();
				params.putString("Cookie", shibCookie);
				args1.putParcelable("ARGS_PARAMS", params);

				getSupportLoaderManager().initLoader(1, args1, this);
			}

			Button btnReplicas = (Button) findViewById(R.id.btnReplicas);
			btnReplicas.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					 Intent intent = new Intent(getBaseContext(),
					 ReplicasActivity.class);
//					Intent intent = new Intent(getBaseContext(),
//							ReplicasMapActivity.class);
					intent.putParcelableArrayListExtra("replicas", replicas);
					intent.putExtra("product", product);
					startActivity(intent);
				}
			});

		}
	}

	@Override
	public Loader<RESTResponse> onCreateLoader(int id, Bundle args) {
		if (args != null && args.containsKey("ARGS_URI")) {
			Uri action = args.getParcelable("ARGS_URI");
			if (args.containsKey("ARGS_PARAMS")) {

				Bundle params = args.getParcelable("ARGS_PARAMS");

				return new RESTLoader(this.getBaseContext(),
						RESTLoader.HTTPVerb.GET, action, params);
			} else {
				return new RESTLoader(this.getBaseContext(),
						RESTLoader.HTTPVerb.GET, action);
			}
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<RESTResponse> loader, RESTResponse data) {

		int code = data.getCode();
		String json = data.getData();
		Log.d(TAG, "" + code);
		if (code == 200 && !json.equals("")) {
			Log.d(TAG, json);
			replicas = getLinksFromJson(json);
		}
	}

	@Override
	public void onLoaderReset(Loader<RESTResponse> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.products_details, menu);
		return true;
	}

	private class MyAdapter extends ArrayAdapter<String[]> {

		Context context;

		public MyAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			this.context = context;
		}

		public void setData(ArrayList<String[]> data) {
			clear();
			if (data != null) {
				for (String[] s : data) {
					add(s);
				}

			}
		}

		private class ViewHolder {
			TextView field, value;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			String[] element = getItem(position);

			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.activity_product_detail_lv_item, null);
				holder = new ViewHolder();
				holder.field = (TextView) convertView
						.findViewById(R.id.fieldName);
				holder.value = (TextView) convertView
						.findViewById(R.id.fieldValue);

				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();

			holder.field.setText(element[0]);
			holder.value.setText(element[1]);

			return convertView;
		}

	}

	private ArrayList<Replica> getLinksFromJson(String json) {
		ArrayList<Replica> replicas = new ArrayList<Replica>();
		try {
			JSONArray replicasArray = (JSONArray) new JSONTokener(json)
					.nextValue();

			for (int i = 0; i < replicasArray.length(); i++) {
				JSONObject replicaJSON = replicasArray.getJSONObject(i);
				Replica r = new Replica(replicaJSON.getDouble("lat"),
						replicaJSON.getDouble("lng"),
						replicaJSON.getInt("enabled"),
						replicaJSON.getString("link"),
						replicaJSON.getString("name"));
				replicas.add(r);
			}

		} catch (JSONException e) {
			Log.e(TAG, "Failed to parse JSON.", e);
		}
		findViewById(R.id.btnReplicas).setEnabled(true);
		return replicas;
	}
}
