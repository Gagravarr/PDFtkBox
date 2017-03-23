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
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * PDFtk bookmark replacement, powered by Apache PDFBox
 */
public class PDFtkBox {
   public static void main(String[] args) throws Exception {
      // For printing help
      Options optsHelp = new Options();
      optsHelp.addOption(
            Option.builder("help")
            .required()
            .desc("print this message")
            .build()
      );
      
      // Normal-style import/export
      Options optsNormal = new Options();
      OptionGroup normal = new OptionGroup();
      Option optExport =
            Option.builder("export")
            .required()
            .hasArg()
            .desc("export bookmarks from pdf" )
            .argName("pdf").build();
      normal.addOption(optExport);
      Option optImport = 
            Option.builder("import")
            .required()
            .hasArg()
            .desc("import bookmarks to pdf" )
            .argName("pdf").build();
      normal.addOption(optImport);
      optsNormal.addOptionGroup(normal);
      Option optBookmarks = 
            Option.builder("bookmarks")
            .hasArg()
            .desc("bookmarks definition file" )
            .argName("bookmarks").build();
      optsNormal.addOption(optBookmarks);
      
      // TODO PDFtk style options

      
      // What are we doing?
      CommandLineParser parser = new DefaultParser();
      
      // Did they want help?
      try {
         parser.parse(optsHelp, args);
         
         // If we get here, they asked for help
         doPrintHelp(optsHelp, optsNormal);
         return;
      } catch (ParseException pe) {}
      
      // Normal-style import/export?
      try {
         CommandLine line = parser.parse(optsNormal, args);
         
         if (line.hasOption(optExport.getOpt())) {
            doExport( line.getOptionValue(optExport.getOpt()), line.getArgs() );
            return;
         }
         if (line.hasOption(optImport.getOpt())) {
            doImport( line.getOptionValue(optExport.getOpt()), 
                      line.getOptionValue(optBookmarks.getOpt()), 
                      line.getArgs() );
            return;
         }
      } catch (ParseException pe) {}

      // TODO Rest
      
      doPrintHelp(optsHelp, optsNormal);
   }
   
   protected static void doPrintHelp(Options optsHelp, Options optsNormal) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("PDFtkBox", optsNormal);
      formatter.printHelp("PDFtkBox", optsHelp);
   }
   
   protected static void doExport(String pdf, String[] args) throws IOException {
      Bookmarks bm = new Bookmarks(new File(pdf));
      if (args.length == 0) {
         System.out.println(bm.getBookmarks());
      } else {
         // TODO Write to the file
         System.err.println("TODO - Write to " + args[0]);
      }
      bm.close();
   }
   protected static void doImport(String pdf, String bookmarks, String[] args) {
      // TODO
      System.err.println("TODO Import");
   }
}