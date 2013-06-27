package net.minecraft;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import javax.imageio.ImageIO;

public class LauncherFrame extends Frame
{
  public static final int VERSION = 15;
  private static final long serialVersionUID = 1L;
  private Launcher launcher;
  private LoginForm loginForm;
  public boolean forceUpdate = false;

  public LauncherFrame() {
    super("Minecraft Launcher by multiplayer-italia.com");
    //System.out.println("Hello!");
    setBackground(Color.BLACK);
    this.loginForm = new LoginForm(this);
    setLayout(new BorderLayout());
    add(this.loginForm, "Center");

    this.loginForm.setPreferredSize(new Dimension(854, 480));
    pack();
    setLocationRelativeTo(null);
    try {
      setIconImage(ImageIO.read(LauncherFrame.class.getResource("favicon.png")));
    } catch (IOException e1) {
      e1.printStackTrace();
    }

    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent arg0)
      {
        new Thread()
        {
          public void run()
          {
            try {
              Thread.sleep(30000L);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            System.out.println("FORCING EXIT!");
            System.exit(0);
          }
        }
        .start();

        if (LauncherFrame.this.launcher != null) {
          LauncherFrame.this.launcher.stop();
          LauncherFrame.this.launcher.destroy();
        }
        System.exit(0);
      } } );
  }

  public String getFakeResult(String userName) {
    return MinecraftUtil.getFakeLatestVersion() + ":35b9fd01865fda9d70b157e244cf801c:" + userName + ":12345:";
  }

  public void login(String userName) {
    String result = getFakeResult(userName);
    String[] values = result.split(":");
    this.launcher = new Launcher();
    this.launcher.forceUpdate = this.forceUpdate;
    this.launcher.customParameters.put("userName", values[2].trim());
    this.launcher.customParameters.put("sessionId", values[3].trim());
    this.launcher.init();
    removeAll();
    add(this.launcher, "Center");
    validate();
    this.launcher.start();
    this.loginForm.loginOk();
    this.loginForm = null;
    setTitle("Minecraft");
  }

  private void showError(String error) {
    removeAll();
    add(this.loginForm);
    this.loginForm.setError(error);
    validate();
  }

  public boolean canPlayOffline(String userName) {
    Launcher launcher2 = new Launcher();
    launcher2.init(userName, "12345");
    return launcher2.canPlayOffline();
  }

  public static void main(String[] args) {
    LauncherFrame launcherFrame = new LauncherFrame();
    launcherFrame.setVisible(true);
  }
}