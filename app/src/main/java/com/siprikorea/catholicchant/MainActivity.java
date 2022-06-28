package com.siprikorea.catholicchant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * Main Activity
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create ViewModel
        ChantViewModel model = new ViewModelProvider(this).get(ChantViewModel.class);
        model.getChantSheet().observe(this, chantSheet -> {
            ImageView imageView = findViewById(R.id.sheet);
            imageView.setImageBitmap(chantSheet);
        });

        // Number
        EditText number = findViewById(R.id.number);
        number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String number = charSequence.toString();
                model.load(number);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Play
        Button play = findViewById(R.id.play);
        play.setOnClickListener(view -> {
            model.play(number.getText().toString());

            hideKeyboard();
        });

        // Stop
        Button stop = findViewById(R.id.stop);
        stop.setOnClickListener(view -> model.stop());
    }

    /**
     * Hide keyboard
     */
    private void hideKeyboard() {
        View currentFocusedView = getCurrentFocus();
        if (currentFocusedView != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), 0);
        }
    }
}