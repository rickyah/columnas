/**
 * @autor Ricardo
 *
 * Clase que gestiona una lista de selecci�n y la presenta por pantalla
 */

package columnasMenu;

import java.util.*;
import javax.microedition.lcdui.*;

import com.nokia.mid.ui.*;

public class CTextChoice {
  private int seleccion;
  private Vector listaOpciones;
  private Font fuente;
  private Image imgFlecha = null;

  public CTextChoice() {
    //La seleccion inicial es la primera
    seleccion = 0;

    //creamos el vector con la lista
    listaOpciones = new Vector(5);

    //Escogemos la fuente y el tama�o.
    fuente = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);

    //Cargamos la imagen con la flecha de seleccion
    try {

      imgFlecha = Image.createImage("/res/flechaSeleccion.png");
    }
    catch (Exception e) {
      //Recogemos info si hay fallos
      System.err.println("Error E/S: " + e.getMessage());
      e.printStackTrace();
      imgFlecha = null;
    }

  }

  //A�ade un elemento a la lista de opciones
  public void Add(String s) {
    listaOpciones.addElement(s);
  }

  //Seleccionamos el siguiente
  public void Next() {
    if (seleccion < listaOpciones.size() - 1) {
      seleccion++;
    }

  }

  //Seleccionamos el anterior
  public void Last() {
    if (seleccion > 0) {
      seleccion--;
    }

  }

  //Retorna la selecci�n actual.
  public String getSeleccion() {
    return (String) listaOpciones.elementAt(seleccion);
  }

  //Dibuja la lista de selecci�n por pantalla
  public void Draw(Graphics g, int x, int y) {

    //Cambiamos el tipo de fuente
    g.setFont(fuente);

    //Tama�o de la fuente
    int altoFuente = fuente.getHeight();


    //Dibujamos la flecha de selecci�n:
    if (imgFlecha != null) {

      int altoFlecha = imgFlecha.getHeight();
      int anchoFlecha = imgFlecha.getWidth();


      g.drawImage(imgFlecha,
                  x,
                  y + (seleccion * altoFuente),
                  Graphics.HCENTER | Graphics.VCENTER
          );

      //Dibujamos los textos de la lista
      for (int i = 0; i < listaOpciones.size(); ++i) {
        //separacion del texto respecto a la flecha 5 pixels
        g.drawString( (String) listaOpciones.elementAt(i),
                     x + anchoFlecha,
                     y + (i * (altoFuente)) - (altoFuente / 2),  //+2 == ajuste texto pues se alinea con baseLine
                     Graphics.LEFT | Graphics.TOP
            );
      }

    }

    //Sino hay imagen dibujamos un simple tri�ngulo
    else {
      //Para dibujar el tri�ngulo debemos usar un objeto espec�fico de Nokia
      DirectGraphics dg = DirectUtils.getDirectGraphics(g);


      //Dibujamos el tri�ngulo que marca la seleccion actual
      dg.fillTriangle(x,
                      y + seleccion * altoFuente - 5,

                      x + (altoFuente / 2) - 1,
                      y + seleccion * altoFuente - 2,

                      x,
                      y + seleccion * altoFuente + 2,

                      0xFF000000 | g.getColor()
          );

      //Dibujamos los textos de la lista
      for (int i = 0; i < listaOpciones.size(); ++i) {
        //separacion del texto respecto al tri�ngulo (ejeX) 5 pixels
        g.drawString( (String) listaOpciones.elementAt(i),
                     x + (altoFuente / 2) + 5,
                     y + (i * altoFuente),
                     Graphics.LEFT | Graphics.BASELINE
            );
      }

    }
  }

}
