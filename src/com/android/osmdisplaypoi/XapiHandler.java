package com.android.osmdisplaypoi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XapiHandler
{
	private URL rtpUrl;
	private ArrayList<Poi> list = new ArrayList<Poi>();
	
	/**
	 * Constructor
	 */
	public XapiHandler(String url)
	{
		try 
		{
			rtpUrl = new URL(url);
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getData()
	{
		NodeList items = null;
		
		try
		{
			//String urlEncoded = "http://open.mapquestapi.com/xapi/api/0.6/node" + URLEncoder.encode("[amenity=fast_food][bbox=-0.1733,51.28,0.1,51.48]");
			//rtpUrl = new URL(urlEncoded);
			URLConnection sc = rtpUrl.openConnection();
			String inputLine;
			String result = "";
			InputStreamReader webContent = new InputStreamReader(sc.getInputStream()); 
			BufferedReader in = new BufferedReader(webContent);
			while ((inputLine = in.readLine()) != null)
			{
				result += inputLine;
			}
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(result)));
			
			Element root = document.getDocumentElement();
			items = root.getElementsByTagName("node");
			
			if (list != null)
			{
				for(int i = 0; i < items.getLength(); i++)
				{
					// We create the POI object
					Poi currentPOI = new Poi();
					
					//we extract the information that we need
					Element item = (Element) items.item(i);
					String id = item.getAttribute("id");
					String longitude = item.getAttribute("lon");
					String latitude = item.getAttribute("lat");
					String name = "";
					String phone = "";
					String webSite = "";
					String addrStreet = "";
					String addrHousenumber = "";
					NodeList tagList = item.getElementsByTagName("tag");
					
					for(int j = 0; j < tagList.getLength(); j++)
					{
						Element tagElement = (Element) tagList.item(j);
						String key = tagElement.getAttribute("k");
						String value = tagElement.getAttribute("v");
						
						if (key.compareTo("name") == 0)
						{
							name = value;
						}
						else if(key.compareTo("phone") == 0 || key.compareTo("contact:phone") == 0)
						{
							phone = value;
						}
						else if (key.compareTo("website") == 0 || key.compareTo("contact:website") == 0)
						{
							webSite = value;
						}
						else if (key.compareTo("addr:street") == 0)
						{
							addrStreet = value;
						}
						else if (key.compareTo("addr:housenumber") == 0)
						{
							addrHousenumber = value;
						}
					}
					currentPOI.id = Long.valueOf(id);
					currentPOI.longitude = Double.parseDouble(longitude);
					currentPOI.latitude = Double.parseDouble(latitude);
					currentPOI.name = name;
					currentPOI.phone = phone;
					currentPOI.webSite = webSite;
					currentPOI.addrStreet = addrStreet;
					currentPOI.addrHousenumber = addrHousenumber;
					list.add(currentPOI);
				}
			}
			
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
	}
	public ArrayList<Poi> getListPoi()
	{
		return list;
	}

}
