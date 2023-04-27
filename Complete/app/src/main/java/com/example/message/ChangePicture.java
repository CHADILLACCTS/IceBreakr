package com.example.message;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ChangePicture extends AppCompatActivity {

    ImageView iconDefault, icon2, icon3, icon4, icon5, icon6, icon7, icon8, icon9;
    ImageView selectedDefault, selected2, selected3, selected4, selected5, selected6, selected7, selected8, selected9;
    ImageView current = null;
    int currentId;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    ImageButton backBtn;

    Button confirm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_picture);

        //Link the variables to their corresponding element in the layout

        //These represent the profile picture icons
        iconDefault = findViewById(R.id.userprofile_profile_pic);
        icon2 = findViewById(R.id.userprofile_profile_pic2);
        icon3 = findViewById(R.id.userprofile_profile_pic3);
        icon4 = findViewById(R.id.userprofile_profile_pic4);
        icon5 = findViewById(R.id.userprofile_profile_pic5);
        icon6 = findViewById(R.id.userprofile_profile_pic6);
        icon7 = findViewById(R.id.userprofile_profile_pic7);
        icon8 = findViewById(R.id.userprofile_profile_pic8);
        icon9 = findViewById(R.id.userprofile_profile_pic9);

        //These represent the "selected" arrow above each profile picture
        //These are set to invisible be default and will become visible when the
        //corresponding profile picture is clicked
        selectedDefault = findViewById(R.id.selected);
        selected2 = findViewById(R.id.selected2);
        selected3 = findViewById(R.id.selected3);
        selected4 = findViewById(R.id.selected4);
        selected5 = findViewById(R.id.selected5);
        selected6 = findViewById(R.id.selected6);
        selected7 = findViewById(R.id.selected7);
        selected8 = findViewById(R.id.selected8);
        selected9 = findViewById(R.id.selected9);

        backBtn = findViewById(R.id.change_picture_back_btn);

        confirm = findViewById(R.id.change_picture_btn);


        // -----------------------------------------------------------------------------------------
        // ----------------------------------------- BUTTON ----------------------------------------

        //When the user clicks the "Back arrow button", they are redirected to the User Profile activity
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChangePicture.this, UserProfile.class);
                startActivity(intent);
                finish();
            }
        });


        //When the user selects the confirm button, first the database of the current user is connected to.
        //Then a reference to the User node in the database is created
        //Then the currently selected imageID is converted to a string and put into the imageID node of that user in the database
        //Then the user is redirected to User Profile
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current != null) {
                    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("imageID", String.valueOf(currentId));
                    reference.updateChildren(hashMap);
                    Intent intent = new Intent(ChangePicture.this, UserProfile.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(ChangePicture.this, "No picture Selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Each button below is linked to a different profile picture
        //For each one, when clicked it checks if current(The "invisible" arrow above the currently selected picture) is null
        //If not null(null is the default at activity launch), the previously selected "arrow" is set to invisible
        //Then the newly selected profile pictures arrow is set to visible
        //Then a string corresponding to the correct name of that profile picture is inserted into the updateID function

        iconDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current != null)
                {
                    current.setVisibility(View.INVISIBLE);
                }
                current = selectedDefault;
                current.setVisibility(View.VISIBLE);
                updateId("icondefault");
            }
        });

        icon2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current != null)
                {
                    current.setVisibility(View.INVISIBLE);
                }
                current = selected2;
                current.setVisibility(View.VISIBLE);
                updateId("icon1");
            }
        });

        icon3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current != null)
                {
                    current.setVisibility(View.INVISIBLE);
                }
                current = selected3;
                current.setVisibility(View.VISIBLE);
                updateId("icon2");
            }
        });

        icon4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current != null)
                {
                    current.setVisibility(View.INVISIBLE);
                }
                current = selected4;
                current.setVisibility(View.VISIBLE);
                updateId("icon3");
            }
        });

        icon5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current != null)
                {
                    current.setVisibility(View.INVISIBLE);
                }
                current = selected5;
                current.setVisibility(View.VISIBLE);
                updateId("icon4");
            }
        });

        icon6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current != null)
                {
                    current.setVisibility(View.INVISIBLE);
                }
                current = selected6;
                current.setVisibility(View.VISIBLE);
                updateId("icon5");
            }
        });

        icon7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current != null)
                {
                    current.setVisibility(View.INVISIBLE);
                }
                current = selected7;
                current.setVisibility(View.VISIBLE);
                updateId("icon6");
            }
        });

        icon8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current != null)
                {
                    current.setVisibility(View.INVISIBLE);
                }
                current = selected8;
                current.setVisibility(View.VISIBLE);
                updateId("icon7");
            }
        });

        icon9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(current != null)
                {
                    current.setVisibility(View.INVISIBLE);
                }
                current = selected9;
                current.setVisibility(View.VISIBLE);
                updateId("icon8");
            }
        });

    }

    //This function takes the entered String with the name of a resource file and updates
    //the currentID with the new identifier.

    void updateId(String name){
        currentId = this.getResources().getIdentifier(name, "drawable", this.getPackageName());
    }

}
