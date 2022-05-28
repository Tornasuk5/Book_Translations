package tornasuk.translations.navfragments;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;

import tornasuk.translations.adapters.PagesAdapter;
import tornasuk.translations.classes.Page;
import tornasuk.translations.classes.Translation;
import tornasuk.translations.Constantes;
import tornasuk.translations.dialogs.NewPag;
import tornasuk.translations.R;
import tornasuk.translations.databinding.PagesLayoutBinding;

public class PagesFrag extends Fragment {

    private PagesLayoutBinding pagesBinding;

    private DatabaseReference firebasebdd;
    private PagesAdapter pagAdapter;
    private ArrayList<Page> pages;
    private ArrayList<Translation> translations;
    private ArrayList<String> deletedPags;
    private String novel;
    private String volume;
    private ConstraintLayout progLayout;
    private int pagPos;

    public PagesFrag(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        pagesBinding = PagesLayoutBinding.inflate(inflater, container, false);
        View fragView = pagesBinding.getRoot();

        requireActivity().invalidateOptionsMenu();

        novel = getArguments().getString("Novel");
        volume = getArguments().getString("Volume");

        fragView.setTag("Páginas "+ novel);

        progLayout = requireActivity().findViewById(R.id.prog_layout);
        progLayout.setVisibility(View.VISIBLE);

        firebasebdd = FirebaseDatabase.getInstance().getReference(novel).child(volume);

        pagesBinding.rvPages.setHasFixedSize(true);
        pagesBinding.rvPages.setLayoutManager(new LinearLayoutManager(getActivity()));

        switch (novel) {
            case "Log Horizon":
                pagesBinding.imgBackgroundPages.setPaddingRelative(275,240,240,240);
                pagesBinding.imgBackgroundPages.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.log_shiroe2));
                break;
            case "Classroom of the Elite":
                pagesBinding.imgBackgroundPages.setPaddingRelative(333,225,225,225);
                pagesBinding.imgBackgroundPages.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.class_ayanokouji));
                break;
            case "No Game No Life":
                pagesBinding.imgBackgroundPages.setPaddingRelative(220,220,247,220);
                pagesBinding.imgBackgroundPages.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ngnl_sora));
                break;
        }

        loadPags();

        pagesBinding.fabPagesDown.setVisibility(View.INVISIBLE);

        pagesBinding.fabPagesDown.setOnClickListener(v -> {
            if(pagesBinding.rvPages != null){
                pagesBinding.rvPages.scrollToPosition(pagAdapter.getItemCount()-1);
            }
        });

        pagesBinding.rvPages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(!recyclerView.canScrollVertically(1))
                    pagesBinding.fabPagesDown.setVisibility(View.INVISIBLE);
                else
                    pagesBinding.fabPagesDown.setVisibility(View.VISIBLE);
            }
        });

        return fragView;
    }
    
    private void loadPags(){
        firebasebdd.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int numPg = 0;
                int lastNumPg = 0;
                int countTranslations = 0;
                pages = new ArrayList<>();
                translations = new ArrayList<>();

                for(DataSnapshot translationSnap : snapshot.getChildren()){
                    countTranslations++;
                    if(!translationSnap.getKey().equals("volImg")){
                        numPg = Integer.parseInt(translationSnap.getKey().split("-")[0].replace(Constantes.refID, ""));
                        if(lastNumPg != 0 && numPg != lastNumPg){
                            pages.add(new Page(lastNumPg, translations));
                            translations = new ArrayList<>();
                        }
                        try {
                            translations.add(translationSnap.getValue(Translation.class));
                        } catch (DatabaseException ignored){
                        }
                    }
                    if(countTranslations == snapshot.getChildrenCount()){
                        if(countTranslations > 1)
                            pages.add(new Page(numPg, translations));
                    } else
                        lastNumPg = numPg;
                }

                pages.sort(Comparator.comparingInt(Page::getNumPage));

                pages.add(new Page());

                pagAdapter = new PagesAdapter(getActivity(), pages);
                pagesBinding.rvPages.setAdapter(pagAdapter);

                LayoutAnimationController animController = AnimationUtils.loadLayoutAnimation(pagesBinding.rvPages.getContext(), R.anim.rv_translations_animation);
                pagesBinding.rvPages.setLayoutAnimation(animController);

                if(pagAdapter.getItemCount() > 10)
                    pagesBinding.fabPagesDown.setVisibility(View.VISIBLE);

                pagesBinding.rvPages.getRecycledViewPool().setMaxRecycledViews(0, 0);

                progLayout.setVisibility(View.INVISIBLE);

                registerForContextMenu(pagesBinding.rvPages);

                pagAdapter.setOnClickPgListener(v -> {
                    if(pagesBinding.rvPages.getChildAdapterPosition(v) == pagAdapter.getItemCount() - 1){
                        NewPag newPag = new NewPag(novel, volume);
                        newPag.show(getParentFragmentManager(), "newPag");
                    } else {
                        Bundle data = new Bundle();
                        data.putInt("Pg", pages.get(pagesBinding.rvPages.getChildAdapterPosition(v)).getNumPage());
                        data.putString("Novel", novel);
                        data.putString("Volume", volume);
                        Navigation.findNavController(v).navigate(R.id.nav_translations, data);
                    }
                });

                pagAdapter.setOnLongClickPgListener(v -> {
                    // BORRA LA PAG Y TODAS SUS TRADUCCIONES
                    pagPos = pagesBinding.rvPages.getChildAdapterPosition(v);
                    deletedPags = new ArrayList<>();
                    int countPg;
                    try {
                        countPg = pages.get(pagPos).getTranslations().size();
                        if (countPg == 0) {
                            String pg = Constantes.refID + pages.get(pagPos).getNumPage() + "-" + (1);
                            deletedPags.add(pg);
                        }
                    } catch(NullPointerException npx){
                        countPg = 0;
                    }
                    if(countPg > 0){
                        for (int i = 1; i <= countPg; i++) {
                            String pg = Constantes.refID + pages.get(pagPos).getNumPage() + "-" + (i);
                            deletedPags.add(pg);
                        }
                    }
                    return false;
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        if(pagPos != pagAdapter.getItemCount() - 1)
            menu.add(0,v.getId(),0,"Eliminar página");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        for(int i = 0; i < deletedPags.size(); i++){
            firebasebdd.child(deletedPags.get(i)).removeValue();
        }
        deletedPags.clear();
        Toast.makeText(getActivity(), R.string.deletedPag, Toast.LENGTH_SHORT).show();
        return super.onContextItemSelected(item);
    }
}
