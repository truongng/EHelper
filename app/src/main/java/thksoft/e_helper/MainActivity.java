package thksoft.e_helper;

import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    TextToSpeech tts;
    EditText ed1;
    Button b1, b2, b3, bStop;
    CheckBox selectAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1 = findViewById(R.id.button_part1);
        b2 = findViewById(R.id.button_part2);
        bStop = findViewById(R.id.button_stop);
        selectAll = findViewById(R.id.checkBox);

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });

        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.stop();
                tts.shutdown();
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] toSpeak;
                if (selectAll.isChecked()) {
                    toSpeak = getAllFile("Part1");
                } else {
                    toSpeak = new String[]{getFile("Part1")};
                }
                if (toSpeak.length == 0) return;
                try {
                    for (int i = 0; i < toSpeak.length; i++) {
                        BufferedReader br = new BufferedReader(new FileReader(toSpeak[i]));
                        String line;
                        Boolean asking = true;
                        while ((line = br.readLine()) != null) {
                            if (asking) {
                                if (Character.isDigit(line.charAt(0))) {
                                    line = line.substring(line.indexOf(" "));
                                }
                                Toast.makeText(getApplicationContext(), line, Toast.LENGTH_SHORT).show();
                                tts.speak(line, TextToSpeech.QUEUE_ADD, null);
                                asking = false;
                            } else {
                                Toast.makeText(getApplicationContext(), line, Toast.LENGTH_LONG).show();
                                tts.speak(line, TextToSpeech.QUEUE_ADD, null);
                                asking = true;
                            }
                        }
                        br.close();
                    }
                }
                catch (IOException e) {
                    //You'll need to add proper error handling here
                    Log.d("ERROR", e.toString());
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] toSpeak;
                if (selectAll.isChecked()) {
                    toSpeak = getAllFile("Part2");
                } else {
                    toSpeak = new String[]{getFile("Part2")};
                }
                if (toSpeak.length == 0) return;
                try {
                    for (int i = 0; i < toSpeak.length; i++) {
                        BufferedReader br = new BufferedReader(new FileReader(toSpeak[i]));
                        String line = null;
                        StringBuilder sb = new StringBuilder();

                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        br.close();
                        String[] text = sb.toString().split("::");
                        if (text.length > 1) {
                            Toast.makeText(getApplicationContext(), text[0], Toast.LENGTH_SHORT).show();
                            tts.speak(text[0], TextToSpeech.QUEUE_ADD, null);

                            Toast.makeText(getApplicationContext(), text[1], Toast.LENGTH_LONG).show();
                            tts.speak(text[1], TextToSpeech.QUEUE_ADD, null);
                        }
                    }
                }
                catch (IOException e) {
                    //You'll need to add proper error handling here
                    Log.d("ERROR", e.toString());
                }
            }
        });
    }

    private String getFile(String sPart){
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/IELTS/Speaking/" + sPart;
            Log.d("Files", "Path: " + path);
            File directory = new File(path);
            if(directory.exists()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    Log.d("Files", "Size: " + files.length);
                    if (files.length > 0) {
                        final Random r = new Random();
                        String file = path + "/" + files[r.nextInt(files.length - 1)].getName();
                        return file;
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Folder does not exist!", Toast.LENGTH_SHORT).show();
                }
            }
        }catch (Exception e){
            Log.d("ERROR", e.toString());
        }
        return "";
    }

    private String[] getAllFile(String sPart) {
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
                    Toast.makeText(getApplicationContext(), "Folder does not exist!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.d("ERROR", e.toString());
        }
        return new String[0];
    }
}
