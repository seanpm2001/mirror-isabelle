/*  Title:      Tools/Graphview/src/visualizer.scala
    Author:     Markus Kaiser, TU Muenchen

Graph visualization interface.
*/

package isabelle.graphview


import isabelle._


import java.awt.{Font, FontMetrics, Color => JColor, Shape, RenderingHints, Graphics2D, Toolkit}
import javax.swing.JComponent


class Visualizer(val model: Model)
{
  visualizer =>

  val parameters = new Parameters

  object Coordinates
  {
    private var layout = Layout_Pendulum.empty_layout

    def apply(k: String): (Double, Double) =
      layout.nodes.get(k) match {
        case Some(c) => c
        case None => (0, 0)
      }

    def apply(e: (String, String)): List[(Double, Double)] =
      layout.dummies.get(e) match {
        case Some(ds) => ds
        case None => Nil
      }

    def reposition(k: String, to: (Double, Double))
    {
      layout = layout.copy(nodes = layout.nodes + (k -> to))
    }

    def reposition(d: ((String, String), Int), to: (Double, Double))
    {
      val (e, index) = d
      layout.dummies.get(e) match {
        case None =>
        case Some(ds) =>
          layout = layout.copy(dummies =
            layout.dummies + (e ->
              (ds.zipWithIndex :\ List.empty[(Double, Double)]) {
                case ((t, i), n) => if (index == i) to :: n else t :: n
              }))
      }
    }

    def translate(k: String, by: (Double, Double))
    {
      val ((x, y), (dx, dy)) = (Coordinates(k), by)
      reposition(k, (x + dx, y + dy))
    }

    def translate(d: ((String, String), Int), by: (Double, Double))
    {
      val ((e, i),(dx, dy)) = (d, by)
      val (x, y) = apply(e)(i)
      reposition(d, (x + dx, y + dy))
    }

    def update_layout()
    {
      layout =
        if (model.current.is_empty) Layout_Pendulum.empty_layout
        else {
          val max_width =
            model.current.entries.map({ case (_, (info, _)) =>
              font_metrics.stringWidth(info.name).toDouble }).max
          val box_distance = max_width + parameters.pad_x + parameters.gap_x
          def box_height(n: Int): Double =
            ((font_metrics.getAscent + font_metrics.getDescent + parameters.pad_y) * (5 max n))
              .toDouble
          Layout_Pendulum(model.current, box_distance, box_height)
        }
    }

    def bounds(): (Double, Double, Double, Double) =
      model.visible_nodes().toList match {
        case Nil => (0, 0, 0, 0)
        case nodes =>
          val X: (String => Double) = (n => apply(n)._1)
          val Y: (String => Double) = (n => apply(n)._2)

          (X(nodes.minBy(X)), Y(nodes.minBy(Y)),
           X(nodes.maxBy(X)), Y(nodes.maxBy(Y)))
      }
  }

  object Drawer
  {
    def apply(g: Graphics2D, n: Option[String])
    {
      n match {
        case None =>
        case Some(_) => Shapes.Growing_Node.paint(g, visualizer, n)
      }
    }

    def apply(g: Graphics2D, e: (String, String), head: Boolean, dummies: Boolean)
    {
      Shapes.Cardinal_Spline_Edge.paint(g, visualizer, e, head, dummies)
    }

    def paint_all_visible(g: Graphics2D, dummies: Boolean)
    {
      g.setFont(font)

      g.setRenderingHints(rendering_hints)

      model.visible_edges().foreach(e => {
          apply(g, e, parameters.arrow_heads, dummies)
        })

      model.visible_nodes().foreach(l => {
          apply(g, Some(l))
        })
    }

    def shape(g: Graphics2D, n: Option[String]): Shape =
      n match {
        case None => Shapes.Dummy.shape(g, visualizer, None)
        case Some(_) => Shapes.Growing_Node.shape(g, visualizer, n)
      }
  }

  object Selection
  {
    private var selected: List[String] = Nil

    def apply() = selected
    def apply(s: String) = selected.contains(s)

    def add(s: String) { selected = s :: selected }
    def set(ss: List[String]) { selected = ss }
    def clear() { selected = Nil }
  }

  object Color
  {
    def apply(l: Option[String]): (JColor, JColor, JColor) =
      l match {
        case None => (JColor.GRAY, JColor.WHITE, JColor.BLACK)
        case Some(c) => {
            if (Selection(c))
              (JColor.BLUE, JColor.GREEN, JColor.BLACK)
            else
              (JColor.BLACK, model.colors.getOrElse(c, JColor.WHITE), JColor.BLACK)
        }
      }

    def apply(e: (String, String)): JColor = JColor.BLACK
  }

  object Caption
  {
    def apply(key: String) = model.complete.get_node(key).name
  }


  /* font rendering information */

  val font = new Font(parameters.font_family, Font.BOLD, parameters.font_size)
  val font_metrics: FontMetrics = Toolkit.getDefaultToolkit.getFontMetrics(font)

  val rendering_hints =
    new RenderingHints(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON)
}