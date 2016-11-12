package group2.travalert;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private Button notificationsButton;
    private Button reminderButton;
    private Button messageButton;
    private Button contactsButton;

    private String chosenRingtone;

    private ListView mylistView;
    private List<Contact> mylist = new ArrayList<Contact>();
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    boolean[] isChecked;
    SharedPreferences myPrefs;

    View.OnClickListener show_contacts = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showContact();
        }
    };
    View.OnClickListener show_notifications = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showNotifications();
        }
    };

    View.OnClickListener show_reminder = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Uri uri;
            String notificationsRingtone = myPrefs.getString("ringtone", "");
            if (notificationsRingtone.equals("")) {
                uri = null;
            } else {
                uri = Uri.parse(myPrefs.getString("ringtone", ""));
            }

            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri);
            startActivityForResult(intent, 5);
        }
    };

    View.OnClickListener show_message = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showMessageDialogue();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        notificationsButton = (Button) findViewById(R.id.notificationButton);
        messageButton = (Button) findViewById(R.id.messageButton);
        reminderButton = (Button) findViewById(R.id.reminderButton);
        contactsButton = (Button) findViewById(R.id.contactsButton);

        notificationsButton.setOnClickListener(show_notifications);
        messageButton.setOnClickListener(show_message);
        reminderButton.setOnClickListener(show_reminder);
        contactsButton.setOnClickListener(show_contacts);

        myPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
       // showContacts();

        //set back button
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        setTitle("Settings");

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
    {
        if (resultCode == Activity.RESULT_OK && requestCode == 5)
        {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (uri != null)
            {
                this.chosenRingtone = uri.toString();
            }
            else
            {
                this.chosenRingtone = "";
            }

            SharedPreferences.Editor peditor = myPrefs.edit();
            peditor.putString("ringtone", this.chosenRingtone);
            peditor.commit();

        }
    }

    private void showNotifications() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.alert_notifications, null);
        int time;

        final NumberPicker minsPicker = (NumberPicker) view.findViewById(R.id.minsPicker);
        minsPicker.setFocusable(false);
        minsPicker.setMaxValue(59);

        time = myPrefs.getInt("alertNotificationTime", 0);
        minsPicker.setValue(time);

        //setup dialogue box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle("Alert Notification Time");

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) { }
        });
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                int newtime = minsPicker.getValue();
                SharedPreferences.Editor peditor = myPrefs.edit();
                peditor.putInt("alertNotificationTime", newtime);
                peditor.commit();

            }
        });
        builder.show();
    }

    private void showMessageDialogue(){
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.alert_message_dialogue, null);
        String msg;

        final EditText msgView = (EditText) view.findViewById(R.id.msgView);

        msg = myPrefs.getString("alertMessage", "Hi! I haven't made it to my destination yet, please call me to make sure I made it there safe! :)");

        msgView.setText(msg);
        //setup dialogue box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle("Alert Message");

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) { }
        });
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String newMessage = msgView.getText().toString();
                SharedPreferences.Editor peditor = myPrefs.edit();
                peditor.putString("alertMessage", newMessage);
                peditor.commit();

            }
        });
        builder.show();
    }

    private void showContact(){
        ContactDialog d = new ContactDialog(this);
        d.show();
       /* AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ArrayList selectedItems = new ArrayList();
        builder.setTitle("Select Emergency Contacts");
        //transferring contact names to another list
        ArrayList<String> names = new ArrayList<String>();
        for (int i = 0;i<mylist.size();i++){
            names.add(mylist.get(i).getName());
        }
        CharSequence[] cs = names.toArray(new CharSequence[mylist.size()]);
        //establishing previous emergency contacts
        final boolean[] selectedContacts = new boolean[mylist.size()];
        String contacts = myPrefs.getString("emerg", "");
        if (contacts != "") {
            List<String> contactList = Arrays.asList(contacts.split(","));
            for (String number : contactList) {
                int k = Integer.parseInt(number);
                selectedContacts[k] = true;
                selectedItems.add(k);
            }
        }
        builder.setMultiChoiceItems(cs, selectedContacts, new DialogInterface.OnMultiChoiceClickListener() {
            // indexSelected contains the index of item (of which checkbox checked)
            @Override
            public void onClick(DialogInterface dialog, int indexSelected,
                                boolean isChecked) {
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    // write your code when user checked the checkbox
                    selectedItems.add(indexSelected);
                } else if (selectedItems.contains(indexSelected)) {
                    // Else, if the item is already in the array, remove it
                    // write your code when user Uchecked the checkbox
                    selectedItems.remove(Integer.valueOf(indexSelected));
                }
            }
        })
                // Set the action buttons
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on OK
                        //  You can write the code  to save the selected item here
                        String pos = "";
                        for (int i = 0; i < selectedItems.size();i++){
                            pos += selectedItems.get(i).toString() + ",";
                        }
                        SharedPreferences.Editor peditor = myPrefs.edit();
                        peditor.putString("emerg", pos);
                        peditor.commit();
                        Toast.makeText(getApplicationContext(), pos , Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                        dialog.dismiss();
                    }
                });

        dialog = builder.create();//AlertDialog dialog; create like this outside onClick
        dialog.show();*/
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact bean = (Contact) parent.getItemAtPosition(position);
    }


   /* private void getContacts() {
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
    }
    /**
     * Show the contacts in the ListView.
     */
   /* private void showContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            getContacts();
        }
    }*/

   /* @Override
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
    }*/


}
