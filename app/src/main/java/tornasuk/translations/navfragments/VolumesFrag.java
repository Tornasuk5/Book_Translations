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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import tornasuk.translations.adapters.VolumesAdapter;
import tornasuk.translations.classes.Translation;
import tornasuk.translations.classes.Volume;
import tornasuk.translations.dialogs.NewVolume;
import tornasuk.translations.R;
import tornasuk.translations.databinding.VolumesLayoutBinding;

public class VolumesFrag extends Fragment {

    private VolumesLayoutBinding volumesBinding;
    private DatabaseReference firebasebdd;
    private VolumesAdapter volumesAdapter;
    private ArrayList<Volume> volumes;
    private ArrayList<Translation> translations;
    private String novel;
    private String volume;
    private ConstraintLayout progLayout;
    private int libroPos;

    public VolumesFrag(){
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        volumesBinding = VolumesLayoutBinding.inflate(inflater, container, false);
        View fragView = volumesBinding.getRoot();

        fragView.setTag("Volumes");

        requireActivity().invalidateOptionsMenu();

        novel = getArguments().getString(getString(R.string.novelArg));

        progLayout = requireActivity().findViewById(R.id.prog_layout);
        progLayout.setVisibility(View.VISIBLE);

        firebasebdd = FirebaseDatabase.getInstance().getReference(novel);

        volumesBinding.rvVolumes.setHasFixedSize(true);
        volumesBinding.rvVolumes.setLayoutManager(new GridLayoutManager(getActivity(),3));

        firebasebdd.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                volumes = new ArrayList<>();

                for (DataSnapshot volSnap : snapshot.getChildren()) {
                    Volume volume = new Volume(volSnap.getKey());
                    translations = new ArrayList<>();
                    for(DataSnapshot translationSnap : volSnap.getChildren()){
                        if(!translationSnap.getKey().equals("volImg")){
                            try {
                                translations.add(translationSnap.getValue(Translation.class));
                            } catch (DatabaseException ignored){
                            }
                        } else volume.setVolImg(String.valueOf(translationSnap.getValue()));
                    }
                    volume.setTranslations(translations);
                    volumes.add(volume);
                }

                volumes.sort((v1, v2) -> {
                    int vol1 = 0;
                    int vol2 = 0;
                    try {
                        vol1 = Integer.parseInt(v1.getNameVolumen().replace("Volume ", ""));
                        vol2 = Integer.parseInt(v2.getNameVolumen().replace("Volume ", ""));
                    } catch (NumberFormatException nfx){

                    }
                    return Integer.compare(vol1, vol2);
                });

                volumes.add(new Volume()); // AÑADIR NUEVO VOLUMEN

                volumesAdapter = new VolumesAdapter(getActivity(), volumes);
                volumesBinding.rvVolumes.setAdapter(volumesAdapter);

                LayoutAnimationController animController = AnimationUtils.loadLayoutAnimation(volumesBinding.rvVolumes.getContext(), R.anim.rv_libros_animation);
                volumesBinding.rvVolumes.setLayoutAnimation(animController);

                volumesBinding.rvVolumes.getRecycledViewPool().setMaxRecycledViews(0, 0);

                progLayout.setVisibility(View.INVISIBLE);

                registerForContextMenu(volumesBinding.rvVolumes);

                volumesAdapter.setOnClickPgListener(v -> {
                    if(volumesBinding.rvVolumes.getChildAdapterPosition(v) == volumesAdapter.getItemCount() - 1){
                        NewVolume newVolume = new NewVolume(novel);
                        newVolume.show(getParentFragmentManager(), "newVolume");
                    } else {
                        Bundle libroData = new Bundle();
                        libroData.putString("Novel", novel);
                        libroData.putString("Volume", volumes.get(volumesBinding.rvVolumes.getChildAdapterPosition(v)).getNameVolumen());
                        Navigation.findNavController(v).navigate(R.id.nav_libroPags, libroData);
                    }
                });

                volumesAdapter.setOnLongClickPgListener(v -> {
                    libroPos = volumesBinding.rvVolumes.getChildAdapterPosition(v);
                    volume = volumes.get(libroPos).getNameVolumen();
                    return false;
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return fragView;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        if(libroPos != volumesAdapter.getItemCount() - 1)
            menu.add(0,v.getId(),0,"Eliminar volumen");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        firebasebdd.child(volume).removeValue();
        Toast.makeText(getActivity(), volume + " eliminado de " + novel, Toast.LENGTH_SHORT).show();
        return super.onContextItemSelected(item);
    }
}
