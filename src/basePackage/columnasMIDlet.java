package basePackage;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import columnasMenu.*;
import columnasJuego.*;

/**
 * @author Ricardo
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class columnasMIDlet  extends MIDlet {

  //Dispositivo movil
  private Display display;

  //Hilo que ejecutará el menu
  private Thread hiloMenu;

  //Hilo que ejecutará el juego
  private Thread hiloJuego;

  //canvas para el menu
  private CMenuCanvas menuCanvas;

  //canvas para el juego
  private CJuegoCanvas juegoCanvas;


  //Constructor
  public columnasMIDlet() {
    //Obtenemos el display
    display = Display.getDisplay(this);


    //Creamos los canvas del juego
    menuCanvas = new CMenuCanvas(this);
    juegoCanvas = new CJuegoCanvas(this);

  }

  /* (non-Javadoc)
   * @see javax.microedition.midlet.MIDlet#startApp()
   */
  public void startApp() throws MIDletStateChangeException {

   //Iniciamos el menu
    IniciarMenu(0);
  }

  /* (non-Javadoc)
   * @see javax.microedition.midlet.MIDlet#pauseApp()
   */
  public void pauseApp() {
    if (juegoCanvas.isRunning())
      juegoCanvas.Pause();
  }

  /* (non-Javadoc)
   * @see javax.microedition.midlet.MIDlet#destroyApp(boolean)
   */
  public void destroyApp(boolean arg0) throws MIDletStateChangeException {

    notifyDestroyed();
  }

  /**
   * Lanza el hilo del menu
   */
  public void IniciarMenu(int puntuacion){
      //Limpiamos recursos
     System.gc();

     //Establecemos el canvas a visualizar
     display.setCurrent(menuCanvas);

     //Iniciamos recursos del menu
    menuCanvas.Init();

    if (puntuacion > 0)
      menuCanvas.setPuntuacionUltimaPartida(puntuacion);

    //Ejecutamos el hilo
    new Thread(menuCanvas).start();
  }



  /**
   * Inicia el hilo del juego
   */
  public void IniciarJuego(){
    //Limpiamos recuross
    System.gc();

    //Establecemos la dificultad
    juegoCanvas.setDificultad( menuCanvas.getDificultad() );

    //Si el juego ya estaba en ejecución
    if ( !juegoCanvas.isRunning() ){

        //Establecemos el canvas a visualizar
      display.setCurrent(juegoCanvas);

        //iniciamos recursos
      juegoCanvas.Init();



      //Ejecutamos el hilo
      new Thread(juegoCanvas).start();
    }

    else {
        //Establecemos el canvas a visualizar
        display.setCurrent(juegoCanvas);
        //Continuamos con el juego
        juegoCanvas.Resume();
    }
  }

  /**
   * Realiza una espera activa
   * @param milisegundos int milisegundos de espera
   */
  public void WaitTime(int milisegundos) {
    long initTime = System.currentTimeMillis();
    long elapsedTimeT = 0;
    long temp;

    while (elapsedTimeT < milisegundos) {
      temp = System.currentTimeMillis();
      elapsedTimeT += temp - initTime;
      initTime += temp - initTime;
    }
  }

}

