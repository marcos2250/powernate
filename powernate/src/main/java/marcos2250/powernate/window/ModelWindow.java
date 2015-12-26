package marcos2250.powernate.window;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import marcos2250.powernate.graph.Node;
import marcos2250.powernate.graph.RelationalNode;
import marcos2250.powernate.util.PowernateSessionMediator;
import marcos2250.powernate.vbscript.PowerDesignerVBScriptGenerator;

public class ModelWindow extends JInternalFrame {

    private static final long serialVersionUID = -1501421300152452657L;

    private ModelView view;

    private boolean cfgShowGrid = true;
    private boolean cfgMarkName = true;
    private boolean dragging;

    private float origemX;
    private float origemY;
    private float origemClickX;
    private float origemClickY;
    private float mouseX;
    private float mouseY;
    private float mouseClickX;
    private float mouseClickY;

    private float escala = 100;

    private double espacamentoDaGrade;

    private Graphics graphics;

    private MainWindow mainWindow;

    public ModelWindow(MainWindow mainWindow) {

        super("Model view", true, // resizable
                false, // closable
                true, // maximizable
                true);// iconifiable
        this.mainWindow = mainWindow;

        setSize(640, 480);

        view = new ModelView();

        view.setFocusable(true);
        view.requestFocusInWindow();

        view.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (KeyEvent.VK_UP == e.getKeyCode()) {
                    moverPosicao(0, 1);
                }
                if (KeyEvent.VK_DOWN == e.getKeyCode()) {
                    moverPosicao(0, -1);
                }
                if (KeyEvent.VK_LEFT == e.getKeyCode()) {
                    moverPosicao(-1, 0);
                }
                if (KeyEvent.VK_RIGHT == e.getKeyCode()) {
                    moverPosicao(1, 0);
                }

                if (KeyEvent.VK_EQUALS == e.getKeyCode()) {
                    aplicarZoom(1.1f);
                }
                if (KeyEvent.VK_MINUS == e.getKeyCode()) {
                    aplicarZoom(0.9f);
                }
                view.repaint();
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseClickX = e.getX();
                mouseClickY = e.getY();
                origemClickX = origemX;
                origemClickY = origemY;

                if (e.getButton() == MouseEvent.BUTTON1) {
                    dragging = true;
                }
            }
        });

        view.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    dragging = false;
                }
            }
        });

        view.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                view.repaint();
            }
        });

        getContentPane().add(view);
    }

    private class ModelView extends JPanel {

        private static final long serialVersionUID = -8630160951581942156L;

        public ModelView() {
            setBorder(BorderFactory.createLineBorder(Color.WHITE));
            setBackground(Color.WHITE);
        }

        public Dimension getPreferredSize() {
            return new Dimension(10, 10);
        }

        public void paint(Graphics g) {
            super.paint(g);
            graphics = g;
            atualizaGrafico();
        }

    }

    private void atualizaGrafico() {

        if (dragging) {
            origemX = origemClickX - ((mouseX - mouseClickX) / escala);
            origemY = origemClickY + ((mouseY - mouseClickY) / escala);
        }

        if (cfgShowGrid) {
            desenharReguas();
        }

        if (getModeler() == null || getModeler().getGraphGenerator().getAllNodes().size() < 2) {
            return;
        }

        desenhaModelo();

    }

    private void desenharReguas() {
        if (escala > 0.5) {
            espacamentoDaGrade = Math.pow(10, 5 - String.valueOf(Math.round(escala)).length()) / 100;
        } else {
            espacamentoDaGrade = Math.round(1 / escala) * 1000;
        }

        if (espacamentoDaGrade < 1) {
            espacamentoDaGrade = 1;
        }

        graphics.setColor(Color.GRAY);

        double gradeSpcOffset;
        int gradeInicio, gradeFim;
        int x, y;

        gradeInicio = (int) (origemX / espacamentoDaGrade);
        gradeFim = (int) ((origemX / espacamentoDaGrade) + ((view.getWidth() / escala) / espacamentoDaGrade) + 1);

        for (int i = gradeInicio; i <= gradeFim; i++) {
            gradeSpcOffset = i * espacamentoDaGrade;
            x = (int) ((-origemX + gradeSpcOffset) * escala);
            graphics.drawLine(x, 0, x, view.getHeight());
        }

        gradeInicio = (int) (origemY / espacamentoDaGrade);
        gradeFim = (int) ((origemY / espacamentoDaGrade) + ((view.getHeight() / escala) / espacamentoDaGrade) + 1);

        for (int i = gradeInicio; i <= gradeFim; i++) {
            gradeSpcOffset = i * espacamentoDaGrade;
            y = (int) (view.getHeight() - (-origemY + gradeSpcOffset) * escala);
            graphics.drawLine(0, y, (int) view.getWidth(), y);
        }
    }

    private void desenhaModelo() {

        int pontoX, pontoY, pontoX1, pontoY1;

        Set<Node> nodes = getModeler().getGraphGenerator().getAllNodes();

        for (Node node : nodes) {

            // nos
            pontoX = (int) ((node.getCoordinateX() - origemX) * escala);
            pontoY = (int) (view.getHeight() - ((node.getCoordinateY() - origemY) * escala));

            graphics.setColor(getConfig().getAWTColor(node.getNodeClass()));
            graphics.drawOval(pontoX - 2, pontoY - 2, 4, 4);

            if (cfgMarkName) {
                imprime(node.toString().substring(0, 3), pontoX, pontoY);
            }

            // linhas
            Set<RelationalNode> neighboringRelationships = node.getNeighboringRelationships();

            graphics.setColor(Color.BLACK);

            for (RelationalNode relationalNode : neighboringRelationships) {

                pontoX1 = (int) ((relationalNode.getCoordinateX() - origemX) * escala);
                pontoY1 = (int) (view.getHeight() - ((relationalNode.getCoordinateY() - origemY) * escala));

                graphics.drawLine((int) pontoX, (int) pontoY, (int) pontoX1, (int) pontoY1);
            }

        }

    }

    private void aplicarZoom(float s) {
        if (escala < 0.01 && s < 1) {
            return;
        }
        if (escala > 6000 && s > 1) {
            return;
        }
        if (s == 0) {
            return;
        }

        float w = view.getWidth();
        float h = view.getHeight();

        float x = (w / (escala * s)) - (w / escala);
        float y = (h / (escala * s)) - (h / escala);

        origemX = origemX - (x / 2);
        origemY = origemY - (y / 2);
        escala = escala * s;
    }

    public void fitView() {

        float minX, minY, maxX, maxY;
        minX = 10000;
        minY = 10000;
        maxX = 0;
        maxY = 0;

        if (getModeler() == null || getModeler().getGraphGenerator() == null
                || getModeler().getGraphGenerator().getAllNodes() == null
                || getModeler().getGraphGenerator().getAllNodes().size() == 0) {
            return;
        }
        Set<Node> nodes = getModeler().getGraphGenerator().getAllNodes();

        for (Node node : nodes) {
            if (node.getCoordinateX() < minX) {
                minX = (float) node.getCoordinateX();
            }
            if (node.getCoordinateX() < minY) {
                minY = (float) node.getCoordinateY();
            }
            if (node.getCoordinateX() > maxX) {
                maxX = (float) node.getCoordinateX();
            }
            if (node.getCoordinateX() > maxY) {
                maxY = (float) node.getCoordinateY();
            }
        }

        origemX = (maxX - minX) / 2;
        origemY = (maxY - minY) / 2;

        float w = view.getWidth();
        float h = view.getHeight();

        if ((Math.abs(maxX - minX) / Math.abs(maxY - minY)) > (w / h)) {
            escala = (float) (((w / Math.abs(maxX - minX))) * 0.9);
        } else {
            escala = (float) (((h / Math.abs(maxY - minY))) * 0.9);
        }

        if (escala != 0) {
            origemX = ((maxX + minX) / 2) - (w / 2) / escala;
            origemY = ((maxY + minY) / 2) - (h / 2) / escala;
        } else {
            escala = 1;
        }

    }

    private void moverPosicao(int x, int y) {
        origemX = origemX + (x / escala) * 10;
        origemY = origemY + (y / escala) * 10;
    }

    private void imprime(String texto, double x, double y) {
        graphics.drawString(texto, (int) x, (int) y);
    }

    private PowerDesignerVBScriptGenerator getModeler() {
        return mainWindow.getModeler();
    }

    private PowernateSessionMediator getConfig() {
        return mainWindow.getConfig();
    }

    public void atualizarView() {
        repaint();
        fitView();
    	if (graphics != null) {
    		atualizaGrafico();
    	}
    }

}
