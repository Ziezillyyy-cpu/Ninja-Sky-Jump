package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Random;


public class Menu extends JPanel {


    private JFrame frame;


    private Image background;
    private Image dayBackground;
    private Image nightBackground;

    private Image logo;
    private Image ninja;
    private Image startImg;
    private Image exitImg;



    private Rectangle startButton;
    private Rectangle exitButton;


    private boolean hoverStart = false;
    private boolean hoverExit = false;



    // ================= DAUN =================

    private ArrayList<Leaf> leaves = new ArrayList<>();

    private Random random = new Random();



    private class Leaf {

        float x;
        float y;
        float speed;
        float size;

    }



    // ================= SIANG MALAM =================

    private boolean isNight = false;

    private Timer backgroundTimer;






    public Menu(JFrame frame){


        this.frame = frame;


        setPreferredSize(
                new Dimension(
                        GamePanel.WIDTH,
                        GamePanel.HEIGHT
                )
        );


        setLayout(null);





        // ================= BACKGROUND =================


        dayBackground = new ImageIcon(
                getClass().getResource(
                        "/game/resources/menu/menu.png"
                )
        ).getImage();



        nightBackground = new ImageIcon(
                getClass().getResource(
                        "/game/resources/menu/menu 2.png"
                )
        ).getImage();



        background = dayBackground;








        // ================= GAMBAR =================


        logo = new ImageIcon(
                getClass().getResource(
                        "/game/resources/menu/judul game.png"
                )
        ).getImage();



        ninja = new ImageIcon(
                getClass().getResource(
                        "/game/resources/menu/ninja.png"
                )
        ).getImage();



        startImg = new ImageIcon(
                getClass().getResource(
                        "/game/resources/menu/Start Game.png"
                )
        ).getImage();



        exitImg = new ImageIcon(
                getClass().getResource(
                        "/game/resources/menu/Exit.png"
                )
        ).getImage();









        // ================= BUAT DAUN =================


        for(int i = 0; i < 20; i++){


            Leaf leaf = new Leaf();


            leaf.x = random.nextInt(GamePanel.WIDTH);

            leaf.y = random.nextInt(GamePanel.HEIGHT);


            leaf.speed = 1 + random.nextFloat() * 2;


            leaf.size = 8 + random.nextFloat() * 8;


            leaves.add(leaf);

        }









        // ================= BUTTON AREA =================


        startButton = new Rectangle(
                150,
                485,
                320,
                100
        );



        exitButton = new Rectangle(
                150,
                590,
                320,
                100
        );









        addMouseListener(new MouseAdapter(){


            @Override
            public void mouseClicked(MouseEvent e){


                if(startButton.contains(e.getPoint())){


                    frame.getContentPane().removeAll();


                    GamePanel gp = new GamePanel();


                    frame.add(gp);


                    frame.revalidate();

                    frame.repaint();


                    gp.requestFocusInWindow();


                }





                if(exitButton.contains(e.getPoint())){


                    System.exit(0);


                }


            }


        });










        addMouseMotionListener(new MouseMotionAdapter(){


            @Override
            public void mouseMoved(MouseEvent e){


                hoverStart =
                        startButton.contains(e.getPoint());



                hoverExit =
                        exitButton.contains(e.getPoint());



                repaint();


            }


        });









        // ================= ANIMASI DAUN =================


        Timer leafTimer = new Timer(40, e -> {



            for(Leaf leaf : leaves){



                leaf.y += leaf.speed;



                if(leaf.y > GamePanel.HEIGHT + 20){


                    leaf.y = -20;


                    leaf.x =
                            random.nextInt(GamePanel.WIDTH);


                }



            }



            repaint();



        });



        leafTimer.start();










        // ================= GANTI SIANG MALAM =================


        backgroundTimer = new Timer(10000, e -> {



            isNight = !isNight;



            if(isNight){


                background = nightBackground;


            }else{


                background = dayBackground;


            }



            repaint();



        });



        backgroundTimer.start();





    }













    @Override
    protected void paintComponent(Graphics g){


        super.paintComponent(g);



        Graphics2D g2 =
                (Graphics2D)g;



        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );







        // ================= BACKGROUND =================


        g2.drawImage(
                background,
                0,
                0,
                getWidth(),
                getHeight(),
                this
        );








        // ================= DAUN =================


        for(Leaf leaf : leaves){


            drawLeaf(
                    g2,
                    leaf.x,
                    leaf.y,
                    leaf.size
            );


        }










        // ================= LOGO =================


        drawImageCenter(
                g2,
                logo,
                300,
                120,
                430
        );









        // ================= NINJA =================


        drawImage(
                g2,
                ninja,
                20,
                570,
                150
        );










        // ================= BUTTON =================


        drawButton(
                g2,
                startImg,
                150,
                485,
                320,
                100,
                hoverStart
        );



        drawButton(
                g2,
                exitImg,
                150,
                590,
                320,
                100,
                hoverExit
        );



    }









    // ================= DAUN BENTUK =================


    private void drawLeaf(
            Graphics2D g2,
            float x,
            float y,
            float size
    ){


        Path2D leaf = new Path2D.Double();



        leaf.moveTo(
                x,
                y-size
        );



        leaf.curveTo(
                x+size,
                y-size/2,
                x+size,
                y+size/2,
                x,
                y+size
        );



        leaf.curveTo(
                x-size,
                y+size/2,
                x-size,
                y-size/2,
                x,
                y-size
        );



        g2.setColor(
                new Color(70,150,50,180)
        );


        g2.fill(leaf);


    }









    private void drawImageCenter(
            Graphics2D g2,
            Image img,
            int centerX,
            int y,
            int width
    ){


        int imgW =
                img.getWidth(this);



        int imgH =
                img.getHeight(this);



        double scale =
                (double)width / imgW;



        int height =
                (int)(imgH * scale);



        int x =
                centerX - width/2;



        g2.drawImage(
                img,
                x,
                y,
                width,
                height,
                this
        );


    }









    private void drawImage(
            Graphics2D g2,
            Image img,
            int x,
            int y,
            int width
    ){


        int imgW =
                img.getWidth(this);



        int imgH =
                img.getHeight(this);



        double scale =
                (double)width / imgW;



        int height =
                (int)(imgH*scale);



        g2.drawImage(
                img,
                x,
                y,
                width,
                height,
                this
        );


    }









    private void drawButton(
            Graphics2D g2,
            Image img,
            int x,
            int y,
            int w,
            int h,
            boolean hover
    ){


        if(hover){


            x -= 5;
            y -= 5;

            w += 10;
            h += 10;


        }



        g2.drawImage(
                img,
                x,
                y,
                w,
                h,
                this
        );


    }



}