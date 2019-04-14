package utfpr.biagini.integrationapp.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import utfpr.biagini.integrationapp.R;

public class Settings1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_DialogWhenLarge);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings1);
    }
}
