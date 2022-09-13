package fr.blagnac.race.javabean;

import androidx.annotation.Nullable;

public class Station {
    public Station(String id, String nom, String lines) {
        this.id = id;
        this.nom = nom;
        this.lines = lines;
    }

    private String id;
    private String nom;
    private String lines;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getLines() {
        return lines;
    }

    public void setLines(String lines) {
        this.lines = lines;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Station compareToStation = (Station)obj;
        boolean test = compareToStation.getId().equals(this.id);
        return compareToStation.getId().equals(this.id);

    }

}
