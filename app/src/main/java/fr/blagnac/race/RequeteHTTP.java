package fr.blagnac.race;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

import android.util.Log;
import android.widget.Toast;

public class RequeteHTTP {
	private String adresse_serveur;

	public RequeteHTTP(String adresse_serveur){
		this.adresse_serveur = adresse_serveur;
	}

	public String doGET(String parametres) throws MalformedURLException, IOException {
		//création de l'objet URL pour la requête
		//Encodage des caractères spéciaux
		URL url = new URL("http://" + adresse_serveur + "/?" + parametres );
		//ouverture de la connection et paramétrage de la requête
		HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
		httpUrlConnection.setReadTimeout(10000 /* milliseconds */);
		httpUrlConnection.setConnectTimeout(15000 /* milliseconds */);
		httpUrlConnection.setRequestMethod("GET");
		httpUrlConnection.setDoInput(true);
		//envoi de la requête au serveur
		httpUrlConnection.connect();
		if(httpUrlConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
		{
			//récupération de la réponse
			String response = RequeteHTTP.readResponse(httpUrlConnection.getInputStream());
			httpUrlConnection.disconnect();
			return response;
		} else {
			httpUrlConnection.disconnect();
			throw new MalformedURLException();
		}
	}

	public static String doGETWithURL(String urlToRequest) throws MalformedURLException, IOException {
		//création de l'objet URL pour la requête
		//Encodage des caractères spéciaux
		URL url = new URL(urlToRequest);
		//ouverture de la connection et paramétrage de la requête
		HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
		httpUrlConnection.setRequestMethod("GET");
		httpUrlConnection.setRequestProperty("Content-length", "0");
		httpUrlConnection.setUseCaches(false);
		httpUrlConnection.setAllowUserInteraction(false);
		httpUrlConnection.setConnectTimeout(10000);
		httpUrlConnection.setReadTimeout(15000);
		httpUrlConnection.connect();

		String response = RequeteHTTP.readResponse(httpUrlConnection.getInputStream());
		httpUrlConnection.disconnect();

		return response;
	}

	public static String readResponse(InputStream stream) throws IOException, UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = reader.readLine();
		builder.append(line);
		return builder.toString();
	}

}

