package com.app.chatbenchmarkapp.utils;


import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import org.jetbrains.annotations.TestOnly;

/**
 * Created by rohitkhirid on 1/18/18.
 */
public final class IuidGenerator {
    public static String getIuid(String modelAbbr) throws IllegalArgumentException {
        StringBuilder builder = new StringBuilder();
        builder.append(getEncodedMicroTimeInString())
                .append("hjkdfi")
                .append(modelAbbr);
        return builder.toString();
    }

    public static String getEncodedString(Long time) {
        return Long.toString(time, 36);
    }

    private static String getEncodedMicroTimeInString() {
        long microTime = IUtils.getCurrentTimeInMicro();
        String timeEncoded = getEncodedString(microTime);
        return timeEncoded;
    }

    @Deprecated
    @TestOnly
    public static String getEncodedUserIuidKey(long userId) {
        return String.format("%7s", Long.toString(userId, 36)).replace(' ', '0');
    }

    @Deprecated
    @TestOnly
    public static String getIuid(String modelAbbr, String encodedTime, String encodedIuidKey) throws IllegalArgumentException {
        StringBuilder builder = new StringBuilder();
        builder.append(encodedTime)
                .append(encodedIuidKey)
                .append(modelAbbr);
        return builder.toString();
    }

    public static long getTimeFromIuid(String iuid) {
        if (iuid != null) {
            String timeString = iuid.substring(0, 10);
            return Long.valueOf(timeString, 36);
        } else {
            return 0l;
        }
    }

    public static boolean isLastElementVisible(LinearLayoutManager linearLayoutManager, Integer totalItemCount) {
        try {
            if (totalItemCount == null) {
                return false;
            }
            if (totalItemCount >= 1) {
                View lastChild = linearLayoutManager.findViewByPosition(totalItemCount - 1);
                if (lastChild != null) {
                    if (lastChild.getVisibility() == View.VISIBLE) {
                        return true;
                    } else {
                    }
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isFirstElementVisible(LinearLayoutManager linearLayoutManager, Integer totalItemCount) {

        try {

            if (totalItemCount == null) {
                return false;
            }

            if (totalItemCount >= 1) {
                View lastChild = linearLayoutManager.findViewByPosition(0);
                if (lastChild != null) {
                    if (lastChild.getVisibility() == View.VISIBLE) {
                        return true;
                    } else {
                    }
                }
            } else {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
