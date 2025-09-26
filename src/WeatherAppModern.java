import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import org.json.*;
import javax.imageio.ImageIO;

public class WeatherAppModern extends JFrame {

    private JTextField cityField;
    private JButton getButton;
    private JLabel todayLabel, tomorrowLabel, todayIcon, tomorrowIcon;

    private final String apiKey = "a61cd8b5ac30b4dfc918396d9e23d1a5"; // kendi anahtarını gir!

    public WeatherAppModern() {
        setTitle("Hava Durumu");
        setSize(500, 380);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // ekran ortası

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(240, 245, 255));
        mainPanel.setLayout(null);

        JLabel title = new JLabel("Hava Durumu Tahminleri");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBounds(130, 10, 300, 40);
        mainPanel.add(title);

        cityField = new JTextField(16);
        cityField.setFont(new Font("Arial", Font.PLAIN, 18));
        cityField.setBounds(80, 60, 210, 36);
        mainPanel.add(cityField);

        getButton = new JButton("Getir");
        getButton.setFont(new Font("Arial", Font.BOLD, 18));
        getButton.setBounds(300, 60, 110, 36);
        mainPanel.add(getButton);

        todayLabel = new JLabel("Bugünün bilgisi...");
        todayLabel.setFont(new Font("Arial", Font.PLAIN, 17));
        todayLabel.setBounds(40, 110, 350, 30);
        mainPanel.add(todayLabel);

        todayIcon = new JLabel();
        todayIcon.setBounds(390, 105, 80, 80);
        mainPanel.add(todayIcon);

        tomorrowLabel = new JLabel("Yarının bilgisi...");
        tomorrowLabel.setFont(new Font("Arial", Font.PLAIN, 17));
        tomorrowLabel.setBounds(40, 200, 350, 30);
        mainPanel.add(tomorrowLabel);

        tomorrowIcon = new JLabel();
        tomorrowIcon.setBounds(390, 195, 80, 80);
        mainPanel.add(tomorrowIcon);

        JLabel credit = new JLabel("Powered by OpenWeatherMap");
        credit.setFont(new Font("Arial", Font.ITALIC, 12));
        credit.setBounds(160, 310, 200, 20);
        credit.setForeground(Color.GRAY);
        mainPanel.add(credit);

        add(mainPanel);
        getButton.addActionListener(e -> fetchWeather());
        cityField.addActionListener(e -> fetchWeather());
    }

    private void fetchWeather() {
        String city = cityField.getText().trim();
        if (city.isEmpty()) {
            todayLabel.setText("Lütfen şehir girin.");
            tomorrowLabel.setText("");
            todayIcon.setIcon(null);
            tomorrowIcon.setIcon(null);
            return;
        }
        try {
            // Forecast API: 5 günlük, 3 saatlik aralıklarla veri döner
            String urlString = "https://api.openweathermap.org/data/2.5/forecast?q="
                    + URLEncoder.encode(city, "UTF-8") + "&appid=" + apiKey + "&units=metric&lang=tr";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");

            int status = conn.getResponseCode();
            if (status != 200) {
                todayLabel.setText("Şehir bulunamadı veya API hatası!");
                tomorrowLabel.setText("");
                todayIcon.setIcon(null);
                tomorrowIcon.setIcon(null);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            JSONArray list = json.getJSONArray("list");

            // Bugün: ilk veri (şu anki saat)
            JSONObject todayObj = list.getJSONObject(0);
            String todayDesc = todayObj.getJSONArray("weather").getJSONObject(0).getString("description");
            String todayMain = todayObj.getJSONArray("weather").getJSONObject(0).getString("main");
            double todayTemp = todayObj.getJSONObject("main").getDouble("temp");
            int todayHumidity = todayObj.getJSONObject("main").getInt("humidity");

            todayLabel.setText("Bugün: " + todayTemp + "°C, " + todayDesc + ", Nem: " + todayHumidity + "%");
            todayIcon.setIcon(getWeatherImage(todayMain));

            // Yarın: yaklaşık 24 saat sonranın verisi (8 x 3 saat = 24 saat)
            JSONObject tomorrowObj = list.getJSONObject(8);
            String tomorrowDesc = tomorrowObj.getJSONArray("weather").getJSONObject(0).getString("description");
            String tomorrowMain = tomorrowObj.getJSONArray("weather").getJSONObject(0).getString("main");
            double tomorrowTemp = tomorrowObj.getJSONObject("main").getDouble("temp");
            int tomorrowHumidity = tomorrowObj.getJSONObject("main").getInt("humidity");

            tomorrowLabel.setText("Yarın: " + tomorrowTemp + "°C, " + tomorrowDesc + ", Nem: " + tomorrowHumidity + "%");
            tomorrowIcon.setIcon(getWeatherImage(tomorrowMain));

        } catch (Exception ex) {
            todayLabel.setText("Hata: " + ex.getMessage());
            tomorrowLabel.setText("");
            todayIcon.setIcon(null);
            tomorrowIcon.setIcon(null);
        }
    }

    // Hava durumu tipine göre görsel seç
    private ImageIcon getWeatherImage(String main) {
        String imgPath = "/img/sunny.png";
        main = main.toLowerCase();
        if (main.contains("cloud")) imgPath = "/img/cloudy.png";
        else if (main.contains("rain")) imgPath = "/img/rainy.png";
        else if (main.contains("snow")) imgPath = "/img/snowy.png";
        else if (main.contains("clear")) imgPath = "/img/sunny.png";
        try {
            Image img = ImageIO.read(getClass().getResource(imgPath));
            return new ImageIcon(img.getScaledInstance(78, 78, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WeatherAppModern().setVisible(true));
    }
}
