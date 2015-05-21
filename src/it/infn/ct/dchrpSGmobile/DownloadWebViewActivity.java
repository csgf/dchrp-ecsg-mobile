package it.infn.ct.dchrpSGmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DownloadWebViewActivity extends Activity {

	private AppPreferences _appPrefs;

	// private ProgressDialog progressDialog;
	// private ProgressDialog pDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.web_view);
		// final ProgressBar Pbar;
		// final TextView txtview = (TextView)findViewById(R.id.tV1);
		// Pbar = (ProgressBar) findViewById(R.id.pB1);
		_appPrefs = new AppPreferences(getApplicationContext());

		WebView myWebView = (WebView) findViewById(R.id.webview);
		Intent intent = getIntent();
		String url = intent.getStringExtra("URL");
		Log.d("CIAO", url);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setCookie("Cookie", _appPrefs.getCookie());

		myWebView.setWebViewClient(new MyWebViewClient(this));

		WebSettings webSettings = myWebView.getSettings();
		webSettings.setBuiltInZoomControls(true);
		myWebView.getSettings().setLoadWithOverviewMode(true);
		myWebView.getSettings().setUseWideViewPort(true);
		if (url.contains("http"))
			myWebView.loadUrl(url);
		else
			myWebView.loadUrl(_appPrefs.getGateway() + "/" + url);

		final ProgressBar Pbar;
		final TextView txtview = (TextView) findViewById(R.id.tv1);
		Pbar = (ProgressBar) findViewById(R.id.pb1);
		final Activity activity = this;
		myWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				// Activities and WebViews measure progress with different
				// scales.
				// The progress meter will automatically disappear when we reach
				// 100%
				activity.setProgress(progress * 100);
				if(progress < 100 && Pbar.getVisibility() == ProgressBar.INVISIBLE){
                    Pbar.setVisibility(ProgressBar.VISIBLE);
                    txtview.setVisibility(View.VISIBLE);
                }
                Pbar.setProgress(progress);
                if(progress == 100) {
                    Pbar.setVisibility(ProgressBar.INVISIBLE);
                    txtview.setVisibility(View.INVISIBLE);
                }
			}

		});
		// pdRing = ProgressDialog.show(this, "Downloading...",
		// "Downloading data.", true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.web_view, menu);
		return true;
	}

	private class MyWebViewClient extends WebViewClient {
		// private String args = null;

		private Activity baseActivity;

		public MyWebViewClient(DownloadWebViewActivity downloadWebViewActivity) {
			this.baseActivity = downloadWebViewActivity;
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// if
			// (!Uri.parse(url).getHost().equals("earthserver-sg.consorzio-cometa.it"))
			// {
			// // This is my web site, so do not override; let my WebView load
			// // the page
			// return false;
			// } else

			// Otherwise, the link is not for a page on my site, so launch
			// another Activity that handles URLs
			return false;
		}

		// @Override
		// public void onPageStarted(WebView view, String url, Bitmap favicon) {
		// super.onPageStarted(view, url, favicon);
		// pdRing.show();
		// }
		@Override
		public void onLoadResource(WebView view, String url) {
			// if (pDialog == null) {
			// pDialog = new ProgressDialog(baseActivity);
			// pDialog.setMessage("Downloading file. Please wait...");
			// pDialog.setIndeterminate(false);
			// pDialog.setMax(100);
			// pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			// pDialog.setCancelable(true);
			// pDialog.show();
			//
			// }
			// loadingFinished = false;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// if (pDialog != null)
			// pDialog.dismiss();
			// File file = new
			// File(Environment.getExternalStorageDirectory().getPath()+"/Download/STANDARD_IMPIANTI_ELETTRICI_CIVILI.pdf");
			//
			// if (file.exists()) {
			// Uri path = Uri.fromFile(file);
			// Intent intent = new Intent(Intent.ACTION_VIEW);
			// intent.setDataAndType(path, "application/pdf");
			// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//
			// try {
			// startActivity(intent);
			// }
			// catch (ActivityNotFoundException e) {
			//
			// }
			// }
			// File f = new File(Environment
			// .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
			// + "STANDARD_IMPIANTI_ELETTRICI_CIVILI.pdf");
			// // if(url.contains("pdf")){
			// Log.d("FILE PATH: ", Uri.fromFile(f).toString());
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
			// // }

		}

	}

}
