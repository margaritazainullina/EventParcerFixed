package com.ith.eventparcerfx1.poi;

import com.ith.eventparcerfx1.Establishment;
import com.ith.eventparcerfx1.TextAreaAppender;
import com.sun.org.apache.xerces.internal.dom.DeferredElementImpl;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextArea;

/**
 * Created by margarita on 07.08.15.
 */
public class PoiUtil {

    final static Logger logger = Logger.getLogger(PoiUtil.class);
    static int page = 0;
    static int lastPage = 0;
    static String pathToCSV;

    static int successfullyParsed = 0;
    static int parsingFailed = 0;
    static int csv = 0;
    static int gpx = 0;

    public static void init(String pathToCSV1) {
        pathToCSV = pathToCSV1;
        PoiAuthenticator.authenticate();
    }

    public static void setLogAppender(TextArea a) {
        TextAreaAppender.setTextArea(a);
    }

    public static void process(String category) {
        Properties properties = new Properties();
        InputStream input = ClassLoader.getSystemResourceAsStream("poi.categories.properties");

        try {
            // load a properties file
            properties.load(input);
            getHtmlPages(properties.getProperty(category), category);
        } catch (IOException ex) {
            logger.error("Properties load error");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static ObservableList<String> getCategories() {
        Properties properties = new Properties();
        InputStream input = ClassLoader.getSystemResourceAsStream("poi.categories.properties");

        try {
            // load a properties file
            properties.load(input);
        } catch (IOException ex) {
            logger.error("Properties load error");
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

    private static String download(String pathToCatregoriesListFile) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(pathToCatregoriesListFile));
            String line = null;

            while ((line = br.readLine()) != null) {
                String category = line.split(",")[0];
                logger.info("Start downloading category: " + category);
                String url = line.split(",")[1];
                getHtmlPages(url, category);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
            }
        }
        return "";
    }

    private static String getHtmlPages(String urlStr, String category) throws IOException {
        page = 0;
        while (page <= lastPage) {
            URL url = null;
            logger.info("Start downloading files from url: " + urlStr + ", page: " + page);
            try {
                if (page != 0) {
                    url = new URL(urlStr + "?page=" + page);
                } else {
                    url = new URL(urlStr);
                }
                URLConnection yc = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        yc.getInputStream(), "UTF-8"));
                String inputLine;
                StringBuilder a = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    a.append(inputLine);
                    a.append("\r\n");
                }
                in.close();
                getNodesIds(url, category);
            } catch (Exception e) {
                logger.error("Wrong page url: " + url);
            }
            page++;
        }
        return "";
    }

    private static HashMap<Integer, String> getNodesIds(URL url, String category) {
        HashMap<Integer, String> ids = new HashMap<Integer, String>();
        Document doc = null;
        try {
            doc = Jsoup.connect(url.toString()).get();

            //get page count
            Elements pager = doc.select(".pager-next");
            try {
                for (Element node : pager) {
                    Elements links = node.select("a");
                    String s = links.get(links.size() - 1).text();
                    lastPage = Integer.parseInt(s);
                }
            } catch (Exception e) {
            }

            Elements nodes = doc.select(".node");
            for (Element node : nodes) {
                Elements links = node.select("a");
                Integer id = null;
                String fileName = "";
                for (Element link : links) {
                    Pattern pattern = Pattern.compile("(/node/)(\\d+)");
                    Matcher m = pattern.matcher(link.attr("href").toString());
                    while (m.find()) {
                        String s = m.group(2);
                        id = Integer.parseInt(s);
                    }
                }
                Elements content = node.select(".content");

                Pattern pattern = Pattern.compile("(.*\\.(gpx|csv))");
                Matcher m = pattern.matcher(content.toString());
                if (m.find()) {
                    fileName = m.group(1);
                }

                if (id != null) {
                    ids.put(id, fileName);
                }
            }
        } catch (IOException e) {
            logger.error("Error in getting nodesIds on the page, going to the next page");
        }

        //parce and get node ids
        Iterator it = ids.entrySet().iterator();
        while (it.hasNext()) {
            String format = "";
            Map.Entry pair = (Map.Entry) it.next();
            Pattern pattern = Pattern.compile("(.*\\.(gpx|csv))");
            Matcher m = pattern.matcher(pair.getValue().toString());
            while (m.find()) {
                format = m.group(2);
            }
            String downloadUrl = "http://www.poi-factory.com/poifile/download/" + format + "?node=" + pair.getKey();
            try {
                downloadFile(category, format, downloadUrl, pair.getValue().toString());
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(PoiUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ids;
    }

    private static void downloadFile(String category, String format, String urlStr, String filename) {

        try {

            //get cookies from start page
            URL myURL1 = new URL("http://www.poi-factory.com");
            HttpURLConnection connection = (HttpURLConnection) myURL1.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            connection.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8,ru;q=0.6,uk;q=0.4,de;q=0.2");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/37.0.2062.120 Chrome/37.0.2062.120 Safari/537.36");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.connect();
            String cookies1 = connection.getHeaderField("Set-Cookie").split(";")[0];

            //login
            URL u = new URL("http://www.poi-factory.com/user");
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.8,ru;q=0.6,uk;q=0.4,de;q=0.2");
            conn.setRequestProperty("Cache-Control", "max-age=0");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Length", "73");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Cookie", "__utmt=1; __gads=ID=2940b2ecbe4ee03e:T=1440159387:S=ALNI_Ma_Q2_xMd31pz_L9EsreX1slsSbCQ; " + cookies1 + "; __utma=193733932.1887744279.1440159367.1440159367.1440159367.1; __utmb=193733932.7.10.1440159367; __utmc=193733932; __utmz=193733932.1440159367.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/37.0.2062.120 Chrome/37.0.2062.120 Safari/537.36");
            String data = "name=margarita&pass=TgbydNaDig&form_id=user_login&un_zone_code=&op=Log+in";
            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            conn.connect();
            String cookies3 = conn.getHeaderField("Set-Cookie").split(";")[0];

            logger.info("Starting downloading file: " + urlStr);

            BufferedInputStream bis = null;
            URL myURL = new URL(urlStr);
            HttpURLConnection connection1 = (HttpURLConnection) myURL.openConnection();
            connection1.setRequestMethod("GET");
            connection1.setRequestProperty("Content-Type", "text/" + format);
            connection1.setRequestProperty("Content-Language", "en-US");
            connection1.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            connection1.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
            connection1.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/37.0.2062.120 Chrome/37.0.2062.120 Safari/537.36");
            connection1.setRequestProperty("Cookie", "__utmt=1; __gads=ID=2940b2ecbe4ee03e:T=1440159387:S=ALNI_Ma_Q2_xMd31pz_L9EsreX1slsSbCQ; " + cookies3 + "; __utma=193733932.1887744279.1440159367.1440159367.1440159367.1; __utmb=193733932.7.10.1440159367; __utmc=193733932; __utmz=193733932.1440159367.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=");
            
            connection1.connect();
            InputStream is = connection1.getInputStream();
            bis = new BufferedInputStream(is, 4 * 1024);
            final Path destination = Paths.get(filename);
            File file = new File(filename);
            if (!file.exists()) {
                File parent = file.getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    logger.error("Couldn't create dir: " + parent);
                }
            }

            Files.copy(bis, destination);
            logger.info("File successfully downloaded and saved to " + filename + "!");
            parseFile(filename);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(PoiUtil.class.getName()).log(Level.SEVERE, null, ex);

        }

    }

    private static void parseFile(String path) {
        File f = new File(path);

        String extension = f.getAbsoluteFile().toString().substring(f.getAbsoluteFile().toString().lastIndexOf(".") + 1, f.getAbsoluteFile().toString().length());
        if (extension.equals("csv")) {
            csv++;
            parseCSV(f);
        } else if (extension.equals("gpx")) {
            gpx++;
            parseGPX(f);
        } else {
            logger.error("Unknown extension ." + extension);
            parsingFailed++;
        }
    }

    private static void parseCSV(File f) {
        logger.info("Parsing file:" + f.getAbsolutePath() + "...");
        String category = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf("/") + 1, f.getAbsolutePath().length());
        StringBuffer sb = new StringBuffer();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] elems = line.split(",");
                if (elems.length == 3) {
                    //input format: "-96.815831","32.741735","Dallas Zoo 650 South R.L. Thorton Freeway. Dallas"
                    //output format: name, description, category, site, lat, lon, street, city, state, postal code
                    sb.append(elems[2] + ";; ;;" + category + ";; ;;" + elems[0] + ";;" + elems[1] + ";; \n");
                } else {
                    String[] tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    if (tokens.length == 4) {
                        //-93.06681,34.52289,AR Hot Springs NP,"Contact  Info P.O. Box 1860 Hot Springs ,AR 71902,501-624-3383"
                        sb.append(tokens[2] + ";; ;;" + category + ";; ;; ;;" + tokens[0] + ";;" + tokens[1] + ";;" + tokens[3] + "\n");
                    } else {
                        parsingFailed++;
                        logger.error("Error in parsing file: " + f.getAbsolutePath());
                        return;
                    }
                }
            }
        } catch (IOException e) {
            parsingFailed++;
            logger.error("Error while reading from file " + f.getAbsolutePath());
        }
        try {
            logger.info("File is successfully parsed: " + f.getAbsolutePath());
            Files.write(Paths.get(pathToCSV), sb.toString().getBytes(), StandardOpenOption.APPEND);
            successfullyParsed++;
        } catch (IOException e) {
            parsingFailed++;
            logger.error("Error while writing to csv file ");
            parsingFailed++;
        }
        f.delete();
    }

    private static void parseGPX(File f) {
        logger.info("Parsing file:" + f.getAbsolutePath() + "...");
        String[] folders = f.getAbsolutePath().split("/");
        String category = folders[folders.length - 2];
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        StringBuilder sb = new StringBuilder();
        try {
            builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(f);
            NodeList wpt = document.getElementsByTagName("wpt");
            for (int i = 0; i < wpt.getLength(); i++) {
                String lat = wpt.item(i).getAttributes().getNamedItem("lat").getNodeValue();
                String lon = wpt.item(i).getAttributes().getNamedItem("lon").getNodeValue();
                NodeList elements = wpt.item(i).getChildNodes();
                String name = "";
                String desc = "";
                String street = "";
                String city = "";
                String state = "";
                String pc = "";
                String telephone = "";
                String site = "";

                for (int j = 0; j < elements.getLength(); j++) {
                    if (elements.item(j) instanceof DeferredElementImpl) {
                        String content = elements.item(j).getTextContent();
                        switch (elements.item(j).getNodeName()) {
                            case "desc":
                                desc = content;
                                break;
                            case "name":
                                name = content;
                                break;
                            case "link":
                                site = elements.item(j).getAttributes().getNamedItem("href").getNodeValue();
                                break;
                            case "extensions": {
                                NodeList extensionsChildren = elements.item(j).getChildNodes();
                                for (int k = 0; k < extensionsChildren.getLength(); k++) {
                                    NodeList waypointExtensionsChildren = extensionsChildren.item(k).getChildNodes();
                                    if (extensionsChildren.item(k).getNodeName().equals("gpxx:WaypointExtension")) {
                                        for (int l = 0; l < waypointExtensionsChildren.getLength(); l++) {
                                            if (waypointExtensionsChildren.item(l).getNodeName().equals("gpxx:Address")) {
                                                for (int m = 0; m < waypointExtensionsChildren.item(l).getChildNodes()
                                                        .getLength(); m++) {
                                                    Node n = waypointExtensionsChildren.item(l).getChildNodes().item(m);
                                                    switch (n.getNodeName()) {
                                                        case "gpxx:StreetAddress":
                                                            street = n.getTextContent();
                                                            break;
                                                        case "gpxx:City":
                                                            city = n.getTextContent();
                                                            break;
                                                        case "gpxx:State":
                                                            state = n.getTextContent();
                                                            break;
                                                        case "gpxx:PostalCode":
                                                            pc = n.getTextContent();
                                                            break;
                                                        case "gpxx:PhoneNumber":
                                                            telephone = n.getTextContent();
                                                            break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                String address = street;
                if (!address.isEmpty()) {
                    address += ", ";
                }
                address += city;
                if (!address.isEmpty()) {
                    address += ", ";
                }
                address += state;
                if (!address.isEmpty()) {
                    address += ", ";
                }
                address += pc;
                Establishment e = new Establishment(address, desc, category, null, lat, lon, name, -1, telephone, site);
                sb.append(e.toString());
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            parsingFailed++;
            logger.error("Parsing failed");
        }
        try {
            logger.info("File is successfully parsed: " + f.getAbsolutePath());
            Files.write(Paths.get(pathToCSV), sb.toString().getBytes(), StandardOpenOption.APPEND);
            successfullyParsed++;
        } catch (IOException e) {
            parsingFailed++;
            logger.error("Error while writing to csv file ");
        }
        f.delete();
    }

}
