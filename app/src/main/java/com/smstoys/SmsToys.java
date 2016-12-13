package com.smstoys;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

public class SmsToys extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_toys);

        requestSmsPermission(this);

    }

    private void requestSmsPermission(Activity thisActivity) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.SEND_SMS}, 201); //201 is just an ID to identify this request.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 201: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Context context = getApplicationContext();
                    CharSequence text = "Permission to Send SMS Granted";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                } else {

                    Context context = getApplicationContext();
                    CharSequence text = "Permission to Send SMS Denied";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        }
    }

    public void btnProcess_Click(View view) {
        Log.d("[DEBUG]","SmsToys.btnProcess_Click: " + view.toString() + " - Context");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Context context = getApplicationContext();
            CharSequence text = "Required Permission Was Not Granted";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }
        new FetchEmailsTask().execute();
    }

    public void btnEmailConfig_Click(View view) {
        Log.d("[DEBUG]","SmsToys.btnProcess_Click: " + view.toString() + " - Context");
        Intent intent = new Intent(this, EmailConfig.class);
        startActivity(intent);
    }

    private class FetchEmailsTask extends AsyncTask<Void, Integer, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            int progressCounter = 0;
            int progressMax;

            Log.d("[DEBUG]","SmsToys.FetchEmailsTask.doInBackground: " + this.toString() + " - Loading Email Settings");
            SharedPreferences emailSettings = getSharedPreferences("EmailSettings", 0);
            String host = emailSettings.getString("host","");
            String user = emailSettings.getString("user","");
            String password = emailSettings.getString("password","");
            String port = emailSettings.getString("port","");
            String protocol = emailSettings.getString("protocol","");
            Boolean is_ssl = emailSettings.getBoolean("is_ssl",true);
            String ssl_factory = emailSettings.getString("ssl_factory","");
            String subject = emailSettings.getString("subject","");
            Log.d("[DEBUG]","SmsToys.FetchEmailsTask.doInBackground: " + this.toString()
                    + " - host: " + host
                    + " - user: " + user
                    + " - password: " + password
                    + " - port: " + port
                    + " - protocol: " + protocol
                    + " - is_ssl: " + is_ssl.toString()
                    + " - ssl factory: " + ssl_factory
                    + " - subject: " + subject
            );

            Log.d("[DEBUG]","SmsToys.FetchEmailsTask.doInBackground: " + this.toString() + " - Loading Date Settings");
            Date lastChecked = getDate(true);
            Date currentTime = getDate(false);
            Log.d("[DEBUG]","SmsToys.FetchEmailsTask.doInBackground: " + this.toString()
                    + " - Last Checked Time: " + lastChecked.toString()
                    + " - Current Time: " + currentTime.toString()
            );

            Log.d("[DEBUG]","SmsToys.FetchEmailsTask.doInBackground: " + this.toString() + " - Set up connection");
            Properties props = new Properties();
            if (is_ssl) {
                props.setProperty("mail.pop3.ssl.trust", "*");
                props.setProperty("mail.pop3.socketFactory.class", ssl_factory);
                props.setProperty("mail.pop3.socketFactory.fallback", "false");
                props.setProperty("mail.pop3.socketFactory.port", port);
            }
            props.setProperty("mail.pop3.port", port);
            URLName url = new URLName(protocol, host, Integer.parseInt(port), null, user, password);
            Session session = Session.getInstance(props, null);

            Log.d("[DEBUG]","SmsToys.FetchEmailsTask.doInBackground: " + this.toString() + " - Connecting");
            List<String> resultMessages = new ArrayList<>();
            Store store;
            Folder inbox;
            Message[] inboxMessages;
            try {
                store = session.getStore(url);
                store.connect();
                Log.d("[DEBUG]","SmsToys.FetchEmailsTask.doInBackground: " + this.toString() + " - Checking for new mail");
                inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);
                inboxMessages = inbox.getMessages();
                progressMax = inbox.getMessageCount();
                if (inboxMessages.length == 0){
                    return new ArrayList<>();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }

            publishProgress(progressCounter, progressMax, 1);

            Log.d("[DEBUG]","SmsToys.FetchEmailsTask.doInBackground: " + this.toString() + " - Fetch all mail");
            // TODO: Fetch only mail that have not been fetched before. This will make the New filter meaningless so don't remove that when done as that could cause issues.
            for (Message message : inboxMessages) {
                try {
                    message.getContent();
                    progressCounter++;
                    publishProgress(progressCounter, progressMax, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Log.d("[DEBUG]","SmsToys.FetchEmailsTask.doInBackground: " + this.toString() + " - Return only new mail");
            List<Message> newMessages = new ArrayList<>();
            for (Message message : inboxMessages) {
                try {
                    if (message.getSentDate().after(lastChecked) && message.getSentDate().before(currentTime)) {
                        newMessages.add(message);
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }

            Log.d("[DEBUG]","SmsToys.FetchEmailsTask.doInBackground: " + this.toString() + " - Return only mail with correct subject");
            List<Message> subjectMessages = new ArrayList<>();
            for (Message message : newMessages) {
                try {
                    if (message.getSubject().toLowerCase().contains(subject.toLowerCase())) {
                        subjectMessages.add(message);
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }

            Log.d("[DEBUG]","SmsToys.FetchEmailsTask.doInBackground: " + this.toString() + " - Return only each part");
            for (Message message : subjectMessages) {
                try {
                    Multipart multipart = (Multipart) message.getContent();
                    for (BodyPart bodypart : getFlatContentList(multipart)) {
                        String content = (String) bodypart.getContent();
                        resultMessages.add(content);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return resultMessages;
        }

        @Override
        protected void onPreExecute() {
            Log.d("[DEBUG]","SmsToys.FetchEmailsTask.onPreExecute: " + this.toString() + " - Thread Starting");
            TextView tvFetcherStatus = (TextView)findViewById(R.id.tvFetcherStatus);
            tvFetcherStatus.setText(getString(R.string.fetcher, "Starting"));
            ProgressBar prgProcess = (ProgressBar)findViewById(R.id.prgProcess);
            prgProcess.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<String> resultMessages) {
            Log.d("[DEBUG]","SmsToys.FetchEmailsTask.onPostExecute: " + this.toString() + " - Thread Finishing");
            TextView tvFetcherStatus = (TextView)findViewById(R.id.tvFetcherStatus);
            tvFetcherStatus.setText(getString(R.string.fetcher, "Idle"));
            ProgressBar prgProcess = (ProgressBar)findViewById(R.id.prgProcess);
            prgProcess.setVisibility(View.INVISIBLE);

            HashMap<String, Pattern> patterns = getPatterns(); //new HashMap<>();

            for (String mailContent : resultMessages) {
                try {
                    new ProcessEmailTask(patterns).execute(mailContent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            Log.d("[DEBUG]","SmsToys.FetchEmailsTask.onPostExecute: " + this.toString() + " - Progress: " + progress[0] + "/" + progress[1]);
            TextView tvFetcherStatus = (TextView)findViewById(R.id.tvFetcherStatus);
            ProgressBar prgProcess = (ProgressBar)findViewById(R.id.prgProcess);
            tvFetcherStatus.setText(getString(R.string.fetching, progress[0]));
            prgProcess.setIndeterminate(false);
            prgProcess.setMax(progress[1]);
            prgProcess.setProgress(progress[0]);
        }

        private List<BodyPart> getFlatContentList(Multipart multipart) throws MessagingException, IOException {
            List<BodyPart> results = new ArrayList<>();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                if (bodyPart.isMimeType("multipart/alternative")) {
                    Multipart nestedPart = (Multipart) bodyPart.getContent();
                    for (BodyPart nestedBody : getFlatContentList(nestedPart)) {
                        results.add( nestedBody);
                    }
                } else if (bodyPart.isMimeType("text/plain")) {
                    results.add(bodyPart);
                }
            }
            return results;
        }
    }

    private HashMap<String, Pattern> getPatterns() {
        HashMap<String, Pattern> patternResults = new HashMap<>();

        patternResults.put("arendeNummer", Pattern.compile(".rendenummer:\\s*(.*?)\\s*IB\\-Nummer"));
        patternResults.put("ibNummer", Pattern.compile("IB\\-Nummer:\\s*(.*?)\\s*Rad"));
        patternResults.put("rad", Pattern.compile("Rad:\\s*(.*?)\\s*Inkommen"));
        patternResults.put("inkommen", Pattern.compile("Inkommen:\\s*(.*?)\\s*Din\\sroll"));
        patternResults.put("roll", Pattern.compile("Din\\sroll:\\s*(.*?)\\s*Status"));
        patternResults.put("status", Pattern.compile("Status:\\s*(.*?)\\s*Adress"));
        patternResults.put("adress", Pattern.compile("Adress:\\s*(.*?)\\s*Avser"));
        patternResults.put("avser", Pattern.compile("Avser:\\s*(.*?)\\s*Beskrivning"));
        patternResults.put("beskrivning", Pattern.compile("Beskrivning:\\s*(.*?)\\s*Notering"));
        patternResults.put("notering", Pattern.compile("Notering:\\s*(.*?)\\s*Handl.ggare\\s\\d"));
        patternResults.put("handlaggare", Pattern.compile("Handl.ggare\\s\\d\\s:\\s*(.*?)\\s*Telefon\\sHandl.ggare\\s\\d"));
        patternResults.put("telefonHandlaggare", Pattern.compile("Telefon\\sHandl.ggare\\s\\d\\s:\\s*(.*?)\\s*Epost\\sHandl.ggare\\s\\d"));
        patternResults.put("epostHandlaggare", Pattern.compile("Epost\\sHandl.ggare\\s\\d\\s*(.*?)\\s*Kontakt\\s\\d"));
        patternResults.put("kontakt", Pattern.compile("Kontakt\\s\\d\\s:\\s*(.*?)\\s*Telefon\\sKontakt\\s\\d"));
        patternResults.put("telefonKontakt", Pattern.compile("Telefon\\sKontakt\\s\\d\\s:\\s*(.*?)\\s*Epost\\skontakt\\s\\d\\s"));
        patternResults.put("epostKontakt", Pattern.compile("Epost\\skontakt\\s\\d\\s:\\s*(.*?)\\s*Med\\sv.nliga\\sh.lsningar"));

        return patternResults;
    }

    private Date getDate(boolean last) {
        SharedPreferences dateSettings = getSharedPreferences("DateSettings", 0);
        Calendar cal = Calendar.getInstance();
        if (last) {
            cal.set(Calendar.YEAR, dateSettings.getInt("lastCheckYear",2000));
            cal.set(Calendar.MONTH, dateSettings.getInt("lastCheckMonth",0));
            cal.set(Calendar.DATE, dateSettings.getInt("lastCheckDay",1));
            cal.set(Calendar.HOUR_OF_DAY, dateSettings.getInt("lastCheckHour",0));
            cal.set(Calendar.MINUTE, dateSettings.getInt("lastCheckMinute",0));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        return cal.getTime();
    }
}