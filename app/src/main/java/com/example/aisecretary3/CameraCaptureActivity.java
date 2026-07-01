package com.example.aisecretary3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;
public class CameraCaptureActivity extends AppCompatActivity {
    private String currentAvatar="Default";
    private static final int CAMERA_REQ = 100;
    private ImageView imagePreview;
    private Bitmap capturedPhoto;
    private String llavaResult = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        currentAvatar = getIntent().getStringExtra("current_avatar");
        if (currentAvatar == null || currentAvatar.isEmpty()) {
            currentAvatar = "Default";
        }

        // Create layout programmatically
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setGravity(android.view.Gravity.CENTER);

        // Image preview
        imagePreview = new ImageView(this);
        imagePreview.setLayoutParams(new LinearLayout.LayoutParams(400, 400));
        imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imagePreview.setBackgroundColor(0xFF1E1E1E);
        layout.addView(imagePreview);

        // Take Photo button
        Button btnTake = new Button(this);
        btnTake.setText("📷 TAKE PHOTO");
        btnTake.setTextColor(0xFF00FF41);
        btnTake.setBackgroundColor(0xFF004411);
        btnTake.setPadding(50, 30, 50, 30);
        btnTake.setTextSize(18);
        btnTake.setOnClickListener(v -> takePhoto());
        layout.addView(btnTake);

        // Describe & Return button
        Button btnConfirm = new Button(this);
        btnConfirm.setText("🧠 DESCRIBE & RETURN");
        btnConfirm.setTextColor(0xFF00FF41);
        btnConfirm.setBackgroundColor(0xFF004411);
        btnConfirm.setPadding(50, 30, 50, 30);
        btnConfirm.setTextSize(18);
        btnConfirm.setOnClickListener(v -> describeAndReturn());
        layout.addView(btnConfirm);

        setContentView(layout);
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQ);
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQ);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQ && resultCode == RESULT_OK) {
            capturedPhoto = (Bitmap) data.getExtras().get("data");
            imagePreview.setImageBitmap(capturedPhoto);
            Toast.makeText(this, "📸 Photo taken! Tap DESCRIBE & RETURN", Toast.LENGTH_LONG).show();
        }
    }

    private void describeAndReturn() {
        if (capturedPhoto == null) {
            Toast.makeText(this, "Take a photo first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 Show a progress dialog
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("🧠 LLaVA is analyzing the image...\n(Can take 30-60 seconds)");
        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            // Convert image to base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            capturedPhoto.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            // Build JSON using JSONObject
            JSONObject payload = new JSONObject();
            payload.put("model", "llava");

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", "What do you see in this image?");

            JSONArray images = new JSONArray();
            images.put(base64Image);
            userMessage.put("images", images);

            messages.put(userMessage);
            payload.put("messages", messages);
            payload.put("stream", false);

            String json = payload.toString();

            // Send to Ollama with LONG timeouts
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(180, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .build();

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    json
            );

            Request request = new Request.Builder()
                    .url("http://127.0.0.1:11434/api/chat")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        llavaResult = "❌ Connection Error: " + e.getMessage();
                        returnResult();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String rawJson = "";

                    try {
                        rawJson = response.body().string();
                        android.util.Log.d("LLAVA_RESPONSE", "Raw JSON: " + rawJson);

                        JSONObject jsonObject = new JSONObject(rawJson);
                        String content = "";

                        if (jsonObject.has("message")) {
                            JSONObject messageObj = jsonObject.getJSONObject("message");
                            if (messageObj.has("content")) {
                                content = messageObj.getString("content");
                            }
                        } else if (jsonObject.has("response")) {
                            content = jsonObject.getString("response");
                        } else if (jsonObject.has("error")) {
                            content = "❌ Error: " + jsonObject.getString("error");
                        } else {
                            content = "Raw response: " + rawJson;
                        }

                        llavaResult = content;
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            returnResult();
                        });

                    } catch (Exception e) {
                        android.util.Log.e("LLAVA_ERROR", "Parsing error: " + e.getMessage());
                        llavaResult = "❌ Parsing error: " + e.getMessage() + "\nRaw response: " + rawJson;
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            returnResult();
                        });
                    }
                }
            });

        } catch (Exception e) {
            android.util.Log.e("LLAVA_ERROR", "Build error: " + e.getMessage());
            progressDialog.dismiss();
            llavaResult = "❌ Build error: " + e.getMessage();
            returnResult();
        }
    }

    private void returnResultOld() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("AI_CAPTURE_RESULT", "🖼️ LLaVA says: " + llavaResult);
        resultIntent.putExtra("AI_USED_MODEL", "llava");
        //return the avatar
        resultIntent.putExtra("avatar_used",currentAvatar);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    private void returnResultX() {
        // 🔥 Call the static method with the current context
        MainActivity.receiveCameraResult(
                this,  // ← Context
                llavaResult,
                "llava",
                currentAvatar
        );

        // Show a toast
        Toast.makeText(this, "✅ Image analyzed and saved!", Toast.LENGTH_SHORT).show();

        // Finish the activity
        finish();
    }

    private void returnResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("AI_CAPTURE_RESULT", llavaResult);
        resultIntent.putExtra("AI_USED_MODEL", "llava");
        resultIntent.putExtra("avatar_used", currentAvatar);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int request, String[] perms, int[] results) {
        super.onRequestPermissionsResult(request, perms, results);
        if (request == CAMERA_REQ && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            takePhoto();
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
        }
    }
}