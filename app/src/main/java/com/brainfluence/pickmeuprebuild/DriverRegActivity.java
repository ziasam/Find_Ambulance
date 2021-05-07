package com.brainfluence.pickmeuprebuild;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.brainfluence.pickmeuprebuild.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class DriverRegActivity extends AppCompatActivity {
    private LinearLayout diver,customer;
    private TextInputLayout email,password,name,phoneNumber;
    private TextInputEditText emailInput,passwordInput,nameInput,phoneNumberInput;
    private Button register;
    private ProgressDialog progressDialog;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private String token;
    private User user;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diver_reg);

        diver = findViewById(R.id.driver);
        customer = findViewById(R.id.customer);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        name = findViewById(R.id.name);
        nameInput = findViewById(R.id.nameInput);
        phoneNumber = findViewById(R.id.phoneNumber);
        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        register = findViewById(R.id.register);
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("");


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

        customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DriverRegActivity.this,CustomerRegActivity.class));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validateName() | !validateEmail() | !validatePhoneNumber() | !validatePass())
                {
                    return;
                }

                progressDialog = new ProgressDialog(DriverRegActivity.this);
                progressDialog.setMessage("Please wait..."); // Setting Message
                progressDialog.setTitle("Registering Driver"); // Setting Title
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                progressDialog.show(); // Display Progress Dialog
                progressDialog.setCancelable(false);



                mAuth.createUserWithEmailAndPassword(emailInput.getText().toString().trim(), passwordInput.getText().toString().trim())
                        .addOnCompleteListener(DriverRegActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser userId = mAuth.getCurrentUser();



                                    user = new User(nameInput.getText().toString().trim(),
                                            emailInput.getText().toString().trim(),
                                            phoneNumberInput.getText().toString().trim(),
                                            passwordInput.getText().toString().trim(),
                                            token,
                                            null,
                                            "driver",
                                            userId.getUid().toString().trim()
                                    );

                                    databaseReference.child("users").child(userId.getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            AlertDialog.Builder builder=new AlertDialog.Builder(DriverRegActivity.this);
                                            builder.setCancelable(false);
                                            builder.setIcon(R.drawable.ic_baseline_info_24);
                                            builder.setTitle("Registration Successful");
                                            builder.setMessage("Registered successfully.Now Please Login to continue.");
                                            builder.setInverseBackgroundForced(true);
                                            builder.setPositiveButton("Login",new DialogInterface.OnClickListener(){

                                                @Override
                                                public void onClick(DialogInterface dialog, int which){
                                                    startActivity(new Intent(DriverRegActivity.this,LoginActivity.class));
                                                    finish();
                                                    dialog.dismiss();
                                                }
                                            });

                                            AlertDialog alert=builder.create();
                                            alert.show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            AlertDialog.Builder builder=new AlertDialog.Builder(DriverRegActivity.this);
                                            builder.setCancelable(true);
                                            builder.setIcon(R.drawable.ic_baseline_info_24);
                                            builder.setTitle("Registration Failed");
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
                                    });
                                } else {
                                    // If sign in fails, display a message to the user.
                                    try
                                    {
                                        throw task.getException();
                                    }
                                    // if user enters wrong email.
                                    catch (FirebaseAuthWeakPasswordException weakPassword)
                                    {
                                        progressDialog.dismiss();
                                        AlertDialog.Builder builder=new AlertDialog.Builder(DriverRegActivity.this);
                                        builder.setCancelable(true);
                                        builder.setIcon(R.drawable.ic_baseline_info_24);
                                        builder.setTitle("WeakPassword Error");
                                        builder.setMessage("Please provide a strong password");
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
                                    // if user enters wrong password.
                                    catch (FirebaseAuthInvalidCredentialsException malformedEmail)
                                    {
                                        progressDialog.dismiss();
                                        AlertDialog.Builder builder=new AlertDialog.Builder(DriverRegActivity.this);
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
                                    catch (FirebaseAuthUserCollisionException existEmail)
                                    {
                                        progressDialog.dismiss();
                                        AlertDialog.Builder builder=new AlertDialog.Builder(DriverRegActivity.this);
                                        builder.setCancelable(true);
                                        builder.setIcon(R.drawable.ic_baseline_info_24);
                                        builder.setTitle("ExistEmail Error");
                                        builder.setMessage("This email is already in use");
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
                                        AlertDialog.Builder builder=new AlertDialog.Builder(DriverRegActivity.this);
                                        builder.setCancelable(true);
                                        builder.setIcon(R.drawable.ic_baseline_info_24);
                                        builder.setTitle("Registration Failed");
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


    private boolean validatePhoneNumber() {
        String val = phoneNumberInput.getText().toString().trim();
        String phoneNumberPattern = "^([0][1]|[+][8][8][0][1])([3-9]{1}[0-9]{8})";
        if(val.isEmpty())
        {
            phoneNumber.setError("Field cannot be empty");
            return false;
        }
        if(!val.matches(phoneNumberPattern))
        {
            phoneNumber.setError("Invalid phone number");
            return false;
        }
        else {
            phoneNumber.setError(null);
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

    private boolean validateName() {
        String val = nameInput.getText().toString();

        if(val.isEmpty())
        {
            name.setError("Field cannot be empty");
            return false;
        }
        else {
            name.setError(null);
            return true;
        }
    }
}