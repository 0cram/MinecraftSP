/*     */ package net.minecraft;
/*     */ 
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Frame;
/*     */ import java.awt.event.WindowAdapter;
/*     */ import java.awt.event.WindowEvent;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.util.Map;
/*     */ import javax.imageio.ImageIO;
/*     */ 
/*     */ public class LauncherFrame extends Frame
/*     */ {
/*     */   public static final int VERSION = 15;
/*     */   private static final long serialVersionUID = 1L;
/*     */   private Launcher launcher;
/*     */   private LoginForm loginForm;
/*  19 */   public boolean forceUpdate = false;
/*     */ 
/*     */   public LauncherFrame() {
/*  22 */     super("Minecraft Launcher by multiplayer-italia.com");
/*  23 */     //System.out.println("Hello!");
/*  24 */     setBackground(Color.BLACK);
/*  25 */     this.loginForm = new LoginForm(this);
/*  26 */     setLayout(new BorderLayout());
/*  27 */     add(this.loginForm, "Center");
/*     */ 
/*  29 */     this.loginForm.setPreferredSize(new Dimension(854, 480));
/*  30 */     pack();
/*  31 */     setLocationRelativeTo(null);
/*     */     try {
/*  33 */       setIconImage(ImageIO.read(LauncherFrame.class.getResource("favicon.png")));
/*     */     } catch (IOException e1) {
/*  35 */       e1.printStackTrace();
/*     */     }
/*     */ 
/*  38 */     addWindowListener(new WindowAdapter()
/*     */     {
/*     */       public void windowClosing(WindowEvent arg0)
/*     */       {
/*  42 */         new Thread()
/*     */         {
/*     */           public void run()
/*     */           {
/*     */             try {
/*  47 */               Thread.sleep(30000L);
/*     */             } catch (InterruptedException e) {
/*  49 */               e.printStackTrace();
/*     */             }
/*  51 */             System.out.println("FORCING EXIT!");
/*  52 */             System.exit(0);
/*     */           }
/*     */         }
/*  42 */         .start();
/*     */ 
/*  55 */         if (LauncherFrame.this.launcher != null) {
/*  56 */           LauncherFrame.this.launcher.stop();
/*  57 */           LauncherFrame.this.launcher.destroy();
/*     */         }
/*  59 */         System.exit(0);
/*     */       } } );
/*     */   }
/*     */ 
/*     */   public String getFakeResult(String userName) {
/*  65 */     return MinecraftUtil.getFakeLatestVersion() + ":35b9fd01865fda9d70b157e244cf801c:" + userName + ":12345:";
/*     */   }
/*     */ 
/*     */   public void login(String userName) {
/*  69 */     String result = getFakeResult(userName);
/*  70 */     String[] values = result.split(":");
/*  71 */     this.launcher = new Launcher();
/*  72 */     this.launcher.forceUpdate = this.forceUpdate;
/*  73 */     this.launcher.customParameters.put("userName", values[2].trim());
/*  74 */     this.launcher.customParameters.put("sessionId", values[3].trim());
/*  75 */     this.launcher.init();
/*  76 */     removeAll();
/*  77 */     add(this.launcher, "Center");
/*  78 */     validate();
/*  79 */     this.launcher.start();
/*  80 */     this.loginForm.loginOk();
/*  81 */     this.loginForm = null;
/*  82 */     setTitle("Minecraft");
/*     */   }
/*     */ 
/*     */   private void showError(String error) {
/*  86 */     removeAll();
/*  87 */     add(this.loginForm);
/*  88 */     this.loginForm.setError(error);
/*  89 */     validate();
/*     */   }
/*     */ 
/*     */   public boolean canPlayOffline(String userName) {
/*  93 */     Launcher launcher2 = new Launcher();
/*  94 */     launcher2.init(userName, "12345");
/*  95 */     return launcher2.canPlayOffline();
/*     */   }
/*     */ 
/*     */   public static void main(String[] args) {
/*  99 */     LauncherFrame launcherFrame = new LauncherFrame();
/* 100 */     launcherFrame.setVisible(true);
/*     */   }
/*     */ }
