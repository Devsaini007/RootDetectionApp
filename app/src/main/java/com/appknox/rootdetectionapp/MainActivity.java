package com.appknox.rootdetectionapp;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.scottyab.rootbeer.RootBeer;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private TextView rootStatusText;
    private EditText userInput;
    private Button submitButton;
    private TextView displayText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        rootStatusText = findViewById(R.id.root_status_text);
        userInput = findViewById(R.id.user_input);
        submitButton = findViewById(R.id.submit_button);
        displayText = findViewById(R.id.display_text);

        // Check root status and update UI
        if (isRooted()) {
            rootStatusText.setText("This device is ROOTED.");
        } else {
            rootStatusText.setText("This device is NOT ROOTED.");

            // Show user input field and button if not rooted
            userInput.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.VISIBLE);

            // Handle button click
            submitButton.setOnClickListener(view -> {
                String input = userInput.getText().toString();
                displayText.setText("You entered: " + input);
                displayText.setVisibility(View.VISIBLE);

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            });
        }
    }

    /**
     * Checks if the device is rooted using a combination of RootBeer and custom checks.
     */
    private boolean isRooted() {
        // RootBeer detection
        RootBeer rootBeer = new RootBeer(this);
        if (rootBeer.isRooted()) {
            return true;
        }

        // Custom checks
        return checkForSuBinary() || checkForRootManagementApps() || checkForDangerousProperties();
    }

    /**
     * Check for the presence of the su binary.
     */
    private boolean checkForSuBinary() {
        String[] paths = {
                "/system/xbin/su",
                "/system/bin/su",
                "/system/su",
                "/data/local/bin/su",
                "/data/local/su",
                "/sbin/su"
        };

        for (String path : paths) {
            if (new File(path).exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for root management apps.
     */
    private boolean checkForRootManagementApps() {
        String[] rootApps = {
                "com.noshufou.android.su",        // Superuser
                "com.koushikdutta.superuser",     // Koush Superuser
                "eu.chainfire.supersu",          // SuperSU
                "com.topjohnwu.magisk"           // Magisk
        };

        for (String packageName : rootApps) {
            try {
                getPackageManager().getPackageInfo(packageName, 0);
                return true; // App is installed
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    /**
     * Check for dangerous system properties indicating root.
     */
    private boolean checkForDangerousProperties() {
        String[] dangerousProperties = {
                "ro.debuggable",
                "ro.secure"
        };

        for (String property : dangerousProperties) {
            String value = getSystemProperty(property);
            if ("1".equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method to get system property.
     */
    private String getSystemProperty(String propertyName) {
        String value = "";
        try {
            Process process = Runtime.getRuntime().exec("getprop " + propertyName);
            byte[] buffer = new byte[1024];
            int bytesRead = process.getInputStream().read(buffer);
            value = new String(buffer, 0, bytesRead).trim();
        } catch (Exception ignored) {
        }
        return value;
    }
}