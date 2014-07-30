/**
 * Class to control Urban Airship:
 * Sends a push notification
 * Logs each push
 * 
 * @author Matt Morgis
 * 
 */

package com.elsevier.ptg.urbanairship;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.commons.codec.binary.Base64;

public class UrbanAirshipIntegrator {

	public UrbanAirshipIntegrator() {
	}
	
	/**
	 * This is the preferred method used to send a push notification to a newsstand app. It will also log each push notification to a text file called 
	 * "Push_Log.txt", which will be stored where ever the code is executed. 
	 * 
	 *
	 * @param  appName  Internal app name. Only used in log file.
	 * @param  appKey  The app key that is provided by Urban Airship.
	 * @param  appMasterSecret  The app master secret proved by Urban Airship.
	 * @param  alertText  Optional. There is default alert text if this param is not specified. If set to 0, alert text will not be sent.
	 * @return      boolean indicating whether push was successful.
	 */
	public boolean sendPush(String appName, String appKey, String appMasterSecret, String alertText) {
		boolean result=false;
		try {
			URL url = new URL("https://go.urbanairship.com/api/push/broadcast/");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			String authString = appKey + ":" + appMasterSecret;
			String authStringBase64 = Base64.encodeBase64String(authString.getBytes());
			authStringBase64 = authStringBase64.trim();
			
			connection.setRequestProperty("Content-type", "application/json");
			connection.setRequestProperty("Authorization", "Basic " + authStringBase64);
			
			String json;
			
			if (alertText.equals("0")) {
				json = "{\"aps\":{\"badge\":1,\"content-available\":1}}"; 
			}
			else {
				json = "{\"aps\":{\"alert\":\"" + alertText + "\",\"badge\":1,\"content-available\":1}}"; 
			}
			
			OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
			osw.write(json);
			osw.close();
			
			
			int responseCode = connection.getResponseCode();
			
			logPush(appName, alertText, responseCode);
			
			if (responseCode != 200) {
				result = false;
			}
			else {
				result = true;
			}
			
					
		}
		catch (Exception e) {
			result=false;
			e.printStackTrace();
		}	
		return result;
		
	}
	
	/**
	 * This method contains the default alert text if the "alertText" param is not set.
	 * @param appName
	 * @param appKey
	 * @param appMasterSecret
	 */
	public void sendPush(String appName, String appKey, String appMasterSecret){
		sendPush (appName,appKey,appMasterSecret, "There is a new issue available.");
	}
	
	//logs the sent push to a text file
	private void logPush(String appName, String alertText, int responseCode) {
		File log = new File("Elsevier_JAT_Push_Log.txt");
		try {
			if (!log.exists()) {
				//System.out.println("New file created");
				log.createNewFile();
			}
			FileWriter fileWriter = new FileWriter(log, true);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat formatter = new SimpleDateFormat ("yyyy/MMM/dd HH:mm:ss");
			String currentTime = formatter.format(currentDate.getTime());
			
			bufferedWriter.write("\n");
			bufferedWriter.write("Push Notification for " + appName + " sent:");
			bufferedWriter.write("\n");
			bufferedWriter.write(currentTime);
			bufferedWriter.write("\n");
			bufferedWriter.write("Alert Text Sent: " + alertText);
			bufferedWriter.write("\n");
			if (responseCode != 200){
				bufferedWriter.write("Error sending push notification!");
				bufferedWriter.write("\n");
				bufferedWriter.write("HTTP Error: " + responseCode);
				bufferedWriter.write("\n");
			}
			
			else {
				bufferedWriter.write("Push sent successfully");
			}
			bufferedWriter.close();
		}
		catch (IOException e) {
			System.out.println("Could not log");
			e.printStackTrace();
		}
	}
}
