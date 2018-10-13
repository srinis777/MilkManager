package com.fibno.srinis.milkmanager;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LauncherActivity extends AppCompatActivity {
    public static final String fileName = "MilkManager";
    public static final String EXTRA_MESSAGE = "com.fibno.srinis.milkmanager.MESSAGE";
    public static final String EXTRA_MESSAGE_KEY = "com.fibno.srinis.milkmanager.MESSAGE.key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        //getApplicationContext().deleteFile(fileName);
        String userName = readFile();
        if (!userName.isEmpty()) {
            Intent intent = new Intent(this, MainActivity.class);
            String message = userName.split(":")[0];
            intent.putExtra(EXTRA_MESSAGE, message);
            intent.putExtra(EXTRA_MESSAGE_KEY, userName.split(":")[1]);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }
    private boolean isFilePresent(String fileName) {
        String path = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.exists();
    }

    private String readFile() {
        String temp="";
        if(!isFilePresent(fileName)) {
            return temp;
        }
        FileInputStream inputStream;
        try {
            inputStream = openFileInput(fileName);
            int c;
            while( (c = inputStream.read()) != -1){
                temp = temp + Character.toString((char)c);
            }
            System.out.println(";;;;;;;;;;" + temp);
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return temp;
        }
    }
    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }
}
