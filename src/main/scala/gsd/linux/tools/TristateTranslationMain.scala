/*
 * This file is part of the Linux Variability Modeling Tools (LVAT).
 *
 * Copyright (C) 2010 Steven She <shshe@gsd.uwaterloo.ca>
 *
 * LVAT is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * LVAT is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LVAT.  (See files COPYING and COPYING.LESSER.)  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package gsd.linux.tools

import gsd.linux._

import java.io.{InputStreamReader, PrintStream}
import org.clapper.argot.{ArgotConverters, ArgotUsageException}
import util.logging.ConsoleLogger

/**
 * Outputs the boolean translation of a Kconfig extract. 
 *
 * @author Steven She (shshe@gsd.uwaterloo.ca)
 */
object TristateTranslationMain extends ArgotUtil with ConsoleLogger {

  val name = "TristateTranslationMain"

  import ArgotConverters._

  val inParam = parser.parameter[String](
    "in-file", "input Kconfig extract (.exconfig) file, stdin if not specified.", true)

  val outParam = parser.parameter[String](
    "out-file", "output file to write boolean expressions, stdout if not specified.", true)

  def main(args: Array[String]) {

    try {
      parser.parse(args)

      val k =
      (pOpt.value, inParam.value) match {
        case (Some(_), Some(_)) =>
          parser.usage("Either a project (-p) is specified or input & output parameters are used.")

        case (Some(p), None) => p.exconfig

        case (None, Some(f)) =>
          log("Reading Kconfig extract from file...")
          KConfigParser.parseKConfigFile(f)

        case (None, None) =>
          log("Using stdin as input...")
          KConfigParser.parseKConfig(new InputStreamReader(System.in))
      }

      val output =
      (pOpt.value, outParam.value) match {
        case (Some(p), None) => new PrintStream(p.boolFile.get)
        case (None, Some(f)) => new PrintStream(f)
        case _ => System.out
      }

      execute(k, output)
    }
    catch {
      case e: ArgotUsageException => println(e.message)
    }
  }

  def execute(k: ConcreteKConfig, out: PrintStream) {
    //First output identifiers
    for (id <- k.identifiers) {
      out.println("@ " + id)
      out.println("@ " + id + "_m")
    }

    val trans = new TristateTranslation(k)
    val exprs = trans.translate

    for (id <- trans.generated) out.println("$ " + id)
    for (e  <- exprs) out.println(e)
  }
}