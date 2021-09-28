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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import tornasuk.translations.Constantes;
import tornasuk.translations.adapters.TranslationsAdapter;
import tornasuk.translations.classes.Translation;
import tornasuk.translations.dialogs.EditTranslation;
import tornasuk.translations.dialogs.NewTranslation;
import tornasuk.translations.R;
import tornasuk.translations.databinding.GeneralLayoutBinding;

public class GeneralTranslationsFrag extends Fragment {

    private DatabaseReference firebasebdd;
    private TranslationsAdapter translationsAdapter;
    private ArrayList<Translation> translations;
    private ConstraintLayout progLayout;
    private String generalTheme;

    public GeneralTranslationsFrag(){
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        GeneralLayoutBinding generalBinding = GeneralLayoutBinding.inflate(inflater, container,false);
        View fragView = generalBinding.getRoot();

        requireActivity().invalidateOptionsMenu();

        generalTheme = getArguments().getString(getString(R.string.generalArg));

        fragView.setTag(generalTheme);

        progLayout = requireActivity().findViewById(R.id.prog_layout);
        progLayout.setVisibility(View.VISIBLE);

        Animation fabClick = AnimationUtils.loadAnimation(requireActivity(), R.anim.button_click);

        firebasebdd = FirebaseDatabase.getInstance().getReference(generalTheme);

        generalBinding.rvGeneral.setHasFixedSize(true);
        generalBinding.rvGeneral.setLayoutManager(new LinearLayoutManager(getActivity()));

        FloatingActionButton fabTranslation = requireActivity().findViewById(R.id.fabTranslation);
        fabTranslation.setVisibility(View.VISIBLE);

        fabTranslation.setOnClickListener(v -> {
            fabTranslation.startAnimation(fabClick);
            NewTranslation newTranslation = new NewTranslation();
            newTranslation.show(getParentFragmentManager(), "newTranslation");
        });

        firebasebdd.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                translations = new ArrayList<>();

                for (DataSnapshot translationSnapshot : snapshot.getChildren()) {
                    translations.add(translationSnapshot.getValue(Translation.class));
                }

                translations.sort((t1, t2) -> Integer.compare(Integer.parseInt(t2.getId().split("-")[1]), Integer.parseInt(t1.getId().split("-")[1])));

                translationsAdapter = new TranslationsAdapter(getActivity(), translations, generalTheme);
                generalBinding.rvGeneral.setAdapter(translationsAdapter);

                LayoutAnimationController animController = AnimationUtils.loadLayoutAnimation(generalBinding.rvGeneral.getContext(), R.anim.rv_translations_animation);
                generalBinding.rvGeneral.setLayoutAnimation(animController);

                progLayout.setVisibility(View.INVISIBLE);

                ItemTouchHelper ith = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        firebasebdd.child(String.valueOf(translations.get(generalBinding.rvGeneral.getChildAdapterPosition(viewHolder.itemView)).getId())).removeValue();
                        translationsAdapter.notifyDataSetChanged();
                    }
                });
                ith.attachToRecyclerView(generalBinding.rvGeneral);

                translationsAdapter.setOnLongClickListener(v -> {
                    Translation translation = translations.get(generalBinding.rvGeneral.getChildAdapterPosition(v));
                    EditTranslation editTranslation = new EditTranslation(translation);
                    editTranslation.show(getParentFragmentManager(), "editTranslation");
                    return false;
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        return fragView;
    }
}
