package tornasuk.translations.dialogs;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.navigation.Navigation;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import tornasuk.translations.Utils;
import tornasuk.translations.classes.Translation;
import tornasuk.translations.Constantes;
import tornasuk.translations.R;
import tornasuk.translations.databinding.DialogTranslationBinding;
import tornasuk.translations.room.TranslationRepository;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.INPUT_METHOD_SERVICE;

public class NewTranslation extends AppCompatDialogFragment {

    private DatabaseReference firebasebdd;
    private String novel;
    private String word;
    private String wordTranslation;
    private String id;
    private Translation translation;
    private int numPag;
    private String volume;
    private ArrayList<Integer> ids;
    private ArrayList<DataSnapshot> volumes;
    private ArrayList<Translation> translations;
    private boolean checkWordPressed;

    public NewTranslation(){
    }

    public NewTranslation(String novel, String volume, int numPag){
        this.novel = novel;
        this.volume = volume;
        this.numPag = numPag;
    }

    private androidx.appcompat.app.AlertDialog newTranslation(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity(), R.style.DialogBackground);

        DialogTranslationBinding translationBinding = DialogTranslationBinding.inflate(LayoutInflater.from(requireActivity()));
        builder.setView(translationBinding.getRoot());

        firebasebdd = FirebaseDatabase.getInstance().getReference();

        translationBinding.fabSearchCheck.setOnClickListener(v1 -> {
            if(!translationBinding.editWord.getText().toString().trim().equals("")){
                checkWordPressed = true;
                Bundle word = new Bundle();
                word.putString("Word", translationBinding.editWord.getText().toString());
                Navigation.findNavController(requireActivity(), R.id.nav_host_frag).navigate(R.id.nav_checkWords, word);

                translationBinding.editWord.clearFocus();
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(translationBinding.editWord.getWindowToken(), 0);
            }
        });

        translationBinding.fabWR.setOnClickListener(v12 -> {
            ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(translationBinding.editWord.getText(), translationBinding.editWord.getText());
            clipboard.setPrimaryClip(clip);
            Intent launchIntent = requireActivity().getPackageManager().getLaunchIntentForPackage("com.wordreference");
            if (launchIntent != null)
                startActivity(launchIntent);
            else
                Toast.makeText(getActivity(), R.string.errorWR, Toast.LENGTH_SHORT).show();

        });

        translationBinding.editWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!translationBinding.editWord.getText().toString().equals(""))
                    translationBinding.fabSearchCheck.setVisibility(View.VISIBLE);
                else
                    translationBinding.fabSearchCheck.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        translationBinding.btnTranslation.setOnClickListener(v13 -> firebasebdd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                volumes = new ArrayList<>();
                translations = new ArrayList<>();
                ids = new ArrayList<>();

                word = translationBinding.editWord.getText().toString();
                wordTranslation = translationBinding.editTranslation.getText().toString();

                for (DataSnapshot novelSnap : snapshot.getChildren()) {
                    if(!novelSnap.getChildren().iterator().next().getKey().contains("Volume")) {
                        for (DataSnapshot translationSnap : novelSnap.getChildren()){
                            if(!translationSnap.getKey().equals("volImg")) {
                                try {
                                    translations.add(translationSnap.getValue(Translation.class));
                                } catch (DatabaseException ignored) {
                                }
                            }
                        }
                    } else {
                        for (DataSnapshot volSnap : novelSnap.getChildren()){
                            if(volSnap.getChildrenCount() > 1)
                                volumes.add(volSnap);
                        }
                    }
                }

                for (DataSnapshot volSnap : volumes){
                    for(DataSnapshot translationSnap : volSnap.getChildren()){
                        if(!translationSnap.getKey().equals("volImg")) {
                            try {
                                translations.add(translationSnap.getValue(Translation.class));
                            } catch (DatabaseException ignored) {
                            }
                        }
                    }
                }

                boolean wordExists = false;
                for(int i = 0; i < translations.size() && !wordExists; i++) {
                    if (translations.get(i).getWord().equals(word))
                        wordExists = true;
                }

                if(!word.trim().equals("") && !wordTranslation.trim().equals("")){
                    if(!wordExists){
                        int count = 0;
                        if(novel != null){
                            for(DataSnapshot transSnapshot : snapshot.child(novel).child(volume).getChildren()){
                                if(transSnapshot.getKey().contains(Constantes.refID + numPag + "-")) {
                                    if(!transSnapshot.getValue().toString().equals(Constantes.refID + numPag + "-" + "1"))
                                        count++;
                                }
                            }
                            id = Constantes.refID + numPag + "-" + (count+1);
                            translation = new Translation(id, word, wordTranslation, volume, novel);
                            firebasebdd.child(novel).child(volume).child(id).setValue(translation);

                        } else {
                            String generalTheme = Utils.getThemeFromID(Constantes.refID);
                            for(DataSnapshot snap : snapshot.child(generalTheme).getChildren()){
                                int idTranslation = Integer.parseInt(snap.getKey().split("-")[1]);
                                ids.add(idTranslation);
                            }

                            ids.sort((id1, id2) -> id2.compareTo(id1));

                            id = Constantes.refID + "-" + (ids.get(0)+1);

                            translation = new Translation(id, word, wordTranslation);
                            firebasebdd.child(generalTheme).child(id).setValue(translation);
                        }

                        TranslationRepository roomRepo = new TranslationRepository(requireActivity().getApplication());
                        roomRepo.insertLastTranslation(translation);

                        Toast.makeText(requireActivity(), R.string.translationAdded, Toast.LENGTH_SHORT).show();

                        dismiss();
                    } else
                        Toast.makeText(requireActivity(), R.string.wordRepeated, Toast.LENGTH_SHORT).show();

                } else
                    Toast.makeText(requireActivity(), R.string.editTextBlank, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));

        return builder.create();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return newTranslation();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(checkWordPressed){
            if(novel != null)
                Navigation.findNavController(requireActivity(), R.id.nav_host_frag).popBackStack(R.id.nav_translations, false);
            else
                Navigation.findNavController(requireActivity(), R.id.nav_host_frag).popBackStack(R.id.nav_general, false);
        }
    }

}
