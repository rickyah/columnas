package columnasJuego;

import java.util.*;

/**
 * @author Ricardo
 * Creado el 02-ene-2005
 *
 * Clase que representa el bloque de tres gemas que controlará el jugador
 *
 * También se utilizará para representar el bloque de tres piezas
 * que aparecerá en la jugada siguiente (next)
 *
 */
public class CBloquePieza {

  //número de gemas por bloque
  public final static short NUM_GEMS = 3;

  //Posición del bloque.
  public CPos pos;

  //Posicion anterior del bloque
  public CPos posAnt;

  //Guarda el color de las tres gemas que contiene el bloque
  public short[] gemsColor;

  //índice de la primera
  public int indexGem;

  //Referencia a las gemas del juego
  private CGemas gemsRef;

  //objeto para crear números aleatorios
  private Random rndObj;

  /*
   * constructor
   */
  CBloquePieza(CGemas gm) {

    gemsRef = gm;

    //indice a cero
    indexGem = 0;

    gemsColor = new short[NUM_GEMS];

    //Iniciamos el valor de la posición a un valor neutral
    pos = new CPos();
    posAnt = new CPos();

    //Creamos las gemas que contiene el bloque
    this.GenerarGemas();

  }

  /*
   * Genena un bloque de 3 gemas aleatoriamente
   */
  public void GenerarGemas() {

    rndObj = new Random(System.currentTimeMillis());

    //Generamos el color de las piezas al azar:
    for (int i = 0; i < NUM_GEMS; ++i) {
      gemsColor[i] = (short) (Math.abs( (rndObj.nextInt() % 6)) + 1);
    }

    indexGem = 0;
  }

  /*
   * Intercambia la posición de las tres gemas
   */
  public void MoverGemas() {

    indexGem++;
    if (indexGem >= NUM_GEMS) {
      indexGem = 0;
    }

  }

  public void MoverAbajo() {
    posAnt.x = pos.x;
    posAnt.y = pos.y;
    pos.y++;
  }

  public void MoverIzquierda() {
    posAnt.x = pos.x;
    pos.x--;
  }

  public void MoverDerecha() {
    posAnt.x = pos.x;
    pos.x++;

  }

  public void initPlayerPos() {
    pos.x = posAnt.x = 3;
    pos.y = posAnt.y = 3;
  }

  /**
   * copyData
   *
   * @param bloqueNext CBloquePieza
   */
  public void copyData(CBloquePieza bloque) {

    //copiamos el color de las gemas
    for (int i = 0; i < NUM_GEMS; ++i) {
      gemsColor[i] = bloque.gemsColor[i];
    }

    //copiamos el índice de la primera gema
    indexGem = bloque.indexGem;

  }

}
