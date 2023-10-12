package ar.edu.utn.frba.dds.controllers;

import ar.edu.utn.frba.dds.models.comunidades.Comunidad;
import ar.edu.utn.frba.dds.models.comunidades.Perfil;
import ar.edu.utn.frba.dds.models.comunidades.Usuario;
import ar.edu.utn.frba.dds.models.incidentes.EstadoIncidente;
import ar.edu.utn.frba.dds.models.incidentes.Incidente;
import ar.edu.utn.frba.dds.models.incidentes.Prestacion;
import ar.edu.utn.frba.dds.models.repositorios.*;
import ar.edu.utn.frba.dds.models.repositorios.reposDeprecados.RepoComunidadDeprecado;
import ar.edu.utn.frba.dds.models.repositorios.reposDeprecados.RepoPrestacionDeprecado;
import ar.edu.utn.frba.dds.models.repositorios.reposDeprecados.RepoUsuarioDeprecado;
import ar.edu.utn.frba.dds.models.serviciosPublicos.Entidad;
import ar.edu.utn.frba.dds.models.serviciosPublicos.Establecimiento;
import ar.edu.utn.frba.dds.models.serviciosPublicos.Servicio;
import io.javalin.http.Context;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class IncidentesController {
    private RepoIncidente repoIncidente;
    private RepoPerfil repoPerfil;
    private RepoEntidad repoEntidad;
    private RepoEstablecimiento repoEstablecimiento;
    private RepoServicio repoServicio;
    private RepoUsuario repoUsuario;
    private RepoComunidad repoComunidad;

    public IncidentesController(RepoIncidente repoIncidente,
                                RepoPerfil repoPerfil,
                                RepoEntidad repoEntidad,
                                RepoEstablecimiento repoEstablecimiento,
                                RepoServicio repoServicio,
                                RepoUsuario repoUsuario,
                                RepoComunidad repoComunidad) {
        this.repoIncidente = repoIncidente;
        this.repoPerfil = repoPerfil;
        this.repoEntidad = repoEntidad;
        this.repoServicio = repoServicio;
        this.repoEstablecimiento = repoEstablecimiento;
        this.repoUsuario = repoUsuario;
        this.repoComunidad = repoComunidad;
    }

    public void index(Context context){
        Map<String, Object> model = new HashMap<>();
        List<Incidente> incidentes = this.repoIncidente.buscarPorComunidad(Integer.parseInt(context.pathParam("id")));

        model.put("incidentes", incidentes);
        context.render("incidentes/incidentes.hbs", model);
    }

    public void show(Context context){
        Incidente incidente = this.repoIncidente.buscarPorId(Integer.valueOf(context.pathParam("id")));
        Map<String, Object> model = new HashMap<>();
        model.put("incidente", incidente);
        context.render("incidentes/incidente.hbs", model);
    }

    public void crear(Context context){
        Map<String, Object> model = new HashMap<>();
        List<Entidad> entidades = this.repoEntidad.buscarTodos();
        List<Establecimiento> establecimientos = this.repoEstablecimiento.buscarTodos();
        List<Servicio> servicios = this.repoServicio.buscarTodos();
        model.put("entidades", entidades);
        model.put("establecimientos", establecimientos);
        model.put("servicios", servicios);
        context.render("incidentes/crear.hbs", model);
    }

    public void procesar_creacion(Context context){
        Perfil perfilApertura = this.repoPerfil.buscarPorId(Integer.valueOf(context.cookie("perfil_id")));
        Establecimiento establecimiento = this.repoEstablecimiento.buscarPorId(Integer.valueOf(context.formParam("establecimiento")));
        Servicio servicio = this.repoServicio.buscarPorId(Integer.valueOf(context.formParam("servicio")));
        String observaciones = context.formParam("observaciones");
        Incidente incidente = crearIncidenteParaPerfil(establecimiento, servicio, perfilApertura, observaciones);
        this.repoIncidente.guardar(incidente);

        context.redirect("/comunidades/" + perfilApertura.getComunidad().getId() + "/incidentes");
    }

    public void save(Context context){
        // TODO
    }

    public void edit(Context context) {
        // TODO
    }

    public void delete(Context context){
        // TODO
    }

    public void cerrar(Context context){
        Incidente incidenteACerrar = this.repoIncidente.buscarPorId(Integer.valueOf(context.pathParam("id")));
        Perfil perfil = this.repoPerfil.buscarPorId(Integer.valueOf(context.cookie("perfil_id")));

        incidenteACerrar.setUsuarioCierre(this.repoUsuario.buscarPorId(perfil.getUsuario().getId()));
        incidenteACerrar.setHorarioCierre(LocalDateTime.now());
        incidenteACerrar.setEstado(EstadoIncidente.RESUELTO);

        // notificar a cada miembro
        perfil.getComunidad().getMiembros()
                .forEach(miembro -> miembro.getUsuario().recibirNotificacionDeCierreDeIncidente(incidenteACerrar));


        repoIncidente.modificar(incidenteACerrar);
        context.redirect("/comunidades/" + perfil.getComunidad().getId() + "/incidentes");
    }

    public void asignarParametros(Incidente incidente, Context context){
        // TODO
    }

    public void crear_o_agregar_prestacion(Establecimiento establecimiento_a_buscar, Servicio servicio_a_buscar, Incidente incidente){
        RepoPrestacion repoPrestacion = new RepoPrestacion();
        List<Prestacion> listaPrestaciones = repoPrestacion.buscarTodos();

        List <Prestacion> listaPrestacionesDelEstablecimiento = listaPrestaciones.stream()
                .filter(prestacion -> prestacion.getEstablecimiento().getNombre() == establecimiento_a_buscar.getNombre())
                .toList();

        if(listaPrestacionesDelEstablecimiento.isEmpty()){
            Prestacion nuevaPrestacion = new Prestacion(establecimiento_a_buscar, servicio_a_buscar);
            nuevaPrestacion.agregarIncidente(incidente);
            repoPrestacion.guardar(nuevaPrestacion);
            incidente.setPrestacion(nuevaPrestacion);
        }
        else {
            Prestacion prestacionDelServicioDelEstablecimiento =  listaPrestacionesDelEstablecimiento.stream()
                    .filter(prestacion -> prestacion.getServicio().getNombre() == servicio_a_buscar.getNombre())
                    .findAny()
                    .orElse(null);

            if(prestacionDelServicioDelEstablecimiento == null){
                Prestacion nuevaPrestacion = new Prestacion(establecimiento_a_buscar, servicio_a_buscar);
                nuevaPrestacion.agregarIncidente(incidente);
                incidente.setPrestacion(nuevaPrestacion);
                repoPrestacion.guardar(nuevaPrestacion);
            }
            else {
                prestacionDelServicioDelEstablecimiento.agregarIncidente(incidente);
                incidente.setPrestacion(prestacionDelServicioDelEstablecimiento);
            }
        }
    }

    public Incidente crearIncidenteParaPerfil(Establecimiento establecimiento, Servicio servicio, Perfil perfil, String observaciones) {

        Comunidad unaComunidad = perfil.getComunidad();
        Incidente incidente = new Incidente(establecimiento, unaComunidad.getNombre(), servicio, perfil.getUsuario());
        incidente.setObservaciones(observaciones);
        incidente.setComunidad(unaComunidad);
        unaComunidad.agregarIncidente(incidente);

        crear_o_agregar_prestacion(establecimiento, servicio, incidente);

        // notificar a cada miembro
        unaComunidad.getMiembros()
                .forEach(miembro -> miembro.getUsuario().recibirNotificacionDeAperturaDeIncidente(incidente));

        List<Usuario> usuarioList = new RepoUsuario().buscarTodos(); // no se si esta bien
        usuarioList.stream()
                .filter(
                        unUsuario -> unUsuario.getServiciosInteres().contains(servicio) &&
                                unUsuario.getEntidadesInteres()
                                        .contains(establecimiento.getEntidad())
                )
                // se lo mandamos a un solo perfil de cada usuario para que no reciba notificaciones repetidas (por cada uno de sus perfiles)
                .forEach(usuario -> usuario.
                        recibirNotificacionDeAperturaDeIncidente(new Incidente(establecimiento, "Servicio Interes Particular", servicio, perfil.getUsuario())));

        return incidente;
    }

    public void crearIncidente(Establecimiento establecimiento, Servicio servicio, Usuario usuarioApertura) {

        List<Comunidad> comunidades = usuarioApertura.getPerfiles()
                .stream()
                .map(perfil -> perfil.getComunidad()).toList();

        for(Comunidad unaComunidad : comunidades) {
            Incidente incidente = new Incidente(establecimiento, unaComunidad.getNombre(), servicio, usuarioApertura);
            unaComunidad.agregarIncidente(incidente);
            // TODO Esto lo tendria que hacer el prestacion controller (creo)
            crear_o_agregar_prestacion(establecimiento, servicio, incidente);

            // notificar a cada miembro
            unaComunidad.getMiembros()
                    .forEach(perfil -> perfil.getUsuario().recibirNotificacionDeAperturaDeIncidente(incidente));
        }

        List<Usuario> usuarioList = new RepoUsuario().buscarTodos(); // no se si esta bien
        usuarioList.stream()
                .filter(
                        unUsuario -> unUsuario.getServiciosInteres().contains(servicio) &&
                                unUsuario.getEntidadesInteres()
                                        .contains(establecimiento.getEntidad())
                )
                // se lo mandamos a un solo perfil de cada usuario para que no reciba notificaciones repetidas (por cada uno de sus perfiles)
                .forEach(usuario -> usuario.
                        recibirNotificacionDeAperturaDeIncidente(new Incidente(establecimiento, "Servicio Interes Particular", servicio, usuarioApertura)));
    }

}
