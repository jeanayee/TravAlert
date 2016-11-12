package group2.travalert;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class CalcTime extends AppCompatActivity {

    private final static int MAX_HOURS = 23;
    private final static int MAX_MINUTES = 59;
    private final static int MINUTES_PER_HOUR = 60;
    private final String URL = "http://maps.googleapis.com/maps/api/directions/json?";
    SharedPreferences pref;
    String myApiKey;
    String estimatedTime;
    String distance;
    SharedPreferences.Editor editor;
    TextView estimatedTimeText;
    TextView distanceText;
    ImageView travelMethod;
    Drawable res;
    String packageName;
    Dialog dialog;
    int minutes, hours;
    View.OnClickListener onOK = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialog.dismiss();
        }
    };
    private Button chooseContacts;
    private NumberPicker hourspicker;
    private NumberPicker minspicker;
    private long time;
    static final int SELECT_CONTACT = 11;
    static final int MAX_CONTACT = 2;
    View.OnClickListener choose_contacts = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            time = ((hourspicker.getValue() * MINUTES_PER_HOUR) + minspicker.getValue()) * 60;
            SharedPreferences.Editor peditor = pref.edit();
            peditor.putLong("alertTime", time);
            peditor.commit();

            System.out.println(Long.toString(time));

            /*Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
            pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
            pickContactIntent.putExtra("max", MAX_CONTACT);
            startActivityForResult(pickContactIntent, SELECT_CONTACT);*/

            Intent intent = new Intent(CalcTime.this, Contacts.class);
            startActivity(intent);
        }
    };
    private InputStream is = null;
    private JSONObject jsonObject = null;
    private String json = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc_time);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        packageName=this.getPackageName();
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = pref.edit();
        dialog = new Dialog(this);

        chooseContacts = (Button) findViewById(R.id.chooseContactsButton);
        hourspicker = (NumberPicker) findViewById(R.id.hoursPicker);
        minspicker = (NumberPicker) findViewById(R.id.minutesPicker);

        chooseContacts.setOnClickListener(choose_contacts);
        hourspicker.setFocusable(false);
        minspicker.setFocusable(false);

        hourspicker.setMaxValue(MAX_HOURS);
        minspicker.setMaxValue(MAX_MINUTES);

        estimatedTimeText=(TextView) findViewById(R.id.estimatedTime);
        distanceText=(TextView) findViewById(R.id.miles);
        travelMethod= (ImageView)findViewById(R.id.travelMethodImg);

        if (pref.getString("Transportation Method", "").equals("driving")) {
            res = ResourcesCompat.getDrawable(getResources(), R.drawable.driving, null);
        }

        if (pref.getString("Transportation Method", "").equals("walking")) {
            res = ResourcesCompat.getDrawable(getResources(), R.drawable.walking, null);
        }

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        MyAsyncTask getDestinationTime = new MyAsyncTask();
        getDestinationTime.execute();

        //set back button
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        setTitle("Calculate Time");

//
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }


    private String getResponseText(String stringUrl) throws IOException {
        StringBuilder response = new StringBuilder();

        URL url = new URL(stringUrl);
        HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
        if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()), 8192);
            String strLine = null;
            while ((strLine = input.readLine()) != null) {
                response.append(strLine);
            }
            input.close();
        }
        return response.toString();
    }


    class MyAsyncTask extends AsyncTask<String, String, Void> {

        InputStream inputStream = null;
        String result = "";
        private ProgressDialog progressDialog = new ProgressDialog(CalcTime.this);

        protected void onPreExecute() {
            progressDialog.setMessage("Downloading your data...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    MyAsyncTask.this.cancel(true);
                }
            });
        }

        @Override
        protected Void doInBackground(String... params) {
            URL url=null;
            System.out.println("entered do in background");
            StringBuilder response = new StringBuilder();
            try {
                ApplicationInfo ai = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                Bundle bundle = ai.metaData;
                myApiKey = bundle.getString("com.google.android.geo.API_KEY");
                System.out.println("entered got api key");
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("NameNotFoundException", e.toString());
                e.printStackTrace();
            }


            /*String uri =
                "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" +
                        getDouble(pref, "Current Location Longitude", 1) + "," +
                        getDouble(pref, "Current Location Latitude", 1) + "&destinations=" +
                        getDouble(pref, "Destination Longitude", 1) + "," +
                        getDouble(pref, "Destination Latitude", 1) + "&key=" + "AIzaSyAErQqEhEIMZwSyoGNK16CwwZq01L1rOZ8";*/

            System.out.println("current loc longitude" + getDouble(pref, "Current Location Longitude", 1));
            System.out.println("current loc latitude" + getDouble(pref, "Current Location Latitude", 1));
            System.out.println("destination longitude" + getDouble(pref, "Destination Longitude", 1) );
            System.out.println("destination latitude" + getDouble(pref, "Destination Latitude", 1) );
           String uri = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" +
                   getDouble(pref, "Current Location Latitude", 1) + "," +
                   getDouble(pref, "Current Location Longitude", 1) +
                   "&destinations=" + getDouble(pref, "Destination Latitude", 1)
                   + "," + getDouble(pref, "Destination Longitude", 1)
                   + "&mode=" + pref.getString("Transportation Method", "driving")
                   + "&key=AIzaSyDjod1npyi1nkZMu8NluLiyXBO7BHdajtI";
            System.out.println("the uri is " + uri);


            try {
                System.out.println("entered forming url");
                url = new
                        URL(uri);
            }
            catch(java.net.MalformedURLException e) {
                Log.e("MalformedURLException", e.toString());
                e.printStackTrace();
            }
            try {
                System.out.println("opening connection");
                HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()), 8192);
                    String strLine = null;
                    while ((strLine = input.readLine()) != null) {
                        response.append(strLine);
                    }
                    input.close();
                }
                System.out.println("read in result");
                result=response.toString();
            }
            catch (java.io.IOException e) {
                Log.e("IOException", e.toString());
                e.printStackTrace();
            }

            return null;
        } // protected Void doInBackground(String... params)

        protected void onPostExecute(Void v) {
            //parse JSON data
            try {
                JSONObject jObject = new JSONObject(result);
                System.out.println(jObject);
                //jObject.getJSONArray("rows");
                estimatedTime=jObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration").getString("text");
                distance=jObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("distance").getString("text");
                System.out.println(distance);
                System.out.println("est = " + estimatedTime);
                estimatedTimeText.setText(estimatedTime);
                distanceText.setText(distance);
                editor.putString("distance", distance);
                editor.commit();
                editor.putString("eta", estimatedTime);
                editor.commit();
                travelMethod.setImageDrawable(res);
                travelMethod.getLayoutParams().height = 50;
                travelMethod.getLayoutParams().width = 50;


                //just mins (i.e 3 min)
                if (!estimatedTime.contains("hours") && !estimatedTime.contains("days") &&  !estimatedTime.contains("hour") && !estimatedTime.contains("day")) {
                    String[] temp = estimatedTime.split("mins");

                    if (temp.length<=1) {
                        temp = estimatedTime.split("min");
                    }

                    minutes=Integer.parseInt(temp[0].replace(" ", ""));
                    System.out.println(temp[0]);
                    hourspicker.setValue(0);
                    minspicker.setValue(minutes);
                }


                //contains days (i.e. 3 days 2 hours)
                else if (estimatedTime.contains("days") || estimatedTime.contains("day")) {
                    String[] temp = estimatedTime.split("hours");

                    if (temp.length<=1) {
                        temp = estimatedTime.split("hour");
                    }

                    String[] temp2 = temp[0].split("days");

                    if (temp2.length<=1) {
                        temp2 = temp[0].split("day");
                    }

                    minutes=Integer.parseInt(temp2[1].replace(" ", ""));
                    hours=Integer.parseInt(temp2[0].replace(" ", ""));
                    System.out.println("minutes " + temp2[1]);
                    System.out.println("hours " + temp2[0]);
                }

                //contains hours and minutes (i.e 2 hours 5 mins)
                else {
                    String[] temp = estimatedTime.split("mins");

                    if (temp.length<=1) {
                        temp = estimatedTime.split("min");
                    }

                    String[] temp2 = temp[0].split("hours");

                    if (temp2.length<=1) {
                        temp2 = temp[0].split("hour");
                    }

                    for (int i = 0; i< temp2.length; i++) {
                        System.out.println("est temp2 at " + i + " = " + temp2[i]);
                    }

                    minutes=Integer.parseInt(temp2[1].replace(" ", ""));
                    hours=Integer.parseInt(temp2[0].replace(" ", ""));
                    System.out.println("minutes " + temp2[1]);
                    System.out.println("hours " + temp2[0]);
                }
                hourspicker.setValue(hours);
                minspicker.setValue(minutes);

                this.progressDialog.dismiss();
            } catch (JSONException e) {
                System.out.println("exception here");
                Log.e("JSONException", "Error: " + e.toString());
                this.progressDialog.dismiss();
                estimatedTimeText.setText("0 min");
                distanceText.setText("0 mi");


                dialog.setContentView(R.layout.no_estimated_time);
                dialog.setTitle("Error");

                Button btnok = (Button) dialog.findViewById(R.id.ok_json);
                btnok.setOnClickListener(onOK);
                Drawable d = new ColorDrawable(Color.BLACK);
                d.setAlpha(130);
                dialog.getWindow().setBackgroundDrawable(d);
                dialog.show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if (requestCode == SELECT_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                //Bundle bundle = data.getExtras();
               // String result= bundle.getString("result");
                //ArrayList<String> contacts = bundle.getStringArrayList("result");

                Uri contactUri = data.getData();
                setRecipient(contactUri);//, contacts);
            }
        }
    }
    public String getData(String contact, int which)
    {
        return contact.split(";")[which];
    }
    private void setRecipient(Uri contactUri){//, ArrayList<String> names) {

        // grab number and name
        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Contacts.DISPLAY_NAME};
        Cursor cursor = getContentResolver()
                .query(contactUri, projection, null, null, null);
        cursor.moveToFirst();
        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        //String namez = "";
       // for (int i = 0;i<names.size();i++){
        //    namez += names.get(i);
       // }
        Toast toast = Toast.makeText(this, name, Toast.LENGTH_SHORT);
        toast.show();
    }
}



