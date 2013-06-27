/*     */ package net.minecraft;
/*     */ 
/*     */ //import SevenZip.LzmaAlone;
/*     */ import java.applet.Applet;
/*     */ import java.io.File;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.FilePermission;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.io.StringWriter;
/*     */ import java.io.Writer;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.Method;
/*     */ import java.net.HttpURLConnection;
/*     */ import java.net.JarURLConnection;
/*     */ import java.net.SocketPermission;
/*     */ import java.net.URI;
/*     */ import java.net.URL;
/*     */ import java.net.URLClassLoader;
/*     */ import java.net.URLConnection;
/*     */ import java.security.AccessControlException;
/*     */ import java.security.AccessController;
/*     */ import java.security.CodeSource;
/*     */ import java.security.PermissionCollection;
/*     */ import java.security.PrivilegedExceptionAction;
/*     */ import java.security.ProtectionDomain;
/*     */ import java.security.SecureClassLoader;
/*     */ import java.security.cert.Certificate;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Vector;
/*     */ import java.util.jar.JarEntry;
/*     */ import java.util.jar.JarFile;
/*     */ import java.util.jar.JarOutputStream;
/*     */ import java.util.jar.Pack200;
/*     */ import java.util.jar.Pack200.Unpacker;
/*     */ 
/*     */ public class GameUpdater
/*     */   implements Runnable
/*     */ {
/*     */   public static final int STATE_INIT = 1;
/*     */   public static final int STATE_DETERMINING_PACKAGES = 2;
/*     */   public static final int STATE_CHECKING_CACHE = 3;
/*     */   public static final int STATE_DOWNLOADING = 4;
/*     */   public static final int STATE_EXTRACTING_PACKAGES = 5;
/*     */   public static final int STATE_UPDATING_CLASSPATH = 6;
/*     */   public static final int STATE_SWITCHING_APPLET = 7;
/*     */   public static final int STATE_INITIALIZE_REAL_APPLET = 8;
/*     */   public static final int STATE_START_REAL_APPLET = 9;
/*     */   public static final int STATE_DONE = 10;
/*     */   public int percentage;
/*     */   public int currentSizeDownload;
/*     */   public int totalSizeDownload;
/*     */   public int currentSizeExtract;
/*     */   public int totalSizeExtract;
/*     */   protected URL[] urlList;
/*     */   private static ClassLoader classLoader;
/*     */   protected Thread loaderThread;
/*     */   protected Thread animationThread;
/*     */   public boolean fatalError;
/*     */   public String fatalErrorDescription;
/*  65 */   protected String subtaskMessage = "";
/*  66 */   protected int state = 1;
/*  67 */   protected boolean lzmaSupported = false;
/*  68 */   protected boolean pack200Supported = false;
/*  69 */   protected String[] genericErrorMessage = { "An error occured while loading the applet.", "Please contact support to resolve this issue.", "<placeholder for error message>" };
/*     */   protected boolean certificateRefused;
/*  71 */   protected String[] certificateRefusedMessage = { "Permissions for Applet Refused.", "Please accept the permissions dialog to allow", "the applet to continue the loading process." };
/*  72 */   protected static boolean natives_loaded = false;
/*  73 */   public boolean forceUpdate = false;
/*  74 */   public static final String[] gameFiles = { "lwjgl.jar", "jinput.jar", "lwjgl_util.jar", "minecraft.jar" };
/*     */   InputStream[] isp;
/*     */   URLConnection urlconnectionp;
/*     */ 
/*     */   public void init()
/*     */   {
/*  79 */     this.state = 1;
/*     */     try {
/*  81 */       Class.forName("LZMA.LzmaInputStream");
/*  82 */       this.lzmaSupported = true;
/*     */     } catch (Throwable localThrowable) {
/*     */     }
/*     */     try {
/*  86 */       Pack200.class.getSimpleName();
/*  87 */       this.pack200Supported = true;
/*     */     } catch (Throwable localThrowable1) {
/*     */     }
/*     */   }
/*     */ 
/*     */   private String generateStacktrace(Exception exception) {
/*  93 */     Writer result = new StringWriter();
/*  94 */     PrintWriter printWriter = new PrintWriter(result);
/*  95 */     exception.printStackTrace(printWriter);
/*  96 */     return result.toString();
/*     */   }
/*     */ 
/*     */   protected String getDescriptionForState() {
/* 100 */     switch (this.state) {
/*     */     case 1:
/* 102 */       return "Initializing loader";
/*     */     case 2:
/* 104 */       return "Determining packages to load";
/*     */     case 3:
/* 106 */       return "Checking cache for existing files";
/*     */     case 4:
/* 108 */       return "Downloading packages";
/*     */     case 5:
/* 110 */       return "Extracting downloaded packages";
/*     */     case 6:
/* 112 */       return "Updating classpath";
/*     */     case 7:
/* 114 */       return "Switching applet";
/*     */     case 8:
/* 116 */       return "Initializing real applet";
/*     */     case 9:
/* 118 */       return "Starting real applet";
/*     */     case 10:
/* 120 */       return "Done loading";
/*     */     }
/* 122 */     return "unknown state";
/*     */   }
/*     */ 
/*     */   protected void loadJarURLs() throws Exception {
/* 126 */     this.state = 2;
/*     */ 
/* 128 */     this.urlList = new URL[gameFiles.length + 1];
/*     */ 
/* 130 */     URL path = new URL("http://s3.amazonaws.com/MinecraftDownload/");
/*     */ 
/* 132 */     for (int i = 0; i < gameFiles.length; i++) {
/* 133 */       this.urlList[i] = new URL(path, gameFiles[i]);
/*     */     }
/*     */ 
/* 136 */     String osName = System.getProperty("os.name");
/* 137 */     String nativeJar = null;
/*     */ 
/* 139 */     if (osName.startsWith("Win"))
/* 140 */       nativeJar = "windows_natives.jar.lzma";
/* 141 */     else if (osName.startsWith("Linux"))
/* 142 */       nativeJar = "linux_natives.jar.lzma";
/* 143 */     else if (osName.startsWith("Mac"))
/* 144 */       nativeJar = "macosx_natives.jar.lzma";
/* 145 */     else if ((osName.startsWith("Solaris")) || (osName.startsWith("SunOS")))
/* 146 */       nativeJar = "solaris_natives.jar.lzma";
/*     */     else {
/* 148 */       fatalErrorOccured("OS (" + osName + ") not supported", null);
/*     */     }
/*     */ 
/* 151 */     if (nativeJar == null)
/* 152 */       fatalErrorOccured("no lwjgl natives files found", null);
/*     */     else
/* 154 */       this.urlList[(this.urlList.length - 1)] = new URL(path, nativeJar);
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 159 */     init();
/* 160 */     this.state = 3;
/*     */ 
/* 162 */     this.percentage = 5;
/*     */     try {
/* 164 */       loadJarURLs();
/*     */ 
/* 166 */       String path = (String)AccessController.doPrivileged(new PrivilegedExceptionAction()
/*     */       {
/*     */         public Object run() throws Exception {
/* 169 */           return MinecraftUtil.getWorkingDirectory() + File.separator + "bin" + File.separator;
/*     */         }
/*     */       });
/* 172 */       File dir = new File(path);
/*     */ 
/* 174 */       if (!dir.exists()) {
/* 175 */         dir.mkdirs();
/*     */       }
/* 177 */       int before = this.percentage;
/* 178 */       boolean cacheAvailable = false;
/* 179 */       if (canPlayOffline()) {
/* 180 */         cacheAvailable = true;
/* 181 */         this.percentage = 90;
/*     */       }
/*     */ 
/* 184 */       if ((this.forceUpdate) || (!cacheAvailable)) {
/* 185 */         if (this.percentage != before) {
/* 186 */           this.percentage = before;
/*     */         }
/* 188 */         System.out.println("Path: " + path);
/* 189 */         downloadJars(path);
/* 190 */         extractJars(path);
/* 191 */         extractNatives(path);
/* 192 */         this.percentage = 90;
/*     */       }
/*     */ 
/* 195 */       updateClassPath(dir);
/* 196 */       this.state = 10;
/*     */     } catch (AccessControlException ace) {
/* 198 */       fatalErrorOccured(ace.getMessage(), ace);
/* 199 */       this.certificateRefused = true;
/*     */     } catch (Exception e) {
/* 201 */       fatalErrorOccured(e.getMessage(), e);
/*     */     } finally {
/* 203 */       this.loaderThread = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void updateClassPath(File dir) throws Exception
/*     */   {
/* 209 */     this.state = 6;
/*     */ 
/* 211 */     this.percentage = 95;
/*     */ 
/* 213 */     URL[] urls = new URL[this.urlList.length];
/* 214 */     for (int i = 0; i < this.urlList.length; i++) {
/* 215 */       urls[i] = new File(dir, getJarName(this.urlList[i])).toURI().toURL();
/* 216 */       System.out.println("URL: " + urls[i]);
/*     */     }
/*     */ 
/* 219 */     if (classLoader == null)
/* 220 */       classLoader = new URLClassLoader(urls)
/*     */       {
/*     */         protected PermissionCollection getPermissions(CodeSource codesource)
/*     */         {
/* 224 */           PermissionCollection perms = null;
/*     */           try {
/* 226 */             Method method = SecureClassLoader.class.getDeclaredMethod("getPermissions", new Class[] { CodeSource.class });
/* 227 */             method.setAccessible(true);
/* 228 */             perms = (PermissionCollection)method.invoke(getClass().getClassLoader(), new Object[] { codesource });
/*     */ 
/* 230 */             String host = "www.minecraft.net";
/*     */ 
/* 232 */             if ((host != null) && (host.length() > 0))
/* 233 */               perms.add(new SocketPermission(host, "connect,accept"));
/*     */             else {
/* 235 */               codesource.getLocation().getProtocol().equals("file");
/*     */             }
/*     */ 
/* 238 */             perms.add(new FilePermission("<<ALL FILES>>", "read"));
/*     */           } catch (Exception e) {
/* 240 */             e.printStackTrace();
/*     */           }
/*     */ 
/* 243 */           return perms;
/*     */         }
/*     */       };
/* 247 */     String path = dir.getAbsolutePath();
/* 248 */     if (!path.endsWith(File.separator)) {
/* 249 */       path = path + File.separator;
/*     */     }
/* 251 */     unloadNatives(path);
/*     */ 
/* 253 */     System.setProperty("org.lwjgl.librarypath", path + "natives");
/* 254 */     System.setProperty("net.java.games.input.librarypath", path + "natives");
/*     */ 
/* 256 */     natives_loaded = true;
/*     */   }
/*     */ 
/*     */   private void unloadNatives(String nativePath) {
/* 260 */     if (!natives_loaded)
/* 261 */       return;
/*     */     try
/*     */     {
/* 264 */       Field field = ClassLoader.class.getDeclaredField("loadedLibraryNames");
/* 265 */       field.setAccessible(true);
/* 266 */       Vector libs = (Vector)field.get(getClass().getClassLoader());
/*     */ 
/* 268 */       String path = new File(nativePath).getCanonicalPath();
/*     */ 
/* 270 */       for (int i = 0; i < libs.size(); i++) {
/* 271 */         String s = (String)libs.get(i);
/*     */ 
/* 273 */         if (s.startsWith(path)) {
/* 274 */           libs.remove(i);
/* 275 */           i--;
/*     */         }
/*     */       }
/*     */     } catch (Exception e) {
/* 279 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public Applet createApplet() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
/* 284 */     Class appletClass = classLoader.loadClass("net.minecraft.client.MinecraftApplet");
/* 285 */     return (Applet)appletClass.newInstance();
/*     */   }
/*     */ 
/*     */   protected void downloadJars(String path) throws Exception
/*     */   {
/* 290 */     this.state = 4;
/*     */ 
/* 292 */     int[] fileSizes = new int[this.urlList.length];
/*     */ 
/* 294 */     for (int i = 0; i < this.urlList.length; i++) {
/* 295 */       System.out.println(this.urlList[i]);
/* 296 */       URLConnection urlconnection = this.urlList[i].openConnection();
/* 297 */       urlconnection.setDefaultUseCaches(false);
/* 298 */       if ((urlconnection instanceof HttpURLConnection)) {
/* 299 */         ((HttpURLConnection)urlconnection).setRequestMethod("HEAD");
/*     */       }
/* 301 */       fileSizes[i] = urlconnection.getContentLength();
/* 302 */       this.totalSizeDownload += fileSizes[i];
/*     */     }
/*     */ 
/* 305 */     int initialPercentage = this.percentage = 10;
/*     */ 
/* 307 */     byte[] buffer = new byte[65536];
/* 308 */     for (int i = 0; i < this.urlList.length; i++) {
/* 309 */       int unsuccessfulAttempts = 0;
/* 310 */       int maxUnsuccessfulAttempts = 3;
/* 311 */       boolean downloadFile = true;
/*     */ 
/* 313 */       while (downloadFile) {
/* 314 */         downloadFile = false;
/*     */ 
/* 316 */         URLConnection urlconnection = this.urlList[i].openConnection();
/*     */ 
/* 318 */         if ((urlconnection instanceof HttpURLConnection)) {
/* 319 */           urlconnection.setRequestProperty("Cache-Control", "no-cache");
/* 320 */           urlconnection.connect();
/*     */         }
/*     */ 
/* 323 */         String currentFile = getFileName(this.urlList[i]);
/* 324 */         InputStream inputstream = getJarInputStream(currentFile, urlconnection);
/* 325 */         FileOutputStream fos = new FileOutputStream(path + currentFile);
/*     */ 
/* 327 */         long downloadStartTime = System.currentTimeMillis();
/* 328 */         int downloadedAmount = 0;
/* 329 */         int fileSize = 0;
/* 330 */         String downloadSpeedMessage = "";
/*     */         int bufferSize;
/* 332 */         while ((bufferSize = inputstream.read(buffer, 0, buffer.length)) != -1)
/*     */         {
/* 334 */           fos.write(buffer, 0, bufferSize);
/* 335 */           this.currentSizeDownload += bufferSize;
/* 336 */           fileSize += bufferSize;
/* 337 */           this.percentage = (initialPercentage + this.currentSizeDownload * 45 / this.totalSizeDownload);
/* 338 */           this.subtaskMessage = ("Retrieving: " + currentFile + " " + this.currentSizeDownload * 100 / this.totalSizeDownload + "%");
/*     */ 
/* 340 */           downloadedAmount += bufferSize;
/* 341 */           long timeLapse = System.currentTimeMillis() - downloadStartTime;
/*     */ 
/* 343 */           if (timeLapse >= 1000L) {
/* 344 */             float downloadSpeed = downloadedAmount / (float)timeLapse;
/*     */ 
/* 346 */             downloadSpeed = (int)(downloadSpeed * 100.0F) / 100.0F;
/*     */ 
/* 348 */             downloadSpeedMessage = " @ " + downloadSpeed + " KB/sec";
/*     */ 
/* 350 */             downloadedAmount = 0;
/*     */ 
/* 352 */             downloadStartTime += 1000L;
/*     */           }
/*     */ 
/* 355 */           this.subtaskMessage += downloadSpeedMessage;
/*     */         }
/*     */ 
/* 358 */         inputstream.close();
/* 359 */         fos.close();
/*     */ 
/* 361 */         if ((!(urlconnection instanceof HttpURLConnection)) || (fileSize == fileSizes[i]) || 
/* 365 */           (fileSizes[i] <= 0)) {
/*     */           continue;
/*     */         }
/* 368 */         unsuccessfulAttempts++;
/*     */ 
/* 370 */         if (unsuccessfulAttempts < maxUnsuccessfulAttempts) {
/* 371 */           downloadFile = true;
/* 372 */           this.currentSizeDownload -= fileSize;
/*     */         } else {
/* 374 */           throw new Exception("failed to download " + currentFile);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 380 */     this.subtaskMessage = "";
/*     */   }
/*     */ 
/*     */   protected InputStream getJarInputStream(String currentFile, URLConnection urlconnection) throws Exception
/*     */   {
/* 385 */     InputStream[] is = new InputStream[1];
/*     */ 
/* 388 */     this.isp = is;
/* 389 */     this.urlconnectionp = urlconnection;
/* 390 */     for (int j = 0; (j < 3) && (is[0] == null); j++) {
/* 391 */       Thread t = new Thread()
/*     */       {
/*     */         public void run()
/*     */         {
/*     */           try {
/* 396 */             GameUpdater.this.isp[0] = GameUpdater.this.urlconnectionp.getInputStream();
/*     */           }
/*     */           catch (Exception localIOException)
/*     */           {
/*     */           }
/*     */         }
/*     */       };
/* 401 */       t.setName("JarInputStreamThread");
/* 402 */       t.start();
/*     */ 
/* 404 */       int iterationCount = 0;
/* 405 */       while ((is[0] == null) && (iterationCount++ < 5))
/*     */         try {
/* 407 */           t.join(1000L);
/*     */         }
/*     */         catch (InterruptedException localInterruptedException) {
/*     */         }
/* 411 */       if (is[0] != null)
/*     */         continue;
/*     */       try
/*     */       {
/* 415 */         t.interrupt();
/* 416 */         t.join();
/*     */       }
/*     */       catch (InterruptedException localInterruptedException1) {
/*     */       }
/*     */     }
/* 421 */     if (is[0] == null) {
/* 422 */       if (currentFile.equals("minecraft.jar")) {
/* 423 */         throw new Exception("Unable to download " + currentFile);
/*     */       }
/* 425 */       throw new Exception("Unable to download " + currentFile);
/*     */     }
/*     */ 
/* 428 */     return is[0];
/*     */   }
/*     */ 
/*     */   protected void extractLZMA(String in, String out) throws Exception
/*     */   {
/* 433 */     File f = new File(in);
/* 434 */     File fout = new File(out);
/* 435 */     //LzmaAlone.decompress(f, fout);  // bho???
/* 436 */     f.delete();
/*     */   }
/*     */ 
/*     */   protected void extractPack(String in, String out) throws Exception
/*     */   {
/* 441 */     File f = new File(in);
/* 442 */     FileOutputStream fostream = new FileOutputStream(out);
/* 443 */     JarOutputStream jostream = new JarOutputStream(fostream);
/*     */ 
/* 445 */     Pack200.Unpacker unpacker = Pack200.newUnpacker();
/* 446 */     unpacker.unpack(f, jostream);
/* 447 */     jostream.close();
/*     */ 
/* 449 */     f.delete();
/*     */   }
/*     */ 
/*     */   protected void extractJars(String path) throws Exception
/*     */   {
/* 454 */     this.state = 5;
/*     */ 
/* 456 */     float increment = 10.0F / this.urlList.length;
/*     */ 
/* 458 */     for (int i = 0; i < this.urlList.length; i++) {
/* 459 */       this.percentage = (55 + (int)(increment * (i + 1)));
/* 460 */       String filename = getFileName(this.urlList[i]);
/*     */ 
/* 462 */       if (filename.endsWith(".pack.lzma")) {
/* 463 */         this.subtaskMessage = ("Extracting: " + filename + " to " + filename.replaceAll(".lzma", ""));
/* 464 */         extractLZMA(path + filename, path + filename.replaceAll(".lzma", ""));
/*     */ 
/* 466 */         this.subtaskMessage = ("Extracting: " + filename.replaceAll(".lzma", "") + " to " + filename.replaceAll(".pack.lzma", ""));
/* 467 */         extractPack(path + filename.replaceAll(".lzma", ""), path + filename.replaceAll(".pack.lzma", ""));
/* 468 */       } else if (filename.endsWith(".pack")) {
/* 469 */         this.subtaskMessage = ("Extracting: " + filename + " to " + filename.replace(".pack", ""));
/* 470 */         extractPack(path + filename, path + filename.replace(".pack", ""));
/* 471 */       } else if (filename.endsWith(".lzma")) {
/* 472 */         this.subtaskMessage = ("Extracting: " + filename + " to " + filename.replace(".lzma", ""));
/* 473 */         extractLZMA(path + filename, path + filename.replace(".lzma", ""));
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void extractNatives(String path) throws Exception {
/* 479 */     this.state = 5;
/*     */ 
/* 481 */     int initialPercentage = this.percentage;
/*     */ 
/* 483 */     String nativeJar = getJarName(this.urlList[(this.urlList.length - 1)]);
/*     */ 
/* 485 */     Certificate[] certificate = Launcher.class.getProtectionDomain().getCodeSource().getCertificates();
/*     */ 
/* 487 */     if (certificate == null) {
/* 488 */       URL location = Launcher.class.getProtectionDomain().getCodeSource().getLocation();
/*     */ 
/* 490 */       JarURLConnection jurl = (JarURLConnection)new URL("jar:" + location.toString() + "!/net/minecraft/Launcher.class").openConnection();
/* 491 */       jurl.setDefaultUseCaches(true);
/*     */       try {
/* 493 */         certificate = jurl.getCertificates();
/*     */       } catch (Exception localException) {
/*     */       }
/*     */     }
/* 497 */     File nativeFolder = new File(path + "natives");
/* 498 */     if (!nativeFolder.exists()) {
/* 499 */       nativeFolder.mkdir();
/*     */     }
/*     */ 
/* 502 */     JarFile jarFile = new JarFile(path + nativeJar, true);
/* 503 */     Enumeration entities = jarFile.entries();
/*     */ 
/* 505 */     this.totalSizeExtract = 0;
/*     */ 
/* 507 */     while (entities.hasMoreElements()) {
/* 508 */       JarEntry entry = (JarEntry)entities.nextElement();
/*     */ 
/* 510 */       if ((entry.isDirectory()) || (entry.getName().indexOf('/') != -1)) {
/*     */         continue;
/*     */       }
/* 513 */       this.totalSizeExtract = (int)(this.totalSizeExtract + entry.getSize());
/*     */     }
/*     */ 
/* 516 */     this.currentSizeExtract = 0;
/*     */ 
/* 518 */     entities = jarFile.entries();
/*     */ 
/* 520 */     while (entities.hasMoreElements()) {
/* 521 */       JarEntry entry = (JarEntry)entities.nextElement();
/*     */ 
/* 523 */       if ((entry.isDirectory()) || (entry.getName().indexOf('/') != -1)) {
/*     */         continue;
/*     */       }
/* 526 */       File f = new File(path + "natives" + File.separator + entry.getName());
/* 527 */       if ((f.exists()) && (!f.delete()))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 532 */       InputStream in = jarFile.getInputStream(jarFile.getEntry(entry.getName()));
/* 533 */       OutputStream out = new FileOutputStream(path + "natives" + File.separator + entry.getName());
/*     */ 
/* 535 */       byte[] buffer = new byte[65536];
/*     */       int bufferSize;
/* 537 */       while ((bufferSize = in.read(buffer, 0, buffer.length)) != -1) {
/* 538 */         out.write(buffer, 0, bufferSize);
/* 539 */         this.currentSizeExtract += bufferSize;
/*     */ 
/* 541 */         this.percentage = (initialPercentage + this.currentSizeExtract * 20 / this.totalSizeExtract);
/* 542 */         this.subtaskMessage = ("Extracting: " + entry.getName() + " " + this.currentSizeExtract * 100 / this.totalSizeExtract + "%");
/*     */       }
/*     */ 
/* 545 */       validateCertificateChain(certificate, entry.getCertificates());
/*     */ 
/* 547 */       in.close();
/* 548 */       out.close();
/*     */     }
/* 550 */     this.subtaskMessage = "";
/*     */ 
/* 552 */     jarFile.close();
/*     */ 
/* 554 */     File f = new File(path + nativeJar);
/* 555 */     f.delete();
/*     */   }
/*     */ 
/*     */   protected static void validateCertificateChain(Certificate[] ownCerts, Certificate[] native_certs) throws Exception
/*     */   {
/* 560 */     if (ownCerts == null) {
/* 561 */       return;
/*     */     }
/* 563 */     if (native_certs == null) {
/* 564 */       throw new Exception("Unable to validate certificate chain. Native entry did not have a certificate chain at all");
/*     */     }
/*     */ 
/* 567 */     if (ownCerts.length != native_certs.length) {
/* 568 */       throw new Exception("Unable to validate certificate chain. Chain differs in length [" + ownCerts.length + " vs " + native_certs.length + "]");
/*     */     }
/*     */ 
/* 571 */     for (int i = 0; i < ownCerts.length; i++)
/* 572 */       if (!ownCerts[i].equals(native_certs[i]))
/* 573 */         throw new Exception("Certificate mismatch: " + ownCerts[i] + " != " + native_certs[i]);
/*     */   }
/*     */ 
/*     */   protected String getJarName(URL url)
/*     */   {
/* 579 */     String fileName = url.getFile();
/*     */ 
/* 581 */     if (fileName.contains("?")) {
/* 582 */       fileName = fileName.substring(0, fileName.indexOf("?"));
/*     */     }
/* 584 */     if (fileName.endsWith(".pack.lzma"))
/* 585 */       fileName = fileName.replaceAll(".pack.lzma", "");
/* 586 */     else if (fileName.endsWith(".pack"))
/* 587 */       fileName = fileName.replaceAll(".pack", "");
/* 588 */     else if (fileName.endsWith(".lzma")) {
/* 589 */       fileName = fileName.replaceAll(".lzma", "");
/*     */     }
/*     */ 
/* 592 */     return fileName.substring(fileName.lastIndexOf('/') + 1);
/*     */   }
/*     */ 
/*     */   protected String getFileName(URL url) {
/* 596 */     String fileName = url.getFile();
/* 597 */     if (fileName.contains("?")) {
/* 598 */       fileName = fileName.substring(0, fileName.indexOf("?"));
/*     */     }
/* 600 */     return fileName.substring(fileName.lastIndexOf('/') + 1);
/*     */   }
/*     */ 
/*     */   protected void fatalErrorOccured(String error, Exception e) {
/* 604 */     e.printStackTrace();
/* 605 */     this.fatalError = true;
/* 606 */     this.fatalErrorDescription = ("Fatal error occured (" + this.state + "): " + error);
/* 607 */     System.out.println(this.fatalErrorDescription);
/* 608 */     if (e != null)
/* 609 */       System.out.println(generateStacktrace(e));
/*     */   }
/*     */ 
/*     */   public boolean canPlayOffline()
/*     */   {
/* 614 */     if ((!MinecraftUtil.getBinFolder().exists()) || (!MinecraftUtil.getBinFolder().isDirectory())) {
/* 615 */       return false;
/*     */     }
/* 617 */     if ((!MinecraftUtil.getNativesFolder().exists()) || (!MinecraftUtil.getNativesFolder().isDirectory())) {
/* 618 */       return false;
/*     */     }
/* 620 */     if (MinecraftUtil.getBinFolder().list().length < gameFiles.length + 1) {
/* 621 */       return false;
/*     */     }
/* 623 */     if (MinecraftUtil.getNativesFolder().list().length < 1) {
/* 624 */       return false;
/*     */     }
/* 626 */     String[] bins = MinecraftUtil.getBinFolder().list();
/* 627 */     for (String necessary : gameFiles) {
/* 628 */       boolean isThere = false;
/* 629 */       for (String found : bins) {
/* 630 */         if (necessary.equalsIgnoreCase(found)) {
/* 631 */           isThere = true;
/* 632 */           break;
/*     */         }
/*     */       }
/* 635 */       if (!isThere)
/*     */       {
/* 638 */         return false;
/*     */       }
/*     */     }
/* 641 */     return true;
/*     */   }
/*     */ }
