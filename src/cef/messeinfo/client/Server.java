package cef.messeinfo.client;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import android.util.Log;

public class Server {

	XMLRPCClient client;

	public Server(String url) {
		URI uri = URI.create(url);
		client = new XMLRPCClient(uri);
	}

	public String helloword() {
		try {
			return (String) client.call("test.helloWorld", "Android");
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("messeinfo", e.getMessage(), e);
		}
		return "";
	}

	public List<Map<String, String>> searchChurch(String query, int start) {

		List<Map<String, String>> list = null;
		try {
			list = (List<Map<String, String>>) client.call("messeinfo.searchChurch", query, start);
		} catch (XMLRPCException e) {
			e.printStackTrace();
			Log.e("messeinfo", e.getMessage(), e);
		}
		return list;
	}
	
	public List<Map<String, Object>> searchSchedule(String query) {

		List<Map<String, Object>> list = null;
		try {
			list = (List<Map<String, Object>>) client.call("messeinfo.searchSchedule", query);
			Log.e("messeinfo", list.toString());
		} catch (XMLRPCException e) {
			e.printStackTrace();
			Log.e("messeinfo", e.getMessage(), e);
		}
		return list;
	}

	public List<Map<String, String>> getNearChurch(Double top_lat, Double top_lon, Double bottom_lat, Double bottom_lon) {

		List<Map<String, String>> list = null;
		try {
			list = (List<Map<String, String>>) client.call("messeinfo.getNearChurch", top_lat, top_lon, bottom_lat, bottom_lon);
			Log.e("messeinfo", list.toString());
		} catch (XMLRPCException e) {
			e.printStackTrace();
			Log.e("messeinfo", e.getMessage(), e);
		}
		return list;
	}

	public List<Map<String, Object>> getSchedule(String code) {

		List<Map<String, Object>> list = null;
		try {
			list = (List<Map<String, Object>>) client.call("messeinfo.getSchedule", code);
			Log.e("messeinfo", list.toString());
		} catch (XMLRPCException e) {
			e.printStackTrace();
			Log.e("messeinfo", e.getMessage(), e);
		}
		return list;
	}

	public Map<String, String> getChurchInfo(String code) {
		Map<String, String> item = null;
		try {
			item = (Map<String, String>) client.call("messeinfo.getChurchInfo", code);
		} catch (XMLRPCException e) {
			e.printStackTrace();
			Log.e("messeinfo", e.getMessage(), e);
		}
		return item;
	}
}
