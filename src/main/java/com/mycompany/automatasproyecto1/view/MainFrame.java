package com.mycompany.automatasproyecto1.view;

import com.mycompany.automatasproyecto1.conversion.ConversorRegex;
import com.mycompany.automatasproyecto1.conversion.ResultadoConversion;
import com.mycompany.automatasproyecto1.conversion.VerificadorEquivalencia;
import com.mycompany.automatasproyecto1.simulation.SimuladorAFD;
import com.mycompany.automatasproyecto1.simulation.ResultadoSimulacion;
import com.mycompany.automatasproyecto1.simulation.PasoSimulacion;
import com.mycompany.automatasproyecto1.io.AutomataJson;

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

import javax.swing.border.*;
import javax.swing.BorderFactory;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.HashMap;
import java.util.Map;

import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

import java.io.IOException;

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
    private ResultadoSimulacion resultadoSimulacionActual;
    private int indicePasoSimulacion = 0;

    private JTextField campoCadena;
    private JLabel etiquetaResultado;
    
    private final Map<Object, String> estilosResaltados = new HashMap<>();
    
    private final JLabel etiquetaAviso = new JLabel(" ");

    private final javax.swing.Timer temporizadorAviso =
        new javax.swing.Timer(2800, e -> {
            etiquetaAviso.setText(" ");
        });
    
    private final Color COLOR_FONDO = new Color(245, 240, 255);
    private final Color COLOR_PANEL = new Color(255, 255, 255);
    private final Color COLOR_LILA = new Color(212, 190, 255);
    private final Color COLOR_LILA_OSCURO = new Color(140, 110, 210);
    private final Color COLOR_BOTON = new Color(196, 173, 255);
    private final Color COLOR_BOTON_TEXTO = new Color(55, 35, 110);
    private final Color COLOR_BORDE = new Color(220, 210, 245);
    private final Color COLOR_MENSAJE = new Color(88, 70, 150);
    
    private final JTextArea areaMensajes = 
            new JTextArea();
    private final JScrollPane scrollMensajes =
            new JScrollPane(areaMensajes);

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
        
        // CONFIGURACIÓN DEL PANEL DE MENSAJES

        areaMensajes.setEditable(false);
        areaMensajes.setLineWrap(true);
        areaMensajes.setWrapStyleWord(true);

        areaMensajes.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        areaMensajes.setForeground(
                new Color(88, 70, 150)
        );

        areaMensajes.setBackground(
                new Color(252, 249, 255)
        );

        areaMensajes.setBorder(
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        );

        scrollMensajes.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(
                                new Color(196, 173, 255), 2, true
                        ),
                        "✦ Mensajes del sistema"
                )
        );

        scrollMensajes.setPreferredSize(
                new Dimension(280, 140)
        );

        areaMensajes.setText("✓ Editor listo para comenzar.\n");


        JPanel encabezado = new JPanel(new BorderLayout());

        encabezado.setBackground(new Color(62, 43, 112));

        encabezado.setBorder(
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        );

        JLabel titulo = new JLabel(
                "AUTOMATAS"
        );

        titulo.setFont(
                new Font("Segoe UI", Font.BOLD, 19)
        );

        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel(
                "Construccion, simulacion y conversion a Regex"
        );

        subtitulo.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        subtitulo.setForeground(
                new Color(224, 210, 255)
        );

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 4));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);

        encabezado.add(textos, BorderLayout.WEST);

        add(encabezado, BorderLayout.NORTH);


        
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
                new GridLayout(9, 1, 5, 5)
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
        
        JButton btnImportar =
                new JButton("Importar Autómata");

        JButton btnExportar =
                new JButton("Exportar Autómata");

        panelHerramientas.add(btnAgregarEstado);
        panelHerramientas.add(btnTransicion);
        panelHerramientas.add(btnInicial);
        panelHerramientas.add(btnAceptacion);
        panelHerramientas.add(btnEliminar);
        panelHerramientas.add(btnRenombrar);
        panelHerramientas.add(btnEditarTransicion);
        panelHerramientas.add(btnImportar);
        panelHerramientas.add(btnExportar);
        
        
        estilizarBotones(panelHerramientas);

        // Destacar Importar y Exportar
        btnImportar.setBackground(new java.awt.Color(197, 215, 255));
        btnExportar.setBackground(new java.awt.Color(255, 205, 230));

        panelHerramientas.setBackground(
                new java.awt.Color(250, 247, 255)
        );

        
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
        
        // Ocultar los botones + y - de los estados
        graphComponent.setFoldingEnabled(false);
        
        graphComponent.getGraphControl().setBackground(
                new Color(252, 250, 255)
        );

        graphComponent.getViewport().setBackground(
                new Color(252, 250, 255)
        );

        graphComponent.setPageVisible(false);
        
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


        // LIENZO CON PANEL DE MENSAJES SUPERPUESTO

        JLayeredPane capaLienzo = new JLayeredPane() {

            @Override
            public void doLayout() {

                graphComponent.setBounds(
                        0, 0, getWidth(), getHeight()
                );

                int ancho = Math.min(280, getWidth() - 24);

                scrollMensajes.setBounds(
                        Math.max(8, getWidth() - ancho - 12),
                        12,
                        ancho,
                        140
                );
            }
        };

        capaLienzo.add(
                graphComponent,
                JLayeredPane.DEFAULT_LAYER
        );

        capaLienzo.add(
                scrollMensajes,
                JLayeredPane.PALETTE_LAYER
        );

        panelLienzo.add(
                capaLienzo,
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
        
        //BOTONES DE IMPORTAR Y EXPORTAR
        
        btnExportar.addActionListener(e ->
                exportarAutomataAArchivo()
        );


        btnImportar.addActionListener(e ->
                importarAutomataDesdeArchivo()
        );
        
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
        
        
        estilizarBotones(panelSimulacion);

        btnSimular.setBackground(
                new java.awt.Color(126, 95, 205)
        );
        btnSimular.setForeground(java.awt.Color.WHITE);

        btnConvertirRegex.setBackground(
                new java.awt.Color(177, 129, 225)
        );
        btnConvertirRegex.setForeground(java.awt.Color.WHITE);

        panelSimulacion.setBackground(
                new java.awt.Color(250, 247, 255)
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

            
            ventana.configurarAccionConversion(() -> {

                try {
                    ResultadoConversion resultado =
                            new ConversorRegex().convertirConPasos(automata);

                    ventana.cargarResultadoConversion(resultado, graph);

                    ventana.configurarVerificador(cadena -> {

                        try {
                            VerificadorEquivalencia verificador =
                                    new VerificadorEquivalencia();

                            boolean aceptadaAFD =
                                    verificador.aceptaAFD(automata, cadena);

                            boolean aceptadaRegex =
                                    verificador.aceptaRegex(
                                            resultado.getExpresion(), cadena
                                    );

                            ventana.mostrarResultadoVerificacion(
                                    aceptadaAFD, aceptadaRegex
                            );

                        } catch (IllegalArgumentException ex) {
                            JOptionPane.showMessageDialog(
                                    ventana,
                                    ex.getMessage(),
                                    "Error de verificación",
                                    JOptionPane.WARNING_MESSAGE
                            );
                        }
                    });

                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(
                            ventana,
                            ex.getMessage(),
                            "Error de conversión",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            });


            ventana.setVisible(true);
        });

        
        
        //BOTÓN REINICIAR
        
        btnReiniciar.addActionListener(e -> {
            reiniciarSimulacion();
        });
        


        // ESTILO GENERAL DE LA VENTANA

        getContentPane().setBackground(
                new Color(245, 240, 255)
        );

        // PANEL DE HERRAMIENTAS
        panelHerramientas.setBackground(
                new Color(250, 247, 255)
        );

        panelHerramientas.setPreferredSize(
                new Dimension(205, 0)
        );

        panelHerramientas.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(
                                        new Color(196, 173, 255),
                                        2, true
                                ),
                                "Herramientas",
                                0, 0,
                                new Font("Segoe UI", Font.BOLD, 13),
                                new Color(88, 60, 150)
                        ),
                        BorderFactory.createEmptyBorder(
                                10, 10, 10, 10
                        )
                )
        );

        // PANEL DEL LIENZO
        panelLienzo.setBackground(Color.WHITE);

        panelLienzo.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(
                                new Color(196, 173, 255),
                                2, true
                        ),
                        "Lienzo del Autómata",
                        0, 0,
                        new Font("Segoe UI", Font.BOLD, 13),
                        new Color(88, 60, 150)
                )
        );

        // PANEL DE SIMULACIÓN
        panelSimulacion.setBackground(
                new Color(250, 247, 255)
        );

        panelSimulacion.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createLineBorder(
                                        new Color(196, 173, 255),
                                        2, true
                                ),
                                "Simulación",
                                0, 0,
                                new Font("Segoe UI", Font.BOLD, 13),
                                new Color(88, 60, 150)
                        ),
                        BorderFactory.createEmptyBorder(
                                6, 10, 8, 10
                        )
                )
        );



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
            
            
            // Configurar los bucles del mismo estado
            if (celdaOrigenTransicion == celdaDestino) {

                graph.setCellStyles(
                        mxConstants.STYLE_ROUNDED,
                        "1",
                        new Object[]{celdaTransicion}
                );

                graph.setCellStyles(
                        mxConstants.STYLE_LOOP,
                        "loopEdgeStyle",
                        new Object[]{celdaTransicion}
                );

                graph.setCellStyles(
                        mxConstants.STYLE_DIRECTION,
                        mxConstants.DIRECTION_NORTH,
                        new Object[]{celdaTransicion}
                );

                graph.setCellStyles(
                        mxConstants.STYLE_SEGMENT,
                        "30",
                        new Object[]{celdaTransicion}
                );
                
                // Dibujar el bucle como curva
                graph.setCellStyles(
                        mxConstants.STYLE_SHAPE,
                        mxConstants.SHAPE_CURVE,
                        new Object[]{celdaTransicion}
                );
            }

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

        mostrarAviso(estado.getNombre() + " ahora es el estado inicial.");
    
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

        if (timerSimulacion != null) {
            timerSimulacion.stop();
        }

        restaurarColoresSimulacion();

        if (automata.getEstadoInicial() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primero debes establecer un estado inicial."
            );
            return;
        }

        SimuladorAFD simulador = new SimuladorAFD(automata);

        resultadoSimulacionActual =
                simulador.simular(campoCadena.getText());

        indicePasoSimulacion = 1;

        etiquetaResultado.setText("Resultado: Simulando...");

        seleccionarEstadoVisual(automata.getEstadoInicial());

        if (resultadoSimulacionActual.getPasos().size() <= 1) {
            finalizarSimulacion();
            return;
        }

        timerSimulacion = new Timer(
                800,
                e -> ejecutarPasoSimulacion()
        );

        timerSimulacion.start();
    }

    
    // EJECUTAR PASO DE SIMULACIÓN
    


    private void ejecutarPasoSimulacion() {

        if (resultadoSimulacionActual == null) {
            return;
        }

        if (indicePasoSimulacion >=
                resultadoSimulacionActual.getPasos().size()) {
            finalizarSimulacion();
            return;
        }

        PasoSimulacion paso =
                resultadoSimulacionActual.getPasos()
                        .get(indicePasoSimulacion);

        indicePasoSimulacion++;

        if (paso.tieneError()) {
            finalizarSimulacion();

            JOptionPane.showMessageDialog(
                    this,
                    paso.getMensaje(),
                    "Validación de cadena",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        seleccionarPasoVisual(
                paso.getEstadoOrigen(),
                paso.getEstadoDestino(),
                paso.getSimboloLeido()
        );

        if (indicePasoSimulacion >=
                resultadoSimulacionActual.getPasos().size()) {
            finalizarSimulacion();
        }
    }

    // FINALIZAR SIMULACIÓN

    private void finalizarSimulacion() {

        if (timerSimulacion != null) {
            timerSimulacion.stop();
        }

        if (resultadoSimulacionActual == null) {
            return;
        }

        etiquetaResultado.setText(
                "Resultado: "
                + resultadoSimulacionActual.getEstadoResultado()
        );
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

        if (timerSimulacion != null) {
            timerSimulacion.stop();
            timerSimulacion = null;
        }

        resultadoSimulacionActual = null;
        indicePasoSimulacion = 0;

        campoCadena.setText("");
        etiquetaResultado.setText("Resultado: ---");

        restaurarColoresSimulacion();
        graph.clearSelection();
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
                        "#BFDBFE",
                        new Object[]{celdaDestino}
                );

                graph.setCellStyles(
                        mxConstants.STYLE_STROKECOLOR,
                        "#2563EB",
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
                        "#8B5CF6",
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
    

    private void exportarAutomataAArchivo() {

        if (automata.getEstados().isEmpty()) {
            mostrarAviso("No hay estados para exportar.");
            return;
        }

        JFileChooser selector = new JFileChooser();

        selector.setFileFilter(
                new FileNameExtensionFilter(
                        "Autómatas JSON (*.json)", "json"
                )
        );

        selector.setSelectedFile(
                new java.io.File("mi_automata.json")
        );

        if (selector.showSaveDialog(this)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File archivo = selector.getSelectedFile();

        if (!archivo.getName().toLowerCase()
                .endsWith(".json")) {

            archivo = new java.io.File(
                    archivo.getParentFile(),
                    archivo.getName() + ".json"
            );
        }

        if (archivo.exists()) {
            int respuesta = JOptionPane.showConfirmDialog(
                    this,
                    "El archivo ya existe. ¿Deseas reemplazarlo?",
                    "Confirmar exportación",
                    JOptionPane.YES_NO_OPTION
            );

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            // Obtener posiciones actuales del dibujo
            Map<Estado, Point> posiciones = new HashMap<>();

            for (Map.Entry<Object, Estado> entrada
                    : mapaEstados.entrySet()) {

                mxGeometry geometria =
                        graph.getModel().getGeometry(
                                entrada.getKey()
                        );

                if (geometria != null) {
                    posiciones.put(
                            entrada.getValue(),
                            new Point(
                                    (int) Math.round(geometria.getX()),
                                    (int) Math.round(geometria.getY())
                            )
                    );
                }
            }

            String json = AutomataJson.exportar(
                    automata, posiciones
            );

            Files.writeString(
                    archivo.toPath(),
                    json,
                    StandardCharsets.UTF_8
            );

            mostrarAviso("Autómata exportado correctamente.");

        } catch (IOException | IllegalArgumentException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error al exportar: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    
    //MOSTRAR MENSAJES
   private void mostrarAviso(String mensaje) {

        areaMensajes.append("• " + mensaje + "\n");

        areaMensajes.setCaretPosition(
                areaMensajes.getDocument().getLength()
        );
    }
    
    
    //Importar 


    private void importarAutomataDesdeArchivo() {

        JFileChooser selector = new JFileChooser();

        selector.setFileFilter(
                new FileNameExtensionFilter(
                        "Autómatas JSON (*.json)", "json"
                )
        );

        if (selector.showOpenDialog(this)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            java.nio.file.Path archivo =
                    selector.getSelectedFile().toPath();

            if (Files.size(archivo) > 2_000_000) {
                throw new IllegalArgumentException(
                        "El archivo supera el límite de 2 MB."
                );
            }

            String json = Files.readString(
                    archivo, StandardCharsets.UTF_8
            );

            // Validar antes de reemplazar el autómata actual
            AutomataJson.Datos datos =
                    AutomataJson.importar(json);

            if (!automata.getEstados().isEmpty()) {

                int respuesta = JOptionPane.showConfirmDialog(
                        this,
                        "¿Deseas reemplazar el autómata actual?",
                        "Importar autómata",
                        JOptionPane.YES_NO_OPTION
                );

                if (respuesta != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            cargarAutomataImportado(datos);

            mostrarAviso(
                    "Autómata importado correctamente."
            );

        } catch (IOException | IllegalArgumentException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error al importar: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    
    //RECONSTRUIR AUTOMATA
    
    
    private void cargarAutomataImportado(
            AutomataJson.Datos datos) {

        // Primero reconstruir el modelo completo
        AutomataController nuevoController =
                new AutomataController();

        Map<String, Estado> nuevosEstados =
                new java.util.LinkedHashMap<>();

        for (AutomataJson.EstadoDato dato : datos.estados) {

            Estado estado = nuevoController.crearEstado(
                    dato.nombre, dato.x, dato.y
            );

            nuevosEstados.put(dato.nombre, estado);

            if (dato.aceptacion) {
                nuevoController.establecerEstadoAceptacion(
                        estado
                );
            }
        }

        if (datos.inicial != null) {

            Estado inicial = nuevosEstados.get(datos.inicial);

            if (inicial == null) {
                throw new IllegalArgumentException(
                        "No se encontró el estado inicial."
                );
            }

            nuevoController.establecerEstadoInicial(inicial);
        }

        for (AutomataJson.TransicionDato dato
                : datos.transiciones) {

            Estado origen = nuevosEstados.get(dato.origen);
            Estado destino = nuevosEstados.get(dato.destino);

            if (origen == null || destino == null) {
                throw new IllegalArgumentException(
                        "Una transición contiene estados inexistentes."
                );
            }

            nuevoController.crearTransicion(
                    origen, destino, dato.simbolo
            );
        }

        // Detener cualquier simulación anterior
        reiniciarSimulacion();

        modoAgregarEstado = false;
        limpiarModoTransicion();
        marcadorEstadoInicial = null;
        estilosResaltados.clear();

        graph.getModel().beginUpdate();

        try {
            Object parent = graph.getDefaultParent();

            // Limpiar el dibujo anterior
            graph.removeCells(
                    graph.getChildCells(parent, true, true)
            );

            mapaEstados.clear();
            mapaTransiciones.clear();

            controller = nuevoController;
            automata = nuevoController.getAutomata();

            Map<Estado, Object> celdas =
                    new HashMap<>();

            // Reconstruir estados visuales
            for (AutomataJson.EstadoDato dato : datos.estados) {

                Estado estado = nuevosEstados.get(dato.nombre);

                Object celda = graph.insertVertex(
                        parent,
                        null,
                        estado.getNombre(),
                        dato.x,
                        dato.y,
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

                mapaEstados.put(celda, estado);
                celdas.put(estado, celda);
            }

            // Reconstruir las transiciones
            for (Transicion transicion
                    : automata.getTransiciones()) {

                Object celda = graph.insertEdge(
                        parent,
                        null,
                        transicion.getSimbolo(),
                        celdas.get(transicion.getOrigen()),
                        celdas.get(transicion.getDestino())
                );

                mxGeometry geometria =
                        graph.getModel().getGeometry(celda);

                if (geometria != null) {

                    mxGeometry nueva =
                            (mxGeometry) geometria.clone();

                    nueva.setOffset(
                            new mxPoint(0, -15)
                    );

                    graph.getModel().setGeometry(celda, nueva);
                }

                graph.setCellStyles(
                        mxConstants.STYLE_LABEL_BACKGROUNDCOLOR,
                        "#FFFFFF",
                        new Object[]{celda}
                );

                graph.setCellStyles(
                        mxConstants.STYLE_MOVABLE,
                        "0",
                        new Object[]{celda}
                );

                mapaTransiciones.put(celda, transicion);
            }

        } finally {
            graph.getModel().endUpdate();
        }

        // Recuperar estados finales
        actualizarEstiloEstados();

        // Recuperar la flecha del estado inicial
        if (automata.getEstadoInicial() != null) {

            for (Map.Entry<Object, Estado> entrada
                    : mapaEstados.entrySet()) {

                if (entrada.getValue()
                        == automata.getEstadoInicial()) {

                    actualizarFlechaInicial(
                            entrada.getKey()
                    );
                    break;
                }
            }
        }

        // Recuperar las curvas de las transiciones
        actualizarCurvasTransiciones();

        graph.clearSelection();
        graphComponent.refresh();
        graphComponent.repaint();
    }
    

    private void estilizarBotones(JPanel panel) {

        for (java.awt.Component componente :
                panel.getComponents()) {

            if (componente instanceof JButton) {

                JButton boton = (JButton) componente;

                boton.setFont(new java.awt.Font(
                        "Segoe UI",
                        java.awt.Font.BOLD,
                        12
                ));

                boton.setBackground(
                        new java.awt.Color(224, 210, 255)
                );

                boton.setForeground(
                        new java.awt.Color(65, 42, 115)
                );

                boton.setFocusPainted(false);
                boton.setContentAreaFilled(true);
                boton.setOpaque(true);

                boton.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(
                                        new java.awt.Color(
                                                189, 165, 235
                                        ),
                                        1,
                                        true
                                ),
                                BorderFactory.createEmptyBorder(
                                        8, 8, 8, 8
                                )
                        )
                );

                boton.setCursor(
                        new Cursor(Cursor.HAND_CURSOR)
                );
            }
        }
    }


}