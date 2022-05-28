package tornasuk.translations.navfragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;

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

import java.util.ArrayList;

import tornasuk.translations.Utils;
import tornasuk.translations.adapters.TranslationsAdapter;
import tornasuk.translations.classes.Translation;
import tornasuk.translations.Constantes;
import tornasuk.translations.dialogs.EditTranslation;
import tornasuk.translations.R;
import tornasuk.translations.databinding.SearchTranslationsLayoutBinding;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SearchTranslations extends Fragment {

    private SearchTranslationsLayoutBinding searchBinding;

    private DatabaseReference firebasebdd;
    private TranslationsAdapter translationsAdapter;
    private ConstraintLayout progLayout;
    private ArrayList<DataSnapshot> volumes;
    private ArrayList<Translation> translations;
    private Translation translation;
    private Animation fabOpen, fabClose;

    public SearchTranslations(){
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        searchBinding = SearchTranslationsLayoutBinding.inflate(inflater, container, false);
        View fragView = searchBinding.getRoot();

        fragView.setTag("Search Translations");

        requireActivity().invalidateOptionsMenu();

        progLayout = requireActivity().findViewById(R.id.prog_layout);
        progLayout.setVisibility(View.VISIBLE);

        fabOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.button_open);
        fabClose = AnimationUtils.loadAnimation(getActivity(), R.anim.button_close);

        searchBinding.clFabsSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus)
                searchBinding.clFabsSearch.setVisibility(View.INVISIBLE);
        });

        firebasebdd = FirebaseDatabase.getInstance().getReference();

        searchBinding.rvSearchTranslations.setHasFixedSize(true);
        searchBinding.rvSearchTranslations.setLayoutManager(new LinearLayoutManager(getActivity()));

        searchBinding.fabSearch2.setOnClickListener(v -> {
            if(searchBinding.clFabsSearch.getVisibility() == View.INVISIBLE){
                searchBinding.clFabsSearch.setVisibility(View.VISIBLE);
                searchBinding.fabSearchTranslation.startAnimation(fabOpen);
                searchBinding.fabSearchWord.startAnimation(fabOpen);
                searchBinding.clFabsSearch.requestFocus();
                searchBinding.editSearch.setText("");
            } else
                closeSortAnimation();

        });

        searchBinding.fabSearchWord.setOnClickListener(v -> setSearchMode("Word"));

        searchBinding.fabSearchTranslation.setOnClickListener(v -> setSearchMode("Translation"));

        searchBinding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(translationsAdapter != null)
                    translationsAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        firebasebdd.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                volumes = new ArrayList<>();
                translations = new ArrayList<>();

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

                translationsAdapter = new TranslationsAdapter(getActivity(), translations, "Search");
                searchBinding.rvSearchTranslations.setAdapter(translationsAdapter);

                LayoutAnimationController animController = AnimationUtils.loadLayoutAnimation(searchBinding.rvSearchTranslations.getContext(), R.anim.rv_translations_animation);
                searchBinding.rvSearchTranslations.setLayoutAnimation(animController);

                searchBinding.rvSearchTranslations.getRecycledViewPool().setMaxRecycledViews(0, 0);

                if(progLayout.getVisibility() == View.VISIBLE)
                    progLayout.setVisibility(View.INVISIBLE);

                translationsAdapter.setOnClickListener(v -> {
                    translation = translations.get(searchBinding.rvSearchTranslations.getChildAdapterPosition(v));
                    Bundle data = new Bundle();
                    String novel = translation.getNovel();
                    if (novel == null) {
                        if (translation.getId().split("-")[0].equals(Constantes.ID_CLANNAD))
                            Navigation.findNavController(requireActivity(), R.id.nav_host_frag).navigate(R.id.nav_clannad);
                        else
                            Navigation.findNavController(requireActivity(), R.id.nav_host_frag).navigate(R.id.nav_general);
                    } else {
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

                ItemTouchHelper ith = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        translation = translations.get(searchBinding.rvSearchTranslations.getChildAdapterPosition(viewHolder.itemView));

                        if(translation.getNovel() == null) {
                            firebasebdd.child(Utils.getThemeFromID(translation.getId()))
                                    .child(translation.getId())
                                    .removeValue();
                        } else {
                            firebasebdd.child(translation.getNovel())
                                    .child(translation.getVolume())
                                    .child(translation.getId())
                                    .removeValue();
                        }
                    }
                });
                ith.attachToRecyclerView(searchBinding.rvSearchTranslations);

                translationsAdapter.setOnLongClickListener(v -> {
                    translation = translations.get(searchBinding.rvSearchTranslations.getChildAdapterPosition(v));
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

    public void setSearchMode(String searchType){
        searchBinding.rvSearchTranslations.setVisibility(View.VISIBLE);

        if(!searchBinding.editSearch.isEnabled())
            searchBinding.editSearch.setEnabled(true);
        searchBinding.editSearch.requestFocus();

        if(searchType.equals("Word"))
            searchBinding.editSearch.setHint("Word");
        else
            searchBinding.editSearch.setHint("Translation");

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchBinding.editSearch, InputMethodManager.SHOW_IMPLICIT);

        translationsAdapter.setFilterType(searchType);
    }

    private void closeSortAnimation(){
        searchBinding.fabSearchTranslation.startAnimation(fabClose);
        searchBinding.fabSearchWord.startAnimation(fabClose);
        fabClose.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                searchBinding.clFabsSearch.setVisibility(View.INVISIBLE);
                searchBinding.clFabsSearch.clearFocus();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        searchBinding.editSearch.clearFocus();
        searchBinding.editSearch.setText("");
        hideKeyboard();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(INPUT_METHOD_SERVICE);
        View view = requireActivity().getCurrentFocus();
        if (view == null) {
            view = new View(requireActivity());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
