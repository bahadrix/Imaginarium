package bahadir.me.imaginarium;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import me.bahadir.gimsearch.GIMSearch;


public class MainActivity extends ActionBarActivity {

    //Holds the founded images and their downloaded bitmap data
    private final Map<GIMSearch.Image, Bitmap> imageMap = new LinkedHashMap<>();


    private ProgressDialog progressDialog;

    //List of image views
    private List<ImageView> imageViews;

    private class ImageLoadTask2 extends AsyncTask<String, String, List<GIMSearch.Image>> {


        @Override
        protected List<GIMSearch.Image> doInBackground(String... params) {
            return null;
        }
    }

    // Image loading task. Runs on background and shows progress dialog.
    private class ImageLoadTask extends AsyncTask<String, String, List<GIMSearch.Image>> {

        @Override
        protected void onPreExecute() {
            //Show progress dialog
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Searching...");
            progressDialog.setMessage("Please wait.");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();

            //Clear image map
            imageMap.clear();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            progressDialog.setTitle(values[0]);
            progressDialog.setMessage(values[1]);
        }

        @Override
        protected List<GIMSearch.Image> doInBackground(String... params) {
            //Only one parameter. Which is search query.
            String search = params[0];

            //Google Search class.
            GIMSearch gimSearch = new GIMSearch();

            //Create image list
            List<GIMSearch.Image> images = null;
            try {
                images = gimSearch.getSinglePage(search);
            } catch (GIMSearch.GIMSearchParseException e) {
                e.printStackTrace();
            }

            // Statistics for progress update
            // Maximum image download count is bounded with min(founded images, image view count)
            int i = 0, totalDownloads = images.size() < imageViews.size() ? images.size() : imageViews.size();

            //Download images
            for (GIMSearch.Image img : images) {
                i++;
                if(i > totalDownloads)
                    break;

                //Update progress dialog with info.
                this.publishProgress("Loading image",
                        String.format("%d/%d", i, totalDownloads)
                );
                try {

                    //Download images
                    Log.i("gim", "Downloading image " + img.getThumbURL());
                    URL thumbURL = new URL(img.getThumbURL());
                    Bitmap bmp = BitmapFactory.decodeStream(thumbURL.openConnection().getInputStream());

                    //Put them to imageMap
                    imageMap.put(img, bmp);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            return images;
        }

        @Override
        protected void onPostExecute(List<GIMSearch.Image> images) {
            //Hide progress dialog
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            //Load images to corresponding views.
            loadFromMap();
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //This list holds imageViews
        imageViews = new ArrayList<>(4);
        imageViews.add( (ImageView) findViewById(R.id.imageView) );
        imageViews.add((ImageView) findViewById(R.id.imageView2));
        imageViews.add((ImageView) findViewById(R.id.imageView3));
        imageViews.add((ImageView) findViewById(R.id.imageView4));

        //The random button
        Button btnRandom = (Button) findViewById(R.id.btnRandom);

        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random rnd = new Random();

                //Create new task. Because each task can execute once.
                ImageLoadTask task = new ImageLoadTask();
                //Get random word
                String randomWord = Words.MOST_USED_NOUNS[rnd.nextInt(Words.MOST_USED_NOUNS.length)];
                //Run task in background for this word.
                task.execute(randomWord);
            }
        });

    }

    private void loadFromMap() {
        //Set bitmaps images to views.
        Iterator<Bitmap> it = imageMap.values().iterator();
        for (int i = 0; i < imageViews.size(); i++) {
            imageViews.get(i).setImageBitmap(it.next());
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
