package fr.blagnac.race;

import android.app.Dialog;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EnvoyerMessage extends Dialog {
    private final MapsActivity mainActivity;
    private String whoToSend = "";
    private String participant_to_send = "";
    private Spinner spinner_participants;

    public EnvoyerMessage(MapsActivity mainAct) {
        super(mainAct);
        this.mainActivity=mainAct;
        setContentView(R.layout.send_message);
        setTitle("Envoyer un message");
        String adresse_serveur = mainActivity.getAdresse_serveur();
        RequeteHTTP requeteServeur = new RequeteHTTP(adresse_serveur);

        try {
            String response = requeteServeur.doGET("cmd=getParticipants");
            String[] participants = response.split(",");
            List<String> liste_participants = new ArrayList<>();

            for(String participant: participants) {
                if (!participant.equals(mainActivity.getMonNom())) {
                    liste_participants.add(participant);
                }
            }

            this.spinner_participants = findViewById(R.id.spinner_participants);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_item , liste_participants);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.spinner_participants.setAdapter(adapter);
            this.spinner_participants.setEnabled(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        RadioButton rbAll = findViewById(R.id.radio_all);
        rbAll.setOnClickListener(v -> {
            this.spinner_participants.setEnabled(false);

        });

        RadioButton rbOneParticipant = findViewById(R.id.radio_one_participant);
        rbOneParticipant.setOnClickListener(v -> {
            this.spinner_participants.setEnabled(true);

        });

        Button ok = findViewById(R.id.button_ok);
        ok.setOnClickListener(v -> {

            RadioGroup radioGroup = findViewById(R.id.rb_choice);
            int selectedId = radioGroup.getCheckedRadioButtonId();
            RadioButton radioButton = findViewById(selectedId);
            whoToSend = radioButton.getText().toString();

            String message = ((EditText) findViewById(R.id.ed_message_to_send)).getText().toString();

            this.participant_to_send = this.spinner_participants.getSelectedItem().toString();

            if(whoToSend.equals("A Tous"))
            {
                try {
                    String response = requeteServeur.doGET("cmd=getParticipants");
                    String[] participants = response.split(",");

                    for (String nom : participants) {
                        if(!nom.equals(mainActivity.getMonNom())) {
                            requeteServeur.doGET("cmd=sendMessage&name=" + nom + "&msg=" + message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if(this.participant_to_send != null)
                {
                    try
                    {
                        requeteServeur.doGET("cmd=sendMessage&name=" + participant_to_send + "&msg=" + message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            dismiss();
        });
    }

}
