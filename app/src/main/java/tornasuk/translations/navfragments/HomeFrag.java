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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import tornasuk.translations.adapters.HomeAdapter;
import tornasuk.translations.classes.Novel;
import tornasuk.translations.classes.Volume;
import tornasuk.translations.R;
import tornasuk.translations.databinding.HomeLayoutBinding;

public class HomeFrag extends Fragment {

    private HomeLayoutBinding homeBinding;

    private ArrayList<Novel> novels;
    private ArrayList<Volume> volumes;
    private ConstraintLayout progLayout;
    private int numNovelTranslations;
    private int totalCountTranslations;
    private HashMap<String, Integer> novelsTranslationsCount;

    private Animation animFade;

    public HomeFrag(){
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        homeBinding = HomeLayoutBinding.inflate(inflater, container, false);
        View fragView = homeBinding.getRoot();

        fragView.setTag("Inicio");

        DatabaseReference firebasebdd = FirebaseDatabase.getInstance().getReference();

        requireActivity().invalidateOptionsMenu();

        progLayout = requireActivity().findViewById(R.id.prog_layout);
        progLayout.setVisibility(View.VISIBLE);

        Animation animRotate = AnimationUtils.loadAnimation(getActivity(), R.anim.img_rotate);
        homeBinding.imgCount.startAnimation(animRotate);

        homeBinding.rvHome.setHasFixedSize(true);
        homeBinding.rvHome.setLayoutManager(new LinearLayoutManager(requireActivity()));

        homeBinding.fabLastTranslations.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_lastTranslations);
        });

        homeBinding.fabSearch.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_searchTranslations);
        });

        homeBinding.rvHome.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(!recyclerView.canScrollVertically(1))
                    homeBinding.imgDownHome.setVisibility(View.INVISIBLE);
                else
                    homeBinding.imgDownHome.setVisibility(View.VISIBLE);
            }
        });

        firebasebdd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                homeBinding.rvHome.smoothScrollToPosition(0);

                novels = new ArrayList<>();
                novelsTranslationsCount = new HashMap<>();
                totalCountTranslations = 0;

                for(DataSnapshot novelSnap : snapshot.getChildren()){
                    numNovelTranslations = 0;
                    volumes = new ArrayList<>();
                    Novel novel = new Novel(novelSnap.getKey());
                    if(novelSnap.hasChild("Volume 1")){
                        for (DataSnapshot volSnap : snapshot.child(novel.getNovelName()).getChildren()) {
                            if (volSnap.getChildrenCount() > 1) {
                                numNovelTranslations += (int) volSnap.getChildrenCount() - 1;
                                totalCountTranslations += numNovelTranslations;
                            }
                            volumes.add(new Volume());
                        }
                        novel.setVolumes(volumes);
                        novel.setNumNovelTranslations(numNovelTranslations);
                    } else {
                        numNovelTranslations = (int) snapshot.child(novel.getNovelName()).getChildrenCount();
                        totalCountTranslations += numNovelTranslations;
                        novel.setNumNovelTranslations(numNovelTranslations);
                        if(novel.getNovelName().equals("Clannad"))
                            novel.setNovelName(getString(R.string.navTextClannad));
                    }
                    novelsTranslationsCount.put(novel.getNovelName(), numNovelTranslations);
                    novels.add(novel);
                }

                int novelPto = 0;

                for(int i = 0; i < novelsTranslationsCount.size(); i++){
                    try {
                        novelPto = (novelsTranslationsCount.get(novels.get(i).getNovelName()) * 100) / totalCountTranslations;
                    } catch (ArithmeticException ax){
                        novelPto = 0;
                    }
                    novels.get(i).setTranslationsPercent(novelPto);
                }

                novels.sort((n1, n2) -> Integer.compare(n2.getTranslationsPercent(), n1.getTranslationsPercent()));

                homeBinding.txtCountTranslations.setText(String.valueOf(totalCountTranslations));
                animFade = AnimationUtils.loadAnimation(getActivity(), R.anim.count_fade_in);
                homeBinding.txtCountTranslations.startAnimation(animFade);

                HomeAdapter homeAdapter = new HomeAdapter(requireActivity(), novels);
                homeBinding.rvHome.setAdapter(homeAdapter);

                LayoutAnimationController animController = AnimationUtils.loadLayoutAnimation(homeBinding.rvHome.getContext(), R.anim.rv_inicio_animation);
                homeBinding.rvHome.setLayoutAnimation(animController);
                homeBinding.rvHome.getRecycledViewPool().setMaxRecycledViews(0, 0);

                progLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return fragView;
    }
}
