package tornasuk.translations.navfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import tornasuk.translations.adapters.TranslationsAdapter;
import tornasuk.translations.classes.Translation;
import tornasuk.translations.R;
import tornasuk.translations.databinding.GeneralLayoutBinding;

public class CheckWordsFrag extends Fragment {

    private DatabaseReference firebasebdd;
    private TranslationsAdapter translationsAdapter;
    private ArrayList<DataSnapshot> volumes;
    private ArrayList<Translation> translations;
    private ArrayList<Translation> translationsChecked;
    private ConstraintLayout progLayout;
    private String checkWord;

    public CheckWordsFrag(){
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        GeneralLayoutBinding generalBinding = GeneralLayoutBinding.inflate(inflater, container, false);
        View fragView = generalBinding.getRoot();

        fragView.setTag("CheckWord");

        requireActivity().invalidateOptionsMenu();

        try {
            checkWord = getArguments().getString("Word");
        } catch (NullPointerException npx){

        }

        progLayout = getActivity().findViewById(R.id.prog_layout);
        progLayout.setVisibility(View.VISIBLE);

        firebasebdd = FirebaseDatabase.getInstance().getReference();

        generalBinding.rvGeneral.setHasFixedSize(true);
        generalBinding.rvGeneral.setLayoutManager(new LinearLayoutManager(getActivity()));

        firebasebdd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                volumes = new ArrayList<>();
                translations = new ArrayList<>();
                translationsChecked = new ArrayList<>();

                for (DataSnapshot novelSnap : snapshot.getChildren()) {
                    if(!novelSnap.getChildren().iterator().next().getKey().contains("Volume")) {
                        for (DataSnapshot translationSnap : novelSnap.getChildren()){
                            if(!translationSnap.getKey().equals("volImg")) {
                                try {
                                    translations.add(translationSnap.getValue(Translation.class));
                                } catch (DatabaseException ignored){
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
                            } catch (DatabaseException ignored){
                            }
                        }
                    }
                }

                if(checkWord != null) {
                    for(int i = 0; i < translations.size(); i++){
                        if(translations.get(i).getWord().contains(checkWord))
                            translationsChecked.add(translations.get(i));
                    }

                    translationsAdapter = new TranslationsAdapter(getActivity(), translationsChecked, "");
                    generalBinding.rvGeneral.setAdapter(translationsAdapter);

                    int numTranslationsChecked = translationsAdapter.getItemCount();

                    if(numTranslationsChecked == 0)
                        Toast.makeText(getActivity(), R.string.checkWordCoincidencias1, Toast.LENGTH_SHORT).show();
                    else if(numTranslationsChecked == 1)
                        Toast.makeText(getActivity(), R.string.checkWordCoincidencias2 , Toast.LENGTH_SHORT).show();
                    else if(numTranslationsChecked > 1)
                        Toast.makeText(getActivity(), "Se han encontrado " + numTranslationsChecked + " coincidencias", Toast.LENGTH_SHORT).show();

                } else
                    Toast.makeText(getActivity(), "Null word", Toast.LENGTH_SHORT).show();

                LayoutAnimationController animController = AnimationUtils.loadLayoutAnimation(generalBinding.rvGeneral.getContext(), R.anim.rv_translations_animation);
                generalBinding.rvGeneral.setLayoutAnimation(animController);

                progLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return fragView;
    }
}
