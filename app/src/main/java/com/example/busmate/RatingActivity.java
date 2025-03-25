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
import android.widget.RatingBar;
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

public class RatingActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ImageView menu;
    LinearLayout home, settings, share, about, complaint, rating, logout;

    // UI components for rating form
    private TextInputEditText nameInput, emailInput, busNumberInput, commentInput;
    private Spinner ratingCategorySpinner;
    private RadioGroup userTypeRadioGroup;
    private RatingBar ratingBar;
    private Button submitButton;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Ratings");

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

        // Initialize rating form fields
        nameInput = findViewById(R.id.rating_name);
        emailInput = findViewById(R.id.rating_email);
        busNumberInput = findViewById(R.id.rating_bus_number);
        commentInput = findViewById(R.id.rating_comment);
        userTypeRadioGroup = findViewById(R.id.user_type_radio_group);
        ratingCategorySpinner = findViewById(R.id.rating_category_spinner);
        ratingBar = findViewById(R.id.rating_bar);
        submitButton = findViewById(R.id.submit_rating_button);

        // Set up the rating category spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.rating_categories, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        ratingCategorySpinner.setAdapter(adapter);

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
                redirectActivity(RatingActivity.this, MainActivity.class);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(RatingActivity.this, SettingsActivity.class);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(RatingActivity.this, ShareActivity.class);
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(RatingActivity.this, AboutActivity.class);
            }
        });

        complaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(RatingActivity.this, ComplaintActivity.class);
            }
        });

        rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RatingActivity.this, "Logout", Toast.LENGTH_SHORT).show();
            }
        });

        // Submit rating button click listener
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRating();
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

    private void submitRating() {
        // Get values from form
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String busNumber = busNumberInput.getText().toString().trim();
        String comment = commentInput.getText().toString().trim();
        String ratingCategory = ratingCategorySpinner.getSelectedItem().toString();
        float ratingValue = ratingBar.getRating();

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

        if (ratingCategory.equals("Select Rating Category")) {
            Toast.makeText(this, "Please select a rating category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userType)) {
            Toast.makeText(this, "Please select if you are a passenger or conductor", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ratingValue == 0) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Generate a unique ID for the rating
        String ratingId = databaseReference.push().getKey();

        // Create rating object
        Rating ratingObj = new Rating(
                ratingId,
                name,
                email,
                busNumber,
                ratingCategory,
                userType,
                ratingValue,
                comment,
                currentDate
        );

        // Save rating to Firebase
        databaseReference.child(ratingId).setValue(ratingObj)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RatingActivity.this,
                                    "Rating submitted successfully", Toast.LENGTH_SHORT).show();
                            clearForm();
                        } else {
                            Toast.makeText(RatingActivity.this,
                                    "Failed to submit rating: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void clearForm() {
        // Clear all fields except email (user might want to submit multiple ratings)
        nameInput.setText("");
        busNumberInput.setText("");
        commentInput.setText("");
        ratingCategorySpinner.setSelection(0);
        userTypeRadioGroup.clearCheck();
        ratingBar.setRating(0);
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