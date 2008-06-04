package columnasJuego;

import javax.microedition.lcdui.*;

/**
 * @author Ricardo
 * Creado el 02-ene-2005
 *
 * Representa las gemas del juego de forma num�rica, adem�s almacena sus im�genes.
 */
public class CGemas {

  public final static short NONE = 0;
  public final static short ROJO = 1;
  public final static short AZUL = 2;
  public final static short VERDE = 3;
  public final static short AMARILLO = 4;
  public final static short VIOLETA = 5;
  public final static short GRIS = 6;

  public Image[] gemImages;

  //constructor
  CGemas() {
    //Creamos un vector de 7 elementos para las 6 gemas y el fondo
    gemImages = new Image[7];
    try {
      gemImages[0] = Image.createImage("/res/bloque_fondo.png");
      gemImages[1] = Image.createImage("/res/gema_roja.png");
      gemImages[2] = Image.createImage("/res/gema_azul.png");
      gemImages[3] = Image.createImage("/res/gema_verde.png");
      gemImages[4] = Image.createImage("/res/gema_amarilla.png");
      gemImages[5] = Image.createImage("/res/gema_violeta.png");
      gemImages[6] = Image.createImage("/res/gema_gris.png");
    }
    catch (Exception e) {
      //Recogemos info si hay fallos
      System.err.println("Error E/S: " + e.getMessage());
      e.printStackTrace();
    }
  }

}
