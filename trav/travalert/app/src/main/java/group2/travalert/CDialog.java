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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CDialog implements AdapterView.OnItemClickListener{
    private SharedPreferences myPrefs;
    private List<Contact> mylist = new ArrayList<Contact>();
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    boolean[] isChecked;
    private Activity a;
    private AlertDialog dialog;
    boolean[] selectedContacts;
    int prev = 0;
    private String context;
    private TextView contacts;

    public CDialog(Activity activity, String context){
        a = activity;
        this.context = context;
        final String contextC = context;
        myPrefs = PreferenceManager.getDefaultSharedPreferences(a.getApplicationContext());
        getContacts();
        //AlertDialog dialog;
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final ArrayList selectedItems = new ArrayList();
        builder.setTitle("Select Contacts");
        //transferring contact names to another list
        ArrayList<String> names = new ArrayList<String>();
        for (int i = 0;i<mylist.size();i++){
            names.add(mylist.get(i).getName() + "\n (" +
                    mylist.get(i).getNumber() + ")");
        }
        CharSequence[] cs = names.toArray(new CharSequence[mylist.size()]);
        //If on Contacts Screen or Confirmation Screen
        //If selected contacts and reopens "Select contacts" then the
        //contacts displayed in the list should be selected
        if (context.equals("Contacts") || context.equals("Confirmation")) {
            String indices = myPrefs.getString("index", "");
            selectedContacts = new boolean[mylist.size()];
            if (!indices.equals("")) {
                List<String> index = Arrays.asList(indices.split(","));
                //selectedContacts = new boolean[mylist.size()];
                for (String number : index) {
                    prev = 1;
                    int k = Integer.parseInt(number);
                    selectedContacts[k] = true;
                    selectedItems.add(k);
                }
            }
        }

        builder.setMultiChoiceItems(cs, selectedContacts, new DialogInterface.OnMultiChoiceClickListener() {
            // indexSelected contains the index of item (of which checkbox checked)
            @Override
            public void onClick(DialogInterface dialog, int indexSelected,
                                boolean isChecked) {
                if (isChecked) {
                    selectedItems.add(indexSelected);
                } else if (selectedItems.contains(indexSelected)) {
                    selectedItems.remove(Integer.valueOf(indexSelected));
                }
            }
        })
                // Set the action buttons
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("- Emergency", null);

        dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                final DialogInterface d = dialog;
                final Button p = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                final Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                final String contact = myPrefs.getString("emerg", "");
                final List<String> contactL = Arrays.asList(contact.split(","));

                p.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        //  Your code when user clicked on OK
                        //  You can write the code  to save the selected item here
                        String pos = "";
                        String c = "";
                        String n = "";
                        String o = "";
                        String e = "Emergency Contacts: \n\n";
                        String other = "\n" + "Other Contacts: \n\n";
                        Collections.sort(selectedItems);
                        for (int i = 0; i < selectedItems.size(); i++) {
                            int k = Integer.parseInt(selectedItems.get(i).toString());
                            if (contactL.contains(k + "")){
                                e += mylist.get(k).getName() + "   (" +
                                        mylist.get(k).getNumber() + ")\n";
                            }else{
                                other += mylist.get(k).getName() + "   (" +
                                        mylist.get(k).getNumber() + ")\n";
                            }
                            c += mylist.get(k).getName() + ",";
                            n += mylist.get(k).getNumber() + ",";
                            o += k + ",";
                            //pos += mylist.get(k).getName() + "   (" +
                                 //   mylist.get(k).getNumber() + ")\n";
                        }
                        SharedPreferences.Editor peditor = myPrefs.edit();
                        peditor.putString("chosenContacts", c);
                        peditor.putString("contactNums", n);
                        peditor.putString("index", o);
                        peditor.commit();
                        if(o.equals("")){
                            Toast.makeText(a.getApplicationContext(), "Please select at least one contact!",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            if(contextC.equals("Contacts")) {
                                TextView clist = (TextView) a.findViewById(R.id.list);
                                clist.setText(e + other);
                            }
                            else if(contextC.equals("Confirmation")) {
                                //Updating Confirmation View
                                contacts = (TextView) a.findViewById(R.id.contacts);
                                int k = Integer.parseInt(selectedItems.get(0).toString());
                                String firstName = mylist.get(k).getName();
                                if (selectedItems.size() > 1) {
                                    firstName += " (+" + (selectedItems.size() - 1) + " more)";
                                }
                                contacts.setText(firstName);
                            }
                            d.dismiss();
                        }

                    }
                });


                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String contacts = myPrefs.getString("emerg", "");
                        List<String> contactList = Arrays.asList(contacts.split(","));
                        if (contacts == "") {
                            Toast.makeText(a.getApplicationContext(), "No Emergency Contacts to add! " +
                                            "\n Go to settings to add some!",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                        //establishing previous emergency contacts
                        if (b.getText().equals("+ Emergency")) {
                            //selectedContacts = new boolean[mylist.size()];
                            for (String number : contactList) {
                                int k = Integer.parseInt(number);
                                selectedContacts[k] = true;
                                ((AlertDialog) d).getListView().setItemChecked(k, true);
                                if (!selectedItems.contains(k)) {
                                    selectedItems.add(k);
                                }
                            }
                            b.setText("- Emergency");
                        } else {
                            b.setText("+ Emergency");
                            //selectedContacts = new boolean[mylist.size()];
                            for (String number : contactList) {
                                int k = Integer.parseInt(number);
                                //System.out.println("index removing is" + k);
                                if (selectedContacts[k] != false) {
                                    selectedContacts[k] = false;
                                    ((AlertDialog) d).getListView().setItemChecked(k, false);
                                    selectedItems.remove(selectedItems.indexOf(k));
                                }
                            }
                        }
                    }
                    }
                });
            }
        });

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
