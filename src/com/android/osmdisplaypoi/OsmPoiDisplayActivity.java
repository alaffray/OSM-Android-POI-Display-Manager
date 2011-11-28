package com.android.osmdisplaypoi;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import com.android.test.R;




public class OsmPoiDisplayActivity extends Activity
{
	/** Called when the activity is first created. */
    private MapController mapController;
    private MapView mapView;
    private MyLocationOverlay myLocOverlay;
    private SlidingDrawer slidingDrawer;
    private AutoCompleteTextView autoComp;
    private Cursor tagCursor;
    private TagAdapter tagCursorAdapter;
    private DbHelper myDb;
    private Context cont;
    private HorizontalScrollView sView;
    private String xapiUrl = "http://open.mapquestapi.com/xapi/api/0.6/node";
    private ItemizedOverlay<OverlayItem> mOverlays;
    private ResourceProxy mResourceProxy;
    private boolean poiDisplay = false;
    private Button clearButton;
    private AlertDialog alertLoading;


    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /**
         * We set the map
         */
        setMap();
        
        /**
         * We initialise the map
         */
        initMap();
        
        /**
         * We initialise the database helper
         */
        initDbHelper(); 
        
        /**
         * We initialise the Auto Complete TextView
         */
        
        initAutoComp();
        
        /**
         * We Initialise the Search button and add different Alert box if the user enter wrong search criteria
         */
        Button searchButton = (Button) findViewById(R.id.searchbutton);
        searchButton.setOnClickListener(new OnClickListener()
        {
			@Override
        	public void onClick(View v)
        	{
        		if(autoComp.getText().toString().equals("") || autoComp.getText().toString().equals("Search"))
        		{
        			AlertDialog.Builder builder = new AlertDialog.Builder(cont);
            		builder.setMessage("You need to enter a search criteria")
            		       .setCancelable(false)
            		       .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            		           public void onClick(DialogInterface dialog, int id) {
            		        	   dialog.cancel();
            		           }
            		       });
            		AlertDialog alert = builder.create();
            		alert.show();
        		}
        		else
        		{
        			Cursor c = myDb.getSubCategories2(autoComp.getText().toString());        
        	        if(c.getCount() > 0) 
                	{
        	        	int osmcolumn = c.getColumnIndex("Osmcode");
                		String osmcode = c.getString(osmcolumn);
                		int descriptioncolumn = c.getColumnIndex("Description");
                		String description = c.getString(descriptioncolumn);
                		int imagelinkcolumn = c.getColumnIndex("Imagelink");
                		String imagelink = c.getString(imagelinkcolumn);
                		String url = xapiUrl + URLEncoder.encode(osmcode + getBoundingBox()); 
            			//XapiHandler handler = new XapiHandler(url);
            			//handler.getData();
            			/**
            			 * Display the POI
            			 */
            			displayLoading();
            			//displayPoi(handler.getListPoi(),description, imagelink);
            			//alertLoading.cancel();
            			(new TaskHandler((OsmPoiDisplayActivity) v.getContext(), url, description, imagelink)).execute();
                	}
        	        else
        	        {
        	        	AlertDialog.Builder builder = new AlertDialog.Builder(cont);
                		builder.setMessage("No such Criteria exist")
                		       .setCancelable(false)
                		       .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                		           public void onClick(DialogInterface dialog, int id) {
                		        	   dialog.cancel();
                		           }
                		       });
                		AlertDialog alert = builder.create();
                		alert.show();
        	        }
        	        c.close();
        		}
        		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(autoComp.getWindowToken(), 0);
        	}
        });
        /**
         * We initialise the Categories
         */
        initCat(this);
        clearButton = (Button) findViewById(R.id.cleanbutton);
        clearButton.setVisibility(View.INVISIBLE);
        clearButton.setOnClickListener(new OnClickListener()
        {

			@Override
        	public void onClick(View v)
        	{
				mapView.getOverlays().remove(mOverlays);
				clearButton.setVisibility(View.INVISIBLE);
        	}
        });
        sView = (HorizontalScrollView) findViewById(R.id.scrollView01);
        cont = this;
        // Get reference to SlidingDrawer
		slidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
	    slidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() 
	    {		
			@Override
			public void onDrawerOpened() 
			{
				// TODO Auto-generated method stub
				mapView.setBuiltInZoomControls(false);
				if (sView.getChildAt(0).getId() == 2)
		        {
		        	sView.removeViewAt(0);
		        	initCat(cont);
		        }
			}
	    });
		slidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() 
		{		
			@Override
			public void onDrawerClosed() 
			{
				// TODO Auto-generated method stub
			}
		});
		
    }
    /**
     * Loading message
     */
    public void displayLoading()
    {
    	AlertDialog.Builder loading = new AlertDialog.Builder(cont);
    	loading.setCancelable(false);
    	loading.setMessage("Loading, Please wait.");
    	alertLoading = loading.create();
    	alertLoading.show();
    }
    public void hideLoading() 
    {
		alertLoading.cancel();
    }
    /**
     * display POI
     * @param p
     * @param d
     * @param img
     */
    public void displayPoi(ArrayList<Poi> p, String d, String img)
    {
    	if(p.isEmpty() == true)
    	{
    		AlertDialog.Builder builder = new AlertDialog.Builder(cont);
    		builder.setMessage("There are no such criteria nearby")
    		       .setCancelable(false)
    		       .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		        	   dialog.cancel();
    		           }
    		       });
    		AlertDialog alert = builder.create();
    		alert.show();
    	}
    	else
    	{
	    	if(poiDisplay == false)
	    	{
		    	mResourceProxy = new ResourceProxyImpl(getApplicationContext());
		    	final int resID = getResources().getIdentifier(img +"2", "drawable", "com.android.test");
		    	Drawable marker = getResources().getDrawable(resID);
		    	final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		    	for(int i = 0 ; i < p.size(); i++)
		    	{	
		    		Poi poi = p.get(i);
		    		OverlayItem overItem = new OverlayItem(poi.name, d +"\n"+ poi.addrHousenumber+ " "+ poi.addrStreet+"\n"+ poi.phone+ "\n"+ poi.webSite, new GeoPoint(p.get(i).latPoint(),p.get(i).longPoint()));
		    		overItem.setMarker(marker);
		    		items.add(overItem);
		    	}
		    	this.mOverlays = new ItemizedIconOverlay<OverlayItem>(items,new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() 
		    	        {
		    	            @Override
		    	            public boolean onItemSingleTapUp(final int index, final OverlayItem item) 
		    	            {
		    	            	 AlertDialog.Builder dialog = new AlertDialog.Builder(cont);
		    	            	 dialog.setTitle(item.getTitle());
		    	            	 dialog.setMessage(item.getSnippet());
		    	            	 dialog.setIcon(resID);
		    	            	 dialog.setNeutralButton("Close", new DialogInterface.OnClickListener() 
		    	            	 {
		    	            		 public void onClick(DialogInterface dialog, int id) 
		    	            		 {
		    	            			 dialog.cancel();
		          		           	}
		          		       	 });
		    	            	 dialog.show();
		    	                 return true;
		    	            }
		    	            @Override
		    	            public boolean onItemLongPress(final int index, final OverlayItem item) 
		    	            {     
		    	                    return false;
		    	            }
		    	        }, mResourceProxy);
		    	this.mapView.getOverlays().add(this.mOverlays);
		    	clearButton.setVisibility(View.VISIBLE);
		    	poiDisplay = true;
	    	}
	    	else
	    	{
	    		this.mapView.getOverlays().remove(this.mOverlays);
	    		poiDisplay = false;
	    		displayPoi(p,d,img);
	    	}
    	}
    }
    /**
     * Override on the Mobile Phone back button
     */
    @Override
    public void onBackPressed() 
    {
    	if (sView.getChildAt(0).getId() == 2)
        {
        	sView.removeViewAt(0);
        	initCat(cont);
        }
    	else if (sView.getChildAt(0).getId() == 1 && slidingDrawer.isOpened())
    	{
    		mapView.setBuiltInZoomControls(true);
    		slidingDrawer.close();
    		
    	}
    	else
    	{
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage("Are you sure you want to exit?")
    		       .setCancelable(false)
    		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		                System.exit(0);
    		           }
    		       })
    		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		                dialog.cancel();
    		           }
    		       });
    		AlertDialog alert = builder.create();
    		alert.show();
    	}
    	return;
    }
    /**
     * Initialisation of the Categories
     */
    public void initCat(Context cont)
    {
    	final Cursor c = myDb.getCategories();
        int imagecolumn = c.getColumnIndex("Imagelink");
        int osmcolumn = c.getColumnIndex("Osmcode");
        int descriptioncolumn = c.getColumnIndex("Description");
        HorizontalScrollView sView = (HorizontalScrollView) findViewById(R.id.scrollView01);
        LinearLayout layout = new LinearLayout(cont);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setBackgroundResource(R.drawable.bg);
        layout.setId(1);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT
        );
        layout.setLayoutParams(param);
        sView.addView(layout);
        for (int i = 1; i < c.getCount()+1; i++) 
        {
        	if (c != null) 
        	{
        		String imageName = c.getString(imagecolumn);
        		String osmCode = c.getString(osmcolumn);
        		String description = c.getString(descriptioncolumn);
        		int resID = getResources().getIdentifier(imageName, "drawable", "com.android.test");        		
        		Button buttonView = new Button(this);
            	buttonView.setId(i);
            	buttonView.setBackgroundResource(resID);
            	buttonView.setOnClickListener(new myOnClickListener(osmCode, description, imageName));
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                layout.addView(buttonView, p);
                c.moveToNext();
        	}	    
        }
        c.close();
    }
    /**
     * Initialisation of the Sub Cat
     * @param id
     */
    public void initSubCat(int id, Context cont)
    {
    	Cursor c = myDb.getSubCategories(id);
    	int imagecolumn = c.getColumnIndex("Imagelink");
        int osmcolumn = c.getColumnIndex("Osmcode");
        int descriptioncolumn = c.getColumnIndex("Description");
    	HorizontalScrollView sView = (HorizontalScrollView) findViewById(R.id.scrollView01);
        LinearLayout layout = new LinearLayout(cont);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setBackgroundResource(R.drawable.bg);
        layout.setId(2);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT
        );
        layout.setLayoutParams(param);
        sView.addView(layout);
        for (int i = 1; i < c.getCount()+1; i++) 
        {
        	if (c != null) 
        	{
        		String imageName = c.getString(imagecolumn);
        		String osmCode = c.getString(osmcolumn);
        		String description = c.getString(descriptioncolumn);
        		int resID = getResources().getIdentifier(imageName, "drawable", "com.android.test");        		
        		Button buttonView = new Button(this);
            	buttonView.setId(i);
            	buttonView.setBackgroundResource(resID);
            	buttonView.setOnClickListener(new myOnClickListener1(osmCode, description, imageName));
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                layout.addView(buttonView, p);
                c.moveToNext();
        	}	    
        }
        c.close();
    }
    /**
     * Initialisation of the Auto Complete search bar
     */
     private void initAutoComp()
    {
    	tagCursor = myDb.getTagCursor("");
        startManagingCursor(tagCursor);
        tagCursorAdapter = new TagAdapter(getApplicationContext(), tagCursor); 
    	autoComp = (AutoCompleteTextView) findViewById(R.id.autocomplete_tag);
    	autoComp.setAdapter(tagCursorAdapter);
    	autoComp.setThreshold(2);
        autoComp.setOnClickListener(new View.OnClickListener() 
        {	
			@Override
			public void onClick(View v) 
			{
				autoComp.setText("");
			}
		});
       
    }
    /**
     * Initialisation of the DbHelper
     */
    private void initDbHelper()
    {
    	myDb = new DbHelper(this);
    	 try
         {
         	myDb.createDb();
         }
         catch(IOException ioe)
         {
         	throw new Error("Unable to create database");
         }
         try 
         {
        	 
      		myDb.openDb();
      
      	 }
         catch(SQLException sqle)
         {
      
      		throw sqle;
      	 }       
    }
    /**
     *  Initialisation of the on Create Options Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    /** 
     * Initialisation of the on Options Item Selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
		switch (item.getItemId())
		{
			case R.id.exit:
				System.exit(0);
		}
    	return (super.onOptionsItemSelected(item));
    	
    }
    /**
     * We get the BoundingBox
     * @return
     */
    private String getBoundingBox()
    {
    	GeoPoint gP = myLocOverlay.getMyLocation();
    	double latitude = gP.getLatitudeE6() / 1E6;
    	double longitude = gP.getLongitudeE6() / 1E6;
    	double radius = 0.05;
    	String leftLong = String.format("%10.6f", longitude - radius).trim();
    	String bottomLat = String.format("%10.6f", latitude - radius).trim();
    	String rightLong = String.format("%10.6f", longitude + radius).trim();
    	String topLat = String.format("%10.6f", latitude + radius).trim();
	
    	String Result ="[bbox="+ leftLong+"," + bottomLat +"," + rightLong + "," + topLat+"]";
    	return Result;
    }
    /**
     * is Route Displayed to false
     * @return
     */
    protected boolean isRouteDisplayed() 
    {
        // TODO Auto-generated method stub
        return false;
    }
    /**
     * we set the map
     */
    public void setMap()
    {
    	mapView = (MapView) findViewById(R.id.mapview);
        mapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(15);
        mapView.setBuiltInZoomControls(false);
    }
    /**
     * We initialise the map 
     */
    private void initMap() 
    {
        myLocOverlay = new MyLocationOverlay(this, mapView);
        mapView.getOverlays().add(myLocOverlay);
        myLocOverlay.enableMyLocation();
        this.myLocOverlay.runOnFirstFix(new Runnable() 
        {
		    public void run() 
		    {
		    	/* Animate to the current location on first GPS fix */
		    	mapController.animateTo(myLocOverlay.getMyLocation());
		    }	
        });	
    }
    /**
     * Reimplmentation of OnClickListener for categories button
     * @author Aurélien
     *
     */
    class myOnClickListener implements OnClickListener
    {
    	private String osmcode;
    	private String description;
    	private String imagelink;
    	public myOnClickListener(String arg1, String arg2, String arg3)
    	{
    		osmcode = arg1;
    		description = arg2;
    		imagelink = arg3;
    		
    	}
		@Override
		public void onClick(View v) 
		{
			// TODO Auto-generated method stub
			if(v.getId() != 2 && v.getId() != 5 && v.getId() != 6 && v.getId() != 8)
    		{
    			((ViewGroup) v.getParent().getParent()).removeView((View) v.getParent());
    			initSubCat(v.getId(), cont);
    		}
    		else
    		{
    			String url = xapiUrl + URLEncoder.encode(osmcode + getBoundingBox()); 
    			//XapiHandler handler = new XapiHandler(url);
    			//handler.getData();
    			/**
    			 * Display the POI
    			 */
    			displayLoading();
    			(new TaskHandler((OsmPoiDisplayActivity) v.getContext(), url, description, imagelink)).execute();
    			//displayPoi(handler.getListPoi(),description, imagelink);
    			//alertLoading.cancel();
    		}
		}  	
    }
    /**
     * Reimplmentation of OnClickListener for subcategories button
     * @author Aurélien
     *
     */
    class myOnClickListener1 implements OnClickListener
    {
    	private String osmcode;
    	private String description;
    	private String imagelink;
    	public myOnClickListener1(String arg1, String arg2, String arg3)
    	{
    		osmcode = arg1;
    		description = arg2;
    		imagelink = arg3;
    	}
		@Override
		public void onClick(View v) 
		{
			// TODO Auto-generated method stub
			String url = xapiUrl + URLEncoder.encode(osmcode + getBoundingBox()); 
			//XapiHandler handler = new XapiHandler(url);
			//handler.getData();
			/**
			 * Display the POI
			 */
			displayLoading();
			(new TaskHandler((OsmPoiDisplayActivity) v.getContext(), url, description, imagelink)).execute();
			//displayPoi(handler.getListPoi(),description, imagelink);
			//alertLoading.cancel();
		}	
    }  
}