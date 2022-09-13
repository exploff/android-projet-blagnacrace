package fr.blagnac.race;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.blagnac.race.constants.ConstantsRace;
import fr.blagnac.race.databinding.ActivityMapsBinding;
import fr.blagnac.race.javabean.Station;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {


    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private LocationManager locationManager;
    private TextView cibleTextView;
    private Location cibleLocation;
    private Marker myMarker;
    private Marker cibleMarker;

    private String adresse_serveur;
    private String monNom;
    private Boolean courseFinie=false;
    private HashMap<String,Marker> marqueursTramway;

    private List<Station> stations;


    private String stateApp = ConstantsRace.STOP;

    private TimerRace timerRace;

    public TimerRace getTimerRace() {
        return this.timerRace;
    }

    public GoogleMap getmMap() {
        return mMap;
    }

    public void setmMap(GoogleMap mMap) {
        this.mMap = mMap;
    }

    public ActivityMapsBinding getBinding() {
        return binding;
    }

    public void setBinding(ActivityMapsBinding binding) {
        this.binding = binding;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public void setLocationManager(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    public TextView getCibleTextView() {
        return cibleTextView;
    }

    public void setCibleTextView(TextView cibleTextView) {
        this.cibleTextView = cibleTextView;
    }

    public Location getCibleLocation() {
        return cibleLocation;
    }

    public void setCibleLocation(Location cibleLocation) {
        this.cibleLocation = cibleLocation;
    }

    public Marker getMyMarker() {
        return myMarker;
    }

    public void setMyMarker(Marker myMarker) {
        this.myMarker = myMarker;
    }

    public Marker getCibleMarker() {
        return cibleMarker;
    }

    public void setCibleMarker(Marker cibleMarker) {
        this.cibleMarker = cibleMarker;
    }

    public String getAdresse_serveur() {
        return adresse_serveur;
    }

    public void setAdresse_serveur(String adresse_serveur) {
        this.adresse_serveur = adresse_serveur;
    }

    public String getMonNom() {
        return monNom;
    }

    public void setMonNom(String monNom) {
        this.monNom = monNom;
    }

    public Boolean getCourseFinie() {
        return courseFinie;
    }

    public void setCourseFinie(Boolean courseFinie) {
        this.courseFinie = courseFinie;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED )
        {
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true); // affiche les boutons de zoom
        mMap.getUiSettings().setZoomGesturesEnabled(true); // autorise le zoom tactile
        mMap.getUiSettings().setCompassEnabled(false); // n'affiche pas le compas
        mMap.getUiSettings().setMyLocationButtonEnabled(true); // affiche le bouton de localisation

        //Mise en place d'une cible
        String cibleDesc = getString(R.string.cible_desc_default); // description de la cible
        double cibleLat = 43.6489983; // coordonnées (latitude, longitude)
        double cibleLon = 1.3749359; // de l’IUT de Blagnac
        this.cibleLocation = new Location("Cible") ;
        this.cibleLocation.setLatitude(cibleLat);
        this.cibleLocation.setLongitude(cibleLon);
        this.cibleTextView = (TextView)findViewById(R.id.tv);
        String cibleTexte = " Cible : " + cibleDesc;
        this.cibleTextView.setText(cibleTexte);

        TextView tv = (TextView) findViewById(R.id.tv);
        tv.setText(getString(R.string.wait_race));


        this.myMarker = mMap.addMarker(new MarkerOptions()
                .title(getString(R.string.my_location_title)).position(new LatLng(0, 0))
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.green_droid))); // Icone Droid vert


        //Mise en place du marqueur de la cible
        this.cibleMarker = mMap.addMarker( new MarkerOptions()
                .title(getString(R.string.cible_title))
                .position(new LatLng(cibleLat,cibleLon))
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) ); // Marqueur bleu

        this.cibleMarker.setVisible(false);
        this.initTramwayMarker();
        this.initStations();

    }


    /**
     * Méthode appelée quand une source de localisation est activée
     * @param provider
     */
    @Override
    public void onProviderEnabled(@NonNull String provider) {
        if ("gps".equals(provider)) { // Si le GPS est activé on s'abonne
            abonnementGPS();
        }
    }

    /**
     * Méthode appelée quand une source de localisation est désactivée
     * @param provider
     */
    @Override
    public void onProviderDisabled(@NonNull String provider) {
        if ("gps".equals(provider)) { // Si le GPS est désactivé on se désabonne
            desabonnementGPS();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu_items, menu);

        //Desactive par défaut le menu de rejoindre une course
        if (this.stateApp.equals(ConstantsRace.IN_PROGRESS)) {
            MenuItem joinRace = menu.findItem(R.id.menu_join_race);
            joinRace.setEnabled(false);
            MenuItem sendMessage = menu.findItem(R.id.lisview_menu_send_message_id);
            sendMessage.setEnabled(true);
        } else if(this.stateApp.equals(ConstantsRace.STOP)) {
            MenuItem item = menu.findItem(R.id.menu_join_race);
            item.setEnabled(true);
            item = menu.findItem(R.id.menu_leave_race);
            item.setEnabled(false);
            MenuItem sendMessage = menu.findItem(R.id.lisview_menu_send_message_id);
            sendMessage.setEnabled(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_join_race:
                joinRace();
                break;
            case R.id.menu_leave_race:
                leaveRace();
                break;
            case R.id.menu_leave_app:
                leaveApp();
                break;
            case R.id.lisview_menu_send_message_id:
                new EnvoyerMessage(this).show();
                break;
        }
        return true;
    }

    private void leaveApp() {
        this.desabonnementGPS();
        RequeteHTTP requeteServeur = new RequeteHTTP(adresse_serveur);
        try {
            requeteServeur.doGET("cmd=removeParticipant&name=" + monNom);

        } catch (IOException e) {
            Log.e("Problème", "Impossible de supprimer le participant " + monNom + " de la course");
        }
        this.finishAndRemoveTask();
    }

    private void leaveRace() {

        RequeteHTTP requeteServeur = new RequeteHTTP(adresse_serveur);
        try {
            requeteServeur.doGET("cmd=removeParticipant&name=" + monNom);

            for(Marker markerAdv : this.timerRace.getMarqueursAdversaires().values()) {
                markerAdv.remove();
            }
            this.timerRace.getMarqueursAdversaires().clear();

        } catch (IOException e) {
            Log.e("Problème", "Impossible de supprimer le participant " + monNom + " de la course");
        }

        TextView textView = (TextView) findViewById(R.id.tv);
        textView.setText("");
        this.cibleMarker.setVisible(false);
        this.myMarker.setSnippet("");
        this.myMarker.showInfoWindow();
        this.timerRace.cancel();
        this.setStateApp(ConstantsRace.STOP);

    }

    private void joinRace() {

        new RejoindreCourse(this).show();
        this.timerRace = new TimerRace(this, 86400000,5000);

    }

    /**
     * Méthode appelée quand les coordonnées GPS du smartphone changent
     * @param myLocation
     */
    @Override
    public void onLocationChanged(@NonNull Location myLocation) {
        // Récupération des cordonnées (latitude, longitude) et création d’un objet
        // myPos de la classe LatLng représentant cette position
        final LatLng position = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        // Centrage de la carte sur la position GPS obtenue
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));

        myMarker.setPosition(position);

        if (this.stateApp.equals(ConstantsRace.IN_PROGRESS)) {

            double distance = myLocation.distanceTo(this.cibleLocation);
            myMarker.setSnippet(getString(R.string.distance_my_location, String.valueOf(Math.round(distance))));
            myMarker.showInfoWindow();

            try {

                RequeteHTTP requeteServeur = new RequeteHTTP(adresse_serveur);
                if (monNom != null) {

                    displayMessagesFromServer(requeteServeur);

                    if (this.courseFinie) {
                        raceFinished(requeteServeur);

                    }

                    requeteServeur.doGET("cmd=setPosition&name=" + monNom
                            + "&lat=" + myLocation.getLatitude()
                            + "&lon=" + myLocation.getLongitude());

                    String messageAdversaire = "None";
                    if (distance < 30.0) {
                        String numCibleAtteinte = requeteServeur.doGET("cmd=setGoalReached&name=" + monNom);

                        if (numCibleAtteinte.equals("3")) {
                            showAlert("Gagnez !!!", "Vous avez atteint la dernière cible");
                            this.courseFinie = true;
                            this.timerRace.cancel();
                            messageAdversaire = "Perdu";
                            requeteServeur.doGET("cmd=removeParticipant&name=" + monNom);
                            this.desabonnementGPS();

                        } else {
                            //recuperation information de la prochaine cible
                            showAlert("Bravo !!!", "Vous avez atteint la cible " + numCibleAtteinte);
                            String cibleResult = requeteServeur.doGET("cmd=getGoal&name=" + monNom);

                            if (!cibleResult.equals("None")) {
                                String[] cible = cibleResult.split(",");
                                if (cible.length == 3) {
                                    try {
                                        this.cibleTextView.setText(getString(R.string.cible_title) + " : " + cible[0]);

                                        this.cibleLocation.setLatitude(Double.valueOf(cible[1]));
                                        this.cibleLocation.setLongitude(Double.valueOf(cible[2]));
                                        this.cibleMarker.setSnippet(cible[0]);
                                        this.cibleMarker.setPosition(
                                                new LatLng(
                                                        Double.valueOf(cible[1]),
                                                        Double.valueOf(cible[2])
                                                )
                                        );

                                        double newDistance = myLocation.distanceTo(this.cibleLocation);
                                        myMarker.setSnippet(getString(R.string.distance_my_location, String.valueOf(Math.round(newDistance))));
                                        myMarker.showInfoWindow();
                                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                                        showAlert("Problème", "Récupération titre,longitude et de la latitude de la cible");
                                        desabonnementGPS();
                                    }
                                }
                                messageAdversaire = monNom + " a atteint la cible " + numCibleAtteinte;

                            } else {
                                showAlert("Problème", "Récupération de la cible indisponible");
                                desabonnementGPS();
                            }
                        }
                        String[] participants = requeteServeur.doGET("cmd=getParticipants").split(",");
                        for (String nom : participants) {
                            if (!nom.equals(monNom)) {
                                requeteServeur.doGET("cmd=sendMessage&name=" + nom + "&msg=" + messageAdversaire);
                            }
                        }
                    }
                }

            } catch (IOException e) {
                showAlert("Problème", "Pas d'accès au réseau");
                Log.e("Error", e.getMessage());
            }
        }
    }

    private void initTramwayMarker() {

        String adresse_api_tramway = "https://data.toulouse-metropole.fr/explore/dataset/stations-de-tramway/download/?format=json&timezone=Europe/Berlin";

        try {

            String response = RequeteHTTP.doGETWithURL(adresse_api_tramway);

            JSONArray jsonResponse = new JSONArray(response);

            this.marqueursTramway = new HashMap<>();
            for (int i = 0; i < jsonResponse.length(); i++) {
                String keyTramway = jsonResponse.getJSONObject(i).getString("recordid");
                JSONObject fieldsObjectTramway = jsonResponse.getJSONObject(i).getJSONObject("fields");
                LatLng position = new LatLng(fieldsObjectTramway.getJSONArray("geo_point_2d").getDouble(0),
                                                fieldsObjectTramway.getJSONArray("geo_point_2d").getDouble(1));
                Marker marker = this.mMap.addMarker(new MarkerOptions()
                                        .title(fieldsObjectTramway.getString("etiquette")).position(position)
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.tramway)));

                marqueursTramway.put(keyTramway, marker);
            }
            // adding on click listener to marker of google maps
            mMap.setOnMarkerClickListener(marker -> {
                try {
                    return displayTimeTableOfTramway(marker);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            });


        } catch (MalformedURLException e) {
            Log.e("TRAMWAY", "Erreur pour créer l'URL des tramways " + e.getMessage());
        } catch (IOException e) {
            Log.e("TRAMWAY", "Erreur pour créer la connexion à l'api tramway " + e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("TRAMWAY", "Erreur pour parser le json reçu " + e.getMessage());
        }

    }

    private void initStations() {
        this.stations = new ArrayList<>();
        String lineT1 = "line:68";
        String lineT2 = "line:110";

        try {

            String response = RequeteHTTP.doGETWithURL("http://api.tisseo.fr/v2/stop_points.json?lineId=" + lineT1 + "&key=a49cf7664ad133a0fb1e4a315902758c4");

            JSONObject jsonArretLigneT1 = new JSONObject(response);
            JSONObject physicalStopsT1 = jsonArretLigneT1.getJSONObject("physicalStops");
            JSONArray physicalStopT1 = physicalStopsT1.getJSONArray("physicalStop");
            for (int i = 0; i < physicalStopT1.length(); i++) {
                String id = physicalStopT1.getJSONObject(i).getString("id");
                String name = physicalStopT1.getJSONObject(i).getString("name");
                this.stations.add(new Station(id, name, lineT1));
            }

            response = RequeteHTTP.doGETWithURL("http://api.tisseo.fr/v2/stop_points.json?lineId=" + lineT2 + "&key=a49cf7664ad133a0fb1e4a315902758c4");

            JSONObject jsonArretLigneT2 = new JSONObject(response);
            JSONObject physicalStopsT2 = jsonArretLigneT2.getJSONObject("physicalStops");
            JSONArray physicalStopT2 = physicalStopsT2.getJSONArray("physicalStop");
            for (int i = 0; i < physicalStopT2.length(); i++) {
                String id = physicalStopT2.getJSONObject(i).getString("id");
                String name = physicalStopT2.getJSONObject(i).getString("name");
                Station st = new Station(id, name, lineT2);
                if(!this.stations.contains(st)) {
                    this.stations.add(st);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private boolean displayTimeTableOfTramway(Marker marker) throws IOException, JSONException {

        if(this.marqueursTramway.containsValue(marker)) {
            List<Station> stopStation = new ArrayList<>();
            for(Station st : this.stations) {
                String nomMarker = marker.getTitle();
                if (st.getNom().compareToIgnoreCase(nomMarker) == 0) {
                    stopStation.add(st);
                }
            }

            String content = "";
            for(Station st : stopStation) {

                String response = RequeteHTTP.doGETWithURL("http://api.tisseo.fr/v2/stops_schedules.json?stopPointId=" + st.getId()
                        + "&lineId=" + st.getLines()
                        + "&key=a49cf7664ad133a0fb1e4a315902758c4");

                JSONObject jsonArretLigne = new JSONObject(response);
                JSONObject departures = jsonArretLigne.getJSONObject("departures");
                JSONArray departure = departures.getJSONArray("departure");
                content += "Destination " + departure.getJSONObject(0).getJSONArray("destination").getJSONObject(0).getString("name") + " \n";
                for (int i = 0; i < departure.length() / 2; i++) {
                    content += departure.getJSONObject(i).getString("dateTime") + "\n";
                }
                content += "\n";
            }
            if (content.equals("")) {
                content = "Problème de coïncidence entre API Tramway et API Tisseo";
            }
            showAlert("Horaires", content);
        }

        return false;
    }

    public void setStateApp(String stateApp) {
        this.stateApp = stateApp;
        this.invalidateOptionsMenu();
    }

    public void displayMessagesFromServer(RequeteHTTP requeteServeur) throws IOException {
        String message = "";
        do {
            message = requeteServeur.doGET("cmd=getMessage&name=" + monNom);
            if (message.equals("Perdu")) {
                this.courseFinie = true;
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            } else if(!message.equals("None")) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

        } while(!message.equals("None"));
    }
    public void raceFinished(RequeteHTTP requeteServeur) throws IOException {
            String gagnant = requeteServeur.doGET("cmd=getWinner");
            showAlert("Perdu !", gagnant + " a atteint la dernière cible !");
            this.timerRace.cancel();
            this.desabonnementGPS();
            requeteServeur.doGET("cmd=removeParticipant&name=" + monNom);

            //On réactive le menu rejoindre une course et on désactive le menu quitter une course
            this.setStateApp(ConstantsRace.STOP);
    }

    /**
     * méthode appelée quand le status d’une source de localisation change
     * @param provider
     * @param status
     * @param extras
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public void abonnementGPS() throws SecurityException {
        // demande de réception de localisations du GPS au minimum toutes les 5000 ms et
        // lorsque la distance entre la dernière localisation reçue est supérieure à 10 m
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
    }
    public void desabonnementGPS() throws SecurityException {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // On appelle la méthode pour se désabonner
        desabonnementGPS();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Obtention de la référence du service
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        // Si le GPS est disponible, on s'y abonne
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            abonnementGPS();
        }
    }

    public void showAlert(String titre, String message) {
        AlertDialog.Builder ADBuilder = new AlertDialog.Builder(this);
        ADBuilder.setTitle(titre);
        ADBuilder.setMessage(message);
        ADBuilder.setCancelable(true);
        ADBuilder.setNeutralButton(android.R.string.ok,
                (dialog, id) -> dialog.cancel());
        AlertDialog AD = ADBuilder.create();
        AD.show();
    }
}
