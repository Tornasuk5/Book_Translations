package tornasuk.translations.navfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;

import tornasuk.translations.adapters.TranslationsAdapter;
import tornasuk.translations.classes.Translation;
import tornasuk.translations.Constantes;
import tornasuk.translations.dialogs.EditTranslation;
import tornasuk.translations.dialogs.NewTranslation;
import tornasuk.translations.R;
import tornasuk.translations.databinding.TranslationsLayoutBinding;

public class NovelTranslationsFrag extends Fragment {

    private DatabaseReference firebasebdd;
    private TranslationsAdapter translationsAdapter;
    private ArrayList<Translation> translations;
    private Translation translation;
    private int numPag;
    private String idTranslation;
    private String novel;
    private String volume;
    private ConstraintLayout progLayout;

    public NovelTranslationsFrag(){
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TranslationsLayoutBinding translationsBinding = TranslationsLayoutBinding.inflate(inflater, container, false);
        View fragView = translationsBinding.getRoot();

        requireActivity().invalidateOptionsMenu();

        try {
            numPag = getArguments().getInt("Pg");
            novel = getArguments().getString("Novel");
            volume = getArguments().getString("Volume");
        } catch (NullPointerException npx){

        }

        fragView.setTag("Translations " + novel);

        Animation fabClick = AnimationUtils.loadAnimation(getActivity(), R.anim.button_click);

        progLayout = requireActivity().findViewById(R.id.prog_layout);
        progLayout.setVisibility(View.VISIBLE);

        FloatingActionButton fab = requireActivity().findViewById(R.id.fabTranslation);
        fab.setVisibility(View.VISIBLE);

        if(numPag > 0){
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Página " + numPag);

            firebasebdd = FirebaseDatabase.getInstance().getReference(novel).child(volume);

            translationsBinding.rvTranslations.setHasFixedSize(true);
            translationsBinding.rvTranslations.setLayoutManager(new LinearLayoutManager(getActivity()));

            FloatingActionButton fabTranslation = requireActivity().findViewById(R.id.fabTranslation);

            fabTranslation.setOnClickListener(v -> {
                fabTranslation.startAnimation(fabClick);
                NewTranslation newTranslation = new NewTranslation(novel, volume, numPag);
                newTranslation.show(getParentFragmentManager(), "newTranslation");
            });

            firebasebdd.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    translations = new ArrayList<>();
                    boolean translationsDetected = false;
                    for (DataSnapshot translationSnap : snapshot.getChildren()) {
                        if(!translationSnap.getKey().equals("volImg")) {
                            try {
                                translation = translationSnap.getValue(Translation.class);
                                String translationPag = translation.getId().split("-")[0];
                                if(translationPag.split(Constantes.refID)[1].equals(String.valueOf(numPag))) {
                                    translations.add(translation);
                                    translationsDetected = true;
                                } else if(translationsDetected)
                                    break;
                            } catch (DatabaseException ignored){
                            }
                        }
                    }

                    translations.sort(Comparator.comparingInt(t -> Integer.parseInt(t.getId().split("-")[1])));

                    translationsAdapter = new TranslationsAdapter(getActivity(), translations, "");
                    translationsBinding.rvTranslations.setAdapter(translationsAdapter);

                    LayoutAnimationController animController = AnimationUtils.loadLayoutAnimation(translationsBinding.rvTranslations.getContext(), R.anim.rv_translations_animation);
                    translationsBinding.rvTranslations.setLayoutAnimation(animController);

                    progLayout.setVisibility(View.INVISIBLE);

                    ItemTouchHelper ith = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                        @Override
                        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                            return false;
                        }

                        @Override
                        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                            firebasebdd.child(String.valueOf(translations.get(translationsBinding.rvTranslations.getChildAdapterPosition(viewHolder.itemView)).getId())).removeValue();
                            translationsAdapter.notifyDataSetChanged();
                        }
                    });
                    ith.attachToRecyclerView(translationsBinding.rvTranslations);

                    translationsAdapter.setOnLongClickListener(v -> {
                        Translation translation = translations.get(translationsBinding.rvTranslations.getChildAdapterPosition(v));
                        EditTranslation editTranslation = new EditTranslation(translation);
                        editTranslation.show(getParentFragmentManager(), "editTranslation");
                        return false;
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        return fragView;
    }
}
