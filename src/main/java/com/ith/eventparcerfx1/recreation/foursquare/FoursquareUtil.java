package com.ith.eventparcerfx1.recreation.foursquare;

import com.ith.eventparcerfx1.Establishment;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FoursquareUtil {

    public static String clientId;
    public static String clientSecret;
    public static int credentialsIdx = 1;
    public static HashMap<String, String> credentials = new HashMap<>();

    public static String pathToCSV;
    public static String pathToCitiesCSV;
    public static List<String> cities = new ArrayList<>();
    public final static Logger logger = Logger.getLogger(FoursquareUtil.class);
    public static int currentPage = 0;
    public static String oldJson = "";

    public static boolean isEmpty(File f) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(f));
        if (br.readLine() == null) {
            return true;
        }
        return false;
    }

    public static List<String> loadCities(String pathToCitiesCSV1) {
        pathToCitiesCSV = pathToCitiesCSV1;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(pathToCitiesCSV1));
            String countryAndCity = null;
            while ((countryAndCity = br.readLine()) != null) {
                cities.add(countryAndCity);
            }
        } catch (Exception e) {
            logger.error("Foursquare: Error while loading cities" + pathToCitiesCSV1);
        }
        return cities;
    }

    public static int numberOfCities(String pathToCitiesCSV1) {
        int number = 0;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(pathToCitiesCSV1));
            String countryAndCity = null;
            while ((countryAndCity = br.readLine()) != null) {
                number++;
            }
        } catch (Exception e) {
            logger.error("Foursquare: Unable to read file: " + pathToCitiesCSV1);
        }
        return number;
    }

    public static String swapCityCountry(String dest) {
        if (dest.split(",").length == 1) {
            return dest;
        }
        if (dest.split(",").length == 2) {
            String[] countries = {"Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei ", "Bulgaria", "Burkina Faso", "Burma", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Central African Republic", "Chad", "Chile", "China", "Colombia", "Comoros", "Congo", "Costa Rica", "Cote d'Ivoire", "Croatia", "Cuba", "Curacao", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Fiji", "Finland", "France", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Greece", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Holy See", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea, North", "Korea", "Kosovo", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Macedonia", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mexico", "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro", "Morocco", "Mozambique", "Namibia", "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Zealand", "Nicaragua", "Niger", "Nigeria", "North Korea", "Norway", "Oman", "Pakistan", "Palau", "Palestinian Territories", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Romania", "Russia", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa ", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Sint Maarten", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Korea", "South Sudan", "Spain ", "Sri Lanka", "Sudan", "Suriname", "Swaziland ", "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand ", "Timor-Leste", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe", "US", "USA", "United States of America", "UK"};
            String part1 = dest.split(",")[0];
            String part2 = dest.split(",")[1];
            for (String country : countries) {
                if (country.toLowerCase().trim().contains(part1.toLowerCase())) {
                    return part2 + "," + part1;
                }
            }
        }
        if (dest.split(",").length == 3) {
            String[] countries = {"Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei ", "Bulgaria", "Burkina Faso", "Burma", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Central African Republic", "Chad", "Chile", "China", "Colombia", "Comoros", "Congo", "Costa Rica", "Cote d'Ivoire", "Croatia", "Cuba", "Curacao", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Fiji", "Finland", "France", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Greece", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Holy See", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea, North", "Korea", "Kosovo", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Macedonia", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mexico", "Micronesia", "Moldova", "Monaco", "Mongolia", "Montenegro", "Morocco", "Mozambique", "Namibia", "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Zealand", "Nicaragua", "Niger", "Nigeria", "North Korea", "Norway", "Oman", "Pakistan", "Palau", "Palestinian Territories", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Romania", "Russia", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa ", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Sint Maarten", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Korea", "South Sudan", "Spain ", "Sri Lanka", "Sudan", "Suriname", "Swaziland ", "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand ", "Timor-Leste", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe", "US", "USA", "United States of America", "UK"};
            String part1 = dest.split(",")[0];
            String part2 = dest.split(",")[1];
            String part3 = dest.split(",")[2];
            for (String country : countries) {
                if (country.toLowerCase().trim().contains(part1.toLowerCase())) {
                    return part3 + "," + part2 + "," + part1;
                }
            }
        }
        return dest;
    }

    public static Establishment parseHtml(String urlStr, Establishment e) {
        Document doc = null;
        try {
            doc = Jsoup.connect(urlStr).get();
            //get imagelinks
            List<String> images = new ArrayList<>();
            for (Element li : doc.getElementsByClass("photoWithContent")) {
                String img = li.getElementsByTag("img").attr("src").replaceAll("(\\d*)x(\\d*)", "width900");
                images.add(img);
            }
            e.setImages(images);
        } catch (IOException e1) {
            logger.info("No images in " + urlStr);
        }
        try {
            //get website
            Element div1 = doc.getElementsByClass("websiteLink").get(0);
            Element div2 = div1.getElementsByClass("linkAttrValue").get(0);
            Element a = div2.getElementsByTag("a").get(0);
            String url = a.attr("href");
            e.setSite(url);
        } catch (Exception e1) {
        }
        return e;
    }

    public static String getJson(String destination, int offset, String categoryId) {
        StringBuilder json = new StringBuilder();

        String dest = destination;
        try {
            dest = URLEncoder.encode(destination, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(FoursquareUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        String urlStr = "https://api.foursquare.com/v2/search/recommendations?locale=en&explicit-lang=true&v=20150813&m=foursquare&limit=100"
                + "&mode=locationInput&near=" + dest + "&client_id=" + clientId + "&client_secret=" + clientSecret
                + "&v=20140806&categoryId=" + categoryId + "&offset=" + offset;

        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 400) {
                logger.error("No info about " + destination);
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = rd.readLine()) != null) {
                    json.append(line);
                }
                rd.close();
            }
        } catch (Exception e) {
            try {
                switchCredentials();
                getJson(destination, offset, categoryId);
            } catch (Exception ex) {
                logger.error("Foursquare: Error while processing " + destination + ", page" + offset);
            }
        }
        if (!json.equals(oldJson)) {
            logger.info("Foursquare: Processing destination " + destination + ", page " + (offset / 100));
        }

        return json.toString();
    }

    public static void loadCredentials() {
        //authenticate for downloading
        Properties properties = new Properties();
        InputStream input = ClassLoader.getSystemResourceAsStream("foursquare.authentication.properties");

        try {
            // load a properties file
            properties.load(input);
        } catch (IOException ex) {
            logger.error("Foursquare: Credentials load error");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error("Foursquare: Credentials load error");
                }
            }
        }
        for (int i = 1; i < properties.size() + 1; i++) {
            credentials.put("client_id" + i, properties.getProperty("client_id" + i));
            credentials.put("client_secret" + i, properties.getProperty("client_secret" + i));
        }
        clientId = credentials.get("client_id1");
        clientSecret = credentials.get("client_secret1");
        logger.info("Foursquare: Credentials loaded");
    }

    public static void switchCredentials() {
        credentialsIdx++;
        if (credentialsIdx >= credentials.size()) {
            credentialsIdx = 1;
        }
        clientId = credentials.get("client_id" + credentialsIdx);
        clientSecret = credentials.get("client_secret" + credentialsIdx);
    }

    public static String getIdOfCategory(String category) {
        Properties properties = new Properties();
        InputStream input = ClassLoader.getSystemResourceAsStream("foursquare.categories.properties");

        try {
            // load a properties file
            properties.load(input);
        } catch (IOException ex) {
            logger.error("Foursquare: Properties load error");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return properties.getProperty(category);
    }

    public static ObservableList<String> getCategories() {
        Properties properties = new Properties();
        InputStream input = ClassLoader.getSystemResourceAsStream("foursquare.categories.properties");

        try {
            // load a properties file
            properties.load(input);
        } catch (IOException ex) {
            logger.error("Foursquare: Properties load error");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        List<String> s = new ArrayList<>();
        for (Object key : properties.keySet()) {
            s.add(key.toString());
        }
        return FXCollections.observableArrayList(s);
    }

}
