package com.example.busmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private EditText signupEmail, signupPassword, signupUsername, signupName;
    private Button signupButton;
    private Spinner userTypeSpinner;
    private TextView loginRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase components
        initializeFirebase();

        // Initialize UI components
        initializeUI();

        // Set up the user type spinner
        setupUserTypeSpinner();

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        auth = FirebaseAuth.getInstance();
    }

    private void initializeUI() {
        signupName = findViewById(R.id.signup_name);
        signupUsername = findViewById(R.id.signup_username);
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        userTypeSpinner = findViewById(R.id.user_type_spinner);
    }

    private void setupUserTypeSpinner() {
        // Set up the Spinner with options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_types, R.layout.spinner_item);

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        userTypeSpinner.setAdapter(adapter);

        userTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0 && selectedItemView instanceof TextView) {
                    // Set hint text color (when it's still the default "Select User Type")
                    TextView hintText = (TextView) selectedItemView;
                    hintText.setTextColor(getResources().getColor(R.color.hintColor));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
    }

    private void setupClickListeners() {
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateSignupFields()) {
                    String email = signupEmail.getText().toString().trim();
                    String pass = signupPassword.getText().toString().trim();
                    String username = signupUsername.getText().toString().trim();
                    String name = signupName.getText().toString().trim();
                    String userType = userTypeSpinner.getSelectedItem().toString();

                    registerUser(email, pass, username, name, userType);
                }
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });
    }

    private boolean validateSignupFields() {
        String name = signupName.getText().toString().trim();
        String username = signupUsername.getText().toString().trim();
        String email = signupEmail.getText().toString().trim();
        String password = signupPassword.getText().toString().trim();
        String userType = userTypeSpinner.getSelectedItem().toString();

        // Validate input fields one by one
        if (TextUtils.isEmpty(name)) {
            signupName.setError("Name cannot be empty");
            signupName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(username)) {
            signupUsername.setError("Username cannot be empty");
            signupUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            signupEmail.setError("Email cannot be empty");
            signupEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signupEmail.setError("Please enter a valid email");
            signupEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            signupPassword.setError("Password cannot be empty");
            signupPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            signupPassword.setError("Password must be at least 6 characters");
            signupPassword.requestFocus();
            return false;
        }

        if (userType.equals("Select User Type")) {
            Toast.makeText(SignUpActivity.this, "Please select a user type", Toast.LENGTH_SHORT).show();
            userTypeSpinner.requestFocus();
            return false;
        }

        return true;
    }

    private void registerUser(String email, String password, String username, String name, String userType) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String userId = auth.getCurrentUser().getUid();
                    saveUserToDatabase(userId, username, name, email, userType);
                } else {
                    String errorMessage = "SignUp Failed";
                    if (task.getException() != null) {
                        errorMessage += ": " + task.getException().getMessage();
                        Log.e(TAG, "Error creating user", task.getException());
                    }
                    Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveUserToDatabase(String userId, String username, String name, String email, String userType) {
        // Save additional user details to Realtime Database
        User user = new User(username, name, email, userType);
        databaseReference.child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, "SignUp Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    finish();
                } else {
                    String errorMessage = "Failed to save user details";
                    if (task.getException() != null) {
                        errorMessage += ": " + task.getException().getMessage();
                        Log.e(TAG, "Error saving user data", task.getException());
                    }
                    Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}