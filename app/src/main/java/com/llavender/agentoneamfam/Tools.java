package com.llavender.agentoneamfam;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.EditText;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


/**
 * Created by jaz020 on 6/25/2015.
 */
public class Tools {

    public static void replaceFragment(Fragment fragment, FragmentManager fManager,
                                       boolean addToBackStack) {
        FragmentTransaction fTransaction = fManager.beginTransaction();
        fTransaction.replace(R.id.fragment_container, fragment);

        if (addToBackStack) fTransaction.addToBackStack(null);

        fTransaction.commit();
    }

    public static void logout(Context context) {
        SharedPreferences.Editor editor =
                context.getSharedPreferences(Singleton.PREFERENCES, 0).edit();
        editor.remove("OfficeUserID");
        editor.remove("OfficeStayLoggedIn");
        editor.apply();

        ParseUser.logOut();
        ((Activity) context).finish();
    }

    public static void updateDateEntry(EditText editText, Calendar calendar) {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        editText.setText(sdf.format(calendar.getTime()));
    }

    public static void updateTimeEntry(EditText editText, Calendar calendar) {
        String timeFormat = "h:mm a";
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.US);

        editText.setText(sdf.format(calendar.getTime()));
    }

    public static byte[] readBytes(Context context, Uri uri) throws IOException {
        // this dynamically extends to take the bytes you read
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    //TODO recomment
    /**
     * Formats the information of a client or a policy object to be
     * viewed as a list item in a TextView
     *
     * @param object The object that is to be formatted(Client or Policy)
     * @param mode   ClIENT or POLICY
     * @return message that was built
     */
    public static String buildMessage(ParseObject object, int mode) {
        String message = "";

        switch (mode) {
            case Singleton.CLIENT:
                message = object.getString("FirstName");
                message += " " + object.getString("LastName") + "\n";
                message += object.getString("Address") + "\n";
                message += object.getString("City") + ", " + object.getString("State") +
                        " " + object.getNumber("ZIP");
                break;

            case Singleton.POLICY:
                message = object.getString("Line1") + "\n" + object.getString("Line2") + "\n" + object.getNumber("PolicyNumber");
                break;

            case Singleton.CLAIM:
                String damages = String.valueOf(object.get("Damages"));
                BigDecimal parsed = new BigDecimal(damages).setScale(2,BigDecimal.ROUND_FLOOR);
                String formattedDamages = NumberFormat.getCurrencyInstance().format(parsed);

                message = object.getObjectId() + "\n" + formattedDamages;
                break;

            case Singleton.MEETING:

                String myFormat = "MM/dd/yy h:mm a";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                String timeFormat = "h:mm a";
                SimpleDateFormat sdf2 = new SimpleDateFormat(timeFormat, Locale.US);

                message = object.getString("Title") + "\n" + object.getString("Location") + "\n" +
                        sdf.format(object.getDate("StartDate")) + "\n" +
                        sdf.format(object.getDate("EndDate")) + "\n" +
                        object.getString("Comment");
                break;
        }

        return message;
    }
}