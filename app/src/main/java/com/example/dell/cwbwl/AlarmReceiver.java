package com.example.dell.cwbwl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.dell.cwbwl.database.MyDB;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        Log.i(AlarmReceiver.class.getSimpleName(), "get Broadcast");
        Intent i = new Intent(context, MainActivity.class);
        Bundle inputBundle = intent.getBundleExtra("record");
        Bundle bundle = new Bundle();
        bundle.putString(MyDB.RECORD_TITLE, inputBundle.getString(MyDB.RECORD_TITLE));
        bundle.putString(MyDB.RECORD_BODY, inputBundle.getString(MyDB.RECORD_BODY));
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra("record", bundle);
//        i.putExtra("record", intent.getBundleExtra("record"));
        context.startActivity(i);
    }
}
