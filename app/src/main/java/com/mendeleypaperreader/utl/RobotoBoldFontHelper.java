package com.mendeleypaperreader.utl;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Created by pedro on 08/03/15.
 */

public class RobotoBoldFontHelper
{
    private static Typeface mRobotoBoldFont = null;

    private static Typeface getRobotoBoldTypeface(Context context) {
        if (mRobotoBoldFont == null)
            mRobotoBoldFont = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
        return mRobotoBoldFont;
    }

    public static TextView applyFont(Context context, TextView textView) {
        if (textView != null)
            textView.setTypeface(getRobotoBoldTypeface(context));
        return textView;
    }
}
