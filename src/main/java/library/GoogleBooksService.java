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

public class GoogleBooksService {

    public static ObservableList<Books> searchBooks(String query) {
        ObservableList<Books> apiBooksList = FXCollections.observableArrayList();
        String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + query;

        try {
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
                        coverImageUrl = volumeInfo.getJSONObject("imageLinks").optString("thumbnail", null);
                    }

                    apiBooksList.add(new Books(0, title, authors, category, 0, coverImageUrl));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return apiBooksList;
    }
}
