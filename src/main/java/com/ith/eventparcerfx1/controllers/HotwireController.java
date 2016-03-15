/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ith.eventparcerfx1.controllers;

import static com.ith.eventparcerfx1.controllers.FoursquareController.logger;
import com.ith.eventparcerfx1.recreation.foursquare.FoursquareUtil;
import com.ith.eventparcerfx1.spots.HotwireUtil;
import static com.ith.eventparcerfx1.spots.HotwireUtil.cities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author illya
 */
public class HotwireController implements Initializable {

    @FXML
    private TextArea logH;
    @FXML
    private TextField outputPathH;
    @FXML
    private TextArea citiesListH;
    @FXML
    private Button loadCitiesButtonH;
    @FXML
    private Button startButtonH;
    @FXML
    private BorderPane searchPane;
    @FXML
    private ProgressIndicator progress;
    @FXML
    private ProgressBar progressBar;

    private static String pathToFileHotwireCsv = "";
    private static String pathToCitiesHotwireCsv = "";
    private static List<String> cities = new ArrayList();
    final static Logger logger = Logger.getLogger(HotwireController.class);
    private boolean isChanged = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        citiesListH.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ifAllStatedHotwire();
            }
        });
    }

    @FXML
    private void onKeyTyped() {
        isChanged = true;
    }

    private void ifAllStatedHotwire() {
        boolean disable = false;
        if (pathToCitiesHotwireCsv.isEmpty() && citiesListH.getText().isEmpty()) {
            disable = true;
        } else if (pathToFileHotwireCsv.isEmpty()) {
            disable = true;
        }
        startButtonH.setDisable(disable);

    }

    public void process(List<String> cities1) {
        cities = cities1;
        process();
    }

    public void process() {
        for (int i = 0; i < cities.size(); i++) {
            progressBar.setProgress(0f);
            final String city1 = cities.get(i);
            final int numberOfCities1 = cities.size();
            final int currentCity = i;
            final float progress1 = (float) ((currentCity * 1.0) / (numberOfCities1 * 1.0));
            progressBar.setProgress(progress1);
            //make request and save jsons
            String destination = "";
            if (city1.split(",").length == 2) {
                destination = city1.split(",")[1].trim();
                HotwireUtil.currentCountry = city1.split(",")[0].trim();
                destination = HotwireUtil.currentCountry + "," + destination;
                HotwireUtil.process(destination);
            } else {
                destination = city1.split(",")[0].trim();
                HotwireUtil.process(destination);
            }
            if (currentCity == numberOfCities1 - 1) {
                progressBar.setProgress(1f);
            }
        }
    }

    @FXML
    private void startHotwire(ActionEvent event) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isChanged) {
                    loadFromTextArea();
                }
                // process(pathToFileFoursquareCsv, pathToCitiesFoursquareCsv, FoursquareUtil.getIdOfCategory(category), includeImagesF.isSelected());
                process(cities);

                HotwireUtil.cities = cities;
                ifAllStatedHotwire();
            }

        }).start();
        FoursquareUtil.cities = cities;
        ifAllStatedHotwire();
    }

    @FXML
    private void loadCitiesHotwire(ActionEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        //Show open file dialog
        File f = fileChooser.showOpenDialog(null);
        pathToCitiesHotwireCsv = f.getAbsolutePath();
        final int numberOfCities = FoursquareUtil.numberOfCities(pathToCitiesHotwireCsv);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final BufferedReader br;
                try {
                    progress.setDisable(false);
                    br = new BufferedReader(new FileReader(pathToCitiesHotwireCsv));

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
                                citiesListH.appendText(countryAndCity + "\n");
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
        ifAllStatedHotwire();
    }

    @FXML
    private void fileDialogueHotwire(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Output folder");
        File selectedDirectory = chooser.showDialog(null);
        pathToFileHotwireCsv = selectedDirectory.getAbsolutePath();
        outputPathH.setText(pathToFileHotwireCsv);
        HotwireUtil.init(pathToFileHotwireCsv, pathToCitiesHotwireCsv);
        ifAllStatedHotwire();

    }

    private List<String> loadFromTextArea() {
        String list = citiesListH.getText();
        String[] citiesArr = list.split("\n");
        cities = new ArrayList<String>(Arrays.asList(citiesArr));
        ifAllStatedHotwire();
        return cities;
    }

}
