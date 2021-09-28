package tornasuk.translations.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import tornasuk.translations.classes.Volume;
import tornasuk.translations.R;
import tornasuk.translations.databinding.ListVolumesBinding;

public class VolumesAdapter extends RecyclerView.Adapter<VolumesAdapter.LibroViewHolder>{

    private final ArrayList<Volume> volumes;
    private final Context context;
    private View.OnClickListener clickPgListener;
    private View.OnLongClickListener longListener;
    private Animation libroClick;

    public VolumesAdapter(Context context, List<Volume> volumes) {
        this.volumes = (ArrayList<Volume>) volumes;
        this.context = context;
    }

    public void setOnClickPgListener(View.OnClickListener clickPgListener){
        this.clickPgListener = clickPgListener;
    }

    public void setOnLongClickPgListener(View.OnLongClickListener longListener){
        this.longListener = longListener;
    }

    public class LibroViewHolder extends RecyclerView.ViewHolder { // MÉTODO QUE LEE LOS DATOS DE LOS POINTS
        ListVolumesBinding listVolumesBinding;

        public LibroViewHolder(ListVolumesBinding binding) {
            super(binding.getRoot());
            listVolumesBinding = binding;
            libroClick = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.libro_click);
        }

        public void onBindVolume(Volume volume){
            if(volume.getNameVolumen() != null){
                if(volume.getNameVolumen().equals("Volume 4,5"))
                    listVolumesBinding.txtNameVol.setText("4.5");
                else
                    listVolumesBinding.txtNameVol.setText(volume.getNameVolumen().replace("Volume ", ""));

                Picasso.get().load(volume.getVolImg()).fit().into(listVolumesBinding.imgVol);

            } else {
                listVolumesBinding.imgVol.setPaddingRelative(100,80,80,80);
                listVolumesBinding.imgVol.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.add_libro));
                listVolumesBinding.txtNameVol.setVisibility(View.INVISIBLE);
            }

            listVolumesBinding.clVolumes.setOnClickListener(v -> {
                if(clickPgListener != null) {
                    clickPgListener.onClick(v);
                    listVolumesBinding.clVolumes.startAnimation(libroClick);
                }
            });

            listVolumesBinding.clVolumes.setOnLongClickListener(v -> {
                if(longListener != null) {
                    longListener.onLongClick(v);
                    listVolumesBinding.clVolumes.startAnimation(libroClick);
                }
                return false;
            });
        }
    }

    @NonNull
    @Override
    public VolumesAdapter.LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // INFLA EL LAYOUT
        return new VolumesAdapter.LibroViewHolder(ListVolumesBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(VolumesAdapter.LibroViewHolder holder, int i) { // LEE LOS DATOS DE CADA PUNTO Y LOS MUESTRA EN EL LAYOUT
        holder.onBindVolume(volumes.get(i));
    }

    @Override
    public int getItemCount() { // DEVUELVE EL Nº DE DATOS QUE HAY EN EL ARRAYLIST
        return volumes.size();
    }

}
