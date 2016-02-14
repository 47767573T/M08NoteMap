package app.notemap;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Date;
import java.util.Map;
import java.util.Random;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

/**
 * A placeholder fragment containing a simple view.
 */
public class F_Main extends Fragment implements View.OnClickListener, View.OnLongClickListener
        , LocationListener {

    //Variables para reconocimiento de voz
    public static final int CODIGO_SOLICITUD_RECONOCIMIENTO = 1;

    //Variables para Localizacion
    public Location lugarActual;
    public ArrayList<Location> lugares;
    LocationManager locManager;
    LocationListener locListener;
    ArrayList<Nota> notasLista;


    //Layout items
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

        notasLista = new ArrayList<>();

        setClickListeners(view);      //Inicia los listeners de los botones del layout
        setLocationListeners(getContext());


        NoteMap app = (NoteMap) getActivity().getApplication();
        Firebase ref = app.getRef();

        //Pruebas con firebase
        ref.child("prueba").setValue("Mc Culo");
        Nota notaInicial = new Nota("Ecaib", "Instituto Poblenou", 41.39834, 2.20318);

        ref.child("prueba").child("Notas").child("nota1").setValue(notaInicial);

        addNotesEnFirebase(5, ref);     //Genera notas de coordenadas aleatorias;

        ref.child("prueba").child("Notas")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot notasSnapshot: snapshot.getChildren()){
                            Nota nota = notasSnapshot.getValue(Nota.class);
                            notasLista.add(nota);
                            Log.e(nota.getTitulo(), nota.getDesc());
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                        msgToast(2, "DataListener de Firebase Cancelado");
                    }
                });


        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imbtVoz:
                Intent intHabla = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intHabla.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                try {
                    startActivityForResult(intHabla, CODIGO_SOLICITUD_RECONOCIMIENTO);
                    etAnotado.setText("");

                } catch (ActivityNotFoundException a) {
                    msgToast(2, "reconocimiento de voz");
                }
                break;

            case R.id.imbtNota:
                break;

            case R.id.imbtMap:
                msgToast(1, lugarActual.getAltitude()+"-"+lugarActual.getLongitude());
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.edtxNota:
                break;
        }
        return false;
    }

    public void setClickListeners(View v) {

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
        if (resultCode == A_Main.RESULT_OK              //Comprueba que hay respuesta
                && intent != null                       //Comprueba que se recibe el intent
                && codigoSolicitud == CODIGO_SOLICITUD_RECONOCIMIENTO) {    //Comprueba el codigo de solicitud

            ArrayList<String> listNotasPorVoz = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            etAnotado.setText(listNotasPorVoz.get(0));  //Aqui escribe el resultado en el textView indicado
            //msgToast(1, "captacion de voz");
        }
    }

    //METODOS PARA LOCATION LISTENER................................................................
    public void setLocationListeners(Context context) {
        locManager = (LocationManager) getContext().getSystemService(context.LOCATION_SERVICE);

        //locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, );

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return ;
        }
        locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    private int checkSelfPermission(String accessFineLocation) {return 0;}


    @Override
    public void onLocationChanged(Location location) {
        locListener = this;
        if (location!=null) {
            lugarActual = location;
           msgToast(1, "Localizacion actual: "+ lugarActual.getAltitude()+"-"+lugarActual.getLongitude());

        }else{
            msgToast(2, "No se encuentra Location");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {msgToast(3, "Red Activada");}

    @Override
    public void onProviderDisabled(String provider) {msgToast(3, "Red Desactivado");}
    //FIN DE METODOS PARA LOCATION LISTENER.........................................................



    //METODOS VARIOS................................................................................
    public void msgToast (int numTag, String msg){
        switch (numTag){
            case 1:
                Toast.makeText(getContext(), "PRUEBA: " + msg, Toast.LENGTH_LONG).show();
                break;
            case 2:
                Toast.makeText(getContext(), "ERROR: " + msg, Toast.LENGTH_LONG).show();
                break;
            case 3:
                Toast.makeText(getContext(), "INFO: " + msg, Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void addNotesEnFirebase (int numVeces, Firebase fb){//41.39834, 2.20318 ECAIB
        Random rnd = new Random();

        for (int i = 2; i < numVeces+2; i++) {
            int lat = rnd.nextInt(10000);
            int lon = rnd.nextInt(10000);
            Double latDb = (Double.valueOf(lat+4130000))/100000;
            Double lonDb = (Double.valueOf(lon+220000))/100000;

            Nota nota = new Nota("titulo"+i, "Descripcion"+i, latDb, lonDb);
            fb.child("prueba").child("Notas").child("nota"+i).setValue(nota);

        }

    }
    //FIN METODOS VARIOS............................................................................

}
