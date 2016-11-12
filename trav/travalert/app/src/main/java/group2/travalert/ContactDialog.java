package group2.travalert;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ContactDialog implements AdapterView.OnItemClickListener{
    private SharedPreferences myPrefs;
    private List<Contact> mylist = new ArrayList<Contact>();
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    boolean[] isChecked;
    private Activity a;
    private AlertDialog dialog;
    boolean[] selectedContacts;

    public ContactDialog(Activity activity){
        a = activity;
        myPrefs = PreferenceManager.getDefaultSharedPreferences(a.getApplicationContext());
        getContacts();
        //AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final ArrayList selectedItems = new ArrayList();
        builder.setTitle("Select Emergency Contacts");
        //transferring contact names to another list
        ArrayList<String> names = new ArrayList<String>();
        for (int i = 0;i<mylist.size();i++){
            names.add(mylist.get(i).getName() + " (" +
            mylist.get(i).getNumber() + ")");
        }
        ArrayList<String> ordered = new ArrayList<String>();
        //establishing previous emergency contacts
        // = new boolean[mylist.size()];
        String contacts = myPrefs.getString("emerg", "");
        if (contacts != "") {
            List<String> contactList = Arrays.asList(contacts.split(","));
            selectedContacts = new boolean[mylist.size()];
            for (String number : contactList) {
                int k = Integer.parseInt(number);
                selectedContacts[k] = true;
                selectedItems.add(k);
            }
        }
        CharSequence[] cs = names.toArray(new CharSequence[mylist.size()]);

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
                        Collections.sort(selectedItems);
                        for (int i = 0; i < selectedItems.size();i++){
                            pos += selectedItems.get(i).toString() + ",";
                        }
                        SharedPreferences.Editor peditor = myPrefs.edit();
                        peditor.putString("emerg", pos);
                        peditor.commit();
                        if (pos.equals("")) {
                            Toast.makeText(a.getApplicationContext(), "You currently have no " +
                                    "emergency contacts!", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(a.getApplicationContext(), "Contacts Saved!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact bean = (Contact) parent.getItemAtPosition(position);
    }


    private void getContacts() {
        List<String> contacts = new ArrayList<>();

        //get phone contacts
        ContentResolver cr = a.getContentResolver();
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


    public void show() {
        dialog.show();
    }


}
