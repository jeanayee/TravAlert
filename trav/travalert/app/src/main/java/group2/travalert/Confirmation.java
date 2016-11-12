package group2.travalert;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class Confirmation extends AppCompatActivity {
    SharedPreferences sp;
    ArrayList<LatLng> markerPoints;
    TextView dest;
    TextView alertTime;
    TextView contacts;
    TextView transportationMethod;
    Dialog dialog;
    Dialog contactsDialog;
    Dialog editTimeDialog;

    TimePickerDialog mTimePicker;
    String time;
    Button editAlertTimeButton, editTransportationMethodButton;
    RadioButton driving, walking;
    Button setTimerButton;
    Intent thisIntent;
    ArrayList<String> contactNames;
    ArrayList<String> contactNumbers;
    CDialog d;
    Dialog editDestinationDialog;
    EditText editDestinationText;
    Geocoder geocoder;
    Marker destinationMarker;
    Marker currentLocationMarker;
    LatLng currentLocation;
    Polyline polyline;
    View.OnClickListener editTransportaitonMethod = new View.OnClickListener() {
        public void onClick(View v) {

            if (sp.getString("Transportation Method", "").equals("Walking")) {
                walking.setChecked(true);
                driving.setChecked(false);
                Log.d("entered here", "1");
            }
            if (sp.getString("Transportation Method", "").equals("Driving")) {
                driving.setChecked(true);
                walking.setChecked(false);
                Log.d("entered here", "2");
            }
            dialog.show();
        }
    };
    View.OnClickListener editDestination = new View.OnClickListener() {
        public void onClick(View v) {
            editDestinationDialog.show();
        }

    };
    View.OnClickListener editTransportaitonMethodOK = new View.OnClickListener() {
        public void onClick(View v) {
            dialog.dismiss();
            SharedPreferences.Editor editor = sp.edit();

            if (walking.isChecked()) {
                editor.putString("Transportation Method", "Walking");
                editor.commit();
            }
            if (driving.isChecked()) {
                editor.putString("Transportation Method", "Driving");
                editor.commit();

            }
            Log.d("sp set to", sp.getString("Transportation Method", ""));
            transportationMethod.setText(sp.getString("Transportation Method", ""));
        }
    };
    View.OnClickListener onSetTimer = new View.OnClickListener() {
        public void onClick(View v) {
            Intent i=new Intent(Confirmation.this, TimerActivity.class);
            startActivity(i);
            //does nothing currently, eventually will lead to final screen
            //all updated info is stored in those field's onclick numbers in SharedPreferences Properly
        }
    };
    View.OnClickListener editContacts = new View.OnClickListener() {
        public void onClick(View v) {
            //contactsDialog.show();
            d.show();
        }
    };
    View.OnClickListener radioListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean checked = ((RadioButton) v).isChecked();
            switch(v.getId()) {
                case R.id.drivingEdit:
                    if (checked)
                        checked=false;
                    walking.setChecked(false);
                    Log.d("etnered walking", "button");
                    break;
                case R.id.walkingEdit:
                    if (checked)
                        checked=false;
                    driving.setChecked(false);
                    Log.d("etnered driving", "button");
                    break;
            }
        }
    };
    View.OnClickListener onEditDestinationText = new View.OnClickListener() {
        public void onClick(View v) {
            editDestinationText.setText("");
        }
        };
    private NumberPicker hourspicker;
    private NumberPicker minspicker;
    View.OnClickListener editAlertTime = new View.OnClickListener() {
        public void onClick(View v) {
            long time=sp.getLong("alertTime", 0);
            hourspicker.setValue((int) (time/60)/60);
            minspicker.setValue((int) (time/60)%60);
            editTimeDialog.show();
        }
    };
    View.OnClickListener onEditAlertTimeOk = new View.OnClickListener() {
        public void onClick(View v) {
            long time = (hourspicker.getValue() * 60 + minspicker.getValue()) * 60;
            SharedPreferences.Editor peditor = sp.edit();
            peditor.putLong("alertTime", time);
            peditor.commit();
            alertTime.setText(timeFormatter(minspicker.getValue(), hourspicker.getValue()));

            editTimeDialog.dismiss();
        }
    };
    private GoogleMap map;
    View.OnClickListener editDestinationMethodOK = new View.OnClickListener() {
        public void onClick(View v) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Destination Address", editDestinationText.getText().toString());
            editor.commit();
            List<Address> addressList = null;
            String destination=editDestinationText.getText().toString();


            if (destination != null || destination.equals("")) {
                try {
                    addressList = geocoder.getFromLocationName(destination, 1);
                    System.out.println(geocoder.getFromLocationName(destination, 1));
                    System.out.println(addressList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Address address = addressList.get(0);
                double destinationlatitutde = address.getLatitude();
                double destinationlongitude = address.getLongitude();

                putDouble(editor, "Destination Latitude", destinationlatitutde);
                editor.commit();
                putDouble(editor, "Destination Longitude", destinationlongitude);
                editor.commit();

                LatLng newDest = new LatLng(destinationlatitutde, destinationlongitude);

                destinationMarker.setPosition(newDest);
                editDestinationDialog.dismiss();

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        newDest, 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(newDest)     // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                dest.setText(destination);

                polyline.remove();

                String url = getDirectionsUrl(currentLocation, newDest);

                DownloadTask downloadTask = new DownloadTask();

                downloadTask.execute(url);

            }
        }
    };
    View.OnClickListener onZoomIn = new View.OnClickListener() {
        public void onClick(View v) {
                    map.animateCamera(CameraUpdateFactory.zoomIn());
                    System.out.print("zoomint in");
                }
    };
    View.OnClickListener onZoomOut = new View.OnClickListener() {
        public void onClick(View v) {
            map.animateCamera(CameraUpdateFactory.zoomOut());
            System.out.print("zooming out");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        dialog = new Dialog(this);
        editDestinationDialog = new Dialog(this);
        dialog.setContentView(R.layout.edit_travel_method);
        dialog.setTitle("Edit Transportation Method");
        //d.setAlpha(130);

        editDestinationDialog.setContentView(R.layout.edit_destination);
        editDestinationDialog.setTitle("Edit Destination");

        d = new CDialog(this, "Confirmation");
        //d.setAlpha(130);

        geocoder = new Geocoder(this);

        thisIntent=getIntent();

        setTitle("Confirmation");


        ListView listView = new ListView(this);
        String names = sp.getString("chosenContacts", "");
        String numbers = sp.getString("contactNums", "");
        contactNames = new ArrayList<String>(Arrays.asList(names.split(",")));
        contactNumbers = new ArrayList<String>(Arrays.asList(numbers.split(",")));
        //ArrayList<String> lst = new Array
        final ArrayAdapter<String> a = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, contactNames);
        listView.setAdapter(a);
        //listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
        //      android.R.id.text1, contactNames));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String name = ((TextView) view).getText().toString();
                final int pos = position;
                if (contactNames.size() == 1 ){
                    Toast.makeText(Confirmation.this, "Can't remove! Need to have at least" +
                            " one contact!", Toast.LENGTH_SHORT).show();
                }else {
                    new AlertDialog.Builder(Confirmation.this)
                            .setTitle("Removing " + name)
                            .setMessage("Are you sure you want to remove " + name + " from your " +
                                    "notified contacts?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Toast.makeText(Confirmation.this, name + " removed", Toast.LENGTH_SHORT).show();
                                    contactNames.remove(pos);
                                    contactNumbers.remove(pos);
                                    String newNames = "";//contactNames.get(0);
                                    String newNums = "";//contactNumbers.get(0);
                                    //Reconstructing Shared Preferences
                                    for (int i = 0; i < contactNames.size(); i++) {
                                        newNames += contactNames.get(i) + ",";
                                        newNums += contactNumbers.get(i) + ",";
                                    }
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putString("chosenContacts", newNames);
                                    editor.putString("contactNums", newNums);
                                    editor.commit();
                                    //Updating Confirmation View
                                    contacts = (TextView) findViewById(R.id.contacts);
                                    String firstName = contactNames.get(0);
                                    if (contactNames.size() > 1) {
                                        firstName += " (+" + (contactNames.size() - 1) + " more)";
                                    }
                                    contacts.setText(firstName);
                                    a.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                }
            }
        });
        contactsDialog = new Dialog(this);
        contactsDialog.setContentView(listView);
        contactsDialog.setTitle("Edit Contacts");

        editTimeDialog=new Dialog(this);
        editTimeDialog.setContentView(R.layout.edit_time);
        editTimeDialog.setTitle("Edit Alert Time");
        Button editAlertTimeOk= (Button) editTimeDialog.findViewById(R.id.editAlertTimeOk);

        hourspicker = (NumberPicker) editTimeDialog.findViewById(R.id.hoursPicker);
        minspicker = (NumberPicker) editTimeDialog.findViewById(R.id.minutesPicker);

        hourspicker.setFocusable(false);
        minspicker.setFocusable(false);

        hourspicker.setMaxValue(23);
        minspicker.setMaxValue(59);

        editAlertTimeOk.setOnClickListener(onEditAlertTimeOk);

        Button zoomIn = (Button) findViewById(R.id.Bzoomi);
        Button zoomOut = (Button) findViewById(R.id.Bzoomo);
        zoomIn.setOnClickListener(onZoomIn);
        zoomOut.setOnClickListener(onZoomOut);


        // Initializing array List
        markerPoints = new ArrayList<LatLng>();

        // Getting reference to SupportMapFragment of the activity_main
        SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);

        // Getting Map for the SupportMapFragment
        map = fm.getMap();

        //map.setMyLocationEnabled(true);
        currentLocation=new LatLng(getDouble(sp, "Current Location Latitude", 1), getDouble(sp, "Current Location Longitude", 1));
        LatLng destination=new LatLng(getDouble(sp, "Destination Latitude", 1), getDouble(sp, "Destination Longitude", 1));


        if(markerPoints.size()>1){
            markerPoints.clear();
            map.clear();
        }

        markerPoints.add(currentLocation);
        markerPoints.add(destination);

        MarkerOptions currentLocationOptions = new MarkerOptions()
                .position(currentLocation);
        currentLocationMarker = map.addMarker(currentLocationOptions);
        currentLocationMarker.setTitle("Current Location");
        MarkerOptions destinationOptions = new MarkerOptions()
                .position(destination);
        destinationMarker = map.addMarker(destinationOptions);
        destinationMarker.setTitle("Destination");


        if(markerPoints.size() >= 2){
            System.out.println("entered where you want");
            LatLng origin = markerPoints.get(0);
            LatLng dest = markerPoints.get(1);

            System.out.println("Origin " + origin.toString());
            System.out.println("dest"+ dest.toString());

            String url = getDirectionsUrl(origin, dest);

            DownloadTask downloadTask = new DownloadTask();

            downloadTask.execute(url);

            System.out.println("finished");
        }
        double latitude=getDouble(sp, "Current Location Latitude", 1.0);
        double longitude=getDouble(sp, "Current Location Longitude", 1.0);


        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latitude, longitude), 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))     // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        dest=(TextView) findViewById(R.id.destinationConfirm);
        dest.setText(sp.getString("Destination Address", ""));

        transportationMethod=(TextView) findViewById(R.id.transportationMethod);
        transportationMethod.setText(sp.getString("Transportation Method", ""));

        alertTime= (TextView) findViewById(R.id.alertTime);
        alertTime.setText(timeFormatter(( ((int) sp.getLong("alertTime", 1) / 60)%60),( (int) (sp.getLong("alertTime", 1)/60)/60 )));
        contacts= (TextView) findViewById(R.id.contacts);
        String firstName = contactNames.get(0);
        if (contactNames.size() > 1){

            firstName += " (+" + (contactNames.size()-1) + " more)";
        }
        contacts.setText(firstName);

        editAlertTimeButton=(Button) findViewById(R.id.editAlertTime);
        editAlertTimeButton.setOnClickListener(editAlertTime);

        driving=(RadioButton) dialog.findViewById((R.id.drivingEdit));
        walking=(RadioButton) dialog.findViewById((R.id.walkingEdit));

        driving.setOnClickListener(radioListener);
        walking.setOnClickListener(radioListener);

        editTransportationMethodButton = (Button) findViewById(R.id.editTransportationMethod);
        editTransportationMethodButton.setOnClickListener(editTransportaitonMethod);

        Button btnok = (Button) dialog.findViewById(R.id.finishedTransportationEdit);
        btnok.setOnClickListener(editTransportaitonMethodOK);

        Button btnokDestination = (Button) editDestinationDialog.findViewById(R.id.finishedDestinationEdit);
        btnokDestination.setOnClickListener(editDestinationMethodOK);

        editDestinationText = (EditText) editDestinationDialog.findViewById(R.id.destinationEdit);
        editDestinationText.setOnClickListener(onEditDestinationText);


        Button editDestinationButton = (Button) findViewById(R.id.editDestination);
        editDestinationButton.setOnClickListener(editDestination);

        Button b = (Button) findViewById(R.id.editContacts);
        b.setOnClickListener(editContacts);

        setTimerButton = (Button) findViewById(R.id.start);
        setTimerButton.setOnClickListener(onSetTimer);

    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

        Criteria criteria = new Criteria();
        Location location=null;

        double latitude=getDouble(sp, "Current Location Latitude", 1.0);
        double longitude=getDouble(sp, "Current Location Longitude", 1.0);


        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latitude, longitude), 9));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        newFragment.show(ft, "timePicker");
    }

    public String timeFormatter(int min, int hours) {
        StringBuilder sb=new StringBuilder();
        Log.d("sp has", String.valueOf(sp.getLong("alertTime", 1)));
        Log.d("min mod conver", String.valueOf((sp.getLong("alertTime", 1) % 60)));
        Log.d("min", String.valueOf(min));
        Log.d("hours", String.valueOf(hours));
        if (hours==0) {
            sb.append("00").append((":"));
        }

        if (hours>0 && hours<10) {
            sb.append("0").append(String.valueOf(hours)).append(":");
        }

        if (hours>=10) {
            sb.append(String.valueOf(hours)).append(":");
        }
        if (min==0) {
            sb.append("00");
        }

        if (min>0 && min<10) {
            sb.append("0").append(min);
        }

        if (min>=10) {
            sb.append(String.valueOf(min));
        }
        return sb.toString();
    }



    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        System.out.print("the URL is " + url);

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            System.out.println("got data " + data);

            br.close();

        }catch(Exception e){
            Log.d("Exception url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
        }
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            System.out.println("returning routes data" + data);
            return data;

        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
                System.out.println("parsed result");
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;
        MarkerOptions markerOptions = new MarkerOptions();

        // Traversing through all the routes
        for(int i=0;i<result.size();i++){
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for(int j=0;j<path.size();j++){
                HashMap<String,String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(2);
            lineOptions.color(Color.RED);
        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions!=null) {
            polyline = map.addPolyline(lineOptions);
        }

        //map.addPolyline(lineOptions);
    }
    }

}