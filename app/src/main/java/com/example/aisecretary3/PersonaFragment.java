package com.example.aisecretary3;
//now ot works with dynamic content you can now scroll and select rhea 26062912324
//2606032207
/*
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;  // ✅ ADD THIS IMPORT
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PersonaFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_persona_old, container, false);

        RadioGroup personaGroup = rootView.findViewById(R.id.persona_group);
        TextView txtPrompt = rootView.findViewById(R.id.txt_current_prompt);

        // ✅ FIXED: Cast to RadioButton first
        RadioButton radioDefault = rootView.findViewById(R.id.radio_default);
        radioDefault.setChecked(true);



        personaGroup.setOnCheckedChangeListener((group, checkedId) -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity == null) return;

            //Handle ALL three options: Default, Bomi, Rhea
            if (checkedId == R.id.radio_default) {
                activity.activeAvatar = "Default";
                activity.systemPrompt = "You are a helpful AI assistant. You are friendly, knowledgeable, and respond in a warm and engaging manner.";
                txtPrompt.setText(activity.systemPrompt);
                activity.updateStatusBar(); // ✅ ADD THIS
            }


            if (checkedId == R.id.radio_bomi) {
                activity.activeAvatar = "Bomi";
                activity.systemPrompt = "You are Bomi, a crisp, professional secretary who knows best about accounting, financial ledgers, and corporate math. Keep answers concise and structured.";
                txtPrompt.setText(activity.systemPrompt);
                activity.updateStatusBar(); // ✅ ADD THIS 260629
            } else if (checkedId == R.id.radio_rhea) {
                activity.activeAvatar = "Rhea";
                activity.systemPrompt = "You are Rhea, an ethereal, ghostly librarian trapped between dimensions. You know everything about past historical data, hidden archives, and forgotten files. Speak with a slightly mysterious, poetic tone.";
                txtPrompt.setText(activity.systemPrompt);
                activity.updateStatusBar(); // ✅ ADD THIS 260629
            }
            else if (checkedId == R.id.radio_somi) {
                activity.activeAvatar = "Somi";
                activity.systemPrompt = "You are Somi, a generic secretary with expertise in sales,marketing,competitive strategy,entertainment.";
                txtPrompt.setText(activity.systemPrompt);
                activity.updateStatusBar(); // ✅ ADD THIS 260629
            }

            android.util.Log.d("ALIVE_CHECK", "👤 Persona switched to: " + activity.activeAvatar);
        });

        return rootView;
        }
*/


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PersonaFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 🚀 Create everything in code - no XML issues!

        // 1. Create ScrollView
        ScrollView scrollView = new ScrollView(getContext());
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setBackgroundColor(0xFF121212);
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(true);

        // 2. Create main container (renamed to myContainer)
        LinearLayout myContainer = new LinearLayout(getContext());
        myContainer.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        myContainer.setOrientation(LinearLayout.VERTICAL);
        myContainer.setPadding(16, 16, 16, 16);

        // 3. Title
        TextView title = new TextView(getContext());
        title.setText("SELECT SECRETARY PERSONA");
        title.setTextColor(0xFF00FF41);
        title.setTextSize(12);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(0, 0, 0, 16);
        myContainer.addView(title);

        // 4. RadioGroup
        RadioGroup radioGroup = new RadioGroup(getContext());
        radioGroup.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        radioGroup.setOrientation(RadioGroup.VERTICAL);

        // 5. Create RadioButtons programmatically
        String[][] personas = {
                {"Default", "(General Purpose Assistant)", "default_avatar"},
                {"Bomi", "(Accounting Expert)", "bomi_avatar"},
                {"Somi", "(General Marketing/Sales)", "somi_avatar"},
                {"Rhea", "(Ethereal Ghostly Librarian)", "rhea_avatar"}
        };

        int[] ids = {
                R.id.radio_default,
                R.id.radio_bomi,
                R.id.radio_somi,
                R.id.radio_rhea
        };

        for (int i = 0; i < personas.length; i++) {
            RadioButton rb = new RadioButton(getContext());
            rb.setId(ids[i]);
            rb.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            rb.setText(personas[i][0] + "\n" + personas[i][1]);
            rb.setTextColor(0xFF00FF41);
            rb.setTextSize(14);
            rb.setPadding(12, 8, 0, 8);

            // Get the drawable resource
            int drawableId = getResourceId(personas[i][2]);
            if (drawableId != 0) {
                rb.setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0);
            }
            rb.setCompoundDrawablePadding(16);
            rb.setGravity(android.view.Gravity.CENTER_VERTICAL);

            radioGroup.addView(rb);
        }

        // Set Default as checked
        RadioButton defaultRb = radioGroup.findViewById(R.id.radio_default);
        if (defaultRb != null) defaultRb.setChecked(true);

        myContainer.addView(radioGroup);

        // 6. System Prompt Section
        TextView promptLabel = new TextView(getContext());
        promptLabel.setText("ACTIVE SYSTEM PROMPT");
        promptLabel.setTextColor(0xFF004411);
        promptLabel.setTextSize(10);
        promptLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        promptLabel.setPadding(0, 16, 0, 4);
        myContainer.addView(promptLabel);

        TextView promptDisplay = new TextView(getContext());
        promptDisplay.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        promptDisplay.setBackgroundColor(0xFF1E1E1E);
        promptDisplay.setPadding(12, 12, 12, 12);
        promptDisplay.setText("Select a persona to load system prompt...");
        promptDisplay.setTextColor(0xFF00FF41);
        promptDisplay.setTextSize(12);
        myContainer.addView(promptDisplay);

        // 7. Add container to ScrollView
        scrollView.addView(myContainer);

        // 8. Setup listener
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity == null) return;

            String avatar;
            String prompt;

            if (checkedId == R.id.radio_default) {
                avatar = "Default";
                prompt = "You are a helpful AI assistant. You are friendly, knowledgeable, and respond in a warm and engaging manner.";
            } else if (checkedId == R.id.radio_bomi) {
                avatar = "Bomi";
                prompt = "You are Bomi, a crisp, professional secretary who knows best about accounting, financial ledgers, and corporate math. Keep answers concise and structured.";
            } else if (checkedId == R.id.radio_somi) {
                avatar = "Somi";
                prompt = "You are Somi, a generic secretary with expertise in sales, marketing, competitive strategy, entertainment.";
            } else if (checkedId == R.id.radio_rhea) {
                avatar = "Rhea";
                prompt = "You are Rhea, an ethereal, ghostly librarian trapped between dimensions. You know everything about past historical data, hidden archives, and forgotten files. Speak with a slightly mysterious, poetic tone.";
            } else {
                avatar = "Default";
                prompt = "You are a helpful AI assistant.";
            }

            activity.activeAvatar = avatar;
            activity.systemPrompt = prompt;
            promptDisplay.setText(prompt);
            activity.updateStatusBar();

            android.util.Log.d("ALIVE_CHECK", "👤 Persona switched to: " + avatar);
        });

        return scrollView;
    }

    private int getResourceId(String drawableName) {
        return getContext().getResources().getIdentifier(
                drawableName, "drawable", getContext().getPackageName());
    }
}