package app.notemap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A placeholder fragment containing a simple view.
 */
public class F_Map extends Fragment {

    Firebase ref;

    private MapView map;
    private IMapController iMapController;
    private MyLocationNewOverlay mlno;
    //private MinimapOverlay minimapOverlay;
    private ScaleBarOverlay scaleBarOverlay;
    private CompassOverlay compassOverlay;
    private RadiusMarkerClusterer agrupacionNotaMarkers;
    private int radioAgrupacion = 100;


    boolean hayControlZoom = true;
    boolean hayControlMultiTouch = true;
    boolean hayCentradoInicial = true;
    boolean hayPrecisionOverlay = true;
    private int zoomInicial;


    public F_Map() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootMapView = inflater.inflate(R.layout.lay_f_map, container, false);

        //Iniciales
        NoteMap app = (NoteMap) getActivity().getApplication();
        ref = app.getRef();
        map = (MapView) rootMapView.findViewById(R.id.mapView);
        zoomInicial = 15;

        //Configuraciones iniciales
        setMap();
        setZoom(zoomInicial);
        setOverlays(getContext());
        drawMarkers(getContext());

        return rootMapView;
    }


    public void setMap(){
        try {
            //Determinamos los tiles del mapa para pintarlo

            map.setTileSource(TileSourceFactory.MAPQUESTOSM);
            map.setTilesScaledToDpi(true);

            //Determinamos otras opciones del mapa
            map.setBuiltInZoomControls(hayControlZoom);
            map.setMultiTouchControls(hayControlMultiTouch);

        }catch(Exception ex){
            ex.printStackTrace();
            msgToast(2, "Dibujando el mapa inicial");
        }
    }

    public void setZoom(int zoom){
        try {
            iMapController = map.getController();
            iMapController.setZoom(zoom);

        }catch (Exception ex){
            ex.printStackTrace();
            iMapController.setZoom(zoomInicial);
            msgToast (2, "Ajustando el Zoom al inicial");
        }
    }

    public void setOverlays(Context context){
        final DisplayMetrics metricaDelMapa = getResources().getDisplayMetrics();
        mlno = new MyLocationNewOverlay(    context
                ,new GpsMyLocationProvider(context)
                ,map
        );

        mlno.enableMyLocation();
        mlno.setDrawAccuracyEnabled(hayPrecisionOverlay);
        mlno.runOnFirstFix(new Runnable() {
            @Override
            public void run() { iMapController.animateTo(mlno.getMyLocation()); }
        });

        //Setear la escala del mapa
        scaleBarOverlay = new ScaleBarOverlay(map);
        scaleBarOverlay.setCentred(hayCentradoInicial);
        scaleBarOverlay.setScaleBarOffset(metricaDelMapa.widthPixels / 2, 10);

        //Setear la Brujula de Overlays
        compassOverlay = new CompassOverlay( context
                ,new InternalCompassOrientationProvider(context)
                , map
        );
        compassOverlay.enableCompass();

        //Añadir los setters al overlay
        map.getOverlays().add(mlno);
        map.getOverlays().add(this.scaleBarOverlay);
        map.getOverlays().add(this.compassOverlay);
    }


    public void drawMarkers (Context context){
        setAgrupacionMarkers(context, radioAgrupacion);

        final Firebase notas = ref.child("prueba").child("Notas");
        notas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot notasFireBase: dataSnapshot.getChildren()){

                    Nota nota = notasFireBase.getValue(Nota.class);
                    Marker notaMarker = new Marker(map);
                    GeoPoint gp = new GeoPoint(nota.getLatitud() ,nota.getLongitud());

                    notaMarker.setPosition(gp);
                    notaMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    notaMarker.setIcon(getResources().getDrawable(R.drawable.marker_default));
                    notaMarker.setTitle(nota.getTitulo());
                    notaMarker.setAlpha(0.7f);

                    agrupacionNotaMarkers.add(notaMarker);
                    msgToast(1, nota.getTitulo()+" añadida");
                }
                agrupacionNotaMarkers.invalidate();
                map.invalidate();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {  }
        });
    }

    public void setAgrupacionMarkers(Context context, int radio){
        agrupacionNotaMarkers = new RadiusMarkerClusterer(context);
        map.getOverlays().add(agrupacionNotaMarkers);

        Drawable clusterIconoDrawable = getResources().getDrawable(android.R.drawable.ic_input_get);
        int markerWidth = clusterIconoDrawable.getIntrinsicWidth();
        int markerHeight = clusterIconoDrawable.getIntrinsicHeight();
        clusterIconoDrawable.setBounds(0, markerHeight, markerWidth, 0);
        Bitmap clusterIconBm = ((BitmapDrawable) clusterIconoDrawable).getBitmap();

        agrupacionNotaMarkers.setIcon(clusterIconBm);
        agrupacionNotaMarkers.setRadius(radio);
    }


    public void msgToast(int numTag, String msg) {
        switch (numTag) {
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


}
