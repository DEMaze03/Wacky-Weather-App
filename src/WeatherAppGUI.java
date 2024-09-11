import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.json.simple.JSONObject;

public class WeatherAppGUI extends JFrame{
	private JSONObject weatherData;
	
	private int width = 450;
	private int height = 650;

	public WeatherAppGUI() {
		//setup GUI and add title
		setTitle("Wacky Weather App");
		
		//end the program's process once it has been closed
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		//set the window size (in pixels)
		setSize(width, height);
		
		//Always load window at the center of the screen
		setLocationRelativeTo(null);
		
		//set the layout manager to null to manually position elements in the window
		setLayout(null);
		
		//prevent window resizing
		setResizable(false);
		
		
		addGuiComponents();
	}
	
	private void addGuiComponents() {
		//search field
		JTextField searchField = new JTextField();
		
		//add a prompt to the search field
		searchField.setToolTipText("Enter a Location:");
		
		//set the location and size of search field
		searchField.setBounds(15, 15, 351, 45);
		
		//set font style and size
		searchField.setFont(new Font("Dialog", Font.PLAIN, 24));
		
		add(searchField);
		
		//weather image - load image, set position, add to window
		JLabel weatherImage = new JLabel(loadImage("src\\Assets\\cloudy.png"));
		weatherImage.setBounds(0,125,450,217);
		add(weatherImage);
		
		//text for the temperature. In farenheit. set position, set font style and size
		JLabel temperatureText = new JLabel("--- F");
		temperatureText.setBounds(0,350,450,54);
		temperatureText.setFont(new Font("Dialog", Font.BOLD, 48));
		
		//align the temperature text to the center, add to window
		temperatureText.setHorizontalAlignment(SwingConstants.CENTER);
		add(temperatureText);
		
		//text for the weather description. set position, set font style and size
		JLabel weatherConditionText = new JLabel("Cloudy");
		weatherConditionText.setBounds(0,405,450,36);
		weatherConditionText.setFont(new Font("Dialog", Font.PLAIN, 32));
		
		//align the weather condition text to the center, add to window
		weatherConditionText.setHorizontalAlignment(SwingConstants.CENTER);
		add(weatherConditionText);
		
		//humidity image - load the image, set its position, add to window
		JLabel humidityImage = new JLabel(loadImage("src\\Assets\\humidity.png"));
		humidityImage.setBounds(15,500,74,66);
		add(humidityImage);
		
		//text for the humidity. Set the text and format it using HTML, set the position, set font and style, add to window
		JLabel humidityText = new JLabel("<html><b>Humidity</b> 100%</html>");
		humidityText.setBounds(90,500,85,55);
		humidityText.setFont(new Font("Dialog", Font.PLAIN, 16));
		add(humidityText);
		
		//windspeed image - load the image, set its position, add to window
		JLabel windspeedImage = new JLabel(loadImage("src\\Assets\\windspeed.png"));
		windspeedImage.setBounds(220,500,74,66);
		add(windspeedImage);
		
		//text for the windspeed. Set the text and format it using HTML, set the position, set font and style, add to window
		JLabel windspeedText = new JLabel("<html><b>Windspeed</b> 15Mph</html>");
		windspeedText.setBounds(310,500,85,55);
		windspeedText.setFont(new Font("Dialog", Font.PLAIN, 16));
		add(windspeedText);
		
		//Search Button
		JButton searchButton = new JButton(loadImage("src\\Assets\\search.png"));
		
		//change the cursor to a hand when hovering over the search button
		searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		searchButton.setBounds(375,13,47,45);
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//get location from the search field
				String userInput = searchField.getText();
				
				//validate input, remove whitespace
				if(userInput.replaceAll("\\s", "").length() <= 0) {
					return;
				}
				
				//get weather data
				weatherData = WeatherAppBackend.getWeatherData(userInput);
				
				//update GUI
				//update weather condition text
				String weatherCondition = (String) weatherData.get("weather_condition");
				
				//update weather image depending on condition
				switch(weatherCondition) {
				case "Clear":
					weatherImage.setIcon(loadImage("src\\Assets\\clear.png"));
				break;
				case "Cloudy":
					weatherImage.setIcon(loadImage("src\\Assets\\cloudy.png"));
				break;
				case "Rain":
					weatherImage.setIcon(loadImage("src\\Assets\\rain.png"));
				break;
				case "Snow":
					weatherImage.setIcon(loadImage("src\\Assets\\snow.png"));
				break;
				}
				
				//update temperature text
				double temperature = (double) weatherData.get("temperature");
				temperatureText.setText(temperature + "F");
				
				//update weather condition text
				weatherConditionText.setText(weatherCondition);
				
				//update humidity text
				long humidity = (long) weatherData.get("humidity");
				humidityText.setText("<html><b>Humidity</b> " + humidity + "%</html>");
				
				//update windspeed text
				double windSpeed = (double) weatherData.get("windspeed");
				windspeedText.setText("<html><b>Windspeed</b> " + windSpeed + "Mph</html>");
				
				
			}
		});
		add(searchButton);
		
	}
	
	//used to create images for GUI components
	private ImageIcon loadImage(String path) {
		try {
			//read the image file from the given path
			BufferedImage image = ImageIO.read(new File(path));
			
			//return the icon so it can be rendered
			return new ImageIcon(image);
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		//error message
		System.out.println("Could not find resource");
		return null;
	}
}