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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

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
      
      // PDFtk style options
      Options optsPDFtk = new Options();
      OptionGroup pdftk = new OptionGroup();
      Option optDumpData =
            Option.builder("dump_data")
            .required()
            .hasArg()
            .desc("dump bookmarks from pdf" )
            .argName("pdf").build();
      pdftk.addOption(optDumpData);
      Option optUpdateInfo = 
            Option.builder("update_info")
            .required()
            .hasArg()
            .desc("update bookmarks in pdf" )
            .argName("pdf").build();
      pdftk.addOption(optUpdateInfo);
      optsPDFtk.addOptionGroup(pdftk);
      Option optOutput = 
            Option.builder("output")
            .hasArg()
            .desc("output to file" )
            .argName("file").build();
      optsPDFtk.addOption(optOutput);

      
      // What are we doing?
      CommandLineParser parser = new DefaultParser();


      // Did they want help?
      try {
         parser.parse(optsHelp, args);
         
         // If we get here, they asked for help
         doPrintHelp(optsHelp, optsNormal, optsPDFtk);
         return;
      } catch (ParseException pe) {}


      // Normal-style import/export?
      try {
         CommandLine line = parser.parse(optsNormal, args);
         
         if (line.hasOption(optExport.getOpt())) {
            doExport( line.getOptionValue(optExport.getOpt()), 
                      line.getOptionValue(optBookmarks.getOpt()), 
                      line.getArgs() );
            return;
         }
         if (line.hasOption(optImport.getOpt()) && line.getArgs().length > 0) {
            doImport( line.getOptionValue(optImport.getOpt()),
                      line.getOptionValue(optBookmarks.getOpt()), 
                      line.getArgs() );
            return;
         }
      } catch (ParseException pe) {}


      // Nobble things for PDFtk-style options and Commons CLI
      if (args.length > 2) {
         for (int i=0; i<args.length; i += 2) {
            for (Option opt : optsPDFtk.getOptions()) {
               if (args[i].equals(opt.getOpt())) {
                  args[i] = "-" + args[i];
               }
            }
         }
      }
      try {
         CommandLine line = parser.parse(optsPDFtk, args);
         
         if (line.hasOption(optDumpData.getOpt())) {
            doExport( line.getOptionValue(optDumpData.getOpt()), 
                      line.getOptionValue(optOutput.getOpt()), 
                      line.getArgs() );
            return;
         }
         if (line.hasOption(optUpdateInfo.getOpt()) && line.getArgs().length > 0) {
            doImport( line.getOptionValue(optUpdateInfo.getOpt()),
                      line.getOptionValue(optOutput.getOpt()), 
                      line.getArgs() );
            return;
         }
      } catch (ParseException pe) {}


      // If in doubt, print help
      doPrintHelp(optsHelp, optsNormal, optsPDFtk);
   }
   
   protected static void doPrintHelp(Options optsHelp, Options optsNormal, Options optsPDFtk) {
      // Some general stuff
      System.out.println("Imports or Exports Bookmarks from a PDF file");
      System.out.println();
      
      // Output the normal help
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("PDFtkBox", optsNormal, true);
      System.out.println();
      
      // Output the PDFtk-style help
      formatter.setOptPrefix("");
      formatter.printHelp("PDFtkBox", optsPDFtk, true);
      
      // Ignore the opts help
   }
   
   protected static void doExport(String pdf, String bookmarks, String[] args) throws IOException {
      Bookmarks bm = new Bookmarks(new File(pdf));
      
      File bmf = null;
      if (bookmarks != null) {
         bmf = new File(bookmarks);
      } else if (args.length > 0) {
         bmf = new File(args[0]);
      }
      
      PrintWriter output;
      if (bmf == null) {
         output = new PrintWriter(System.out);
      } else {
         output = new PrintWriter(bmf, "UTF-8");
      }
      
      bm.exportBookmarks(output);
      output.close();
      bm.close();
   }
   protected static void doImport(String pdf, String bookmarks, String[] args) throws IOException {
      Bookmarks bm = new Bookmarks(new File(pdf));

      InputStream istream;
      if (bookmarks != null) {
         istream = new FileInputStream(new File(bookmarks));
      } else {
         istream = System.in;
      }
      BufferedReader input = new BufferedReader(new InputStreamReader(istream,"UTF-8"));

      bm.importBookmarks(input, new File(args[0]));
      
      input.close();
      bm.close();
   }
}