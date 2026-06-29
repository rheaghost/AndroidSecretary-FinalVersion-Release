package com.example.aisecretary3;
//2606032207

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
        View rootView = inflater.inflate(R.layout.fragment_persona, container, false);

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

}//PersonaFragment