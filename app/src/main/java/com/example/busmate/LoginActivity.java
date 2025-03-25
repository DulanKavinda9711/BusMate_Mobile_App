package com.example.busmate;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 101010;
    private static final String TAG = "LoginActivity";

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;
    private EditText loginEmail, loginPassword;
    private TextView signupRedirectText, forgotPassword;
    private Button loginButton;
    private ImageView fingerprintButton;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI Elements
        initializeUI();

        // Configure password visibility toggle
        setupPasswordToggle();

        // Set up shared preferences
        setupSharedPreferences();

        // Set up click listeners
        setupClickListeners();

        // Initialize biometric authentication
        initializeBiometricAuthentication();
    }

    private void initializeUI() {
        // Firebase Initialization
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        // UI Elements
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signUpRedirectText);
        forgotPassword = findViewById(R.id.forgot_password);
        fingerprintButton = findViewById(R.id.fingerprint_button);
    }

    private void setupPasswordToggle() {
        EditText passwordField = findViewById(R.id.login_password);
        ImageView passwordToggle = findViewById(R.id.password_toggle);

        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Hide the password
                passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_visibility_off);
                isPasswordVisible = false;
            } else {
                // Show the password
                passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordToggle.setImageResource(R.drawable.ic_visibility_off);
                isPasswordVisible = true;
            }
            passwordField.setSelection(passwordField.getText().length()); // Move cursor to the end
        });
    }

    private void setupSharedPreferences() {
        // Shared Preferences
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        boolean isLogin = sharedPreferences.getBoolean("isLogin", false);
        fingerprintButton.setVisibility(isLogin ? View.VISIBLE : View.GONE);
    }

    private void setupClickListeners() {
        // Login Button Listener
        loginButton.setOnClickListener(v -> {
            if (validateLoginFields()) {
                String email = loginEmail.getText().toString().trim();
                String pass = loginPassword.getText().toString().trim();
                performAuth(email, pass);
            }
        });

        // Redirect to Signup
        signupRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        // Forgot Password Listener
        forgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private boolean validateLoginFields() {
        String email = loginEmail.getText().toString().trim();
        String pass = loginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            loginEmail.setError("Email cannot be empty");
            loginEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.setError("Please enter a valid email");
            loginEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(pass)) {
            loginPassword.setError("Password cannot be empty");
            loginPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performAuth(String email, String pass) {
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserCredentials(email, pass);
                                getUserType(user.getUid());
                            } else {
                                Toast.makeText(LoginActivity.this, "Authentication failed: User is null", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                            Toast.makeText(LoginActivity.this, "Login Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserCredentials(String email, String pass) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", pass);
        editor.putBoolean("isLogin", true);
        editor.apply();
    }

    private void getUserType(String userId) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("Users").child(userId).child("userType");

        // Read the user role from the database
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userType = dataSnapshot.getValue(String.class);
                if (userType != null) {
                    handleSuccessfulLogin(userType);
                } else {
                    // Default to "Passenger" if user type not found
                    handleSuccessfulLogin("Passenger");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch user type", databaseError.toException());
                Toast.makeText(LoginActivity.this, "Failed to fetch user role", Toast.LENGTH_SHORT).show();
                // Default to "Passenger" in case of an error
                handleSuccessfulLogin("Passenger");
            }
        });
    }

    private void handleSuccessfulLogin(String userType) {
        // Save the role in shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userType", userType);
        editor.apply();

        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

        // Check user role and navigate accordingly
        Intent intent;
        if ("Conductor".equals(userType)) {
            intent = new Intent(LoginActivity.this, ConductorMainActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, PassengerMainActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void initializeBiometricAuthentication() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "App can authenticate using biometrics.");
                setupBiometricPrompt();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "Fingerprint sensor not available", Toast.LENGTH_SHORT).show();
                fingerprintButton.setVisibility(View.GONE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Sensor not available or busy", Toast.LENGTH_SHORT).show();
                fingerprintButton.setVisibility(View.GONE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                promptBiometricEnrollment();
                fingerprintButton.setVisibility(View.GONE);
                break;
            default:
                fingerprintButton.setVisibility(View.GONE);
                break;
        }
    }

    private void setupBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                String email = sharedPreferences.getString("email", "");
                String pass = sharedPreferences.getString("password", "");

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {
                    performAuth(email, pass);
                } else {
                    Toast.makeText(LoginActivity.this, "No saved credentials. Please login with email and password",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setSubtitle("Use your fingerprint to log in")
                .setNegativeButtonText("Cancel")
                .build();

        fingerprintButton.setOnClickListener(view -> biometricPrompt.authenticate(promptInfo));
    }

    private void promptBiometricEnrollment() {
        Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
        enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
        startActivityForResult(enrollIntent, REQUEST_CODE);
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot, null);
        EditText emailBox = dialogView.findViewById(R.id.emailBox);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnReset).setOnClickListener(v -> {
            String userEmail = emailBox.getText().toString().trim();

            if (TextUtils.isEmpty(userEmail) || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                Toast.makeText(LoginActivity.this, "Enter your registered email", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Check your email", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Toast.makeText(LoginActivity.this, "Unable to send reset email: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        dialog.show();
    }
}