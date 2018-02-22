/**
 * Created by TrNguyen on 29/01/2018.
 */
package thksoft.e_helper;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utilities {
    static int curFileIndex = -1;
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

    static String[] getFilesFromServer(String dir, String extList, String searchPatern) {
        String[] result = new String[]{};
        try {
            String respondText = new Downloader().execute(dir + "/index.php?ext=" + extList).get();
            String[] myFiles = respondText.split("\n");
            List<String> res = new ArrayList<String>();
            for (int i = 0; i < myFiles.length; i++) {
                if (searchPatern.equals("*.*") && extList.contains(myFiles[i].substring(myFiles[i].lastIndexOf(".")))) {
                    res.add(dir + "/" + myFiles[i]);
                } else if (myFiles[i].contains(searchPatern)) {
                    res.add(dir + "/" + myFiles[i]);
                }
            }
            result = res.toArray(new String[res.size()]);
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

    static void playAudio(MediaPlayer.OnCompletionListener ctx, MediaPlayer.OnPreparedListener pre, String url) {
        try {
            killMediaPlayer();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(ctx);
            mediaPlayer.setOnPreparedListener(pre);
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
}
