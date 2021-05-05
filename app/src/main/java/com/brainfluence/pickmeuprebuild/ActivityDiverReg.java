package com.brainfluence.pickmeuprebuild;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ActivityDiverReg extends AppCompatActivity {
    private LinearLayout diver,customer;
    private TextInputLayout email,password,name,phoneNumber;
    private TextInputEditText emailInput,passwordInput,nameInput,phoneNumberInput;
    private Button register;
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

        customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityDiverReg.this,CustomerRegActivity.class));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validateName() | !validateEmail() | !validatePhoneNumber() | !validatePass())
                {
                    return;
                }
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