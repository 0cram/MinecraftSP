/*     */ package net.minecraft;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.net.HttpURLConnection;
/*     */ import java.net.URL;
/*     */ import java.util.Properties;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ 
/*     */ public class MinecraftUtil
/*     */ {
/*  20 */   private static File workDir = null;
/*  21 */   private static File binDir = null;
/*  22 */   private static File resourcesDis = null;
/*  23 */   private static File optionsFile = null;
/*  24 */   private static File lastloginFile = null;
/*  25 */   private static File savesDir = null;
/*  26 */   private static File tempFolder = null;
/*  27 */   private static File nativesFolder = null;
/*     */ 
/*     */   public static File getWorkingDirectory() {
/*  30 */     if (workDir == null) {
/*  31 */       workDir = getWorkingDirectory("minecraft");
/*     */     }
/*  33 */     return workDir;
/*     */   }
/*     */ 
/*     */   public static File getBinFolder() {
/*  37 */     if (binDir == null) {
/*  38 */       binDir = new File(getWorkingDirectory(), "bin");
/*     */     }
/*  40 */     return binDir;
/*     */   }
/*     */ 
/*     */   public static File getResourcesFolder() {
/*  44 */     if (resourcesDis == null) {
/*  45 */       resourcesDis = new File(getWorkingDirectory(), "resources");
/*     */     }
/*  47 */     return resourcesDis;
/*     */   }
/*     */ 
/*     */   public static File getOptionsFile() {
/*  51 */     if (optionsFile == null) {
/*  52 */       optionsFile = new File(getWorkingDirectory(), "options.txt");
/*     */     }
/*  54 */     return optionsFile;
/*     */   }
/*     */ 
/*     */   public static File getLoginFile() {
/*  58 */     if (lastloginFile == null) {
/*  59 */       lastloginFile = new File(getWorkingDirectory(), "lastlogin");
/*     */     }
/*  61 */     return lastloginFile;
/*     */   }
/*     */ 
/*     */   public static File getSavesFolder() {
/*  65 */     if (savesDir == null) {
/*  66 */       savesDir = new File(getWorkingDirectory(), "saves");
/*     */     }
/*  68 */     return savesDir;
/*     */   }
/*     */ 
/*     */   public static File getNativesFolder() {
/*  72 */     if (nativesFolder == null) {
/*  73 */       nativesFolder = new File(getBinFolder(), "natives");
/*     */     }
/*  75 */     return nativesFolder;
/*     */   }
/*     */ 
/*     */   public static File getTempFolder() {
/*  79 */     if (tempFolder == null) {
/*  80 */       tempFolder = new File(System.getProperties().getProperty("java.io.tmpdir"), "MCBKPMNGR");
/*     */     }
/*  82 */     if (!tempFolder.exists()) {
/*  83 */       tempFolder.mkdirs();
/*     */     }
/*  85 */     return tempFolder;
/*     */   }
/*     */ 
/*     */   public static File getWorkingDirectory(String applicationName) {
/*  89 */     String userHome = System.getProperty("user.home", ".");
/*     */     File workingDirectory;
/*  91 */     switch (getPlatform().ordinal()) {
/*     */     case 0:
/*     */     case 1:
/*  94 */       workingDirectory = new File(userHome, '.' + applicationName + '/');
/*  95 */       break;
/*     */     case 2:
/*  97 */       String applicationData = System.getenv("APPDATA");
/*  98 */       if (applicationData != null)
/*  99 */         workingDirectory = new File(applicationData, "." + applicationName + '/');
/*     */       else {
/* 101 */         workingDirectory = new File(userHome, '.' + applicationName + '/');
/*     */       }
/* 103 */       break;
/*     */     case 3:
/* 105 */       workingDirectory = new File(userHome, "Library/Application Support/" + applicationName);
/* 106 */       break;
/*     */     default:
/* 108 */       workingDirectory = new File(userHome, applicationName + '/');
/*     */     }
/* 110 */     if ((!workingDirectory.exists()) && (!workingDirectory.mkdirs())) {
/* 111 */       throw new RuntimeException("The working directory could not be created: " + workingDirectory);
/*     */     }
/* 113 */     return workingDirectory;
/*     */   }
/*     */ 
/*     */   private static OS getPlatform() {
/* 117 */     String osName = System.getProperty("os.name").toLowerCase();
/* 118 */     if (osName.contains("win")) {
/* 119 */       return OS.windows;
/*     */     }
/* 121 */     if (osName.contains("mac")) {
/* 122 */       return OS.macos;
/*     */     }
/* 124 */     if (osName.contains("solaris")) {
/* 125 */       return OS.solaris;
/*     */     }
/* 127 */     if (osName.contains("sunos")) {
/* 128 */       return OS.solaris;
/*     */     }
/* 130 */     if (osName.contains("linux")) {
/* 131 */       return OS.linux;
/*     */     }
/* 133 */     if (osName.contains("unix")) {
/* 134 */       return OS.linux;
/*     */     }
/* 136 */     return OS.unknown;
/*     */   }
/*     */ 
///*     */   public static String excutePost(String targetURL, String urlParameters) {
///* 140 */     HttpURLConnection connection = null;
///*     */     try {
///* 142 */       URL url = new URL(targetURL);
///* 143 */       connection = (HttpURLConnection)url.openConnection();
///* 144 */       connection.setRequestMethod("POST");
///* 145 */       connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
///*     */ 
///* 147 */       connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
///* 148 */       connection.setRequestProperty("Content-Language", "en-US");
///*     */ 
///* 150 */       connection.setUseCaches(false);
///* 151 */       connection.setDoInput(true);
///* 152 */       connection.setDoOutput(true);
///*     */ 
///* 154 */       wr = new DataOutputStream(connection.getOutputStream());
///* 155 */       wr.writeBytes(urlParameters);
///* 156 */       wr.flush();
///* 157 */       wr.close();
///*     */ 
///* 159 */       InputStream is = connection.getInputStream();
///* 160 */       BufferedReader rd = new BufferedReader(new InputStreamReader(is));
///*     */ 
///* 162 */       StringBuffer response = new StringBuffer();
///*     */       String line;
///* 164 */       while ((line = rd.readLine()) != null) {
///* 165 */         response.append(line);
///* 166 */         response.append('\r');
///*     */       }
///* 168 */       rd.close();
///* 169 */       String str1 = response.toString();
///* 170 */       //String str1 = str1;  // bho?
///*     */       return str1;
///*     */     }
///*     */     catch (Exception e)
///*     */     {
///* 172 */       e.printStackTrace();
///* 173 */       DataOutputStream wr = null;
/////*     */       return wr;
///*     */     }
///*     */     finally
///*     */     {
///* 175 */       if (connection != null)
///* 176 */         connection.disconnect(); 
///* 176 */      }throw localObject;
//*     */   }
/*     */ 
/*     */   public static void resetVersion()
/*     */   {
/* 187 */     DataOutputStream dos = null;
/*     */     try {
/* 189 */       File dir = new File(getWorkingDirectory() + File.separator + "bin" + File.separator);
/* 190 */       File versionFile = new File(dir, "version");
/* 191 */       dos = new DataOutputStream(new FileOutputStream(versionFile));
/* 192 */       dos.writeUTF("0");
/* 193 */       dos.close();
/*     */     } catch (FileNotFoundException ex) {
/* 195 */       Logger.getLogger(MinecraftUtil.class.getName()).log(Level.SEVERE, null, ex);
/*     */     } catch (IOException ex) {
/* 197 */       Logger.getLogger(MinecraftUtil.class.getName()).log(Level.SEVERE, null, ex);
/*     */     } finally {
/*     */       try {
/* 200 */         dos.close();
/*     */       } catch (IOException ex) {
/* 202 */         Logger.getLogger(MinecraftUtil.class.getName()).log(Level.SEVERE, null, ex);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getFakeLatestVersion() {
/*     */     try {
/* 209 */       File dir = new File(getWorkingDirectory() + File.separator + "bin" + File.separator);
/* 210 */       File file = new File(dir, "version");
/* 211 */       DataInputStream dis = new DataInputStream(new FileInputStream(file));
/* 212 */       String version = dis.readUTF();
/* 213 */       dis.close();
/* 214 */       if (version.equals("0")) {
/* 215 */         return "1285241960000";
/*     */       }
/* 217 */       return version; } catch (IOException ex) {
/*     */     }
/* 219 */     return "1285241960000";
/*     */   }
/*     */ 
/*     */   private static enum OS
/*     */   {
/* 183 */     linux, solaris, windows, macos, unknown;
/*     */   }
/*     */ }