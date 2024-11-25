package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class GoogleBooksService {

    // Changed the thread pool size to 5
    private static final ExecutorService executor = Executors.newFixedThreadPool(5); // Create a thread pool with 5 threads

    // Method to fetch books concurrently based on different queries
    public static ObservableList<Books> searchBooksConcurrently(String... queries) throws InterruptedException, ExecutionException {
        // A list to store Future objects that represent the results of each task
        List<Future<ObservableList<Books>>> futures = new ArrayList<>();

        // Submit tasks for each query
        for (String query : queries) {
            futures.add(executor.submit(() -> searchBooks(query)));
        }

        ObservableList<Books> allBooksList = FXCollections.observableArrayList();

        // Wait for all tasks to complete and merge the results
        for (Future<ObservableList<Books>> future : futures) {
            allBooksList.addAll(future.get()); // Collect the results
        }

        return allBooksList;
    }

    // Method to fetch books using a single query (updated with URL encoding fix)
    public static ObservableList<Books> searchBooks(String query) {
        ObservableList<Books> apiBooksList = FXCollections.observableArrayList();
        try {
            // Encode the query
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + encodedQuery;

            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
                apiBooksList = parseAPIResponse(response.toString());
            } else {
                throw new RuntimeException("API Error: Response code " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return apiBooksList;
    }

    // Method to parse the API response and create Books objects (unchanged)
    public static ObservableList<Books> parseAPIResponse(String jsonResponse) {
        ObservableList<Books> apiBooksList = FXCollections.observableArrayList();

        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray items = jsonObject.optJSONArray("items");

            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    JSONObject volumeInfo = item.optJSONObject("volumeInfo");

                    String title = volumeInfo.optString("title", "Unknown");
                    String authors = volumeInfo.has("authors") ?
                            volumeInfo.getJSONArray("authors").join(", ").replace("\"", "") : "Unknown";
                    String category = volumeInfo.has("categories") ?
                            volumeInfo.getJSONArray("categories").join(", ").replace("\"", "") : "Unknown";

                    String coverImageUrl = null;
                    if (volumeInfo.has("imageLinks")) {
                        JSONObject imageLinks = volumeInfo.getJSONObject("imageLinks");
                        // Prefer high-quality images if available
                        coverImageUrl = imageLinks.optString("medium", null); // Medium-resolution
                        if (coverImageUrl == null) {
                            coverImageUrl = imageLinks.optString("thumbnail", null); // Default resolution
                        }
                    }

                    apiBooksList.add(new Books(0, title, authors, category, 0, coverImageUrl));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return apiBooksList;
    }

    // Shutdown the executor when it's no longer needed
    public static void shutdownExecutor() {
        executor.shutdown();
    }
}
