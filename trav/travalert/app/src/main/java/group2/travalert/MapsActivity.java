package group2.travalert;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends AppCompatActivity {

    /* GPS Constant Permission */
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    /* Position */
    private static final int MINIMUM_TIME = 10000;  // 10s
    private static final int MINIMUM_DISTANCE = 50; // 50m
    SharedPreferences sp;
    Button startTimerButton;
    EditText location_tf, destination;
    Geocoder geocoder;
    RadioButton rb1, rb2;
    Dialog dialog;
    Dialog dialog2;
    boolean previouslyStarted=false;
    Intent thisIntent;
    Context context;
    View.OnClickListener onStartTimer = new View.OnClickListener() {
        public void onClick(View v) {
            SharedPreferences.Editor editor = sp.edit();
            if (rb1.isChecked()) {
                editor.putString("Transportation Method", "driving");
                editor.commit();
            } else if (rb2.isChecked()){
                editor.putString("Transportation Method", "walking");
                editor.commit();
            }
            System.out.println("current location says " + location_tf.getText());
            System.out.println("destination says " + destination.getText());


            /* Current location missing
            if (location_tf.getText().toString().equals("Set Current Location") && !destination.getText().toString().equals("Set Destination"))  {
                System.out.println("entered not good");
                CharSequence text = "Please Enter Current Location!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            else */
            if (destination.getText().toString().equals("Set Destination")) { //&& !location_tf.getText().toString().equals("Set Current Location")
                CharSequence text = "Please Enter Destination!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }

            /*
            else if (location_tf.getText().toString().equals("Set Current Location") && destination.getText().toString().equals("Set Destination")) {
                System.out.println("entered not good");
                CharSequence text = "Please Enter Current Location and Destination!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } */

            else {
                if (!rb1.isChecked() && !rb2.isChecked()){
                    Toast toast = Toast.makeText(context, "Please select a transportation" +
                            " method!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    Intent intent = new Intent(MapsActivity.this, CalcTime.class);
                    startActivity(intent);
                }
            }


        }
    };
    View.OnClickListener onToSettings= new View.OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            dialog.dismiss();
        }
    };
    View.OnClickListener onOK = new View.OnClickListener() {
        public void onClick(View v) {
            dialog2.dismiss();
        }
    };
    View.OnClickListener radioListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean checked = ((RadioButton) v).isChecked();
            switch(v.getId()) {
                case R.id.driving:
                    if (checked)
                        checked=false;
                    rb2.setChecked(false);
                    break;
                case R.id.walking:
                    if (checked)
                        checked=false;
                    rb1.setChecked(false);
                    break;
            }
        }
    };
    View.OnClickListener onDestinationClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MapsActivity.this, ChooseDestination.class);
            intent.putExtra("Option", "Destination");
            startActivityForResult(intent, 1);
        }
    };
    View.OnClickListener onCurrentLocationClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MapsActivity.this, ChooseDestination.class);
            intent.putExtra("Option", "Current Location");
            startActivityForResult(intent, 1);
        }
    };
    Location location;
    private double lat = 0;
    private double lng = 0;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sp.edit();
        thisIntent=getIntent();
        Bundle extras = thisIntent.getExtras();

        context = getApplicationContext();



        startTimerButton = (Button) findViewById(R.id.startTimer);
        startTimerButton.setOnClickListener(onStartTimer);
        rb1 = (RadioButton) findViewById(R.id.driving);
        rb2 = (RadioButton) findViewById(R.id.walking);
        rb1.setOnClickListener(radioListener);
        rb2.setOnClickListener(radioListener);
        geocoder = new Geocoder(this);



        location_tf = (EditText) findViewById(R.id.TFaddress);
        destination = (EditText) findViewById(R.id.destination_from_homepage);

        location_tf.setText("My Current Location");
        destination.setText("Set Destination");
        System.out.println("entered on create");

        //get current location
        InitializeLocationManager();
        try {
            System.out.println("Try provider = " + provider);
            location = locationManager.getLastKnownLocation(provider); //LocationManager.GPS_PROVIDER
        } catch (SecurityException e) {
            System.out.println("problem with gps");
        }

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            System.out.println("Current location not available");
        }

        destination.setOnClickListener(onDestinationClick);
        location_tf.setOnClickListener(onCurrentLocationClick);

        setUpMapIfNeeded();

        LatLng latLng = new LatLng(lat, lng);
        mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lng))      // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        //save current location to shared preferences
        String currentLocationText = location_tf.getText().toString();
        List<Address> addressList = null;
        editor.putString("Current Location Address", currentLocationText);
        editor.commit();
        Address address = null;

        putDouble(editor, "Current Location Latitude", lat);
        editor.commit();

        putDouble(editor, "Current Location Longitude", lng);
        editor.commit();

        if (sp.getBoolean("Visited", false)==false) {

            dialog2 = new Dialog(this);

            dialog2.setContentView(R.layout.instructions);
            dialog2.setTitle("Instructions");

            Button btnok = (Button) dialog2.findViewById(R.id.ok);
            btnok.setOnClickListener(onOK);
            Drawable d = new ColorDrawable(Color.BLACK);
            d.setAlpha(130);
            dialog2.getWindow().setBackgroundDrawable(d);
            dialog2.show();
            editor.putBoolean("Visited", true);
            editor.commit();
        }

    }

    public void InitializeLocationManager()
    {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, true); //or false??
        System.out.println("Initialize provider = " + provider);

        // API 23: we have to check if ACCESS_FINE_LOCATION and/or ACCESS_COARSE_LOCATION permission are granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // No one provider activated: prompt GPS
            if (provider == null || provider.equals("")) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }

            // At least one provider activated. Get the coordinates
            switch (provider) {
                case "passive":
                    /*
                    android.location.LocationListener locationListener = new android.location.LocationListener() {
                        public void onLocationChanged(Location location) {
                            //Any method here
                        }
                        public void onStatusChanged (String provider, int status, Bundle extras){}
                        public void onProviderEnabled(String provider) {}
                        public void onProviderDisabled (String provider){}
                    };
                    locationManager.requestLocationUpdates(provider,0,0, locationListener);
                    */

                    //open dialog that links to settings to turn on gps?
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);//activity
                    alertDialogBuilder
                            .setMessage("GPS is disabled in your device. Enable it?")
                            .setCancelable(false)
                            .setPositiveButton("Enable GPS",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {

                                            /*Intent callGPSSettingIntent = new Intent(
                                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivity(callGPSSettingIntent);*/

                                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 5);
                                        }
                                    });
                    alertDialogBuilder.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();


                    //locationManager.requestLocationUpdates(provider, MINIMUM_TIME, MINIMUM_DISTANCE, android.location.LocationListener);
                    location = locationManager.getLastKnownLocation(provider);
                    break;
                case "network":
                    break;
                case "gps":
                    android.location.LocationListener locationListener2 = new android.location.LocationListener() {
                        public void onLocationChanged(Location location) {
                            //Any method here
                        }
                        public void onStatusChanged (String provider, int status, Bundle extras){}
                        public void onProviderEnabled(String provider) {}
                        public void onProviderDisabled (String provider){}
                    };
                    locationManager.requestLocationUpdates(provider,0,0, locationListener2);

                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
                    location = locationManager.getLastKnownLocation(provider);

                    System.out.println("gps location = " + location);
                    break;
            }
        } else {
            // The ACCESS_COARSE_LOCATION is denied, then I request it and manage the result in
            // onRequestPermissionsResult() using the constant MY_PERMISSION_ACCESS_FINE_LOCATION
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSION_ACCESS_COARSE_LOCATION);
            }
            // The ACCESS_FINE_LOCATION is denied, then I request it and manage the result in
            // onRequestPermissionsResult() using the constant MY_PERMISSION_ACCESS_FINE_LOCATION
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_ACCESS_FINE_LOCATION);
            }
        }


    }


    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        System.out.println("Latitude = " + String.valueOf(lat));
        System.out.println("Longitude = " + String.valueOf(lng));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        System.out.println("ENTERD SAVED INSTANCE STATE");
        savedInstanceState.putString("Current Location Address", (location_tf.getText().toString()));
        savedInstanceState.putString("Destination Address", (destination.getText().toString()));

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                if ((data.getBooleanExtra("fromCurrentAddress", false)) == false) {
                    destination.setText(data.getStringExtra("Destination Address"));
                    LatLng destinationLocation=new LatLng(data.getDoubleExtra("Destination Latitude", 1), data.getDoubleExtra("Destination Longitude", 1));
                    mMap.addMarker(new MarkerOptions().position(destinationLocation).title("Destination"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(destinationLocation));
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(destinationLocation)      // Sets the center of the map to location user
                            .zoom(17)                   // Sets the zoom
                            .bearing(90)                // Sets the orientation of the camera to east
                            .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    System.out.println("entered dest address");
                }
                else {
                    location_tf.setText((data.getStringExtra("Current Location Address")));
                    LatLng currentLocation=new LatLng(data.getDoubleExtra("Current Location Latitude", 1), data.getDoubleExtra("Current Location Longitude", 1));
                    mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(currentLocation)      // Sets the center of the map to location user
                            .zoom(17)                   // Sets the zoom
                            .bearing(90)                // Sets the orientation of the camera to east
                            .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    System.out.println("entered curr address");
                }

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("entered result Canceled", "");
                location_tf.setText("Choose Current Location");
                destination.setText("Choose Destination");
            }
        }

        if (requestCode == 5 && resultCode == 0) {
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            // API 23: we have to check if ACCESS_FINE_LOCATION and/or ACCESS_COARSE_LOCATION permission are granted
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                if (provider != null) {
                    switch (provider.length()) {
                        case 0:
                            //GPS still not enabled..
                            break;
                        default:
                            provider = "gps";
                            android.location.LocationListener locationListener3 = new android.location.LocationListener() {
                                public void onLocationChanged(Location location) {
                                    //Any method here
                                }

                                public void onStatusChanged(String provider, int status, Bundle extras) {
                                }

                                public void onProviderEnabled(String provider) {
                                }

                                public void onProviderDisabled(String provider) {
                                }
                            };
                            locationManager.requestLocationUpdates(provider, 0, 0, locationListener3);

                            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
                            location = locationManager.getLastKnownLocation(provider);

                            System.out.println("gps location = " + location);
                            Toast.makeText(this, "GPS is now enabled.", Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }
        } else {
            //the user did not enable his GPS
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;

        } else if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    public void onSearch()
    {

        String location = destination.getText().toString();
        List<Address> addressList = null;
        if(location != null || !location.equals(""))
        {
            try {
                addressList = geocoder.getFromLocationName(location , 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude() , address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(""));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(address.getLatitude(), address.getLongitude()))      // Sets the center of the map to location user
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }


    public void onZoom(View view)
    {
        if(view.getId() == R.id.Bzoomin)
        {
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
        }
        if(view.getId() == R.id.Bzoomout)
        {
            mMap.animateCamera(CameraUpdateFactory.zoomOut());
        }
    }


    public void changeType(View view)
    {
        if(mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL)
        {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {


    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }



}

