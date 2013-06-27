package net.minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;

public class MinecraftLauncher
{
  private static final long MIN_HEAP = 511L;
  private static final long RECOMMENDED_HEAP = 1024L;
  private static boolean debugMode = false;

  public static void main(String[] args) throws Exception
  {
    if ((args.length > 0) && (args[0].contains("debug"))) {
      debugMode = true;
    }

    long heapSizeMegs = Runtime.getRuntime().maxMemory() / 1024L / 1024L;

    if (heapSizeMegs > 511L) {
      LauncherFrame.main(args);
    } else {
      ArrayList params = new ArrayList();

      String pathToJar = MinecraftLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

      params.add("javaw");
      params.add("-Xms512m");
      params.add("-Xmx1024m");
      params.add("-Dsun.java2d.noddraw=true");
      params.add("-Dsun.java2d.d3d=false");
      params.add("-Dsun.java2d.opengl=false");
      params.add("-Dsun.java2d.pmoffscreen=false");
      params.add("-classpath");
      params.add(pathToJar);
      params.add("net.minecraft.LauncherFrame");
      if (!debugMode)
        try {
          ProcessBuilder pb = new ProcessBuilder(params);
          Process process = pb.start();
          if (process == null) {
            throw new Exception("!");
          }

          System.exit(0);
        }
        catch (IOException ec)
        {
        }
      try {
        params.set(0, "java");
        ProcessBuilder pb = new ProcessBuilder(params);
        Process process = pb.start();

        if (process == null) {
          throw new IOException("!");
        }

        if (debugMode) {
          OutputConsole console = new OutputConsole();
          BufferedReader reader1 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
          BufferedReader reader2 = new BufferedReader(new InputStreamReader(process.getInputStream()));
          Thread errorViewer = new Thread(reader1, console, process)
          {
            BufferedReader reader = this.val$reader1;

            public void run()
            {
              boolean terminated = false;
              boolean noerrors = true;
              String output = "";
              this.val$console.acquire();
              while ((!terminated) && (output != null)) {
                int exitvalue = -2147483648;
                try {
                  exitvalue = this.val$process.exitValue();
                } catch (IllegalThreadStateException ex) {
                  try {
                    output = this.reader.readLine();
                    System.err.println(output);
                    this.val$console.appendText("\nError: " + output);
                  } catch (IOException ex1) {
                    output = null;
                  }
                }
                if (exitvalue != -2147483648) {
                  terminated = true;
                  if (exitvalue != 0) {
                    noerrors = false;
                  }
                }
              }
              if (noerrors)
                this.val$console.release();
            }
          };
          Thread outViewer = new Thread(reader2, console, process)
          {
            BufferedReader reader = this.val$reader2;

            public void run()
            {
              boolean terminated = false;
              boolean noerrors = true;
              String output = "";
              this.val$console.acquire();
              while ((!terminated) && (output != null)) {
                int exitvalue = -2147483648;
                try {
                  exitvalue = this.val$process.exitValue();
                } catch (IllegalThreadStateException ex) {
                  try {
                    output = this.reader.readLine();
                    System.out.println(output);
                    this.val$console.appendText("\nOutput: " + output);
                  } catch (IOException ex1) {
                    output = null;
                  }
                }
                if (exitvalue != -2147483648) {
                  terminated = true;
                  if (exitvalue != 0) {
                    noerrors = false;
                  }
                }
              }
              if (noerrors)
                this.val$console.release();
            }
          };
          errorViewer.start();
          outViewer.start();
        }

        if (!debugMode)
          System.exit(0);
      }
      catch (IOException e) {
        System.out.println("Java couldn't figure out a way to get more memory.\nIf the game crashes, run to the hills!");

        LauncherFrame.main(args);
      }
    }
  }
}