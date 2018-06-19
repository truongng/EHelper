/**
 * Created by TrNguyen on 29/01/2018.
 */
package thksoft.pte_helper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Utilities {
    final static int REQ_CODE_SPEECH_INPUT = 100;
    static int curFileIndex = -1;
    static int lastPercent = 0;
    //---First Hit Implementation---
    static HashMap<String, Float> firstHit = new HashMap<String, Float>();
    private static MediaPlayer mediaPlayer;

    static String ExtractContent(String str) {
        try {
            if (str.contains("(") && str.contains(")")) {
                while (str.contains("(")) {
                    int idx = str.indexOf("(");
                    String tmp = str.substring(idx, str.indexOf(")", idx) + 1);
                    str = str.replace(tmp, "");
                }
            }
            if (Character.isDigit(str.charAt(0))) {
                str = str.substring(str.indexOf(" "));
            }
        } catch (Exception ex) {
            Log.d("EX", ex.toString());
        }
        return str.trim();
    }

    static boolean isNotNullNotEmpty(final String string) {
        return string != null && !string.isEmpty() && !string.trim().isEmpty();
    }

    static String getWordOnly(String word) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            Character c = word.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '\'' || c == ' ') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    static String getNumberOnly(String word) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            Character c = word.charAt(i);
            if ((c >= '0' && c <= '9')) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    static int getNextFileId(boolean choseRandomly, int max, int fIndex, boolean visitOnly) {
        Random r = new Random();
        List<Integer> visited = new ArrayList<Integer>();
        do {
            if (visited.size() >= max) {
                return -1;
            }
            if (choseRandomly) {
                fIndex = r.nextInt(max);
            } else if (++fIndex >= max) fIndex = 0;
            if (!visited.contains(fIndex)) visited.add(fIndex);
        } while (getFirstHit(fIndex) * 100 >= MainActivity.accurateRate && !visitOnly);
        return fIndex;
    }

    static String getFileNameWithoutExtention(String file) {
        String res = "";
        if (file.indexOf("/") >= 0) {
            res = file.substring(file.lastIndexOf("/") + 1);
            if (res.indexOf(".") >= 0) res = res.substring(0, res.lastIndexOf("."));
        }
        return getWordOnly(res);
    }

    static String getFile(String sPart, Context ctx, boolean choseRandomly) {
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/IELTS/Speaking/" + sPart;
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            if (directory.exists()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    Log.d("Files", "Size: " + files.length);
                    if (files.length > 0) {
                        if (choseRandomly) {
                            Random r = new Random();
                            curFileIndex = r.nextInt(files.length - 1);
                        } else {
                            if (++curFileIndex >= files.length) curFileIndex = 0;
                        }
                        String file = files[curFileIndex].getName();
                        file = path + "/" + file;
                        return file;
                    }
                } else {
                    Toast.makeText(ctx, "Folder does not exist!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.d("ERROR", e.toString());
        }
        return "";
    }

    static String[] getAllFile(String sPart, Context ctx) {
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/IELTS/Speaking/" + sPart;
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            if (directory.exists()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    String[] res = new String[files.length];
                    for (int i = 0; i < files.length; i++) {
                        String file = path + "/" + files[i].getName();
                        res[i] = file;
                    }
                    return res;
                } else {
                    Toast.makeText(ctx, "Folder does not exist!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.d("ERROR", e.toString());
        }
        return new String[0];
    }

    static String[] getAllFile(String sPart, String ext, Context ctx) {
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/IELTS/Speaking/" + sPart;
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            if (directory.exists()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    List<String> res = new ArrayList<String>();
                    for (int i = 0; i < files.length; i++) {
                        String file = path + "/" + files[i].getName();
                        if (file.substring(file.lastIndexOf(".")).equals(ext))
                            res.add(file);
                    }
                    return res.toArray(new String[0]);
                } else {
                    Toast.makeText(ctx, "Folder does not exist!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.d("ERROR", e.toString());
        }
        return new String[0];
    }

    static String[] getFilesFromServer(String dir, String extList, String searchPatern) {
        String[] result = new String[]{};
        try {
            String url = dir;
            if (!extList.equals("")) url += "/index.php?ext=" + extList;
            String respondText = new Downloader().execute(url).get();
            String[] myFiles = respondText.split("\n");
            if (!extList.equals("")) {
                List<String> res = new ArrayList<String>();
                for (int i = 0; i < myFiles.length; i++) {
                    if (searchPatern.equals("*.*") && extList.contains(myFiles[i].substring(myFiles[i].lastIndexOf(".")))) {
                        res.add(dir + "/" + myFiles[i]);
                    } else if (myFiles[i].contains(searchPatern)) {
                        res.add(dir + "/" + myFiles[i]);
                    }
                }
                result = res.toArray(new String[res.size()]);
            } else {
                return myFiles;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    static String getFileContent(String file) {
        StringBuilder sb = new StringBuilder();
        try {
            if (file.contains("http")) {
                sb.append(new Downloader().execute(file).get());
            } else {
                String line;
                BufferedReader br = new BufferedReader(new FileReader(file));
                while ((line = br.readLine()) != null) {
                    sb.append(ExtractContent(line)).append("\n");
                }
                br.close();
            }
            sb.setLength(Math.max(sb.length() - 1, 0));
        } catch (Exception e) {
            Log.d("ERROR", e.toString());
        }
        return sb.toString();
    }

    static String[] getFileLine(String file) {
        StringBuilder sb = new StringBuilder();
        try {
            if (file.contains("http")) {
                sb.append(new Downloader().execute(file).get());
            } else {
                String line;
                BufferedReader br = new BufferedReader(new FileReader(file));
                while ((line = br.readLine()) != null) {
                    sb.append(ExtractContent(line)).append("\n");
                }
                br.close();
            }
        } catch (Exception e) {
            Log.d("ERROR", e.toString());
        }
        return sb.toString().split("\n");
    }

    static void playAudio(MediaPlayer.OnCompletionListener ctx,
                          MediaPlayer.OnPreparedListener pre,
                          MediaPlayer.OnErrorListener err, String url) {
        try {
            killMediaPlayer();
            lastPercent = 0;
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(ctx);
            mediaPlayer.setOnPreparedListener(pre);
            mediaPlayer.setOnErrorListener(err);
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void killMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void promptSpeechInput(Activity ctx, String text) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, ctx.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra("android.speech.extra.DICTATION_MODE", true);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 20000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        if (text.equals(""))
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, ctx.getString(R.string.speech_prompt));
        else intent.putExtra(RecognizerIntent.EXTRA_PROMPT, text);
        try {
            ctx.startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(ctx, ctx.getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    static void loadFirstHit(String cat, int maxValue) {
        try {
            firstHit.clear();
            String[] res = getFileContent(MainActivity.base_url + "/result.php?c=" + cat).split(";");
            for (int i = 0; i < res.length; i++) {
                String[] tmp = res[i].split(",");
                try {
                    firstHit.put(tmp[0], Float.parseFloat(tmp[1]));
                } catch (Exception e) {
                    Log.d("LoadFirstHit", e.toString());
                    firstHit.put(tmp[0], 0.0f);
                }
            }
            if (maxValue != res.length) {
                for (int i = 0; i < maxValue; i++) {
                    String key = String.format("%s%04d", cat, i);
                    if (!firstHit.containsKey(key)) firstHit.put(key, 0.0f);
                }
            }
        } catch (Exception ex) {
            Log.d("LoadFirstHit", ex.toString());
        }
    }

    static void saveFirstHit(String part, float value) {
        try {
            firstHit.put(part, value);
            //Update to Server
            final String url = MainActivity.base_url + String.format("/result.php?p=%s&r=%.2f", part, value);
            new Thread(new Runnable() {
                public void run() {
                    String responseString = getFileContent(url);
                    if (responseString.contains("Error")) {
                        Log.d("Fail UpdateFirstHit", responseString);
                    }
                }
            }).start();
        } catch (Exception ex) {
            Log.d("LoadFirstHit", ex.toString());
        }
    }

    static float getFirstHit(int idx) {
        try {
            for (String key : firstHit.keySet()) {
                if (key.contains(String.format("%04d", idx))) return firstHit.get(key);
            }
            return 0.0f;
        } catch (Exception ex) {
            Log.d("getFirstHit", ex.toString());
            return 0.0f;
        }
    }

    static void clearFirstHit() {
        try {
            for (String key : firstHit.keySet()) {
                firstHit.put(key, 0.0f);
            }
        } catch (Exception ex) {
            Log.d("clearFirstHit", ex.toString());
        }
    }
}
