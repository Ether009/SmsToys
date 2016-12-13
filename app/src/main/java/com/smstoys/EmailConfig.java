package com.smstoys;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class EmailConfig extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_config);

        SharedPreferences emailSettings = getSharedPreferences("EmailSettings", 0);

        EditText edHost = (EditText) findViewById(R.id.edHost);
        EditText edUser = (EditText) findViewById(R.id.edUser);
        EditText edPassword = (EditText) findViewById(R.id.edPassword);
        EditText edPort = (EditText) findViewById(R.id.edPort);
        CheckBox cbSSL = (CheckBox) findViewById(R.id.cbSSL);
        EditText edProtocol = (EditText) findViewById(R.id.edProtocol);
        EditText edSubject = (EditText) findViewById(R.id.edSubject);

        edHost.setText(emailSettings.getString("host",""));
        edUser.setText(emailSettings.getString("user",""));
        edPassword.setText(emailSettings.getString("password",""));
        edPort.setText(emailSettings.getString("port",""));
        cbSSL.setChecked(emailSettings.getBoolean("is_ssl",true));
        edProtocol.setText(emailSettings.getString("protocol","pop3"));
        edSubject.setText(emailSettings.getString("subject",""));
    }

    public void btnSave_Click(View view) {
        Log.d("[DEBUG]","SmsToys.btnProcess_Click: " + view.toString() + " - Context");
        SharedPreferences emailSettings = getSharedPreferences("EmailSettings", 0);
        SharedPreferences.Editor editor = emailSettings.edit();
        EditText edHost = (EditText) findViewById(R.id.edHost);
        EditText edUser = (EditText) findViewById(R.id.edUser);
        EditText edPassword = (EditText) findViewById(R.id.edPassword);
        EditText edPort = (EditText) findViewById(R.id.edPort);
        CheckBox cbSSL = (CheckBox) findViewById(R.id.cbSSL);
        EditText edProtocol = (EditText) findViewById(R.id.edProtocol);
        EditText edSubject = (EditText) findViewById(R.id.edSubject);

        editor.putString("host",edHost.getText().toString());
        editor.putString("user",edUser.getText().toString());
        editor.putString("password",edPassword.getText().toString());
        editor.putString("port", edPort.getText().toString());
        editor.putString("protocol", edProtocol.getText().toString());
        editor.putString("subject", edSubject.getText().toString());
        editor.putBoolean("is_ssl", cbSSL.isChecked());

        //TODO Have these values to be configurable when SSL is selected. Advanced menu perhaps?
        editor.putString("ssl_factory","javax.net.ssl.SSLSocketFactory");

        editor.apply();

        finish();
    }
}
