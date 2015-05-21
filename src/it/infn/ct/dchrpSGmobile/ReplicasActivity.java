package it.infn.ct.dchrpSGmobile;

import it.infn.ct.dchrpSGmobile.pojos.NewProduct;
import it.infn.ct.dchrpSGmobile.pojos.Replica;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

enum Extension {
	TIF, PDF;
}

public class ReplicasActivity extends ListActivity {

	public static final String TAG = ReplicasActivity.class.getSimpleName();;
	private MyAdapter adapter;
	private ArrayList<Replica> replicas;
	private NewProduct product;
	private ProgressDialog pDialog;

	private AppPreferences _appPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_replicas);
		_appPrefs = new AppPreferences(getApplicationContext());
		Intent intent = getIntent();
		replicas = intent.getParcelableArrayListExtra("replicas");
		product = (NewProduct) intent.getSerializableExtra("product");
		adapter = new MyAdapter(getBaseContext(), R.layout.replica_lv_item);
		adapter.setData(replicas);
		getListView().setAdapter(adapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.replicas, menu);
		return true;
	}

	private class MyAdapter extends ArrayAdapter<Replica> {

		Context context;

		public MyAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);

			this.context = context;
		}

		public void setData(List<Replica> data) {
			clear();
			if (data != null) {
				for (Replica r : data) {
					add(r);
				}
			}
		}

		/* private view holder class */
		private class ViewHolder {
			ImageView imageView;
			TextView name;//, hostnameName;
			// final List<Column> visibleColumns = columns.subList(0, 4);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			Replica r = getItem(position);

			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.replica_lv_item, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
//				holder.hostnameName = (TextView) convertView
//						.findViewById(R.id.host);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.folderImage);
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();
			holder.name.setText(r.getName());

//			holder.hostnameName.setText(r.getLink());
			if (r.getEnabled() != 0)
				holder.imageView.setImageResource(R.drawable.enabled_download);
			else
				holder.imageView
						.setImageResource(R.drawable.not_enabled_replica);

			return convertView;
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent intent = new Intent(getBaseContext(),
				DownloadWebViewActivity.class);
		// if(isDownloadManagerAvailable(this));

		String url = replicas.get(position).getLink().trim().replace("\"", "");
		if (replicas.get(position).getEnabled() == 1) {
			if (!url.contains("http")) {
				// url = "https://earthserver-sg.consorzio-cometa.it/" + url;
				url = _appPrefs.getGateway() + "/" + url;
			}
			try {
				URL u1 = new URL(url);
				if (u1.getFile().toLowerCase().contains(".jpg")
						|| u1.getFile().toLowerCase().contains(".png")
						|| u1.getFile().toLowerCase().contains(".xml")) {
					intent.putExtra("URL", url);
					startActivity(intent);
				}else{
					File file = new File(Environment
							.getExternalStorageDirectory().getPath()
							+ "/Download/"
							+ product.getEntry("FileName")[1].toString());

					if (!file.exists()) {
						new DownloadFileFromURL(this).execute(url);
					} else
						openFile(file);
				}
//				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else
			Toast.makeText(
					this,
					"You can't download this file from this replica, this replica is not enabled.",
					Toast.LENGTH_SHORT).show();

	}

	private void openFile(File file) {
		Uri path = Uri.fromFile(file);

		if (path.getLastPathSegment().contains(".pdf")) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(path, "application/pdf");
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(this,
						"No application found to open the dowloaded file.",
						Toast.LENGTH_SHORT).show();
				Log.e(TAG, e.getMessage());
			}
		} else
			Toast.makeText(this,
					"No application found to open the dowloaded file.",
					Toast.LENGTH_SHORT).show();
	}

	/**
	 * Background Async Task to download file
	 * */
	class DownloadFileFromURL extends AsyncTask<String, String, String> {

		Activity baseActivity;
		private final HttpClient Client = new DefaultHttpClient();
		private String Content;

		protected DownloadFileFromURL(Activity baseActivity) {
			this.baseActivity = baseActivity;
		}

		/**
		 * Before starting background thread Show Progress Bar Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pDialog = new ProgressDialog(baseActivity);
			pDialog.setMessage("Downloading file. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setMax(100);
			pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pDialog.setCancelable(true);
			pDialog.show();
			// showDialog(ProgressDialog.STYLE_HORIZONTAL);
		}

		/**
		 * Downloading file in background thread
		 * */
		@Override
		protected String doInBackground(String... f_url) {
			int count;
			try {
				URL url = new URL(f_url[0]);

				HttpGet httpget = new HttpGet(f_url[0]);
				httpget.setHeader("Cookie", _appPrefs.getCookie());
				HttpContext localContext = new BasicHttpContext();

				HttpResponse response = Client.execute(httpget, localContext);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					long lenghtOfFile = entity.getContentLength();
					InputStream input = entity.getContent();

					FileOutputStream output = new FileOutputStream(new File(
							Environment.getExternalStorageDirectory().getPath()
									+ "/Download/",
							product.getEntry("FileName")[1].toString()));

					byte data[] = new byte[1024];

					long total = 0;

					while ((count = input.read(data)) != -1) {
						total += count;
						// publishing the progress....
						// After this onProgressUpdate will be called
						publishProgress(""
								+ (int) ((total * 100) / lenghtOfFile));

						// writing data to file
						output.write(data, 0, count);
					}

					// flushing output
					output.flush();

					// closing streams
					output.close();
					input.close();
				}
				// ResponseHandler<String> responseHandler = new
				// BasicResponseHandler();
				// Content = Client.ex.execute(httpget, responseHandler);

				// HttpURLConnection conection =
				// (HttpURLConnection)url.openConnection();
				// conection.setRequestMethod("GET");
				// conection.setDoOutput(true);
				// String tmp = _appPrefs.getCookie();
				// conection.setRequestProperty("Cookie",
				// _appPrefs.getCookie());
				//
				// conection.connect();
				// // this will be useful so that you can show a tipical 0-100%
				// // progress bar
				// int lenghtOfFile = conection.getContentLength();
				//
				// // download the file
				// // InputStream input = conection.getInputStream();
				// InputStream input = new BufferedInputStream(url.openStream(),
				// 8192);
				//
				// FileOutputStream output = new FileOutputStream(new File(
				// Environment.getExternalStorageDirectory().getPath()
				// + "/Download/",
				// product.getEntry("FileName")[1].toString()));
				// // Output stream
				// // OutputStream output = new FileOutputStream(Environment
				// // .getExternalStorageDirectory().getPath()
				// // + "/Download/"
				// // + product.getEntry("FileName")[1].toString());
				//
				// byte data[] = new byte[1024];
				//
				// long total = 0;
				//
				// while ((count = input.read(data)) != -1) {
				// total += count;
				// // publishing the progress....
				// // After this onProgressUpdate will be called
				// publishProgress("" + (int) ((total * 100) / lenghtOfFile));
				//
				// // writing data to file
				// output.write(data, 0, count);
				// }
				//
				// // flushing output
				// output.flush();
				//
				// // closing streams
				// output.close();
				// input.close();

			} catch (Exception e) {
				Log.e("Error: ", e.getMessage());
			}

			return null;
		}

		/**
		 * Updating progress bar
		 * */
		protected void onProgressUpdate(String... progress) {
			// setting progress percentage
			pDialog.setProgress(Integer.parseInt(progress[0]));
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		@Override
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after the file was downloaded
			pDialog.dismiss();
			// // Displaying downloaded image into image view
			// // Reading image path from sdcard
			// String imagePath = Environment.getExternalStorageDirectory()
			// .getPath() + "/Download/downloadedfile.jpg";
			// // setting downloaded into image view
			// ImageView my_Image = (ImageView) findViewById(R.id.folderImage);
			// my_Image.setImageDrawable(Drawable.createFromPath(imagePath));

			File file = new File(Environment.getExternalStorageDirectory()
					.getPath()
					+ "/Download/"
					+ product.getEntry("FileName")[1].toString());

			if (file.exists()) {
				openFile(file);
			}
			// File f = new File(Environment
			// .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
			// +);
			// Intent intent = new Intent(Intent.ACTION_VIEW,
			// Uri.fromFile(f));
			// intent.setType("application/pdf");
			// PackageManager pm = getPackageManager();
			// List<ResolveInfo> activities = pm.queryIntentActivities(intent,
			// 0);
			// if (activities.size() > 0) {
			// startActivity(intent);
			// } else {
			// // Do something else here. Maybe pop up a Dialog or Toast
			// }

		}

	}

}
