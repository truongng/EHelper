package thksoft.e_helper;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    final int REQ_CODE_SPEECH_INPUT = 100;
    TextToSpeech tts;
    TextView tvQuestion, tvAnswer;
    Button b1, b2, b3, bStop, bNext;
    ImageButton btnSpeak;
    CheckBox cbSelectAll, cbRepeat, cbShowAnswer, cbRandomQ, cbRealTest, cbPushAll;
    String[] toSpeak = new String[]{};
    String[] text = new String[]{};
    int sPart = 0;
    int fileIdx = 0;
    int lineIdx = 0;
    boolean showExample = false, choseRandomly = false, isRealTest = false, bSelectAll = false,
            bRepeat = false, bPushAll = false;
    Handler mHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1 = findViewById(R.id.button_part1);
        b2 = findViewById(R.id.button_part2);
        b3 = findViewById(R.id.button_part3);
        bStop = findViewById(R.id.button_start);
        bNext = findViewById(R.id.button_next);
        cbSelectAll = findViewById(R.id.checkBox_all);
        cbRepeat = findViewById(R.id.checkBox_repeat);
        cbShowAnswer = findViewById(R.id.cbShowAnswer);
        cbRandomQ = findViewById(R.id.chkRandomQ);
        cbRealTest = findViewById(R.id.chkRealTest);
        cbPushAll = findViewById(R.id.chkPushAll);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvAnswer = findViewById(R.id.tvAnswer);
        btnSpeak = findViewById(R.id.btnSpeak);

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onDone(String utteranceId) {
                            Log.d("MainActivity", "TTS finished " + utteranceId);
                            if (!isRealTest) {
                                int delay = 1000;
                                if (Integer.parseInt(utteranceId) % 2 != 0) delay = 2000;
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        speakOut();
                                    }
                                }, delay);
                            }
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
                    tts.setLanguage(Locale.getDefault());
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

        bNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRealTest) {
                    lineIdx++;
                } else {
                    if (choseRandomly) {
                        Random r = new Random();
                        fileIdx = r.nextInt(toSpeak.length - 1);
                    } else if (++fileIdx >= toSpeak.length) fileIdx = 0;
                    text = new String[]{};
                    lineIdx = 0;
                }
                speakOut();
            }
        });

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        cbRealTest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isRealTest = isChecked;
                if (isChecked) {
                    findViewById(R.id.tvAnswerText).setVisibility(View.INVISIBLE);
                    tvQuestion.setVisibility(View.INVISIBLE);
                    btnSpeak.setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.tvAnswerText).setVisibility(View.VISIBLE);
                    tvQuestion.setVisibility(View.VISIBLE);
                    btnSpeak.setVisibility(View.INVISIBLE);
                }
            }
        });

        cbPushAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bPushAll = isChecked;
            }
        });

        cbSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bSelectAll = isChecked;
                cbRandomQ.setEnabled(isChecked);
            }
        });

        cbShowAnswer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showExample = isChecked;
            }
        });

        cbRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bRepeat = isChecked;
            }
        });

        cbRandomQ.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                choseRandomly = isChecked;
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bPushAll) {
                    speakAll("Part1");
                    return;
                }
                toSpeak = new String[]{};
                text = new String[]{};
                if (bSelectAll) {
                    toSpeak = Utilities.getAllFile("Part1", getApplicationContext());
                } else {
                    toSpeak = new String[]{Utilities.getFile("Part1", getApplicationContext(), choseRandomly)};
                }
                if (toSpeak.length == 0) {
                    Toast.makeText(getApplicationContext(), "No file!", Toast.LENGTH_SHORT).show();
                } else {
                    sPart = 1;
                    if (choseRandomly) {
                        Random r = new Random();
                        fileIdx = r.nextInt(toSpeak.length - 1);
                    } else fileIdx = 0;
                    lineIdx = 0;
                    speakOut();
                }
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bPushAll) {
                    speakAll("Part2");
                    return;
                }
                toSpeak = new String[]{};
                text = new String[]{};
                if (bSelectAll) {
                    toSpeak = Utilities.getAllFile("Part2", getApplicationContext());
                } else {
                    toSpeak = new String[]{Utilities.getFile("Part2", getApplicationContext(), choseRandomly)};
                }
                if (toSpeak.length == 0) {
                    Toast.makeText(getApplicationContext(), "No file!", Toast.LENGTH_SHORT).show();
                } else {
                    sPart = 2;
                    if (choseRandomly) {
                        Random r = new Random();
                        fileIdx = r.nextInt(toSpeak.length - 1);
                    } else fileIdx = 0;
                    lineIdx = 0;
                    speakOut();
                }
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bPushAll) {
                    speakAll("Part3");
                    return;
                }
                toSpeak = new String[]{};
                text = new String[]{};
                if (bSelectAll) {
                    toSpeak = Utilities.getAllFile("Part3", getApplicationContext());
                } else {
                    toSpeak = new String[]{Utilities.getFile("Part3", getApplicationContext(), choseRandomly)};
                }
                if (toSpeak.length == 0) {
                    Toast.makeText(getApplicationContext(), "No file!", Toast.LENGTH_SHORT).show();
                } else {
                    sPart = 3;
                    if (choseRandomly) {
                        Random r = new Random();
                        fileIdx = r.nextInt(toSpeak.length - 1);
                    } else fileIdx = 0;
                    lineIdx = 0;
                    speakOut();
                }
            }
        });

        mHandler = new Handler(android.os.Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if (message.what % 2 == 0) {
                    if (showExample) {
                        tvQuestion.setText(message.obj.toString());
                        if (isRealTest) tvAnswer.setText(text[lineIdx]);
                        else tvAnswer.setText("");
                    } else {
                        tvQuestion.setText("");
                        tvAnswer.setText("");
                    }
                } else {
                    if (showExample) tvAnswer.setText(message.obj.toString());
                    else tvAnswer.setText("");
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    tvAnswer.setText(result.get(0));
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pte:
                Intent pte = new Intent(this, PteActivity.class);
                startActivity(pte);
                break;
            default:
                break;
        }
        return true;
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void speakOut() {
        try {
            if (fileIdx < toSpeak.length) {
                if (lineIdx >= text.length && Utilities.isNotNullNotEmpty(toSpeak[fileIdx])) {
                    String fileContent = Utilities.getFileContent(toSpeak[fileIdx]);
                    switch (sPart) {
                        case 2:
                            text = fileContent.split("::");
                            break;
                        case 1:
                        case 3:
                            text = fileContent.split("\n");
                            break;
                    }
                    lineIdx = 0;
                }
                if (text.length != 0) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(fileIdx)
                            + String.valueOf(lineIdx));
                    String sText = Utilities.ExtractContent(text[lineIdx]);
                    tts.speak(sText, TextToSpeech.QUEUE_FLUSH, map);
                    Message message = mHandler.obtainMessage(lineIdx, sText);
                    message.sendToTarget();
                    if (++lineIdx >= text.length) {
                        if (choseRandomly) {
                            Random r = new Random();
                            fileIdx = r.nextInt(toSpeak.length - 1);
                        } else {
                            fileIdx++;
                        }
                    }
                }
            } else if (bRepeat) {
                fileIdx = 0;
                speakOut();
            }
        } catch (Exception ex) {
            Log.d("MainActivity", ex.toString());
        }
    }

    private void speakAll(String sPart) {
        toSpeak = Utilities.getAllFile(sPart, getApplicationContext());
        for (int i = 0; i < toSpeak.length; i++) {
            tts.speak(Utilities.getFileContent(toSpeak[i]), TextToSpeech.QUEUE_ADD, null);
        }
    }
}
