package com.example.busmate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ImageView menu, profilePic;

    LinearLayout home, settings, share, about, complaint, rating, logout;

    TableLayout tableLayout;
    EditText searchEditText;
    RadioGroup searchOptionRadioGroup;
    RadioButton radioBusNumber, radioRouteName, radioRouteNumber;

    // Store all bus data for filtering
    private List<BusData> allBusData = new ArrayList<>();
    private String currentSearchText = "";
    private int currentSearchOption = 0; // 0: Bus Number, 1: Route Name, 2: Route Number
    private NavigationView navigationView;
    private TextView navUsername, navEmail;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);
        home = findViewById(R.id.home);
        about = findViewById(R.id.about);
        logout = findViewById(R.id.logout);
        settings = findViewById(R.id.settings);
        share = findViewById(R.id.share);
        complaint = findViewById(R.id.complaint);
        rating = findViewById(R.id.rating);
        tableLayout = findViewById(R.id.tableLayout);

        // Initialize search views
        searchEditText = findViewById(R.id.searchEditText);
        searchOptionRadioGroup = findViewById(R.id.searchOptionRadioGroup);
        radioBusNumber = findViewById(R.id.radioBusNumber);
        radioRouteName = findViewById(R.id.radioRouteName);
        radioRouteNumber = findViewById(R.id.radioRouteNumber);

        // Firebase reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bus Data");


        // Menu click listener to open the drawer
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer(drawerLayout);
            }
        });

        // Other navigation click listeners
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(MainActivity.this, SettingsActivity.class);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(MainActivity.this, ShareActivity.class);
            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(MainActivity.this, AboutActivity.class);
            }
        });

        complaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(MainActivity.this, ComplaintActivity.class);
            }
        });

        rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectActivity(MainActivity.this, RatingActivity.class);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Logout", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchText = s.toString().toLowerCase().trim();
                filterAndDisplayBusData();
            }
        });

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    filterAndDisplayBusData();
                    return true;
                }
                return false;
            }
        });

        searchOptionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioBusNumber) {
                    currentSearchOption = 0;
                } else if (checkedId == R.id.radioRouteName) {
                    currentSearchOption = 1;
                } else if (checkedId == R.id.radioRouteNumber) {
                    currentSearchOption = 2;
                }
                filterAndDisplayBusData();
            }
        });

        // Firebase data retrieval with improved UI
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check if data exists
                if (!dataSnapshot.exists()) {
                    Toast.makeText(MainActivity.this, "No data available", Toast.LENGTH_SHORT).show();
                    Log.d("Firebase", "No data available");
                    return;
                }

                // Clear the existing data
                allBusData.clear();

                // Populate the allBusData list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BusData busData = snapshot.getValue(BusData.class);
                    if (busData != null) {
                        allBusData.add(busData);
                    }
                }

                // Filter and display bus data based on current search criteria
                filterAndDisplayBusData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(MainActivity.this, "Error retrieving data", Toast.LENGTH_SHORT).show();
                Log.d("Firebase", "Database error: " + databaseError.getMessage());
            }
        });
    }

    // Filter and display bus data based on search criteria
    private void filterAndDisplayBusData() {
        List<BusData> filteredData = new ArrayList<>();

        if (currentSearchText.isEmpty()) {
            // If search is empty, show all data
            filteredData.addAll(allBusData);
        } else {
            // Filter data based on search text and selected option
            for (BusData busData : allBusData) {
                boolean matches = false;

                switch (currentSearchOption) {
                    case 0: // Bus Number
                        if (busData.getBus_number_plate().toLowerCase().contains(currentSearchText)) {
                            matches = true;
                        }
                        break;
                    case 1: // Route Name
                        if (busData.getRoute_name().toLowerCase().contains(currentSearchText)) {
                            matches = true;
                        }
                        break;
                    case 2: // Route Number
                        if (busData.getRoute_number().toLowerCase().contains(currentSearchText)) {
                            matches = true;
                        }
                        break;
                }

                if (matches) {
                    filteredData.add(busData);
                }
            }
        }

        // Display the filtered data
        displayBusData(filteredData);
    }

    // Display bus data in the table
    private void displayBusData(List<BusData> busDataList) {
        // Clear the table before adding new data
        tableLayout.removeAllViews();

        // Create a header row for the table with styling
        TableRow headerRow = new TableRow(MainActivity.this);
        headerRow.setBackgroundColor(getResources().getColor(R.color.purple));
        headerRow.setPadding(10, 15, 10, 15);

        // Create and style the header TextViews
        String[] headers = {"Bus Number", "Route Name", "Route", "Arrival Time", "Departure Time"};
        for (String header : headers) {
            TextView headerText = new TextView(MainActivity.this);
            headerText.setText(header);
            headerText.setTextColor(getResources().getColor(android.R.color.white));
            headerText.setPadding(15, 5, 15, 5);
            headerText.setTextSize(16);
            headerText.setTypeface(null, Typeface.BOLD);
            headerRow.addView(headerText);
        }

        // Add header row to the table
        tableLayout.addView(headerRow);

        // Add a divider
        View divider = new View(MainActivity.this);
        TableLayout.LayoutParams dividerParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, 2);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        tableLayout.addView(divider);

        // Show a message if no data matches the search
        if (busDataList.isEmpty()) {
            TableRow noDataRow = new TableRow(MainActivity.this);
            TextView noDataText = new TextView(MainActivity.this);
            noDataText.setText("No matching buses found");
            noDataText.setPadding(15, 20, 15, 20);
            noDataText.setTextSize(16);
            noDataText.setGravity(Gravity.CENTER);
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.span = 5; // Span across all columns
            noDataText.setLayoutParams(params);
            noDataRow.addView(noDataText);
            tableLayout.addView(noDataRow);
            return;
        }

        // Iterate through the data and add styled rows to the table
        int rowIndex = 0;
        for (BusData busData : busDataList) {
            TableRow row = new TableRow(MainActivity.this);
            row.setPadding(10, 10, 10, 10);

            // Alternate row background for better readability
            if (rowIndex % 2 == 0) {
                row.setBackgroundColor(getResources().getColor(android.R.color.white));
            } else {
                row.setBackgroundColor(getResources().getColor(R.color.lightGray));
            }

            // Create and add styled TextViews for each column
            String[] dataValues = {
                    busData.getBus_number_plate(),
                    busData.getRoute_name(),
                    busData.getRoute_number(),
                    busData.getArrival_time(),
                    busData.getDeparture_time()
            };

            for (String value : dataValues) {
                TextView textView = new TextView(MainActivity.this);
                textView.setText(value);
                textView.setPadding(15, 10, 15, 10);
                textView.setTextSize(14);
                row.addView(textView);
            }

            // Make the row clickable to show more details
            final BusData finalBusData = busData;
            row.setClickable(true);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Show a dialog with more details
                    showBusDetails(finalBusData);
                }
            });

            // Add the row to the table
            tableLayout.addView(row);

            // Add a thin divider between rows
            if (rowIndex < busDataList.size() - 1) {
                View rowDivider = new View(MainActivity.this);
                TableLayout.LayoutParams rowDividerParams = new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT, 1);
                rowDivider.setLayoutParams(rowDividerParams);
                rowDivider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                tableLayout.addView(rowDivider);
            }

            rowIndex++;
        }
    }

    // Method to show bus details when a row is clicked
    private void showBusDetails(BusData busData) {
        // Create an AlertDialog to show more details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bus Details");

        // Create a custom layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_bus_details1, null);
        TextView busNumberText = dialogView.findViewById(R.id.busNumberText);
        TextView routeNameText = dialogView.findViewById(R.id.routeNameText);
        TextView routeNumberText = dialogView.findViewById(R.id.routeNumberText);
        TextView arrivalTimeText = dialogView.findViewById(R.id.arrivalTimeText);
        TextView departureTimeText = dialogView.findViewById(R.id.departureTimeText);

        // Set the values
        busNumberText.setText("Bus Number: " + busData.getBus_number_plate());
        routeNameText.setText("Route Name: " + busData.getRoute_name());
        routeNumberText.setText("Route Number: " + busData.getRoute_number());
        arrivalTimeText.setText("Arrival Time: " + busData.getArrival_time());
        departureTimeText.setText("Departure Time: " + busData.getDeparture_time());

        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

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