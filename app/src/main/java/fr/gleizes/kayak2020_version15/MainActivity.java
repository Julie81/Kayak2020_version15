package fr.gleizes.kayak2020_version15;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button buttonMontres;
    Button buttonTrajet;
    Button buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonMontres = findViewById(R.id.button_montre);
        buttonTrajet = findViewById(R.id.button_map);
        buttonStart = findViewById(R.id.button_start);

        buttonTrajet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openactivityTrajet();
            }
        });

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openactivityStart();
            }
        });
    }

    public void openactivityTrajet(){
        Intent intentTrajet = new Intent(this, ConfigurationTrajet.class);
        startActivity(intentTrajet);

    }

    public void openactivityStart(){
        Intent intentStart = new Intent(this, navigationGuiding.class);
        startActivity(intentStart);
    }



}