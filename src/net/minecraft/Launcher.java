/*     */ package net.minecraft;
/*     */ 
/*     */ import java.applet.Applet;
/*     */ import java.applet.AppletStub;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Font;
/*     */ import java.awt.FontMetrics;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.Image;
/*     */ import java.awt.image.BufferedImage;
/*     */ import java.awt.image.VolatileImage;
/*     */ import java.io.IOException;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.URL;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import javax.imageio.ImageIO;
/*     */ 
/*     */ public class Launcher extends Applet
/*     */   implements Runnable, AppletStub
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*  23 */   public Map<String, String> customParameters = new HashMap();
/*     */   private GameUpdater gameUpdater;
/*  25 */   private boolean gameUpdaterStarted = false;
/*     */   private Applet applet;
/*     */   private Image bgImage;
/*  28 */   private boolean active = false;
/*  29 */   private int context = 0;
/*     */   private VolatileImage img;
/*  31 */   public boolean forceUpdate = false;
/*     */ 
/*     */   public boolean isActive()
/*     */   {
/*  35 */     if (this.context == 0) {
/*  36 */       this.context = -1;
/*     */       try {
/*  38 */         if (getAppletContext() != null)
/*  39 */           this.context = 1;
/*     */       }
/*     */       catch (Exception localException) {
/*     */       }
/*     */     }
/*  44 */     if (this.context == -1) {
/*  45 */       return this.active;
/*     */     }
/*  47 */     return super.isActive();
/*     */   }
/*     */ 
/*     */   public void init(String userName, String sessionId) {
/*     */     try {
/*  52 */       this.bgImage = ImageIO.read(LoginForm.class.getResource("dirt.png")).getScaledInstance(32, 32, 16);
/*     */     } catch (IOException e) {
/*  54 */       e.printStackTrace();
/*     */     }
/*     */ 
/*  57 */     this.customParameters.put("username", userName);
/*  58 */     this.customParameters.put("sessionid", sessionId);
/*     */ 
/*  60 */     this.gameUpdater = new GameUpdater();
/*  61 */     this.gameUpdater.forceUpdate = this.forceUpdate;
/*     */   }
/*     */ 
/*     */   public boolean canPlayOffline() {
/*  65 */     return this.gameUpdater.canPlayOffline();
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  70 */     if (this.applet != null) {
/*  71 */       this.applet.init();
/*  72 */       return;
/*     */     }
/*  74 */     init(getParameter("userName"), getParameter("sessionId"));
/*     */   }
/*     */ 
/*     */   public void start()
/*     */   {
/*  79 */     if (this.applet != null) {
/*  80 */       this.applet.start();
/*  81 */       return;
/*     */     }
/*  83 */     if (this.gameUpdaterStarted) {
/*  84 */       return;
/*     */     }
/*     */ 
/*  87 */     Thread t = new Thread()
/*     */     {
/*     */       public void run()
/*     */       {
/*  91 */         Launcher.this.gameUpdater.run();
/*     */         try {
/*  93 */           if (!Launcher.this.gameUpdater.fatalError)
/*  94 */             Launcher.this.replace(Launcher.this.gameUpdater.createApplet());
/*     */         }
/*     */         catch (ClassNotFoundException e) {
/*  97 */           e.printStackTrace();
/*     */         } catch (InstantiationException e) {
/*  99 */           e.printStackTrace();
/*     */         } catch (IllegalAccessException e) {
/* 101 */           e.printStackTrace();
/*     */         }
/*     */       }
/*     */     };
/* 105 */     t.setDaemon(true);
/* 106 */     t.start();
/*     */ 
/* 108 */     t = new Thread()
/*     */     {
/*     */       public void run()
/*     */       {
/* 112 */         while (Launcher.this.applet == null) {
/* 113 */           Launcher.this.repaint();
/*     */           try {
/* 115 */             Thread.sleep(10L);
/*     */           } catch (InterruptedException e) {
/* 117 */             e.printStackTrace();
/*     */           }
/*     */         }
/*     */       }
/*     */     };
/* 122 */     t.setDaemon(true);
/* 123 */     t.start();
/*     */ 
/* 125 */     this.gameUpdaterStarted = true;
/*     */   }
/*     */ 
/*     */   public void stop()
/*     */   {
/* 130 */     if (this.applet != null) {
/* 131 */       this.active = false;
/* 132 */       this.applet.stop();
/* 133 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void destroy()
/*     */   {
/* 139 */     if (this.applet != null) {
/* 140 */       this.applet.destroy();
/* 141 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void replace(Applet applet) {
/* 146 */     this.applet = applet;
/* 147 */     applet.setStub(this);
/* 148 */     applet.setSize(getWidth(), getHeight());
/*     */ 
/* 150 */     setLayout(new BorderLayout());
/* 151 */     add(applet, "Center");
/*     */ 
/* 153 */     applet.init();
/* 154 */     this.active = true;
/* 155 */     applet.start();
/* 156 */     validate();
/*     */   }
/*     */ 
/*     */   public void update(Graphics g)
/*     */   {
/* 161 */     paint(g);
/*     */   }
/*     */ 
/*     */   public void paint(Graphics g2)
/*     */   {
/* 166 */     if (this.applet != null) {
/* 167 */       return;
/*     */     }
/*     */ 
/* 170 */     int w = getWidth() / 2;
/* 171 */     int h = getHeight() / 2;
/* 172 */     if ((this.img == null) || (this.img.getWidth() != w) || (this.img.getHeight() != h)) {
/* 173 */       this.img = createVolatileImage(w, h);
/*     */     }
/*     */ 
/* 176 */     Graphics g = this.img.getGraphics();
/* 177 */     for (int x = 0; x <= w / 32; x++) {
/* 178 */       for (int y = 0; y <= h / 32; y++) {
/* 179 */         g.drawImage(this.bgImage, x * 32, y * 32, null);
/*     */       }
/*     */     }
/* 182 */     g.setColor(Color.LIGHT_GRAY);
/*     */ 
/* 184 */     String msg = "Updating Minecraft";
/* 185 */     if (this.gameUpdater.fatalError) {
/* 186 */       msg = "Failed to launch";
/*     */     }
/*     */ 
/* 189 */     g.setFont(new Font(null, 1, 20));
/* 190 */     FontMetrics fm = g.getFontMetrics();
/* 191 */     g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 - fm.getHeight() * 2);
/*     */ 
/* 193 */     g.setFont(new Font(null, 0, 12));
/* 194 */     fm = g.getFontMetrics();
/* 195 */     msg = this.gameUpdater.getDescriptionForState();
/* 196 */     if (this.gameUpdater.fatalError) {
/* 197 */       msg = this.gameUpdater.fatalErrorDescription;
/*     */     }
/*     */ 
/* 200 */     g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 + fm.getHeight() * 1);
/* 201 */     msg = this.gameUpdater.subtaskMessage;
/* 202 */     g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 + fm.getHeight() * 2);
/*     */ 
/* 204 */     if (!this.gameUpdater.fatalError) {
/* 205 */       g.setColor(Color.black);
/* 206 */       g.fillRect(64, h - 64, w - 128 + 1, 5);
/* 207 */       g.setColor(new Color(32768));
/* 208 */       g.fillRect(64, h - 64, this.gameUpdater.percentage * (w - 128) / 100, 4);
/* 209 */       g.setColor(new Color(2138144));
/* 210 */       g.fillRect(65, h - 64 + 1, this.gameUpdater.percentage * (w - 128) / 100 - 2, 1);
/*     */     }
/*     */ 
/* 213 */     g.dispose();
/*     */ 
/* 215 */     g2.drawImage(this.img, 0, 0, w * 2, h * 2, null);
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */   }
/*     */ 
/*     */   public String getParameter(String name) {
/* 223 */     String custom = (String)this.customParameters.get(name);
/* 224 */     if (custom != null)
/* 225 */       return custom;
/*     */     try
/*     */     {
/* 228 */       return super.getParameter(name);
/*     */     } catch (Exception e) {
/* 230 */       this.customParameters.put(name, null);
/*     */     }
/* 232 */     return null;
/*     */   }
/*     */ 
/*     */   public void appletResize(int width, int height)
/*     */   {
/*     */   }
/*     */ 
/*     */   public URL getDocumentBase()
/*     */   {
/*     */     try {
/* 242 */       return new URL("http://www.youtube.com/user/multiplayeritalia");
/*     */     } catch (MalformedURLException e) {
/* 244 */       e.printStackTrace();
/*     */     }
/* 246 */     return null;
/*     */   }
/*     */ }

