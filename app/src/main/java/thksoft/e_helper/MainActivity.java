package thksoft.e_helper;

import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    TextToSpeech tts;
    EditText ed1;
    Button b1, b2, b3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1=(Button)findViewById(R.id.button_part1);

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeak = getFile("Part1");
                if(toSpeak.isEmpty()) return;
                try {
                    BufferedReader br = new BufferedReader(new FileReader(toSpeak));
                    String line;
                    Boolean asking = true;
                    while ((line = br.readLine()) != null) {
                        if(asking){
                            if(Character.isDigit(line.charAt(0))){
                                line = line.substring(line.indexOf(" "));
                            }
                            Toast.makeText(getApplicationContext(), line,Toast.LENGTH_SHORT).show();
                            tts.speak(line, TextToSpeech.QUEUE_ADD, null);
                            asking = false;
                        }else{
                            Toast.makeText(getApplicationContext(), line,Toast.LENGTH_LONG).show();
                            tts.speak(line, TextToSpeech.QUEUE_ADD, null);
                            asking = true;
                        }
                    }
                    br.close();
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
}
