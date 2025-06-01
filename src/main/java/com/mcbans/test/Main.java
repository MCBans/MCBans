package com.mcbans.test;

import com.mcbans.client.Client;
import com.mcbans.client.BadApiKeyException;
import com.mcbans.plugin.MCBans;
import com.mcbans.utils.TooLargeException;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        LOGGER.info("Starting MCBans API connection test");

        // Set encryption to false as we're not in a plugin context
        MCBans.encryptAPI = false;
        LOGGER.info("Encryption set to: " + MCBans.encryptAPI);

        // API key should be provided as a command line argument
        String apiKey = args.length > 0 ? args[0] : "test_api_key";
        LOGGER.info("Using API key: " + apiKey);

        LOGGER.info("Process: Attempting to connect to MCBans API");

        try {
            // Create client and connect to the API
            LOGGER.info("Step 1: Creating client and establishing connection");
            try {
                Client client = new Client(apiKey);
                LOGGER.info("Step 1: Connection established successfully");

                // Verify the connection
                LOGGER.info("Step 2: Verifying connection");
                try {
                    client.verifyConnection();
                    LOGGER.info("Step 2: Connection verified successfully");

                    // Disconnect from the API
                    LOGGER.info("Step 3: Disconnecting from the API");
                    try {
                        client.close();
                        LOGGER.info("Step 3: Disconnected from the API successfully");

                        // Print success message
                        LOGGER.info("RESULT: SUCCESS - All steps completed successfully");
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "FAILED at Step 3: Error disconnecting from the API - " + e.getMessage(), e);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "FAILED at Step 2: Error verifying connection - " + e.getMessage(), e);
                } catch (TooLargeException e) {
                    LOGGER.log(Level.SEVERE, "FAILED at Step 2: Data too large during verification - " + e.getMessage(), e);
                }
            } catch (BadApiKeyException e) {
                LOGGER.log(Level.SEVERE, "FAILED at Step 1: Invalid API key - " + e.getMessage(), e);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "FAILED at Step 1: IO exception during connection - " + e.getMessage(), e);
            } catch (TooLargeException e) {
                LOGGER.log(Level.SEVERE, "FAILED at Step 1: Data too large during connection - " + e.getMessage(), e);
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
                LOGGER.log(Level.SEVERE, "FAILED at Step 1: Encryption issue during connection - " + e.getMessage(), e);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "FAILED: Unexpected exception - " + e.getMessage(), e);
        }
    }
}
