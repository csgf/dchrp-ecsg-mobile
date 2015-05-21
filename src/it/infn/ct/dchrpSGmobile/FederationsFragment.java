package it.infn.ct.dchrpSGmobile;

import it.infn.ct.dchrpSGmobile.net.ImageDownloaderTask;
import it.infn.ct.dchrpSGmobile.net.RESTLoader;
import it.infn.ct.dchrpSGmobile.net.RESTLoader.RESTResponse;
import it.infn.ct.dchrpSGmobile.pojos.Federation;
import it.infn.ct.dchrpSGmobile.pojos.IDP;
import it.infn.ct.dchrpSGmobile.utils.ConnectionDetector;
import it.infn.ct.dchrpSGmobile.utils.DialogFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FederationsFragment extends ListFragment implements
		LoaderCallbacks<RESTLoader.RESTResponse>,
		DialogInterface.OnClickListener {

	private static final String TAG = FederationsFragment.class.getSimpleName();
	private static final String SERVER_URL = "https://gridp.garr.it/ds/WAYF?entityID=";
	private static final String SUFFIX = "/shibboleth&json=true";

	private ProgressDialog pdRing;

	private int mCurrentPosition = -1;
	OnFederationSelectedListener mCallback;
	private FederationAdapter adapter;
	private AppPreferences _appPrefs;
	private ArrayList<Federation> federations;

	private ConnectionDetector cd;

	// Interfaccia per l'invio di messagggi tra i fragments
	public interface OnFederationSelectedListener {
		/** Called by FederationsFragment when a list item is selected */
		public void onFederationSelected(Federation federation, int position);
	}

	// @Override
	// public void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	//
	// // layout diverso per terminali con versione inferiore alla 3
	// int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
	// android.R.layout.simple_list_item_activated_1
	// : android.R.layout.simple_list_item_1;
	//
	// // Creo un array adapter con le federations
	// mAdapter = new ArrayAdapter<String>(
	// this.getActivity().getBaseContext(), layout);
	//
	// setListAdapter(mAdapter);
	//
	// Uri uri = Uri.parse(SERVER_URL
	// + "https://earthserver-sg.consorzio-cometa.it" + SUFFIX);
	// Log.i(TAG, uri.toString());
	//
	// Bundle args = new Bundle();
	// args.putParcelable("ARGS_URI", uri);
	//
	// getLoaderManager().initLoader(1, args, this);
	// }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		_appPrefs = new AppPreferences(this.getActivity().getBaseContext());
		// if (savedInstanceState != null) {
		Uri uri = Uri.parse(SERVER_URL + _appPrefs.getGateway() + SUFFIX);
		Log.i(TAG, uri.toString());

		cd = new ConnectionDetector(getActivity().getApplicationContext());
		Bundle args = new Bundle();
		args.putParcelable("ARGS_URI", uri);

		if (cd.isConnectingToInternet())
			getLoaderManager().initLoader(1, args, this);
		else {
			Dialog d = DialogFactory.getDialog(getActivity(),
					"No internet connection",
					"You don't have internet connection.", false, "Connect",
					"Exit", this, this);
			d.show();
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		adapter = new FederationAdapter(getActivity().getBaseContext(),
				R.layout.federation_lv_item);
//		View footerView = ((LayoutInflater) getActivity().getSystemService(
//				Context.LAYOUT_INFLATER_SERVICE)).inflate(
//				R.layout.not_sure_footer, null, false);

//		getListView().addFooterView(footerView);
		getListView().setAdapter(adapter);

		getActivity().setTitle("Select your Identity Federation");
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
		// pdRing = ProgressDialog.show(getActivity(), "Loading...",
		// "Loading data...");
		// Quando è mostrato un two panel evidenzio il list item selezionato
		// (Faccio qesto nell'onStart perchè in questo moment l'elemento è
		// disponibile.)
		if (getFragmentManager().findFragmentById(R.id.federations_fragment) != null) {
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Con questo sono certo che la container Activity ha implementato
		// l'interfaccia OnFederetaionSelectedListener altrimenti lancio una
		// eccezione
		try {
			mCallback = (OnFederationSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFederationSelectedListener");
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (position < federations.size()) {
			// Notifica all'Activity padre l'item selezionato
			mCallback.onFederationSelected(federations.get(position), position);

			mCurrentPosition = position;
			// Setta l'item come check per essere evidenziato quando ci sono i
			// due panel visibili.
			getListView().setItemChecked(position, true);
		} 
//		else {
//			Dialog d = DialogFactory
//					.getDialog(
//							getActivity(),
//							"Suggestion",
//							"In case you do not belong to any of the shown Identity Federations, "
//									+ "or you are not sure you do, you are welcome to register to our Identity Provider Open."
//									+ " When registration procedure will be completed, you can sign in again and select GrIDP "
//									+ "as your Identity Federation and then IDPOPEN GARR as your Identity Provider.",
//							false, "Register", "Cancel",
//							new DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which) {
//									String url = "https://idpopen.garr.it/register";
//									Intent i = new Intent(Intent.ACTION_VIEW);
//									i.setData(Uri.parse(url));
//									startActivity(i);
//								}
//							}, null);
//			d.setOwnerActivity(getActivity());
//			d.show();
//		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		outState.putInt("position", mCurrentPosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	public Loader<RESTResponse> onCreateLoader(int id, Bundle args) {

		if (args != null && args.containsKey("ARGS_URI")) {
			Uri action = args.getParcelable("ARGS_URI");
			if (args.containsKey("ARGS_PARAMS")) {

				Bundle params = args.getParcelable("ARGS_PARAMS");

				return new RESTLoader(this.getActivity().getBaseContext(),
						RESTLoader.HTTPVerb.GET, action, params);
			} else {
				return new RESTLoader(this.getActivity().getBaseContext(),
						RESTLoader.HTTPVerb.GET, action);
			}
		}

		return null;
	}

	@Override
	public void onLoadFinished(Loader<RESTResponse> loader, RESTResponse data) {
		int code = data.getCode();
		String json = data.getData();
		// Log.d(TAG, "onLoadFinished with code: " + code + " and json: " +
		// json);
		if (pdRing != null)
			pdRing.dismiss();

		if (code == 200 && !json.equals("")) {

			// For really complicated JSON decoding I usually do my heavy
			// lifting
			// Gson and proper model classes, but for now let's keep it simple
			// and use a utility method that relies on some of the built in
			// JSON utilities on Android.
			federations = getFederationsFromJson(json);

			// Load our list adapter with our Federations.
			adapter.setData(federations);

			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
			// mAdapter.clear();

			// for (Federation federation : federations) {
			// mAdapter.add(federation.getName());
			// }

		} else {
			Toast.makeText(
					this.getActivity().getBaseContext(),
					"Failed to load Federation data. Check your internet settings.",
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onLoaderReset(Loader<RESTResponse> arg0) {

		Log.d("TEST", "LOADER: " + arg0.getId() + " HAS BEEN resetted");
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		Log.d(TAG, "onDestroyView");
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, "onResume");
		if (cd.isConnectingToInternet()) {
			Uri uri = Uri.parse(SERVER_URL + _appPrefs.getGateway() + SUFFIX);
			Log.i(TAG, uri.toString());
			Bundle args = new Bundle();
			args.putParcelable("ARGS_URI", uri);
			getLoaderManager().initLoader(1, args, this);
		}
		// int checkedItem = getListView().getCheckedItemPosition();
		// if (checkedItem != -1)
		// getListView().setItemChecked(
		// checkedItem, false);
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d(TAG, "onStop");
	}

	private ArrayList<Federation> getFederationsFromJson(String json) {

		ArrayList<Federation> federationsList = new ArrayList<Federation>();
		try {
			JSONObject federationsWrapper = (JSONObject) new JSONTokener(json)
					.nextValue();
			JSONArray federationsJSON = federationsWrapper
					.getJSONArray("federations");
			// ArrayList<IDP> allIdps = new ArrayList<IDP>();
			List<String> filelist = Arrays.asList(getActivity()
					.getBaseContext().fileList());
			for (int i = 0; i < federationsJSON.length(); i++) {
				Federation f = new Federation();
				JSONObject federationJSON = federationsJSON.getJSONObject(i);
				f.setName(federationJSON.getString("name"));
				String flagURL = federationJSON.getString("flag");
				String logoURL = federationJSON.getString("logo");
				f.setFlagURL(flagURL);
				f.setLogoURL(logoURL);
				if (filelist != null && filelist.size() != 0) {
					if (filelist.contains(flagURL.substring(flagURL
							.lastIndexOf("/") + 1))) {
						f.setFlag(getImageFromInternalStorage(flagURL
								.substring(flagURL.lastIndexOf("/") + 1)));
					}
					if (filelist.contains(logoURL.substring(logoURL
							.lastIndexOf("/") + 1))) {
						f.setLogo(getImageFromInternalStorage(logoURL
								.substring(logoURL.lastIndexOf("/") + 1)));
					}
				}
				f.setCountry(federationJSON.getString("country"));
				JSONArray idpsListJSON = federationJSON.getJSONArray("idps");
				IDP[] idpsArray = new IDP[idpsListJSON.length()];
				for (int j = 0; j < idpsListJSON.length(); j++) {
					IDP idpObj = new IDP();
					JSONObject idpJSON = idpsListJSON.getJSONObject(j);
					idpObj.setDisplayName(idpJSON.getString("displayName"));
					idpObj.setOrigin(idpJSON.getString("origin"));
					if (idpJSON.has("country"))
						idpObj.setCountry(idpJSON.getString("country"));

					String IDPFlagURL = "";
					String IDPLogoURL = "";

					if (idpJSON.has("flag"))
						IDPFlagURL = idpJSON.getString("flag");
					idpObj.setFlagURL(IDPFlagURL);

					if (idpJSON.has("logo"))
						IDPLogoURL = idpJSON.getString("logo");
					idpObj.setLogoURL(IDPLogoURL);

					if (filelist != null && filelist.size() != 0) {
						if (filelist.contains(IDPFlagURL.substring(IDPFlagURL
								.lastIndexOf("/") + 1))) {
							idpObj.setFlag(getImageFromInternalStorage(IDPFlagURL
									.substring(IDPFlagURL.lastIndexOf("/") + 1)));
						}
						if (filelist.contains(IDPLogoURL.substring(IDPLogoURL
								.lastIndexOf("/") + 1))) {
							idpObj.setLogo(getImageFromInternalStorage(IDPLogoURL
									.substring(IDPLogoURL.lastIndexOf("/") + 1)));
						}
					}
					idpsArray[j] = idpObj;
					// allIdps.add(idpObj);
				}
				ArrayList<IDP> tmpIdpsArray = new ArrayList<IDP>();

				for (int j = 0; j < idpsArray.length; j++) {
					if (idpsArray[j].getDisplayName()
							.toLowerCase(Locale.getDefault())
							.contains("idpopen")) {
						tmpIdpsArray.add(0, idpsArray[j]);

					} else if (idpsArray[j].getDisplayName()
							.toLowerCase(Locale.getDefault())
							.contains("social networks")) {
						tmpIdpsArray.add(1, idpsArray[j]);
					} else
						tmpIdpsArray.add(idpsArray[j]);
				}
				tmpIdpsArray.toArray(idpsArray);
				f.setIdps(idpsArray);

				if (f.getName().toLowerCase(Locale.getDefault())
						.contains("gridp"))
					federationsList.add(0, f);
				else
					federationsList.add(f);
			}

			// Federation federationAllIdps = new Federation("All IDPs",
			// allIdps.toArray(new IDP[allIdps.size()]));
			// federationsList.add(0, federationAllIdps);

		} catch (JSONException e) {
			Log.e(TAG, "Failed to parse JSON.", e);
		}
		return federationsList;
	}

	private Bitmap getImageFromInternalStorage(String filename) {
		Bitmap image = null;
		try {
			FileInputStream is = getActivity().getBaseContext().openFileInput(
					filename);
			image = BitmapFactory.decodeStream(is);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return image;
	}

	private class FederationAdapter extends ArrayAdapter<Federation> {

		Context context;

		public FederationAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);

			this.context = context;
		}

		public void setData(ArrayList<Federation> data) {
			clear();
			if (data != null) {
				for (Federation f : data) {
					add(f);
				}
			}

		}

		private class ViewHolder {
			TextView federationName;
			TextView country;
			ImageView flag;
			ImageView logo;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			Federation f = getItem(position);

			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			if (convertView == null) {

				int layout = R.layout.federation_lv_item;
				convertView = mInflater.inflate(layout, null);
				holder = new ViewHolder();

				holder.federationName = (TextView) convertView
						.findViewById(R.id.txtFederationName);
				holder.country = (TextView) convertView
						.findViewById(R.id.txtCountry);
				holder.flag = (ImageView) convertView
						.findViewById(R.id.flagImage);
				holder.logo = (ImageView) convertView
						.findViewById(R.id.logoImage);
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();

			holder.federationName.setText(f.getName());
			holder.country.setText(f.getCountry());
			if (f.getFlag() != null)
				holder.flag.setImageBitmap(f.getFlag());
			else
				new ImageDownloaderTask(context, holder.flag, f).execute(f
						.getFlagURL());
			if (f.getLogo() != null)
				holder.logo.setImageBitmap(f.getLogo());
			else
				new ImageDownloaderTask(context, holder.logo, f).execute(f
						.getLogoURL());
			return convertView;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case -1:
			Log.d(TAG, "Positve Pressed");
			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			break;

		case -2:
			_appPrefs.saveCookie("");
			getActivity().finish();
			break;
		default:
			Log.d(TAG, "Unrecognized button pressed.");
			break;
		}

	}

}
