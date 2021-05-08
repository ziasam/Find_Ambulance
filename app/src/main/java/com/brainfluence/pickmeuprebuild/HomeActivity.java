package com.brainfluence.pickmeuprebuild;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import es.dmoral.toasty.Toasty;

import static com.brainfluence.pickmeuprebuild.LoginActivity.EMAIL;
import static com.brainfluence.pickmeuprebuild.LoginActivity.SHARED_PREFS;
import static com.brainfluence.pickmeuprebuild.LoginActivity.USER_NAME;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private SharedPreferences sharedPref;
    private TextView userName,emailAddress;
    private String name,email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passanger_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);

        userName = headerView.findViewById(R.id.userName);
        emailAddress =headerView.findViewById(R.id.emailAddress);
        sharedPref = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        name = sharedPref.getString(USER_NAME,"user");
        email = sharedPref.getString(EMAIL,"email");
        userName.setText(name);
        emailAddress.setText(email);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.nav_slideshow)
                {
                    AlertDialog.Builder builder=new AlertDialog.Builder(HomeActivity.this);
                    builder.setCancelable(true);
                    builder.setIcon(R.mipmap.ic_launcher);
                    builder.setTitle("Exit App");
                    builder.setMessage("Are you sure you want to logout?");
                    builder.setInverseBackgroundForced(true);
                    builder.setPositiveButton("Yes",new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            FirebaseAuth.getInstance().signOut();
                            sharedPref.edit().clear().commit();
                            startActivity(new Intent(HomeActivity.this,LoginActivity.class));
                            finish();

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
                return true;
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.passanger_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed()
    {
        exitapp();
    }
    private void exitapp() {
        AlertDialog.Builder builder=new AlertDialog.Builder(HomeActivity.this);
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