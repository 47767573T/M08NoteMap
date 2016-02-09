package app.notemap;

/**
 * Created by Moises on 09/02/2016.
 */
public class Nota {
    String titulo;
    String desc;
    Double longitud;
    Double latitud;

    public Nota() {
    }

    public Nota(String titulo, String desc, Double longitud, Double latitud) {
        this.titulo = titulo;
        this.desc = desc;
        this.longitud = longitud;
        this.latitud = latitud;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }
}