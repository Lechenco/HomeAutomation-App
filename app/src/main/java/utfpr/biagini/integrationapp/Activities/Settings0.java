package utfpr.biagini.integrationapp.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import utfpr.biagini.integrationapp.R;

public class Settings0 extends AppCompatActivity {

    //Shared para o tema
    private static final String FILE = "Shared_Persistance";
    private static final String THEME = "THEME";

    //Cria o textView sobre o app
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Define o tema do layout de acordo com o shared
        SharedPreferences shared = getSharedPreferences(FILE, Context.MODE_PRIVATE);
        String theme="";
        theme=shared.getString(THEME, theme);
        if(theme.equals("DARK")){
            setTheme(R.style.Theme_AppCompat_DialogWhenLarge);
        }else if(theme.equals("LIGHT")){
            setTheme(R.style.AppTheme);
        }else{
            setTheme(R.style.Theme_AppCompat_Light);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings0);
    }

    public void editRooms(View view){
        Intent set1 = new Intent(this, Settings1.class);
        startActivity(set1);
    }
    //Botão dos Temas, salva a preferencia no shared, seta o novo tema pro layout e recrea a tela
    public void selectThemeLight(View view){
        SharedPreferences shared = getSharedPreferences(FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(THEME, "LIGHT");
        setTheme(R.style.AppTheme);
        editor.commit();
        Toast.makeText(this, R.string.applyingLmessage, Toast.LENGTH_SHORT).show();
        recreate();
    }
    public void selectThemeDark(View view){
        SharedPreferences shared = getSharedPreferences(FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(THEME, "DARK");
        setTheme(R.style.Theme_AppCompat_DialogWhenLarge);
        editor.commit();
        Toast.makeText(this, R.string.applyingDmessage, Toast.LENGTH_SHORT).show();
        recreate();
    }
    public void resetTheme(View view){
        SharedPreferences shared = getSharedPreferences(FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(THEME, "");
        setTheme(R.style.Theme_AppCompat_Light);
        editor.commit();
        Toast.makeText(this, R.string.applyingDdmessage, Toast.LENGTH_SHORT).show();
        recreate();
    }
}
