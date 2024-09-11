import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/*
 *   --WeatherAppBackend--
 * - Retrieves weather data from the APIs, this logic will fetch the latest weather data from Open-Meteo forecast API, GUI will display the fetched data to the user
 * 
 * 	 Author: Daylon Maze
 */

public class WeatherAppBackend {
	//fetch the weather data for a given location
	public static JSONObject getWeatherData(String locationName) {
		//get data from the geolocation API for a given location
		JSONArray locationData = getLocationData(locationName);
		
		//extract the latitude and longitude data
		JSONObject location = (JSONObject) locationData.get(0);
		double latitude = (double) location.get("latitude");
		double longitude = (double) location.get("longitude");
		
		//build the API request URL with the location's coordinates
		String urlString = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude + "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&temperature_unit=fahrenheit&wind_speed_unit=mph&precipitation_unit=inch&timezone=auto";
		
		try {
			//call the API and record its response
			HttpURLConnection connection = fetchApiResponse(urlString);
			
			//check for response status - 200 means success
			if (connection.getResponseCode() != 200) {
				System.out.println("Error: Could not connect to API");
				return null;
			}
			
			//store the resulting JSON data
			StringBuilder resultJson = new StringBuilder();
			Scanner scanner = new Scanner(connection.getInputStream());
			while(scanner.hasNext()) {
				resultJson.append(scanner.nextLine());
			}
			
			//close the scanner
			scanner.close();
			
			//close the URL connection
			connection.disconnect();
			
			//parse the data
			JSONParser parser = new JSONParser();
			JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));
			
			//get hourly data
			JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
			
			//we need the current hour's data, so get the index of the current hour
			JSONArray time = (JSONArray) hourly.get("time");
			int index = findIndexOfCurrentTime(time);
			
			//get temperature
			JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
			double temperature = (double) temperatureData.get(index);
			
			//get the weather code
			JSONArray weatherCode = (JSONArray) hourly.get("weather_code");
			String weatherCondition = convertWeatherCode((long) weatherCode.get(index));
			
			//get the humidity
			JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
			long humidity = (long) relativeHumidity.get(index);
			
			//get the windspeed
			JSONArray windspeedData = (JSONArray) hourly.get("wind_speed_10m");
			double windSpeed = (double) windspeedData.get(index);
			
			//create our own JSON object and store all gathered data there, then return the JSON object
			JSONObject weatherData = new JSONObject();
			weatherData.put("temperature", temperature);
			weatherData.put("weather_condition", weatherCondition);
			weatherData.put("humidity", humidity);
			weatherData.put("windspeed", windSpeed);
			
			return weatherData;
			
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	//retrieves coordinates for a given location name
	public static JSONArray getLocationData(String locationName) {
		
		//replaces all whitespace with a '+' for the API call
		locationName = locationName.replaceAll(" ", "+");
		
		//generate the URL for the API call
		String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" + locationName + "&count=10&language=en&format=json";
		
		try {
			//call the API and record its response
			HttpURLConnection conn = fetchApiResponse(urlString);
			
			//check for response status - 200 means success
			if(conn.getResponseCode() != 200) {
				System.out.println("Error: Could not connect to API");
				return null;
			}else {
				//store the API results
				StringBuilder resultJson = new StringBuilder();
				Scanner scanner = new Scanner(conn.getInputStream());
				
				//read and store the JSON data into resultJson
				while(scanner.hasNext()) {
					resultJson.append(scanner.nextLine());
				}
				
				//close the scanner
				scanner.close();
				
				//close the URL connection
				conn.disconnect();
				
				//parse the JSON string and cast it to type JSONObject
				JSONParser parser = new JSONParser();
				JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));
				
				//get the list of location data the API generated from the given location
				JSONArray locationData = (JSONArray) resultsJsonObj.get("results");
				return locationData;
				
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static HttpURLConnection fetchApiResponse(String urlString) {
		try {
			
			//try to connect, use URI to avoid deprecated URL cosntructor
			URI uri = new URI(urlString);
	        URL url = uri.toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			connection.setRequestMethod("GET");
			
			//connect to the api and return 
			connection.connect();
			return connection;
		}catch(IOException e) {
			e.printStackTrace();
		} catch(URISyntaxException e) {
			e.printStackTrace();
		}
		
		//if could not make connection
		return null;
	}
	
	private static int findIndexOfCurrentTime(JSONArray timeList) {
		String currentTime = getCurrentTime();
		
		//iterate through time list until time matches current time
		for(int i = 0; i < timeList.size(); i++) {
			String time = (String) timeList.get(i);
			if(time.equalsIgnoreCase(currentTime)) {
				return i;
			}
		}
		
		return 0;
	}
	
	private static String getCurrentTime() {
		//get the current date and time
		LocalDateTime currentDateTime = LocalDateTime.now();
		
		//format the date and time to YYYY-MM-DDTHH:00 (ex: 2024-09-08T06:00) 
		//This is how the time and date is formatted in the API
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");
		
		//format and return the date and time
		String formattedDateTime = currentDateTime.format(formatter);
		
		return formattedDateTime;
	}
	
	//convert the weather code to be more readable
	private static String convertWeatherCode(long weathercode) {
		String weatherCondition = "";
		if(weathercode == 0L) { //
			weatherCondition = "Clear";
		}else if (weathercode <= 3L && weathercode > 0L) { //For simplification, weather codes 1 through 3 are considered "Cloudy"
			weatherCondition = "Cloudy";
		}else if ((weathercode >= 51L && weathercode <= 67L) || (weathercode >= 80L && weathercode <= 99L)) { //For simplification, weather codes 51 through 67 and 80 through 99 are considered "Rain"
			weatherCondition = "Rain";
		}else if (weathercode >= 71L && weathercode <= 77L) { //For simplification, weather codes 71 through 77 are considered "Snow"
			weatherCondition = "Snow";
		}
		
		return weatherCondition;
	}
	
}
