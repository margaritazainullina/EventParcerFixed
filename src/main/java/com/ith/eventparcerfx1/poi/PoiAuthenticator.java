package com.ith.eventparcerfx1.poi;

import static com.ith.eventparcerfx1.recreation.foursquare.FoursquareUtil.logger;
import com.ith.eventparcerfx1.spots.HotwireUtil;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * Created by margarita on 10.08.15.
 */
public class PoiAuthenticator extends Authenticator {

    final static Logger logger = Logger.getLogger(PoiAuthenticator.class);

    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    private final PasswordAuthentication authentication;

    public static void authenticate() {
        //authenticate for downloading        
        Properties properties = new Properties();
        InputStream input = ClassLoader.getSystemResourceAsStream("poi.authentication.properties");

        try {
            // load a properties file
            properties.load(input);
        } catch (IOException ex) {
            logger.error("Credentials load error");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error("Credentials load error");
                }
            }
        }

        logger.info("Authentication is successful!");
        Authenticator.setDefault(new PoiAuthenticator(properties));
       
    }

    public PoiAuthenticator(Properties properties) {
        String userName = properties.getProperty(USERNAME_KEY);
        String password = properties.getProperty(PASSWORD_KEY);
        if (userName == null || password == null) {
            authentication = null;
        } else {
            authentication = new PasswordAuthentication(userName, password.toCharArray());
        }
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return authentication;
    }
}
