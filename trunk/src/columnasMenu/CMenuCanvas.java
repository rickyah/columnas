package columnasMenu;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.io.*;

import basePackage.*;
import com.nokia.mid.ui.*;

public class CMenuCanvas
    extends FullCanvas implements Runnable {

  ////////////////////////////////////////////////////////////
  //Posibles estados del men�
  private class CMenuStates {

    //Men� principal
    public static final short Principal = 0;

    //Men� de selecci�n de opciones
    public static final short Opciones = 1;

     //Seleccionando la dificultad
    public static final short Dificultad = 2;

    //Seleciona el menu de inicio del juego (nuevo/continuar)
    public static final short InicioJuego = 3;

    //Selecciona el menu Acerca de
    public static final short Acerca = 4;

    //Cargando
    public static final short Cargando = 5;

    //Puntuaciones
    public static final short Puntuaciones = 6;

  };


  //////////////////////////////////////////////////////////
  //Variables generales
  //////////////////////////////////////////////////////////

  //MIDlet principal
  private columnasMIDlet parentMIDlet;
  private Display myDisplay;

  //Tama�o de la pantalla
  private int screenHeight;
  private int screenWidth;

  //Fuente de la pantalla
  private Font tipoFuente;

  //Cambiamos el nombre de las teclas de los nokia
  private static int KEY_MENU_IZQ = FullCanvas.KEY_SOFTKEY1;
  private static int KEY_MENU_DER = FullCanvas.KEY_SOFTKEY2;

  ////////////////////////////////////////////////////////
  //Variables de juego
  ////////////////////////////////////////////////////////

  private final int MAX_PUNTUACIONES = 5;

  //Estado actual del men�
  private int menuState = -1;

  //Dificultad seleccionada en el menu
  private static int menuDificultad = 0;

  //Indica si el menu de seleccion de dificultad se ha dibujado antes
  private boolean menuPaintedBefore = false;

  //Im�genes necesarias para la pantalla de inicio
  private Image imgSplashScreen, imgPartenon, imgMenuPrincipal, imgMenuAceptarVolver;

  //Fuente para escribir las puntuaciones
  private Font fuentePuntuaciones;

  //Cadenas de texto con los nombres de los jugadores con puntuaci�n m�xima
  private String[] nomJugadoresPuntuaciones;

  //Puntuaciones m�ximas
  private int[] puntuacionesMaximas;

  //Objeto para trabajar con el registro
  private RecordStore puntRS, nombRS;

  //Para comprobar si la puntuacion es m�xima
  private int puntuacionUltimaPartida;
  private String nombreJugador;
  private int indexJugador;

  ////////////////////////////////////////////////////////
  // Constantes para widgets
  ////////////////////////////////////////////////////////

  //Constantes para la posici�n de las puntuaciones
  private final int PUNTUACION_X_LEFT = 10;
  private final int PUNTUACION_X_RIGHT = 120;
  private final int PUNTUACION_Y_BASE = 60;

  //Constantes para los nombres de los registros
  private final String strRegistroPuntuaciones = "PuntuacionesColumnas";
  private final String strRegistroNombres = "NombresPuntuaciones";


  //Constantes de texto
  private final String strAyuda = "El objetivo del juego es formar tres o m�s gemas "
     + "adyacentes de igual color, por medio de los bloques de tres gemas que manejamos. "
     + "Para conseguir nuestro objetivo podemos desplazar el bloque a izquierda y derecha, "
     + "as� como cambiar el orden de las gemas del bloque."
     + "A mayor nivel de dificultad, mas r�pido caer� el bloque.\n"
     + "Tecla 4 Mueve el bloque a la izquierda.\n"
     + "Tecla 6 Mueve el bloque a la derecha.\n"
     + "Tecla 5 o 2 Cambia el orden de las gemas.\n"
     + "Tecla 8 hace caer el bloque.\n"
     + "Tambi�n se puede usar el pad o joytick para manejar el juego.";

  private final String strAcerca = "Columnas ver 1.0 \n rickyah@gmail.com \n Para Patry. :)";

  //Constantes para la posicion del selector de dificultad
  private final int selectorDificultad_X = 10;
  private final int selectorDificultad_Y = 85;

  //Constantes para la posicion del selector de opciones
  private final int selectorOpciones_X = 15;
  private final int selectorOpciones_Y = 55;


  ////////////////////////////////////////////////////////
  //Elementos del menu
  ////////////////////////////////////////////////////////

  //Barra de estado en forma de columna (selector de dificultad)
  private CColumnGauge barraDificultad;

  //Lista de opciones a elegir (selector de opciones)
  private CTextChoice listaOpciones;

  //Opciones posibles del selector
  private final String opMenuDificultad = "Dificultad";
  private final String opPuntuaciones = "Puntuaciones";
  private final String opMenuAyuda = "Ayuda";
  private final String opMenuAcerca = "Acerca de";

  //Pantalla de ayuda, acerca de y puntuacion maxima
  private Alert menuAyuda, menuAcerca, alertaPuntuacionMaxima;

  //Pantalla para introducir nombre
  private CMyTextBox intrNombTB;


  //////////////////////////////////////////////////////////
  //M�todos
  //////////////////////////////////////////////////////////

  /**
   * El constructor solo inicia la variable de MIDlet.
   *
   * @param m MIDlet que invoc� este hilo.
   */
  public CMenuCanvas(columnasMIDlet pm) {
    //Guardamos el MIDlet que inici� esta ejecuci�n
    parentMIDlet = pm;

    //tambi�n el display actual
    myDisplay = Display.getDisplay(parentMIDlet);

    //Tama�o de la pantalla
    screenHeight = getHeight();
    screenWidth = getWidth();
    
    //Para moviles que no pertenezcan a la serie40
    if (screenHeight > 128)
        screenHeight = 128;
    if (screenWidth > 128)
        screenWidth = 128;            

  }

  /**
   * Inicia todos los recursos necesarios para el menu
   */
  public void Init() {

    //Inicializamos o cargamos las puntuaciones
    try {
      CargaPuntuaciones();
    }
    catch (Exception ex) {
    }

    //Fuente de la pantalla
    tipoFuente = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);

    menuState = CMenuStates.Cargando;
    repaint();
    serviceRepaints();

    //cargamos las im�genes
    try {
      imgPartenon = Image.createImage("/res/partenon.png");

      imgSplashScreen = Image.createImage("/res/splash.png");

      imgMenuPrincipal = Image.createImage("/res/menu_principal.png");

      imgMenuAceptarVolver = Image.createImage("/res/menu_acep_vol.png");
    }
    catch (Exception e) {
      //Recogemos info si hay fallos
      System.err.println("Error E/S: " + e.getMessage());
      e.printStackTrace();
    }

    //Creamos la barra de dificultad con forma de columna.
    barraDificultad = new CColumnGauge(menuDificultad);

    //Creamos la lista de opciones del menu
    listaOpciones = new CTextChoice();

    //A�adimos las diferentes opciones que mostrar�
    listaOpciones.Add(opMenuDificultad);
    listaOpciones.Add(opPuntuaciones);
    listaOpciones.Add(opMenuAyuda);
    listaOpciones.Add(opMenuAcerca);


    //Creamos el menu de ayuda
     menuAyuda = new Alert("Ayuda", strAyuda, null, AlertType.INFO);
     menuAyuda.setTimeout(Alert.FOREVER);
    //Creamos el menu "Acerca de"
     menuAcerca = new Alert ("Acerca de", strAcerca, null, AlertType.INFO);
     menuAcerca.setTimeout(Alert.FOREVER);
     //Pantalla de puntuacion maxima
     alertaPuntuacionMaxima = new Alert("Felicidades!", "Puntuacion m�xima",  null, null);
     alertaPuntuacionMaxima.setTimeout(2500);

     //Pantalla para introducir el nombre
     intrNombTB = new CMyTextBox("Introduce nombre", myDisplay, this);

  }

  /**
   * Libera todos los recursos necesarios para el men�
   */
  public void Destroy()  {
    //Destruimos las im�genes
    imgPartenon = null;
    imgSplashScreen = null;
    imgMenuAceptarVolver = null;
    imgMenuPrincipal = null;
    imgMenuAceptarVolver = null;

    //"Destruimos" los widgets
    barraDificultad = null;
    listaOpciones = null;
    menuAyuda = null;
    menuAcerca = null;

    //Destruimos las estructuras para las puntuaciones m�ximas
    nomJugadoresPuntuaciones = null;
    puntuacionesMaximas = null;

    //Recolector de basura
    System.gc();

    menuState = CMenuStates.Cargando;

  }

  /**
   * Retorna la dificultad seleccionada en el men�
   * @return int
   */
  public int getDificultad(){
    return menuDificultad;
  }

  /**
   * Establece la puntuacion de la ultima partida para comprobar si es una
   * puntuaci�n m�xima
   * @param puntuacion int
   */
  public void setPuntuacionUltimaPartida(int puntuacion){

  puntuacionUltimaPartida = puntuacion;

}

  /**
     * Carga las puntuaciones m�ximas o bien las genera si es la primera ejecuci�n.
     * @throws RecordStoreNotOpenException
     * @throws InvalidRecordIDException
     * @throws RecordStoreException
     * @throws IOException
     */
    private void CargaPuntuaciones() throws RecordStoreNotOpenException,
        InvalidRecordIDException, RecordStoreException, IOException {

       //Inicializamos las estructuras para puntuaciones
       nomJugadoresPuntuaciones = new String[MAX_PUNTUACIONES];
       puntuacionesMaximas = new int[MAX_PUNTUACIONES];

     //Cargamos el registro con la puntuaciones, o lo creamos la primera vez
     if ( RecordStore.listRecordStores() == null ){

         //Abrimos el registro, y si no existe lo creamos
         puntRS = RecordStore.openRecordStore(strRegistroPuntuaciones, true);
         nombRS = RecordStore.openRecordStore(strRegistroNombres, true);

         //Lo rellenamos con nombres y puntuaciones prefijados
         setHardCodedScores();

         for (int i = 0; i < MAX_PUNTUACIONES; ++i){

           //Necesitamos flujos para escribir en el registro
           ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
           ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
           DataOutputStream os1 = new DataOutputStream(baos1);
           DataOutputStream os2 = new DataOutputStream(baos2);


           //Escribimos en el registro el nombre
           os1.writeUTF(nomJugadoresPuntuaciones[i]);
           nombRS.addRecord( baos1.toByteArray(), 0, baos1.size() );

           //Escribimos en el registro la puntuacion
           os2.writeInt(puntuacionesMaximas[i]);
           puntRS.addRecord(baos2.toByteArray(), 0, baos2.size() );
       }

     }

//Si no, simplemente lo abrimos y cargamos las puntuaciones
   else{

         puntRS = RecordStore.openRecordStore(strRegistroPuntuaciones, false);
         nombRS = RecordStore.openRecordStore(strRegistroNombres, false);

         //Recorremos el registro de puntuaciones ////////////////////////////////

         //Cargamos las puntuaciones
         //re = puntRS.enumerateRecords(null, null, false);

         //Inicializamos para la iteracion por el array
        // i = 0;

         //Recorremos todos los registros
         for (int i = 0; i < MAX_PUNTUACIONES; ++i){
             //Necesitamos flujos para leer del registro
             ByteArrayInputStream bais = new ByteArrayInputStream( puntRS.getRecord(i+1) );
             DataInputStream is = new DataInputStream(bais);
             //Leemos el dato y lo almacenamos en el array
             puntuacionesMaximas[i] = is.readInt();
             //siguiente elemento del array
             //i++;
         }

         //Recorremos el registro de nombres /////////////////////////////////////

         //Cargamos los nombres

         //Inicializamos para la iteracion por el array
         //i = 0;

         //Recorremos todos los registoros
          for (int i = 0; i < MAX_PUNTUACIONES; ++i){

             //Necesitamos flujos para leer del registro
             ByteArrayInputStream bais = new ByteArrayInputStream(nombRS.getRecord(i+1));
             DataInputStream is = new DataInputStream(bais);
             //Leemos el dato y lo almacenamos en el array
             nomJugadoresPuntuaciones[i] = is.readUTF();

           //siguiente elemento del array
           //i++;
          }

     } //Fin if - else

     //Cerramos los registros

       nombRS.closeRecordStore();
       puntRS.closeRecordStore();

     //LLamamos al recolector de basura
       System.gc();
    }

    /**
     * Comprueba si la puntuacion pasada como par�metro es una puntuacion maxima
     * y en ese caso actualiza tanto las puntuaciones cargadas en memoria como
     * el registro RSM
     *
     * @param puntuacion int
     * @throws RecordStoreException
     * @throws IOException
     */
    private boolean CompruebaPuntuacion(int puntuacion){

      //Comprobamos si es puntuaci�n m�xima.
      for (int i = 0; i < MAX_PUNTUACIONES; ++i)
        //Si es una puntuacion m�xima
        if (puntuacion > puntuacionesMaximas[i]) {
          indexJugador = i;
          intrNombTB.setDisplayable(this);
          //Mostramos la pantalla de alerta que indica una puntuacion m�xima
          myDisplay.setCurrent(alertaPuntuacionMaxima, intrNombTB);

         // parentMIDlet.WaitTime(6000);

          return true;

          }

        return false;
    }

    /**
     * Dada una puntuaci�n, actualiza los registros si esta es una puntuacion m�xima
     * Utiliza las variables puntuacionUltimaPartida y nombreJugador
     * @throws RecordStoreException
     * @throws IOException
     */
    public void ActualizaPuntuacion() throws RecordStoreException, IOException {

      String nombre = intrNombTB.getString();

      //Escribimos la puntuaci�n maxima en el registro as� como el nombre del jugador
      RecordStore puntRS, nombRS;

      //Abrimos los registros
      puntRS = RecordStore.openRecordStore(strRegistroPuntuaciones, false);
      nombRS = RecordStore.openRecordStore(strRegistroNombres, false);


      //"Movemos hacia abajo" las puntuaciones por debajo de la actual
      int i = MAX_PUNTUACIONES-1;
      while (i != indexJugador){

        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        DataOutputStream os1 = new DataOutputStream(baos1);
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        DataOutputStream os2 = new DataOutputStream(baos2);

        os1.writeUTF(nomJugadoresPuntuaciones[i-1]);
        os2.writeInt(puntuacionesMaximas[i-1]);

        nombRS.setRecord(i+1, baos1.toByteArray(), 0, baos1.size() );
        nomJugadoresPuntuaciones[i] = nomJugadoresPuntuaciones[i-1];

        puntRS.setRecord(i+1, baos2.toByteArray(), 0, baos2.size());
        puntuacionesMaximas[i] = puntuacionesMaximas[i-1];

        i--;
      }

      //y escribimos la puntuaci�n m�xima actual
      ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
      DataOutputStream os1 = new DataOutputStream(baos1);
      ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
      DataOutputStream os2 = new DataOutputStream(baos2);

      //Escribimos en el buffer el string con el nombre
      os1.writeUTF(nombre);
      //Escribimos en el buffer la nueva puntuacion maxima
      os2.writeInt(puntuacionUltimaPartida);

      //Actualizamos el registro y las estructuras de datos para el nombre
      nombRS.setRecord(indexJugador+1, baos1.toByteArray(), 0, baos1.size() );
      nomJugadoresPuntuaciones[indexJugador] = nombre;

      //Actualizamos el registro y las estructuras de datos para la puntuacion
      puntRS.setRecord(indexJugador+1, baos2.toByteArray(), 0, baos2.size() );
      puntuacionesMaximas[indexJugador] = puntuacionUltimaPartida;

      //Cerramos los registros
      puntRS.closeRecordStore();
      nombRS.closeRecordStore();


    }

  /**
   * Inicializa las puntuaciones a valores prefijados
   */
  private void setHardCodedScores(){
    nomJugadoresPuntuaciones[0] = "Ender";
    nomJugadoresPuntuaciones[1] = "Bean";
    nomJugadoresPuntuaciones[2] = "Valentine";
    nomJugadoresPuntuaciones[3] = "Peter";
    nomJugadoresPuntuaciones[4] = "Ricky";

    puntuacionesMaximas[0] = 5000;
    puntuacionesMaximas[1] = 4000;
    puntuacionesMaximas[2] = 3000;
    puntuacionesMaximas[3] = 2000;
    puntuacionesMaximas[4] = 1000;

  }

  ////////////////////////////////////////////////////////////////////////////
  //M�todos callback de canvas
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Manejamos pulsaciones de teclas
   * @param keyCode int C�digo de la tecla pulsada
   */
  protected void keyPressed(int keyCode) {

    int action = getGameAction(keyCode);


    //Diferentes opciones segun el estado
    switch (menuState) {

      case CMenuStates.Principal:
        if (keyCode == KEY_MENU_IZQ)
          //menu de opciones
          menuState = CMenuStates.Opciones;

        else if (keyCode == KEY_MENU_DER){
          /** @todo Comprobar si ten�amos un juego pausado */
          menuState = CMenuStates.InicioJuego;
          /** @todo Quitar esto en la versi�n que guarda partida */
          menuState = CMenuStates.InicioJuego;

        }
        break;


      case CMenuStates.Opciones:

        if (keyCode == KEY_MENU_DER)
          //Vuelta al menu de opciones
          menuState = CMenuStates.Principal;

        else if (keyCode == KEY_MENU_IZQ){

          //Comprobamos la selecci�n del usuario y pasamos a ese estado.
          //al men� correspondiente
          if (listaOpciones.getSeleccion() == opMenuAyuda){
            Display.getDisplay(parentMIDlet).setCurrent(menuAyuda);
          }


          else if (listaOpciones.getSeleccion() == opMenuDificultad){
            menuState = CMenuStates.Dificultad;
            //acualizamos la barra con el valor de la dificultad.
            barraDificultad.setValue(menuDificultad);
          }

          else if (listaOpciones.getSeleccion() == opMenuAcerca)
              Display.getDisplay(parentMIDlet).setCurrent(menuAcerca);

          else if (listaOpciones.getSeleccion() == opPuntuaciones)
            menuState = CMenuStates.Puntuaciones;

        }

        //movemos el cursor en la lista de opciones.
        else if (action == Canvas.UP)
          listaOpciones.Last();

        else if (action == Canvas.DOWN)
          listaOpciones.Next();

        break;


      case CMenuStates.Dificultad:

        if (action == Canvas.RIGHT)
          barraDificultad.incValue();

        else if (action == Canvas.LEFT)
          barraDificultad.decValue();

        else if ((keyCode == KEY_MENU_IZQ) || (action == Canvas.FIRE)){
          menuDificultad = barraDificultad.getValue();
          menuState = CMenuStates.Opciones;
        }

        else if (keyCode == KEY_MENU_DER)
          menuState = CMenuStates.Opciones;

        break;


     case CMenuStates.Puntuaciones:

       menuState = CMenuStates.Opciones;

       break;

    }

  }

  /**
   * Dibujado
   * @param graphics Graphics Objeto para dibujar gr�ficos.
   */
  protected void paint(Graphics graphics) {
    //Establecemos una fuente mas visible
    graphics.setFont(tipoFuente);

    switch (menuState){

      case CMenuStates.Principal:
        graphics.drawImage(imgSplashScreen, 0, 0, Graphics.TOP | Graphics.LEFT);
        graphics.drawImage(imgPartenon, 
                0, 
                screenHeight - imgPartenon.getHeight(), 
                Graphics.TOP | Graphics.LEFT);
        graphics.drawImage(imgMenuPrincipal, 
                0, 
                screenHeight - imgMenuPrincipal.getHeight(),
                Graphics.TOP | Graphics.LEFT);
        break;

      case CMenuStates.Opciones:
        graphics.drawImage(imgSplashScreen, 0, 0, Graphics.TOP | Graphics.LEFT);
        graphics.drawImage(imgMenuAceptarVolver, 
                0,
                screenHeight - imgMenuAceptarVolver.getHeight(), 
                Graphics.TOP | Graphics.LEFT);
        listaOpciones.Draw(graphics, selectorOpciones_X, selectorOpciones_Y );
        break;

      case CMenuStates.Dificultad:
        graphics.drawImage(imgSplashScreen, 0, 0, Graphics.TOP | Graphics.LEFT);
        graphics.drawString("Nivel " + (barraDificultad.getValue() +1), screenWidth / 2, selectorDificultad_Y - 15, Graphics.HCENTER |Graphics.BASELINE);
        barraDificultad.drawGauge(graphics, selectorDificultad_X, selectorDificultad_Y);
        graphics.drawImage(imgMenuAceptarVolver, 
                0,
                screenHeight - imgMenuAceptarVolver.getHeight(), 
                Graphics.TOP | Graphics.LEFT);
        break;

      case CMenuStates.Puntuaciones:
        graphics.drawImage(imgSplashScreen, 0, 0, Graphics.TOP | Graphics.LEFT);
        Font tmpFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        graphics.setFont( tmpFont );
        for (int i=0; i<MAX_PUNTUACIONES; ++i){
          graphics.drawString(nomJugadoresPuntuaciones[i],
                              PUNTUACION_X_LEFT ,
                              PUNTUACION_Y_BASE + ( i * tmpFont.getHeight() ),
                              Graphics.BASELINE | Graphics.LEFT );
          graphics.drawString (String.valueOf(puntuacionesMaximas[i]),
                               PUNTUACION_X_RIGHT,
                               PUNTUACION_Y_BASE + ( i * tmpFont.getHeight() ),
                               Graphics.BASELINE | Graphics.RIGHT );
        }


        break;

      case CMenuStates.Cargando :
        graphics.setColor(0x00FFFFFF);
        graphics.fillRect(0,0, screenWidth, screenHeight);
        graphics.setColor(0x00000000);
        graphics.setFont(tipoFuente);
        graphics.drawString("CARGANDO",
                            screenWidth / 2,
                            screenHeight / 2,
                            Graphics.HCENTER | Graphics.BASELINE);
        break;
    }
  }


  ////////////////////////////////////////////////////////////////////////////
  //Interface Runnable
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Bucle principal
   */
  public void run() {

    //El estado inicial es el men� principal
     menuState = CMenuStates.Principal;

     //Comprobamos si hay una puntuacion que pueda ser m�xima
      CompruebaPuntuacion(puntuacionUltimaPartida);


    //Bucle inicial del juego
    while (menuState != CMenuStates.InicioJuego){

      repaint();
      serviceRepaints();
    }

   // Al acabar el bucle destruimos los datos e iniciamos el juego
   this.Destroy();
   parentMIDlet.IniciarJuego();

  }

}
