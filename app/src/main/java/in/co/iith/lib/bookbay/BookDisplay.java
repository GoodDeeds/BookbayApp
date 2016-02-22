package in.co.iith.lib.bookbay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookDisplay extends AppCompatActivity {

    private ArrayAdapter<String> mDisplayBookListAdapter;

    String query_string = "";
    String message="";
    JSONArray bookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_display);
        Intent intent = getIntent();
        message = intent.getExtras().getString("Test message");

        FetchBooks fetchbooks = new FetchBooks();
        fetchbooks.execute(message);
        String emptyArray[]={};
        final List<String> listOfBooks = new ArrayList<String>(
                Arrays.asList(emptyArray));
        mDisplayBookListAdapter = new ArrayAdapter<String>(
                this,R.layout.book_list_text_view, R.id.book_list_text_view_text_view,listOfBooks);
        ListView listview = (ListView) this.findViewById(R.id.books_list_view);
        listview.setAdapter(mDisplayBookListAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String strid = bookList.getJSONObject(position).getString("id");
                   // Toast.makeText(getApplicationContext(), strid, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), BookDetails.class);
                    intent.putExtra("Sending id",strid);
                    if(intent.resolveActivity(getPackageManager())!=null)
                    {
                        startActivity(intent);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }
        });
    }

    public class FetchBooks extends AsyncTask<String, Void, String[]> {

        public String[] getBookDataFromJSON(String bookJSONObject) throws JSONException {
            bookList = new JSONArray(bookJSONObject);
            if(bookList.length()==0)
            {
                String[] resultString = new String[0];
                // resultString[0] = "No results found";
                query_string = "No books found for query \""+message+"\"";
                return resultString;
            }
            query_string = "Search result for query \""+message+"\"";
            String[] resultString = new String[bookList.length()];
            String bookName;
            String courseID;
            String bookAuthor;


            for(int i=0;i<bookList.length();i++) {
                JSONObject loadedData = bookList.getJSONObject(i);
                bookName = loadedData.getString("name");
                courseID = loadedData.getString("course");
                bookAuthor = loadedData.getString("author");
                resultString[i] = "Name: " + bookName + "\nCourse: " + courseID + "\nAuthor: " + bookAuthor;
              //  resultString[i] = loadedData.getString("name");
                Log.v("List of books",resultString[i]);

            }

            return resultString;
        }

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
// so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL = "https://lib.iith.co.in/searchq?";
                final String QUERY_PARAM = "q";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .build();
                URL url = new URL(builtUri.toString());


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e("Bookbay", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        return getBookDataFromJSON(forecastJsonStr);
                    } catch (JSONException e) {
                        Log.e("Bookbay", "Error closing stream", e);
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(String[] result) {
            if(result!=null){
                mDisplayBookListAdapter.clear();
                for (String dayForecastStr : result){
                    mDisplayBookListAdapter.add(dayForecastStr);

                }

            }
            TextView searchResult = (TextView) findViewById(R.id.search_query_textview);
            searchResult.setText(query_string);

        }

    }


    /*public void onClick(View v) {
        startActivity(new Intent(this, IndexActivity.class));
        finish();

    }*/

//    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
//        ImageView bmImage;
//
//        public DownloadImageTask(ImageView bmImage) {
//            this.bmImage = bmImage;
//        }
//
//        protected Bitmap doInBackground(String... urls) {
//            String urldisplay = urls[0];
//            Bitmap mIcon11 = null;
//            try {
//                InputStream in = new java.net.URL(urldisplay).openStream();
//                mIcon11 = BitmapFactory.decodeStream(in);
//            } catch (Exception e) {
//                Log.e("Error", e.getMessage());
//                e.printStackTrace();
//            }
//            return mIcon11;
//        }
//
//        protected void onPostExecute(Bitmap result) {
//            bmImage.setImageBitmap(result);
//        }
//    }

}
