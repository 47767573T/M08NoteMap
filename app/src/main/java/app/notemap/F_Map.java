package app.notemap;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A placeholder fragment containing a simple view.
 */
public class F_Map extends Fragment {

    MapController mapController;
    boolean hayControlZoom = true;
    boolean hayControlMultiTouch = true;
    boolean hayPrecisionOverlay = true;
    public ArrayList<Nota> notaLista;
    public LinkedList<OverlayItem>notaMarkersList;

    public MapView mapView;

    public F_Map() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootMapView = inflater.inflate(R.layout.lay_f_map, container, false);

        NoteMap app = (NoteMap) getActivity().getApplication();
        Firebase ref = app.getRef();

        ref.child("prueba").child("Notas").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                notasGetter(snapshot);
            }

            @Override
            public void onCancelled(FirebaseError error) {
                msgToast(2, "DataListener de Firebase Cancelado");
            }
        });


        MapView mapView = (MapView) getActivity().findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(hayControlZoom);
        mapView.setMultiTouchControls(hayControlMultiTouch);
        mapController = (MapController) mapView.getController();


        Drawable marker = getResources().getDrawable(android.R.drawable.ic_input_get);
        int markerWidth = marker.getIntrinsicWidth();
        int markerHeight = marker.getIntrinsicHeight();
        marker.setBounds(0, markerHeight, markerWidth, 0);

        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getContext());

        MyLocationNewOverlay mlno = new MyLocationNewOverlay(getContext(), mapView);
        mlno.setDrawAccuracyEnabled(hayPrecisionOverlay);

        mapView.getOverlays().add(mlno);

        ItemizedIconOverlay markersOverlay = new ItemizedIconOverlay<OverlayItem>
                (notaMarkersList, marker, null, resourceProxy);
        mapView.getOverlays().add(markersOverlay);


        //Añadir markers


        return rootMapView;
    }

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

    public void notasGetter (DataSnapshot notasSnapshot){

        if (notasSnapshot.getChildrenCount() != notaLista.size()){
            notaLista.clear();
            for (DataSnapshot notasFireBase : notasSnapshot.getChildren()) {
                Nota nota = notasSnapshot.getValue(Nota.class);
                notaLista.add(nota);
                msgToast(3, "Añadiendo marcadores...");
                Log.e(nota.getTitulo(), nota.getDesc());
            }
            markersGetter(true);
        }

    }

    public void markersGetter (boolean markersNuevo){


        for (int i = 0 ; i < notaLista.size() ; i++) {
            Double lat = (notaLista.get(i).getLatitud())*1E6;
            Double lon = (notaLista.get(i).getLongitud())*1E6;

            String titulo = (notaLista.get(i).getTitulo());
            String desc = (notaLista.get(i).getDesc());
            GeoPoint gp = new GeoPoint(lat, lon);

            OverlayItem oli = new OverlayItem(String.valueOf(i), titulo, desc, gp);
            notaMarkersList.add(oli);
        }


    }

}
