/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ith.eventparcerfx1.controllers;

import com.ith.eventparcerfx1.Event;
import com.ith.eventparcerfx1.TextAreaAppender;
import com.ith.eventparcerfx1.recreation.events.SeatwaveUtil;
import static com.ith.eventparcerfx1.recreation.events.SeatwaveUtil.key;
import static com.ith.eventparcerfx1.recreation.events.SeatwaveUtil.loadCities;
import com.ith.eventparcerfx1.recreation.foursquare.FoursquareUtil;
import com.ith.eventparcerfx1.spots.GoogleMapsUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import org.controlsfx.control.CheckComboBox;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

/**
 * FXML Controller class
 *
 * @author illya
 */
public class SeatwaveController implements Initializable {

    @FXML
    private CheckBox includeImages;
    @FXML
    private TextArea log;
    @FXML
    private DatePicker startDate;
    @FXML
    private TextField outputPath;
    @FXML
    private TextArea citiesList;
    @FXML
    private Button startButton;
    @FXML
    private BorderPane searchPane;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private ProgressBar progressBar;

    private boolean isChanged = false;
    private boolean isRunning = false;

    private static String pathToFileCsv = "";
    private static String pathToCitiesCsv = "";
    private static List<String> selectedCategories = new ArrayList();
    private static List<String> cities = new ArrayList();
    public final static Logger logger = Logger.getLogger(SeatwaveUtil.class);

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        TextAreaAppender.setTextArea(log);
        createCategories();
        if (citiesList != null) {
            citiesList.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    ifAllStated();
                }
            });
        }
    }

    @FXML
    private void onKeyTyped() {
        isChanged = true;
    }

    private void createCategories() {
        if (searchPane != null) {
            ObservableList<String> values = SeatwaveUtil.getCategories();

            // Create the CheckComboBox with the data 
            final CheckComboBox<String> checkComboBox = new CheckComboBox<String>(values);
            searchPane.setLeft(checkComboBox);

            checkComboBox.getCheckModel().getSelectedItems().addListener(new ListChangeListener<String>() {
                public void onChanged(ListChangeListener.Change<? extends String> c) {
                    selectedCategories = checkComboBox.getCheckModel().getSelectedItems();
                    ifAllStated();
                }
            });
        }
    }

    private void ifAllStated() {
        if (isRunning) {
            startButton.setDisable(true);
        } else {
            boolean disable = false;
            if (selectedCategories.isEmpty()) {
                disable = true;
            } else if (pathToCitiesCsv.isEmpty() && citiesList.getText().isEmpty()) {
                disable = true;
            } else if (pathToFileCsv.isEmpty()) {
                disable = true;
            } else if (startDate.getValue() == null) {
                disable = true;
            }
            startButton.setDisable(disable);
        }
    }

    @FXML
    private void start(ActionEvent event) throws IOException {
        isRunning = true;
        pathToFileCsv = outputPath.getText();
        SeatwaveUtil.init(pathToFileCsv, pathToCitiesCsv);
        FoursquareUtil.cities = cities;
        //ifAllStated();

        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        startButton.setDisable(true);
                        if (!isChanged) {
                            for (String category : selectedCategories) {
                                process(pathToFileCsv, pathToCitiesCsv, category, startDate.getValue());
                            }
                        } else {
                            cities = loadFromTextArea();
                            for (String category : selectedCategories) {
                                process(pathToFileCsv, cities, category, startDate.getValue());
                            }
                        }
                        return null;
                    }
                };
            }

            @Override
            protected void succeeded() {
                isRunning = false;
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Info");
                alert.setHeaderText("Done");
                String s = "Parsing info from seatwave is completed.";
                alert.setContentText(s);
                startButton.setDisable(false);

                Optional<ButtonType> result = alert.showAndWait();
            }

            @Override
            protected void failed() {
                isRunning = false;
                startButton.setDisable(false);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                String s = "Quota is exceeded. Try again later.";
                alert.setContentText(s);

                Optional<ButtonType> result = alert.showAndWait();
            }
        };
        service.start();

    }

    public void process(String pathToCSV1, String pathToCitiesCSV1, String category, LocalDate start) throws Exception {
        pathToFileCsv = pathToCSV1;
        pathToFileCsv = pathToCitiesCSV1;

        int categoryIndex = selectedCategories.indexOf(category);

        loadCities(pathToFileCsv);
        final String category1 = category;
        progressBar.setProgress(0f);
        System.out.println("s" + progressBar);
        for (String city : cities) {
            Double progress1 = ((cities.indexOf(city)) * 1.0 / cities.size()) * ((categoryIndex + 1) * 1.0 / selectedCategories.size() * 1.0);
            progressBar.setProgress(progress1);
            final String city1 = city;
            final LocalDate start1 = start;
            try {
                getJson(category1, FoursquareUtil.swapCityCountry(city1), start1);
            } catch (UnsupportedEncodingException ex) {
                java.util.logging.Logger.getLogger(SeatwaveController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (categoryIndex == selectedCategories.size() - 1) {
            logger.info("Seatwave: Done!");
            progressBar.setProgress(1f);
        }
    }

    public void process(String pathToCSV1, List<String> cities, String category, LocalDate start) throws Exception {
        pathToFileCsv = pathToCSV1;
        int categoryIndex = selectedCategories.indexOf(category);
        progressBar.setProgress(0f);
        for (String city : cities) {
            try {
                Double progress1 = ((cities.indexOf(city)) * 1.0 / cities.size()) * ((categoryIndex + 1) * 1.0 / selectedCategories.size() * 1.0);
                progressBar.setProgress(progress1);
                getJson(category, city, start);
            } catch (UnsupportedEncodingException ex) {
                java.util.logging.Logger.getLogger(SeatwaveController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        logger.info("Seatwave: Done!");
        progressBar.setProgress(1f);
    }

    public Event parseJson(String xml, String category, String where) throws Exception {
        Event h = null;
        try {
            JSONArray events = null;
            JSONObject obj = XML.toJSONObject(xml);
            events = obj.getJSONObject("EventsResponse").getJSONObject("Events").getJSONArray("Event");

            logger.info("Seatwave: Found " + events.length() + " events in category " + category);

            for (int i = 0; i < events.length(); i++) {
                try {
                    JSONObject event = (JSONObject) events.get(i);
                    String name = event.getString("EventGroupName");
                    String link = event.getString("SwURL");
                    String date = event.getString("Date");
                    String desc = "";
                    try {
                        desc = event.getString("Details");
                    } catch (JSONException e) {
                    }
                    String img = "";
                    try {
                        img = event.getString("EventGroupImageURL");
                    } catch (JSONException e) {
                    }
                    String dest = event.getString("VenueName");
                    String country = event.getString("Country");
                    String city = event.getString("Town");
                    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                    LocalDate d = LocalDate.parse(date, formatter);
                    List<String> images = new ArrayList<>();
                    images.add(img);
                    h = new Event(dest, desc, category, images, "", "", name, -1, "", link, d);

                    String old = h.getAddress();
                    if (GoogleMapsUtil.isQuotaExceeded()) {
                        throw new Exception("Quota is exceeded");
                    }
                    try {
                        GoogleMapsUtil.addCoordinatesInfo(h, city);
                        if (!h.getAddress().equals(old)) {
                            h.setAddress(dest + ", " + h.getAddress());
                        }
                    } catch (Exception ex) {
                    }
                    if (h.getLat().isEmpty()) {
                        h.setAddress(h.getAddress() + ", " + city + ", " + country);
                    }
                    if (country.equals("USA")) {
                        country = "United States";
                    }
                    String filename = country + "-" + category + ".csv";

                    File file = new File(SeatwaveUtil.pathToCSV + "/" + filename);
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    if (FoursquareUtil.isEmpty(file)) {
                        Files.write(Paths.get(SeatwaveUtil.pathToCSV + "/" + filename), "Title;Description;Website;Latitude;Longitude;Address;Image links;Rating;Start date;End date\n".getBytes(), StandardOpenOption.APPEND);
                    }

                    if (h.isValid()) {
                        Files.write(Paths.get(SeatwaveUtil.pathToCSV + "/" + filename), h.toString().getBytes(), StandardOpenOption.APPEND);
                        logger.info("Info about " + name + " parsed and saved to " + SeatwaveUtil.pathToCSV);
                    }

                    double progress1 = (1.0 / (events.length() * 1.0)) / (cities.size() * selectedCategories.size());
                    progressBar.setProgress(progressBar.getProgress() + progress1);

                } catch (Exception e) {
                }
            }
        } catch (JSONException e) {
            logger.error("Seatwave: no results found in category " + category);
        }
        return h;
    }

    public String getJson(String what, String where, LocalDate start) throws UnsupportedEncodingException, Exception {
        StringBuilder json = new StringBuilder();
        String date = start.getYear() + "-" + start.getMonthValue() + "-" + start.getDayOfMonth();
        if (where.split(",").length > 1) {
            where = where.split(",")[0];
        }

        String urlStr = "http://api-sandbox.seatwave.com/v2/discovery/events?"
                + "apikey=" + key + "&siteid=1&what=" + what + "&where=" + URLEncoder.encode(where, "UTF-8") + "&whenfrom=" + date + "&pgsize=1000&format=json";

        HttpURLConnection conn;
        String line;
        BufferedReader rd = null;
        try {
            URL url1 = new URL(urlStr);
            conn = (HttpURLConnection) url1.openConnection();
            conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                json.append(line);
            }
        } catch (Exception e) {
            logger.error("Seatwave: Error while processing category " + what + " in " + where);
        } finally {
            try {
                rd.close();
            } catch (IOException ex) {
            }
        }
        parseJson(json.toString(), what, where);
        return json.toString();
    }

    @FXML
    private void loadCitiesSeatwave(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        //Show open file dialog
        File f = fileChooser.showOpenDialog(null);
        pathToCitiesCsv = f.getAbsolutePath();
        final int numberOfCities = FoursquareUtil.numberOfCities(pathToCitiesCsv);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final BufferedReader br;
                try {
                    br = new BufferedReader(new FileReader(pathToCitiesCsv));
                    for (int i = 0; i < numberOfCities; i++) {
                        final int currentCity = i;

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progress.setOpacity(1);
                                String countryAndCity = null;
                                try {
                                    countryAndCity = br.readLine();
                                } catch (IOException ex) {
                                    java.util.logging.Logger.getLogger(HotwireController.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                float progress1 = (float) ((currentCity * 1.0) / (numberOfCities * 1.0));
                                progress.setProgress(progress1);
                                cities.add(countryAndCity);
                                citiesList.appendText(countryAndCity + "\n");
                                if (currentCity == numberOfCities - 1) {
                                    progress.setProgress(
                                            1f);
                                }
                            }
                        });
                    }
                } catch (FileNotFoundException ex) {
                    java.util.logging.Logger.getLogger(FoursquareController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).
                start();
        FoursquareUtil.cities = cities;
    }

    @FXML
    private void fileDialogue(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Output folder");
        File selectedDirectory = chooser.showDialog(null);
        pathToFileCsv = selectedDirectory.getAbsolutePath();
        outputPath.setText(pathToFileCsv);
        SeatwaveUtil.init(pathToFileCsv, pathToCitiesCsv);
        ifAllStated();
    }

    private List<String> loadFromTextArea() {
        String list = citiesList.getText();
        String[] citiesArr = list.split("\n");
        cities = new ArrayList<String>(Arrays.asList(citiesArr));
        ifAllStated();
        return cities;
    }
}
