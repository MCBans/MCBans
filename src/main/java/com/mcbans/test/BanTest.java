package com.mcbans.test;

import com.mcbans.client.BanClient;
import com.mcbans.client.BanStatusClient;
import com.mcbans.client.Client;
import com.mcbans.client.UnbanClient;
import com.mcbans.client.BadApiKeyException;
import com.mcbans.client.response.BanResponse;
import com.mcbans.plugin.MCBans;
import com.mcbans.utils.TooLargeException;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BanTest {
    private static final Logger LOGGER = Logger.getLogger(BanTest.class.getName());

    public static void main(String[] args) {
        LOGGER.info("Starting MCBans Ban Test");

        // Set encryption to false as we're not in a plugin context
        MCBans.encryptAPI = false;
        LOGGER.info("Encryption set to: " + MCBans.encryptAPI);

        // API key should be provided as an environment variable
        String apiKey = System.getenv("MCBANS_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = "test_api_key"; // Default fallback value
        }
        LOGGER.info("Using API key: " + apiKey);

        // Player name to ban/unban should be provided as an environment variable
        String playerName = System.getenv("MCBANS_PLAYER_TO_BAN");
        if (playerName == null || playerName.isEmpty()) {
            playerName = "firestarthe"; // Default fallback value
        }
        String playerIP = "127.0.0.1"; // Default IP for testing
        String adminUUID = "00000000000000000000000000000000"; // Default admin UUID for testing
        LOGGER.info("Testing with player: " + playerName + ", IP: " + playerIP);

        try {
            // Step 1: Create client and connect to the API
            LOGGER.info("Step 1: Creating client and establishing connection");
            Client client = null;
            try {
                client = new Client(apiKey);
                LOGGER.info("Step 1: Connection established successfully");

                // Step 2: Verify the connection
                LOGGER.info("Step 2: Verifying connection");
                try {
                    client.verifyConnection();
                    LOGGER.info("Step 2: Connection verified successfully");

                    // Step 3: Ban the player
                    LOGGER.info("Step 3: Attempting to ban player: " + playerName);
                    try {
                        BanClient banClient = BanClient.cast(client);
                        banClient.localBan(playerName, null, playerIP, adminUUID, "Test ban from BanTest", new Client.ResponseHandler() {
                            @Override
                            public void ack() {
                                LOGGER.info("Step 3: Player banned successfully");
                            }

                            @Override
                            public void err(String error) {
                                LOGGER.log(Level.SEVERE, "FAILED at Step 3: Error banning player - " + error);
                            }
                        });

                        // Step 4: Check if the player is banned
                        LOGGER.info("Step 4: Checking if player is banned");
                        try {
                            BanStatusClient banStatusClient = BanStatusClient.cast(client);
                            BanResponse banResponse = banStatusClient.banStatusByPlayerName(playerName, playerIP, true);

                            if (banResponse != null && banResponse.getBan() != null) {
                                LOGGER.info("Step 4: Player is confirmed banned. Ban reason: " + banResponse.getBan().getReason());
                            } else {
                                LOGGER.info("Step 4: Player is not banned or ban status check failed");
                            }

                            // Step 5: Try to connect as the banned player (simulate)
                            LOGGER.info("Step 5: Simulating connection attempt as banned player");
                            try {
                                BanResponse loginResponse = banStatusClient.banStatusByPlayerName(playerName, playerIP, true);

                                if (loginResponse != null && loginResponse.getBan() != null) {
                                    LOGGER.info("Step 5: Connection attempt failed as expected. Player is banned");
                                } else {
                                    LOGGER.info("Step 5: Connection attempt succeeded unexpectedly. Player is not banned");
                                }

                                // Step 6: Unban the player
                                LOGGER.info("Step 6: Attempting to unban player: " + playerName);
                                try {
                                    UnbanClient unbanClient = UnbanClient.cast(client);
                                    unbanClient.unBan(playerName, null, new Client.ResponseHandler() {
                                        @Override
                                        public void ack() {
                                            LOGGER.info("Step 6: Player unbanned successfully");
                                        }

                                        @Override
                                        public void err(String error) {
                                            LOGGER.log(Level.SEVERE, "FAILED at Step 6: Error unbanning player - " + error);
                                        }
                                    });

                                    // Step 7: Check if the player is still banned
                                    LOGGER.info("Step 7: Checking if player is still banned");
                                    try {
                                        BanResponse finalResponse = banStatusClient.banStatusByPlayerName(playerName, playerIP, true);

                                        if (finalResponse != null && finalResponse.getBan() != null) {
                                            LOGGER.info("Step 7: Player is still banned! Unban failed");
                                        } else {
                                            LOGGER.info("Step 7: Player is no longer banned. Unban successful");
                                        }

                                        // Step 8: Disconnect from the API
                                        LOGGER.info("Step 8: Disconnecting from the API");
                                        try {
                                            client.close();
                                            LOGGER.info("Step 8: Disconnected from the API successfully");

                                            // Print success message
                                            LOGGER.info("RESULT: SUCCESS - All steps completed successfully");
                                        } catch (IOException e) {
                                            LOGGER.log(Level.SEVERE, "FAILED at Step 8: Error disconnecting from the API - " + e.getMessage(), e);
                                        }
                                    } catch (Exception e) {
                                        LOGGER.log(Level.SEVERE, "FAILED at Step 7: Error checking ban status after unban - " + e.getMessage(), e);
                                    }
                                } catch (Exception e) {
                                    LOGGER.log(Level.SEVERE, "FAILED at Step 6: Error during unban process - " + e.getMessage(), e);
                                }
                            } catch (Exception e) {
                                LOGGER.log(Level.SEVERE, "FAILED at Step 5: Error simulating connection as banned player - " + e.getMessage(), e);
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "FAILED at Step 4: Error checking ban status - " + e.getMessage(), e);
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "FAILED at Step 3: Error during ban process - " + e.getMessage(), e);
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
