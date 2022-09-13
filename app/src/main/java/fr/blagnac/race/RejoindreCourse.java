package fr.blagnac.race;

import java.io.IOException;
import java.net.MalformedURLException;

import com.google.android.gms.maps.model.LatLng;

import android.app.Dialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import fr.blagnac.race.constants.ConstantsRace;


public class RejoindreCourse extends Dialog{

	public RejoindreCourse(MapsActivity mainAct) {
		super(mainAct);
		setContentView(R.layout.connexion_serveur);
		setTitle("Connexion à un serveur");
		Button ok = (Button) findViewById(R.id.button_ok);
		ok.setOnClickListener(v -> {
			String adresse_serveur = ((EditText) findViewById(R.id.ed_serveur)).getText().toString();
			String nom = ((EditText) findViewById(R.id.ed_nom)).getText().toString();
			mainAct.setAdresse_serveur(adresse_serveur);
			mainAct.setMonNom(nom);
			mainAct.getMyMarker().setTitle(nom);
			try {
				RequeteHTTP requeteServeur = new RequeteHTTP(adresse_serveur);
				String isEnCours = requeteServeur.doGET("cmd=isRaceOnProgress");
				if ( isEnCours.equals("None") ) {
					mainAct.desabonnementGPS();
					mainAct.showAlert("Problème", "Le serveur n'est pas actif");
					dismiss();
				}
				else if ( isEnCours.equals("False") ) {
					Toast.makeText(mainAct, "Premier participant connecté \n  Initialisation de la course", Toast.LENGTH_LONG).show();
					requeteServeur.doGET("cmd=reinitRace");
				}
				String resultRequest = requeteServeur.doGET("cmd=addParticipant&name=" + nom);
				if (resultRequest.equals("OK")) {
					Toast.makeText(mainAct, "Ajout du participant à la course", Toast.LENGTH_LONG).show();

					String cibleResult = requeteServeur.doGET("cmd=getGoal&name=" + nom);

					if(!cibleResult.equals("None")) {
						Toast.makeText(mainAct, "Récupération de la cible et affichage", Toast.LENGTH_LONG).show();
						String[] cible = cibleResult.split(",");
						try {
							mainAct.getCibleMarker().setVisible(true);

							mainAct.getCibleTextView().setText(mainAct.getString(R.string.cible_title) + " : " + cible[0]);

							mainAct.getCibleLocation().setLatitude(Double.valueOf(cible[1]));
							mainAct.getCibleLocation().setLongitude(Double.valueOf(cible[2]));
							mainAct.getCibleMarker().setSnippet(cible[0]);
							mainAct.getCibleMarker().setPosition(
									new LatLng(
											Double.valueOf(cible[1]),
											Double.valueOf(cible[2])
									)
							);
							mainAct.getCibleMarker().setVisible(true);
							mainAct.setStateApp(ConstantsRace.IN_PROGRESS);


						} catch (NumberFormatException | IndexOutOfBoundsException e){
							mainAct.showAlert("Problème", "Récupération titre,longitude et de la latitude de la cible");
							mainAct.desabonnementGPS();
							dismiss();
						}
					} else {
						mainAct.showAlert("Problème", "Récupération de la cible indisponible");
						mainAct.desabonnementGPS();
						dismiss();
					}
				} else if (resultRequest.equals("None")) {
					mainAct.showAlert("Problème", "Ajout impossible (pas de course en cours, participant déjà présent) \nVeuillez réinitialiser ou terminer la course");
					mainAct.setStateApp(ConstantsRace.STOP);
					mainAct.desabonnementGPS();
					dismiss();
				}

				mainAct.abonnementGPS();
				mainAct.getTimerRace().start();
				dismiss();
			} catch (MalformedURLException e) {
				mainAct.showAlert("Problème", "URL invalide");
				dismiss();
			} catch (IOException e) {
				mainAct.showAlert("Problème", "Pas d'accès au réseau");
				dismiss();
			}
		});
	}
}
