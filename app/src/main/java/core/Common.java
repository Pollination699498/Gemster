package core;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import core.monsterbook.MonsterBookItem;
import pollinationp.gemster.R;

/**
 * Created by WONSEOK OH on 2016-12-04.
 */

public class Common {

    private static final String PREF_MAIN = "pref_main";
    public static final String MAIN_INIT = "main_init";
    public static final String MAIN_SPEC = "main_spec";
    public static final String MAIN_TIER = "main_tier";
    public static final String MAIN_DNA = "main_DNA";
    public static final String MAIN_DNA_USE = "main_DNA_use";
    public static final String MAIN_DATA_COLLECT = "main_data_collect"; // JSON Object

    public static final int DEBUG_DEFAULT = 900;
    public static final int DEBUG_CHECK_DNA_TIME = 901;
    public static final int DEBUG_CHECK_EVOLUTION_TIME = 902;

    public static final long TIME_DELAY = 10L;
    public static final long DNA_TIME = 0L;
    public static final long EVOLUTION_TIME = 2000; // 2sec

    public static void setPrefData(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_MAIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getMonsterKey(int spec, int tier) {
        return spec + "_" + tier;
    }

    private static boolean isIntegerValue(String key) {
        return MAIN_SPEC.equals(key) || MAIN_TIER.equals(key) || MAIN_DNA.equals(key) || MAIN_DNA_USE.equals(key);
    }

    public static String getDefaultValue(String key) {
        int result;
        if (MAIN_INIT.equals(key) || MAIN_DNA_USE.equals(key)) result = 1;
        else result = 0;

        return String.valueOf(result);
    }

    public static Object getPrefData(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_MAIN, Context.MODE_PRIVATE);
        Object value = prefs.getString(key, "");

        if (isIntegerValue(key)) {
            if ("".equals(value)) return 0;
            return Integer.parseInt((String) value);
        } else {
            return value;
        }
    }

    public static void initSharedPrefData(Context context) {
        // init shared pref values if have not initialized yet
        if ("".equals(getPrefData(context, MAIN_INIT))) {
            setPrefData(context, MAIN_INIT, getDefaultValue(MAIN_INIT));
            setPrefData(context, MAIN_SPEC, getDefaultValue(MAIN_SPEC));
            setPrefData(context, MAIN_TIER, getDefaultValue(MAIN_TIER));
            setPrefData(context, MAIN_DNA, getDefaultValue(MAIN_DNA));
            setPrefData(context, MAIN_DNA_USE, getDefaultValue(MAIN_DNA_USE));
            setIsCollected(context, 0, 0);
        }
    }

    public static boolean isExceptionalTier(Context context) {
        int maxTier = getMaxTier(context);
        int tier = (int) Common.getPrefData(context, Common.MAIN_TIER);
        if (tier >= maxTier) {
            return true;
        }
        return false;
    }

    public static boolean isAbleToEvol(Context context) {
        int maxTier = getMaxTier(context);
        int tier = (int) Common.getPrefData(context, Common.MAIN_TIER);
        if (tier + 1 >= maxTier) {
            return false;
        }
        return true;
    }

    public static int getMaxTier(Context context) {
        int spec = (int) Common.getPrefData(context, Common.MAIN_SPEC);
        int[] arrMaxTier = context.getResources().getIntArray(R.array.array_max_tier_of_spec);
        return arrMaxTier[spec];
    }

    public static void setIsCollected(Context context, int spec, int tier) {
        try {
            HashMap<String, Boolean> map = new HashMap<>();
            String data = (String) getPrefData(context, MAIN_DATA_COLLECT);
            if (data != null && !data.equals("")) {
                JSONObject jsonObject = new JSONObject(data);
                Iterator<?> keys = jsonObject.keys();

                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    Boolean value = jsonObject.getBoolean(key);
                    map.put(key, value);
                }
            }

            String key = getMonsterKey(spec, tier);
            map.put(key, true);

            JSONObject jsonObject = new JSONObject(map);
            setPrefData(context, MAIN_DATA_COLLECT, jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static boolean isCollected(Context context, String monsterKey) {
        boolean isCollected = false;
        try {
            JSONObject jsonObject = new JSONObject((String) getPrefData(context, MAIN_DATA_COLLECT));
            String jsonKey = monsterKey;
            isCollected = jsonObject.getBoolean(jsonKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return isCollected;
    }

    public static int getGemDrawableId(Context context, int spec, int tier) {
        String name = "gem_image_";
        name += getMonsterKey(spec, tier);
        int id = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return id;
    }

    public static int getDNAQuantity(int tier) {
        double base = 1.3f;
        double result = Math.pow(base, tier);
        return (int) result;
    }

    public static double getPerProb(int tier) {
        //double base = 0.6f;
        double base = 1.0f;
        double result = Math.pow(base, tier + 1);
        return result;
    }

    public static int processCompleteDNAUseCount(int tier) {
        double perProb = getPerProb(tier);
        double completeCnt = Math.ceil(1.0f / perProb);
        return (int) completeCnt;
    }

    public static int getCompleteDNAUseCount(int tier) {
        return processCompleteDNAUseCount(tier);
    }

    public static int getCurrentCompleteDNAUseCount(Context context) {
        int tier = (int) getPrefData(context, MAIN_TIER);
        return processCompleteDNAUseCount(tier);
    }

    public static ArrayList<MonsterBookItem> getMonsterBookItemList(Context context) {
        ArrayList<MonsterBookItem> mListItem = new ArrayList<>();
        int[] arrMaxTier = context.getResources().getIntArray(R.array.array_max_tier_of_spec);
        for (int idxSpec = 0; idxSpec < arrMaxTier.length; idxSpec++) {
            int maxTier = arrMaxTier[idxSpec];
            for (int idxTier = 0; idxTier < maxTier; idxTier++) {
                Integer resourceId = getGemDrawableId(context, idxSpec, idxTier);
                mListItem.add(new MonsterBookItem(idxSpec, idxTier, resourceId));
            }
        }
        return mListItem;
    }

    public static byte[] getUserData() {
        JSONObject jsonObject = new JSONObject();
        try {
            Context context = GemsterApp.getInstance();
            jsonObject.put(MAIN_SPEC, String.valueOf((int) getPrefData(context, MAIN_SPEC)));
            jsonObject.put(MAIN_TIER, String.valueOf((int) getPrefData(context, MAIN_TIER)));
            jsonObject.put(MAIN_DNA, String.valueOf((int) getPrefData(context, MAIN_DNA)));
            jsonObject.put(MAIN_DNA_USE, String.valueOf((int) getPrefData(context, MAIN_DNA_USE)));
            jsonObject.put(MAIN_DATA_COLLECT, getPrefData(context, MAIN_DATA_COLLECT));
            return jsonObject.toString().getBytes("UTF-8");
        } catch (JSONException je) {
            je.printStackTrace();
        } catch (UnsupportedEncodingException usee) {
            usee.printStackTrace();
        }

        return null;
    }

    public static void setUserData(byte[] data) {
        try {
            JSONObject jsonObject = new JSONObject(new String(data, "UTF-8"));
            Context context = GemsterApp.getInstance();
            setPrefData(context, MAIN_SPEC, jsonObject.get(MAIN_SPEC).toString());
            setPrefData(context, MAIN_TIER, jsonObject.get(MAIN_TIER).toString());
            setPrefData(context, MAIN_DNA, jsonObject.get(MAIN_DNA).toString());
            setPrefData(context, MAIN_DNA_USE, jsonObject.get(MAIN_DNA_USE).toString());
            setPrefData(context, MAIN_DATA_COLLECT, jsonObject.get(MAIN_DATA_COLLECT).toString());
        } catch (JSONException je) {
            je.printStackTrace();
        } catch (UnsupportedEncodingException usee) {
            usee.printStackTrace();
        }
    }

    public static void resetUserData() {
        Context context = GemsterApp.getInstance();
        Common.setPrefData(context, Common.MAIN_TIER, Common.getDefaultValue(Common.MAIN_TIER));
        Common.setPrefData(context, Common.MAIN_DNA, Common.getDefaultValue(Common.MAIN_DNA));
        Common.setPrefData(context, Common.MAIN_DNA_USE, Common.getDefaultValue(Common.MAIN_DNA_USE));
        Common.setPrefData(context, Common.MAIN_DATA_COLLECT, "");
        Common.setIsCollected(context, 0, 0);
    }

    public static boolean isNetworkConnected() {
        Context context = GemsterApp.getInstance();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null;
    }

}
