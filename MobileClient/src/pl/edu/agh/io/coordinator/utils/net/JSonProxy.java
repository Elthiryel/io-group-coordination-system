package pl.edu.agh.io.coordinator.utils.net;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.app.ExpandableListActivity;

import pl.edu.agh.io.coordinator.resources.Group;
import pl.edu.agh.io.coordinator.resources.Layer;
import pl.edu.agh.io.coordinator.resources.MapItem;
import pl.edu.agh.io.coordinator.resources.Message;
import pl.edu.agh.io.coordinator.resources.Point;
import pl.edu.agh.io.coordinator.resources.User;
import pl.edu.agh.io.coordinator.resources.UserItem;
import pl.edu.agh.io.coordinator.resources.UserState;
import pl.edu.agh.io.coordinator.utils.net.exceptions.CouldNotCreateGroupException;
import pl.edu.agh.io.coordinator.utils.net.exceptions.CouldNotLogInException;
import pl.edu.agh.io.coordinator.utils.net.exceptions.CouldNotRemoveException;
import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidGroupException;
import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidLayerException;
import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidMapItemException;
import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidSessionIDException;
import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidUserException;
import pl.edu.agh.io.coordinator.utils.net.exceptions.InvalidUserItemException;
import pl.edu.agh.io.coordinator.utils.net.exceptions.NetworkException;

public class JSonProxy implements IJSonProxy {
	
	private static JSonProxy instance;
	private static long sessionID = -1;
	// without ending slash
	private static String serverName = "http://io.wojtasskorcz.eu.cloudbees.net";

	private JSonProxy() {
	}

	public static synchronized JSonProxy getInstance() {
		if (instance == null)
			instance = new JSonProxy();
		return instance;
	}

	private JSONObject createSessionOnlyParams() {
		Map<String, Long> paramsInString = new HashMap<String, Long>();
		paramsInString.put("sessionID", sessionID);
		JSONObject params = new JSONObject(paramsInString);
		return params;
	}
	
	private String getJSonString(String methodName, JSONObject params)
			throws NetworkException {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(serverName + "/" + methodName);

		try {
			StringEntity stringEntity = new StringEntity(params.toString(),
					HTTP.UTF_8);
			stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));

			httpPost.setEntity(stringEntity);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		try {
			HttpResponse response = client.execute(httpPost);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else
				throw new NetworkException();
		} catch (Exception e) {
			throw new NetworkException();
		}

		return builder.toString();
	}

	@Override
	public synchronized void login(String userName, String password)
			throws CouldNotLogInException, NetworkException {
		Map<String, String> paramsInString = new HashMap<String, String>();
		paramsInString.put("id", userName);
		paramsInString.put("password", password);
		JSONObject params = new JSONObject(paramsInString);

		try {
			String jsonString = getJSonString("login", params);
			JSONObject jsonObject = new JSONObject(jsonString);
			String exception = jsonObject.getString("exception");
			if (exception.equals("null"))
				sessionID = jsonObject.getLong("retval");
			else {
				sessionID = -1;
				throw new CouldNotLogInException();
			}
		} catch (JSONException e) {
			throw new CouldNotLogInException();
		}
	}

	@Override
	public synchronized void logout() throws InvalidSessionIDException,
			NetworkException {
		JSONObject params = createSessionOnlyParams();

		try {
			String jsonString = getJSonString("logout", params);
			JSONObject jsonObject = new JSONObject(jsonString);
			String exception = jsonObject.getString("exception");
			sessionID = -1;
			if (exception.equals("InvalidSessionID"))
				throw new InvalidSessionIDException();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<MapItem> getMapItems(Layer layer) throws InvalidSessionIDException, InvalidLayerException, NetworkException {
		Set<MapItem> toReturn = new HashSet<MapItem>();
		Map<String, Object> paramsInString = new HashMap<String, Object>();
		paramsInString.put("sessionID", sessionID);
		paramsInString.put("layer", layer.getName());
		JSONObject params = new JSONObject(paramsInString);
		try {
			String jsonString = getJSonString("getMapItems", params);
			JSONObject jsonObject = new JSONObject(jsonString);
			String exception = jsonObject.getString("exception");
			if (exception.equals("InvalidSessionID")) {
				throw new InvalidSessionIDException();
			} else if (exception.equals("InvalidLayer")) {
				throw new InvalidLayerException();
			} else {
				JSONArray array = jsonObject.getJSONArray("retval");
				int limit = array.length();
				for (int i = 0; i < limit; ++i) {
					JSONObject jsonMapItem = array.getJSONObject(i);
					MapItem mapItem = new MapItem(jsonMapItem);
					toReturn.add(mapItem);
				}
			}			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	@Override
	public Set<Layer> getLayers() throws InvalidSessionIDException,
			NetworkException {
		Set<Layer> toReturn = new HashSet<Layer>();
		JSONObject params = createSessionOnlyParams();
		try {
			String jsonString = getJSonString("getLayers", params);
			JSONObject jsonObject = new JSONObject(jsonString);
			String exception = jsonObject.getString("exception");
			if (exception.equals("InvalidSessionID")) {
				throw new InvalidSessionIDException();
			} else {
				JSONArray array = jsonObject.getJSONArray("retval");
				int limit = array.length();
				for (int i = 0; i < limit; ++i) {
					String layer = array.getString(i);
					toReturn.add(new Layer(layer));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	@Override
	public MapItem addItemToLayer(Layer layer, Point point, String data)
			throws InvalidSessionIDException, InvalidLayerException,
			NetworkException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeMapItem(MapItem item) throws InvalidSessionIDException,
			InvalidMapItemException, NetworkException {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateSelfState(UserState newState)
			throws InvalidSessionIDException, NetworkException {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<UserItem> getPossibleUserItems()
			throws InvalidSessionIDException, NetworkException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addItemToUser(User user, UserItem item)
			throws InvalidSessionIDException, InvalidUserException,
			InvalidUserItemException, NetworkException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeItemFromUser(User user, UserItem item)
			throws InvalidSessionIDException, InvalidUserException,
			InvalidUserItemException, CouldNotRemoveException, NetworkException {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<User> getUsers() throws InvalidSessionIDException,
			NetworkException {
		Set<User> ret = new HashSet<User>();
		Map<String, Long> paramsInString = new HashMap<String, Long>();
		paramsInString.put("sessionID", sessionID);
		JSONObject params = new JSONObject(paramsInString);

		try {
			String jsonString = getJSonString("getUsers", params);
			JSONObject jsonObject = new JSONObject(jsonString);

			String exception = jsonObject.getString("exception");

			if (exception.equals("InvalidSessionID"))
				throw new InvalidSessionIDException();
			else {
				JSONArray retArray = jsonObject.getJSONArray("retval");
				int max = retArray.length();
				for (int i = 0; i < max; i++) {
					JSONObject jsonUser = retArray.getJSONObject(i);
					User user = new User(jsonUser);
					ret.add(user);
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return ret;
	}

	@Override
	public Set<UserItem> getUserItems(User user)
			throws InvalidSessionIDException, InvalidUserException,
			NetworkException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Group> getGroups() throws InvalidSessionIDException,
			NetworkException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createGroup(String groupName) throws InvalidSessionIDException,
			CouldNotCreateGroupException, NetworkException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addToGroup(User user, Group group)
			throws InvalidSessionIDException, InvalidUserException,
			InvalidGroupException, NetworkException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeFromGroup(User user, Group group)
			throws InvalidSessionIDException, InvalidUserException,
			InvalidGroupException, CouldNotRemoveException, NetworkException {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<Group> getGroupUsers(Group group)
			throws InvalidSessionIDException, InvalidGroupException,
			NetworkException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendMessage(String message) throws InvalidSessionIDException,
			NetworkException {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<Message> getMessages() throws InvalidSessionIDException,
			NetworkException {
		// TODO Auto-generated method stub
		return null;
	}
}
