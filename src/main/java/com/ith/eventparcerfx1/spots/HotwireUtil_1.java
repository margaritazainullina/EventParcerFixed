package com.ith.eventparcerfx1.spots;

import com.ith.eventparcerfx1.Establishment;
import com.ith.eventparcerfx1.recreation.foursquare.FoursquareUtil;
import static com.ith.eventparcerfx1.recreation.foursquare.FoursquareUtil.pathToCSV;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by illya on 12.08.15.
 */
public class HotwireUtil_1 {

    final static Logger logger = Logger.getLogger(HotwireUtil_1.class);
    public static final String urlStr = "https://vacation.hotwire.com/Hotel-Search?inpAjax=true&responsive=true";
    public static int numberOfLines = 0;
    public static List<String> cities = new ArrayList<>();
    public static String pathToCSV;
    public static String pathToCitiesCSV;
    public static String currentCountry = "";

    public static void init(String pathToCSV1, String pathToCitiesCSV1) {
        if (!pathToCSV1.isEmpty()) {
            pathToCSV = pathToCSV1;
        }
        if (!pathToCitiesCSV1.isEmpty()) {
            pathToCitiesCSV = pathToCitiesCSV1;
        }
    }

   
    public static Establishment addMissingInfoFromHotelPage(String url, Establishment h) {
        try {
            Document doc = Jsoup.connect(url.toString()).get();

            //get image links
            Element wrapper = doc.select(".jumbo-wrapper").get(0);
            List<String> imageLinks = new ArrayList<>();
            for (Element image : wrapper.select("img")) {
                String link = image.attr("src").trim();
                if (link.isEmpty()) {
                    link = image.attr("data-src").trim();
                }
                if (link.startsWith("//")) {
                    link = link.substring(2, link.length());
                }
                if (!link.isEmpty()) {
                    imageLinks.add(link);
                }
            }

            Element telephone = doc.select("[itemprop=\"telephone\"]").first();
            Element postalCode = doc.select(".postal-code").first();
            Element country = doc.select(".country").first();
            Element province = doc.select(".province").first();
            Element city = doc.select(".city").first();
            Element streetAddress = doc.select("[itemprop=\"street-address\"]").first();
            String address = "";
            if (country != null) {
                address += country.html() + ", ";
            }
            if (province != null) {
                address += province.html() + ", ";
            }
            if (city != null) {
                address += city.html() + ", ";
            }
            if (streetAddress != null) {
                address += streetAddress.html() + ", ";
            }
            if (postalCode != null) {
                address += postalCode.html();
            } else {
                address = address.substring(0, address.length() - 2);
            }
            h.setTelephone(telephone.html());
            h.setAddress(address);
            h.setImages(imageLinks);
            try {
                h.setAddress(GoogleMapsUtil.getAddress(h.getLat(), h.getLon()));
            } catch (UnsupportedEncodingException ex) {
                java.util.logging.Logger.getLogger(HotwireUtil_1.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (MalformedURLException e) {
            logger.error("Hotwire: Unable to load page " + url);
        } catch (IOException e) {
            logger.error("Hotwire: Unable to parse page " + url);
        }
        return h;
    }

    public static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

    public static void loadCitiesInfo() {
        try {
            int numberOfLines = countLines(pathToCitiesCSV);
        } catch (FileNotFoundException e) {
            logger.error("Hotwire: File is not found " + pathToCitiesCSV);
            return;
        } catch (IOException e) {
            logger.error("Hotwire: Error while loading cities" + pathToCitiesCSV);
            return;
        }

        //get cities for request
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(pathToCitiesCSV));
            String countryAndCity = null;
            int cityCount = 0;
            while ((countryAndCity = br.readLine()) != null) {
                cityCount++;
                cities.add(countryAndCity);
            }
        } catch (Exception e) {
            logger.error("Hotwire: Error while loading cities " + pathToCitiesCSV);
        }

    }

}
