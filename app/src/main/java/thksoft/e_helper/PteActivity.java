package thksoft.e_helper;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.danikula.videocache.HttpProxyCacheServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class PteActivity extends AppCompatActivity {
    final int REQ_CODE_SPEECH_INPUT = 100;
    TextToSpeech tts;
    String base_url = "http://kidsnature.space/pte", url = "", dataSource = "";
    String[] audioFiles, questions;
    int fileIdx = -1;
    boolean choseRandomly = false;
    Spinner spinCat;
    Button bRepeatSentence, bAnswerShortQuestion, bDictation, bStart, bRepeat, bShowQuestion,
            bShowAnswer, bCheckResult;
    CheckBox chkChoseRandomly;
    TextView tvQuestion, tvResult;
    EditText etAnswer;
    ProgressDialog progDailog;
    HttpProxyCacheServer proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pte);
        spinCat = findViewById(R.id.cbCategory);
        bRepeatSentence = findViewById(R.id.button_rs);
        bAnswerShortQuestion = findViewById(R.id.button_asq);
        bDictation = findViewById(R.id.button_dic);
        bStart = findViewById(R.id.button_start);
        bRepeat = findViewById(R.id.button_repeat);
        bShowQuestion = findViewById(R.id.button_showQuestion);
        bShowAnswer = findViewById(R.id.button_showAnswer);
        bCheckResult = findViewById(R.id.button_check);
        chkChoseRandomly = findViewById(R.id.chkChoseRandomly);
        tvQuestion = findViewById(R.id.tvQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        tvResult = findViewById(R.id.tvResultText);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onDone(String utteranceId) {
                            Log.d("MainActivity", "TTS finished " + utteranceId);
                            if (!url.contains("dictation")) promptSpeechInput();
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

        bRepeatSentence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                url = base_url + "/repeatsentence";
                updateSpiner(url);
                etAnswer.setEnabled(false);
            }
        });

        bAnswerShortQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                url = base_url + "/answershortquestion";
                updateSpiner(url);
                etAnswer.setEnabled(false);
            }
        });

        bDictation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                url = base_url + "/dictation";
                updateSpiner(url);
                etAnswer.setEnabled(true);
            }
        });

        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (choseRandomly) {
                    Random r = new Random();
                    fileIdx = r.nextInt(questions.length - 1);
                } else if (++fileIdx >= questions.length) fileIdx = 0;
                speakOut();
            }
        });

        bRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileIdx < 0 || fileIdx >= questions.length) return;
                speakOut();
            }
        });

        bShowQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvQuestion.setText(questions[fileIdx].split("-")[0].trim());
            }
        });

        bShowAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String answer = questions[fileIdx];
                if (url.contains("answershortquestion")) {
                    String[] tmp = answer.split("-");
                    if (tmp.length > 1) answer = tmp[1].trim();
                    else answer = "";
                }
                etAnswer.setText(answer);
            }
        });

        bCheckResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResult();
            }
        });

        chkChoseRandomly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                choseRandomly = isChecked;
            }
        });

        spinCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                dataSource = spinCat.getSelectedItem().toString();
                if (!dataSource.startsWith("TU")) {
                    audioFiles = Utilities.getFileLine(url + "/" + dataSource + ".audio");
                } else if (dataSource.startsWith("ATU")) {
                    audioFiles = Utilities.getFilesFromServer(url + "/" + dataSource, ".mp3,.m4a,.wav", "*.*");
                } else {
                    audioFiles = Utilities.getFileLine(url + "/" + dataSource + ".txt");
                }
                questions = Utilities.getFileLine(url + "/" + dataSource + ".txt");
                fileIdx = -1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
    }

    private void speakOut() {
        tvQuestion.setText("");
        etAnswer.setText("");
        if (dataSource.startsWith("TU")) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(fileIdx));
            tts.speak(audioFiles[fileIdx], TextToSpeech.QUEUE_FLUSH, map);
        } else {
            if (proxy == null) proxy = App.getProxy(this);
            progDailog = ProgressDialog.show(this, "Please wait ...", "Retrieving data ...", true);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        String audioUrl = proxy.getProxyUrl(audioFiles[fileIdx]);
                        Utilities.playAudio(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                if (!url.contains("dictation")) promptSpeechInput();
                            }
                        }, new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                progDailog.dismiss();
                            }
                        }, audioUrl);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    etAnswer.setText(result.get(0));
                    showResult();
                }
                break;
            }
        }
    }

    private void showResult() {
        String question = questions[fileIdx];
        if (url.contains("answershortquestion")) {
            String[] tmp = question.split("-");
            if (tmp.length > 1) question = tmp[1].trim();
            else question = "";
        }
        tvResult.setText("Result: " + String.format("%.2f", StringSimilarity.similarity(
                question.toLowerCase(), etAnswer.getText().toString().toLowerCase())) + "%");
    }

    private void updateSpiner(String url) {
        String[] files = Utilities.getFilesFromServer(url, ".txt,.audio", "*.*");
        List<String> spinnerArray = new ArrayList<String>();
        for (int i = 0; i < files.length; i++) {
            String cat = files[i].substring(files[i].lastIndexOf("/") + 1, files[i].lastIndexOf("."));
            if (!spinnerArray.contains(cat)) spinnerArray.add(cat);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCat.setAdapter(adapter);
        etAnswer.setText("");
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
