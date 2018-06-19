package thksoft.pte_helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.danikula.videocache.HttpProxyCacheServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    final static String base_url = "http://kidsnature.space/pte1";
    //*******************CONSTANT********************
    public static float accurateRate = 70;
    TextToSpeech tts;
    String url = "", dataSource = "";
    String practiceUrl = "";
    String[] audioFiles, questions;
    int fileIdx = -1, maxLength = -1, totalPractised = 0, needUpdateIdx = 0;
    //    List<Integer> practised = new ArrayList<Integer>();
    boolean choseRandomly = false, surfMode = false, showQuestion = false, visitOnly = false;
    boolean multiVoices = false, focusAccuracy = false;
    //    volatile boolean isError = false;
    Spinner spinCat, spinPart, spinQuestionsList;
    Button bStart, bRepeat, bShowQuestion, bShowAnswer, bCheckResult, bViewAchievements;
    CheckBox chkChoseRandomly, chkSurfAll, chkShowQuestion, chkVisitOnly, chkVoices, chkAccuracy;
    TextView tvQuestion, tvResult;
    EditText etAnswer;
    ImageView img;
    ProgressDialog progDialog;
    HttpProxyCacheServer proxy;
    ImageButton btnSpeak;
    Activity thisAct = null;
    Handler mHandler = null;
    Timer timeout = new Timer();
    Voice[] voiceArray = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinCat = findViewById(R.id.cbCategory);
        spinPart = findViewById(R.id.cbPart);
        spinQuestionsList = findViewById(R.id.cbQuestionsList);
        bStart = findViewById(R.id.button_start);
        bRepeat = findViewById(R.id.button_repeat);
        bShowQuestion = findViewById(R.id.button_showQuestion);
        bShowAnswer = findViewById(R.id.button_showAnswer);
        bCheckResult = findViewById(R.id.button_check);
        bViewAchievements = findViewById(R.id.button_todayAchievements);
        chkChoseRandomly = findViewById(R.id.chkChoseRandomly);
        chkSurfAll = findViewById(R.id.chkSurfMode);
        chkShowQuestion = findViewById(R.id.chkShowQuestion);
        chkVisitOnly = findViewById(R.id.chkVisitOnly);
        chkVoices = findViewById(R.id.chkVoices);
        chkAccuracy = findViewById(R.id.chkAccuracy);
        tvQuestion = findViewById(R.id.tvQuestion);
        etAnswer = findViewById(R.id.etAnswer);
        tvResult = findViewById(R.id.tvResultText);
        btnSpeak = findViewById(R.id.btnSpeak);
        img = findViewById(R.id.imageView);

        thisAct = this;
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Set<Voice> sVoice = tts.getVoices();
                    for (Iterator<Voice> v = sVoice.iterator(); v.hasNext(); ) {
                        Voice currentVoice = v.next();
                        if (!currentVoice.getLocale().getLanguage().contains("en")) {
                            v.remove();
                        }
                    }
                    voiceArray = sVoice.toArray(new Voice[sVoice.size()]);
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onDone(String utteranceId) {
                            Log.d("MainActivity", "TTS finished " + utteranceId);
                            if (visitOnly) {
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        Message message = mHandler.obtainMessage(0, "");
                                        message.sendToTarget();
                                    }
                                }, 3000);
                            } else if (!url.contains("dictation") && !url.contains("summaryspokentext")) {
                                Utilities.promptSpeechInput(thisAct, "");
                            }
                            if (multiVoices) {
                                Random r = new Random();
                                int voiceIdx = r.nextInt(voiceArray.length);
                                tts.setVoice(voiceArray[voiceIdx]);
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

                } else {
                    Log.e("MainActivity", "Initilization Failed!");
                }
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.practice_setcion, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinPart.setAdapter(adapter);
        spinPart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                setTitle("PTE - " + spinPart.getSelectedItem().toString());
                etAnswer.setEnabled(false);
                Date today = Calendar.getInstance().getTime();
                practiceUrl = base_url + "/index.php?d=" + DateFormat.format("yyyy-MM-dd", today).toString();
                switch (spinPart.getSelectedItemPosition()) {
                    case 0:
                        url = base_url + "/readaloud";
                        practiceUrl += "&p=ReadAloud";
                        break;
                    case 1:
                        url = base_url + "/repeatsentence";
                        practiceUrl += "&p=RepeatSentence";
                        break;
                    case 2:
                        url = base_url + "/answershortquestion";
                        practiceUrl += "&p=AnswerShortQuestion";
                        break;
                    case 3:
                        url = base_url + "/dictation";
                        practiceUrl += "&p=Dictation";
                        etAnswer.setEnabled(true);
                        break;
                    case 4:
                        url = base_url + "/retell";
                        practiceUrl += "&p=Retell";
                        break;
                    case 5:
                        url = base_url + "/summaryspokentext";
                        practiceUrl += "&p=SST";
                        break;
                    case 6:
                        url = base_url + "/hiw";
                        practiceUrl += "&p=HighlighIncorrectWords";
                        break;
                    case 7:
                        url = base_url + "/describeimage";
                        practiceUrl += "&p=DescribeImage";
                        break;
                }
                updateDataCategory(url);
                totalPractised = currentPractice(practiceUrl);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (needUpdateIdx++ % 2 == 0 || visitOnly)
                        fileIdx = Utilities.getNextFileId(choseRandomly, maxLength, fileIdx, visitOnly);
                    if (fileIdx < 0) {
                        Toast.makeText(getApplicationContext(), "All GOOD!!!", Toast.LENGTH_SHORT).show();
                        Message message = mHandler.obtainMessage(4, v.getContext());
                        message.sendToTarget();
                        return;
                    }

                    if (url.contains("readaloud")) {
                        if (fileIdx < questions.length) {
                            if (questions[fileIdx].contains("http")) {
                                questions[fileIdx] = Utilities.getFileContent(questions[fileIdx]);
                            }
                            Utilities.promptSpeechInput(thisAct, questions[fileIdx]);
                        }
                    } else if (url.contains("describeimage")) {
                        // grab image to display
                        try {
                            String content = String.format("<img src=\"%s\" align=\"middle\">", questions[fileIdx]);
                            PicassoImageGetter imageGetter = new PicassoImageGetter(getApplicationContext(), tvQuestion);
                            Spannable html;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                html = (Spannable) Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY, imageGetter, null);
                            } else {
                                html = (Spannable) Html.fromHtml(content, imageGetter, null);
                            }
                            tvQuestion.setText(html);
                        } catch (Exception e) {
                            Log.d("MAIN", e.toString());
                        }
                    } else {
                        speakOut();
                    }
                    if (showQuestion) {
                        bShowQuestion.performClick();
                    }
                    setTitle("PTE - " + spinPart.getSelectedItem().toString() + " = "
                            + String.valueOf(totalPractised) + ":" + String.valueOf(fileIdx + 1));
                    //Update the Question List idex
                    Message message = mHandler.obtainMessage(5, fileIdx);
                    message.sendToTarget();
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        bRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileIdx < 0 || fileIdx >= questions.length) return;
                if (url.contains("readaloud")) {
                    Utilities.promptSpeechInput(thisAct, questions[fileIdx]);
                } else {
                    speakOut();
                }
            }
        });

        bShowQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileIdx < 0 || fileIdx >= questions.length) return;
                if (url.contains("answershortquestion"))
                    tvQuestion.setText(questions[fileIdx].split("-")[0].trim());
                else
                    tvQuestion.setText(questions[fileIdx]);
            }
        });

        bShowAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileIdx < 0 || fileIdx >= questions.length) return;
                String answer = questions[fileIdx];
                if (url.contains("answershortquestion")) {
                    String[] tmp = answer.split("-");
                    if (tmp.length > 1) answer = tmp[1].trim();
                    else answer = "";
                }
                etAnswer.setText(answer);
                tts.speak(answer, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (spinPart.getSelectedItemPosition() == 0) {
                    Utilities.promptSpeechInput(thisAct, questions[fileIdx]);
                } else {
                    Utilities.promptSpeechInput(thisAct, "");
                }
            }
        });

        bCheckResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showResult();
            }
        });

        bViewAchievements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date today = Calendar.getInstance().getTime();
                String url = base_url + "/index.php?d=" + DateFormat.format("yyyy-MM-dd", today).toString();
                String[] tmp = Utilities.getFileContent(url).split(";");
                if (tmp.length > 1) {
                    String textResult = "";
                    for (int i = 1; i < tmp.length; i++) {
                        String[] temp = tmp[i].split(",");
                        textResult += temp[0] + " = " + temp[1] + "\n";
                    }
                    tvQuestion.setText(textResult);
                }
            }
        });

        chkChoseRandomly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                choseRandomly = isChecked;
            }
        });

        chkSurfAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                surfMode = isChecked;
            }
        });

        chkShowQuestion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showQuestion = isChecked;
            }
        });

        chkVisitOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                visitOnly = isChecked;
            }
        });

        chkVoices.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                multiVoices = isChecked;
            }
        });
        chkAccuracy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                focusAccuracy = isChecked;
            }
        });
        spinCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            dataSource = spinCat.getSelectedItem().toString();
                            if (!url.contains("describeimage")) {
                                if (dataSource.startsWith("ATU")) {
                                    audioFiles = Utilities.getFilesFromServer(url + "/" + dataSource, ".mp3,.m4a,.wav,.txt", "*.*");
                                } else if (!dataSource.startsWith("TU")) {
                                    audioFiles = Utilities.getFileLine(url + "/" + dataSource + ".audio");
                                } else if (spinPart.getSelectedItemPosition() != 0) { //TU
                                    audioFiles = Utilities.getFileLine(url + "/" + dataSource + ".txt");
                                }
                            }
                            if (url.contains("readaloud")) {
                                questions = Utilities.getFilesFromServer(url, ".txt", dataSource);
                            } else if (url.contains("describeimage")) {
                                questions = Utilities.getFilesFromServer(url, ".jpg,.png,.bmp", "*.*");
                            } else {
                                questions = Utilities.getFileLine(url + "/" + dataSource + ".txt");
                            }
                            if (questions == null) maxLength = -1;
                            else maxLength = questions.length;
                            if (audioFiles != null)
                                if (maxLength < audioFiles.length) maxLength = audioFiles.length;
                            needUpdateIdx = 0;
                            Message message = mHandler.obtainMessage(1, "Update Questions List");
                            message.sendToTarget();
                            //Update firt hit
                            Utilities.loadFirstHit(String.format("%s_%s_", spinPart.getSelectedItem().toString().replace(" ", ""),
                                    dataSource), maxLength);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        spinQuestionsList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!choseRandomly) fileIdx = spinQuestionsList.getSelectedItemPosition();
                if (spinQuestionsList.getSelectedItemPosition() > 0) needUpdateIdx++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        mHandler = new Handler(android.os.Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 0:
                        bStart.performClick();
                        break;
                    case 1:
                        updateQuestionsList();
                        break;
                    case 2:
                        bRepeat.performClick();
                        break;
                    case 3:
                        Toast.makeText(getApplicationContext(), "Loading timeout!", Toast.LENGTH_SHORT).show();
                        bStart.performClick();
                        break;
                    case 4:
                        try {
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            Utilities.clearFirstHit();
                                            bStart.performClick();
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            break;
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder((Context) message.obj);
                            builder.setMessage("All GOOD, do you want to restart all question?").setPositiveButton("Yes", dialogClickListener)
                                    .setNegativeButton("No", dialogClickListener).show();
                        } catch (Exception ex) {
                            Log.d("MAIN", ex.toString());
                        }
                        break;
                    case 5: //Update the question list
                        spinQuestionsList.setSelection((int) message.obj);
                        break;
                    case 6: //Update the first hit result
                        Utilities.saveFirstHit(String.format("%s_%s_%04d", spinPart.getSelectedItem().toString().replace(" ", ""),
                                spinCat.getSelectedItem().toString(), fileIdx), ((float) message.obj) / 100);
                        break;
                }
            }
        };

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private int currentPractice(String practiceUrl) {
        int value = 0;
        try {
//            practised.clear();
            String currentQ = Utilities.getFileContent(practiceUrl);
            value = Integer.parseInt(Utilities.getNumberOnly(currentQ));
        } catch (Exception ex) {
            Log.d("MAIN", ex.toString());
        }
        return value;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void speakOut() {
        tvQuestion.setText("");
        etAnswer.setText("");
        if (dataSource.startsWith("TU") || audioFiles[fileIdx].contains(".txt")) {
            Bundle bd = new Bundle();
            bd.putInt(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, fileIdx);
            String text = audioFiles[fileIdx];
            if (audioFiles[fileIdx].contains(".txt"))
                text = Utilities.getFileContent(audioFiles[fileIdx]);
            if (url.contains("answershortquestion"))
                text = audioFiles[fileIdx].split("-")[0].trim();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, bd, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
            if (url.contains("summaryspokentext")) {
                String topic = audioFiles[fileIdx];
                topic = topic.substring(topic.lastIndexOf('_') + 1, topic.lastIndexOf('.'));
                tvQuestion.setText(topic);
                etAnswer.setText(text);
            }
        } else {
            if (proxy == null) proxy = App.getProxy(this);
            progDialog = ProgressDialog.show(this, "Please wait ...", "Retrieving data ...", true);
            progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            final Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        String audioUrl = proxy.getProxyUrl(audioFiles[fileIdx]);
//                        isError = true;
                        Utilities.playAudio(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer mp) {
                                                    if (visitOnly) {
                                                        new Timer().schedule(new TimerTask() {
                                                            @Override
                                                            public void run() {
                                                                Message message = mHandler.obtainMessage(0, "");
                                                                message.sendToTarget();
                                                            }
                                                        }, 3000);
                                                    } else {
                                                        if (!url.contains("dictation") && !url.contains("summaryspokentext"))
                                                            Utilities.promptSpeechInput(thisAct, "");
                                                    }
                                                }
                                            }, new MediaPlayer.OnPreparedListener() {
                                                @Override
                                                public void onPrepared(MediaPlayer mp) {
//                                                    isError = false;
                                                    progDialog.dismiss();
                                                    timeout.cancel();
                                                }
                                            }, new MediaPlayer.OnErrorListener() {
                                                @Override
                                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                                    progDialog.dismiss();
                                                    timeout.cancel();
                                                    Toast.makeText(getApplicationContext(), "Loading error!", Toast.LENGTH_SHORT).show();
                                                    return false;
                                                }
                                            }
                                , audioUrl);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
            //Schedule the timeout
            timeout = new Timer();
            timeout.schedule(new TimerTask() {
                @Override
                public void run() {
                    progDialog.dismiss();
                    try {
                        thread.interrupt();
                        thread.join(1000);
                        if (surfMode) {
                            Message message = mHandler.obtainMessage(3, "");
                            message.sendToTarget();
                        }
                    } catch (Exception ex) {
                        Log.d("MAIN", ex.toString());
                    }
                }
            }, 10000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utilities.REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String text = "";
                    for (int i = 0; i < result.size(); i++)
                        if (result.get(i).length() > text.length())
                            text = result.get(i);
                    etAnswer.setText(text);
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
            if (tmp.length > 1) question = Utilities.getWordOnly(tmp[1].trim());
            else question = "";
        }
        float res = StringSimilarity.wordsSimilarity(question.toLowerCase(),
                etAnswer.getText().toString().toLowerCase());
        tvResult.setText("Result: " + String.format("%.1f", res) + "%");
        //Update the first hit result
        Message message = mHandler.obtainMessage(6, res);
        message.sendToTarget();
        if (res >= accurateRate) {
            try {
                totalPractised++;
                String updateResult = Utilities.getFileContent(practiceUrl + "&q=" + String.valueOf(totalPractised));
                Log.d("MAIN-UPDATE RESULT", updateResult);
                //Notify the achievement
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (surfMode && focusAccuracy) { //Repeat this question.
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Message message = mHandler.obtainMessage(2, "");
                    message.sendToTarget();
                }
            }, 3000);
            return;
        }
        //Should we move to the next question
        if (surfMode) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Message message = mHandler.obtainMessage(0, "");
                    message.sendToTarget();
                }
            }, 3000);
        }
    }

    private void updateDataCategory(String url) {
        String[] files = Utilities.getFilesFromServer(url + "/index.php?cat=true", "", "*.*");
        List<String> spinnerArray = Arrays.asList(files);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCat.setAdapter(adapter);
        etAnswer.setText("");
    }

    private void updateQuestionsList() {
        List<String> spinnerArray = new ArrayList<String>();
        for (int i = 0; i < maxLength; i++) spinnerArray.add(String.format("%03d", i + 1));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinQuestionsList.setAdapter(adapter);
        spinQuestionsList.setSelection(-1);
    }
}

