package com.example.busmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private DrawerLayout drawerLayout;
    private ImageView menu;
    private LinearLayout home, settings, share, about, logout;

    private TextInputEditText editUsername, editName, editEmail, editUserType;
    private Button btnSaveProfile;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // User not logged in, redirect to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        // Initialize UI components
        initUI();

        // Load current user data
        loadUserData();

        // Set up click listeners
        setupClickListeners();
    }

    private void initUI() {
        // Navigation components
        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);
        home = findViewById(R.id.home);
        settings = findViewById(R.id.settings);
        share = findViewById(R.id.share);
        about = findViewById(R.id.about);
        logout = findViewById(R.id.logout);

        // Profile edit components
        editUsername = findViewById(R.id.edit_username);
        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editUserType = findViewById(R.id.edit_user_type);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        // Populate UI with user data
                        editUsername.setText(user.getUsername());
                        editName.setText(user.getName());
                        editEmail.setText(user.getEmail());
                        editUserType.setText(user.getUserType());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading user data: " + error.getMessage());
                Toast.makeText(EditProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        // Navigation drawer listeners
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer(drawerLayout);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(EditProfileActivity.this, MainActivity.class);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(EditProfileActivity.this, SettingsActivity.class);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(EditProfileActivity.this, ShareActivity.class);
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(EditProfileActivity.this, AboutActivity.class);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditProfileActivity.this, "Logout", Toast.LENGTH_SHORT).show();
            }
        });

        // Save profile button listener
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });
    }

    private void saveUserProfile() {
        String username = editUsername.getText().toString().trim();
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String userType = editUserType.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(username)) {
            editUsername.setError("Username cannot be empty");
            editUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            editName.setError("Name cannot be empty");
            editName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Email cannot be empty");
            editEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Please enter a valid email");
            editEmail.requestFocus();
            return;
        }

        // Show progress or disable button
        btnSaveProfile.setEnabled(false);

        // Update email in Firebase Auth if changed
        if (!email.equals(currentUser.getEmail())) {
            currentUser.updateEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User email updated successfully");
                                updateUserDataInDatabase(username, name, email, userType);
                            } else {
                                btnSaveProfile.setEnabled(true);
                                Log.e(TAG, "Error updating email", task.getException());
                                Toast.makeText(EditProfileActivity.this, "Failed to update email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            // If email not changed, just update database
            updateUserDataInDatabase(username, name, email, userType);
        }
    }

    private void updateUserDataInDatabase(String username, String name, String email, String userType) {
        User updatedUser = new User(username, name, email, userType);

        userRef.setValue(updatedUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                btnSaveProfile.setEnabled(true);

                if (task.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    // Go back to settings
                    redirectActivity(EditProfileActivity.this, SettingsActivity.class);
                } else {
                    Log.e(TAG, "Error updating user data", task.getException());
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Navigation drawer helper methods (same as your SettingsActivity)
    public static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public static void redirectActivity(Activity activity, Class secondActivity) {
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDrawer(drawerLayout);
    }
}