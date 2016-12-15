package com.smstoys;

import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ProcessEmailTask extends AsyncTask<Object, Integer, Integer> {
    private HashMap<String, Pattern> patterns = new HashMap<>();

    ProcessEmailTask(HashMap<String, Pattern> inputPatterns) {
        super();
        patterns = inputPatterns;
    }

    @Override
    protected Integer doInBackground(Object... objects) {
        Log.d("[DEBUG]", "SmsToys.ProcessEmailTask.doInBackground - " + this.toString() + " - Thread Started");
        String inputString = (String) objects[0];

        Log.i("[INFO]", "Extracting data. Content size: " + inputString.length());
        Log.d("[DEBUG]", "SmsToys.ProcessEmailTask.doInBackground - " + this.toString() + " - Content: " + inputString);

        String content = inputString.replaceAll("(\\r|\\n|\\r\\n)+", " ");
        HashMap<String, String> data = new HashMap<>();

        for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {
            String key = entry.getKey();
            Pattern pattern = entry.getValue();

            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                data.put(key, matcher.group(1));
                Log.d("[DEBUG]", "SmsToys.ProcessEmailTask.doInBackground - " + this.toString() + " - Regex Key: " + key);
                Log.d("[DEBUG]", "SmsToys.ProcessEmailTask.doInBackground - " + this.toString() + " - Regex Match: " + matcher.group(1));
            }
        }

        //TODO Sending the SMS should probably be moved to its own thread and its own class. Doing quick and dirty for now.
        //Verify phone number
        Log.d("[DEBUG]", "SmsToys.ProcessEmailTask.doInBackground - " + this.toString() + " - Prepare Phone Number");
        String phoneNumber = data.get("telefonHandlaggare"); //Make sure we don't modify the in data for this.
        phoneNumber = phoneNumber.replaceAll("(-| |\\r\\n)+", ""); //Remove newlines, whitespace and dash.
        phoneNumber = phoneNumber.replaceAll("\\+46", "0"); //Remove Swedish country code since that's not needed.
        Log.d("sms.prepare.number", "Number Stripped: " + phoneNumber);

        //--Construct the Message
        //TODO Make sms message configurable from the email data.
        //TODO Make sure sms message conforms to sms limit, or make it send multiple. Config should warn if multiple.
        Log.d("[DEBUG]", "SmsToys.ProcessEmailTask.doInBackground - " + this.toString() + " - Prepare Message");
        String message = "Vi har tagit emot ditt ärende " + data.get("ibNummer") + ". En elektriker kommer kontakta dig så snart som möjligt för att boka en tid.";

        Log.d("[DEBUG]", "SmsToys.ProcessEmailTask.doInBackground - " + this.toString() + " - SMS Being sent to " + phoneNumber + " with message: " + message);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        return 0; //Return error code. 0 if all ok.
    }

    @Override
    protected void onPreExecute() {
        Log.d("[DEBUG]", "SmsToys.ProcessEmailTask.onPreExecute - " + this.toString() + " - Thread Starting");
    }

    @Override
    protected void onPostExecute(Integer result) {
        Log.d("[DEBUG]", "SmsToys.ProcessEmailTask.onPostExecute - " + this.toString() + " - Thread Finishing");
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        Log.d("[DEBUG]", "SmsToys.ProcessEmailTask.onProgressUpdate - " + this.toString() + " - Progress Updating");
    }
}