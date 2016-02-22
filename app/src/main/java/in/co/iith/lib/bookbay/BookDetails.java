package in.co.iith.lib.bookbay;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class BookDetails extends AppCompatActivity {

    String bookId;
    JSONArray bookList;
    String bookName;
    String bookurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        Intent intent = getIntent();
        bookId = intent.getExtras().getString("Sending id");
        FetchBookDetails fetchbooks = new FetchBookDetails();
        fetchbooks.execute(bookId);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public class FetchBookDetails extends AsyncTask<String, Void, String[]> {

        public String[] getBookDataFromJSON(String bookJSONObject) throws JSONException {
            bookList = new JSONArray(bookJSONObject);
//            if(bookList.length()==0)
//            {
//                String[] resultString = new String[0];
//                // resultString[0] = "No results found";
//                query_string = "No books found for query \""+message+"\"";
//                return resultString;
//            }
//            query_string = "Search result for query \""+message+"\"";
            JSONObject loadedData = bookList.getJSONObject(0);
            String[] resultString = new String[6];

            String courseID;
            String bookAuthor;

            String description;
            resultString[1] = bookName = loadedData.getString("name");
            resultString[3] = courseID = loadedData.getString("course");
            resultString[2] = bookAuthor = loadedData.getString("author");
            resultString[0] = bookId = loadedData.getString("id");
            resultString[4] = description = loadedData.getString("description");
            resultString[5] = bookurl = loadedData.getString("url");

            //  resultString[i] = loadedData.getString("name");




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
                final String FORECAST_BASE_URL = "https://lib.iith.co.in/api/book?";
                final String QUERY_PARAM = "id";

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
                TextView bookName = (TextView) findViewById(R.id.book_name_view);
                TextView bookAuthor = (TextView) findViewById(R.id.author_name_view);
                TextView bookDescription = (TextView) findViewById(R.id.book_description);
                bookName.setText(result[1]);
                bookAuthor.setText(result[2]);
                bookDescription.setText(result[4]);


            }

        }

    }
    public void download(View view) {

        String url = bookurl; //"https://lib.iith.co.in/u/api/book/download/" + bookId + "/" + bookName + ".pdf";

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Downloading...");
        request.setTitle(bookName);
// in order for this if to run, you must use the android 3.2 to compile your app
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, bookName + ".pdf");

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }
//    public void download()
//    {
////        Intent intent = new Intent(getApplicationContext(),DownloadBook.class);
////        intent.putExtra("Download",bookId);
////        if(intent.resolveActivity(getPackageManager())!=null)
////        {
////            startActivity(intent);
////        }
//        DownloadBook downloadBook = new DownloadBook();
//        DownloadBook.execute();
//    }
//
//    public class DownloadBook extends AsyncTask<String, Void, String> {
//        String bookID;
//        String bookName;
//
//        public DownloadBook()
//        {
//
//
//        }
//        public String[] getBookDataFromJSON(String bookJSONObject) throws JSONException {
//
////            if(bookList.length()==0)
////            {
////                String[] resultString = new String[0];
////                // resultString[0] = "No results found";
////                query_string = "No books found for query \""+message+"\"";
////                return resultString;
////            }
////            query_string = "Search result for query \""+message+"\"";
//            JSONObject loadedData = bookList.getJSONObject(0);
//            String[] resultString = new String[5];
//
//            String courseID;
//            String bookAuthor;
//
//            String description;
//            resultString[1] = bookName = loadedData.getString("name");
//            resultString[3] = courseID = loadedData.getString("course");
//            resultString[2] = bookAuthor = loadedData.getString("author");
//            resultString[0] = bookID = loadedData.getString("id");
//            resultString[4] = description = loadedData.getString("description");
//
//            //  resultString[i] = loadedData.getString("name");
//
//
//
//
//            return resultString;
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            // These two need to be declared outside the try/catch
//// so that they can be closed in the finally block.
//            JSONObject loadedData = null;
//            try {
//                loadedData = bookList.getJSONObject(0);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            String[] resultString = new String[5];
//
//            String courseID;
//            String bookAuthor;
//
//            String description;
//            try {
//                resultString[1] = bookName = loadedData.getString("name");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            try {
//                resultString[3] = courseID = loadedData.getString("course");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            try {
//                resultString[2] = bookAuthor = loadedData.getString("author");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            try {
//                resultString[0] = bookID = loadedData.getString("id");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            try {
//                resultString[4] = description = loadedData.getString("description");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            // Will contain the raw JSON response as a string.
//            String forecastJsonStr = null;
//
//            try {
//                // Construct the URL for the OpenWeatherMap query
//                // Possible parameters are available at OWM's forecast API page, at
//                // http://openweathermap.org/API#forecast
//                final String FORECAST_BASE_URL = "https://lib.iith.co.in/api/book?";
//                final String QUERY_PARAM = "id";
//
//                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
//                        .appendQueryParameter(QUERY_PARAM, params[0])
//                        .build();
//                URL url = new URL("https://lib.iith.co.in/u/book/download/"+bookId+"/"+bookName+".pdf");
//
//
//                // Create the request to OpenWeatherMap, and open the connection
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                // Read the input stream into a String
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                if (inputStream == null) {
//                    // Nothing to do.
//                    forecastJsonStr = null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                    // But it does make debugging a *lot* easier if you print out the completed
//                    // buffer for debugging.
//                    buffer.append(line + "\n");
//                }
//
//                if (buffer.length() == 0) {
//                    // Stream was empty.  No point in parsing.
//                    return null;
//                }
//                forecastJsonStr = buffer.toString();
//                FileOutputStream output = new FileOutputStream("/sdcard/"+bookName+".pdf");
//
//            } catch (IOException e) {
//                Log.e("Bookbay", "Error ", e);
//                // If the code didn't successfully get the weather data, there's no point in attempting
//                // to parse it.
//                return null;
//            } finally {
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    return "Done";
//                }
//            }
//            return null;
//        }
//        @Override
//        protected void onPostExecute(String result) {
//            if(result!=null){
//                Toast.makeText(getApplicationContext(),"File Downloaded",Toast.LENGTH_SHORT);
//
//
//
//            }
//            else
//            {
//                Toast.makeText(getApplicationContext(),"Error downloading file",Toast.LENGTH_SHORT);
//            }
//
//        }
//
//    }


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
