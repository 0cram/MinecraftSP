///*     */ package net.minecraft;
///*     */ 
///*     */ import java.io.BufferedReader;
///*     */ import java.io.IOException;
///*     */ import java.io.InputStreamReader;
///*     */ import java.io.PrintStream;
///*     */ import java.net.URI;
///*     */ import java.net.URL;
///*     */ import java.security.CodeSource;
///*     */ import java.security.ProtectionDomain;
///*     */ import java.util.ArrayList;
/*     */ 
///*     */ public class MinecraftLauncher
///*     */ {
///*     */   private static final long MIN_HEAP = 511L;
///*     */   private static final long RECOMMENDED_HEAP = 1024L;
///*  15 */   private static boolean debugMode = false;
///*     */ 
///*     */   public static void main(String[] args) throws Exception
///*     */   {
///*  19 */     if ((args.length > 0) && (args[0].contains("debug"))) {
///*  20 */       debugMode = true;
///*     */     }
///*     */ 
///*  23 */     long heapSizeMegs = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
///*     */ 
///*  25 */     if (heapSizeMegs > 511L) {
///*  26 */       LauncherFrame.main(args);
///*     */     } else {
///*  28 */       ArrayList params = new ArrayList();
///*     */ 
///*  30 */       String pathToJar = MinecraftLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
///*     */ 
///*  32 */       params.add("javaw");
///*  33 */       params.add("-Xms512m");
///*  34 */       params.add("-Xmx1024m");
///*  35 */       params.add("-Dsun.java2d.noddraw=true");
///*  36 */       params.add("-Dsun.java2d.d3d=false");
///*  37 */       params.add("-Dsun.java2d.opengl=false");
///*  38 */       params.add("-Dsun.java2d.pmoffscreen=false");
///*  39 */       params.add("-classpath");
///*  40 */       params.add(pathToJar);
///*  41 */       params.add("net.minecraft.LauncherFrame");
///*  42 */       if (!debugMode)
///*     */         try {
///*  44 */           ProcessBuilder pb = new ProcessBuilder(params);
///*  45 */           Process process = pb.start();
///*  46 */           if (process == null) {
///*  47 */             throw new Exception("!");
///*     */           }
///*     */ 
///*  50 */           System.exit(0);
///*     */         }
///*     */         catch (IOException ec)
///*     */         {
///*     */         }
///*     */       try {
///*  56 */         params.set(0, "java");
///*  57 */         ProcessBuilder pb = new ProcessBuilder(params);
///*  58 */         Process process = pb.start();
///*     */ 
///*  60 */         if (process == null) {
///*  61 */           throw new IOException("!");
///*     */         }
///*     */ 
///*  64 */         if (debugMode) {
///*  65 */           OutputConsole console = new OutputConsole();
///*  66 */           BufferedReader reader1 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
///*  67 */           BufferedReader reader2 = new BufferedReader(new InputStreamReader(process.getInputStream()));
///*  68 */           Thread errorViewer = new Thread(reader1, console, process)
///*     */           {
///*  70 */             BufferedReader reader = this.val$reader1;
///*     */ 
///*     */             public void run()
///*     */             {
///*  74 */               boolean terminated = false;
///*  75 */               boolean noerrors = true;
///*  76 */               String output = "";
///*  77 */               this.val$console.acquire();
///*  78 */               while ((!terminated) && (output != null)) {
///*  79 */                 int exitvalue = -2147483648;
///*     */                 try {
///*  81 */                   exitvalue = this.val$process.exitValue();
///*     */                 } catch (IllegalThreadStateException ex) {
///*     */                   try {
///*  84 */                     output = this.reader.readLine();
///*  85 */                     System.err.println(output);
///*  86 */                     this.val$console.appendText("\nError: " + output);
///*     */                   } catch (IOException ex1) {
///*  88 */                     output = null;
///*     */                   }
///*     */                 }
///*  91 */                 if (exitvalue != -2147483648) {
///*  92 */                   terminated = true;
///*  93 */                   if (exitvalue != 0) {
///*  94 */                     noerrors = false;
///*     */                   }
///*     */                 }
///*     */               }
///*  98 */               if (noerrors)
///*  99 */                 this.val$console.release();
///*     */             }
///*     */           };
///* 103 */           Thread outViewer = new Thread(reader2, console, process)
///*     */           {
///* 105 */             BufferedReader reader = this.val$reader2;
///*     */ 
///*     */             public void run()
///*     */             {
///* 109 */               boolean terminated = false;
///* 110 */               boolean noerrors = true;
///* 111 */               String output = "";
///* 112 */               this.val$console.acquire();
///* 113 */               while ((!terminated) && (output != null)) {
///* 114 */                 int exitvalue = -2147483648;
///*     */                 try {
///* 116 */                   exitvalue = this.val$process.exitValue();
///*     */                 } catch (IllegalThreadStateException ex) {
///*     */                   try {
///* 119 */                     output = this.reader.readLine();
///* 120 */                     System.out.println(output);
///* 121 */                     this.val$console.appendText("\nOutput: " + output);
///*     */                   } catch (IOException ex1) {
///* 123 */                     output = null;
///*     */                   }
///*     */                 }
///* 126 */                 if (exitvalue != -2147483648) {
///* 127 */                   terminated = true;
///* 128 */                   if (exitvalue != 0) {
///* 129 */                     noerrors = false;
///*     */                   }
///*     */                 }
///*     */               }
///* 133 */               if (noerrors)
///* 134 */                 this.val$console.release();
///*     */             }
///*     */           };
///* 138 */           errorViewer.start();
///* 139 */           outViewer.start();
///*     */         }
///*     */ 
///* 142 */         if (!debugMode)
///* 143 */           System.exit(0);
///*     */       }
///*     */       catch (IOException e) {
///* 146 */         System.out.println("Java couldn't figure out a way to get more memory.\nIf the game crashes, run to the hills!");
///*     */ 
///* 148 */         LauncherFrame.main(args);
///*     */       }
///*     */     }
///*     */   }
///*     */ }
