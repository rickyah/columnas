package columnasJuego;

public class MovimientosJuego {
  public boolean izquierda = false;
  public boolean derecha = false;
  public boolean caida = false;
  public boolean moverGemas = false;

//  public boolean teclaProcesada = true;
//  public boolean teclaLiberada = false;

  public MovimientosJuego(){
    ResetMovimientos();
  }

  public void ResetMovimientos(){

    izquierda = derecha = caida = moverGemas = false;
//    teclaProcesada = true;
//    teclaLiberada = false;
  }

}
