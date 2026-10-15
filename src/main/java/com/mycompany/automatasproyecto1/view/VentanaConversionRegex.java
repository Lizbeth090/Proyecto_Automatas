
package com.mycompany.automatasproyecto1.view;

import com.mxgraph.view.mxGraph;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxConstants;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class VentanaConversionRegex extends JFrame {

    private final JPanel panelGrafo =
            new JPanel(new BorderLayout());

    // Un único lienzo para toda la conversión
    private final mxGraphComponent componenteVisual =
            new mxGraphComponent(new mxGraph());

    private final JLabel etiquetaPaso =
            new JLabel("Conversión no iniciada");

    private final JTextArea areaDescripcion =
            new JTextArea();

    private final JTextField campoRegex =
            new JTextField();

    private final JButton botonAnterior =
            new JButton("Anterior");

    private final JButton botonSiguiente =
            new JButton("Siguiente");
    
    private final JButton botonReproducir =
        new JButton("Reproducir");
    
    private final JTextField campoCadenaVerificacion =
        new JTextField();

    private final JButton botonVerificar =
            new JButton("Verificar cadena");
    
    private final JButton botonIniciar =
        new JButton("Iniciar conversión");

    private Runnable accionConversion;

    private final JLabel etiquetaAFD =
            new JLabel("AFD: ---");

    private final JLabel etiquetaRegex =
            new JLabel("Regex: ---");

    private final JLabel etiquetaComparacion =
            new JLabel("Resultado: ---");

    private java.util.function.Consumer<String>
            accionVerificacion;

    private final javax.swing.Timer temporizador =
        new javax.swing.Timer(
                1200,
                e -> avanzarAutomaticamente()
        );

    private final List<PasoVisual> pasos =
            new ArrayList<>();

    private int indicePaso = 0;

    // INFORMACIÓN DE CADA PASO
    private static class PasoVisual {

        mxGraph grafo;
        String titulo;
        String descripcion;
        String regex;

        PasoVisual(mxGraph grafo, String titulo,
                   String descripcion, String regex) {

            this.grafo = grafo;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.regex = regex;
        }
    }

    // CONSTRUCTOR
    public VentanaConversionRegex() {

        setTitle("Conversión de autómata a Regex");
        setSize(950, 650);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // ENCABEZADO
        JPanel encabezado =
                new JPanel(new FlowLayout(FlowLayout.LEFT));

        encabezado.add(new JLabel("Proceso:"));
        encabezado.add(etiquetaPaso);

        add(encabezado, BorderLayout.NORTH);

        // CONFIGURACIÓN DEL LIENZO
        panelGrafo.setBorder(
                BorderFactory.createTitledBorder(
                        "Grafo de conversión"
                )
        );

        componenteVisual.setConnectable(false);
        componenteVisual.setPageVisible(false);

        componenteVisual.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        componenteVisual.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_NEVER
        );

        panelGrafo.add(
                componenteVisual,
                BorderLayout.CENTER
        );

        // DESCRIPCIÓN
        areaDescripcion.setEditable(false);
        areaDescripcion.setLineWrap(true);
        areaDescripcion.setWrapStyleWord(true);

        JScrollPane scroll =
                new JScrollPane(areaDescripcion);

        scroll.setPreferredSize(
                new Dimension(100, 120)
        );

        scroll.setBorder(
                BorderFactory.createTitledBorder(
                        "Descripción del paso"
                )
        );

        JPanel centro =
                new JPanel(new BorderLayout(8, 8));

        centro.add(panelGrafo, BorderLayout.CENTER);
        centro.add(scroll, BorderLayout.SOUTH);

        add(centro, BorderLayout.CENTER);

        // RESULTADO
        JPanel inferior =
                new JPanel(new BorderLayout(8, 8));

        JPanel resultado =
                new JPanel(new BorderLayout(8, 8));

        resultado.add(
                new JLabel("Regex final:"),
                BorderLayout.WEST
        );

        campoRegex.setEditable(false);

        resultado.add(
                campoRegex,
                BorderLayout.CENTER
        );

        inferior.add(resultado, BorderLayout.NORTH);

        // Panel de verificación de equivalencia
        JPanel panelVerificacion = new JPanel(
                new BorderLayout(8, 8)
        );

        panelVerificacion.setBorder(
                BorderFactory.createTitledBorder(
                        "Verificación de equivalencia"
                )
        );

        JPanel filaEntrada = new JPanel(
                new BorderLayout(8, 0)
        );

        filaEntrada.add(
                new JLabel("Cadena:"),
                BorderLayout.WEST
        );

        filaEntrada.add(
                campoCadenaVerificacion,
                BorderLayout.CENTER
        );

        botonVerificar.setEnabled(false);

        filaEntrada.add(
                botonVerificar,
                BorderLayout.EAST
        );

        JPanel filaResultados = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 15, 0)
        );

        filaResultados.add(etiquetaAFD);
        filaResultados.add(etiquetaRegex);
        filaResultados.add(etiquetaComparacion);

        panelVerificacion.add(
                filaEntrada,
                BorderLayout.NORTH
        );

        panelVerificacion.add(
                filaResultados,
                BorderLayout.CENTER
        );

        inferior.add(
                panelVerificacion,
                BorderLayout.CENTER
        );
        
        
        // BOTONES
        JPanel controles =
                new JPanel(new FlowLayout(FlowLayout.RIGHT));


        JButton botonCerrar =
                new JButton("Cerrar");

        botonAnterior.setEnabled(false);
        botonSiguiente.setEnabled(false);

        // Pendiente de conectar con el algoritmo
        botonIniciar.setEnabled(false);
        
        botonIniciar.addActionListener(e -> {
            if (accionConversion != null) {
                accionConversion.run();
            }
        });

        botonAnterior.addActionListener(e ->
                mostrarPaso(indicePaso - 1)
        );

        botonSiguiente.addActionListener(e ->
                mostrarPaso(indicePaso + 1)
        );
        
        
        botonReproducir.addActionListener(e -> {
            alternarReproduccion();
        });

        botonCerrar.addActionListener(e -> dispose());
        
        botonVerificar.addActionListener(e -> {

            if (accionVerificacion != null) {
                accionVerificacion.accept(
                        campoCadenaVerificacion.getText()
                );
            }

        });

        controles.add(botonAnterior);
        controles.add(botonSiguiente);
        controles.add(botonReproducir);
        controles.add(botonIniciar);
        controles.add(botonCerrar);

        inferior.add(controles, BorderLayout.SOUTH);

        add(inferior, BorderLayout.SOUTH);

        // AJUSTAR AL ABRIR LA VENTANA
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {

                if (!pasos.isEmpty()) {

                    ajustarGrafoAlLienzo(
                            pasos.get(indicePaso).grafo
                    );

                    componenteVisual.repaint();
                }
            }
        });
    }

    // MOSTRAR COPIA DEL AUTOMATA ORIGINAL
    public void mostrarCopiaAutomata(mxGraph original) {

        pasos.clear();
        indicePaso = 0;

        agregarPasoVisual(
                original,
                "Autómata original",
                "Esta es una copia del autómata creado "
                + "en el editor principal.",
                ""
        );
    }

    // AGREGAR PASO VISUAL
    public void agregarPasoVisual(
            mxGraph original,
            String titulo,
            String descripcion,
            String regex) {

        mxGraph copia = new mxGraph();

        // Bloquear modificaciones
        copia.setCellsEditable(false);
        copia.setCellsMovable(false);
        copia.setCellsResizable(false);
        copia.setCellsBendable(false);
        copia.setCellsDisconnectable(false);
        copia.setAllowDanglingEdges(false);

        Object[] celdas = original.getChildCells(
                original.getDefaultParent(),
                true,
                true
        );

        Object[] clones =
                original.cloneCells(celdas, true);

        copia.getModel().beginUpdate();

        try {

            copia.addCells(clones);

        } finally {

            copia.getModel().endUpdate();
        }

        pasos.add(
                new PasoVisual(
                        copia,
                        titulo,
                        descripcion,
                        regex
                )
        );

        mostrarPaso(indicePaso);
    }

    // CAMBIAR ENTRE PASOS
    private void mostrarPaso(int indice) {

        if (indice < 0 || indice >= pasos.size()) {
            return;
        }

        indicePaso = indice;

        PasoVisual paso = pasos.get(indicePaso);

        // Preparar posición antes de mostrar el grafo
        ajustarGrafoAlLienzo(paso.grafo);

        // Cambiar solamente el grafo.
        // No se destruye el componente visual.
        componenteVisual.setGraph(paso.grafo);

        componenteVisual.getViewport().setViewPosition(
                new Point(0, 0)
        );

        // Actualizar información
        etiquetaPaso.setText(
                "Paso " + (indicePaso + 1)
                + " de " + pasos.size()
                + " - " + paso.titulo
        );

        areaDescripcion.setText(paso.descripcion);
        campoRegex.setText(paso.regex);

        // Actualizar botones
        actualizarBotones();
        
        componenteVisual.repaint();
    }

    // CENTRAR Y AJUSTAR EL GRAFO
    private void ajustarGrafoAlLienzo(mxGraph grafo) {

        Dimension area =
                componenteVisual.getViewport().getExtentSize();

        if (area.width <= 0 || area.height <= 0) {
            return;
        }

        grafo.getView().validate();

        mxRectangle limites =
                grafo.getView().getGraphBounds();

        if (limites == null
                || limites.getWidth() <= 0
                || limites.getHeight() <= 0) {
            return;
        }

        // Obtener la transformación actual
        double escalaActual =
                grafo.getView().getScale();

        if (escalaActual <= 0) {
            return;
        }

        mxPoint trasladoActual =
                grafo.getView().getTranslate();

        // Recuperar límites sin escala
        double x = limites.getX() / escalaActual
                - trasladoActual.getX();

        double y = limites.getY() / escalaActual
                - trasladoActual.getY();

        double ancho =
                limites.getWidth() / escalaActual;

        double alto =
                limites.getHeight() / escalaActual;

        double margen = 40;

        double disponibleX =
                area.getWidth() - margen * 2;

        double disponibleY =
                area.getHeight() - margen * 2;

        if (disponibleX <= 0 || disponibleY <= 0) {
            return;
        }

        // Calcular la escala adecuada
        double escala = Math.min(
                1.0,
                Math.min(
                        disponibleX / ancho,
                        disponibleY / alto
                )
        );

        if (escala <= 0) {
            return;
        }

        // Calcular desplazamiento al centro
        double moverX =
                (area.getWidth() - ancho * escala)
                / (2 * escala) - x;

        double moverY =
                (area.getHeight() - alto * escala)
                / (2 * escala) - y;

        // Aplicar ambos cambios al mismo tiempo
        grafo.getView().scaleAndTranslate(
                escala,
                moverX,
                moverY
        );
    }

    // PRUEBA INDEPENDIENTE
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            VentanaConversionRegex ventana =
                    new VentanaConversionRegex();

            ventana.setVisible(true);
        });
    }
    
    //REPRODUCCIÓN DEL AUTOMATA 
    
    
    private void alternarReproduccion() {

        if (temporizador.isRunning()) {

            temporizador.stop();

        } else {

            if (pasos.size() < 2) {
                return;
            }

            // Si terminó, comenzar desde el primer paso
            if (indicePaso >= pasos.size() - 1) {
                mostrarPaso(0);
            }

            temporizador.start();
        }

        actualizarBotones();
    }

    private void avanzarAutomaticamente() {

        if (indicePaso < pasos.size() - 1) {
            mostrarPaso(indicePaso + 1);
        }

        if (indicePaso >= pasos.size() - 1) {
            temporizador.stop();
        }

        actualizarBotones();
    }

    private void actualizarBotones() {

        boolean reproduciendo = temporizador.isRunning();

        botonAnterior.setEnabled(
                !reproduciendo && indicePaso > 0
        );

        botonSiguiente.setEnabled(
                !reproduciendo
                && indicePaso < pasos.size() - 1
        );

        botonReproducir.setEnabled(pasos.size() > 1);

        botonReproducir.setText(
                reproduciendo ? "Pausar" : "Reproducir"
        );
    }
    
    //DETENER EL TEMPORIZADOR AL CERRAR LA VENTANA
    
    @Override
    public void dispose() {
        temporizador.stop();
        super.dispose();
    }
    
    // Configurar verificación de cadenas
    public void configurarVerificador(
            java.util.function.Consumer<String> accion) {

        this.accionVerificacion = accion;
        botonVerificar.setEnabled(accion != null);
    }

    // Mostrar resultados de equivalencia
    public void mostrarResultadoVerificacion(
            boolean aceptadaAFD,
            boolean aceptadaRegex) {

        etiquetaAFD.setText(
                "AFD: " + (aceptadaAFD
                        ? "Aceptada" : "Rechazada")
        );

        etiquetaRegex.setText(
                "Regex: " + (aceptadaRegex
                        ? "Aceptada" : "Rechazada")
        );

        if (aceptadaAFD == aceptadaRegex) {

            etiquetaComparacion.setText(
                    "Resultado: Coinciden"
            );

            etiquetaComparacion.setForeground(
                    new Color(30, 135, 65)
            );

        } else {

            etiquetaComparacion.setText(
                    "Resultado: No coinciden"
            );

            etiquetaComparacion.setForeground(
                    new Color(190, 45, 45)
            );
        }
    }
    
    
    
    public void resaltarEstadoEnPaso(
            int numeroPaso, String nombreEstado) {

        if (numeroPaso < 0 || numeroPaso >= pasos.size()) {
            return;
        }

        mxGraph grafo = pasos.get(numeroPaso).grafo;

        Object[] estados = grafo.getChildVertices(
                grafo.getDefaultParent()
        );

        for (Object celda : estados) {

            Object valor = grafo.getModel().getValue(celda);

            if (valor != null
                    && valor.toString().trim().equals(nombreEstado)) {

                grafo.getModel().beginUpdate();

                try {
                    // Resaltar el estado seleccionado
                    grafo.setCellStyles(
                            mxConstants.STYLE_FILLCOLOR,
                            "#FFF3B0",
                            new Object[]{celda}
                    );

                    grafo.setCellStyles(
                            mxConstants.STYLE_STROKECOLOR,
                            "#D97706",
                            new Object[]{celda}
                    );

                    grafo.setCellStyles(
                            mxConstants.STYLE_STROKEWIDTH,
                            "4",
                            new Object[]{celda}
                    );

                } finally {
                    grafo.getModel().endUpdate();
                }

                if (indicePaso == numeroPaso) {
                    componenteVisual.repaint();
                }

                break;
            }
        }
    }
    
    
    
    public void agregarPruebaEliminacionVisual(
            int pasoOrigen, String nombreEstado) {

        if (pasoOrigen < 0 || pasoOrigen >= pasos.size()) {
            return;
        }

        mxGraph original = pasos.get(pasoOrigen).grafo;

        agregarPasoVisual(
                original,
                "Eliminación visual de " + nombreEstado,
                "Demostración visual de la eliminación. "
                + "Las nuevas transiciones serán calculadas "
                + "por el algoritmo de conversión.",
                ""
        );

        mxGraph copia = pasos.get(pasos.size() - 1).grafo;

        Object[] estados = copia.getChildVertices(
                copia.getDefaultParent()
        );

        for (Object celda : estados) {

            Object valor = copia.getModel().getValue(celda);

            if (valor != null
                    && valor.toString().trim().equals(nombreEstado)) {

                copia.getModel().beginUpdate();

                try {
                    copia.removeCells(
                            new Object[]{celda}, true
                    );
                } finally {
                    copia.getModel().endUpdate();
                }

                break;
            }
        }
    }
    
    
    
    // Resaltar una transición en un paso
    public void resaltarTransicionEnPaso(
            int numeroPaso,
            String nombreOrigen,
            String nombreDestino) {

        if (numeroPaso < 0 || numeroPaso >= pasos.size()) {
            return;
        }

        mxGraph grafo = pasos.get(numeroPaso).grafo;

        Object[] aristas = grafo.getChildEdges(
                grafo.getDefaultParent()
        );

        grafo.getModel().beginUpdate();

        try {
            for (Object arista : aristas) {

                Object origen = grafo.getModel()
                        .getTerminal(arista, true);

                Object destino = grafo.getModel()
                        .getTerminal(arista, false);

                if (origen == null || destino == null) {
                    continue;
                }

                String desde = String.valueOf(
                        grafo.getModel().getValue(origen)
                ).trim();

                String hasta = String.valueOf(
                        grafo.getModel().getValue(destino)
                ).trim();

                if (desde.equals(nombreOrigen)
                        && hasta.equals(nombreDestino)) {

                    grafo.setCellStyles(
                            mxConstants.STYLE_STROKECOLOR,
                            "#2563EB",
                            new Object[]{arista}
                    );

                    grafo.setCellStyles(
                            mxConstants.STYLE_STROKEWIDTH,
                            "4",
                            new Object[]{arista}
                    );

                    grafo.setCellStyles(
                            mxConstants.STYLE_FONTCOLOR,
                            "#1D4ED8",
                            new Object[]{arista}
                    );
                }
            }

        } finally {
            grafo.getModel().endUpdate();
        }

        if (indicePaso == numeroPaso) {
            componenteVisual.repaint();
        }
    }
    
    
    public void configurarAccionConversion(Runnable accion) {
        this.accionConversion = accion;
        botonIniciar.setEnabled(accion != null);
    }

}
