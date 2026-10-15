package com.mycompany.automatasproyecto1.view;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;
import com.mxgraph.util.mxEvent;

import com.mycompany.automatasproyecto1.controller.AutomataController;
import com.mycompany.automatasproyecto1.model.Automata;
import com.mycompany.automatasproyecto1.model.Estado;
import com.mycompany.automatasproyecto1.model.Transicion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Ventana principal del editor de autómatas.
 */
public class MainFrame extends JFrame {

    private JPanel panelHerramientas;
    private JPanel panelLienzo;
    private JPanel panelSimulacion;

    private mxGraph graph;
    private mxGraphComponent graphComponent;

    private AutomataController controller;
    private Automata automata;

    private boolean modoAgregarEstado = false;
    
    
    private Object marcadorEstadoInicial = null;

    private Map<Object, Estado> mapaEstados;
    private Map<Object, Transicion> mapaTransiciones;

    private boolean modoCrearTransicion = false;
    private Estado estadoOrigenTransicion = null;
    private Object celdaOrigenTransicion = null;

    private Timer timerSimulacion;
    private String cadenaSimulacion = "";
    private int posicionSimulacion = 0;
    private Estado estadoActualSimulacion = null;

    private JTextField campoCadena;
    private JLabel etiquetaResultado;
    
    private final Map<Object, String> estilosResaltados = new HashMap<>();
    
    private final JLabel etiquetaAviso = new JLabel(" ");

    private final javax.swing.Timer temporizadorAviso =
        new javax.swing.Timer(2800, e -> {
            etiquetaAviso.setText(" ");
        });
    
    /**
     * Constructor de la ventana.
     */
    public MainFrame() {

        controller = new AutomataController();
        automata = controller.getAutomata();

        mapaEstados = new HashMap<>();
        mapaTransiciones = new HashMap<>();

        setTitle("Editor de Autómatas y Expresiones Regulares");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        
       // BARRA INFORMATIVA
        etiquetaAviso.setBorder(
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );

        etiquetaAviso.setForeground(
                new java.awt.Color(45, 90, 150)
        );

        temporizadorAviso.setRepeats(false);

        add(etiquetaAviso, BorderLayout.NORTH);

        
        // PANEL DE HERRAMIENTAS
        
        panelHerramientas = new JPanel();

        panelHerramientas.setPreferredSize(
                new Dimension(180, 0)
        );

        panelHerramientas.setBorder(
                BorderFactory.createTitledBorder(
                        "Herramientas"
                )
        );

        panelHerramientas.setLayout(
                new GridLayout(7, 1, 5, 5)
        );

        JButton btnAgregarEstado =
                new JButton("Agregar Estado");

        JButton btnTransicion =
                new JButton("Crear Transición");

        JButton btnInicial =
                new JButton("Estado Inicial");

        JButton btnAceptacion =
                new JButton("Estado Aceptación");

        JButton btnEliminar =
                new JButton("Eliminar");

       
        JButton btnRenombrar =
        new JButton("Renombrar Estado");
        
        JButton btnEditarTransicion =
        new JButton("Editar Transición");

        panelHerramientas.add(btnAgregarEstado);
        panelHerramientas.add(btnTransicion);
        panelHerramientas.add(btnInicial);
        panelHerramientas.add(btnAceptacion);
        panelHerramientas.add(btnEliminar);
        panelHerramientas.add(btnRenombrar);
        panelHerramientas.add(btnEditarTransicion);

        
        // PANEL DEL LIENZO
        

        panelLienzo = new JPanel();

        panelLienzo.setBorder(
                BorderFactory.createTitledBorder(
                        "Lienzo del Autómata"
                )
        );

        panelLienzo.setLayout(
                new BorderLayout()
        );

        graph = new mxGraph();
        
        // FLECHA INICIAL FUERA DEL CIRCULO
        
        graph.setConstrainChildren(false);
        
        graph.setCellsEditable(false);
        
        // Evitar que las transiciones se desconecten
        
        graph.setCellsDisconnectable(false);
        
        graph.setAllowDanglingEdges(false);
        
        graph.setDisconnectOnMove(false);

        // Evitar deformarlas arrastrando sus puntos
        
        graph.setCellsBendable(false);
        
        // Impedir cambiar el tamaño de los estados
        
        graph.setCellsResizable(false);

        // Permitir mover los estados
        
        graph.setCellsMovable(true);

        graph.addListener(mxEvent.CELLS_MOVED, (sender, evt) -> {
            Object[] movidas = (Object[]) evt.getProperty("cells");
            if (movidas == null) return;

            for (Object celda : movidas) {
                if (mapaEstados.containsKey(celda)) {
                    javax.swing.SwingUtilities.invokeLater(
                            this::actualizarCurvasTransiciones);
                    break;
                }
            }
        });

        Object parent =
                graph.getDefaultParent();

        graphComponent =
                new mxGraphComponent(graph);
        
        graphComponent.setConnectable(false);
        

        getRootPane().getInputMap(
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
        ).put(
                javax.swing.KeyStroke.getKeyStroke("ESCAPE"),
                "cancelarModo"
        );

        getRootPane().getActionMap().put(
                "cancelarModo",
                new javax.swing.AbstractAction() {

                    @Override
                    public void actionPerformed(
                            java.awt.event.ActionEvent e) {

                        if (!modoAgregarEstado && !modoCrearTransicion) {
                            return;
                        }

                        modoAgregarEstado = false;
                        limpiarModoTransicion();
                        mostrarAviso("Operación cancelada.");
                    }
                }
        );


        panelLienzo.add(
                graphComponent,
                BorderLayout.CENTER
        );

        
        // BOTÓN AGREGAR ESTADO
        

        btnAgregarEstado.addActionListener(e -> {

            modoAgregarEstado = true;

            modoCrearTransicion = false;

            estadoOrigenTransicion = null;

            celdaOrigenTransicion = null;

            graphComponent.setCursor(
                    new Cursor(
                            Cursor.CROSSHAIR_CURSOR
                    )
            );
        });

        
        // BOTÓN CREAR TRANSICIÓN
        

        btnTransicion.addActionListener(e -> {

            modoAgregarEstado = false;

            modoCrearTransicion = true;

            estadoOrigenTransicion = null;

            celdaOrigenTransicion = null;

            graphComponent.setCursor(
                    new Cursor(
                            Cursor.DEFAULT_CURSOR
                    )
            );

            mostrarAviso("Selecciona el estado de origen.");
        });

        
        // BOTÓN ESTADO INICIAL
      

        btnInicial.addActionListener(e -> {

            establecerEstadoInicial();

        });

        
        // BOTÓN ESTADO DE ACEPTACIÓN
        

        btnAceptacion.addActionListener(e -> {

            establecerEstadoAceptacion();

        });

        
        // BOTÓN ELIMINAR
        

        btnEliminar.addActionListener(e -> {

            eliminarElementoSeleccionado();

        });

        
        
        btnRenombrar.addActionListener(e -> {
            renombrarEstadoSeleccionado();
        });

        
        btnEditarTransicion.addActionListener(e -> {
            editarTransicionSeleccionada();
        });
        
        
        // EVENTOS DEL LIENZO
        

        graphComponent
                .getGraphControl()
                .addMouseListener(
                        new MouseAdapter() {

                    @Override
                    public void mousePressed(
                            MouseEvent e) {

                        // -------------------------------------------------
                        // MODO AGREGAR ESTADO
                        // -------------------------------------------------

                        if (modoAgregarEstado) {

                            crearEstadoEnPosicion(
                                    e.getX(),
                                    e.getY(),
                                    parent
                            );

                            return;
                        }

                        
                        // MODO CREAR TRANSICIÓN
                        

                        if (modoCrearTransicion) {

                            procesarClickTransicion(e);

                            return;
                        }
                    }
                }
        );

        // =========================================================
        // SINCRONIZAR POSICIÓN DE LOS ESTADOS
        // =========================================================

        graphComponent
                .getGraphControl()
                .addMouseListener(
                        new MouseAdapter() {

                    @Override
                    public void mouseReleased(
                            MouseEvent e) {

                        sincronizarPosicionEstado();
                    }
                }
        );

        
        // PANEL DE SIMULACIÓN
        

        panelSimulacion = new JPanel();

        panelSimulacion.setBorder(
                BorderFactory.createTitledBorder(
                        "Simulación"
                )
        );

        panelSimulacion.setLayout(
                new FlowLayout(
                        FlowLayout.LEFT
                )
        );

        JLabel etiquetaCadena =
                new JLabel("Cadena:");

        campoCadena =
                new JTextField(20);

        JButton btnSimular =
                new JButton("Simular");
        
        JButton btnConvertirRegex =
        new JButton("Convertir a Regex");
        
        JButton btnReiniciar =
        new JButton("Reiniciar Simulación");

        etiquetaResultado =
                new JLabel(
                        "Resultado: ---"
                );

        panelSimulacion.add(
                etiquetaCadena
        );

        panelSimulacion.add(
                campoCadena
        );

        panelSimulacion.add(
                btnSimular
        );
        
        panelSimulacion.add(
                btnConvertirRegex
        );

        panelSimulacion.add(
                btnReiniciar
        );

        panelSimulacion.add(
                etiquetaResultado
        );

        
        // BOTÓN SIMULAR

        btnSimular.addActionListener(e -> {
            
            restaurarColoresSimulacion();
            
            iniciarSimulacion();

        });
        
        
        //BOTÓN DE CONVERSION REGEX
        
        btnConvertirRegex.addActionListener(e -> {

            VentanaConversionRegex ventana =
                    new VentanaConversionRegex();

            ventana.mostrarCopiaAutomata(graph);
            ventana.setVisible(true);

        });
        
        
        //BOTÓN REINICIAR
        
        btnReiniciar.addActionListener(e -> {
            reiniciarSimulacion();
        });

        
        // AGREGAR PANELES A LA VENTANA
        

        add(
                panelHerramientas,
                BorderLayout.WEST
        );

        add(
                panelLienzo,
                BorderLayout.CENTER
        );

        add(
                panelSimulacion,
                BorderLayout.SOUTH
        );
    }

    // =============================================================
    // CREAR ESTADO
    // =============================================================

    private void crearEstadoEnPosicion(
            int x,
            int y,
            Object parent) {

        String nombre = 
                obtenerNombreEstadoDisponible();

        Estado estado =
                controller.crearEstado(
                        nombre,
                        x,
                        y
                );

        graph.getModel().beginUpdate();

        try {

            Object celda =
        graph.insertVertex(
                parent,
                null,
                estado.getNombre(),
                x - 40,
                y - 40,
                80,
                80,
                "shape=ellipse;"
                + "fillColor=#FFFFFF;"
                + "strokeColor=#000000;"
                + "strokeWidth=2;"
                + "fontSize=16;"
                + "align=center;"
                + "verticalAlign=middle;"
        );

            mapaEstados.put(
                    celda,
                    estado
            );

        } finally {

            graph.getModel().endUpdate();
        }

        

        modoAgregarEstado = false;

        graphComponent.setCursor(
                new Cursor(
                        Cursor.DEFAULT_CURSOR
                )
        );
    }

    
    // CREAR TRANSICIÓN
    

    private void procesarClickTransicion(
            MouseEvent e) {

        Object celda =
                graphComponent.getCellAt(
                        e.getX(),
                        e.getY()
                );

        if (celda == null) {
            return;
        }

        Estado estado =
                mapaEstados.get(celda);

        if (estado == null) {
            return;
        }

        
        // PRIMER CLICK: ESTADO ORIGEN
        

        if (estadoOrigenTransicion == null) {

            estadoOrigenTransicion =
                    estado;

            celdaOrigenTransicion =
                    celda;

            mostrarAviso(
                    "Origen: " + estado.getNombre()
                    + ". Ahora selecciona el destino."
            );

            return;
        }

        // ---------------------------------------------------------
        // SEGUNDO CLICK: ESTADO DESTINO
        // ---------------------------------------------------------

        Estado estadoDestino =
                estado;

        Object celdaDestino =
                celda;

        String simbolo =
                JOptionPane.showInputDialog(
                        this,
                        "Escribe el símbolo de la transición:",
                        "Crear Transición",
                        JOptionPane.PLAIN_MESSAGE
                );

        // Si se cancela
        if (simbolo == null) {

            limpiarModoTransicion();

            return;
        }

        simbolo =
                simbolo.trim();

        // No permitir símbolo vacío
        if (simbolo.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "El símbolo no puede estar vacío."
            );
            return;
        }
        
        // CREAR TRANSICIÓN EN EL MODELO
       

        Transicion transicion =
                controller.crearTransicion(
                        estadoOrigenTransicion,
                        estadoDestino,
                        simbolo
                );

        
        // CREAR TRANSICIÓN VISUAL
        

        graph.getModel().beginUpdate();

        try {

            Object celdaTransicion =
                    graph.insertEdge(
                            graph.getDefaultParent(),
                            null,
                            simbolo,
                            celdaOrigenTransicion,
                            celdaDestino
                    );

            // =====================================================
            // IMPORTANTE:
            //
            // Movemos la etiqueta de la transición ligeramente
            // hacia arriba para que no quede atravesada por la
            // línea de la flecha.
            // =====================================================

            mxGeometry geometriaTransicion =
                    graph.getModel()
                            .getGeometry(
                                    celdaTransicion
                            );

            if (geometriaTransicion != null) {

                geometriaTransicion.setOffset(
                        new mxPoint(
                                0,
                                -15
                        )
                );
            }

            

            graph.setCellStyles(
                    mxConstants
                            .STYLE_LABEL_BACKGROUNDCOLOR,
                    "#FFFFFF",
                    new Object[]{
                        celdaTransicion
                    }
            );

            mapaTransiciones.put(
                    celdaTransicion,
                    transicion
            );
            
            graph.setCellStyles(
                    mxConstants.STYLE_MOVABLE,
                    "0",
                    new Object[]{celdaTransicion}
            );

        } finally {

            graph.getModel().endUpdate();
        }

        actualizarCurvasTransiciones();

        // ---------------------------------------------------------
        // FINALIZAR MODO TRANSICIÓN
        // ---------------------------------------------------------

        limpiarModoTransicion();
        
        mostrarAviso("Transición creada correctamente.");
    }

    
    // ESTABLECER ESTADO INICIAL
    

    private void establecerEstadoInicial() {

        Object celda =
                graph.getSelectionCell();

        if (celda == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Primero selecciona un estado."
            );

            return;
        }

        Estado estado =
                mapaEstados.get(celda);

        if (estado == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "La selección no es un estado."
            );

            return;
        }

        controller.establecerEstadoInicial(
                estado
        );

        actualizarEtiquetasEstados();
        
        actualizarFlechaInicial(celda);

        JOptionPane.showMessageDialog(
                this,
                estado.getNombre()
                + " ahora es el estado inicial."
        );
    }

    
    // ESTABLECER ESTADO DE ACEPTACIÓN
    

    private void establecerEstadoAceptacion() {

        Object celda =
                graph.getSelectionCell();

        if (celda == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Primero selecciona un estado."
            );

            return;
        }

        Estado estado =
                mapaEstados.get(celda);

        if (estado == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "La selección no es un estado."
            );

            return;
        }

        // Si ya es de aceptación, lo quitamos.
        if (estado.isAceptacion()) {

            automata.eliminarEstadoAceptacion(
                    estado
            );

        } else {

            controller.establecerEstadoAceptacion(
                    estado
            );
        }

        actualizarEtiquetasEstados();

        actualizarEstiloEstados();
    }

    // =============================================================
    // ACTUALIZAR ETIQUETAS DE LOS ESTADOS
    // =============================================================

    private void actualizarEtiquetasEstados() {

        graph.getModel().beginUpdate();

        try {

            for (
                    Map.Entry<Object, Estado> entrada
                    : mapaEstados.entrySet()
            ) {

                Object celda =
                        entrada.getKey();

                Estado estado =
                        entrada.getValue();

                String texto =
                        estado.getNombre();

                

                // Estado de aceptación
                

                graph.getModel().setValue(
                        celda,
                        texto
                );
            }

        } finally {

            graph.getModel().endUpdate();
        }
    }

    
//FLECHA DE ESTADO INICIAL
    

private void actualizarFlechaInicial(Object celdaEstado) {

    graph.getModel().beginUpdate();

    try {
        // Eliminar la flecha anterior si existe.
        if (marcadorEstadoInicial != null
                && graph.getModel().getParent(marcadorEstadoInicial) != null) {

            graph.removeCells(new Object[]{marcadorEstadoInicial});
        }

        // Crear una flecha visual afuera del círculo.
        marcadorEstadoInicial = graph.insertVertex(
                celdaEstado,
                null,
                "➜",
                -36, 32,
                48, 36,
                "shape=label;"
                + "fillColor=none;"
                + "strokeColor=none;"
                + "fontColor=#000000;"
                + "fontSize=30;"
                + "fontStyle=1;"
                + "align=center;"
                + "verticalAlign=middle;"
                + "movable=0;"
                + "resizable=0;"
                + "editable=0;"
                + "selectable=0;"
        );

    } finally {
        graph.getModel().endUpdate();
    }

    graphComponent.repaint();
}

    
    // ACTUALIZAR ESTILO DE LOS ESTADOS

private void actualizarEstiloEstados() {

    graph.getModel().beginUpdate();

    try {
        for (Map.Entry<Object, Estado> entrada
                : mapaEstados.entrySet()) {

            Object celda = entrada.getKey();
            Estado estado = entrada.getValue();

            if (estado.isAceptacion()) {

                // Estado final: doble círculo
                graph.setCellStyles(
                        mxConstants.STYLE_SHAPE,
                        mxConstants.SHAPE_DOUBLE_ELLIPSE,
                        new Object[]{celda}
                );

            } else {

                // Estado normal: un círculo
                graph.setCellStyles(
                        mxConstants.STYLE_SHAPE,
                        mxConstants.SHAPE_ELLIPSE,
                        new Object[]{celda}
                );
            }

            graph.setCellStyles(
                    mxConstants.STYLE_STROKEWIDTH,
                    "2",
                    new Object[]{celda}
            );
        }

    } finally {
        graph.getModel().endUpdate();
    }
}


    
    // ELIMINAR ELEMENTO SELECCIONADO
    

    private void eliminarElementoSeleccionado() {

        Object celda =
                graph.getSelectionCell();

        if (celda == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Primero selecciona un estado o transición."
            );

            return;
        }

        // ---------------------------------------------------------
        // INTENTAR ELIMINAR ESTADO
        // ---------------------------------------------------------

        Estado estado =
                mapaEstados.get(celda);

        if (estado != null) {

            int respuesta =
                    JOptionPane.showConfirmDialog(
                            this,
                            "¿Eliminar el estado "
                            + estado.getNombre()
                            + "?",
                            "Eliminar estado",
                            JOptionPane.YES_NO_OPTION
                    );

            if (
                    respuesta
                    != JOptionPane.YES_OPTION
            ) {

                return;
            }

            mapaTransiciones.entrySet().removeIf(entrada -> {
                Transicion t = entrada.getValue();
                return t.getOrigen() == estado || t.getDestino() == estado;
            });

            controller.eliminarEstado(
                    estado
            );

            mapaEstados.remove(
                    celda
            );

            graph.removeCells(
                    new Object[]{
                        celda
                    }
            );

            actualizarEtiquetasEstados();

            actualizarEstiloEstados();
            actualizarCurvasTransiciones();

            return;
        }

        // ---------------------------------------------------------
        // INTENTAR ELIMINAR TRANSICIÓN
        // ---------------------------------------------------------

        Transicion transicion =
                mapaTransiciones.get(celda);

        if (transicion != null) {

            controller.eliminarTransicion(
                    transicion
            );

            mapaTransiciones.remove(
                    celda
            );

            graph.removeCells(
                    new Object[]{
                        celda
                    }
            );

            actualizarCurvasTransiciones();
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "No se pudo identificar el elemento."
        );
    }

    // =============================================================
    // SINCRONIZAR POSICIÓN DEL ESTADO
    // =============================================================

    private void sincronizarPosicionEstado() {

        Object celda =
                graph.getSelectionCell();

        if (celda == null) {
            return;
        }

        Estado estado =
                mapaEstados.get(celda);

        if (estado == null) {
            return;
        }

        mxGeometry geometria =
                graph.getModel()
                        .getGeometry(celda);

        if (geometria == null) {
            return;
        }

        estado.setX(
                (int) geometria.getX()
        );

        estado.setY(
                (int) geometria.getY()
        );
    }

    
    // INICIAR SIMULACIÓN
   

    private void iniciarSimulacion() {

        if (
                timerSimulacion != null
                && timerSimulacion.isRunning()
        ) {

            timerSimulacion.stop();
        }

       

        if (
                automata.getEstadoInicial()
                == null
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Primero debes establecer un estado inicial."
            );

            return;
        }

        cadenaSimulacion =
                campoCadena.getText();

        posicionSimulacion =
                0;

        estadoActualSimulacion =
                automata.getEstadoInicial();

        etiquetaResultado.setText(
                "Resultado: Simulando..."
        );

        seleccionarEstadoVisual(
                estadoActualSimulacion
        );

        // ---------------------------------------------------------
        // CADENA VACÍA
        // ---------------------------------------------------------

        if (cadenaSimulacion.isEmpty()) {

            finalizarSimulacion();

            return;
        }

        

        timerSimulacion =
                new Timer(
                        800,
                        e ->
                                ejecutarPasoSimulacion()
                );

        timerSimulacion.start();
    }

    
    // EJECUTAR PASO DE SIMULACIÓN
    

    private void ejecutarPasoSimulacion() {

        if (
                posicionSimulacion
                >= cadenaSimulacion.length()
        ) {

            finalizarSimulacion();

            return;
        }

        char caracter =
                cadenaSimulacion.charAt(
                        posicionSimulacion
                );

        String simbolo =
                String.valueOf(
                        caracter
                );

        Estado siguiente =
                buscarSiguienteEstado(
                        estadoActualSimulacion,
                        simbolo
                );

       
        // NO EXISTE TRANSICIÓN
        

        if (siguiente == null) {

            timerSimulacion.stop();

            etiquetaResultado.setText(
                    "Resultado: RECHAZADA"
            );

            JOptionPane.showMessageDialog(
                    this,
                    "La cadena fue rechazada.\n\n"
                    + "No existe una transición desde "
                    + estadoActualSimulacion.getNombre()
                    + " con el símbolo '"
                    + simbolo
                    + "'."
            );

            return;
        }

        
        // AVANZAR AL SIGUIENTE ESTADO
       

        Estado origenPaso = estadoActualSimulacion;

        estadoActualSimulacion = siguiente;

        posicionSimulacion++;

        seleccionarPasoVisual(
                origenPaso,
                siguiente,
                simbolo
        );

        // ---------------------------------------------------------
        // FINALIZAR SI YA SE PROCESÓ TODA LA CADENA
        // ---------------------------------------------------------

        if (
                posicionSimulacion
                >= cadenaSimulacion.length()
        ) {

            finalizarSimulacion();
        }
    }

    // BUSCAR SIGUIENTE ESTADO

    private Estado buscarSiguienteEstado(
            Estado actual,
            String simbolo) {

        for (
                Transicion transicion
                : automata.getTransiciones()
        ) {

            if (
                    transicion.getOrigen()
                    == actual
                    &&
                    transicion.getSimbolo()
                            .equals(simbolo)
            ) {

                return transicion.getDestino();
            }
        }

        return null;
    }

    // FINALIZAR SIMULACIÓN

    private void finalizarSimulacion() {

        if (
                timerSimulacion != null
                && timerSimulacion.isRunning()
        ) {

            timerSimulacion.stop();
        }

        if (
                automata
                        .getEstadosAceptacion()
                        .contains(
                                estadoActualSimulacion
                        )
        ) {

            etiquetaResultado.setText(
                    "Resultado: ACEPTADA"
            );

        } else {

            etiquetaResultado.setText(
                    "Resultado: RECHAZADA"
            );
        }
    }

    // SELECCIONAR ESTADO VISUALMENTE

    private void seleccionarEstadoVisual(
            Estado estado) {

        for (
                Map.Entry<Object, Estado> entrada
                : mapaEstados.entrySet()
        ) {

            if (
                    entrada.getValue()
                    == estado
            ) {

                graph.setSelectionCell(
                        entrada.getKey()
                );

                return;
            }
        }
    }

    // LIMPIAR MODO TRANSICIÓN

    private void limpiarModoTransicion() {

        modoCrearTransicion =
                false;

        estadoOrigenTransicion =
                null;

        celdaOrigenTransicion =
                null;

        graphComponent.setCursor(
                new Cursor(
                        Cursor.DEFAULT_CURSOR
                )
        );
    }
    
    // RENOMBRAR ESTADOS
    
    private void renombrarEstadoSeleccionado() {

        Object celda = graph.getSelectionCell();

        Estado estado = mapaEstados.get(celda);

        if (estado == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primero selecciona un estado."
            );
            return;
        }

        String nuevoNombre = JOptionPane.showInputDialog(
                this,
                "Escribe el nuevo nombre del estado:",
                estado.getNombre()
        );

        if (nuevoNombre == null) {
            return;
        }

        nuevoNombre = nuevoNombre.trim();

        if (nuevoNombre.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "El nombre no puede estar vacío."
            );
            return;
        }

        // Evitar nombres repetidos
        for (Estado otro : automata.getEstados()) {

            if (otro != estado
                    && otro.getNombre().equals(nuevoNombre)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Ya existe un estado con ese nombre."
                );
                return;
            }
        }

        // Actualizar el modelo
        estado.setNombre(nuevoNombre);

        // Actualizar el lienzo
        actualizarEtiquetasEstados();

        JOptionPane.showMessageDialog(
                this,
                "Estado renombrado correctamente."
        );
    }
    
    
    //EDITAR TRANSICION
    
    private void editarTransicionSeleccionada() {

        Object celda = graph.getSelectionCell();
        Transicion transicion = mapaTransiciones.get(celda);

        if (transicion == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primero selecciona una transición."
            );
            return;
        }

        String nuevoSimbolo = JOptionPane.showInputDialog(
                this,
                "Nuevo símbolo de la transición:",
                transicion.getSimbolo()
        );

        if (nuevoSimbolo == null) {
            return;
        }

        nuevoSimbolo = nuevoSimbolo.trim();

        if (nuevoSimbolo.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "El símbolo no puede estar vacío."
            );
            return;
        }

        // Evitar transiciones duplicadas en un AFD.
        for (Transicion otra : automata.getTransiciones()) {
            if (otra != transicion
                    && otra.getOrigen() == transicion.getOrigen()
                    && otra.getSimbolo().equals(nuevoSimbolo)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Ya existe una transición con ese símbolo "
                        + "desde el mismo estado."
                );
                return;
            }
        }

        // Actualizar los datos.
        transicion.setSimbolo(nuevoSimbolo);
        automata.agregarSimbolo(nuevoSimbolo);

        // Actualizar la etiqueta visual.
        graph.getModel().beginUpdate();
        try {
            graph.getModel().setValue(celda, nuevoSimbolo);
        } finally {
            graph.getModel().endUpdate();
        }

        JOptionPane.showMessageDialog(
                this,
                "Transición actualizada correctamente."
        );
    }
    
    
    //REINICIAR SIMULACIÓN 
    
    private void reiniciarSimulacion() {

        // Detener cualquier animación en ejecución
        if (timerSimulacion != null) {
            timerSimulacion.stop();
            timerSimulacion = null;
        }

        // Reiniciar datos de la simulación
        cadenaSimulacion = "";
        posicionSimulacion = 0;
        estadoActualSimulacion = null;

        // Limpiar el campo de entrada
        campoCadena.setText("");

        // Reiniciar resultado
        etiquetaResultado.setText("Resultado: ---");

        // Quitar selección visual de los estados
        graph.clearSelection();

        // Actualizar lienzo
        graphComponent.repaint();
        
        restaurarColoresSimulacion();
        
        graphComponent.repaint();
        
        
    }

    //SEPARAR TRANSICIONES
    
    private void actualizarCurvasTransiciones() {
        Object[] aristas = graph.getChildEdges(graph.getDefaultParent());

        graph.getModel().beginUpdate();
        try {
            for (Object arista : aristas) {
                Object origen = graph.getModel().getTerminal(arista, true);
                Object destino = graph.getModel().getTerminal(arista, false);

                if (origen == null || destino == null || origen == destino) {
                    continue;
                }

                boolean tieneOpuesta = false;
                for (Object otra : aristas) {
                    if (otra != arista
                            && graph.getModel().getTerminal(otra, true) == destino
                            && graph.getModel().getTerminal(otra, false) == origen) {
                        tieneOpuesta = true;
                        break;
                    }
                }

                mxGeometry anterior = graph.getModel().getGeometry(arista);
                if (anterior == null) continue;
                mxGeometry nueva = (mxGeometry) anterior.clone();

                if (tieneOpuesta) {
                    mxGeometry g1 = graph.getModel().getGeometry(origen);
                    mxGeometry g2 = graph.getModel().getGeometry(destino);
                    if (g1 == null || g2 == null) continue;

                    double x1 = g1.getX() + g1.getWidth() / 2;
                    double y1 = g1.getY() + g1.getHeight() / 2;
                    double x2 = g2.getX() + g2.getWidth() / 2;
                    double y2 = g2.getY() + g2.getHeight() / 2;
                    double dx = x2 - x1;
                    double dy = y2 - y1;
                    double distancia = Math.hypot(dx, dy);
                    if (distancia < 1) continue;

                    double separacion = 28;
                    double puntoX = (x1 + x2) / 2
                            - dy / distancia * separacion;
                    double puntoY = (y1 + y2) / 2
                            + dx / distancia * separacion;

                    java.util.List<mxPoint> puntos = new java.util.ArrayList<>();
                    puntos.add(new mxPoint(puntoX, puntoY));
                    nueva.setPoints(puntos);
                } else {
                    // Al eliminar la flecha opuesta, volver a una línea normal.
                    nueva.setPoints(null);
                }

                graph.getModel().setGeometry(arista, nueva);
                graph.setCellStyles(
                        mxConstants.STYLE_SHAPE,
                        tieneOpuesta ? mxConstants.SHAPE_CURVE
                                : mxConstants.SHAPE_CONNECTOR,
                        new Object[]{arista}
                );
                graph.setCellStyles(
                        mxConstants.STYLE_MOVABLE,
                        "0",
                        new Object[]{arista}
                );
            }
        } finally {
            graph.getModel().endUpdate();
        }
    }

    //Recorrido
    
    
    private void seleccionarPasoVisual(
            Estado origen,
            Estado destino,
            String simbolo) {

        // Restaurar los colores del paso anterior
        restaurarColoresSimulacion();

        Object celdaDestino = null;
        Object celdaTransicion = null;

        // Buscar el estado de destino
        for (Map.Entry<Object, Estado> entrada
                : mapaEstados.entrySet()) {

            if (entrada.getValue() == destino) {
                celdaDestino = entrada.getKey();
                break;
            }
        }

        // Buscar la transición utilizada
        for (Map.Entry<Object, Transicion> entrada
                : mapaTransiciones.entrySet()) {

            Transicion t = entrada.getValue();

            if (t.getOrigen() == origen
                    && t.getDestino() == destino
                    && t.getSimbolo().equals(simbolo)) {

                celdaTransicion = entrada.getKey();
                break;
            }
        }

        graph.getModel().beginUpdate();

        try {
            // COLOR VERDE PARA EL ESTADO ACTUAL
            if (celdaDestino != null) {

                estilosResaltados.put(
                        celdaDestino,
                        graph.getModel().getStyle(celdaDestino)
                );

                graph.setCellStyles(
                        mxConstants.STYLE_FILLCOLOR,
                        "#A5D6A7",
                        new Object[]{celdaDestino}
                );

                graph.setCellStyles(
                        mxConstants.STYLE_STROKECOLOR,
                        "#1B7F39",
                        new Object[]{celdaDestino}
                );

                graph.setCellStyles(
                        mxConstants.STYLE_STROKEWIDTH,
                        "4",
                        new Object[]{celdaDestino}
                );
            }

            // COLOR NARANJA PARA LA TRANSICIÓN
            if (celdaTransicion != null) {

                estilosResaltados.put(
                        celdaTransicion,
                        graph.getModel().getStyle(celdaTransicion)
                );

                graph.setCellStyles(
                        mxConstants.STYLE_STROKECOLOR,
                        "#FF8C00",
                        new Object[]{celdaTransicion}
                );

                graph.setCellStyles(
                        mxConstants.STYLE_STROKEWIDTH,
                        "4",
                        new Object[]{celdaTransicion}
                );
            }

        } finally {
            graph.getModel().endUpdate();
        }

        graph.clearSelection();
        graphComponent.repaint();
    }

    
    // COLOR EN SIMULACION
    private void restaurarColoresSimulacion() {

        graph.getModel().beginUpdate();

        try {
            for (Map.Entry<Object, String> entrada
                    : estilosResaltados.entrySet()) {

                Object celda = entrada.getKey();

                if (graph.getModel().getParent(celda) != null) {
                    graph.getModel().setStyle(
                            celda, entrada.getValue()
                    );
                }
            }

            estilosResaltados.clear();

        } finally {
            graph.getModel().endUpdate();
        }
    }

    
    private String obtenerNombreEstadoDisponible() {

        int numero = 0;

        while (true) {

            String nombre = "q" + numero;
            boolean existe = false;

            for (Estado estado : automata.getEstados()) {

                if (estado.getNombre().equals(nombre)) {
                    existe = true;
                    break;
                }
            }

            if (!existe) {
                return nombre;
            }

            numero++;
        }
    }
    
    
    //MOSTRAR MENSAJES
    private void mostrarAviso(String mensaje) {

        etiquetaAviso.setText("  " + mensaje);

        temporizadorAviso.restart();
    }

}