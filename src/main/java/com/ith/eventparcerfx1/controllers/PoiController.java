/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ith.eventparcerfx1.controllers;

import com.ith.eventparcerfx1.poi.PoiUtil;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.apache.log4j.Logger;
import org.controlsfx.control.CheckComboBox;
import com.ith.eventparcerfx1.recreation.foursquare.FoursquareUtil;
import java.io.File;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author illya
 */
public class PoiController implements Initializable {

    @FXML
    private CheckBox includeImagesP;
    @FXML
    private TextArea logP;
    @FXML
    private TextField outputPathP;
    @FXML
    private Button startButtonP;
    @FXML
    private BorderPane searchPane;
    @FXML
    private ProgressIndicator progress;

    private static String pathToFilePoiCsv;
    private static List<String> selectedCategories = new ArrayList();
    final static Logger logger = Logger.getLogger(PoiController.class);

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        createPoiCategories();
      //  PoiUtil.setLogAppender(logP);
    }

    @FXML
    private void startPoi(ActionEvent event) throws IOException {
        PoiUtil.init(pathToFilePoiCsv);
        for (String category : selectedCategories) {
            PoiUtil.process(category);
        }
    }

    @FXML
    private void fileDialoguePoi(ActionEvent event) throws IOException {
        //Set extension filter
        FileChooser fileChooser = new FileChooser();
        //Show open file dialog
        File f = fileChooser.showOpenDialog(null);
        pathToFilePoiCsv = f.getAbsolutePath();
        outputPathP.setText(pathToFilePoiCsv);
    }

    private void createPoiCategories() {
        ObservableList<String> values = PoiUtil.getCategories();

        // Create the CheckComboBox with the data 
        final CheckComboBox<String> checkComboBox = new CheckComboBox<String>(values);
        searchPane.setRight(checkComboBox);
        FoursquareUtil.loadCredentials();

        checkComboBox.getCheckModel().getSelectedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                selectedCategories = checkComboBox.getCheckModel().getSelectedItems();
                ifAllStatedPoi();
            }
        });
    }
    private void ifAllStatedPoi() {
        if (selectedCategories.isEmpty()) {
            startButtonP.setDisable(true);
        }
        if (pathToFilePoiCsv == null) {
            startButtonP.setDisable(true);
        }
        startButtonP.setDisable(false);
    }
}
