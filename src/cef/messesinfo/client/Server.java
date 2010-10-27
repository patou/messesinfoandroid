package cef.messesinfo.client;

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

    /**
     * Search a list of church
     * @param query The query to search
     * @param start The start of the search
     * @return
     * @throws XMLRPCException
     */
    public List<Map<String, String>> searchLocation(String query, int start, int limit) throws XMLRPCException {

	List<Map<String, String>> list = null;
	list = (List<Map<String, String>>) client.call("massinfov2.searchLocation", query, start, limit);
	return list;
    }

    /**
     * Search a mass with a query
     * @param query The text query to search
     * @return
     * @throws XMLRPCException
     */
    public List<Map<String, Object>> searchSchedule(String query) throws XMLRPCException {

	List<Map<String, Object>> list = null;
	list = (List<Map<String, Object>>) client.call("massinfov2.searchSchedule", query, 0, 50);
	return list;
    }

    /**
     * Get all church near in the given square of geo point
     * @param top_lat
     * @param top_lon
     * @param bottom_lat
     * @param bottom_lon
     * @return
     * @throws XMLRPCException
     */
    public List<Map<String, String>> getNearLocation(Double top_lat, Double top_lon, Double bottom_lat, Double bottom_lon) throws XMLRPCException {

	List<Map<String, String>> list = null;
	list = (List<Map<String, String>>) client.call("massinfov2.getNearLocation", top_lat, top_lon, bottom_lat, bottom_lon);
	return list;
    }

    /**
     * Get the list of schedule of a church
     * @param id
     * @return
     * @throws XMLRPCException
     */
    public List<Map<String, Object>> getLocationSchedule(String id, int start, int limit) throws XMLRPCException {

	List<Map<String, Object>> list = null;
	list = (List<Map<String, Object>>) client.call("massinfov2.getLocationSchedule", id, start, limit);
	return list;
    }

    /**
     * Get all information for a given church
     * @param id
     * @return
     * @throws XMLRPCException
     */
    public Map<String, String> getLocationInfo(String id) throws XMLRPCException {
	Map<String, String> item = null;
	item = (Map<String, String>) client.call("massinfov2.getLocationInfo", id);
	return item;
    }

    /**
     * Get a message, and log informations
     * @param map Information maps
     * @return
     * @throws XMLRPCException
     */
    public Map<String, String> getMessage(HashMap<String, String> map) throws XMLRPCException {
	Map<String, String> item = null;
	item = (Map<String, String>) client.call("message.getMessage", map);
	return item;
    }
}
