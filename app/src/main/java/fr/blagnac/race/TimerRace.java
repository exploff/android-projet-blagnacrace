package fr.blagnac.race;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

public class TimerRace extends CountDownTimer{
	private final MapsActivity mainActivity;
	private HashMap<String,Marker> marqueursAdversaires;

	public TimerRace(MapsActivity mainAct, long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
		this.mainActivity=mainAct;
		this.marqueursAdversaires = new HashMap<String,Marker>();
	}

	public HashMap<String,Marker> getMarqueursAdversaires() {
		return this.marqueursAdversaires;
	}

	@Override
	public void onTick(long millisUntilFinished) {
		RequeteHTTP requeteServeur = new RequeteHTTP(mainActivity.getAdresse_serveur());
		try {

			this.mainActivity.displayMessagesFromServer(requeteServeur);
			if(this.mainActivity.getCourseFinie()) {
				this.mainActivity.raceFinished(requeteServeur);
			}

			String response = requeteServeur.doGET("cmd=getParticipants");

			if(!response.equals("None")) {
				List<String> participants = Arrays.asList(response.split(","));

				Set<String> adversairesLocal = marqueursAdversaires.keySet();
				for(String adv : adversairesLocal) {
					if (!participants.contains(adv)) {
						this.marqueursAdversaires.get(adv).remove();
						this.marqueursAdversaires.remove(adv);
					}
				}

				String monNom = this.mainActivity.getMonNom();
				for(String nom : participants) {
					if(!nom.equals(monNom)) {
						String reponsePosition = requeteServeur.doGET("cmd=getPosition&name=" + nom);
						if (!reponsePosition.equals("None")) {
							String [] coordonnees = reponsePosition.split(",");
							LatLng position = new LatLng(Double.valueOf(coordonnees[0]), Double.valueOf(coordonnees[1]));
							Marker marker;
							if (this.marqueursAdversaires.containsKey(nom)) {
								marker = this.marqueursAdversaires.get(nom);
							} else {
								marker = this.mainActivity.getmMap().addMarker(new MarkerOptions()
										.title(nom).position(position)
										.icon(BitmapDescriptorFactory
												.fromResource(R.drawable.red_droid)));
								this.marqueursAdversaires.put(nom, marker);
							}
							marker.setPosition(position);
						}
					}
				}
			}

		}
		catch  (IOException e) {
			this.mainActivity.showAlert("Problème", "Pas d'accès au réseau");
			Log.e("Error", e.getMessage());
		}
	}

	@Override
	public void onFinish() {
	}

}
