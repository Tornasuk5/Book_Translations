package tornasuk.translations;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import tornasuk.translations.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mainBinding;
    private AppBarConfiguration appBarConfig;
    private NavController navController;
    private String fragTag;
    private NavHostFragment navHostFragment;
    private String destinationFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        appBarConfig = new AppBarConfiguration.Builder(R.id.nav_inicio, R.id.nav_general, R.id.nav_overlord, R.id.nav_loghorizon,
                R.id.nav_classroom, R.id.nav_ngnl, R.id.nav_clannad)
                .setOpenableLayout(mainBinding.drawerNav)
                .build();

        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_frag);
        navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);
        NavigationUI.setupWithNavController(mainBinding.navView, navController);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword("rockero_5@hotmail.es", "Rockguitar5").addOnSuccessListener(authResult -> {
        }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "No se ha podido iniciar sesión", Toast.LENGTH_SHORT).show());

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(mainBinding.drawerNav.isOpen())
                    mainBinding.drawerNav.close();
                if(fragTag.equals(getString(R.string.navTextInicio))){
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else
                    navController.popBackStack();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        mainBinding.drawerNav.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if(fragTag.equals("Search Translations")){
                    mainBinding.navView.requestFocus();
                    hideKeyboard();
                }
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            setFabInvisible();
            destinationFrag = String.valueOf(destination.getLabel());
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_icon, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        fragTag = destinationFrag;

        MenuItem icon = menu.findItem(R.id.bar_icon);

        if(!icon.isVisible())
            icon.setVisible(true);

        if(fragTag.equals(getString(R.string.navTextInicio)) ||
                fragTag.equals("Search Translations") ||
                fragTag.equals("Last Translations"))
            icon.setVisible(false);

        if(fragTag.equals("Páginas") || fragTag.equals("Translations")){
            fragTag = getFragTag();
            if(fragTag.contains("Páginas"))
                fragTag = fragTag.replace("Páginas ","");
            else
                fragTag = fragTag.replace("Translations ","");
        }

        switch (fragTag) {
            case "General":
                Constantes.refID = Constantes.ID_GENERAL;
                icon.setIcon(R.drawable.icon_general);
                break;
            case "Clannad ~After Story~":
                Constantes.refID = Constantes.ID_CLANNAD;
                icon.setIcon(R.drawable.icon_clannad);
                break;
            case "Overlord":
                Constantes.refID = Constantes.ID_OVERLORD;
                icon.setIcon(R.drawable.overlord_logo3);
                mainBinding.navView.setCheckedItem(R.id.nav_view);
                mainBinding.navView.getMenu().getItem(2).setChecked(true);
                break;
            case "Log Horizon":
                Constantes.refID = Constantes.ID_LOGHORIZON;
                icon.setIcon(R.drawable.icon_log2);
                mainBinding.navView.setCheckedItem(R.id.nav_view);
                mainBinding.navView.getMenu().getItem(3).setChecked(true);
                break;
            case "Classroom of the Elite":
                Constantes.refID = Constantes.ID_CLASSROOM;
                icon.setIcon(R.drawable.icon_class1);
                mainBinding.navView.setCheckedItem(R.id.nav_view);
                mainBinding.navView.getMenu().getItem(4).setChecked(true);
                break;
            case "No Game No Life":
                Constantes.refID = Constantes.ID_NGNL;
                icon.setIcon(R.drawable.icon_ngnl);
                mainBinding.navView.setCheckedItem(R.id.nav_view);
                mainBinding.navView.getMenu().getItem(5).setChecked(true);
                break;
            case "CheckWord":
                icon.setIcon(R.drawable.search_translations2);
                break;
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void setFabInvisible(){
        mainBinding.fabTranslation.setVisibility(View.INVISIBLE);
    }

    public String getFragTag(){
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_frag);
        Fragment frag = navHostFragment.getChildFragmentManager().getFragments().get(0);
        View fragView = frag.getView();

        return String.valueOf(fragView != null ? fragView.getTag() : "");
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfig) || super.onSupportNavigateUp();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}