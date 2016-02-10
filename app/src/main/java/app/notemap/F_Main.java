package app.notemap;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class F_Main extends Fragment {

    public F_Main() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lay_f_main, container, false);

        NoteMap app = (NoteMap)getActivity().getApplication();
        Firebase ref = app.getRef();

        ref.child("prueba").setValue("Mc Culo");

        Nota notaInicial = new Nota("Ecaib", "Instituto Poblenou", 41.39834,2.20318);
        Nota notaVacia = new Nota();
        ref.child("prueba").child("Notas").child("nota1").setValue(notaInicial);
        ref.child("prueba").child("Notas").child("nota2").setValue(notaVacia);


        ref.child("prueba").child("Notas").child("nota1")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        System.out.println("XXX:" + snapshot.getValue());
                        msgToast(getContext(), "titulo", snapshot.getValue().toString());

                        System.out.println(snapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {

                        msgToast(getContext(), "Error", "Listener");
                    }
                });
        return view;
    }

    public void msgToast (Context context, String tag, String msg){
        Toast.makeText(context, tag + ": " + msg, Toast.LENGTH_LONG).show();
    }
}
