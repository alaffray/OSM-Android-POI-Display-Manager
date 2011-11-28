package com.android.osmdisplaypoi;

public class Poi
{
	public long id;
	public String tagType;
	public String name;
	public double latitude;
	public double longitude;
	public String phone;
	public String webSite;
	public String image;
	public String addrHousenumber;
	public String addrStreet;
	
	public Poi()
	{
	}
	public int latPoint()
	{
		return ((int)(latitude * 1E6));
	}
	public int longPoint()
	{
		return ((int)(longitude * 1E6));
	}
}
