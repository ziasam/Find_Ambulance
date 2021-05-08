package com.brainfluence.pickmeuprebuild;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    private Button register,login;
    private TextInputLayout email,password;
    private TextInputEditText emailInput,passwordInput;
    private String token;
    private SharedPreferences sharedPref;
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference,databaseReferenceWrite;
    private ProgressDialog progressDialog;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String IS_LOGGED_IN = "isLoggedIn";
    public static final String ACCOUNT_TYPE = "accountType";
    public static final String UID = "uid";
    public static final String EMAIL = "email";
    public static final String USER_NAME = "userName";
    private ValueEventListener listener;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("");
        databaseReferenceWrite = firebaseDatabase.getReference("");
        sharedPref = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Token", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        token = task.getResult();

                        Log.d("Token", token);
//                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,CustomerRegActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validateEmail()  | !validatePass())
                {
                    return;
                }

                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("Please wait..."); // Setting Message
                progressDialog.setTitle("Validating"); // Setting Title
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                progressDialog.show(); // Display Progress Dialog
                progressDialog.setCancelable(false);


                mAuth.signInWithEmailAndPassword(emailInput.getText().toString().trim(),passwordInput.getText().toString().trim())
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String child = null;


                              listener= databaseReference.child("users").child(user.getUid().toString().trim()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            SharedPreferences.Editor editor = sharedPref.edit();
                                            editor.putBoolean(IS_LOGGED_IN, true);
                                            editor.putString(ACCOUNT_TYPE,snapshot.child("userType").getValue().toString().trim());
                                            editor.putString(EMAIL,snapshot.child("email").getValue().toString().trim());
                                            editor.putString(UID,user.getUid());
                                            editor.putString(USER_NAME,snapshot.child("name").getValue().toString().trim());

                                            editor.apply();


                                            databaseReferenceWrite.child("users").child(user.getUid().toString().trim()).child("token").setValue(token);
                                            startActivity(new Intent(LoginActivity.this,HomeActivity.class));

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });


                                } else {

                                    try
                                    {
                                        throw task.getException();
                                    }
                                    // if user enters wrong password.
                                    catch (FirebaseAuthInvalidCredentialsException malformedEmail)
                                    {
                                        progressDialog.dismiss();
                                        AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
                                        builder.setCancelable(true);
                                        builder.setIcon(R.drawable.ic_baseline_info_24);
                                        builder.setTitle("Validation Error");
                                        builder.setMessage("Email and password do not match");
                                        builder.setInverseBackgroundForced(true);
                                        builder.setPositiveButton("Close",new DialogInterface.OnClickListener(){

                                            @Override
                                            public void onClick(DialogInterface dialog, int which){
                                                dialog.dismiss();
                                            }
                                        });

                                        AlertDialog alert=builder.create();
                                        alert.show();
                                    }

                                    catch (Exception e)
                                    {
                                        progressDialog.dismiss();
                                        AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
                                        builder.setCancelable(true);
                                        builder.setIcon(R.drawable.ic_baseline_info_24);
                                        builder.setTitle("Login Failed");
                                        builder.setMessage("Please try again");
                                        builder.setInverseBackgroundForced(true);
                                        builder.setPositiveButton("Close",new DialogInterface.OnClickListener(){

                                            @Override
                                            public void onClick(DialogInterface dialog, int which){
                                                dialog.dismiss();
                                            }
                                        });

                                        AlertDialog alert=builder.create();
                                        alert.show();

                                    }

                                }
                            }
                        });
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            }
        });
    }

    private boolean validatePass() {
        String val = passwordInput.getText().toString();

        if(val.isEmpty())
        {
            password.setError("Field cannot be empty");
            return false;
        }
        if(val.length()<6)
        {
            password.setError("Password should contain 6 or more characters");
            return false;
        }
        else {
            password.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        String val = emailInput.getText().toString();
        String emailPattern = "^(.+)@(.+)$";

        if(val.isEmpty())
        {
            email.setError("Field cannot be empty");
            return false;
        }
        if(!val.matches(emailPattern))
        {
            email.setError("Invalid email address");
            return false;
        }
        else {
            email.setError(null);
            return true;
        }
    }

    @Override
    public void onBackPressed()
    {
        exitapp();
    }
    private void exitapp() {
        AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
        builder.setCancelable(true);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("Exit App");
        builder.setMessage("Are you sure you want to leave the application?");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Yes",new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which){
                finish();
                moveTaskToBack(true);

            }
        });

        builder.setNegativeButton("No",new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }
}