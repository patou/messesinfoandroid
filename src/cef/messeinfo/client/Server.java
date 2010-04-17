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

    public List<Map<String, String>> searchChurch(String query, int start) throws XMLRPCException {

	List<Map<String, String>> list = null;
	list = (List<Map<String, String>>) client.call("messesinfo.searchChurch", query, start);
	return list;
    }

    public List<Map<String, Object>> searchSchedule(String query) throws XMLRPCException {

	List<Map<String, Object>> list = null;
	list = (List<Map<String, Object>>) client.call("messesinfo.searchSchedule", query);
	return list;
    }

    public List<Map<String, String>> getNearChurch(Double top_lat, Double top_lon, Double bottom_lat, Double bottom_lon) throws XMLRPCException {

	List<Map<String, String>> list = null;
	list = (List<Map<String, String>>) client.call("messesinfo.getNearChurch", top_lat, top_lon, bottom_lat, bottom_lon);
	return list;
    }

    public List<Map<String, Object>> getSchedule(String code) throws XMLRPCException {

	List<Map<String, Object>> list = null;
	list = (List<Map<String, Object>>) client.call("messesinfo.getSchedule", code);
	return list;
    }

    public Map<String, String> getChurchInfo(String code) throws XMLRPCException {
	Map<String, String> item = null;
	item = (Map<String, String>) client.call("messesinfo.getChurchInfo", code);
	return item;
    }

    public Map<String, String> getMessage(HashMap<String, String> map) throws XMLRPCException {
	Map<String, String> item = null;
	item = (Map<String, String>) client.call("message.getMessage", map);
	return item;
    }
}
