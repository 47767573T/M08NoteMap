package app.notemap;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class F_Main extends Fragment implements View.OnClickListener, View.OnLongClickListener{


    public static final int CODIGO_SOLICITUD_RECONOCIMIENTO = 1;


    private ImageButton btHablar;
    private ImageButton btMapear;
    private ImageButton btAnotar;

    private EditText etAnotado;


    public F_Main() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lay_f_main, container, false);

        setInicialListeners(view);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imbtVoz:
                Intent intHabla = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intHabla.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                try {
                    startActivityForResult(intHabla, CODIGO_SOLICITUD_RECONOCIMIENTO);
                    etAnotado.setText("");

                } catch (ActivityNotFoundException a) {
                    msgToast(getContext(), "ERROR: ", "reconocimiento de voz");
                }
                break;

        }

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    public void setInicialListeners(View v){

        btHablar = (ImageButton) v.findViewById(R.id.imbtVoz);
        btHablar.setOnClickListener(this);
        btMapear = (ImageButton) v.findViewById(R.id.imbtMap);
        btHablar.setOnClickListener(this);
        btAnotar = (ImageButton) v.findViewById(R.id.imbtNota);
        btHablar.setOnClickListener(this);

        etAnotado = (EditText) v.findViewById(R.id.edtxNota);

    }

    @Override
    public void onActivityResult(int codigoSolicitud, int resultCode, Intent intent) {
        super.onActivityResult(codigoSolicitud, resultCode, intent);

        //Validamos si el codigo de solicitud es el mismo y si la voz captada no da error
        switch (codigoSolicitud) {
            case CODIGO_SOLICITUD_RECONOCIMIENTO: {
                if (resultCode == A_Main.RESULT_OK && null != intent) {

                    ArrayList<String> listReconocimeintoPorVoz = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    etAnotado.setText(listReconocimeintoPorVoz.get(0));   //Aqui escribe el resultado en el textView indicado
                    msgToast(getContext(), "PRUEBA: ", "captacion de voz");
                }
                break;
            }
        }
    }

}
