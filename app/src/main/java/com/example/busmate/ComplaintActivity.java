package com.example.busmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ComplaintActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ImageView menu;
    LinearLayout home, settings, share, about, complaint, rating, logout;

    // UI components for complaint form
    private TextInputEditText nameInput, emailInput, busNumberInput, locationInput, descriptionInput;
    private Spinner complaintTypeSpinner;
    private RadioGroup userTypeRadioGroup;
    private Button submitButton;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Complaints");

        // Initialize UI components
        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);
        home = findViewById(R.id.home);
        about = findViewById(R.id.about);
        logout = findViewById(R.id.logout);
        settings = findViewById(R.id.settings);
        share = findViewById(R.id.share);
        complaint = findViewById(R.id.complaint);
        rating = findViewById(R.id.rating);

        // Initialize complaint form fields
        nameInput = findViewById(R.id.complaint_name);
        emailInput = findViewById(R.id.complaint_email);
        busNumberInput = findViewById(R.id.complaint_bus_number);
        locationInput = findViewById(R.id.complaint_location);
        descriptionInput = findViewById(R.id.complaint_description);
        userTypeRadioGroup = findViewById(R.id.user_type_radio_group);
        complaintTypeSpinner = findViewById(R.id.complaint_type_spinner);
        submitButton = findViewById(R.id.submit_complaint_button);

        // Set up the complaint type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.complaint_types, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        complaintTypeSpinner.setAdapter(adapter);

        // Pre-fill user information if logged in
        prefillUserInfo();

        // Navigation drawer click listeners
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer(drawerLayout);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(ComplaintActivity.this, MainActivity.class);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(ComplaintActivity.this, SettingsActivity.class);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(ComplaintActivity.this, ShareActivity.class);
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(ComplaintActivity.this, AboutActivity.class);
            }
        });

        complaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });

        rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(ComplaintActivity.this, RatingActivity.class);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ComplaintActivity.this, "Logout", Toast.LENGTH_SHORT).show();
            }
        });

        // Submit complaint button click listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComplaint();
            }
        });
    }

    private void prefillUserInfo() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            emailInput.setText(currentUser.getEmail());
            // Name could be retrieved from the database based on user ID if needed
        }
    }

    private void submitComplaint() {
        // Get values from form
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String busNumber = busNumberInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String complaintType = complaintTypeSpinner.getSelectedItem().toString();

        // Get selected user type
        int selectedRadioButtonId = userTypeRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
        String userType = "";
        if (selectedRadioButton != null) {
            userType = selectedRadioButton.getText().toString();
        }

        // Validate input fields
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(busNumber)) {
            busNumberInput.setError("Bus number is required");
            busNumberInput.requestFocus();
            return;
        }

        if (complaintType.equals("Select Complaint Type")) {
            Toast.makeText(this, "Please select a complaint type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(location)) {
            locationInput.setError("Location is required");
            locationInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userType)) {
            Toast.makeText(this, "Please select if you are a passenger or conductor", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            descriptionInput.setError("Description is required");
            descriptionInput.requestFocus();
            return;
        }

        // Get current date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Generate a unique ID for the complaint
        String complaintId = databaseReference.push().getKey();

        // Create complaint object
        Complaint complaintObj = new Complaint(
                complaintId,
                name,
                email,
                busNumber,
                complaintType,
                location,
                userType,
                description,
                currentDate,
                "Pending" // Initial status
        );

        // Save complaint to Firebase
        databaseReference.child(complaintId).setValue(complaintObj)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ComplaintActivity.this,
                                    "Complaint submitted successfully", Toast.LENGTH_SHORT).show();
                            clearForm();
                        } else {
                            Toast.makeText(ComplaintActivity.this,
                                    "Failed to submit complaint: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void clearForm() {
        // Clear all fields except email (user might want to submit multiple complaints)
        nameInput.setText("");
        busNumberInput.setText("");
        locationInput.setText("");
        descriptionInput.setText("");
        complaintTypeSpinner.setSelection(0);
        userTypeRadioGroup.clearCheck();
    }

    // Navigation drawer methods
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