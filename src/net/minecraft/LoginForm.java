/*     */ package net.minecraft;
/*     */ 
//		  import anjocaido.minecraftmanager.MinecraftBackupManager;  // rimuovo l'import del backupmanager e puo essere eliminato dal jar
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Button;
/*     */ import java.awt.Checkbox;
/*     */ import java.awt.Color;
/*     */ import java.awt.Cursor;
/*     */ import java.awt.Desktop;
/*     */ import java.awt.Font;
/*     */ import java.awt.FontMetrics;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Image;
/*     */ import java.awt.Insets;
/*     */ import java.awt.Label;
/*     */ import java.awt.Panel;
/*     */ import java.awt.Rectangle;
/*     */ import java.awt.TextField;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.MouseAdapter;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.awt.image.BufferedImage;
/*     */ import java.awt.image.VolatileImage;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.net.URL;
/*     */ import java.util.Random;
/*     */ import javax.crypto.Cipher;
/*     */ import javax.crypto.CipherInputStream;
/*     */ import javax.crypto.CipherOutputStream;
/*     */ import javax.crypto.SecretKey;
/*     */ import javax.crypto.SecretKeyFactory;
/*     */ import javax.crypto.spec.PBEKeySpec;
/*     */ import javax.crypto.spec.PBEParameterSpec;
/*     */ import javax.imageio.ImageIO;
/*     */ 
/*     */ public class LoginForm extends Panel
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*     */   private Image bgImage;
/*  45 */   private TextField userName = new TextField(20);
//   		private Checkbox forceUpdateBox = new Checkbox("Force Update"); // elimino il bottone del force update --> forceupdatebox
/*  47 */   private Button launchButton = new Button("Gioca"); //("Enter Game");
/*  48 */   private Label errorLabel = new Label("", 1);
/*  49 */   private Label creditsVersion = new Label("Version 1.5.0 MPI");
//   		private Button openManager = new Button("Backup Manager");  // elimino il bottone del backup manager -> openManager
/*     */   private LauncherFrame launcherFrame;
/*  52 */   private boolean outdated = false;
/*     */   private VolatileImage img;
/*     */ 
/*     */   public LoginForm(LauncherFrame launcherFrame)
/*     */   {
/*  56 */     this.launcherFrame = launcherFrame;
/*     */ 
/*  58 */     GridBagLayout gbl = new GridBagLayout();
/*  59 */     setLayout(gbl);
/*     */ 
/*  61 */     add(buildLoginPanel());
/*     */     try {
/*  63 */       this.bgImage = ImageIO.read(LoginForm.class.getResource("dirt.png")).getScaledInstance(32, 32, 16);
/*     */     } catch (IOException e) {
/*  65 */       e.printStackTrace();
/*     */     }
/*     */ 
/*  68 */     readUsername();
/*  69 */     this.launchButton.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent ae) {
// ------------- rimozione della funzione force update ----------------	
//          if (LoginForm.this.forceUpdateBox.getState()) {
//             LoginForm.this.launcherFrame.forceUpdate = false; // era settato true
//         		}
// ------------- rimozione della funzione force update ----------------	
/*  75 */         LoginForm.this.launcherFrame.login(LoginForm.this.userName.getText());
/*     */       } } );
/*     */   }
/*     */ 
/*     */   private void readUsername() {
/*     */     try {
/*  82 */       File lastLogin = new File(MinecraftUtil.getWorkingDirectory(), "lastlogin");
/*     */ 
/*  84 */       Cipher cipher = getCipher(2, "passwordfile");
/*     */       DataInputStream dis;
/*  86 */       if (cipher != null)
/*  87 */         dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLogin), cipher));
/*     */       else {
/*  89 */         dis = new DataInputStream(new FileInputStream(lastLogin));
/*     */       }
/*  91 */       this.userName.setText(dis.readUTF());
/*  92 */       dis.close();
/*     */     } catch (Exception e) {
/*  94 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   private void writeUsername() {
/*     */     try {
/* 100 */       File lastLogin = new File(MinecraftUtil.getWorkingDirectory(), "lastlogin");
/*     */ 
/* 102 */       Cipher cipher = getCipher(1, "passwordfile");
/*     */       DataOutputStream dos;
/* 104 */       if (cipher != null)
/* 105 */         dos = new DataOutputStream(new CipherOutputStream(new FileOutputStream(lastLogin), cipher));
/*     */       else {
/* 107 */         dos = new DataOutputStream(new FileOutputStream(lastLogin));
/*     */       }
/* 109 */       dos.writeUTF(this.userName.getText());
/* 110 */       dos.writeUTF("");
/* 111 */       dos.close();
/*     */     } catch (Exception e) {
/* 113 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   private Cipher getCipher(int mode, String password) throws Exception {
/* 118 */     Random random = new Random(43287234L);
/* 119 */     byte[] salt = new byte[8];
/* 120 */     random.nextBytes(salt);
/* 121 */     PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);
/*     */ 
/* 123 */     SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
/* 124 */     Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
/* 125 */     cipher.init(mode, pbeKey, pbeParamSpec);
/* 126 */     return cipher;
/*     */   }
/*     */ 
/*     */   public void update(Graphics g)
/*     */   {
/* 131 */     paint(g);
/*     */   }
/*     */ 
/*     */   public void paint(Graphics g2)
/*     */   {
/* 136 */     int w = getWidth() / 2;
/* 137 */     int h = getHeight() / 2;
/* 138 */     if ((this.img == null) || (this.img.getWidth() != w) || (this.img.getHeight() != h)) {
/* 139 */       this.img = createVolatileImage(w, h);
/*     */     }
/*     */ 
/* 142 */     Graphics g = this.img.getGraphics();
/* 143 */     for (int x = 0; x <= w / 32; x++) {
/* 144 */       for (int y = 0; y <= h / 32; y++) {
/* 145 */         g.drawImage(this.bgImage, x * 32, y * 32, null);
/*     */       }
/*     */     }
/* 148 */     g.setColor(Color.LIGHT_GRAY);
/*     */ 
/* 150 */     String msg = "Minecraft Launcher";
/* 151 */     g.setFont(new Font(null, 1, 20));
/* 152 */     FontMetrics fm = g.getFontMetrics();
/* 153 */     g.drawString(msg, w / 2 - fm.stringWidth(msg) / 2, h / 2 - fm.getHeight() * 2);
/*     */ 
/* 155 */     g.dispose();
/* 156 */     g2.drawImage(this.img, 0, 0, w * 2, h * 2, null);
/*     */   }
/*     */ 
/*     */   private Panel buildLoginPanel() {
/* 160 */     Panel panel = new Panel()
/*     */     {
/*     */       private static final long serialVersionUID = 1L;
/* 163 */       private Insets insets = new Insets(12, 24, 16, 32);
/*     */ 
/*     */       public Insets getInsets()
/*     */       {
/* 167 */         return this.insets;
/*     */       }
/*     */ 
/*     */       public void update(Graphics g)
/*     */       {
/* 172 */         paint(g);
/*     */       }
/*     */ 
/*     */       public void paint(Graphics g)
/*     */       {
/* 177 */         super.paint(g);
/* 178 */         int hOffs = 0;
/*     */ 
/* 180 */         g.setColor(Color.BLACK);
/* 181 */         g.drawRect(0, 0 + hOffs, getWidth() - 1, getHeight() - 1 - hOffs);
/* 182 */         g.drawRect(1, 1 + hOffs, getWidth() - 3, getHeight() - 3 - hOffs);
/* 183 */         g.setColor(Color.WHITE);
/*     */ 
/* 185 */         g.drawRect(2, 2 + hOffs, getWidth() - 5, getHeight() - 5 - hOffs);
/*     */       }
/*     */     };
/* 188 */     panel.setBackground(Color.GRAY);
/* 189 */     BorderLayout layout = new BorderLayout();
/* 190 */     layout.setHgap(0);
/* 191 */     layout.setVgap(8);
/* 192 */     panel.setLayout(layout);
/*     */ 
/* 194 */     GridLayout gl1 = new GridLayout(0, 1);
/* 195 */     GridLayout gl2 = new GridLayout(0, 1);
/* 196 */     gl1.setVgap(2);
/* 197 */     gl2.setVgap(2);
/* 198 */     Panel titles = new Panel(gl1);
/* 199 */     Panel values = new Panel(gl2);
//------------- rimozione del checkbox force update ----------------	
/* 201 */     titles.add(new Label("Username:", 2));
//			  titles.add(new Label("", 2));
/* 203 */     values.add(this.userName);
//		      values.add(this.forceUpdateBox);
//------------- rimozione del checkbox force update ----------------	
/* 206 */     panel.add(titles, "West");
/* 207 */     panel.add(values, "Center");
/*     */ 
/* 209 */     Panel loginPanel = new Panel(new BorderLayout());
/*     */ 
/* 211 */     Panel registerPanel = new Panel(new BorderLayout());
/*     */     try {
/* 213 */       if (this.outdated) {
/* 214 */         Label accountLink = new Label("You need to update the launcher!")
/*     */         {
/*     */           private static final long serialVersionUID = 0L;
/*     */ 
/*     */           public void paint(Graphics g) {
/* 220 */             super.paint(g);
/*     */ 
/* 222 */             int x = 0;
/* 223 */             int y = 0;
/*     */ 
/* 225 */             FontMetrics fm = g.getFontMetrics();
/* 226 */             int width = fm.stringWidth(getText());
/* 227 */             int height = fm.getHeight();
/*     */ 
/* 229 */             if (getAlignment() == 0)
/* 230 */               x = 0;
/* 231 */             else if (getAlignment() == 1)
/* 232 */               x = getBounds().width / 2 - width / 2;
/* 233 */             else if (getAlignment() == 2) {
/* 234 */               x = getBounds().width - width;
/*     */             }
/* 236 */             y = getBounds().height / 2 + height / 2 - 1;
/*     */ 
/* 238 */             g.drawLine(x + 2, y, x + width - 2, y);
/*     */           }
/*     */ 
/*     */           public void update(Graphics g)
/*     */           {
/* 243 */             paint(g);
/*     */           }
/*     */         };
/* 246 */         accountLink.setCursor(Cursor.getPredefinedCursor(12));
/* 247 */         accountLink.addMouseListener(new MouseAdapter()
/*     */         {
/*     */           public void mousePressed(MouseEvent arg0)
/*     */           {
/*     */             try {
/* 252 */               Desktop.getDesktop().browse(new URL("http://www.minecraft.net/download.jsp").toURI());
/*     */             } catch (Exception e) {
/* 254 */               e.printStackTrace();
/*     */             }
/*     */           }
/*     */         });
/* 258 */         accountLink.setForeground(Color.BLUE);
/* 259 */         registerPanel.add(accountLink, "West");
/* 260 */         registerPanel.add(new Panel(), "Center");
/*     */       } else {
/* 262 */         Label accountLink = new Label("Need account?")
/*     */         {
/*     */           private static final long serialVersionUID = 0L;
/*     */ 
/*     */           public void paint(Graphics g) {
/* 268 */             super.paint(g);
/*     */ 
/* 270 */             int x = 0;
/* 271 */             int y = 0;
/*     */ 
/* 273 */             FontMetrics fm = g.getFontMetrics();
/* 274 */             int width = fm.stringWidth(getText());
/* 275 */             int height = fm.getHeight();
/*     */ 
/* 277 */             if (getAlignment() == 0)
/* 278 */               x = 0;
/* 279 */             else if (getAlignment() == 1)
/* 280 */               x = getBounds().width / 2 - width / 2;
/* 281 */             else if (getAlignment() == 2) {
/* 282 */               x = getBounds().width - width;
/*     */             }
/* 284 */             y = getBounds().height / 2 + height / 2 - 1;
/*     */ 
/* 286 */             g.drawLine(x + 2, y, x + width - 2, y);
/*     */           }
/*     */ 
/*     */           public void update(Graphics g)
/*     */           {
/* 291 */             paint(g);
/*     */           }
/*     */         };
/* 294 */         accountLink.setCursor(Cursor.getPredefinedCursor(12));
/* 295 */         accountLink.addMouseListener(new MouseAdapter()
/*     */         {
/*     */           public void mousePressed(MouseEvent arg0)
/*     */           {
/*     */             try {
/* 300 */               Desktop.getDesktop().browse(new URL("http://www.minecraft.net/register.jsp").toURI());
/*     */             } catch (Exception e) {
/* 302 */               e.printStackTrace();
/*     */             }
/*     */           }
/*     */         });
/* 306 */         accountLink.setForeground(Color.BLUE);
/* 307 */         registerPanel.add(this.creditsVersion, "West");
/* 308 */         registerPanel.add(new Panel(), "Center");
/*     */       }
/*     */     } catch (Error localError) {
/*     */     }
/* 312 */     loginPanel.add(registerPanel, "Center");
/* 313 */     loginPanel.add(this.launchButton, "East");
/* 314 */     Panel anjoPanel = new Panel();
// ------------------------elimino il bottone del Backup Manager ----------------------
//     this.openManager.addActionListener(new ActionListener()
//     {
//       public void actionPerformed(ActionEvent e) {
//         new MinecraftBackupManager().setVisible(false);  // da true a false per disabilitare il bottone backup manager
//       }
//     });
//     anjoPanel.add(this.openManager);
//------------------------elimino il bottone del Backup Manager ----------------------
/* 322 */     loginPanel.add(anjoPanel, "South");
/* 323 */     panel.add(loginPanel, "South");
/*     */ 
/* 325 */     this.errorLabel.setFont(new Font(null, 2, 16));
/* 326 */     this.errorLabel.setForeground(new Color(8388608));
/* 327 */     panel.add(this.errorLabel, "North");
/*     */ 
/* 329 */     return panel;
/*     */   }
/*     */ 
/*     */   public void setError(String errorMessage) {
/* 333 */     removeAll();
/* 334 */     add(buildLoginPanel());
/* 335 */     this.errorLabel.setText(errorMessage);
/* 336 */     validate();
/*     */   }
/*     */ 
/*     */   public void loginOk() {
/* 340 */     writeUsername();
/*     */   }
/*     */ }

