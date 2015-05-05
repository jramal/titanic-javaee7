package com.titanic.ventapasajes.controller;

import com.titanic.ventapasajes.modelo.*;
import com.titanic.ventapasajes.repositorio.ProgramacionRepositorio;
import com.titanic.ventapasajes.service.RegistroVentaService;
import com.titanic.ventapasajes.util.FacesUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.CellEditEvent;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Celeritech Peru on 02/03/2015.
 */
@Named
@RequestScoped
public class SeleccionarAsientosBean implements Serializable {


    private static final long serialVersionUID = 1L;

    @Inject
    private RegistroVentaService ventaService;

    @Inject
    private ProgramacionRepositorio programacionRepositorio;

    @Inject
    private HttpServletRequest request;

    private Programacion programacion;

    private Venta venta;



    public void inicializar(){


        if(!FacesUtil.isPostback()) {
            venta = ventaService.obtenerVenta(programacion);
            if (venta == null) {
                nuevaVenta();
            }
        }

    }




    public Programacion getProgramacion() {
        return programacion;
    }

    public void setProgramacion(Programacion programacion) {
        this.programacion = programacion;
    }



    private void nuevaVenta() {
        venta = new Venta();
        venta.setProgramacion(programacion);
        venta.setTotalVenta(BigDecimal.ZERO);

        List<FilaBoletoSuperior> filasBoletosSuperiores = new ArrayList<>();
        List<FilaBoletoInferior> filasBoletosInferiores = new ArrayList<>();

        venta.setFilasBoletoSuperiores(clonarFilasSuperiores(filasBoletosSuperiores));
        venta.setFilasBoletosInferiores(clonarFilasInferiores(filasBoletosInferiores));

        ventaService.registrarVenta(venta);
    }

    private List<FilaBoletoInferior> clonarFilasInferiores(List<FilaBoletoInferior> filasBoletosInferiores) {

        for(Fila fila: programacion.getBus().getFilasInferiores()){

            FilaBoletoInferior filaBoletoInferior = new FilaBoletoInferior();
            filaBoletoInferior.setUbicacionPlanta(fila.getUbicacionPlanta());
            filaBoletoInferior.setVenta(venta);
            filaBoletoInferior.setBoletosInferiores(new ArrayList<BoletoInferior>());

            for(Celda celdaInferior: fila.getCeldasInferiores()){

                BoletoInferior boletoInferior = new BoletoInferior();
                boletoInferior.setCalidad(celdaInferior.getCalidad());
                boletoInferior.setEstadoBoleto(celdaInferior.getEstadoCelda());
                boletoInferior.setFilaBoletoInferior(filaBoletoInferior);
                boletoInferior.setNumeroAsiento(celdaInferior.getNumeroAsiento());
                boletoInferior.setNumeroBoleto(celdaInferior.getNumeroCelda());
                boletoInferior.setTipoBoleto(celdaInferior.getTipoCelda());
                boletoInferior.setPrecio(new BigDecimal(0));
                filaBoletoInferior.getBoletosInferiores().add(boletoInferior);

            }

            filasBoletosInferiores.add(filaBoletoInferior);
        }

        return filasBoletosInferiores;
    }

    private List<FilaBoletoSuperior> clonarFilasSuperiores(List<FilaBoletoSuperior> filasBoletosSuperiores) {

        for(FilaSuperior filaSuperior: programacion.getBus().getFilasSuperiores()){

            FilaBoletoSuperior filaBoletoSuperior = new FilaBoletoSuperior();
            filaBoletoSuperior.setUbicacionPlanta(filaSuperior.getUbicacionPlanta());
            filaBoletoSuperior.setVenta(venta);
            filaBoletoSuperior.setBoletosSuperiores(new ArrayList<BoletoSuperior>());

            for(CeldaSuperior celdaSuperior: filaSuperior.getCeldasSuperiores()){

                BoletoSuperior boletoSuperior = new BoletoSuperior();
                boletoSuperior.setCalidad(celdaSuperior.getCalidad());
                boletoSuperior.setEstadoBoleto(celdaSuperior.getEstadoCelda());
                boletoSuperior.setFilaBoletoSuperior(filaBoletoSuperior);
                boletoSuperior.setNumeroAsiento(celdaSuperior.getNumeroAsiento());
                boletoSuperior.setNumeroBoleto(celdaSuperior.getNumeroCelda());
                boletoSuperior.setTipoBoleto(celdaSuperior.getTipoCelda());
                boletoSuperior.setPrecio(new BigDecimal(0));
                filaBoletoSuperior.getBoletosSuperiores().add(boletoSuperior);

            }

            filasBoletosSuperiores.add(filaBoletoSuperior);
        }

        return filasBoletosSuperiores;
    }


    public void notificarPUSH() {
        String summary = "Reservar Reservado";
        String detail = "Nuevo asiento reservado";
        String CHANNEL = "/notify";

        EventBus eventBus = EventBusFactory.getDefault().eventBus();
        eventBus.publish(CHANNEL,
                new FacesMessage(StringEscapeUtils.escapeHtml(summary), StringEscapeUtils.escapeHtml(detail)));
    }


    public void onCellEdit(CellEditEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();

        if(newValue != null && !newValue.equals(oldValue)) {

            this.venta = ventaService.registrarVenta(this.venta);
            notificarPUSH();
        }
    }

    public EstadoBoleto[] getEstadoBoleto() {
        return EstadoBoleto.values();
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }
}