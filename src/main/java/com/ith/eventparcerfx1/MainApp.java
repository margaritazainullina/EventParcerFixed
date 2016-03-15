package com.ith.eventparcerfx1;

import com.ith.eventparcerfx1.recreation.events.EventListerUtil;
import com.ith.eventparcerfx1.spots.GoogleMapsUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainApp extends Application {

    public static HashMap<String, String> categories = new HashMap();
    public static int quotaUsed = 0;
    public static int totalQuota = getTotalQuota();

    @Override
    public void start(final Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Main.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        stage.setTitle("Event parsing");
        stage.setMinHeight(700);
        stage.setMinWidth(700);
        stage.setScene(scene);
        stage.show();

        quotaUsed = getQuotaUsedForLast24h();
        int num = (int) Math.log10(totalQuota - quotaUsed) + 1;
        int approx = totalQuota - quotaUsed;
        if (num == 7) {
            approx = approx - approx % 1000000;
        }
        if (num == 6) {
            approx = approx - approx % 100000;
        }
        if (num == 5) {
            approx = approx - approx % 10000;
        }
        if (num == 4) {
            approx = approx - approx % 1000;
        }
        if (num == 3) {
            approx = approx - approx % 100;
        }
        if (num == 2) {
            approx = approx - approx % 10;
        }
        if (num == 1) {
            approx = 0;
        }
        if ((approx < 0)||(approx>700000)) {
            approx = 0;
        }

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Google API usage");
        String s = "Approximately " + approx + " requests for next 24 h are left";
        alert.setContentText(s);
        alert.showAndWait();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent t) {
                t.consume();
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Exit");
                String s = "Do you really want to exit?";
                alert.setContentText(s);

                Optional<ButtonType> result = alert.showAndWait();

                if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
                try {
                    saveQuotaInfo();
                    saveProxyInfo();
                } catch (IOException ex) {
                    Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
                }
                    t.consume();
                    Platform.exit();
                    System.exit(0);
                } else {
                    alert.close();
                }
            }
        });
    }

    private void saveProxyInfo() throws IOException {
        File file = new File("proxyInfo.csv");
        if (!file.exists()) {
            file.createNewFile();
        }
       
        FileWriter f = new FileWriter("proxyInfo.csv");
        try {
            f.write(EventListerUtil.proxiesStr);
        } catch (Exception e) {
        } finally {
            f.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static int getTotalQuota() {
        return GoogleMapsUtil.keys.length * 2500;
    }

    private static int getQuotaUsedForLast24h() throws IOException {
        int i = 0;
        File file = new File("quotaInfo.csv");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileInputStream fstream = new FileInputStream("quotaInfo.csv");
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String strLine;
        while ((strLine = br.readLine()) != null) {
            String[] s = strLine.split(",");
            LocalDateTime d = LocalDateTime.parse(s[0]);
            long minutes = ChronoUnit.MINUTES.between(d, LocalDateTime.now());
            if (minutes < (60 * 24)) {
                i += Integer.parseInt(s[1]);
            }
        }
        br.close();
        return i;
    }

    private void saveQuotaInfo() throws IOException {
        File file = new File("quotaInfo.csv");
        if (!file.exists()) {
            file.createNewFile();
        }
        String s = LocalDateTime.now() + "," + quotaUsed + "\n";
        Files.write(Paths.get("quotaInfo.csv"), s.getBytes(), StandardOpenOption.APPEND);
    }
}
