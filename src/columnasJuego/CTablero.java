package columnasJuego;

import java.util.Vector;
import javax.microedition.lcdui.Graphics;
import java.util.Enumeration;

public class CTablero {

  //Posición del tablero a la hora de dibujar
  public final int TABLERO_X = 5;
  public final int TABLERO_Y = 4;

  //Tamaño en píxeles de cada casilla del tablero
  public final int TAMCASILLA = 10;

  //Dimensiones del tablero
  public final short ALTO_TABLERO = 12 + CBloquePieza.NUM_GEMS;
  public final short ANCHO_TABLERO = 6 ;

  //Punto de referencia del marcador de puntuacion
  private final int MARCADOR_X = 75;
  private final int MARCADOR_Y = 21;

  //Duración de la animación de destruir gemas
  private final int TIME_DESTROYANIM = 1200;

  //Informacion de las casillas
  private CCasilla _dataTablero[][] = new CCasilla[ANCHO_TABLERO][ALTO_TABLERO];

  //Lista de gemas marcadas para ser destruidas
  private Vector _gemasDestruidas;

  //Lista de columnas movidas
  private int[] _columnasMovidas = new int[ANCHO_TABLERO];

  //Referencia a las gemas
  CGemas _gemas;

  //indica el tiempo transcurrido de la animacion en curso.
  private long tiempoAnimacion;

  //Indica el tiempo transcurrido entre dos frames de la animación
  private long tiempoFrameAnimacion;

  //Indica los dos estados posibles de la animacion
  private boolean firstStepAnimacion;

  //indica si se está ejecutando la animación de destruir gemas
  private boolean hayAnimacion;

  // /** @todo comentar esto */
  private static final short LIM_SUP_JUEGO = 2;
  /**
   * Constructor
   */
  public CTablero(CGemas g) {
    _gemas = g;
    _gemasDestruidas = new Vector(5);
    hayAnimacion = false;
    InicializarTablero();
  }

  /**
   * Reinicializamos el tablero
   */
  public void InicializarTablero(){
    //inicializamos el tablero
    for (int i = 0; i < ANCHO_TABLERO; ++i)
      for (int j = 0; j < ALTO_TABLERO; ++j)
        _dataTablero[i][j] = new CCasilla();
  }

  /**
   * Dada una posición del tablero retorna el color de la gema contenida en dicha
   * posición, si es que la hay.
   *
   * @param x int Posición X del tablero.
   * @param y int Posición Y del tablero.
   * @return int Color de la gema.
   */
  public short getColorCasilla(int x, int y){
    return _dataTablero[x][y].colorGema;
  }

  /**
   * Dada una posición del tablero retorna si la gema contenida en dicha
   * posición está marcada para ser destruida.
   *
   * @param x int Posición X del tablero.
   * @param y int Posición Y del tablero.
   * @return boolean true si la gema está marcada como destruida, false en caso contrario.
   */
  public boolean getEstadoCasilla(int x, int y){
    return _dataTablero[x][y].esDestruida;
  }

  /**
   * Actualiza el tablero destruyendo las gema marcadas como eliminadas
   *
   * @param g Graphics objeto para dibujar
   */
  public void BorraGemas(Graphics g) {

    //Recorremos las columnas
    for (int k = 0; k < ANCHO_TABLERO; ++k) {

      //Comenzamos a recorrer las casillas de esta columna hasta encontrar la
      //primera gema marcada como destruida
      int i = ALTO_TABLERO - 1;
      int j;

      //Al finalizar este bucle i contiene el índice de la 1ª casilla con una gema
      //que debe destruirse, o bien hemos llegado a la última gema de la columna
      while ( (i >= 0) && (!_dataTablero[k][i].esDestruida) &&
             (_dataTablero[k][i].colorGema != CGemas.NONE))
        --i;

      while ( ( i >= 0 ) && (_dataTablero[k][i].colorGema != CGemas.NONE) ){

        //Recorremos desde la gema siguiente a la primera gema destruida hasta el final, representado
        //por una casilla sin gema.
        j = i - 1;

        while ( ( j >= 0 ) && _dataTablero[k][j].colorGema != CGemas.NONE) {

          //si la casilla actual contiene una gema que debe ser destruida
          //la dibujamos una casilla sin gema y eliminamos el flag de destruida
          if (_dataTablero[k][j].esDestruida) {

            //Borramos la casilla donde estaba la gema
            g.drawImage(_gemas.gemImages[CGemas.NONE],
                        TABLERO_X + (k * TAMCASILLA),
                        TABLERO_Y + ((j-3) * TAMCASILLA),
                        Graphics.TOP | Graphics.LEFT);
            _dataTablero[k][j].esDestruida = false;
            _dataTablero[k][j].colorGema = CGemas.NONE;
          }
          //Si la casilla actual no debe ser destruida es porque o bien contiene
          //una gema de otro color, o bien es la primera casilla sin gemas de la columna,
          //luego lo movemos a la casilla i (que apunta a la 1ª casilla a destruir)
          //o lo que es lo mismo, bajamos la colunmna
          else {

            //Copiamos el color de la gema y la dibujamos (borrándola previamente
            _dataTablero[k][i].colorGema = _dataTablero[k][j].colorGema;
            _dataTablero[k][i].esDestruida = false;

            g.drawImage(_gemas.gemImages[CGemas.NONE],
                        TABLERO_X + (k * TAMCASILLA),
                        TABLERO_Y + ((i-3) * TAMCASILLA),
                        Graphics.TOP | Graphics.LEFT);
            g.drawImage(_gemas.gemImages[_dataTablero[k][i].colorGema],
                        TABLERO_X + (k * TAMCASILLA),
                        TABLERO_Y + ((i-3) * TAMCASILLA),
                        Graphics.TOP | Graphics.LEFT);

            //Borramos la casilla donde estaba la gema
            g.drawImage(_gemas.gemImages[CGemas.NONE],
                        TABLERO_X + (k * TAMCASILLA),
                        TABLERO_Y + ((j-3) * TAMCASILLA),
                        Graphics.TOP | Graphics.LEFT);
            _dataTablero[k][j].esDestruida = false;
            _dataTablero[k][j].colorGema = CGemas.NONE;

            //seguimos con la siguiente casilla
            --i;
          }

          //Siguiente casilla
          --j;

        }

        //Falta tratar el último elemento: si todas las casillas con gemas
        //de la columna son destruidas
        if (_dataTablero[k][i].esDestruida) {
          //Borramos la casilla donde estaba la gema
          g.drawImage(_gemas.gemImages[CGemas.NONE],
                      TABLERO_X + (k * TAMCASILLA),
                      TABLERO_Y + ((i-3) * TAMCASILLA),
                      Graphics.TOP | Graphics.LEFT);
          _dataTablero[k][i].esDestruida = false;
          _dataTablero[k][i].colorGema = CGemas.NONE;

        }

      }

    }

  }

  /**
   * Inicia la animación de parpadeo al destruir las gemas
   */
  public void IniciarAnimacionDestruirGemas() {
    hayAnimacion = true;
    tiempoAnimacion = 0;
    tiempoFrameAnimacion = 0;
  }

  /**
   * Dibuja la animacion que muestra cómo se destruyen las piezas.
   * @param g Graphics
   * @param elapsedTime long
   * @return boolean treu cuando ha finalizado la animacion
   */
  public void DibujarAnimacionDestruirGemas(Graphics g, long elapsedTime) {

    //Actualizamos el tiempo que lleva la animacion
    tiempoAnimacion += elapsedTime;
    tiempoFrameAnimacion += elapsedTime;

    if (tiempoAnimacion >= TIME_DESTROYANIM){
      tiempoAnimacion = tiempoFrameAnimacion = 0;
      firstStepAnimacion = true;
      hayAnimacion = false;
      this.BorraGemas(g);
      return;
    }

    CPos tmpPos;
    int colorGemActual;
    for (Enumeration e = _gemasDestruidas.elements(); e.hasMoreElements();){
      tmpPos = (CPos) e.nextElement();
      colorGemActual = _dataTablero[tmpPos.x][tmpPos.y].colorGema;
      if (firstStepAnimacion)
        g.drawImage(_gemas.gemImages[CGemas.NONE],
                    TABLERO_X + (tmpPos.x * TAMCASILLA),
                    TABLERO_Y + ((tmpPos.y-3) * TAMCASILLA),
                    Graphics.LEFT | Graphics.TOP);
      else
        g.drawImage(_gemas.gemImages[colorGemActual],
            TABLERO_X + (tmpPos.x * TAMCASILLA),
            TABLERO_Y + ((tmpPos.y -3) * TAMCASILLA),
            Graphics.LEFT | Graphics.TOP);
    }

    if (tiempoFrameAnimacion > 150){
      tiempoFrameAnimacion = 0;
      firstStepAnimacion = !firstStepAnimacion;
    }
    hayAnimacion =  true;
  }

  /**
   * Indica si la animación de destricción de gemas se está ejecutando.
   * @return boolean true si la animación se está ejecutando, false en caso contrario
   */
  public boolean hayAnimacion(){
    return hayAnimacion;
  }

  /**
   * Comprueba si el jugador ha perdido la partida debido a que las gemas cruzan
   * la parte superior de la pantalla.
   *
   * @return boolean true si ha perdido, falso en caso contrario.
   */
  public boolean CompruebaFinPartida() {
    for (int i=0; i < ANCHO_TABLERO; ++i) {
      if (_dataTablero[i][LIM_SUP_JUEGO].colorGema != CGemas.NONE)
           return true;
    }

    return false;
  }

  /**
   * Limpiamos el registro de gemas eliminadas
   */
  public void ReiniciarGemasDestruidas(){
     //Eliminamos el vector de gemas destruidas
    _gemasDestruidas.removeAllElements();
 }

 /**
 * Limpiamos el registro de gemas movidas
 */
 public void ReiniciarGemasMovidas(){
 //Borramos la lista de movidas
  for (int i = 0; i < _columnasMovidas.length; ++i)
    _columnasMovidas[i] = -1;

 }

  /**
   * Recorre el array con las columnas del tablero que se han movido y busca gemas
   * que puedan ser destruidas.
   *
   * La lista de columnas movidas guarda la fila a partir de la cual todas las gemas
   * de esa columna superiores a ella se han movido, con lo que empezamos a buscar desde ahí.
   *
   * @return boolean True si se encontraron gemas a destruir.
   */
  public boolean BuscaGemasDestruibles() {

    //indica si al final del proceso hay gemas marcadas para destruir.
    boolean hayGemasADestruir = false;

    //recorremos cada columna para ver si hay gemas que se han movido en ella
    for (int k = 0; k < _columnasMovidas.length; ++k) {

      //Si hay alguna gema que se ha movido en esta columna
      if (_columnasMovidas[k] != -1) {

       //Posición de la primera gema que se ha movido
       // int x = k; /** @todo puedo eliminar esta variable (la k no cambia en el proceso) */
        int y = _columnasMovidas[k];

        //Ya la podemos marcar no movida y la tratamos
        _columnasMovidas[k] = -1;

        //Recorremos todas las gemas a partir de la primera y hacia arriba
        //Podemos obtener el color de la gema del tablero porque previamente hemos
        //actualizado éste
        while ( (y >= 0) && (_dataTablero[k][y].colorGema != CGemas.NONE)) {

          //Buscamos gemas iguales adyacentes a la actual y las marcamos para eliminarlas.
          //Si se elimina alguna gema las que están por encima se marcarán como movidas
          //por lo que el estado del juego debe cambiarse para reflejar que se van a destruir
          //las gemas
          if( MarcaGemasAdyacentes (k, y) )
            hayGemasADestruir = true;

          //Siguiente gema (gema superior a la que tratamos)
          y--;
        }

      }
    } //Fin recorrido columnas

    //Si tenemos que destruir las gemas cambiamos el estado del juego
    return hayGemasADestruir;
  }

  /**
   * Dado un bloque de gemas del jugador, fija su posición en el tablero.
   *
   * @param bloque CBloquePieza Pieza de juego que ha de fijarse en el tablero.
   */
  public void FijaBloqueJugador(CBloquePieza bloque){

    int i = CBloquePieza.NUM_GEMS - 1;
    int j = bloque.indexGem;


    while (i >= 0 ) {
      j--;

      if (j < 0) {
        j = CBloquePieza.NUM_GEMS - 1;
      }

      //Si la pieza sobrepasa en altura los límites del tablero no actualizamos nada
      if ( !( (bloque.pos.y -i) < 0)  )
        _dataTablero[bloque.pos.x][bloque.pos.y - i].colorGema = bloque.gemsColor[j];

      i--;
    }

    //Actualizamos la lista de gemas movidas
    _columnasMovidas[bloque.pos.x] = bloque.pos.y;
}

  /**
   * Obtiene el número de gemas que están marcadas para destruir
   * @return int
   */
  public int getNumGemasDestruidas(){
    return _gemasDestruidas.size();
}

  /**
    * Dada una posición de una gema en el tablero busca tres o mas gemas adyacentes
    * del mismo color y las marca para su posterior destrucción
    *
    * @param x int Posición X en el tablero.
    * @param y int Posición Y en el tablero.
    * @return boolean
    */
  private boolean MarcaGemasAdyacentes(int x, int y) {
    //Variables ////////////////

    //No hay gema en la casilla, salimos
    if (_dataTablero[x][y].colorGema == CGemas.NONE)
      return false;

    //Color de la gema que estamos comparando
    int colorGema = _dataTablero[x][y].colorGema;

    //número gemas de igual color en cada direccion
    int cont;

    //variables temporales para recorrer las gemas adyacentes
    int i, j;

    //gemas potenciales que se pueden destruir en cada dirección
    CPos[] gemasDestTemp = new CPos[4];

    //Indica si se ha destruido alguna gema
    boolean hayGemasDestruidas = false;

    ////////////////////////////////////////////////////////////////////
    //Horizontal  //////////////////////////////////////////////////////
    cont = 0;

    //Hacia la derecha
    i = x + 1;
    while ( (i < ANCHO_TABLERO) && (_dataTablero[i][y].colorGema == colorGema) ) {

      //Guardamos la posición de la gema
      gemasDestTemp[cont] = new CPos(i, y);
      cont++;

      //Siguiente gema
      ++i;
    }

    //Hacia la izquierda
    i = x - 1;
    while ( (i >= 0) && (_dataTablero[i][y].colorGema == colorGema)) {

      //Guardamos la posición de la gema
      gemasDestTemp[cont] = new CPos(i, y);
      cont++;

      //Siguiente gema
      --i;
    }

    //Actualizamos la lista de gemas si tenemos mas de 2 gemas del mismo color
    //que la que tratamos (hacen un total de tres)
    if(cont >= 2){
      ActualizaDestruidasParciales(x, y, cont, gemasDestTemp);
      hayGemasDestruidas = true;
    }
     ////////////////////////////////////////////////////////////////////
  //Vertical  ////////////////////////////////////////////////////////
     cont = 0;

    //Hacia arriba
    j = y-1;
    while ( (j >= 0 ) && (_dataTablero[x][j].colorGema == colorGema)  ){

      //Guardamos la posición de la gema
      gemasDestTemp[cont] = new CPos (x,j);
      cont++;

      //Siguiente gema
      --j;
    }

    //Hacia abajo
    j = y+1;
    while ( (j < ALTO_TABLERO) && (_dataTablero[x][j].colorGema == colorGema) ){

      //Guardamos la posición de la gema
      gemasDestTemp[cont] = new CPos(x,j);
      cont++;

      //Siguiente gema
      ++j;
    }

    //Actualizamos la lista de gemas si tenemos mas de 2 gemas del mismo color
    //que la que tratamos (hacen un total de tres)
    if(cont >= 2){
      ActualizaDestruidasParciales(x, y, cont, gemasDestTemp);
      hayGemasDestruidas = true;
    }

    ////////////////////////////////////////////////////////////////////
    //Diagonal principal (\) ///////////////////////////////////////////
    cont = 0;

    //izquierda arriba
    i = x - 1;
    j = y - 1;
    while ( (i >= 0) && (j >= 0) && (_dataTablero[i][j].colorGema == colorGema) ){

      //Guardamos la posición de la gema
      gemasDestTemp[cont] = new CPos(i, j);
      cont++;

      //Siguiente gema
      --i;
      --j;
    }

    //derecha abajo
    i = x + 1;
    j = y + 1;
    while ( (i < ANCHO_TABLERO) && (j < ALTO_TABLERO) && (_dataTablero[i][j].colorGema == colorGema)) {

      //Guardamos la posición de la gema
      gemasDestTemp[cont] = new CPos(i, j);
      cont++;

      //Siguiente gema
      ++i;
      ++j;
    }

    //Actualizamos la lista de gemas si tenemos mas de 3 iguales
    //Ademas hemos de marcarlas como movidas
    if(cont >= 2){
      ActualizaDestruidasParciales(x, y, cont, gemasDestTemp);
      hayGemasDestruidas = true;
    }

    ////////////////////////////////////////////////////////////////////
    //Diagonal secundaria (/) //////////////////////////////////////////
   cont = 0;

  //derecha arriba
    i = x + 1;
    j = y - 1;
    while ( (i < ANCHO_TABLERO) && (j >= 0) && (_dataTablero[i][j].colorGema == colorGema)) {

      //Guardamos la posición de la gema
      gemasDestTemp[cont] = new CPos(i, j);
      cont++;

      //Siguiente gema
      ++i;
      --j;
    }

  //izquierda abajo
    i = x - 1;
    j = y + 1;
    while (  (i >= 0) && (j < ALTO_TABLERO) &&
           (_dataTablero[i][j].colorGema == colorGema)) {

      //Guardamos la posición de la gema
      gemasDestTemp[cont] = new CPos(i, j);
      cont++;

      //Siguiente gema
      --i;
      ++j;
    }

    //Actualizamos la lista de gemas si tenemos mas de 2 gemas del mismo color
    //que la que tratamos (hacen un total de tres)
    if(cont >= 2){
      ActualizaDestruidasParciales(x, y, cont, gemasDestTemp);
      hayGemasDestruidas = true;
    }

    return hayGemasDestruidas;
  }

  /**
   *
   * @param x int
   * @param y int
   * @param cont int
   * @param gemasDestTemp CPos[]
   */
  private void ActualizaDestruidasParciales(int x, int y, int cont, CPos[] gemasDestTemp) {

     //Gema actual : sólo se mete una vez en la pila
     if (!_dataTablero[x][y].esDestruida) {
       //Gema inicial
       _dataTablero[x][y].esDestruida = true;
       _gemasDestruidas.addElement(new CPos(x, y));
     }

     //La gema actual se ha movido
     if ( y > _columnasMovidas[x])
       _columnasMovidas[x] = y;


     //Metemos el resto de gemas en el vector de destruidas
     for (int i = 0; i < cont; ++i) {
       //evita meter en el vector varias veces la misma posicion de gema
       if (!_dataTablero[gemasDestTemp[i].x][gemasDestTemp[i].y].esDestruida) {
         _dataTablero[gemasDestTemp[i].x][gemasDestTemp[i].y].esDestruida = true;
         _gemasDestruidas.addElement(gemasDestTemp[i]);
       }
       //Como éstas también se han movido procedemos de la misma manera que
       //en el caso anterior
       if (gemasDestTemp[i].y > _columnasMovidas[gemasDestTemp[i].x])
         _columnasMovidas[gemasDestTemp[i].x] = gemasDestTemp[i].y;
     }
   }

}
