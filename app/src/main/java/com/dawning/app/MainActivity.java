package com.dawning.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.dawning.upgrade.UpgradeBean;
import com.dawning.upgrade.UpgradeConfig;
import com.dawning.upgrade.UpgradeManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UpgradeManager.Companion.getInitialize()
                .config(new UpgradeConfig(false))
                .setLifecycle(getLifecycle())
                .check(this,
                        new UpgradeBean("1.1",
                        2,
                        "https://static.helianhealth.com/llapp/2019-10-30/helianwifi_2.7.4.apk",
                                "a_test",
                        "update.apk",
                        false));



//        AlertDialog dialog = new AlertDialog.Builder(this).create();
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
    }
}
