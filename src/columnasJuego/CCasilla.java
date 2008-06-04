package columnasJuego;

/**
 * @author Ricardo
 * Creado el 04-ene-2005
 *
 * Representa una casilla del tablero.
 *
 * Cada casilla almacena el color de la gema y si ha de ser eliminada la gema
 * que contiene
 *
 */
class CCasilla {
  public boolean esDestruida;
  public short colorGema;

  CCasilla() {
    esDestruida = false;
    colorGema = CGemas.NONE;
  }

}
