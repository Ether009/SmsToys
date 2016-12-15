package com.smstoys;

import android.app.Activity;
import android.content.SharedPreferences;
import android.widget.TextView;

class UpdateLastTime {
    static void UpdateTime(Activity mActivity) {
        SharedPreferences dateSettings = mActivity.getSharedPreferences("DateSettings", 0);
        //TODO Use the dedicated method for this.
        int iYear = dateSettings.getInt("lastCheckYear", 2000);
        int iMonth = dateSettings.getInt("lastCheckMonth", 0);
        int iDay = dateSettings.getInt("lastCheckDay", 0);
        int iHour = dateSettings.getInt("lastCheckHour", 0);
        int iMinute = dateSettings.getInt("lastCheckMinute", 0);
        TextView txtLastTime = (TextView) mActivity.findViewById(R.id.txtLastTime);
        txtLastTime.setText(String.valueOf(iHour) + ":" + String.valueOf(iMinute) + " " + String.valueOf(iDay) + "-" + String.valueOf(iMonth + 1) + "-" + String.valueOf(iYear));
    }
}
