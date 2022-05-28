package tornasuk.translations.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tornasuk.translations.Utils;
import tornasuk.translations.classes.Translation;
import tornasuk.translations.R;
import tornasuk.translations.databinding.DialogTranslationBinding;

public class EditTranslation extends AppCompatDialogFragment {

    private DatabaseReference firebasebdd;
    private Translation translation;
    private String word;
    private String wordTranslation;

    public EditTranslation(Translation translation){
        this.translation = translation;
    }

    private androidx.appcompat.app.AlertDialog editTranslation(){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity(), R.style.DialogBackground);

        DialogTranslationBinding translationBinding = DialogTranslationBinding.inflate(LayoutInflater.from(requireActivity()));
        builder.setView(translationBinding.getRoot());

        if(translation.getVolume() == null) // TRADUCCIÓN GENERAL EDITADA
            firebasebdd = FirebaseDatabase.getInstance().getReference(Utils.getThemeFromID(translation.getId())).child(translation.getId());
        else  // TRADUCCIÓN LIBRO EDITADA
            firebasebdd = FirebaseDatabase.getInstance().getReference(translation.getNovel()).child(translation.getVolume()).child(translation.getId());

        translationBinding.fabSearchCheck.setVisibility(View.GONE);
        translationBinding.fabWR.setVisibility(View.GONE);

        translationBinding.btnTranslation.setText(R.string.okMod);

        translationBinding.editWord.setText(translation.getWord());
        translationBinding.editTranslation.setText(translation.getWordTranslation());

        translationBinding.btnTranslation.setOnClickListener(v1 -> firebasebdd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                word = translationBinding.editWord.getText().toString();
                wordTranslation = translationBinding.editTranslation.getText().toString();
                if(!word.trim().equals("") && !wordTranslation.trim().equals("")){
                    Translation translationMod;
                    if(translation.getVolume() == null)
                        translationMod = new Translation(translation.getId(), word, wordTranslation);
                    else
                        translationMod = new Translation(translation.getId(), word, wordTranslation, translation.getVolume(), translation.getNovel());
                    firebasebdd.setValue(translationMod);
                    Toast.makeText(getActivity(), R.string.translationMod, Toast.LENGTH_SHORT).show();
                    dismiss();
                } else
                    Toast.makeText(getContext(), R.string.editTextBlank, Toast.LENGTH_SHORT).show();
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
        return editTranslation();
    }
}
