/*
 * 	--Wacky Weather App--
 * 
 * -Java application to give the user an accurate weather forecast for a given city. 
 * -This application utilizes Open-Meteo's weather forecast and geolocation APIs to fetch weather data for the user
 * 
 * Author: Daylon Maze
 */

import javax.swing.*;

public class WeatherAppLauncher {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				
				//Set the window to visible, starting the application
				new WeatherAppGUI().setVisible(true);
				
				
			}
		});
	}
}
