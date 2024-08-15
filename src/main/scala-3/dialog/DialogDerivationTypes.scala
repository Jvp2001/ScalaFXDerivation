package com.joshuapetersen.scala.scalafx.derivation
package dialog

import scalafx.scene.Node
import scalafx.scene.control.{CheckBox, DatePicker, Label, Labeled, TextField, TitledPane}
import scalafx.scene.layout.VBox

import scala.compiletime.summonAll
import scala.quoted.Type


object DialogDerivationTypes:

  import java.time.LocalDate
  import scala.compiletime.constValueTuple
  import scala.deriving.Mirror


  enum Container(label: String):
    case SinglePrimitive(label: String, node: Node) extends Container(label)
    case MultiPrimitive(label: String, nodes: Seq[Node]) extends Container(label)
    case Composite(label: String, containers: Seq[Container]) extends Container(label)

  trait Editor[T]:
    def uiComponent: Node
    def getValue: T

    def setValue(value: T): Unit

    def container(label: String): Container

    def isRequired: Boolean = false

  object Editor:

    import Container.*

    trait FreshInstance

    object FreshInstance:


      given FreshInstance = new FreshInstance {}

    end FreshInstance

    given (using FreshInstance): Editor[String] with
      val textField = new TextField
      
      override def uiComponent = textField

      override def getValue: String = textField.getText

      override def setValue(x: String): Unit = textField.text = x

      override def container(label: String): Container = SinglePrimitive(label, textField)

      override def isRequired: Boolean = true

    given (using FreshInstance): Editor[Int] with
      val textField = new TextField

      override def uiComponent = textField
      
      override def getValue: Int = textField.getText.toInt

      override def setValue(x: Int): Unit = textField.text = x.toString

      override def container(label: String): Container = SinglePrimitive(label, textField)

      override def isRequired: Boolean = true

    given (using FreshInstance): Editor[Float] with
      val textField = new TextField
      
      override def uiComponent = textField
      
      override def getValue: Float = textField.getText.toFloat

      override def setValue(x: Float): Unit = textField.text = x.toString

      override def container(label: String): Container = SinglePrimitive(label, textField)

      override def isRequired: Boolean = true

    given (using FreshInstance): Editor[Double] with
      val textField = new TextField

      override def uiComponent = textField
      
      override def getValue: Double = textField.getText.toDouble

      override def setValue(x: Double): Unit = textField.text = x.toString

      override def container(label: String): Container = SinglePrimitive(label, textField)

      override def isRequired: Boolean = true

    given (using FreshInstance): Editor[LocalDate] with
      val datePicker = new DatePicker

      override def uiComponent = datePicker
      
      override def getValue: LocalDate = datePicker.getValue

      override def setValue(x: LocalDate): Unit = datePicker.setValue(x)

      override def container(label: String): Container = SinglePrimitive(label, datePicker)

    given (using FreshInstance): Editor[Boolean] with
      val checkBox = new CheckBox
      
      override def uiComponent = checkBox

      override def getValue: Boolean = checkBox.isSelected

      override def setValue(x: Boolean): Unit = checkBox.selected = x

      override def container(label: String): Container = SinglePrimitive(label, checkBox)
    end given

    given[A : Editor] (using FreshInstance)(using editor: Editor[A]): Editor[Option[A]] with
      override def getValue: Option[A] = Some(editor.getValue)

      override def setValue(value: Option[A]): Unit = value foreach editor.setValue

      override def container(label: String): Container = editor.container(label)

      override def isRequired: Boolean = false
      
      override def uiComponent: Node = editor.uiComponent
    end given


    inline given [A <: Product](using m: Mirror.ProductOf[A]): Editor[A] =
      new Editor[A]:
        val labels = constValueTuple[m.MirroredElemLabels].toList.asInstanceOf[List[String]]
        type ElemEditors = Tuple.Map[m.MirroredElemTypes, Editor]
        val elemEditors = summonAll[ElemEditors].toList.asInstanceOf[List[Editor[Any]]]
        val containers = labels.zip(elemEditors) map : (label, editor) =>
          editor.container(label)

        override def getValue: A =
          val elems = elemEditors.map(_.getValue)
          val tuple = elems.foldRight[Tuple](EmptyTuple)(_ *: _)
          m.fromProduct(tuple)

        override def setValue(a: A): Unit =
          val elems = a.productIterator.toList
          elems.zip(elemEditors) foreach : (elem, editor) =>
            editor setValue elem

        override def container(label: String): Container = Container.Composite(label, containers)

        override def uiComponent: Node = VBox(5, containers.map(e => e.productIterator.toList.head.asInstanceOf[Editor[Any]].uiComponent) *)
  end Editor

  trait Layouter:
    def layoutSinglePrimitive(primitive: Container.SinglePrimitive): Node

    def layoutMultiPrimitive(primitive: Container.MultiPrimitive): Node

    def layoutComposite(composite: Container.Composite, isTopLevel: Boolean): Node

    final def layout(container: Container, isTopLevel: Boolean = true): Node = container match
      case singlePrimitive: Container.SinglePrimitive => layoutSinglePrimitive(singlePrimitive)
      case multiPrimitive: Container.MultiPrimitive => layoutMultiPrimitive(multiPrimitive)
      case Container.Composite(label, containers) => layoutComposite(Container.Composite(label, containers), isTopLevel)

  object Layouter:

    object Default extends Layouter:
      override def layoutSinglePrimitive(primitive: Container.SinglePrimitive): Node =
        primitive.node match
          case labeled: Labeled =>
            labeled.text = primitive.label
            labeled
          case other =>
            VBox(5, new Label(primitive.label.fromCamelCase), other)

      override def layoutMultiPrimitive(primitive: Container.MultiPrimitive): Node =
        VBox(5, new Label(primitive.label.fromCamelCase) +: primitive.nodes *)

      override def layoutComposite(composite: Container.Composite, isTopLevel: Boolean): Node =
        val vbox = new VBox(10, composite.containers.map(c => layout(c, isTopLevel = false)) *)
        if isTopLevel then vbox
        else new TitledPane:
          text = composite.label.fromCamelCase
          content = vbox
          collapsible = false
          expanded = true
    end Default


end DialogDerivationTypes





