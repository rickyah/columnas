package columnasJuego;

import javax.microedition.lcdui.*;

import basePackage.*;
import com.nokia.mid.ui.*;

public class CJuegoCanvas
    extends FullCanvas implements Runnable {

  /**
   * Pseudo-enumeraci�n para los estados del juego
   * */
  private class CGameStates {

    public static final short EnJuego = 0;

    public static final short BuscandoGemas = 2;

    public static final short DestruyendoGemas = 3;

    public static final short FinJuego = 4;

    public static final short Cargando = 5;

    public static final short GameOver = 6;

  }

  //Tama�o de la pantalla
  private int screenHeight;
  private int screenWidth;

  //BackBuffer y su objeto graphics asociado (DOUBLE BUFFERING)
  private Image backBuffer;
  private Graphics backBufferGraphics;

  //Fuente de la pantalla
  private Font fuentePuntuacion;

  //Estado actual del juego
  private short juegoState = -1;

  //C�lculo del tiempo en el juego
  private long logicLastTimeCall;
  private long frameLastTimeCall;
  private long frameElapsedTime;

  //tiempo para medir la caida del bloque
  private long timeDrop;

  //Constantes  /////////////////////////////////////////////////

  //Frecuencia de ejecuci�n de la logica (veces que se ejecuta por segundo)
  private final int LOGIC_TICK = 1000 / 7;

  //Punto de referencia del bloque next
  private final int BLOQUE_NEXT_X = 92;
  private final int BLOQUE_NEXT_Y = 64;

  //Punto referencia texto del marcador de puntuacion
  private final int TEXTO_MARCADOR_X = 120;
  private final int TEXTO_MARCADOR_Y = 28;

  //Punto de referencia del marcador de puntuaicon
  private final int MARCADOR_X = 75;
  private final int MARCADOR_Y = 21;

  //Tama�o alto texto "pausa"
  private static final short altoTextoPausa = 28;
  //Tama�o alto texto "game over"
  private static final short altoTextoGameOver = 53;

  //Puntuaci�n base de cada gema
  private final int GEM_SCORE_VALUE = 10;

  // Fin constantes ////////////////////////////////////////////

  //Imagen temporal para reestablecer el estado de la pantalla de juego
  //al salir de la pausa
  private Image imgTempTexto = null;
  private boolean imagenTextoDibujada = false;

  //Imagenes del tablero de juego
  private Image imgPantallaJuego, imgFondoPuntuacion, imgTextos, imgMenuPausa, imgMenuConfirmaSalir;

  //Informacion sobre las gemas
  private CGemas gemas;

  //Tablero
  CTablero tablero;

  //Bloques de piezas del jugador y next
  CBloquePieza bloqueJugador, bloqueNext;

  //C�lculo de puntuaci�n y dificultad
  private int dificultad = 0;
  private int score;
  private int scoreMultiplier;

  //Indica si la pantalla inicial del juego que no se actualizar� esta dibujada
  private boolean pantallaInicialDibujada = false;

  //Estado actual del juego
  private short state = CGameStates.Cargando;

  //Objeto con los posibles movimientos del juego
  private MovimientosJuego movimientos;

  //Referencia al MIDlet que lanzo este hilo
  private columnasMIDlet parentMIDlet;

  //Indica si el juego esta en pausa
  private boolean pausado = false;

  //Indica si el jugador intenta salir (pantalla de confirmaci�n)
  private boolean confirmaSalir = false;




  public CJuegoCanvas(columnasMIDlet pm) {

    //Guardamos el tama�o de la pantalla
    screenWidth = this.getWidth();
    screenHeight = this.getHeight();
    
    //Para moviles que no pertenezcan a la serie40
    if (screenHeight > 128)
        screenHeight = 128;
    if (screenWidth > 128)
        screenWidth = 128;

    //Guardamos el MIDlet padre
    parentMIDlet = pm;

  }

  /**
   * Inicializa todos los recursos necesarios para el juego
   */
  public void Init() {
    //Creamos una fuente para la puntuaci�n
    fuentePuntuacion = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN,
                                    Font.SIZE_SMALL);

    //Inicializamos los posibles movimientos
    movimientos = new MovimientosJuego();

    //Creamos la imagen para el backbuffer y obtenemos su objeto gr�fico
    try {
      backBuffer = Image.createImage(screenHeight, screenWidth);
      backBufferGraphics = backBuffer.getGraphics();
    }
    catch (Exception e) {
    }

    //Cargamos las imagenes
    try {

      //imagen del tablero
      imgPantallaJuego = Image.createImage("/res/pantalla.png");

      //imagen para borrar la puntuacion
      imgFondoPuntuacion = Image.createImage("/res/fondo_punt.png");

      //Textos de pausa y game over
      imgTextos = Image.createImage("/res/textos.png");

      //botones menu pausa
      imgMenuPausa = Image.createImage("/res/menu_pausa_juego.png");

      //menu confirmacion de salid
      imgMenuConfirmaSalir = Image.createImage("/res/menu_confirma_salir.png");

    }
    catch (Exception e) {
    }

    //Establecemos la fuente de la puntuacion y su color
    backBufferGraphics.setFont(fuentePuntuacion);
    backBufferGraphics.setColor(0, 0, 0); //color negro

    //Creamos las gemas
    gemas = new CGemas();

    //Creamos el tablero
    tablero = new CTablero(gemas);

    //Creamos los bloques de piezas
    bloqueJugador = new CBloquePieza(gemas);
    bloqueJugador.initPlayerPos();
    bloqueNext = new CBloquePieza(gemas);

    //inicializamos puntuacion
    score = 0;
    scoreMultiplier = 1;

    //Pantalla inicial no mostrada a�n
    pantallaInicialDibujada = false;

    //pantalla de pausa no disponible
    imagenTextoDibujada = false;

    //Comienza el juego
    state = CGameStates.EnJuego;
    pausado = confirmaSalir = false;

  }

  /**
   * Libera todos los recursos
   */
  public void Destroy() {
    //Estado cargando (por si sucede un evento paint)
    state = CGameStates.Cargando;

    //Liberamos la fuente
    fuentePuntuacion = null;

    //Liberar im�genes
    imgPantallaJuego = null;
    imgFondoPuntuacion = null;
    imgMenuPausa = null;
    imgMenuPausa = null;
    imgTextos = null;

    //Liberar objetos
    gemas = null;
    tablero = null;
    bloqueJugador = null;
    bloqueNext = null;

    //Liberamos el backbuffer
    backBuffer = null;
    backBufferGraphics = null;

    //Liberamos buffer temporal para la pantalla pausa.
    imgTempTexto = null;

    //Llamamos al recolector de basura
    System.gc();
  }

  ///////////////////////////////////////////////////////////////////
  //Gets y Sets
  ///////////////////////////////////////////////////////////////////

  public void setDificultad(int d) {
    dificultad = d;
  }

  /**
   * Indica si el juego esta en ejecuci�n
   * @return boolean
   */
  public boolean isRunning(){
    if ( pausado || (state == CGameStates.Cargando)  )
      return false;
    else return true;
  }

  /**
   * Indica si el juego est� en pausa
   * @return boolean
   */
  public boolean isPaused(){
    return pausado;
  }

  /**
   * Pausa el juego
   */
  public void Pause(){
    pausado = true;
  }

  /**
   * Retoma la ejecuci�n del juego
   */
  public void Resume(){
    pausado = false;
  }


  ///////////////////////////////////////////////////////////////////
  //Eventos externos
  ///////////////////////////////////////////////////////////////////
  protected void hideNotify()  {
    pausado = true;
  }

  ///////////////////////////////////////////////////////////////////
  //Funciones para la l�gica
  ///////////////////////////////////////////////////////////////////

  /**
   * Calcula el tiempo que tarda el bloque del jugador en caer
   * @param elapsedTime tiempo transcurrido entre dos frames
   * @return boolean true si el bloque debe caer, false si el tiempo transcurrido no es suficiente para que caiga el bloque
   */
  public boolean GestionaCaidaBloqueTiempo(long elapsedTime) {
    //Sumamos el tiempo transcurrido al total
    timeDrop += elapsedTime;

    //1000 es el tiempo en milisegundos
    if (timeDrop > (1000 - ( (dificultad -1)  * 100))) {
      timeDrop = 0;

      //si no podemos mover el bloque una casilla hacia abajo (bien por que sea el
      //l�mite del tablero o bien porque lo bloquea una gema, lo fijamos
      if ( (bloqueJugador.pos.y + 1 > (tablero.ALTO_TABLERO - 1)) ||
          (tablero.getColorCasilla(bloqueJugador.pos.x, bloqueJugador.pos.y + 1) != CGemas.NONE)) {

        //fijamos los datos del bloque en el tablero
        tablero.FijaBloqueJugador(bloqueJugador);

        //inicializamos el nuevo bloque del jugador con la informacion del bloque next
        bloqueJugador.copyData(bloqueNext);
        bloqueJugador.initPlayerPos();

        //Generamos un nuevo bloque next
        bloqueNext.GenerarGemas();

        //Indicamos que ha caido un bloque
        return true;

      }
      //si podemos bajar el bloque entonces actualizamos su posici�n
      else {
        if (!movimientos.caida)
          bloqueJugador.MoverAbajo();
        return false;
      }


    }

    return false;
  }

  /**
   * Valida y ejecuta los movimientos del bloque del jugador
   * @return boolean si el bloque del jugador qued� fijado en el tablero
   */
  public boolean GestionaMovimientoBloque() {

    boolean haCaidoBloque = false;

    //comprobamos si el bloque cae
    if (movimientos.caida) {

      //si no podemos mover el bloque una casilla hacia abajo (bien por que sea el
      //l�mite del tablero o bien porque lo bloquea una gema, lo fijamos
      if ( (bloqueJugador.pos.y + 1 > (tablero.ALTO_TABLERO - 1)) ||
          (tablero.getColorCasilla(bloqueJugador.pos.x, bloqueJugador.pos.y + 1) !=
           CGemas.NONE)) {

        //fijamos los datos del bloque en el tablero
        tablero.FijaBloqueJugador(bloqueJugador);

        //inicializamos el nuevo bloque del jugador con la informacion del bloque next
        bloqueJugador.copyData(bloqueNext);
        bloqueJugador.initPlayerPos();

        //Generamos un nuevo bloque next
        bloqueNext.GenerarGemas();

        //Indicamos que ha caido un bloque
        haCaidoBloque = true;

      }
      //si podemos bajar el bloque actualizamos su posici�n
      else {
        bloqueJugador.MoverAbajo();
      }

 //     if (movimientos.teclaLiberada)
 //         movimientos.ResetMovimientos();

    }

    //Cambiamos la posici�n de las gemas
    if (movimientos.moverGemas) {
      bloqueJugador.MoverGemas();
      movimientos.moverGemas = false;
    }

    //Movimientos laterales
    if (movimientos.derecha) {
      //si el movimiento es v�lido
      if ( (bloqueJugador.pos.x < (tablero.ANCHO_TABLERO - 1)) &&
          (tablero.getColorCasilla(bloqueJugador.pos.x + 1, bloqueJugador.pos.y) ==
           CGemas.NONE)) {
        //movemos el bloque del jugador
        bloqueJugador.MoverDerecha();

     //   if (movimientos.teclaLiberada)
     //     movimientos.ResetMovimientos();
      }
    }

    if (movimientos.izquierda) {
      //si el movimiento es v�lido
      if ( (bloqueJugador.pos.x > 0) &&
          (tablero.getColorCasilla(bloqueJugador.pos.x - 1, bloqueJugador.pos.y) ==
           CGemas.NONE)) {
        //movemos el bloque del jugador
        bloqueJugador.MoverIzquierda();

  //      if (movimientos.teclaLiberada)
  //        movimientos.ResetMovimientos();
      }
    }

    //retornamos si ha caido o no un bloque
    return haCaidoBloque;

  }

  /**
   * Realiza una espera activa
   * @param milisegundos int milisegundos de espera
   */
  private void WaitTime(int milisegundos) {
    long initTime = System.currentTimeMillis();
    long elapsedTimeT = 0;
    long temp;

    while (elapsedTimeT < milisegundos) {
      temp = System.currentTimeMillis();
      elapsedTimeT += temp - initTime;
      initTime += temp - initTime;
    }
  }

  ////////////////////////////////////////////////////////////////
  //Funciones de dibujado
  ////////////////////////////////////////////////////////////////

  /**
   * Dibuja el bloque de gemas del jugador
   * */
  private void DibujaBloqueJugador(Graphics g) {

    //s�lo dibujamos en el espacio del tablero por lo que
    //creamos un rect�ngulo de clip que lo contenga
    g.setClip(tablero.TABLERO_X,
              tablero.TABLERO_Y,
              tablero.ANCHO_TABLERO * tablero.TAMCASILLA,
              tablero.ALTO_TABLERO * tablero.TAMCASILLA);

    int i = 0;
    int j = bloqueJugador.indexGem;

    //Borramos el bloque anterior ///////////////////////////////////
    /* Chapuzilla: en realidad borramos 4 gemas: las 3 correspondientes al bloque
     y la superior al bloque, sino �sta permaneceria al caer el bloque del jugador
        una posici�n.*/
    while (i <= CBloquePieza.NUM_GEMS) {

      g.drawImage(gemas.gemImages[CGemas.NONE],
                  tablero.TABLERO_X +
                  ( (bloqueJugador.posAnt.x) * tablero.TAMCASILLA),
                  tablero.TABLERO_Y +
                  ( (bloqueJugador.pos.y - 3) * tablero.TAMCASILLA) -
                  (tablero.TAMCASILLA * i),
                  Graphics.TOP | Graphics.LEFT);

      i++;
    }

    //Ahora Redibujamos el bloque //////////////////////////7
    i = CBloquePieza.NUM_GEMS - 1;
    //Recordatorio: j = bloqueJugador.indexGem;

    while (i >= 0) {
      j--;

      if (j < 0) {
        j = CBloquePieza.NUM_GEMS - 1;
      }

      //Ahora redibujamos el nuevo bloque
      g.drawImage(gemas.gemImages[bloqueJugador.gemsColor[j]],
                  tablero.TABLERO_X +
                  ( (bloqueJugador.pos.x) * tablero.TAMCASILLA),
                  tablero.TABLERO_Y +
                  ( (bloqueJugador.pos.y - 3) * tablero.TAMCASILLA) -
                  (tablero.TAMCASILLA * i),
                  Graphics.TOP | Graphics.LEFT);

      --i;
    }
    g.setClip(0, 0, screenWidth, screenHeight);
  }

  /**
   * Muestra el bloque de gemas siguiente en la pantalla
   */
  private void DibujaBloqueNext(Graphics g) {

    g.setClip(BLOQUE_NEXT_X,
              BLOQUE_NEXT_Y,
              tablero.TAMCASILLA,
              tablero.TAMCASILLA * CBloquePieza.NUM_GEMS);

    // Necesitamos dibujarlos de atras a adelante
    int i = 0;
    int j = bloqueNext.indexGem;

    while (i < CBloquePieza.NUM_GEMS) {
      j--;
      if (j < 0) {
        j = CBloquePieza.NUM_GEMS - 1;
      }
      //Primero borramos
      g.drawImage(gemas.gemImages[CGemas.NONE],
                  BLOQUE_NEXT_X,
                  BLOQUE_NEXT_Y + (tablero.TAMCASILLA * i),
                  Graphics.TOP | Graphics.LEFT);
      //Y ahora dibumajos la nueva gema
      g.drawImage(gemas.gemImages[bloqueNext.gemsColor[j]],
                  BLOQUE_NEXT_X,
                  BLOQUE_NEXT_Y + (tablero.TAMCASILLA * i),
                  Graphics.TOP | Graphics.LEFT);
      ++i;
    }

    g.setClip(0, 0, screenWidth, screenHeight);

  }

  /**
   * Dibuja en pantalla el texto de pausa a partir de una imagen.
   * La funci�n guarda el estado del backbuffer anterior
   * @param g Graphics
   */
  private void DibujaTextoPausa() {

      //Guardamos la imagen anterior de la pantalla para reestablecerla al salir de la pausa
      imgTempTexto = Image.createImage(screenWidth, screenHeight);

      //Objeto gr�fico para la imagen.
      Graphics imgGraph = imgTempTexto.getGraphics();

      //Copiamos ese �rea del backbuffer para luego reestablecerlo
      imgGraph.drawImage(backBuffer,
                         0,
                         0,
                         Graphics.TOP | Graphics.LEFT);

      //Marcamos la imagen de texto como dibujada
      imagenTextoDibujada = true;



    //Dibujamos el men� de pausa
    imgGraph.drawImage(imgMenuPausa, 
            0, 
            screenHeight - imgMenuPausa.getHeight(), 
            Graphics.TOP | Graphics.LEFT);

    //Establecemos la pantalla de clip centrada y de tama�o la porci�n de la imagen
    //ocupada por el texto "Pausa"
    //*
    imgGraph.setClip( (screenWidth / 2) - (imgTextos.getWidth() / 2),
               (screenHeight / 2) - (altoTextoPausa / 2),
               imgTextos.getWidth(),
               altoTextoPausa);
    //*/

    //Dibujamos la imagen centrada horizontalmente y desplazada verticalmente
    //hacia arriba la distancia ocupada por el texto game over para que el
    //texto pausa quede centrado
    imgGraph.drawImage(imgTextos,
                (screenWidth / 2) - (imgTextos.getWidth() / 2),
                (screenHeight / 2) - altoTextoGameOver - (altoTextoPausa / 2),
                Graphics.TOP | Graphics.LEFT);

    //Reestablecemos el �rea de clip
    imgGraph.setClip(0, 0, screenWidth, screenHeight);

  }

  /**
   * Dibuja en pantalla el texto Game Over a partir de una imagen
   * @param g Graphics
   */
  private void DibujaTextoFinJuego(Graphics g) {
    //Establecemos la pantalla de clip centrada y de tama�o la porci�n de la imagen
    //ocupada por el texto "game over"
    g.setClip( (screenWidth / 2) - (imgTextos.getWidth() / 2),
              (screenHeight / 2) - (53 / 2),
              imgTextos.getWidth(),
              53);

    //Dibujamos la imagen centrada, vertical y horizontalmente
    g.drawImage(imgTextos,
                (screenWidth / 2) - (imgTextos.getWidth() / 2),
                (screenHeight / 2) - (53 / 2),
                Graphics.TOP | Graphics.LEFT);

    //Reestablecemos el �rea de clip
    g.setClip(0, 0, screenWidth, screenHeight);

  }


  ////////////////////////////////////////////////////////////////////////////
  //Funciones Callback de Canvas
  ////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @param graphics Graphics Objeto para operaciones gr�ficas
   */
  protected void paint(Graphics g) {

    //Lo que hay que dibujar cambia segun el estado
    switch (state) {
      case CGameStates.EnJuego:

        //Dibujamos la pantalla de juego y la puntuaci�n la primera vez
        if (!pantallaInicialDibujada) {
          backBufferGraphics.drawImage(imgPantallaJuego, 0, 0,
                                       Graphics.TOP | Graphics.LEFT);

          //Borramos la puntacion
          backBufferGraphics.drawImage(imgFondoPuntuacion,
                                       MARCADOR_X,
                                       MARCADOR_Y,
                                       Graphics.TOP | Graphics.LEFT);

          //Podria ser m�s eficiente usar stringbuffer�s para esto, pero como solo se hace
          //cuando destruimos gemas el rendimiento no baja mucho
          backBufferGraphics.drawString(new Integer(score).toString(),
                                        TEXTO_MARCADOR_X,
                                        TEXTO_MARCADOR_Y,
                                        Graphics.BASELINE | Graphics.RIGHT);

          //Marcamos la pantalla como dibujada inicialmente
          pantallaInicialDibujada = true;

        }

        //Dibujamos la pieza next
        DibujaBloqueNext(backBufferGraphics);

        //Dibujamos el bloque del jugador
        DibujaBloqueJugador(backBufferGraphics);
        break;

      case CGameStates.BuscandoGemas:

        /**
         * Aqu� actualizamos el marcador. Es un poco chapuza pero es el mejor lugar
         * para hacerlo s�lo cuando sea necesario, en vez de actualizarlo una vez por frame
         */
        //Borramos la puntacion anterior
        backBufferGraphics.drawImage(imgFondoPuntuacion,
                                     MARCADOR_X,
                                     MARCADOR_Y,
                                     Graphics.TOP | Graphics.LEFT);

        //Actualizamos la puntacion
        //Podria ser m�s eficiente usar stringbuffer�s para esto, pero como solo se hace una vez
        //por frame el rendimiento no baja mucho
        backBufferGraphics.drawString(new Integer(score).toString(),
                                      TEXTO_MARCADOR_X,
                                      TEXTO_MARCADOR_Y,
                                      Graphics.BASELINE | Graphics.RIGHT);

        break;

      case CGameStates.DestruyendoGemas:

        //S�lo vamos a modificar el espacio ocupado por el tablero
        backBufferGraphics.setClip(tablero.TABLERO_X,
                                   tablero.TABLERO_Y,
                                   tablero.ANCHO_TABLERO * tablero.TAMCASILLA,
                                   tablero.ALTO_TABLERO * tablero.TAMCASILLA);

        //Si hay animacion la actualizamos
        if (tablero.hayAnimacion()) {
          tablero.DibujarAnimacionDestruirGemas(backBufferGraphics,  frameElapsedTime );
        }

        //Restauramos el clip
        backBufferGraphics.setClip(0, 0, screenWidth, screenHeight); ;
        break;

      case CGameStates.GameOver:
        DibujaTextoFinJuego(backBufferGraphics);
        break;

      case CGameStates.Cargando:

        //Aqu� no usamos backbuffer dibujamos directamente sobre el canvas
        g.setColor(0x00FFFFFF);
        g.fillRect(0, 0, screenWidth, screenHeight);
        g.setColor(0x00000000);
        g.setFont(Font.getFont(Font.FACE_SYSTEM,
                               Font.STYLE_BOLD,
                               Font.SIZE_MEDIUM));
        g.drawString("CARGANDO",
                     screenWidth / 2,
                     screenHeight / 2,
                     Graphics.HCENTER | Graphics.BASELINE);

        return;

    } //fin switch

    //Si el juego est� en pausa dibujamos el texto

    if (pausado){

       //Escribimos en la pantalla la confirmacion para salir
       if (confirmaSalir){
         //Copiamos los contenidos del backbuffer al canvas (doble buffering)
         g.drawImage(backBuffer, 0, 0, Graphics.TOP | Graphics.LEFT);

         //g.setColor(255, 255, 255); //color blanco
         g.drawString("�Desea salir?",
                      screenHeight /2,
                      screenWidth /2,
                      Graphics.HCENTER | Graphics.BASELINE);

         g.drawImage(imgMenuConfirmaSalir, 
                 0, 
                 screenHeight - imgMenuConfirmaSalir.getHeight(),
                 Graphics.TOP | Graphics.LEFT);
       }
       else{
       //Copiamos el BackBuffer y en la copia dibujamos el menu y el texto pausa
       DibujaTextoPausa();

       //Dibujamos directamente sobre el canvas la imagen con el texto de pausa
        g.drawImage(imgTempTexto, 0, 0, Graphics.TOP | Graphics.LEFT);
       }
    }
    else{
      //Copiamos los contenidos del backbuffer al canvas (doble buffering)
      g.drawImage(backBuffer, 0, 0, Graphics.TOP | Graphics.LEFT);
      imagenTextoDibujada = false;
    }
  }

  /**
   * Maneja las pulsaciones de teclas
   * @param keyCode int
   */
  protected void keyPressed(int keyCode) {

    //Traducimos el keyCode a una GameAction
    int action = getGameAction(keyCode);


    //C�digo spagetti para gestionar la confirmaci�n de salir del juego

    if ( (keyCode == KEY_SOFTKEY2) && (!confirmaSalir) )
      pausado = !pausado;

    else if ( (pausado && !confirmaSalir) && (keyCode == KEY_SOFTKEY1) )
      confirmaSalir = true;

    else if ( confirmaSalir && (keyCode == KEY_SOFTKEY1) )
      state = CGameStates.FinJuego;

    else if ( confirmaSalir &&  (keyCode == KEY_SOFTKEY2) )
      confirmaSalir = false;


    //Operamos segun el estado
   if (state ==  CGameStates.EnJuego){

        //Operamos segun la gameAction y activamos el movimiento
        switch (action) {

          case LEFT:
            movimientos.izquierda = true;
            // movimientos.teclaProcesada = false;
            break;

          case RIGHT:
            movimientos.derecha = true;
            // movimientos.teclaProcesada = false;
            break;

          case UP:
          case FIRE:
            movimientos.moverGemas = true;
           // movimientos.teclaProcesada = false;
            break;

          case DOWN:
            movimientos.caida = true;
            // movimientos.teclaProcesada = false;
            break;
        }

   }



  }

  /**
   * Recoge el fin de pulsaci�n de una tecla
   * @param keyCode int
   */
  protected void keyReleased(int keyCode) {

    //solo si estamos jugando
    if (state == CGameStates.EnJuego) {

      //Traducimos el keyCode a una GameAction
      int action = getGameAction(keyCode);


  //    if (movimientos.teclaProcesada == true)

        //Operamos segun la gameAction y anulamos el movimiento
        switch (action) {

          case LEFT:
            movimientos.izquierda = false;
            break;

          case RIGHT:
            movimientos.derecha = false;
            break;

          case DOWN:
            movimientos.caida = false;
            break;

        }
//      else
 //       movimientos.teclaLiberada = true;
    }
  }


  ////////////////////////////////////////////////////////////////////////////
  //Iterface Runnable
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Bucle principal del juego
   */
  public void run() {


    //Limitar el juego a 30FPS en todos los dispositivos
    int framecount = 0;
    long totalFrameTime = 0;

    //Inicializamos los tiempos por primera vez
    logicLastTimeCall = frameLastTimeCall = System.currentTimeMillis();
    frameElapsedTime = 0;
    timeDrop = 0;

    //Bucle principal
    while (state != CGameStates.FinJuego) {

      //Actualizamos el tiempo transcurrido entre frames
      frameElapsedTime = System.currentTimeMillis() - frameLastTimeCall;
      frameLastTimeCall = System.currentTimeMillis();

      //Si est� pausado debemos aumentar el tiempo de l�gica para que la diferencia
      //de tiempos se mantenga
      if(pausado)
        logicLastTimeCall += frameElapsedTime;



      //Si no estamos en pausa
      if (!pausado) {


       //La l�gica cambia dependiendo del estado
        switch (state) {

          //Ciclo de juego en el que la pieza cae y el jugador la mueve
          case CGameStates.EnJuego:

            //Comprobamos si cae el bloque
            if ( GestionaCaidaBloqueTiempo(frameElapsedTime) )
             state = CGameStates.BuscandoGemas;

            //Comprobamos el movimiento del bloque por parte del jugador, asi
            //como si este se ha fijado en el tablero s�lo cada cierto num de frames
            if ( (System.currentTimeMillis() - logicLastTimeCall) > LOGIC_TICK ){

              //si el bloque se ha fijado buscamos gemas a destruir
              if (GestionaMovimientoBloque())
                state = CGameStates.BuscandoGemas;

              //movimientos.ResetMovimientosJugador();

              //Actualizamos el tiempo para la l�gica
              logicLastTimeCall = System.currentTimeMillis();
            }
            break;

            //Buscando gemas que pueden destruirse
          case CGameStates.BuscandoGemas:

            //si encontramos gemas que se pueden destruir cambiamos de estado
            if (tablero.BuscaGemasDestruibles()) {
              state = CGameStates.DestruyendoGemas;
              tablero.IniciarAnimacionDestruirGemas();
            }

            //Comprobamos si el juego ha finalizado
            else if (tablero.CompruebaFinPartida()) {
              state = CGameStates.GameOver;
            }

            //Si todo va bien seguimos jugando
            else {

              //Reseteamos todos los movimientos
              movimientos.ResetMovimientos();

              //Restauramos el multiplicador de dificultad a uno
              scoreMultiplier = 1;

              //Eliminamos la info con las gemas movidas
              tablero.ReiniciarGemasMovidas();

              //Pasamos al estado de juego
              state = CGameStates.EnJuego;
            }

            break;

            //Destruyendo las gemas
          case CGameStates.DestruyendoGemas:

            //si acab� la animaci�n de destruir gemas volvemos a comprobar las gemas
            if (!tablero.hayAnimacion()) {

              /**
               * Actualizamos puntuaci�n
               * El multiplicador de puntuaci�n aumenta al destruir varios grupos de gemas
               * en una jugada.
               * Por cada gema destruida por encima de tres se suma un bonus
               */
              score += (tablero.getNumGemasDestruidas() * GEM_SCORE_VALUE * scoreMultiplier);
              score += (tablero.getNumGemasDestruidas() - 3) * dificultad;

              //Cambiamos de estado
              state = CGameStates.BuscandoGemas;

              //Aumentamos el multiplicador, con un m�ximo de 4
              if (scoreMultiplier < 4) {
                scoreMultiplier++;
              }

              //Eliminamos la info con las gemas destruidas
              tablero.ReiniciarGemasDestruidas();

            }
            break;

          case CGameStates.GameOver:
            repaint();
            state = CGameStates.FinJuego;
            WaitTime(1500);
            break;

        } //fin switch de estados

      } //Fin if pausa



        //Repintamos la pantalla
        repaint();
        serviceRepaints();

        //Limitamos a 30 FPS
        if (++framecount > 30)
        {
          //Saltamos tiempo hasta hacer un segundo
          while (totalFrameTime < 1000){
            WaitTime(10);
            totalFrameTime += 10;
          }

          //Reinicializamos el contador de frames
          framecount = 0;
        }

    } //Fin while bucle

    //Esperamos un segundo antes de salir
    WaitTime(1000);

    //Destruir todos los objetos
    this.Destroy();

    //Al finalizar el bucle notificamos al MIDlet que tiene que inicializar el men�
    parentMIDlet.IniciarMenu(score);

  }
} //fin CJuegoCanvas
