package chess;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import chess.Control.Piecetype;
import chess.Control.Piecetype.Team;

public class View extends JFrame{
	private static final long serialVersionUID = 1L;
	/**
	 * -1 Empty space
	 *  0 green Ring, possible move
	 *  1 White King
	 *  2 White Queen
	 *  3 White Rook
	 *  4 White Bishop
	 *  5 White Knight
	 *  6 White Pawn
	 *  7 Black King
	 *  8 Black Queen
	 *  9 Black Rook
	 * 10 Black Bishop
	 * 11 Black Knight
	 * 12 Black Pawn
	 */
	public final static String[] pieces = {"\u25EF","\u2654","\u2655","\u2656","\u2657","\u2658","\u2659","\u265A","\u265B","\u265C","\u265D","\u265E","\u265F"};
	private int fieldsize;
	private final JPanel panel;
	private final static int width = 8;
	private final static int height = 8;
	private JLabel [][] pattern;
	private Font font;
	public Color white, dark, choosefield;
	public View() {
		this("Schach",50);
	}
	public View(String name, int size) {
		this.fieldsize = size;
		JPanel surround = new JPanel(new BorderLayout());
		add(surround);
		JPanel characters = new JPanel(new GridLayout(1, width+2));
		JPanel numbers = new JPanel(new GridLayout(height,1));
		//Generate numbers 
		JLabel jl = new JLabel();
		//jl.setSize(fieldsize/5,fieldsize/5);
		//characters.add(new JLabel());
		for(int i = 0; i < width;i++) {
			jl = new JLabel(""+((char)(65+i)));
			jl.setHorizontalAlignment(SwingConstants.CENTER);
			jl.setSize(fieldsize,fieldsize/5);
			characters.add(jl);
		}
		for(int i = height; i > 0;i--) {
			jl = new JLabel(""+i);
			jl.setVerticalAlignment(SwingConstants.CENTER);
			jl.setSize(fieldsize/5,fieldsize);
			numbers.add(jl);
		}
		//surround.add(BorderLayout.NORTH,characters);
		surround.add(BorderLayout.SOUTH,characters);
		//surround.add(BorderLayout.EAST,numbers);
		surround.add(BorderLayout.WEST,numbers);
		panel = new JPanel(new GridLayout(width,height));
		surround.add(BorderLayout.CENTER,panel);
		
		setSize(fieldsize*(width+1), fieldsize*(height+1));
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.white = Color.WHITE;
		this.dark = Color.DARK_GRAY;
		this.choosefield = Color.GREEN;
		this.font = panel.getFont().deriveFont(50.0f);
		init();
        setVisible(true);  
	}
	private void init() {
		//Draw Background
		pattern = new JLabel[width][height];
		JLabel field;
		int i = 0;
		for(int y = 0; y < height;y++) {
			for(int x = 0; x < width;x++) {
				field = new JLabel();
				field.setText(i+++"");
				field.setOpaque(true);				//Why standard see-through?
				field.setSize(fieldsize,fieldsize);
				field.setLocation(0, 0);
				field.setFont(font);
				field.setHorizontalAlignment(SwingConstants.CENTER);
				field.setVerticalAlignment(SwingConstants.CENTER);
				field.setBackground(((x+y)%2 == 0)?white:dark);

				panel.add(field);
				pattern[x][y] = field;
			}
		}
		
		
	}
	public void ConnectWithController(Control c) {
		addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				// TODO Auto-generated method stub
				font = font.deriveFont((float)Math.min(panel.getWidth()/width, panel.getHeight()/height));
				for(int x = 0; x < pattern.length;x++) {
					for(int y = 0; y < pattern[x].length;y++) {
						pattern[x][y].setFont(font);
					}
				}
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		panel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
					//nothing
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				c.clicked((e.getX()*width)/(panel.getWidth()),(e.getY()*height)/(panel.getHeight()));
			}
		});
		addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_R) {
					c.restart();
				}
				if(e.getKeyCode() == KeyEvent.VK_LEFT) {
					c.undo();
				}
				if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
					c.redo();
				}
				
			}
		});
	}
	/**
	 * Draws the Pieces with the index given in toset onto the Board
	 * Precondition toset.length == width && toset[].length == height && given value in toset must be a vaild index of this.pieces or -1
	 * @param toset
	 */
	public void playground(Piecetype[][] toset) {
		font = font.deriveFont((float)Math.min(panel.getWidth()/width, panel.getHeight()/height));
		for(int x = 0; x < toset.length;x++) {
			for(int y = 0; y < toset[x].length;y++) {
				pattern[x][y].setFont(font);
				pattern[x][y].setText(toset[x][y].symbol);
				/*
				if(toset[x][y] == Piecetype.RING) {
					pattern[x][y].setForeground(choosefield);
				}else {
					pattern[x][y].setForeground(new Color(51,51,51));

				}*/
			}
		}
	}
	private List<Move> lastoptions;
	public void showMoves(List<Move> moves) {
		int[] temp;
		if(lastoptions != null) {
			for(Move m : lastoptions) {
				temp = m.to();
				pattern[temp[0]][temp[1]].setBackground(((temp[0]+temp[1])%2 == 0)?white:dark);
			}
		}
		if(moves != null)
		for(Move m : moves) {
			temp = m.to();
			pattern[temp[0]][temp[1]].setBackground(choosefield);
		}
		lastoptions = moves;
	}
	public Piecetype promote(Team team) {
		Object[] options = (team == Team.WHITE?
					new Object[]{Piecetype.WHITE_QUEEN.symbol,Piecetype.WHITE_ROOK.symbol,Piecetype.WHITE_BISHOP.symbol,Piecetype.WHITE_KNIGHT.symbol}:
					new Object[]{Piecetype.DARK_QUEEN.symbol,Piecetype.DARK_ROOK.symbol,Piecetype.DARK_BISHOP.symbol,Piecetype.DARK_KNIGHT.symbol});
		int result = JOptionPane.showOptionDialog(this, "Promote to", "Promotion Dialog", JOptionPane.INFORMATION_MESSAGE, 0, null, options, options[1]);
		System.out.println(result);
		for(Piecetype p : Piecetype.values()) {
			if(p.symbol.equals(options[result])) {
				return p;
			}
		}
		return null;
		
	}

}
