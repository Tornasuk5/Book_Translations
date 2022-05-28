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

import java.util.ArrayList;

import tornasuk.translations.adapters.TranslationsAdapter;
import tornasuk.translations.classes.Translation;
import tornasuk.translations.R;
import tornasuk.translations.databinding.GeneralLayoutBinding;

public class CheckWordsFrag extends Fragment {

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
            ArrayList<Translation> translationsChecked = (ArrayList<Translation>) getArguments().getSerializable("Checked translations");

            ConstraintLayout progLayout = getActivity().findViewById(R.id.prog_layout);
            progLayout.setVisibility(View.VISIBLE);

            generalBinding.rvGeneral.setHasFixedSize(true);
            generalBinding.rvGeneral.setLayoutManager(new LinearLayoutManager(getActivity()));

            TranslationsAdapter translationsAdapter = new TranslationsAdapter(getActivity(), translationsChecked, "");
            generalBinding.rvGeneral.setAdapter(translationsAdapter);

            int numTranslationsChecked = translationsChecked.size();

            if(numTranslationsChecked == 0)
                Toast.makeText(getActivity(), R.string.checkWordCoincidencias1, Toast.LENGTH_SHORT).show();
            else if(numTranslationsChecked == 1)
                Toast.makeText(getActivity(), R.string.checkWordCoincidencias2 , Toast.LENGTH_SHORT).show();
            else if(numTranslationsChecked > 1)
                Toast.makeText(getActivity(), "Se han encontrado " + numTranslationsChecked + " coincidencias", Toast.LENGTH_SHORT).show();

            LayoutAnimationController animController = AnimationUtils.loadLayoutAnimation(generalBinding.rvGeneral.getContext(), R.anim.rv_translations_animation);
            generalBinding.rvGeneral.setLayoutAnimation(animController);

            progLayout.setVisibility(View.INVISIBLE);

        } catch (NullPointerException npx){
            Toast.makeText(getActivity(),R.string.errorCheckingWord , Toast.LENGTH_SHORT).show();
        }

        return fragView;
    }
}
