package tornasuk.translations.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tornasuk.translations.classes.Volume;
import tornasuk.translations.R;
import tornasuk.translations.databinding.DialogNewVolumeBinding;

public class NewVolume extends AppCompatDialogFragment {

    private DatabaseReference firebasebdd;
    private String libro;

    public NewVolume(String libro) {
        this.libro = libro;
    }

    private androidx.appcompat.app.AlertDialog newVolume() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireActivity(), R.style.DialogBackground);
        DialogNewVolumeBinding newVolumeBinding = DialogNewVolumeBinding.inflate(LayoutInflater.from(requireActivity()));
        builder.setView(newVolumeBinding.getRoot());

        firebasebdd = FirebaseDatabase.getInstance().getReference(libro);

        firebasebdd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot snapshot) {
                newVolumeBinding.btnAddVolume.setOnClickListener(v1 -> {
                    String editVol = newVolumeBinding.editTextVolume.getText().toString();
                    if(!editVol.trim().equals("")) {
                        String vol = "Volume " + editVol;
                        boolean volRepeated = false;
                        for(DataSnapshot volumen : snapshot.getChildren()){
                            if(volumen.getKey().equals(vol)){
                                volRepeated = true;
                                break;
                            }
                        }
                        if(!volRepeated){
                            Volume volume = new Volume(vol, "img");
                            firebasebdd.child(vol).setValue(volume);
                            Toast.makeText(requireActivity(), R.string.volumeAdded, Toast.LENGTH_SHORT).show();
                            dismiss();
                        } else
                            Toast.makeText(requireActivity(), R.string.volRepeated, Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(requireActivity(), R.string.editTextBlank, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return builder.create();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return newVolume();
    }
}
