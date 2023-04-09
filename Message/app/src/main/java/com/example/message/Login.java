package com.example.message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    EditText email, password;
    TextView forgotPassword;
    Button loginBtn;

    CheckBox rememberMe;


    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String REMEMBER = "remember";
    boolean check;


    FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        password = findViewById(R.id.login_password);
        email = findViewById(R.id.login_email);
        loginBtn = findViewById(R.id.login_btn);
        rememberMe = findViewById(R.id.remember_Me);
        forgotPassword = findViewById(R.id.forgot_password);


        auth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String l_password = password.getText().toString();
                String l_email = email.getText().toString();

                if (TextUtils.isEmpty(l_password) | TextUtils.isEmpty(l_email)) {
                    Toast.makeText(Login.this, "All fields must be filled!", Toast.LENGTH_SHORT).show();
                } else {
                    auth.signInWithEmailAndPassword(l_email, l_password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        check = rememberMe.isChecked();
                                        //Toast.makeText(Login.this, String.valueOf(check), Toast.LENGTH_SHORT).show();
                                        saveData(rememberMe.isChecked());
                                        Intent intent = new Intent(Login.this, HomeScreen.class);
                                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(Login.this, "Login failed!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, ResetPassword.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
    public void saveData(boolean checked) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(REMEMBER, checked);
        editor.commit();
        Toast.makeText(this, String.valueOf(checked), Toast.LENGTH_SHORT).show();
    }
}
