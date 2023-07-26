package ar.edu.utn.frba.dds.serviciosPublicos;

import ar.edu.utn.frba.dds.comunidades.Usuario;
import ar.edu.utn.frba.dds.incidentes.EstadoIncidente;
import ar.edu.utn.frba.dds.incidentes.Incidente;
import ar.edu.utn.frba.dds.incidentes.Prestacion;
import ar.edu.utn.frba.dds.localizacion.Localizacion;
import ar.edu.utn.frba.dds.repositorios.RepoIncidente;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class Entidad {
    private String nombre;

    @Setter
    private Localizacion localizacion;
    private List<Establecimiento> establecimientos;
    private Map<String,String> atributosVariables;
    private List<Usuario> usuariosAsignados;

    // private Boolean servicioPublico;

    public List<Servicio> servicios() {
        return this.establecimientos.stream().
                map(establecimiento -> establecimiento.getServicios()).
                flatMap(List::stream).
                collect(Collectors.toList());
    }

    public Entidad(String nombre) {
        this.nombre = nombre;
        this.establecimientos = new ArrayList<>();
        this.atributosVariables = new HashMap<String, String>();
        this.usuariosAsignados = new ArrayList<>();
    }

    public void agregarEstablecimientos(Establecimiento ... establecimientos) {
        Collections.addAll(this.establecimientos, establecimientos);
    }

    public void eliminarEstablecimiento(Establecimiento establecimiento){
        this.establecimientos.remove(establecimiento);
    }

    public void agregarAtributoVar(String nombre, String valor) {
        this.atributosVariables.put(nombre, valor);
    }

    public void eliminarAtributoVar(String nombre){
        this.atributosVariables.remove(nombre);
    }

    public void avisar_a_usuarios() {}

    public double getPromedioCierreRanking() {
        List<Incidente> incidentes = RepoIncidente.getInstancia().getListaIncidentes().stream()
                .filter(unIncidete -> unIncidete.seOriginoEnEntidad(this))
                .filter(incidente -> incidente.seReportoEnLaSemanaDeLaFecha(LocalDateTime.now()))
                .toList();

        return (double) incidentes.stream().mapToDouble(incidente -> {
            LocalDateTime fechaCierre = incidente.getHorarioCierre();
            LocalDateTime fechaApertura = incidente.getHorarioApertura();

            return ChronoUnit.MINUTES.between(fechaApertura, fechaCierre);
        }).count() / incidentes.size();
    }

    public int cantIncidentesEnLaSemana(List<Prestacion> listaDePrestacionesGlobal, LocalDateTime fechaDeSemana) {
        //Filtrar las prestacionesDeEntidad de la entidad
        List<Prestacion> prestacionesDeEntidad = listaDePrestacionesGlobal.stream().
                filter(prestacion -> prestacion.getEstablecimiento().getEntidad().equals(this)).toList();

//        List<Incidente> incidentesNoRepetidosDeLaSemana = new ArrayList();
        Integer cantidadIncidentesNoRepetidosDeLaSemana = 0;

        // Recorremos la lista de prestaciones con los incidentes que se abrieron en la ultima semana
        for (Prestacion prestacion : prestacionesDeEntidad) {
            List<Incidente> incidentesDeLaSemana = prestacion.getIncidentes().stream().
                    filter(incidente -> incidente.seReportoEnLaSemanaDeLaFecha(fechaDeSemana)).
                    toList();

            while (incidentesDeLaSemana.size() > 0) {
                Incidente primerIncidente = incidentesDeLaSemana.get(0);

                cantidadIncidentesNoRepetidosDeLaSemana++;
                incidentesDeLaSemana = incidentesDeLaSemana.stream().
                        filter(otroIncidente -> !otroIncidente.equals(primerIncidente)).toList();

                if (primerIncidente.getEstado().equals(EstadoIncidente.RESUELTO)) continue;

                incidentesDeLaSemana = incidentesDeLaSemana.stream().filter(otroIncidente ->
                        Duration.between(primerIncidente.getHorarioApertura(), otroIncidente.getHorarioApertura()).toHours() > 24
                ).toList();
            }
        }

        return cantidadIncidentesNoRepetidosDeLaSemana;
    }

}
