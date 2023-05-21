package com.rongjingtaihegezheng.cert;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import io.dcloud.PandoraEntry;

public class MyPandoraEntry extends PandoraEntry {

    @Override
    public void startActivity(Intent intent) {
        ComponentName componentName = intent.getComponent();
        String packageName = componentName.getPackageName();
        String className = componentName.getClassName();
        if(className.equals("io.dcloud.PandoraEntryActivity")){
            intent.setComponent(new ComponentName(packageName, packageName+".MainActivity"));
        }
        Log.e("mypandoraEntry", String.format("activity = %s, startActivity, activity name = %s", this, intent.getComponent().getClassName()));
        super.startActivity(intent);
    }

}
