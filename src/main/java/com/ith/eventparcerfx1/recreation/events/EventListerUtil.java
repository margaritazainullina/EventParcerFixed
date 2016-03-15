package com.ith.eventparcerfx1.recreation.events;

import com.ith.eventparcerfx1.Event;
import com.ith.eventparcerfx1.recreation.foursquare.FoursquareUtil;
import com.ith.eventparcerfx1.spots.GoogleMapsUtil;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import org.jsoup.select.Elements;

/**
 * Created by illya on 12.08.15.
 */
public class EventListerUtil {

    final static Logger logger = Logger.getLogger(EventListerUtil.class);
    private static final String urlStr = "https://vacation.hotwire.com/Hotel-Search?inpAjax=true&responsive=true";
    public static int numberOfLines = 0;
    public static List<String> cities = new ArrayList<>();
    public static String pathToCSV = "";
    private static String pathToCitiesCSV;
    public static String currentCountry = "";
    public static String currentIP;
    public static int currentPort;
    public static List<String> proxies = new ArrayList<>();
    public static String proxiesStr;

    public static void init(String pathToCSV1, String pathToCitiesCSV1) {
        if (!pathToCSV1.isEmpty()) {
            pathToCSV = pathToCSV1;
        }
        if (!pathToCitiesCSV1.isEmpty()) {
            pathToCitiesCSV = pathToCitiesCSV1;
        }
    }
    
    public static boolean switchProxy() throws Exception {
        
        for (int i = 0; i < proxies.size(); i++) {
            if (currentIP == null) {
                currentIP = proxies.get(0).split(":")[0];
                currentPort = Integer.parseInt(proxies.get(0).split(":")[1]);
                return true;
            }
            if (i != proxies.size() - 1) {
                if (proxies.get(i).split(":")[0].equals(currentIP)) {
                    currentIP = proxies.get(i + 1).split(":")[0];
                    currentPort = Integer.parseInt(proxies.get(i + 1).split(":")[1]);
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public static void process(LocalDate start, LocalDate end) throws MalformedURLException, IOException {
        // authorize();

        String date1 = start.getYear() + "-" + start.getMonthValue() + "-" + start.getDayOfMonth();
        String date2 = end.getYear() + "-" + end.getMonthValue() + "-" + end.getDayOfMonth();
        String clause = null;
        try {
            clause = URLEncoder.encode(" Status!='DELETED' AND EndDate>='" + date2 + "' AND StartDate>='" + date1 + "'  AND DateTrust!='APPROXIMATE'  AND VendorEvent='YES' ", "UTF-8");
        } catch (UnsupportedEncodingException ex) {
        }

        URL myURL1 = new URL("http://www.eventlister.com/events/search/?Framed=&FramedTheme=&"
                + "Radius=100&SearchLocationMethod=Zip&Zip=&CityState=&"
                + "StartDate_month=" + start.getMonthValue() + "&StartDate_day=" + start.getDayOfMonth() + "&StartDate_year=" + start.getYear() + "&"
                + "EndDate_month=" + end.getMonthValue() + "&EndDate_day=" + end.getDayOfMonth() + "&EndDate_year=" + end.getYear() + "&"
                + "Submit=true&PerPage=1000000&KeyWords=&SearchEventNumber=&"
                + "AddedSince_month=01&AddedSince_day=01&AddedSince_year=2002&"
                + "UpdatedSince_month=01&UpdatedSince_day=01&UpdatedSince_year=2002");

        //URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) myURL1.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");
        con.setRequestProperty("Host", "www.eventlister.com");
//        int responseCode = con.getResponseCode();
//        System.out.println("\nSending 'GET' request to URL : " + myURL1);
//        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Document doc = Jsoup.parse(response.toString());
        Element results = doc.select(".results").get(0);
        Elements trs = results.select("tr");
        for (int i = 1; i < trs.size(); i++) {
            parse(trs, i);
        }
    }

    public static Event parse(Elements trs, int i) {
        Event e = new Event();
        Element tr = trs.get(i);
        Elements tds = tr.select("td");
        if (tds.get(0).html().isEmpty()) {
            return null;
        }
        Element date = tds.get(0);
        //09/26-09/11 2015
        String stripped = date.html().replaceAll("<[^>]*>", "");
        String s[] = stripped.split("&nbsp;");
        String year = Calendar.getInstance().get(Calendar.YEAR) + "";
        try {
            year = stripped.split("&nbsp;")[1];
        } catch (Exception ex) {
        }
        String dateS1 = "";
        try {
            dateS1 = stripped.split("&nbsp;")[0].split("-")[0];
        } catch (Exception ex) {
            dateS1 = stripped.split("&nbsp;")[0];
        }
        String dateE1 = "";
        try {
            dateE1 = stripped.split("&nbsp;")[0].split("-")[1];
        } catch (Exception ex) {
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        if (!dateS1.isEmpty()) {
            try {
                e.setDate(LocalDate.parse(year + "/" + dateS1, formatter));
            } catch (Exception ex) {
            }
        }
        if (!dateE1.isEmpty()) {
            try {
                e.setEndDate(LocalDate.parse(year + "/" + dateE1, formatter));
            } catch (Exception ex) {
            }
        }
        try {
            int persentage = Integer.parseInt(tds.get(3).text().replaceAll("\\D+", ""));
            if (persentage < 40) {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }

        Element link = tds.get(1);
        String name = link.html();
        String html = "http://www.eventlister.com" + tds.get(3).select("a").get(1).attr("href");
        String address = tds.get(2).html();
        e.setName(name);
        e.setAddress(address);
        try {
            e = parseDetailsPage(html, e);

            File file = new File(pathToCSV + "/" + "eventLister.csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            if (FoursquareUtil.isEmpty(file)) {
                Files.write(Paths.get(pathToCSV + "/" + "eventLister.csv"), "Title;Description;Website;Latitude;Longitude;Address;Image links;Rating;Start date;End date\n".getBytes(), StandardOpenOption.APPEND);
            }
            if (e.isValid()) {
                Files.write(Paths.get(pathToCSV + "/" + "eventLister.csv"), e.toString().getBytes(), StandardOpenOption.APPEND);
            }
        } catch (Exception ex) {
            logger.error("EventLister: parsing failed");
        }
        return e;
    }

    private static Event parseDetailsPage(String urlStr, Event e) throws IOException, Exception {
        URL url = new URL(urlStr);
        URLConnection con = null;
        if (EventListerUtil.currentIP != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(EventListerUtil.currentIP, EventListerUtil.currentPort));
            con = url.openConnection(proxy);
        } else {
            con = url.openConnection();
        }

        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36");
        con.setRequestProperty("Host", "www.eventlister.com");
        con.setRequestProperty("Method", "GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        try {
            Document doc = Jsoup.parse(response.toString());

            String latitude = "";
            try {
                latitude = doc.select(".latitude").select(".value-title").get(0).html();
            } catch (Exception ex) {
            }
            String longitude = "";
            try {
                longitude = doc.select(".longitude").select(".value-title").get(0).html();
            } catch (Exception ex) {
            }
            Element title = doc.select("title").first();
            e.setName(title.text().split(",")[0]);

            Elements edbs = doc.select(".edb");
            try {
                logger.info("EventLister: parsing " + e.getName());
                Element block1 = edbs.get(0);
                String address = block1.text().replace("(?i)Event location", "").trim();
                Element block2 = edbs.get(2);
                String category = block2.select("table").select("tr").get(0).select("td").get(0).text();
                category = category.substring(category.indexOf("Primary Category") + 18, category.lastIndexOf("Additional Categories"));
                category = category.substring(category.indexOf("-") + 1, category.lastIndexOf("-")).trim();

                Element block3 = edbs.get(3);
                String desc = "";
                if (edbs.get(3).text().contains("Description")) {
                    desc = edbs.get(3).text().replace("Description", "").trim();;
                }
                e.setDesc(desc);
                e.setCategory(category);
            } catch (Exception ex) {
            }
            e.setLat(latitude);
            e.setLon(longitude);
            if (!latitude.isEmpty() && !longitude.isEmpty()) {
                e.setAddress(GoogleMapsUtil.getAddress(latitude, longitude));
            } else {
                Event e1 = GoogleMapsUtil.addCoordinatesInfo(e, e.getAddress().split(",")[0].trim());
                if (e1 != null) {
                    e = e1;
                }
            }
        } catch (Exception ex) {
            EventListerUtil.switchProxy();
            parseDetailsPage(urlStr, e);
        }
        return e;
    }

}
