package com.rahim.metawaregraph.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rahim on 18-Jun-16.
 */
public class IOUtils {
    private static final String TAG = "Metaware";

    // Create folder and file
    private static File makeDirectoryAndFile() {
        File file = new File(Environment.getExternalStorageDirectory(), "Metaware");
        if (!file.exists()){
            file.mkdir();
            createHeader(file);
        }
        return file;
    }

    // File name according to date
    private static String getFileName(){
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        String dateString = format.format(new Date());
        return dateString;
    }

    // Append Data to csv
    public static void appendData(String string) throws Exception{
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            File directory = makeDirectoryAndFile();

            File file = new File(directory, getFileName()+".csv");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                Date date = new Date();
                PrintWriter pw = new PrintWriter(fileOutputStream);
                pw.println(date.getTime() + "," + string);
                pw.flush();
                pw.close();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, e.toString());
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }
        }else {
           throw new IOException("No external memory");
        }

    }

    // Create csv Header
    private static void createHeader(File directory){
        String header = "Time,Accelerometer X (g),Accelerometer Y (g),Accelerometer Z (g),Gyroscope X (deg/s),Gyroscope Y (deg/s),Gyroscope Z (deg/s)";

        File file = new File(directory, getFileName()+".csv");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            PrintWriter pw = new PrintWriter(fileOutputStream);
            pw.println(header);
            pw.flush();
            pw.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }

}
