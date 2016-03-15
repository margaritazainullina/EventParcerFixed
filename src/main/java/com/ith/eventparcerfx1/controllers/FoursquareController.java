/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ith.eventparcerfx1.controllers;

import com.ith.eventparcerfx1.Establishment;
import com.ith.eventparcerfx1.TextAreaAppender;
import com.ith.eventparcerfx1.recreation.events.EventListerUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
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
import com.ith.eventparcerfx1.recreation.foursquare.FoursquareUtil;
import static com.ith.eventparcerfx1.recreation.foursquare.FoursquareUtil.loadCities;
import static com.ith.eventparcerfx1.recreation.foursquare.FoursquareUtil.loadCredentials;
import static com.ith.eventparcerfx1.recreation.foursquare.FoursquareUtil.pathToCSV;
import static com.ith.eventparcerfx1.recreation.foursquare.FoursquareUtil.pathToCitiesCSV;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

/**
 * FXML Controller class
 *
 * @author illya
 */
public class FoursquareController implements Initializable {

    @FXML
    private CheckBox includeImagesF;
    @FXML
    private TextArea log;
    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private TextField outputPathF;
    @FXML
    private TextArea citiesListF;
    @FXML
    private Button startButtonF;
    @FXML
    private BorderPane searchPane;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private ProgressBar progressBar;

    private boolean isRunning = false;

    private boolean isChanged = false;
    private boolean prevUnnecessary = false;

    private static String pathToFileFoursquareCsv = "";
    private static String pathToCitiesFoursquareCsv = "";
    private static List<String> selectedCategories = new ArrayList();
    private static List<String> cities = new ArrayList();
    FoursquareUtil fu = new FoursquareUtil();
    final static Logger logger = Logger.getLogger(FoursquareUtil.class);

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        TextAreaAppender.setTextArea(log);
        createFoursquareCategories();
        if (citiesListF != null) {
            citiesListF.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    ifAllStatedFoursquare();
                }
            });
        }
    }

    private void createFoursquareCategories() {
        if (searchPane != null) {
            ObservableList<String> foursquareValues = FoursquareUtil.getCategories();

            // Create the CheckComboBox with the data 
            final CheckComboBox<String> checkComboBox = new CheckComboBox<String>(foursquareValues);
            searchPane.setLeft(checkComboBox);
            FoursquareUtil.loadCredentials();

            checkComboBox.getCheckModel().getSelectedItems().addListener(new ListChangeListener<String>() {
                public void onChanged(ListChangeListener.Change<? extends String> c) {
                    selectedCategories = checkComboBox.getCheckModel().getSelectedItems();
                    ifAllStatedFoursquare();
                }
            });
        }
    }

    private void ifAllStatedFoursquare() {
        if (isRunning) {
            startButtonF.setDisable(true);
        } else {
            boolean disable = false;
            if (selectedCategories.isEmpty()) {
                disable = true;
            } else if (pathToCitiesFoursquareCsv.isEmpty() && citiesListF.getText().isEmpty()) {
                disable = true;
            } else if (pathToFileFoursquareCsv.isEmpty()) {
                disable = true;
            }
            startButtonF.setDisable(disable);
        }

    }

    @FXML
    private void onKeyTyped() {
        isChanged = true;
    }

    @FXML
    private void startFoursquare(ActionEvent event) throws IOException, InterruptedException {
        FoursquareUtil.cities = cities;
        //ifAllStatedFoursquare();
        isRunning = true;
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        startButtonF.setDisable(true);
                        if (!isChanged) {
                            for (String category : selectedCategories) {
                                process(pathToFileFoursquareCsv, pathToCitiesFoursquareCsv, category, includeImagesF.isSelected());
                            }
                        } else {
                            for (String category : selectedCategories) {
                                cities = loadFromTextArea();
                                process(pathToFileFoursquareCsv, cities, category, includeImagesF.isSelected());
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
                String s = "Parsing info from foursquare is completed.";
                alert.setContentText(s);
                startButtonF.setDisable(false);

                Optional<ButtonType> result = alert.showAndWait();
            }
        };
        service.start();

    }

    public void process(String pathToCSV1, String pathToCitiesCSV1, String category, boolean withImages) {
        pathToCSV = pathToCSV1;
        pathToCitiesCSV = pathToCitiesCSV1;

        final int categoryIndex = selectedCategories.indexOf(category);

        loadCredentials();
        loadFromTextArea();
        //loadCities(pathToCitiesCSV);
        final String categoryId1 = FoursquareUtil.getIdOfCategory(category);
        final boolean withImages1 = withImages;
        int i = 0;
        for (String city : cities) {
            i++;
            progressBar.setProgress(0f);
            final String city1 = city;
            final int numberOfCities1 = cities.size();
            final int currentCity = i;
            final float progress1 = (float) ((currentCity * 1.0 * (categoryIndex + 1) / (numberOfCities1 * 1.0 * selectedCategories.size())));
            //final float progress1 = (float) ((currentCity * 1.0 / (numberOfCities1 * 1.0)));
            progressBar.setProgress(progress1);

            paging(city1, categoryId1, withImages1);

        }
        FoursquareUtil.currentPage = 0;
        if (categoryIndex == selectedCategories.size() - 1) {
            progressBar.setProgress(1f);
        }
    }

    private void parseJson(String json, boolean withImages) throws IOException, Exception {

        Establishment h;
        JSONObject obj = new JSONObject(json.trim());
        JSONObject response = obj.getJSONObject("response");
        JSONObject group = response.getJSONObject("group");
        JSONArray results = group.getJSONArray("results");
        int totalResults = group.getInt("totalResults");
        if (totalResults == 0) {
            throw new Exception("No results found");
        }

        if (results.length() == 0) {
            logger.info("Foursquare: No results for ");
        } else {
            logger.info("Foursquare: Found " + totalResults + " results");
            FoursquareUtil.currentPage += results.length();

            for (int i = 0; i < results.length(); i++) {
                try {
                    double progress1 = (1.0 / (totalResults * 1.0)) / (cities.size() * selectedCategories.size());
                    progressBar.setProgress(progressBar.getProgress() + progress1);

                    JSONObject venue = results.getJSONObject(i).getJSONObject("venue");
                    JSONObject snippets = results.getJSONObject(i).getJSONObject("snippets");
                    String name = venue.getString("name");
                    Double rating = -1d;
                    try {
                        rating = venue.getDouble("rating") / 2.0;
                    } catch (Exception e) {
                    }

                    JSONObject location = venue.getJSONObject("location");
                    String lat = "";
                    String lon = "";
                    try {
                        lat = location.getDouble("lat") + "";
                        lon = location.getDouble("lng") + "";
                    } catch (Exception e) {
                    }

                    String address = "";
                    try {
                        if (location.getString("address") != null) {
                            address += location.getString("address") + ", ";
                        }
                    } catch (Exception e) {
                    }
                    try {
                        if (location.getString("state") != null) {
                            address += location.getString("state") + ", ";
                        }
                    } catch (Exception e) {
                    }
                    try {
                        if (location.getString("city") != null) {
                            address += location.getString("city") + ", ";
                        }
                    } catch (Exception e) {
                    }
                    try {
                        if (location.getString("country") != null) {
                            address += location.getString("country") + ", ";
                        }
                    } catch (Exception e) {
                    }
                    try {
                        if (location.getString("postalCode") != null) {
                            address += location.getString("postalCode") + ", ";
                        }
                    } catch (Exception e) {
                    }
                    address = address.substring(0, address.length() - 2);
                    JSONArray categories = venue.getJSONArray("categories");
                    String category = categories.getJSONObject(0).getString("name").replace("/", "_");
                    String phone = "";
                    try {
                        JSONObject contact = results.getJSONObject(i).getJSONObject("contact");
                        phone = contact.getString("phone");
                    } catch (Exception e) {
                    }

                    String desc = "";
                    String name1 = "";
                    String url = "";;

                    try {
                        JSONObject item = (JSONObject) snippets.getJSONArray("items").get(0);
                        JSONObject detail = item.getJSONObject("detail");
                        JSONObject object = detail.getJSONObject("object");
                        desc = object.getString("text");
                        name1 = name.toLowerCase().replace(" ", "-");
                        url = "https://foursquare.com/v/" + name1 + "/" + venue.getString("id");

                    } catch (Exception e) {
                    }

                    h = new Establishment(address, desc, category, new ArrayList<String>(), lat, lon, name, rating, phone, "");
                    try {
                        if (withImages) {
                            FoursquareUtil.parseHtml(url, h);
                        }
                    } catch (Exception e) {
                    }
                    String filename = "";
                    if (location.getString("country") != null) {
                        filename = location.getString("country") + "-";
                    }
                    filename += category + ".csv";

                    File file = new File(pathToCSV + "/" + filename);
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    if (FoursquareUtil.isEmpty(file)) {
                        Files.write(Paths.get(pathToCSV + "/" + filename), "Title;Description;Website;Latitude;Longitude;Address;Image links;Rating\n".getBytes(), StandardOpenOption.APPEND);
                    }
                    if (h.isValid()) {
                        Files.write(Paths.get(pathToCSV + "/" + filename), h.toString().getBytes(), StandardOpenOption.APPEND);
                        logger.info("Foursquare: info about " + h.getName() + ", " + h.getAddress() + " is successfully parsed and saved ");
                    }
                } catch (Exception e) {
                }
            }

        }
    }

    public boolean isUnnecessaryPage(String json) {
        JSONObject obj;
        try {
            obj = new JSONObject(json.trim());
            JSONObject response = obj.getJSONObject("response");
            JSONObject group = response.getJSONObject("group");
            JSONArray results = group.getJSONArray("results");

            if (results.length() == 1) {
                JSONObject displayType = (JSONObject) (results.get(0));
                if (displayType.getString("displayType").equals("headerAndChips")) {
                    return true;
                }
            }
        } catch (JSONException ex) {
            return true;
        }
        return false;
    }

    public void paging(String city, String categoryId, boolean withImages) {
        try {
            String newJson = FoursquareUtil.getJson(FoursquareUtil.swapCityCountry(city), FoursquareUtil.currentPage, categoryId);
            if (newJson.isEmpty()) {
                return;
            }
         /*   if (isUnnecessaryPage(newJson) && isUnnecessaryPage(FoursquareUtil.oldJson)) {
                FoursquareUtil.currentPage++;
                FoursquareUtil.oldJson = newJson;
                paging(city, categoryId, withImages);
            }*/
            if (newJson.equals(FoursquareUtil.oldJson)||
                    (isUnnecessaryPage(newJson) && isUnnecessaryPage(FoursquareUtil.oldJson))) {
                FoursquareUtil.oldJson = "";
                FoursquareUtil.currentPage = 0;
            } else {
                try {
                    FoursquareUtil.oldJson = newJson;
                    parseJson(newJson, withImages);
                    paging(city, categoryId, withImages);
                } catch (IOException e) {
                    FoursquareUtil.oldJson = "";
                    FoursquareUtil.currentPage = 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void process(String pathToCSV1, List<String> cities1, String category, boolean withImages) {
        pathToCSV = pathToCSV1;
        //loadCredentials();
        cities = cities1;
        try {
            progressBar.setProgress(0f);
            int i = 0;
            for (String city : cities) {
                i++;
                paging(city, FoursquareUtil.getIdOfCategory(category), withImages);
                final int categoryIndex = selectedCategories.indexOf(category);
                final int numberOfCities1 = cities.size();
                // final float progress1 = (float) ((i * 1.0 * (categoryIndex + 1) / (numberOfCities1 * 1.0 * selectedCategories.size())));
                // progressBar.setProgress(progress1);
            }
            progressBar.setProgress(1f);

        } catch (Exception e) {
        }
    }

    @FXML
    private void loadCitiesFoursquare(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        //Show open file dialog
        File f = fileChooser.showOpenDialog(null);
        pathToCitiesFoursquareCsv = f.getAbsolutePath();
        final int numberOfCities = FoursquareUtil.numberOfCities(pathToCitiesFoursquareCsv);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final BufferedReader br;
                try {
                    br = new BufferedReader(new FileReader(pathToCitiesFoursquareCsv));

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
                                    java.util.logging.Logger.getLogger(HotwireController.class
                                            .getName()).log(Level.SEVERE, null, ex);
                                }
                                float progress1 = (float) ((currentCity * 1.0) / (numberOfCities * 1.0));
                                progress.setProgress(progress1);
                                cities.add(countryAndCity);
                                citiesListF.appendText(countryAndCity + "\n");
                                if (currentCity == numberOfCities - 1) {
                                    progress.setProgress(
                                            1f);
                                }
                            }
                        });

                    }

                } catch (FileNotFoundException ex) {
                    java.util.logging.Logger.getLogger(FoursquareController.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).
                start();
        FoursquareUtil.cities = cities;
        // ifAllStatedFoursquare();
    }

    @FXML
    private void fileDialogueFoursquare(ActionEvent event) {

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Output folder");
        File selectedDirectory = chooser.showDialog(null);

        pathToFileFoursquareCsv = selectedDirectory.getAbsolutePath();
        outputPathF.setText(pathToFileFoursquareCsv);
        ifAllStatedFoursquare();
    }

    private List<String> loadFromTextArea() {
        String list = citiesListF.getText();
        String[] citiesArr = list.split("\n");
        cities = new ArrayList<String>(Arrays.asList(citiesArr));
        ifAllStatedFoursquare();
        return cities;
    }

}
