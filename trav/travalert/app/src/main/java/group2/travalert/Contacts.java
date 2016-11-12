package group2.travalert;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Contacts extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private ListView mylistView;
    private Button confirmDetails;
    private Button addE;
    private List<Contact> mylist = new ArrayList<Contact>();
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private int selected = -1;
    private ContactAdapter objAdapter;
    private TextView text;

    SharedPreferences myPrefs;
    boolean[] isChecked;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        //setting up toolbar and back button
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //setting up prefs and buttons
        myPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        confirmDetails = (Button) findViewById(R.id.confirmDetailsButton);
        confirmDetails.setOnClickListener(confirm_Details);
        addE = (Button) findViewById(R.id.addEmerg);
        addE.setOnClickListener(add_E);
        text = (TextView) findViewById(R.id.list);
        showContacts();
        //Resetting contacts to zero
        SharedPreferences.Editor peditor = myPrefs.edit();
        peditor.remove("index");
        peditor.remove("chosenContacts");
        peditor.commit();
        String contacts = myPrefs.getString("emerg", "");
        List<String> contactList = Arrays.asList(contacts.split(","));
        String e = "Emergency Contacts:  \n\n";
        String other = "\n" + "Other Contacts: \n\n";
        //Making emergency contacts preselected
        String c = "";
        String index = "";
        if (!contacts.equals("")) {
            for (int i = 0; i < contactList.size(); i++) {
                int k = Integer.parseInt(contactList.get(i));
                index += k + ",";
                e += mylist.get(k).getName() + "   (" +
                        mylist.get(k).getNumber() + ")\n";
                c += mylist.get(k).getName() + ",";
            }
            peditor.putString("index", index);
            peditor.putString("chosenContacts", c);
            peditor.commit();
        }else{
            e += "Currently no contacts in emergency list \n";
        }
        text.setText(e + other);
    }
    View.OnClickListener add_E = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showContact();
        }
    };
    private void showContact(){
        CDialog d = new CDialog(this, "Contacts");
        d.show();

    }

    View.OnClickListener confirm_Details = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String f = myPrefs.getString("chosenContacts", "");
            if (f.equals("")) {
                Toast.makeText(getApplicationContext(), "Please choose at least one contact!", Toast.LENGTH_SHORT).show();
            }
            else{
                //Toast.makeText(getApplicationContext(), "Contacts Saved!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Contacts.this, Confirmation.class);
                startActivity(intent);
            }
            //}
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact bean = (Contact) parent.getItemAtPosition(position);
    }


    private void getContacts() {
        List<String> contacts = new ArrayList<>();

        //get phone contacts
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null,  "upper("+ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");

        // Move the cursor to first. Also check whether the cursor is empty or not.
        if (cur.moveToFirst()) {
            // Iterate through the cursor
            do {
                int phoneType = cur.getInt(
                        cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                    String id = cur.getString( //getting id, name, and number
                            cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String phoneNumber = cur.getString(
                            cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                    phoneNumber = phoneNumber.replaceAll("\\W", "");
                    name = name.toUpperCase();
                    contacts.add(name);
                    //Creating a contact to add to list
                    Contact objContact = new Contact();
                    objContact.setID(id);
                    objContact.setName(name);
                    objContact.setNumber(phoneNumber);
                    mylist.add(objContact);
                    System.out.println("object added!");
                }
            } while (cur.moveToNext());
        }
        // Close the cursor
        isChecked = new boolean[cur.getCount()];
        for (int i = 0; i < isChecked.length; i++) {
            isChecked[i] = false;
        }
        cur.close();
        objAdapter = new ContactAdapter(
                Contacts.this, R.layout.activity_contacts_item, mylist, isChecked);//,selected);
       // mylistView.setAdapter(objAdapter);
    }
    /**
     * Show the contacts in the ListView.
     */
    private void showContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.

            getContacts();
            objAdapter = new ContactAdapter(
                    Contacts.this, R.layout.activity_contacts_item, mylist,isChecked);//,selected);
            //mylistView.setAdapter(objAdapter);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
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
}

