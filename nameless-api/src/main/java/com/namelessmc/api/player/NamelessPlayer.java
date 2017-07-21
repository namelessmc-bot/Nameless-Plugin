package com.namelessmc.api.player;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.api.NamelessException;
import com.namelessmc.api.utils.PostString;
import com.namelessmc.api.utils.RequestUtil;
import com.namelessmc.api.utils.RequestUtil.Request;
import com.namelessmc.api.utils.RequestUtil.RequestType;

public class NamelessPlayer {

	private String userName;
	private String displayName;
	private String uuid;
	private int groupID;
	private int reputation;
	private Date registeredDate;
	private boolean exists;
	private boolean validated;
	private boolean banned;
	
	private boolean https;
	private URL baseUrl;
	
	private JsonParser parser;

	public NamelessPlayer(UUID uuid, URL baseUrl, boolean https) {	
		this.baseUrl = baseUrl;
		this.https = https;
		
		Request request = RequestUtil.sendRequest(RequestType.POST, baseUrl, "get", "uuid=" + PostString.urlEncodeString(uuid.toString()), https);
		
		parser = new JsonParser();
		JsonObject response = request.getResponse();
		
		if (!request.hasSucceeded()) {
			exists = false;
			return;
		}

		//No errors, parse response
		
		JsonObject message = parser.parse(response.get("message").getAsString()).getAsJsonObject();

		exists = true;

		// Convert UNIX timestamp to date
		Date registered = new Date(Long.parseLong(message.get("registered").toString().replaceAll("^\"|\"$", "")) * 1000);

		// Display get user.
		userName = message.get("username").getAsString();
		displayName = message.get("displayname").getAsString();
		uuid = UUID.fromString(message.get("uuid").getAsString());
		groupID = message.get("group_id").getAsInt();
		registeredDate = registered;
		reputation = message.get("reputation").getAsInt();
		validated = message.get("validated").getAsString().equals("1");
		banned = message.get("banned").getAsString().equals("1");
	}

	public String getUsername() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return userName;
	}

	public String getDisplayName() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return displayName;
	}

	public String getUUID() {
		return uuid;
	}

	public int getGroupID() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return groupID;
	}

	public int getReputations() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return reputation;
	}

	public Date getRegisteredDate() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return registeredDate;
	}

	public boolean exists() {	
		return exists;
	}

	public boolean isValidated() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return validated;
	}

	public boolean isBanned() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return banned;
	}
	
	public int getAlertCount() throws NamelessException {
		String postString = "uuid=" + PostString.urlEncodeString(uuid.toString());
		Request request = RequestUtil.sendRequest(RequestType.POST, baseUrl, "getNotifications", postString, https);
		
		if (!request.hasSucceeded()) {
			throw new NamelessException(request.getException());
		}
		
		JsonObject response = request.getResponse();
		JsonObject message = parser.parse(response.get("message").getAsString()).getAsJsonObject();
		return message.get("alerts").getAsInt();
	}
	
	public int getMessageCount() throws NamelessException {
		String postString = "uuid=" + PostString.urlEncodeString(uuid.toString());
		Request request = RequestUtil.sendRequest(RequestType.POST, baseUrl, "getNotifications", postString, https);
		
		if (!request.hasSucceeded()) {
			throw new NamelessException(request.getException());
		}
		
		JsonObject response = request.getResponse();
		JsonObject message = parser.parse(response.get("message").getAsString()).getAsJsonObject();
		return message.get("messages").getAsInt();
	}
	
	public void setGroup(String groupName) throws NamelessException {
		Request request = RequestUtil.sendRequest(RequestType.POST, baseUrl, "setGroup", "uuid=" + PostString.urlEncodeString(uuid.toString()) + "?group_id=", https);
		if (!request.hasSucceeded()) {
			throw new NamelessException(request.getException());
		}
	}

	public void updateUsername(String newUserName) throws NamelessException {
		String encodedId = PostString.urlEncodeString(uuid.toString());
		String encodedName = PostString.urlEncodeString(newUserName);
		String postString = "id=" + encodedId + "?new_username=" + encodedName;
		Request request = RequestUtil.sendRequest(RequestType.POST, baseUrl, "updateUsername", postString, https);
		if (!request.hasSucceeded()) {
			throw new NamelessException(request.getException());
		}
	}
	
	public void register(String minecraftName, String email) throws NamelessException {
		String encodedUuid = PostString.urlEncodeString(uuid.toString());
		String encodedName = PostString.urlEncodeString(minecraftName);
		String encodedEmail = PostString.urlEncodeString(email);
		String postString = String.format("username=%s&uuid=%s&email=%s", encodedUuid, encodedName, encodedEmail);

		Request request = RequestUtil.sendRequest(RequestType.POST, baseUrl, "register", postString, https);

		if (!request.hasSucceeded()) {
			String errorMessage = request.getException().getMessage();
			if (errorMessage.contains("Username") || errorMessage.contains("UUID") || errorMessage.contains("Email")) {
				throw new IllegalArgumentException(errorMessage);
			} else {
				throw new NamelessException(request.getException());
			}
		}
	}

	public NamelessReportPlayer reportPlayer(String[] args) {
		NamelessReportPlayer report = new NamelessReportPlayer(uuid, args);// TODO Fix this
		return report;
	}

}