package thksoft.e_helper;

import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    TextToSpeech tts;
    EditText ed1;
    Button b1, b2, b3, bStop;
    CheckBox selectAll, repeat;
    String[] toSpeak = new String[]{};
    String[] text = new String[]{};
    int sPart = 0;
    int fileIdx = 0;
    int lineIdx = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1 = findViewById(R.id.button_part1);
        b2 = findViewById(R.id.button_part2);
        bStop = findViewById(R.id.button_stop);
        selectAll = findViewById(R.id.checkBox_all);
        repeat = findViewById(R.id.checkBox_repeat);

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onDone(String utteranceId) {
                            Log.d("MainActivity", "TTS finished");
                            speakOut();
                        }

                        @Override
                        public void onError(String utteranceId) {
                            Log.d("MainActivity", "TTS onError");
                        }

                        @Override
                        public void onStart(String utteranceId) {
                            Log.d("MainActivity", "TTS onStart");
                        }
                    });
                    tts.setLanguage(Locale.UK);
                } else {
                    Log.e("MainActivity", "Initilization Failed!");
                }
            }
        });

        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.stop();
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectAll.isChecked()) {
                    toSpeak = getAllFile("Part1");
                } else {
                    toSpeak = new String[]{getFile("Part1")};
                }
                if (toSpeak.length == 0) {
                    Toast.makeText(getApplicationContext(), "No file!", Toast.LENGTH_SHORT).show();
                }
                sPart = 1;
                fileIdx = 0;
                lineIdx = 0;
                speakOut();
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectAll.isChecked()) {
                    toSpeak = getAllFile("Part2");
                } else {
                    toSpeak = new String[]{getFile("Part2")};
                }
                if (toSpeak.length == 0) {
                    Toast.makeText(getApplicationContext(), "No file!", Toast.LENGTH_SHORT).show();
                }
                sPart = 2;
                fileIdx = 0;
                lineIdx = 0;
                speakOut();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }

    private void speakOut() {
        try {
            if (fileIdx < toSpeak.length) {
                if (lineIdx >= text.length && Utilities.isNotNullNotEmpty(toSpeak[fileIdx])) {
                    BufferedReader br = new BufferedReader(new FileReader(toSpeak[fileIdx]));
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    switch (sPart) {
                        case 2:
                            text = sb.toString().split("::");
                            break;
                        case 1:
                        case 3:
                            text = sb.toString().split("\n");
                            break;
                    }
                    lineIdx = 0;
                }
                if (text.length != 0) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(fileIdx)
                            + String.valueOf(lineIdx));
                    tts.speak(Utilities.ExtractContent(text[lineIdx++]), TextToSpeech.QUEUE_FLUSH, map);
                    if (lineIdx == text.length) fileIdx++;
                }
            } else if (repeat.isChecked()) {
                fileIdx = 0;
                speakOut();
            }
        } catch (Exception ex) {
            Log.d("MainActivity", ex.toString());
        }
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
