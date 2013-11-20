/*
 * Copyright (C) 2009-2013 Mathias Doenitz, Alexander Myltsev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled2

import java.nio.charset.Charset

sealed abstract class ParserInput[T] {
  def elems(): Seq[T]
  def charAt(ix: Int): Char
  def length: Int
  def sliceString(start: Int, end: Int): String
  override def toString: String = sliceString(0, length)
  /**
   * In-place append
   * @param input sequence to append
   * @return
   */
  def append(input: Seq[T]): Unit

  /**
   * @param line starts at 1
   *
   * @return
   */
  def getLine(line: Int): String = toString.split('\n')(line - 1)
}

// bimorphic ParserInput implementation
// Note: make sure to not add another implementation, otherwise usage of this class
// might turn megamorphic at the call-sites thereby effectively disabling method inlining!
object ParserInput {
  val UTF8 = Charset.forName("UTF-8")

  private[ParserInput] class ParserBytesInput(bytes: Array[Byte], charset: Charset) extends ParserInput[Byte] {
    private[ParserBytesInput] var content: Array[Byte] = bytes.clone()

    def elems(): Seq[Byte] = content
    def charAt(ix: Int) = content(ix).toChar
    def length = content.length
    def sliceString(start: Int, end: Int) = new String(content, start, end - start, charset)
    def append(input: Seq[Byte]): Unit = content ++= input
  }

  implicit def apply(bytes: Array[Byte]): ParserInput[Byte] = apply(bytes, UTF8)
  def apply(bytes: Array[Byte], charset: Charset): ParserInput[Byte] = new ParserBytesInput(bytes, charset)

  private[ParserInput] class ParserCharsInput(string: String) extends ParserInput[Char] {
    private[ParserCharsInput] val content = new StringBuilder(string)
    def elems(): Seq[Char] = content
    def charAt(ix: Int) = content.charAt(ix)
    def length = content.length
    def sliceString(start: Int, end: Int) = content.substring(start, end)
    def append(input: Seq[Char]): Unit = content ++= input
  }

  implicit def apply(string: String): ParserInput[Char] = new ParserCharsInput(string)
  implicit def apply(chars: Array[Char]): ParserInput[Char] = apply(new String(chars))
}
