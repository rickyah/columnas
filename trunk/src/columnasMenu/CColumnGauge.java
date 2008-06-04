/**
 * @autor Ricardo
 *
 * Clase que gestiona una barra de estados formada por bitmaps.
 */
package columnasMenu;

import javax.microedition.lcdui.*;

/**
 * @author Ricardo
 * Creado el 03-ene-2005
 */
public class CColumnGauge {

  //Imagenes que forma la barra
  private Image dif_bloque, dif_izq, dif_der;

  //valor actual de la barra
  private int value;

  //L�mite de valores que puede tomar
  private final short MAX_VALUE = 9;
  private final short MIN_VALUE = 0;

  //ancho -en pixels- del bloque izquierdo, derecho y central respectivamente
  private int dif_izq_width, dif_der_width, bloque_width;

  //ancho total del widget
  private int total_width;

  //alto total del widget
  private int total_height;

  CColumnGauge(int value) {
    Init(value);
  }

  /**
   * Inicializa la clase
   * @param v int
   */
  public void Init(int v) {
      //Fijamos el valor pasado como par�metro como valor de la barra
  value = v;

  //Cargamos las im�genes
  try {

    dif_bloque = Image.createImage("/res/dif_bloq.png");
    dif_izq = Image.createImage("/res/dif_extr_izq.png");
    dif_der = Image.createImage("/res/dif_extr_der.png");
  }
  catch (Exception e) {
    //Recogemos info si hay fallos
    System.err.println("Error E/S: " + e.getMessage());
    e.printStackTrace();
  }

  //Anchura del bloque de dificultad
  bloque_width = dif_bloque.getWidth();

  //Anchura de los extremos
  dif_izq_width = dif_izq.getWidth();
  dif_der_width = dif_der.getWidth();

  //Ancho total
  total_width = dif_izq_width + dif_der_width + (bloque_width * MAX_VALUE);

  //Alto total
  if (dif_izq.getHeight() > dif_der.getWidth()) {
    total_height = dif_izq.getHeight();
  }
  else {
    total_height = dif_der.getHeight();
  }

  value = 0;

  }

  /**
   *
   */
  public void Destroy(){

  }

  //retorna el ancho total que puede ocupar el widget en p�xels
  public int getWidth() {

    return total_width;
  }

  //retorna el alto maximo que puede ocupar el widget en p�xels
  public int getHeight() {
    return total_height;
  }

  //metodo que dibuja el selector en una posici�n determinada
  public void drawGauge(Graphics g, int x, int y) {

    //Dibujamos las im�genes de los extremos
    g.drawImage(dif_izq,
                x,
                y,
                Graphics.TOP | Graphics.LEFT);

    g.drawImage(dif_der,
                x + dif_izq_width + (bloque_width * MAX_VALUE),
                y,
                Graphics.TOP | Graphics.LEFT);

    //Dibujamos cada uno de los bloques que representan la dificultad
    for (int i = 0; i < value; ++i) {
      g.drawImage(dif_bloque,
                  (i * bloque_width) + x + dif_izq_width,
                  y + 3,
                  Graphics.TOP | Graphics.LEFT);
    }

  }

  //Incrementamos
  public void incValue() {
    if (value < MAX_VALUE) {
      value++;
    }
  }

  //Decrementamos
  public void decValue() {
    if (value > MIN_VALUE) {
      value--;
    }

  }

  public int getValue() {
    return value;
  }
  public void setValue(int v) {
    //comprobamos l�mites
    if (v > MAX_VALUE)
      value = MAX_VALUE;
    else if (v < MIN_VALUE)
      value = MIN_VALUE;

    //Establecemos valor
    else value = v;

    return;
  }
}
