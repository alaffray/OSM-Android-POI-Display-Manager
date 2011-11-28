package com.android.osmdisplaypoi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper
{
	private static String DB_PATH = "data/data/com.android.test/databases/";
	private static String DB_NAME = "dissertationDb5";
	private SQLiteDatabase myDb;
	private final Context myContext;
	 /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
	public DbHelper(Context context)
	{
		super(context, DB_NAME, null, 1);
		this.myContext = context;
	}
	public void createDb() throws IOException
	{
		boolean dbExist = checkDb();
		if(dbExist)
		{
		}
		else
		{
			this.getReadableDatabase();
			try
			{
				copyDb();
			}
			catch(IOException e)
			{
				throw new Error("Error copying database");
			}
		}
	}
	 /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
	private boolean checkDb()
	{
		SQLiteDatabase checkDB = null;
		try
		{
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		}
		catch(SQLiteException e)
		{
			//database does't exist yet.
	    }
	    if(checkDB != null)
	    {
	    	checkDB.close();
	    }
	    	return checkDB != null ? true : false;
	}
	 /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
	private void copyDb() throws IOException
	{ 
    	//Open the local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DB_NAME);
 
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0)
    	{
    		myOutput.write(buffer, 0, length);
    	}
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
 
    }
    public void openDb() throws SQLException{
    	 
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	myDb = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    }
 
    @Override
	public synchronized void close() {
 
    	    if(myDb != null)
    		    myDb.close();
 
    	    super.close();
 
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	public Cursor getTagCursor(String args)
	{      
	 String sqlQuery = "";
	 Cursor result = null;
	    
	 sqlQuery  = "SELECT _id, Description, Osmcode FROM Tag WHERE Description LIKE '%" + args + "%' ORDER BY Description";
	 
	 if (myDb == null)
	 {
	  openDb();
	 }
	 
	 if (myDb!=null)
	 {          
	  result = myDb.rawQuery(sqlQuery, null);
	 }
	 
	 return result;
	}
	public Cursor getCategories()
	{
		String sqlQuery = "";
		Cursor result = null;
		
		sqlQuery  = " SELECT _id, Description, Osmcode, Imagelink FROM Categories";
		    
		if (myDb == null)
		{
			openDb();
		}
		if (myDb!=null)
		{          
			result = myDb.rawQuery(sqlQuery, null);
			result.moveToFirst();
		}
		return result;
	}
	public Cursor getSubCategories(int i)
	{
		String sqlQuery = "";
		Cursor result = null;
		
		sqlQuery  = " SELECT _id, Description, Osmcode, Imagelink, Category FROM Tag WHERE Category ="+ i;
		    
		if (myDb == null)
		{
			openDb();
		}
		if (myDb!=null)
		{          
			result = myDb.rawQuery(sqlQuery, null);
			result.moveToFirst();
		}
		return result;
	}
	public Cursor getSubCategories2(String args)
	{
		String sqlQuery = "";
		Cursor result = null;
		
		sqlQuery  = " SELECT _id, Description, Osmcode, Imagelink, Category FROM Tag WHERE Description ='"+ args +"'";
		    
		if (myDb == null)
		{
			openDb();
		}
		if (myDb!=null)
		{          
			result = myDb.rawQuery(sqlQuery, null);
			result.moveToFirst();
		}
		return result;
	}
}
