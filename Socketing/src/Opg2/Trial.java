package Opg2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Trial {

    public static void main(String[] args) {
        try {
            // URL to send the GET request to
            String urlString = "https://cdn.useamp.com/api/conversion/merchant_store/targets/cart/spektakelstrik.myshopify.com";
            URL url = new URL(urlString);

            // Open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set up the request method and headers
            connection.setRequestMethod("GET");
            connection.setRequestProperty(":authority", "cdn.useamp.com");
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("accept-encoding", "gzip, deflate, br, zstd");
            connection.setRequestProperty("accept-language", "en-US,en;q=0.9");
            connection.setRequestProperty("if-none-match", "W/\"1a3a717b03cb057002e533030319973a\"");
            connection.setRequestProperty("origin", "https://spektakelstrik.dk");
            connection.setRequestProperty("priority", "u=1, i");
            connection.setRequestProperty("referer", "https://spektakelstrik.dk/");
            connection.setRequestProperty("sec-ch-ua", "\"Chromium\";v=\"128\", \"Not;A=Brand\";v=\"24\", \"Google Chrome\";v=\"128\"");
            connection.setRequestProperty("sec-ch-ua-mobile", "?0");
            connection.setRequestProperty("sec-ch-ua-platform", "\"Windows\"");
            connection.setRequestProperty("sec-fetch-dest", "empty");
            connection.setRequestProperty("sec-fetch-mode", "cors");
            connection.setRequestProperty("sec-fetch-site", "cross-site");
            connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");

            // Read the response
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Close connections
            in.close();
            connection.disconnect();

            // Print the response
            System.out.println("Response Content: " + content.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
