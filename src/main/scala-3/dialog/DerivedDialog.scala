package com.joshuapetersen.scala.scalafx.derivation
package dialog
import scalafx.Includes.*
import scalafx.scene.control.{ButtonType, Dialog, DialogPane, TextField}
import dialog.DialogDerivationTypes.*

import scalafx.beans.property.BooleanProperty

import scala.compiletime.summonAll


class DerivedDialog[A >: Null : Editor] private (mode: DerivedDialogMode = DerivedDialogMode.New, layouter: Layouter = Layouter.Default, value0: Option[A] = None) extends Dialog[A]:
  val editor = summon[Editor[A]]


  dialogPane = new DialogPane:
    headerText = s"$mode ${value0.getOrElse("").getClass.getSimpleName}"
    this.content = layouter.layout(editor.container(""))
    buttonTypes ++= Seq(
      ButtonType.OK,
      ButtonType.Cancel
    )

  resultConverter = buttonType =>
    if buttonType == ButtonType.OK then
      editor.getValue
    else
      null

  // go through all editors and see if any are required, if so, check if they have a value

  


  value0 foreach editor.setValue

end DerivedDialog

object DerivedDialog:

  def createDerivedDialog[A >: Null : Editor](a: A): DerivedDialog[A] = new DerivedDialog[A](value0 = Some(a))

export DerivedDialog.createDerivedDialog
