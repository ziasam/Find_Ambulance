package com.brainfluence.pickmeuprebuild;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private Button register,login;
    private TextInputLayout email,password;
    private TextInputEditText emailInput,passwordInput;

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


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,CustomerRegActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(!validateEmail()  | !validatePass())
//                {
//                    return;
//                }
                startActivity(new Intent(LoginActivity.this,PassangerHomeActivity.class));
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
}