package com.example.datarecoverynew.utils;

import android.app.Activity;
import android.content.Intent;

import com.example.datarecoverynew.R;

public class ThemesUtils {

    public static int sTheme = 0;
    public final static int THEME_MATERIAL_LIGHT = 0;
    public final static int THEME_DARK_THEME = 1;
    public final static int THEME_1 = 2;
    public final static int THEME_2 = 3;
    public final static int THEME_3 = 4;
    public final static int THEME_4 = 5;
    public final static int THEME_5 = 6;

    public static void changeToTheme(Activity activity, int theme) {
        sTheme = theme;
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    public static void onActivityCreateSetTheme(Activity activity) {
        switch (sTheme) {
            default:
            case THEME_DARK_THEME:
                activity.setTheme(R.style.DarkTheme);
                break;
            case THEME_MATERIAL_LIGHT:
                activity.setTheme(R.style.LightTheme);
                break;
            case THEME_1:
                activity.setTheme(R.style.THEME_1);
                break;
            case THEME_2:
                activity.setTheme(R.style.THEME_2);
                break;
            case THEME_3:
                activity.setTheme(R.style.THEME_3);
                break;
            case THEME_4:
                activity.setTheme(R.style.THEME_4);
                break;
            case THEME_5:
                activity.setTheme(R.style.THEME_5);
                break;
        }
    }
}
