package com.hankun.ship.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.StringUtils;
import com.hankun.ship.R;
import com.hankun.ship.app.ShipApp;

/**
 * Created by ding_you on 5/28/2016.
 */
public class ShowMessage {


    public enum MessageType {
        TOAST,
        NOTIFICATION,
        DIALOG
    }

    public enum MessageDuring {
        SHORT,//0 ==> short
        LONG  //1 ==> long
    }


    public static void showToast(Context ctx, String msg, MessageDuring shortOrLong) {
        Toast toast = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showDialog(Activity activity, String msg, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout customView = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.custom_alert_dialog, null);
        final AlertDialog dialog = builder.setView(customView).create();

        TextView titleTv = customView.findViewById(R.id.dialog_title_tv);
        titleTv.setText(title);
        titleTv.setTypeface(ShipApp.getApp().getTypeface());

        TextView contentTv = customView.findViewById(R.id.dialog_content_tv);
        contentTv.setText(msg);
        contentTv.setTypeface(ShipApp.getApp().getTypeface());

        Button btn = customView.findViewById(R.id.dialog_btn);
        btn.setTypeface(ShipApp.getApp().getTypeface());
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
