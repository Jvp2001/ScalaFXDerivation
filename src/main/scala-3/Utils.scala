package com.joshuapetersen.scala.scalafx.derivation

import scala.annotation.tailrec
import scala.quoted.{Expr, Quotes, Type}

extension(s: String)
  def fromCamelCase: String =
    val length = s.length

    @tailrec
    def process(currIndex: Int, prevWasLower: Boolean, wasBoundary: Boolean, acc: List[Char]): String =
      if currIndex >= length then acc.reverse.mkString
      else
        val curr = s.charAt(currIndex)
        val nextIndex = currIndex + 1
        val nextIsLower = (nextIndex < length) && s.charAt(nextIndex).isLower
        val isBoundary = curr.isUpper && (prevWasLower || nextIsLower)
        val isDelimitation = isBoundary && !wasBoundary
        val out =
          if currIndex == 0 then curr.toUpper
          else if isDelimitation then ' '
          else if isBoundary && nextIsLower then curr.toLower
          else curr
        process(if !isDelimitation then nextIndex else currIndex + 1, curr.isLower, isBoundary, out :: acc)

    process(0, false, false, Nil)
