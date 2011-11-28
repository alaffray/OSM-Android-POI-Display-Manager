package com.android.osmdisplaypoi;

import com.android.test.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class TagAdapter extends CursorAdapter
{
    private DbHelper dbAdapter = null;
 
    public TagAdapter(Context context, Cursor c)
    {
        super(context, c);
        dbAdapter = new DbHelper(context);
        dbAdapter.openDb();
    }
    
    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        String item = createItem(cursor);      
        ((TextView) view).setText(item);      
    }
    
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final TextView view = (TextView) inflater.inflate(R.layout.auto_complete_tag, parent, false);
        
        String item = createItem(cursor);
        view.setText(item);
        return view;
    }
 
    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint)
    {
        Cursor currentCursor = null;
        
        if (getFilterQueryProvider() != null)
        {
            return getFilterQueryProvider().runQuery(constraint);
        }
        
        String args = "";
        
        if (constraint != null)
        {
            args = constraint.toString();      
        }
 
        currentCursor = dbAdapter.getTagCursor(args);
 
        return currentCursor;
    }
    
    @Override
    public String convertToString(Cursor cursor) {
    	// this method dictates what is shown when the user clicks each entry in your autocomplete list
    	// in my case i want the number data to be shown
    	int columnIndex = cursor.getColumnIndexOrThrow("Description");

    	return cursor.getString(columnIndex);
    }
    
    private String createItem(Cursor cursor)
    {
        String item = cursor.getString(1);      
        return item;
    }
    
    public void close()
    {
        dbAdapter.close();
    }
}
