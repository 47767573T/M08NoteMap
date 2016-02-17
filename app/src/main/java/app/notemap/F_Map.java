package app.notemap;

import android.content.Context;
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A placeholder fragment containing a simple view.
 */
public class F_Map extends Fragment {

    Firebase ref;

    MapController mapController;
    MyLocationNewOverlay mlno;
    boolean hayControlZoom = true;
    boolean hayControlMultiTouch = true;
    boolean hayPrecisionOverlay = true;
    public ArrayList<Nota> notaLista;
    public LinkedList<OverlayItem> notaMarkersList;

    public MapView map;

    public F_Map() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootMapView = inflater.inflate(R.layout.lay_f_map, container, false);

        NoteMap app = (NoteMap) getActivity().getApplication();
        ref = app.getRef();
        map = (MapView) rootMapView.findViewById(R.id.mapView);
        notaMarkersList = new LinkedList<>();

        ref.child("prueba").child("Notas").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot){
                notasGetter(snapshot);
            }
            @Override
            public void onCancelled(FirebaseError error) {msgToast(2, "DataListener de Firebase Cancelado");}
        });


        map = (MapView) rootMapView.findViewById(R.id.mapView);
        map.setBuiltInZoomControls(hayControlZoom);
        map.setMultiTouchControls(hayControlMultiTouch);
        mapController = (MapController) map.getController();

        Drawable marker = getResources().getDrawable(android.R.drawable.ic_input_get);
        int markerWidth = marker.getIntrinsicWidth();
        int markerHeight = marker.getIntrinsicHeight();
        marker.setBounds(0, markerHeight, markerWidth, 0);

        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getContext());

        mlno = new MyLocationNewOverlay(getContext(),new GpsMyLocationProvider(getContext()), map);
        mlno.enableMyLocation();
        //mlno.setDrawAccuracyEnabled(hayPrecisionOverlay);

        map.getOverlays().add(mlno);

        ItemizedIconOverlay<OverlayItem> markersOverlay = new ItemizedIconOverlay<>
                (notaMarkersList, marker, null, resourceProxy);
        map.getOverlays().add(markersOverlay);

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
        msgToast(1, String.valueOf(notaLista.size()));
        if (notasSnapshot.getChildrenCount() != notaLista.size()){
            notaLista.clear();
            for (DataSnapshot notasFireBase : notasSnapshot.getChildren()) {
                Nota nota = notasFireBase.getValue(Nota.class);
                notaLista.add(nota);
                msgToast(3, "Añadiendo marcadores...");
                Log.e(nota.getTitulo(), nota.getDesc());
            }
            markersFiller(true);
        }
    }

    public void markersFiller (boolean hayNuevosMarkers){

        if (hayNuevosMarkers) {
            for (int i = 0; i < notaLista.size(); i++) {
                Double lat = (notaLista.get(i).getLatitud()) * 1E6;
                Double lon = (notaLista.get(i).getLongitud()) * 1E6;

                String titulo = (notaLista.get(i).getTitulo());
                String desc = (notaLista.get(i).getDesc());
                GeoPoint gp = new GeoPoint(lat, lon);

                OverlayItem overlayItem = new OverlayItem(String.valueOf(i), titulo, desc, gp);
                notaMarkersList.add(overlayItem);
            }
        }
    }

}
