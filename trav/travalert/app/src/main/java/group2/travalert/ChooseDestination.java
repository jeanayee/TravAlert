package group2.travalert;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChooseDestination extends AppCompatActivity {

    protected static ArrayList<String> destinationItems;
    protected static destinationItemAdapter aa;
    protected static DatabaseAdapter dbAdapter;
    private static Cursor curse;
    Button continueButton;
    EditText destinationText;
    SharedPreferences sp;
    Geocoder geocoder;
    Intent thisIntent;
    Intent confirmationIntent;
    Intent mapsIntent;
    Context context;

    View.OnClickListener onContinueButton = new View.OnClickListener() {
        public void onClick(View v) {
            if (thisIntent.getStringExtra("Option").equals("Destination")) {
                SharedPreferences.Editor editor = sp.edit();
                Log.d("option entered", "destination");
                String destination = destinationText.getText().toString();
                List<Address> addressList = null;
                editor.putString("Destination Address", destination);
                editor.commit();
                Address address = null;

                Intent returnIntent = new Intent();
                returnIntent.putExtra("Destination Address", destination);
                returnIntent.putExtra("fromCurrentAddress", false);

                if (destination != null || destination.equals("")) {
                    try {
                        addressList = geocoder.getFromLocationName(destination, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        address = addressList.get(0);
                        double destinationlatitutde = address.getLatitude();
                        double destinationlongitude = address.getLongitude();

                        putDouble(editor, "Destination Latitude", destinationlatitutde);
                        returnIntent.putExtra("Destination Latitude", destinationlatitutde);
                        editor.commit();
                        putDouble(editor, "Destination Longitude", destinationlongitude);
                        returnIntent.putExtra("Destination Longitude", destinationlongitude);
                        editor.commit();


                        setResult(Activity.RESULT_OK, returnIntent);
                        dbAdapter.insertItem(destination);
                        if (destinationText.getText().toString().equals("Choose Destination")) {
                            CharSequence text = "Please Enter Current Location!";
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        } else {
                            finish();
                        }
                    } catch (java.lang.IndexOutOfBoundsException e) {
                        CharSequence text = "Invalid Address";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        e.printStackTrace();
                    }

                    //startActivity(mapsIntent);
                }
            }

            if (thisIntent.getStringExtra("Option").equals("Current Location")) {
                SharedPreferences.Editor editor = sp.edit();
                Log.d("option entered", "Current Location");
                String currentLocationText = destinationText.getText().toString();
                List<Address> addressList = null;
                editor.putString("Current Location Address", currentLocationText);
                editor.commit();
                Address address = null;

                Intent returnIntent = new Intent();
                returnIntent.putExtra("Current Location Address", currentLocationText);
                returnIntent.putExtra("fromCurrentAddress", true);


                if (currentLocationText != null || currentLocationText.equals("")) {
                    try {
                        addressList = geocoder.getFromLocationName(currentLocationText, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        address = addressList.get(0);
                        double currentLocationlatitutde = address.getLatitude();
                        double currentLocationlongitude = address.getLongitude();

                        putDouble(editor, "Current Location Latitude", currentLocationlatitutde);
                        returnIntent.putExtra("Current Location Latitude", currentLocationlatitutde);
                        editor.commit();
                        putDouble(editor, "Current Location Longitude", currentLocationlongitude);
                        returnIntent.putExtra("Current Location Longitude", currentLocationlongitude);
                        editor.commit();


                        setResult(Activity.RESULT_OK, returnIntent);
                        dbAdapter.insertItem(currentLocationText);

                        if (destinationText.getText().toString().equals("Choose Current Location")) {
                            CharSequence text = "Please Enter Current Location!";
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        } else {
                            finish();
                        }
                    } catch (java.lang.IndexOutOfBoundsException e) {
                        CharSequence text = "Invalid Address";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        e.printStackTrace();
                    }


                    //startActivity(mapsIntent);
                }

            }
        }
    };
    View.OnClickListener onTextClick = new View.OnClickListener() {
        public void onClick(View v) {
            destinationText.setText("");
        }
    };
    private ListView recentDestinationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_destination);
        dbAdapter = new DatabaseAdapter(this);
        dbAdapter.open();
        thisIntent=getIntent();
        confirmationIntent=new Intent(ChooseDestination.this, Confirmation.class);
        mapsIntent = new Intent(ChooseDestination.this, MapsActivity.class);
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        context=getApplicationContext();
      //  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);
        //setTitle("Choose Destination");

        recentDestinationsList = (ListView) findViewById(R.id.recentDestinations);
        // create ArrayList of courses from database
        destinationItems = new ArrayList<String>();
        // make array adapter to bind arraylist to listview with new custom item layout
        aa = new destinationItemAdapter(this, R.layout.destination_item_layout, destinationItems);

        recentDestinationsList.setAdapter(aa);
        updateArray();

        continueButton=(Button) findViewById(R.id.continueButton);
        continueButton.setOnClickListener(onContinueButton);

        destinationText=(EditText) findViewById(R.id.choose_destination);
        geocoder = new Geocoder(this);

        recentDestinationsList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d("entered", "item selected");
                int index = position; // position in array adapter
                curse = dbAdapter.getAllItems();
                curse.move(curse.getCount() - index);
                String address = curse.getString(1);
                destinationText.setText(address);
            }
        });


        destinationText.setOnClickListener(onTextClick);

        if (thisIntent.getStringExtra("Option").equals("Destination")) {
            destinationText.setText("Choose Destination");
        }

        else {
            destinationText.setText("Choose Current Location");
        }

    }

    public void updateArray() {
        curse =dbAdapter.getAllItems();
        destinationItems.clear();
        if (curse.moveToFirst())
            do {
                String result = curse.getString(1);
                destinationItems.add(0, result);  // puts in reverse order
            } while (curse.moveToNext());

        aa.notifyDataSetChanged();
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

}
