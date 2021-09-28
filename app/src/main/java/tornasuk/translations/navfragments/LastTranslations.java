package tornasuk.translations.navfragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Queue;

import tornasuk.translations.adapters.TranslationsAdapter;
import tornasuk.translations.classes.Translation;
import tornasuk.translations.Constantes;
import tornasuk.translations.dialogs.EditTranslation;
import tornasuk.translations.R;
import tornasuk.translations.databinding.LastTranslationsLayoutBinding;
import tornasuk.translations.room.TranslationRepository;

public class LastTranslations extends Fragment {

    private LastTranslationsLayoutBinding lastTranslationsBinding;

    private TranslationsAdapter translationsAdapter;
    private ConstraintLayout progLayout;
    private ArrayList<Translation> lastTranslations;
    private HashMap<String, Translation> translations;
    private ArrayList<DataSnapshot> volumes;
    private DatabaseReference firebasebdd;

    public LastTranslations(){
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        lastTranslationsBinding = LastTranslationsLayoutBinding.inflate(inflater, container, false);
        View fragView = lastTranslationsBinding.getRoot();

        fragView.setTag("Last Translations");

        requireActivity().invalidateOptionsMenu();

        firebasebdd = FirebaseDatabase.getInstance().getReference();

        progLayout = requireActivity().findViewById(R.id.prog_layout);
        progLayout.setVisibility(View.VISIBLE);

        lastTranslationsBinding.rvLastTranslations.setHasFixedSize(true);
        lastTranslationsBinding.rvLastTranslations.setLayoutManager(new LinearLayoutManager(getActivity()));
        lastTranslationsBinding.rvLastTranslations.setVerticalScrollBarEnabled(false);

        firebasebdd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                translations = new HashMap<>();
                volumes = new ArrayList<>();
                for (DataSnapshot novelSnap : snapshot.getChildren()) {
                    if(!novelSnap.getChildren().iterator().next().getKey().contains("Volume")) {
                        for (DataSnapshot translationSnap : novelSnap.getChildren()){
                            if(!translationSnap.getKey().equals("volImg")) {
                                try {
                                    translations.put(translationSnap.getKey(), translationSnap.getValue(Translation.class));
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
                                translations.put(translationSnap.getKey(), translationSnap.getValue(Translation.class));
                            } catch (DatabaseException ignored) {
                            }
                        }
                    }
                }

                new Thread(() -> {
                    TranslationRepository roomRepo = new TranslationRepository(requireActivity().getApplication());
                    lastTranslations = (ArrayList<Translation>) roomRepo.getLastTranslations();
                    for (Translation translationFor : lastTranslations){
                        if(translations.containsKey(translationFor.getId())) {
                            Translation translation = translations.get(translationFor.getId());
                            if(!translation.getWord().equals(translationFor.getWord()) ||
                                    !translation.getWordTranslation().equals(translationFor.getWordTranslation())) {
                                roomRepo.updateTranslation(translations.get(translationFor.getId()));
                                translationFor.setWord(translation.getWord());
                                translationFor.setWordTranslation(translation.getWordTranslation());
                            }
                        } else {
                            roomRepo.deleteTranslation(translationFor);
                            lastTranslations.remove(translationFor);
                        }
                    }
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Collections.reverse(lastTranslations);
                        translationsAdapter = new TranslationsAdapter(getActivity(), lastTranslations, "Last");
                        lastTranslationsBinding.rvLastTranslations.setAdapter(translationsAdapter);

                        LayoutAnimationController animController = AnimationUtils.loadLayoutAnimation(lastTranslationsBinding.rvLastTranslations.getContext(), R.anim.rv_translations_animation);
                        lastTranslationsBinding.rvLastTranslations.setLayoutAnimation(animController);

                        if(progLayout.getVisibility() == View.VISIBLE)
                            progLayout.setVisibility(View.INVISIBLE);

                        translationsAdapter.setOnClickListener(v -> {
                            Translation translation = lastTranslations.get(lastTranslationsBinding.rvLastTranslations.getChildAdapterPosition(v));
                            Bundle data = new Bundle();
                            String novel = translation.getNovel();
                            if (novel == null) {
                                if (translation.getId().split("-")[0].equals(Constantes.ID_CLANNAD))
                                    Navigation.findNavController(requireActivity(), R.id.nav_host_frag).navigate(R.id.nav_clannad);
                                else
                                    Navigation.findNavController(requireActivity(), R.id.nav_host_frag).navigate(R.id.nav_general);
                            }else {
                                int numPg = 0;
                                switch (translation.getNovel()){
                                    case "Classroom of the Elite":
                                        numPg = Integer.parseInt(translation.getId().split("-")[0].replace(Constantes.ID_CLASSROOM, ""));
                                        break;
                                    case "Overlord":
                                        numPg = Integer.parseInt(translation.getId().split("-")[0].replace(Constantes.ID_OVERLORD, ""));
                                        break;
                                    case "Log Horizon":
                                        numPg = Integer.parseInt(translation.getId().split("-")[0].replace(Constantes.ID_LOGHORIZON, ""));
                                        break;
                                    case "No Game No Life":
                                        numPg = Integer.parseInt(translation.getId().split("-")[0].replace(Constantes.ID_NGNL, ""));
                                        break;
                                }

                                data.putInt("Pg", numPg);
                                data.putString("Novel", translation.getNovel());
                                data.putString("Volume", translation.getVolume());
                                Navigation.findNavController(requireActivity(), R.id.nav_host_frag).navigate(R.id.nav_translations, data);
                            }
                        });

                        translationsAdapter.setOnLongClickListener(v -> {
                            Translation translation = lastTranslations.get(lastTranslationsBinding.rvLastTranslations.getChildAdapterPosition(v));
                            EditTranslation editTranslation = new EditTranslation(translation);
                            editTranslation.show(getParentFragmentManager(), "editTranslation");
                            return false;
                        });
                    });
                }).start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return fragView;
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
