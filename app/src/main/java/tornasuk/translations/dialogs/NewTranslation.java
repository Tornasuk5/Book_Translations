package tornasuk.translations.dialogs;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;

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
    private int numPag;
    private String volume;
    private boolean wordChecked;
    private boolean wordExists;
    private ArrayList<DataSnapshot> volumes;
    private ArrayList<Translation> translations;
    private ArrayList<Translation> translationsChecked;

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
            word = translationBinding.editWord.getText().toString();
            if(!word.trim().equals("")){
                wordChecked = true;
                translationBinding.fabSearchCheck.setVisibility(View.INVISIBLE);
                translationBinding.progBarCheck.setVisibility(View.VISIBLE);

                firebasebdd.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(translations == null) {
                            volumes = new ArrayList<>();
                            translations = new ArrayList<>();
                            translationsChecked = new ArrayList<>();

                            if (!word.trim().equals("")) {
                                for (DataSnapshot novelSnap : snapshot.getChildren()) {
                                    if (novelSnap.hasChild("Volume 1")) {
                                        for (DataSnapshot volSnap : novelSnap.getChildren()) {
                                            if (volSnap.getChildrenCount() > 1)
                                                volumes.add(volSnap);
                                        }
                                    } else {
                                        for (DataSnapshot translationSnap : novelSnap.getChildren()) {
                                            if (!translationSnap.getKey().equals("volImg")) {
                                                try {
                                                    translations.add(translationSnap.getValue(Translation.class));
                                                } catch (DatabaseException ignored) {
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!volumes.isEmpty()) {
                                    for (DataSnapshot volSnap : volumes) {
                                        for (DataSnapshot translationSnap : volSnap.getChildren()) {
                                            if (!translationSnap.getKey().equals("volImg")) {
                                                try {
                                                    translations.add(translationSnap.getValue(Translation.class));
                                                } catch (DatabaseException ignored) {
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            translationsChecked.clear();
                            wordExists = false;
                        }

                        for (Translation translationFor : translations) {
                            if(translationFor.getWord().contains(word) || word.contains(translationFor.getWord())) {
                                wordExists = true;
                                translationsChecked.add(translationFor);
                            }
                        }

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Checked translations", (Serializable) translationsChecked);
                        Navigation.findNavController(requireActivity(), R.id.nav_host_frag).navigate(R.id.nav_checkWords, bundle);

                        translationBinding.editWord.clearFocus();
                        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(translationBinding.editWord.getWindowToken(), 0);

                        translationBinding.progBarCheck.setVisibility(View.INVISIBLE);
                        translationBinding.fabSearchCheck.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else Toast.makeText(requireActivity(), R.string.editTextBlank, Toast.LENGTH_SHORT).show();
        });

        translationBinding.fabWR.setOnClickListener(v12 -> {
            ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(translationBinding.editWord.getText(), translationBinding.editWord.getText());
            clipboard.setPrimaryClip(clip);

            Intent launchIntent = requireActivity().getPackageManager().getLaunchIntentForPackage("com.wordreference");

            if (launchIntent != null) startActivity(launchIntent);
            else Toast.makeText(getActivity(), R.string.errorWR, Toast.LENGTH_SHORT).show();

        });

        translationBinding.editWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (wordChecked) wordChecked = false;
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
                if(wordChecked){
                    if(!wordExists){
                        wordTranslation = translationBinding.editTranslation.getText().toString();
                        if(!wordTranslation.trim().equals("")){
                            Translation translation;
                            int count = 1;
                            if(novel != null){
                                for(DataSnapshot transSnapshot : snapshot.child(novel).child(volume).getChildren()){
                                    if(!transSnapshot.getKey().equals("volImg")){
                                        String[] idPgTranslation = transSnapshot.getKey().split("-");
                                        if(transSnapshot.getValue().toString().equals(idPgTranslation[0] + "-1")) break;
                                        else if(idPgTranslation[0].equals(Constantes.refID + numPag))
                                            count++;
                                    }
                                }
                                id = Constantes.refID + numPag + "-" + (count);
                                translation = new Translation(id, word, wordTranslation, volume, novel);
                                firebasebdd.child(novel).child(volume).child(id).setValue(translation);

                            } else {
                                String generalTheme = Utils.getThemeFromID(Constantes.refID);
                                int numIdMax = 0;
                                for (DataSnapshot translationSnap : snapshot.child(generalTheme).getChildren()){
                                    Translation translation2 = translationSnap.getValue(Translation.class);
                                    int translationId = Integer.parseInt(translation2.getNumId());
                                    if (translationId > numIdMax) numIdMax = translationId;
                                }

                                id = Constantes.refID + "-" + (numIdMax + 1);

                                translation = new Translation(id, word, wordTranslation);
                                firebasebdd.child(generalTheme).child(id).setValue(translation);

                            }

                            TranslationRepository roomRepo = new TranslationRepository(requireActivity().getApplication());
                            roomRepo.insertLastTranslation(translation);

                            Toast.makeText(requireActivity(), R.string.translationAdded, Toast.LENGTH_SHORT).show();
                            dismiss();


                        } else Toast.makeText(requireActivity(), R.string.editTextBlank, Toast.LENGTH_SHORT).show();

                    } else Toast.makeText(requireActivity(), R.string.wordRepeated, Toast.LENGTH_SHORT).show();

                } else Toast.makeText(requireActivity(), R.string.wordNotChecked, Toast.LENGTH_SHORT).show();
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
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_frag);
        if(navController.getCurrentDestination().getLabel().equals("Checking word…")){
            if(novel != null)
                navController.popBackStack(R.id.nav_translations, false);
            else {
                switch (Constantes.refID){
                    case Constantes.ID_KOICHOCO:
                        navController.popBackStack(R.id.nav_koichoco, false);
                        break;
                    case Constantes.ID_CLANNAD:
                        navController.popBackStack(R.id.nav_clannad, false);
                        break;
                    case Constantes.ID_GENERAL:
                        navController.popBackStack(R.id.nav_general, false);
                        break;
                }
            }
        }
    }
}
