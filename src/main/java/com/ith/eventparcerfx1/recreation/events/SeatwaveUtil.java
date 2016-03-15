package com.ith.eventparcerfx1.recreation.events;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * @author Margarita
 */
public class SeatwaveUtil {

    public final static Logger logger = Logger.getLogger(SeatwaveUtil.class);
    public static String pathToCSV = null;
    public static String pathToCitiesCSV = null;
    public final static String key = "ec8a08c313184b6fb4e162a759f98315";
    public static List<String> cities = new ArrayList<>();

    public static void init(String pathToCSV1, String pathToCitiesCSV1) {
        if (!pathToCSV1.isEmpty()) {
            pathToCSV = pathToCSV1;
        }
        if (!pathToCitiesCSV1.isEmpty()) {
            pathToCitiesCSV = pathToCitiesCSV1;
        }
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
            e.printStackTrace();
            logger.error("Seatwave: Error while loading cities " + pathToCitiesCSV1);
        }
        return cities;
    }

    public static ObservableList<String> getCategories() {
        List<String> s = new ArrayList<>(Arrays.asList("Concerts", "Sport", "Theatre", "Comedy", "Festivals"));
        return FXCollections.observableArrayList(s);
    }
}
