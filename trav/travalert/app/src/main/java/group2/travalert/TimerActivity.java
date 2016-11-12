package group2.travalert;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Arrays;
import java.util.List;

public class TimerActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    //converts minutes to milliseconds
    private final long TIMECONVERTER = 60*1000;

    SharedPreferences myPrefs;
    View.OnClickListener done_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showArrivedDialog();
        }
    };
    View.OnClickListener cancel_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showCancelDialog();
        }
    };
    View.OnClickListener viewMap_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(TimerActivity.this, ViewMap.class );
            startActivity(intent);
        }
    };

    private TextView hrsTimer;
    private TextView minsTimer;
    private TextView secTimer;
    private TextView miles;
    private TextView eta;
    private ProgressBar pbar;
    private CountDownTimer timer;
    private boolean timerHasStarted = false;
    private int nId =1;

    private boolean activityVisible;
    private NotificationManager nManager;
    private static final int TIMEALERTID = 1;
    private static final int OUTOFTIMEID = 0;

    View.OnClickListener edit_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showEditDialog();
        }
    };
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        TextView distance=(TextView) findViewById(R.id.miles);
        distance.setText(myPrefs.getString("distance", ""));
        TextView eta=(TextView) findViewById(R.id.eta);
        eta.setText(String.valueOf((myPrefs.getLong("alertTime", 0)/60) + " mins"));

        setUp();

        setTitle("Timer");

        checkAndroidVersion();


        startTime = myPrefs.getLong("alertTime", 80);

        timer = new MyCountDownTimer((startTime*1000), 1000);
        timer.start();
        timerHasStarted = true;

        setTitle("Timer");

    }

    @Override
    protected void onResume() {
        super.onResume();
        activityVisible = true;
        if (nManager != null) {
            nManager.cancel(TIMEALERTID);
            nManager.cancel(OUTOFTIMEID);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityVisible = false;

    }

    private void setUp() {
        //get relevant text views
        hrsTimer = (TextView) findViewById(R.id.hourTimer);
        minsTimer = (TextView) findViewById(R.id.minuteTimer);
        secTimer = (TextView) findViewById(R.id.secTimer);
        miles = (TextView) findViewById(R.id.miles);
        eta = (TextView) findViewById(R.id.eta);
        pbar = (ProgressBar) findViewById(R.id.pBar);

        //set onclick listeners

        findViewById(R.id.doneButton).setOnClickListener(done_click);
        findViewById(R.id.editTimeButton).setOnClickListener(edit_click);
        findViewById(R.id.cancelButton).setOnClickListener(cancel_click);
        findViewById(R.id.viewMap).setOnClickListener(viewMap_click);



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

    private void checkAndroidVersion() {
        if (ContextCompat.checkSelfPermission(this, //activity
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

// Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, //activity
                    Manifest.permission.SEND_SMS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, //thisactivity
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);

                // MY_PERMISSIONS_REQUEST_SEND_SMS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // sms-related task you need to do.

                } else {

                }
                return;
            }

        }
    }

    public void showAlertDialog() {
        String alertTime = String.valueOf(myPrefs.getInt("alertNotificationTime", 0));


        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(alertTime + " Minutes Remaining!");
        alertDialog.setMessage("You have " + alertTime + " minutes left on the timer! Add more " +
                "time if you need to, or an alert message will be sent!");



        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Done",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showOutOfTimeDialog() {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Out of Time");
        alertDialog.setMessage("Alert has been sent to your emergency contacts!");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Done",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showArrivedDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Arrived at Destination");
        if(secTimer.getText().equals("00")){
            alertDialog.setMessage("I hope you made it back safely after the alert was sent!");
        }else {
            alertDialog.setMessage("Glad you made it safely!");
        }
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Done",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        timer.cancel();
                        Intent intent = new Intent(TimerActivity.this, MapsActivity.class);
                        startActivity(intent);
                    }
                });
        alertDialog.show();
    }

    private void showCancelDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage("Are you sure you want to cancel the timer?");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        timer.cancel();
                        Intent i=new Intent(TimerActivity.this, MapsActivity.class);
                        startActivity(i);

                    }
                });

        alertDialog.show();

    }

    private void showEditDialog() {
        int hour = Integer.parseInt(hrsTimer.getText().toString());
        int mins = Integer.parseInt(minsTimer.getText().toString());
        TimePickerDialog timePick=
            new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view,
                                          int hours, int minutes) {
                    hrsTimer.setText(String.valueOf(hours));
                    minsTimer.setText(String.valueOf(minutes));
                    secTimer.setText(String.valueOf(59));
                    int totalmins = minutes + (hours*60);
                    long totalsecs = totalmins *60;
                    long newtime = (minutes * TIMECONVERTER) + (60*TIMECONVERTER*hours);
                    if (timerHasStarted) {
                        timer.cancel();
                        timer = new MyCountDownTimer(newtime, 1000);
                        timer.start();
                    }
                    SharedPreferences.Editor peditor = myPrefs.edit();
                    peditor.putLong("alertTime", totalsecs);
                    peditor.commit();
                    }
                },
                hour,mins,true);
        timePick.setTitle("Edit Time");
        timePick.show();

    }

    private void sendNotification() {
        String timeAlert= String.valueOf(myPrefs.getInt("alertNotificationTime", 0));
        String ringtone = myPrefs.getString("ringtone", "");
        Uri uri;

        if (ringtone.equals("")) {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            uri = Uri.parse(ringtone);
        }
        System.out.println("out of alert time tone" + ringtone);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.alarm_white_24dp)
                        .setContentTitle("Timer Alert")
                        .setSound(uri)
                        .setContentText("You have " + timeAlert + " minutes before an alert message is sent out!");

        Intent notificationIntent = new Intent(this, TimerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Adds the Intent that starts the Activity to the top of the stack
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mBuilder.setContentIntent(intent);


        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        nManager.notify(TIMEALERTID, mBuilder.build());

    }

    private void sendOutOfTimeNotification() {
        //String ringtone = myPrefs.getString("ringtone", "");
        String ringtone = "";
        Uri uri;
        System.out.println("out of time tone" + ringtone);
        if (ringtone.equals("")) {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else {
            uri = Uri.parse(ringtone);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.alarm_white_24dp)
                        .setContentTitle("Out of Time")
                        .setSound(uri)
                        .setContentText("Alert has been sent to your notified contacts!");

        Intent notificationIntent = new Intent(this, TimerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Adds the Intent that starts the Activity to the top of the stack
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        mBuilder.setContentIntent(intent);


        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        nManager.notify(OUTOFTIMEID, mBuilder.build());

    }

    @Override
    public void onBackPressed() {
        timer.cancel();
        super.onBackPressed();
    }

    public class MyCountDownTimer extends CountDownTimer
    {

        public MyCountDownTimer(long startTime, long interval)
        {
            super(startTime, interval);
        }

        @Override
        public void onFinish()
        {

            SmsManager smsManager = SmsManager.getDefault();
            String contacts = myPrefs.getString("contactNums", "9712088258");
            System.out.println("contacts = " + contacts);
            List<String> contactList = Arrays.asList(contacts.split(","));
            String msg = myPrefs.getString("alertMessage", "Hi! I haven't made it to my destination yet, please call me to make sure I made it there safe! :)");
            for (String number : contactList){
                number = number.replaceAll("\\W", "");
                smsManager.sendTextMessage(number, null, msg, null, null);
                System.out.println(number);
            }

            if (activityVisible) {
                showOutOfTimeDialog();
            } else {
                sendOutOfTimeNotification();
            }

            //set to 0
            minsTimer = (TextView) findViewById(R.id.minuteTimer);
            hrsTimer.setText("00");
            minsTimer.setText("00");
            secTimer.setText("00");
        }



        @Override
        public void onTick(long millisUntilFinished)
        {
            long totalMins = millisUntilFinished/TIMECONVERTER;
            long totalsecs = millisUntilFinished/1000;
            int secondsLeft = (int) (millisUntilFinished/1000) % 60;
            int hoursLeft = (int) totalMins/60;
            int minsLeft = (int) totalMins % 60;


            hrsTimer.setText(String.valueOf(hoursLeft));
            minsTimer.setText(String.valueOf(minsLeft));
            secTimer.setText(String.valueOf(secondsLeft));

            int alertTime = myPrefs.getInt("alertNotificationTime", 0);

            if (minsLeft == alertTime && secondsLeft == 0) {
                if (activityVisible) {
                    showAlertDialog();
                } else {
                    sendNotification();
                }
            }

            //save time to shared prefereces
            SharedPreferences.Editor peditor = myPrefs.edit();
            peditor.putLong("alertTime", totalsecs);
            peditor.commit();

        }
    }

}
