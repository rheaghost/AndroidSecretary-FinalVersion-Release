package com.example.aisecretary3;
import android.content.BroadcastReceiver;//ok step 5 260629
import android.content.Context; //step 5
import android.content.Intent; //ok step 5
import android.content.IntentFilter;//ok step 5
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;//ok step 5

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatFragment extends Fragment {

    private TextView chatHistoryView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        chatHistoryView = v.findViewById(R.id.chat_history_text);

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.chatHistoryView = chatHistoryView;
            activity.chatHistoryView.setMovementMethod(new android.text.method.ScrollingMovementMethod());
            activity.loadHistoryFromDatabase();

            activity.chatHistoryView.post(() -> {
                activity.chatHistoryView.scrollTo(0, activity.lastScrollPos);
            });

            activity.btnScrollDown = v.findViewById(R.id.btn_scroll_down);
            activity.btnScrollDown.setOnClickListener(view -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).scrollToBottom();
                }
                activity.btnScrollDown.setVisibility(View.GONE);
            });
        }

        // 🔥 Register the broadcast receiver
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                cameraResultReceiver,
                new IntentFilter("CAMERA_RESULT")
        );

        return v;
    }

    public void appendCameraResultOld(String result, String model) {
        if (chatHistoryView != null) {
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            chatHistoryView.append("\n[" + time + "] Me: 📸 Photo captured\n");
            chatHistoryView.append("[" + time + "] " + model.toUpperCase() + ": " + result + "\n\n");

            if (chatHistoryView.getParent() instanceof ScrollView) {
                ((ScrollView) chatHistoryView.getParent()).fullScroll(View.FOCUS_DOWN);
            }
        }
    }

    public void appendCameraResultOld1(String result, String model) {
        if (chatHistoryView != null) {
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            //get the current avatar name from mainactivity 26062919
            MainActivity activity = (MainActivity) getActivity();
            String avatarName = "Default";
            if (activity != null && activity.activeAvatar != null) {
                avatarName = activity.activeAvatar;
            }


            chatHistoryView.append("\n[" + time + "] Me: 📸 Photo captured\n");
            //chatHistoryView.append("[" + time + "] " + model.toUpperCase() + ": " + result + "\n\n");
            chatHistoryView.append("[" + time + "] " + avatarName + " (" + model.toUpperCase() + "): " + result + "\n\n");

            // 🔥 Backup save directly from ChatFragment to ensure database persistence
            //MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                activity.saveToDatabase(
                        activity.activeRole,
                        activity.activeAvatar,
                        "📸 Photo captured",
                        result,
                        model != null ? model : "llava",
                        0.0
                );
                android.util.Log.d("CAMERA_RESULT", "✅ Backup save from ChatFragment with avatar: " + activity.activeAvatar);
            }

            if (chatHistoryView.getParent() instanceof ScrollView) {
                ((ScrollView) chatHistoryView.getParent()).fullScroll(View.FOCUS_DOWN);
            }
        }
    }

    public void appendCameraResult(String result, String model) {  //26062919
        if (chatHistoryView != null) {
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // 🔥 Get the current avatar from MainActivity
            MainActivity activity = (MainActivity) getActivity();
            String avatarName = "Default";
            if (activity != null && activity.activeAvatar != null && !activity.activeAvatar.isEmpty()) {
                avatarName = activity.activeAvatar;
            }

            // 🔥 Append user message
            chatHistoryView.append("\n[" + time + "] Me: 📸 Photo captured\n");

            // 🔥 Append AI response with Avatar (Model): message
            chatHistoryView.append("[" + time + "] " + avatarName + " (" + model.toUpperCase() + "): " + result + "\n\n");

            // Backup save directly from ChatFragment
            if (activity != null) {
                activity.saveToDatabase(
                        activity.activeRole,
                        activity.activeAvatar,
                        "📸 Photo captured",
                        result,
                        model != null ? model : "llava",
                        0.0
                );
            }

            // Scroll to bottom
            if (chatHistoryView.getParent() instanceof ScrollView) {
                ((ScrollView) chatHistoryView.getParent()).fullScroll(View.FOCUS_DOWN);
            }
        }
    }

    // 🔥 Broadcast Receiver
    private final BroadcastReceiver cameraResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra("result");
            String model = intent.getStringExtra("model");
            if (result != null) {
                appendCameraResult(result, model != null ? model : "LLAVA");
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister the receiver
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(cameraResultReceiver);

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.chatHistoryView = null;
            activity.btnScrollDown = null;
        }
    }
}