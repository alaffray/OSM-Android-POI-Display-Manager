package com.android.osmdisplaypoi;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.os.AsyncTask;

class TaskHandler extends AsyncTask<Void, Void, ArrayList<Poi>> 
{

	private XapiHandler xapiHandler;
	private WeakReference<OsmPoiDisplayActivity> activityWeak;
	private String description;
	private String imagelink;

	/**
	 * Constructor.
	 */
	public TaskHandler(OsmPoiDisplayActivity activity, String url, String descr, String imgLnk) 
	{
		xapiHandler = new XapiHandler(url);
		activityWeak = new WeakReference<OsmPoiDisplayActivity>(activity);
    	description = descr;
    	imagelink = imgLnk;
	}

	@Override
	protected ArrayList<Poi> doInBackground(Void... params) 
	{
		xapiHandler.getData();
		return xapiHandler.getListPoi();
	}
	
	@Override
	protected void onPostExecute(ArrayList<Poi> result) 
	{
		OsmPoiDisplayActivity activity = activityWeak.get();
		if (activity != null) {
			activity.displayPoi(result, description, imagelink);
			activity.hideLoading();
		}
	}
	
}
