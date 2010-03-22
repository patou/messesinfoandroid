package cef.messeinfo.client;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

public class Server {

	XMLRPCClient client;

	public Server(String url) {
		URI uri = URI.create(url);
		client = new XMLRPCClient(uri);
	}

	public List<Map<String, String>> searchChurch(String query, int start) {

		List<Map<String, String>> list = null;
		try {
			list = (List<Map<String, String>>) client.call("messesinfo.searchChurch", query, start);
		} catch (XMLRPCException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public List<Map<String, Object>> searchSchedule(String query) {

		List<Map<String, Object>> list = null;
		try {
			list = (List<Map<String, Object>>) client.call("messesinfo.searchSchedule", query);
		} catch (XMLRPCException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Map<String, String>> getNearChurch(Double top_lat, Double top_lon, Double bottom_lat, Double bottom_lon) {

		List<Map<String, String>> list = null;
		try {
			list = (List<Map<String, String>>) client.call("messesinfo.getNearChurch", top_lat, top_lon, bottom_lat, bottom_lon);
		} catch (XMLRPCException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Map<String, Object>> getSchedule(String code) {

		List<Map<String, Object>> list = null;
		try {
			list = (List<Map<String, Object>>) client.call("messesinfo.getSchedule", code);
		} catch (XMLRPCException e) {
			e.printStackTrace();
		}
		return list;
	}

	public Map<String, String> getChurchInfo(String code) {
		Map<String, String> item = null;
		try {
			item = (Map<String, String>) client.call("messesinfo.getChurchInfo", code);
		} catch (XMLRPCException e) {
			e.printStackTrace();
		}
		return item;
	}

	public Map<String, String> getMessage(HashMap<String, String> map) {
		Map<String, String> item = null;
		try {
			item = (Map<String, String>) client.call("message.getMessage", map);
		} catch (XMLRPCException e) {
			e.printStackTrace();
		}
		return item;
	}
}
