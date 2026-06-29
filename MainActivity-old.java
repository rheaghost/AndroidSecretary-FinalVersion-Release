//as os 2601091843

package com.example.aisecretary3;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//these three required for simpledataformat 2601090220
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

//speech related
//import android.speech.RecognitionListener;
//import android.speech.RecognizerIntent;
//import android.speech.SpeechRecognizer;
//import android.content.Intent;
//import java.util.ArrayList;
//import java.util.concurrent.TimeUnit;
//import android.graphics.PorterDuff; // Needed for the status dot color filter

//mic permission handshake related
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import android.content.pm.PackageManager;
//import android.Manifest;

//tts related
import android.speech.tts.TextToSpeech;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    // 1. Declare variables at the top
    private TextView statusDisp;
    private TextView txtAiOutput;
    private EditText editMsg;
    private Button btnSend;
    private SpeechRecognizer speechRecognizer;

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


         */

        // Initialize your "VB-style" Database Helper
        SecretaryDbHelper db = new SecretaryDbHelper(this);

        // Link UI to Code
        TextView battDisp = findViewById(R.id.txt_batt);
        Button runBtn = findViewById(R.id.btn_send);
        // 2. Initialize them (Link to your XML IDs)
        statusDisp = findViewById(R.id.txt_status);
        txtAiOutput = findViewById(R.id.chat_history_text); // Or whatever ID you used
        editMsg = findViewById(R.id.edit_msg);
        //btnSend = findViewById(R.id.btn_send);
        //btnSend->runbtnb
        txtAiOutput.setMovementMethod(new ScrollingMovementMethod());
        runBtn.setOnClickListener(v -> {
            // Your logic to check local vs PC and run Ollama
            //runAILogic(editMsg.toString());//ggl android edittext to string 2601090944
            String userMsg = editMsg.getText().toString();
            if (!userMsg.isEmpty()) {
                // Option A: Real Run (commented out for now)
                // runAILogic(userMsg);

                // Option B: Mock Run (use this to test the UI)
                simulateAiResponse(userMsg);
            }

        });

        //stop tts output by long click
        runBtn.setOnLongClickListener(v -> {
            if (tts != null && tts.isSpeaking()) {
                tts.stop();
                statusDisp.setText("STATUS: AI SILENCED");
            }
            return true;
        });

        LinearLayout metricsBar = findViewById(R.id.metrics_bar);

        metricsBar.setOnLongClickListener(v -> {
            // 1. Create a confirmation popup (Safety first!)
            new AlertDialog.Builder(this)
                    .setTitle("Wipe Memory?")
                    .setMessage("Do you want to clear all chat logs?")
                    .setPositiveButton("Wipe", (dialog, which) -> {
                        clearAllHistory(); // The method we wrote earlier
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true; // Tells Android we handled the click
        });


        loadHistoryFromDatabase();

        //mic related
        //private SpeechRecognizer speechRecognizer;

// Inside onCreate:
        ImageButton btnMic = findViewById(R.id.btn_mic);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Optional: If you talk about K-Pop or specific Korean names often,
        // you can try adding Korean as a secondary language hint
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        // Optional: If you talk about K-Pop or specific Korean names often,
         // you can try adding Korean as a secondary language hint
        //speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        btnMic.setOnClickListener(v -> {
            statusDisp.setText("STATUS: LISTENING...");
            speechRecognizer.startListening(speechIntent);
        });

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (data != null) {
                    String spokenText = data.get(0);
                    editMsg.setText(spokenText); // Put voice into the text box
                    simulateAiResponse(spokenText); // Auto-trigger the AI!
                }
            }
            // Implement other empty methods (onReadyForSpeech, onError, etc.)
            @Override public void onError(int error) { statusDisp.setText("STATUS: MIC ERROR"); }
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });


        //end mic related
         //mic permission popup
        // Check for Microphone Permission at startup
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
        //end mic permission poipup

        //tts
        /*
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US); // Or Locale.KOREA if you want a Korean accent!
            }
        });
        */
        /*2nd try
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                // Optional: tts.setPitch(1.1f);
            } else {
                runOnUiThread(() -> statusDisp.setText("STATUS: TTS INIT FAILED"));
            }
        });

         */

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Get the result of setting the language
                int result = tts.setLanguage(Locale.getDefault()); // Try default first

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    runOnUiThread(() -> statusDisp.setText("STATUS: TTS VOICE MISSING"));
                } else {
                    runOnUiThread(() -> statusDisp.setText("STATUS: TTS READY"));
                }
            } else {
                runOnUiThread(() -> statusDisp.setText("STATUS: TTS INIT FAILED"));
            }
        });



        //end tts


    }//onCreate

    public float getBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        // Passing 'null' for the receiver returns the current status immediately
        Intent batteryStatus = this.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return (level / (float)scale) * 100;
    }
    private Handler handler = new Handler();
    private Runnable batteryMonitor = new Runnable() {
        @Override
        public void run() {
            // 1. Get the level using the method we just fixed
            float level = getBatteryLevel();

            // 2. Update the UI text
            TextView battDisp = findViewById(R.id.txt_batt);
            battDisp.setText("BATT: " + (int)level + "%");

            // 3. Safety Check: If battery is low, change color to red
            if (level < 20) {
                battDisp.setTextColor(Color.RED);
            } else {
                battDisp.setTextColor(Color.GREEN);
            }
            /*
            // Inside the run() method above
            if (level < 25 && isAiRunning) {
                statusDisp.setText("STATUS: LOW POWER / THROTTLING");
                // Maybe lower the num_threads here?
            }
            */

            // 4. Repeat this every 30,000 milliseconds (30 seconds)
            handler.postDelayed(this, 30000);
        }
    };

    // Start the monitor when the app opens
    @Override
    protected void onResume() {
        super.onResume();
        handler.post(batteryMonitor);
    }

    // Stop the monitor when the app closes to save battery
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(batteryMonitor);
    }

    private void saveToDatabase(String prompt, String reply) {
        SecretaryDbHelper db = new SecretaryDbHelper(this);

        // We capture current metrics for the "6-digit" log
        float currentBatt = getBatteryLevel();
        //double tps = calculateTPS(); // You can implement a simple timer here
        double tps=0.0;

        db.saveLog("Secretary", prompt, reply, null, tps, "STABLE");
    }

    private void runAILogic(String userPrompt) {
        OkHttpClient client = new OkHttpClient();

        // Use your ngrok URL for high performance, or localhost for outdoors
        String targetUrl = "http://localhost:11434/api/generate";

        // Inside runAILogic
        String systemPrefix = "You are a professional AI Secretary. Be concise and helpful. ";
        String combinedPrompt = systemPrefix + "User says: " + userPrompt;

        // Use combinedPrompt instead of userPrompt in the JSON string
        String json = "{\"model\": \"llama3\", \"prompt\": \"" + combinedPrompt + "\", \"stream\": false}";


        // Prepare the JSON (similar to what you did in JS)
        //String json = "{\"model\": \"llama3\", \"prompt\": \"" + userPrompt + "\", \"stream\": false}";

        RequestBody body = RequestBody.create(
                json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(targetUrl)
                .post(body)
                .build();

        // Run the request in a background thread so the UI doesn't freeze
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> statusDisp.setText("STATUS: CONNECTION FAILED"));
            }

            /*
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String aiReply = response.body().string();

                    // Parse the JSON and save to your "VB-style" Database
                    runOnUiThread(() -> {
                        String time = getCurrentTime();
                        String formattedText = "[" + time + "] Secretary: " + aiReply;

                        // This updates the TextView you placed between the Recycler and Input area
                        txtAiOutput.append("\n" + formattedText);

                        txtAiOutput.setText(aiReply);
                        saveToDatabase(userPrompt, aiReply);
                    });

                    //tts speech outrput

                    runOnUiThread(() -> {
                        //String finalMessage = "..."; // Your AI reply
                        txtAiOutput.append("\nSecretary: " + aiReply);

                        // THE VOICE: Speak the reply out loud
                        tts.speak(aiReply, TextToSpeech.QUEUE_FLUSH, null, null);
                    });

                }
            }//onResponse
            */

            /*onResponse 2nd try
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String rawJson = response.body().string();

                    try {
                        // 1. Extract ONLY the text from the JSON
                        JSONObject jsonObject = new JSONObject(rawJson);
                        final String cleanReply = jsonObject.getString("response");

                        runOnUiThread(() -> {
                            String time = getCurrentTime();

                            // 2. Update UI with the clean text
                            txtAiOutput.append("\n[" + time + "] Secretary: " + cleanReply);

                            // 3. Save to DB
                            saveToDatabase(userPrompt, cleanReply);

                            // 4. THE VOICE: Speak the clean text
                            if (tts != null) {

                                //tts.speak(cleanReply, TextToSpeech.QUEUE_FLUSH, null, null);
                                tts.speak(cleanReply, TextToSpeech.QUEUE_FLUSH, null, "SecretaryID");
                            }

                            statusDisp.setText("STATUS: STABLE");
                        });

                    } catch (JSONException e) {
                        runOnUiThread(() -> statusDisp.setText("STATUS: PARSE ERROR"));
                    }
                }
            }//onResponse


             */

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String rawJson = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(rawJson);
                        final String cleanReply = jsonObject.getString("response");

                        runOnUiThread(() -> {
                            String time = getCurrentTime();
                            txtAiOutput.append("\n[" + time + "] Secretary: " + cleanReply);
                            saveToDatabase(userPrompt, cleanReply);

                            // Add a small check to see if it's speaking
                            int speakStatus = tts.speak(cleanReply, TextToSpeech.QUEUE_FLUSH, null, "MsgID");
                            if (speakStatus == TextToSpeech.ERROR) {
                                statusDisp.setText("STATUS: TTS ERROR");
                            }
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> statusDisp.setText("STATUS: JSON ERROR"));
                    }
                }
            }//onResponse

        });
    }//runAILogic

    private void clearAllHistory() {
        SecretaryDbHelper dbHelper = new SecretaryDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Deletes everything in the chat_history table
        db.delete("chat_history", null, null);
        db.close();

        // Refresh your UI
        txtAiOutput.setText("");
        // If using a list, clear your adapter too!
        runOnUiThread(() -> statusDisp.setText("STATUS: HISTORY CLEARED"));
    }//clearAllHistory

    private String getCurrentTime() {
        // Format: "HH:mm" (e.g., 14:30) or "MM-dd HH:mm" for date+time
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }//getCurrentTime

    // TEST METHOD: Simulates the AI response
    private void simulateAiResponse(String userPrompt) {
        statusDisp.setText("STATUS: MOCKING...");

        // Create a 1-second delay so it feels real
        new Handler().postDelayed(() -> {
            String mockReply = "Hello! I am your Secretary. I received your message: " + userPrompt;
            String time = getCurrentTime();

            // Update the Live Output TextView
            txtAiOutput.append("\n[" + time + "] Secretary: " + mockReply);

            // Test the Database Save
            saveToDatabase(userPrompt, mockReply);

            if (tts != null) {
                tts.speak(mockReply, TextToSpeech.QUEUE_FLUSH, null, "MockID");
                // Vibrate for 100 milliseconds
                ((android.os.Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(100);
            }

            statusDisp.setText("STATUS: MOCK SUCCESS");
            editMsg.setText(""); // Clear input
        }, 1000);
    }//simulateairesponse

    private void loadHistoryFromDatabase() {
        SecretaryDbHelper dbHelper = new SecretaryDbHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query the table: Select everything from chat_history
        Cursor cursor = db.rawQuery("SELECT prompt, response, timestamp FROM chat_history", null);

        StringBuilder historyBuilder = new StringBuilder();

        if (cursor.moveToFirst()) {
            do {
                // Get data from columns (Index 0 is prompt, 1 is response, 2 is timestamp)
                String prompt = cursor.getString(0);
                String response = cursor.getString(1);
                String time = cursor.getString(2);

                // Format it just like your Live messages
                historyBuilder.append("\n[")
                        .append(time)
                        .append("] Me: ")
                        .append(prompt)
                        .append("\n[")
                        .append(time)
                        .append("] Secretary: ")
                        .append(response)
                        .append("\n");

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        // Set the text to the UI
        txtAiOutput.setText(historyBuilder.toString());
    }//loadhistoryfromdatabase

    /*
    private void checkPCConnection() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS) // Don't wait too long
                .build();

        Request request = new Request.Builder()
                .url("https://your-ngrok-url.ngrok-free.app") // Your PC address
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    findViewById(R.id.status_dot).getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                    statusDisp.setText("OUTDOOR MODE");
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    findViewById(R.id.status_dot).getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                    statusDisp.setText("PC POWERED");
                });// Inside your checkPCConnection() onResponse or onFailure
                runOnUiThread(() -> {
                    View dot = findViewById(R.id.status_dot);
                    android.graphics.drawable.GradientDrawable background =
                            (android.graphics.drawable.GradientDrawable) dot.getBackground();

                    if (isPcOnline) {
                        background.setColor(Color.GREEN);
                        statusDisp.setText("PC POWERED");
                    } else {
                        background.setColor(Color.RED);
                        statusDisp.setText("OUTDOOR MODE");
                    }
                });


            }
        });
    }//checkpcconnection


     */
    private void checkPCConnection() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .build();

        // Use your specific ngrok or local IP address
        Request request = new Request.Builder().url("http://192.168.0.7:11434").build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> updateStatusDot(Color.RED, "OUTDOOR MODE"));
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> updateStatusDot(Color.GREEN, "PC POWERED"));
            }
        });
    }//checkPCConnection

    // Helper to keep code clean
    private void updateStatusDot(int color, String statusText) {
        View dot = findViewById(R.id.status_dot);
        TextView statusDisp = findViewById(R.id.txt_status);

        // Get the background (our status_circle.xml) and cast it to a GradientDrawable
        android.graphics.drawable.GradientDrawable background =
                (android.graphics.drawable.GradientDrawable) dot.getBackground();

        if (background != null) {
            background.setColor(color);
        }

        statusDisp.setText(statusText);
    }









}