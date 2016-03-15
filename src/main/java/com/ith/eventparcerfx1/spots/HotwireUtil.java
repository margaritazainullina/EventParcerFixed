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

/**
 * Created by illya on 12.08.15.
 */
public class HotwireUtil {

    final static Logger logger = Logger.getLogger(HotwireUtil.class);
    private static final String urlStr = "https://vacation.hotwire.com/Hotel-Search?inpAjax=true&responsive=true";
    public static int numberOfLines = 0;
    public static List<String> cities = new ArrayList<>();
    private static String pathToCSV;
    private static String pathToCitiesCSV;
    public static String currentCountry = "";

    public static void init(String pathToCSV1, String pathToCitiesCSV1) {
        if (!pathToCSV1.isEmpty()) {
            pathToCSV = pathToCSV1;
        }
        if (!pathToCitiesCSV1.isEmpty()) {
            pathToCitiesCSV = pathToCitiesCSV1;
        }
    }

    public static void process(String destination) {

        Date today = new Date();
        SimpleDateFormat dateFormat = null;

        Calendar c = Calendar.getInstance();
        c.setTime(today);
        c.add(Calendar.DATE, 1);
        Date tomorrow = c.getTime();
        c.add(Calendar.DATE, 2);
        Date dayAfterTomorrow = c.getTime();

        dateFormat = new SimpleDateFormat("MM/dd/YYYY");
        String date1 = dateFormat.format(today);
        String date2 = dateFormat.format(tomorrow);
        String date3 = dateFormat.format(dayAfterTomorrow);
        destination = FoursquareUtil.swapCityCountry(destination);
        loadJsonsFromRest(destination, date2, date3, 1);
    }

    private static void loadJsonsFromRest(String destination, String date1, String date2, int adults) {
        try {
            String parameters = "destination=" + URLEncoder.encode(destination, "UTF-8") + "&startDate="
                    + URLEncoder.encode(date1, "UTF-8")
                    + "endDate=" + URLEncoder.encode(date2, "UTF-8") + "&adults=" + adults;

            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

            writer.write(parameters);
            writer.flush();
            String line;
            StringBuffer json = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            writer.close();
            reader.close();

            parceJson(json.toString(), destination);
        } catch (Exception e) {
        }
    }

    private static void parceJson(String json, String destination) {
        try {
            JSONObject obj = new JSONObject(json.trim());
            JSONArray results = obj.getJSONArray("results");
            Establishment h;
            if (results.length() == 0) {
                logger.info("Hotwire: No results for " + destination);
            } else {
                logger.info("Hotwire: Found " + results.length() + " hotels in " + destination);
                for (int i = 0; i < results.length(); i++) {
                    JSONObject hotel = results.getJSONObject(i).getJSONObject("retailHotelInfoModel");
                    String name = hotel.getString("hotelName");
                    String category = hotel.getString("structureType");
                    String desc = hotel.getString("hotelDescription");
                    String lat = hotel.getString("latitude");
                    String lon = hotel.getString("longitude");
                    double stars = Double.parseDouble(hotel.getString("hotelStarRating"));
                    h = new Establishment("", desc, category, new ArrayList<String>(), lat, lon, name, stars, "", "");

                    String infositeUrl = "";
                    try {
                        infositeUrl = results.getJSONObject(i).getString("infositeUrl");
                        int index = infositeUrl.indexOf("Hotel-Information") + 17;
                        infositeUrl = infositeUrl.substring(0, index);
                        h = addMissingInfoFromHotelPage(infositeUrl, h);

                        String filename = destination.split(",")[1];
                        filename += "-" + category + ".csv";

                        File file = new File(pathToCSV + "/" + filename);
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        if (FoursquareUtil.isEmpty(file)) {
                            Files.write(Paths.get(pathToCSV + "/" + filename), "Title;Description;Website;Latitude;Longitude;Address;Image links;Rating\n".getBytes(), StandardOpenOption.APPEND);
                        }
                        Files.write(Paths.get(pathToCSV + "/" + filename), h.toString().getBytes(), StandardOpenOption.APPEND);
                        logger.error("Hotwire: Info about hotel: " + h.getName() + " " + h.getAddress() + " is successfully parsed and saved!");

                    } catch (Exception e) {
                        logger.error("Hotwire: Unable to write info about hotel: " + h.getName() + " " + h.getAddress());
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static Establishment addMissingInfoFromHotelPage(String url, Establishment h) {
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
            logger.error("Hotwire: Unknown error " + pathToCitiesCSV);
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
            logger.error("Hotwire: Unknown error " + pathToCitiesCSV);
        }

    }

}
