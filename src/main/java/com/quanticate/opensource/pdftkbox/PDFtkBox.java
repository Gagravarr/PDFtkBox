/* ====================================================================
  Copyright 2017 Quanticate Ltd

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
==================================================================== */
package com.quanticate.opensource.pdftkbox;

import java.io.File;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * PDFtk bookmark replacement, powered by Apache PDFBox
 */
public class PDFtkBox {
   public static void main(String[] args) throws Exception {
      Options options = new Options();
      
      // TODO Different options for different use-cases
      
      options.addOption(new Option("help", "print this message"));
      options.addOption(
            Option.builder("import")
            .hasArg()
            .numberOfArgs(2)
            .desc("import bookmarks from odf" )
            .argName("pdf").build()
      );
      options.addOption(
            Option.builder("export")
            .hasArg()
            .desc("export bookmarks from pdf" )
            .argName("pdf").build()
      );
      
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp( "PDFtkBox", options );
      
      // TODO Do this properly
      if (args.length >= 2) {
         Bookmarks bm = new Bookmarks(new File(args[1]));
         System.out.println(bm.getBookmarks());
         bm.close();
      }
   }
}
