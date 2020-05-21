import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.awt.*;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

/**
 * Represents a window that can be drawn in using a software renderer.
 */

import java.util.concurrent.*;


public class Display extends Canvas implements EnableDisableI
{
	/** The window being used for display */
	public final JFrame         m_frame;
	/** The bitmap representing the final image to display */
	private final RenderContext  m_frameBuffer;
	private final BufferedImage  m_displayImage;
	/** The pixels of the display image, as an array of byte components */
	private final byte[]         m_displayComponents;
	/** The buffers in the Canvas */
	private final BufferStrategy m_bufferStrategy;
	/** A graphics object that can draw into the Canvas's buffers */
	private final Graphics       m_graphics;
	
	//public ErythrocyteInfo info;

	volatile ArrayList<ErythrocyteForm> erys = new ArrayList<ErythrocyteForm>();
	volatile ArrayList<Model> objs  = new ArrayList<Model>();

	private JButton addBtn;
	private JButton detectDtn;
	
    private  JButton setImage = null;
    JLabel  imageLabel    = null;
	protected  JLabel  imageAmount    = new JLabel("Загружено изображений:0");

	private  JLabel  erysAmount    = new JLabel("Эритроцитов:0");
    
    private  JButton  setTexture    = null;
    JLabel  textureLabel    = null;
	protected  JLabel  textureAmount    = new JLabel("Загружено текстур:0");

	private  JButton  setForm    = null;
	JLabel  formLabel    = null;
	protected  JLabel  formAmount    = new JLabel("Загружено форм:0");

	private  JLabel  imageHelper    = new JLabel("Левой кнопкой мыши выделите участок на");
	private  JLabel  imageHelper1    = new JLabel("изображении ниже. Для автоматического");
	private  JLabel  imageHelper2    = new JLabel("определения нажмите на 'определить'");
	private  JLabel  imageHelper3    = new JLabel("");

    private  JFileChooser fileChooser = null;

	private imagePanel image = null;

	private JSlider form_slide, gemoglob_slide, oxygen_slide;
	private JLabel def, gemoglob_label, oxygen_label;
	public JLabel errorField = new JLabel();
	private JButton updateButton = new JButton();
	private JLabel loadingLabel = new JLabel();

	protected RenderContext GetFrameBuffer() { return m_frameBuffer; }

	protected int change_x, change_y, change_z;

	protected	JPanel eryPanel = new JPanel();
	protected JScrollPane scrollPane = null;
	protected	JPanel imagesPanel = new JPanel();
	protected JScrollPane imagesPane = null;
	protected	JPanel texturePanel = new JPanel();
	protected JScrollPane texturePane = null;
	protected	JPanel formPanel = new JPanel();
	protected JScrollPane formPane = null;

	protected EryObjectForm choosenImage = null;
	protected EryObjectForm choosenTexture = null;
	protected EryObjectForm choosenForm = null;

	public Boolean wantUpdate = false;
    
    private JButton downBtn, upBtn, leftBtn, rightBtn;
    
    private JSlider far_slider;

	private boolean left_pressed;
	private boolean right_pressed;
	private boolean up_pressed;
	private boolean down_pressed;

	JLabel label_scale_factor, label_min_neighbors, label_min_size, label_max_size;
	String label_scale_factor_s="Коэффициент масштабируемости ", label_min_neighbors_s="Количество соседей ", label_min_size_s="Наименьший размер ", label_max_size_s="Наибольший размер ";
	int label_scale_factor_v = 100, label_min_neighbors_v = 10, label_min_size_v = 10, label_max_size_v = 100;
	private JSlider scale_factor, min_neighbors, min_size, max_size;

	Boolean pause = false;

	ReentrantLock allowEryUpdate = new ReentrantLock();

	TokenProjectScene TPS = null;

	private JCheckBox toAll = new JCheckBox("Применять действие ко всем");

	protected void DeleteAllEry() {
		for(Iterator<ErythrocyteForm> iter = erys.iterator(); iter.hasNext();)
		{
			ErythrocyteForm form = iter.next();
			display.actionErythrocyte(form, false, false);
		}
	}

	int fieldWidth = 0;
	int fieldHeight = 0;
    
	public Display(int pos_x, int pos_y, int width, int height, TokenProjectScene tps, SceneWithObjects scene)
	{
		fieldWidth = width / 10 * 5; //width / 3 * 2
		fieldHeight = height / 3 * 2-60;
		this.TPS = tps;

		Dimension size = new Dimension(width, height);
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);

		//Creates images used for display.
		m_frameBuffer = new RenderContext(width, height);
		m_displayImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		m_displayComponents = 
			((DataBufferByte)m_displayImage.getRaster().getDataBuffer()).getData();

		m_frameBuffer.Clear((byte)0x80);
		m_frameBuffer.DrawPixel(100, 100, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xFF);

		//Create a JFrame designed specifically to show this Display.
		
		//m_frame.add(this);
		m_frame = new JFrame();

		m_frame.setLocation(pos_x, pos_y);
		
		image = new imagePanel(400, 400);

		label_scale_factor = new JLabel(label_scale_factor_s+label_scale_factor_v);
		label_min_neighbors = new JLabel(label_min_neighbors_s+label_min_neighbors_v);
		label_min_size = new JLabel(label_min_size_s+label_min_size_v);
		label_max_size = new JLabel(label_max_size_s+label_max_size_v);

		scale_factor = new JSlider(1, 100, label_scale_factor_v);
		min_neighbors = new JSlider(1, 50, label_min_neighbors_v);
		min_size = new JSlider(5, 300, label_min_size_v);
		max_size = new JSlider(10, 500, label_max_size_v);

		scale_factor.addChangeListener(event -> {
			int v = scale_factor.getValue();
			label_scale_factor_v = v;
			label_scale_factor.setText(label_scale_factor_s+v);
		});

		min_neighbors.addChangeListener(event -> {
			int v = min_neighbors.getValue();
			label_min_neighbors_v = v;
			label_min_neighbors.setText(label_min_neighbors_s+v);
		});

		min_size.addChangeListener(event -> {
			int v = min_size.getValue();
			label_min_size_v = v;
			label_min_size.setText(label_min_size_s+v);
		});

		max_size.addChangeListener(event -> {
			int v = max_size.getValue();
			label_max_size_v = v;
			label_max_size.setText(label_max_size_s+v);
		});

		Dictionary <Integer, JLabel> scale_factor_labels = new Hashtable<Integer, JLabel>();
		scale_factor_labels.put(100, new JLabel("<html><font color=gray size=4>10"));
		scale_factor_labels.put(50, new JLabel("<html><font color=gray size=4>5"));
		scale_factor_labels.put(25, new JLabel("<html><font color=gray size=4>2.5"));
		scale_factor_labels.put(10, new JLabel("<html><font color=gray size=4>1"));
		scale_factor_labels.put(1, new JLabel("<html><font color=gray size=4>0.1"));
		scale_factor.setLabelTable(scale_factor_labels);
		scale_factor.setPaintLabels(true);

		Dictionary <Integer, JLabel> min_neighbors_labels = new Hashtable<Integer, JLabel>();
		min_neighbors_labels.put(50, new JLabel("<html><font color=gray size=4>50"));
		min_neighbors_labels.put(40, new JLabel("<html><font color=gray size=4>40"));
		min_neighbors_labels.put(30, new JLabel("<html><font color=gray size=4>30"));
		min_neighbors_labels.put(20, new JLabel("<html><font color=gray size=4>20"));
		min_neighbors_labels.put(10, new JLabel("<html><font color=gray size=4>10"));
		min_neighbors_labels.put(1, new JLabel("<html><font color=gray size=4>1"));
		min_neighbors.setLabelTable(min_neighbors_labels);
		min_neighbors.setPaintLabels(true);

		Dictionary <Integer, JLabel> min_size_labels = new Hashtable<Integer, JLabel>();
		min_size_labels.put(300, new JLabel("<html><font color=gray size=4>300"));
		min_size_labels.put(150, new JLabel("<html><font color=gray size=4>150"));
		min_size_labels.put(50, new JLabel("<html><font color=gray size=4>50"));
		min_size_labels.put(5, new JLabel("<html><font color=gray size=4>5"));
		min_size.setLabelTable(min_size_labels);
		min_size.setPaintLabels(true);

		Dictionary <Integer, JLabel> max_size_labels = new Hashtable<Integer, JLabel>();
		max_size_labels.put(500, new JLabel("<html><font color=gray size=4>500"));
		max_size_labels.put(250, new JLabel("<html><font color=gray size=4>250"));
		max_size_labels.put(100, new JLabel("<html><font color=gray size=4>100"));
		max_size_labels.put(10, new JLabel("<html><font color=gray size=4>10"));
		max_size.setLabelTable(max_size_labels);
		max_size.setPaintLabels(true);

		setImage = new JButton("Загрузить снимок");
		setImage.setBounds(fieldWidth + 50, 55, 230, 20);
		
	    imageLabel = new JLabel("Выбрано: 'По умолчанию'");
	    imageLabel.setBounds(fieldWidth + 50, 460, 230, 20);
		imageAmount.setBounds(fieldWidth + 50, 80, 230, 20);

		final String dir = System.getProperty("user.dir");
		System.out.println("current dir = " + dir);

	    setTexture = new JButton("Загрузить текстуру");
	    setTexture.setBounds(fieldWidth + 290, 55, 230, 20);

	    textureLabel = new JLabel("Выбрано: 'По умолчанию'");
	    textureLabel.setBounds(fieldWidth  + 290, 460, 230, 20);
		textureAmount.setBounds(fieldWidth + 290, 80, 230, 20);

		setForm = new JButton("Загрузить форму");
		setForm.setBounds(fieldWidth + 530, 55, 230, 20);

		formLabel = new JLabel("Выбрано: 'По умолчанию'");
		formLabel.setBounds(fieldWidth  + 530, 460, 230, 20);
		formAmount.setBounds(fieldWidth + 530, 80, 230, 20);

		fileChooser = new JFileChooser();
		 
        image.setBounds(fieldWidth + 50, 480, 330, 330);
		imageHelper.setBounds(fieldWidth + 340 + 60, 485, 350, 20);
		imageHelper1.setBounds(fieldWidth + 340 + 60, 505, 350, 20);
		imageHelper2.setBounds(fieldWidth + 340 + 60, 525, 350, 20);
		imageHelper3.setBounds(fieldWidth + 340 + 60, 545, 350, 20);

		label_scale_factor.setBounds(fieldWidth + 340 + 60, 565, 300, 20);
		label_min_neighbors.setBounds(fieldWidth + 340 + 60, 615, 300, 20);
		label_min_size.setBounds(fieldWidth + 340 + 60, 665, 300, 20);
		label_max_size.setBounds(fieldWidth + 340 + 60, 715, 300, 20);

		scale_factor.setBounds(fieldWidth + 340 + 60, 585, 300, 30);
		min_neighbors.setBounds(fieldWidth + 340 + 60, 635, 300, 30);
		min_size.setBounds(fieldWidth + 340 + 60, 685, 300, 30);
		max_size.setBounds(fieldWidth + 340 + 60, 735, 300, 30);

		errorField.setVisible(false);

		updateButton.setBounds(10, 10, 150, 20);
		updateButton.setText("Обновить");

		toAll.setBounds(160, 10, 300, 20);
		toAll.setBackground(Color.decode("#F4F5F6"));

		loadingLabel.setBounds(10, 30,500, 20);
		loadingLabel.setText("Загрузка");
		loadingLabel.setVisible(false);

		errorField.setBounds(10, 10, 700, 20);

		updateButton.addActionListener(actionEvent -> {
			wantUpdate = true;
			SceneWithObjectsResponse swo = ApiHandler.getScene(TPS);
			if (ApiHandler.hasError(errorField, swo, false)) {
				return;
			}
			Main.Lauch(tps.token, tps.projectID, swo.sceneWithObjects);
			m_frame.dispose();
		});
		errorField.setVisible(false);
        
        addBtn = new JButton("Добавить");
		addBtn.setBounds(fieldWidth + 400, 785, 150, 20);

		detectDtn = new JButton("Определить");
		detectDtn.setBounds(fieldWidth + 570, 785, 150, 20);
        
        ErythrocyteInfo info = new ErythrocyteInfo(0, 0, 0, 0);
		info.addMeta(true);

		gemoglob_slide = new JSlider((int)(100*info.min_gemog),
				(int)(100*info.max_gemog), (int)(100*(int)info.gemoglob));

		oxygen_slide = new JSlider((int)(100*info.min_oxygen),
				(int)(100*info.max_oxygen), (int)(100*info.oxygen));
		
		gemoglob_label = new JLabel("Цветовой показатель крови:1");
		oxygen_label = new JLabel("Индекс сатурации:1");
		
		oxygen_label.setBounds(435, 210, 200, 20);
		gemoglob_label.setBounds(235, 210, 200, 20);
		
		
		gemoglob_slide.addChangeListener(event -> {
			info.gemoglob = (float)(gemoglob_slide.getValue()) / 100;
			gemoglob_label.setText("Цветовой показатель крови:" + info.gemoglob);
		  });
		
		oxygen_slide.addChangeListener(event -> {
			info.oxygen = (float)(oxygen_slide.getValue()) / 100;
			oxygen_label.setText("Индекс сатурации:" + info.oxygen);
		  });
		
		def = new JLabel("Коэффициент деформации:" + info.form / 2);

		form_slide = new JSlider((int)(100*info.min_form),
				(int)(100*info.max_form), (int)(100*info.form));
		form_slide.addChangeListener(event -> {
			info.form = (float)(form_slide.getValue()) / 100;
			def.setText("Коэффициент деформации:" + info.form / 2);
		  });
		// 430, 420
		gemoglob_label.setBounds(fieldWidth + 300, 5, 250, 15);
		gemoglob_slide.setBounds(fieldWidth + 300, 20, 250, 30);
		oxygen_label.setBounds(fieldWidth + 600, 5, 220, 15);
		oxygen_slide.setBounds(fieldWidth + 550, 20, 200, 30);
		def.setBounds(fieldWidth + 50, 5, 250, 15);
		form_slide.setBounds(fieldWidth + 40, 20, 250, 30);
        
		Dictionary <Integer, JLabel> form_labels = new Hashtable<Integer, JLabel>();
		form_labels.put(85, new JLabel("<html><font color=red size=4>0.425"));
        form_labels.put(40, new JLabel("<html><font color=gray size=4>0.2"));
        form_labels.put(0, new JLabel("<html><font color=gray size=4>0"));
        form_labels.put(-20, new JLabel("<html><font color=gray size=4>-0.1"));
        form_labels.put(-50, new JLabel("<html><font color=red size=4>-0.25"));
		
        form_slide.setLabelTable(form_labels);
		
        form_slide.setPaintLabels(true);
        
        Dictionary <Integer, JLabel> gemob_labels = new Hashtable<Integer, JLabel>();
        // гиперхромная анемия 110
        // микроцитарная анемии 80
        gemob_labels.put(110, new JLabel("<html><font color=red size=4>1.1"));
        gemob_labels.put(100, new JLabel("<html><font color=gray size=4>1"));
        gemob_labels.put(80, new JLabel("<html><font color=red size=4>0.8"));
		
        gemoglob_slide.setLabelTable(gemob_labels);
        gemoglob_slide.setPaintLabels(true);
        
        Dictionary <Integer, JLabel> oxygen_labels = new Hashtable<Integer, JLabel>();
        // обычного взрослого 98
        oxygen_labels.put(98, new JLabel("<html><font color=green size=4>0.98"));
        // курящего 92
        //oxygen_labels.put(new Integer(92), new JLabel("<html><font color=blue size=4>0.92"));
        // инфекционные заболевания 88
        oxygen_labels.put(88, new JLabel("<html><font color=red size=4>0.88"));
        /*
        oxygen_labels.put(new Integer(44), new JLabel("<html><font color=gray size=4>норма новорожденного"));
        oxygen_labels.put(new Integer(34), new JLabel("<html><font color=gray size=4>лёгкая гипоксия"));
        oxygen_labels.put(new Integer(24), new JLabel("<html><font color=gray size=4>тяжелая гипоксия"));
		*/
        oxygen_slide.setLabelTable(oxygen_labels);
        oxygen_slide.setPaintLabels(true);
        
        addBtn.setEnabled(false);

        scrollPane = new JScrollPane(eryPanel);

		eryPanel.setLayout(new BoxLayout(eryPanel, BoxLayout.X_AXIS));
		scrollPane.setBounds(10, fieldHeight+20, fieldWidth, 290);

		erysAmount.setBounds(10, fieldHeight, 200, 20);

		imagesPane = new JScrollPane(imagesPanel);

		imagesPanel.setLayout(new BoxLayout(imagesPanel, BoxLayout.Y_AXIS));
		imagesPane.setBounds(fieldWidth + 50, 100, 230, 360);

		texturePane = new JScrollPane(texturePanel);

		texturePanel.setLayout(new BoxLayout(texturePanel, BoxLayout.Y_AXIS));
		texturePane.setBounds(fieldWidth + 290, 100, 230, 360);

		formPane = new JScrollPane(formPanel);;

		formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
		formPane.setBounds(fieldWidth + 530, 100, 230, 360);
        
        BoundedRangeModel model = new DefaultBoundedRangeModel(0, 0, -200, 1000);
        far_slider = new JSlider(model);
        
        far_slider.setOrientation(JSlider.VERTICAL);
        far_slider.setMajorTickSpacing(50);
        far_slider.setMinorTickSpacing(10);
        far_slider.setPaintTicks(true);
        far_slider.setBounds(fieldWidth + 5, 10, 40, fieldHeight);
        far_slider.setEnabled(false);
        
        change_z = 0;
        
        far_slider.addChangeListener(new ChangeListener() {
        	@Override
            public void stateChanged(ChangeEvent e) {
        		change_z = far_slider.getValue();
            }
        });
        
        Font BigFontTR = new Font("TimesRoman", Font.BOLD, 15);
        
        leftBtn = new JButton("←");
        leftBtn.setFont(BigFontTR);
        leftBtn.setBackground(new Color(150,150,150,255));
        leftBtn.setForeground(new Color(255,255,255,255));

        rightBtn = new JButton("→");
        rightBtn.setFont(BigFontTR);
        rightBtn.setBackground(new Color(150,150,150,255));
        rightBtn.setForeground(new Color(255,255,255,255));
        
        upBtn = new JButton("↑");
        upBtn.setFont(BigFontTR);
        upBtn.setBackground(new Color(150,150,150,255));
        upBtn.setForeground(new Color(255,255,255,255));
        
        downBtn = new JButton("↓");
        downBtn.setFont(BigFontTR);
        downBtn.setBackground(new Color(150,150,150,255));
        downBtn.setForeground(new Color(255,255,255,255));
        
        leftBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                left_pressed = true;
                new Thread() {
                    public void run() {
                        while (left_pressed) {
                        	int z = (200 + far_slider.getValue()) / 100;
                        	if (z < 200) {
                        		z = 10;
                        	}
                        	Display.this.change_x = Display.this.change_x -  100 / z;
                        	try {
                        		sleep(30);
                        	}
                        	catch (Exception exception) {
								exception.printStackTrace();
                        	}
                        }
                    }

                }.start();
            }

            public void mouseReleased(MouseEvent e) {
            	left_pressed = false;
            }

        });
        
        rightBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	right_pressed = true;
                new Thread() {
                    public void run() {
                        while (right_pressed) {
                        	int z = (200 + far_slider.getValue()) / 100;
                        	if (z < 200) {
                        		z = 10;
                        	}
                        	Display.this.change_x = Display.this.change_x +  100 / z;
                        	try {
                        		sleep(30);
                        	}
                        	catch (Exception exception) {
								exception.printStackTrace();
                        	}
                        }
                    }

                }.start();
            }

            public void mouseReleased(MouseEvent e) {
            	right_pressed = false;
            }
        });
        
        upBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	up_pressed = true;
                new Thread() {
                    public void run() {
                        while (up_pressed) {
                        	int z = (200 + far_slider.getValue()) / 100;
                        	if (z < 200) {
                        		z = 10;
                        	}
                        	Display.this.change_y = Display.this.change_y +  100 / z;
                        	try {
                        		sleep(30);
                        	}
                        	catch (Exception exception) {
								exception.printStackTrace();
                        	}
                        }
                    }

                }.start();
            }

            public void mouseReleased(MouseEvent e) {
            	up_pressed = false;
            }
        });
        
        downBtn.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	down_pressed = true;
                new Thread() {
                    public void run() {
                        while (down_pressed) {
                        	int z = (200 + far_slider.getValue()) / 100;
                        	if (z < 200) {
                        		z = 10;
                        	}
                        	Display.this.change_y = Display.this.change_y -  100 / z;
                        	try {
                        	sleep(30);
                        	}
                        	catch (Exception exception) {
								exception.printStackTrace();
                        	}
                        }
                    }

                }.start();
            }

            public void mouseReleased(MouseEvent e) {
            	down_pressed = false;
            }
        });

        leftBtn.setEnabled(false);
        rightBtn.setEnabled(false);
        upBtn.setEnabled(false);
        downBtn.setEnabled(false);
        this.setEnabled(false);
        
        leftBtn.setBounds(fieldWidth - 150, fieldHeight - 100, 50, 50);
        rightBtn.setBounds(fieldWidth - 50, fieldHeight - 100, 50, 50);
        upBtn.setBounds(fieldWidth - 100, fieldHeight - 150, 50, 50);
        downBtn.setBounds(fieldWidth - 100, fieldHeight - 50, 50, 50);

		Update(TPS, scene.files, scene.erythrocytes);

        m_frame.add(setTexture);
        m_frame.add(imageLabel);
        m_frame.add(textureLabel);

		imageLabel.setBackground(Color.decode("#F4F5F6"));
		textureLabel.setBackground(Color.decode("#F4F5F6"));
        
        m_frame.add(leftBtn);
        m_frame.add(rightBtn);
        m_frame.add(upBtn);
        m_frame.add(downBtn);
        m_frame.add(far_slider);
        m_frame.add(scrollPane);
        m_frame.add(addBtn);
        m_frame.add(detectDtn);
		m_frame.add(setForm);
		m_frame.add(formLabel);

		setTexture.setBackground(Color.decode("#F4F5F6"));
		m_frame.add(imageAmount);
		m_frame.add(textureAmount);
		m_frame.add(formAmount);

		imageAmount.setBackground(Color.decode("#F4F5F6"));
		textureAmount.setBackground(Color.decode("#F4F5F6"));
		formAmount.setBackground(Color.decode("#F4F5F6"));

		m_frame.add(imagesPane);
		m_frame.add(texturePane);
		m_frame.add(formPane);

		imagesPane.setBackground(Color.decode("#F4F5F6"));
		texturePane.setBackground(Color.decode("#F4F5F6"));
		formPane.setBackground(Color.decode("#F4F5F6"));

		m_frame.add(errorField);
		m_frame.add(updateButton);
		m_frame.add(loadingLabel);

		imagesPane.setBackground(Color.decode("#F4F5F6"));
		texturePane.setBackground(Color.decode("#F4F5F6"));
		formPane.setBackground(Color.decode("#F4F5F6"));
        
        m_frame.add(gemoglob_label);
        m_frame.add(gemoglob_slide);
        m_frame.add(oxygen_label);
        m_frame.add(oxygen_slide);
        m_frame.add(def);
        m_frame.add(form_slide);
		m_frame.add(setImage);
		m_frame.add(image);
		m_frame.add(imageHelper);
		m_frame.add(imageHelper1);
		m_frame.add(imageHelper2);
		m_frame.add(imageHelper3);

		m_frame.add(label_scale_factor);
		m_frame.add(label_min_neighbors);
		m_frame.add(label_min_size);
		m_frame.add(label_max_size);
		m_frame.add(scale_factor);
		m_frame.add(min_neighbors);
		m_frame.add(min_size);
		m_frame.add(max_size);

		m_frame.add(erysAmount);
		m_frame.add(toAll);
		m_frame.add(this);

		m_frame.pack();
		m_frame.setResizable(false);
		m_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		m_frame.setTitle("Blood cells");
		m_frame.setSize(width, height);
		m_frame.setVisible(true);

		createBufferStrategy(1);
		m_bufferStrategy = getBufferStrategy();
		m_graphics = m_bufferStrategy.getDrawGraphics();

		addFileChooserListeners(image, TPS.token, TPS.projectID, scene.scene.ID);

		setFocusable(true);
		requestFocus();
	}


private void addFileChooserListeners(imagePanel image, AuthToken token, int projectID, int sceneID)
{
	setImage.addActionListener(e -> chooseResourse(false,true));
	setTexture.addActionListener(e -> chooseResourse(false,false));
	setForm.addActionListener(actionEvent -> chooseResourse(true,false));
	
	image.addMouseListener(new MouseListener() {
    
		@Override
        public void mouseReleased(MouseEvent e) {
			
			 Display.this.image.xs = e.getX();
             Display.this.image.ys = e.getY();
             Display.this.image.repaint();
        }

		@Override
		public void mouseClicked(MouseEvent e) {
			 Display.this.image.x = e.getX();
             Display.this.image.y = e.getY();
             Display.this.image.xs = e.getX();
             Display.this.image.ys = e.getY();
             Display.this.addBtn.setEnabled(false);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			Display.this.image.x = e.getX();
            Display.this.image.y = e.getY();
            Display.this.image.xs = e.getX();
            Display.this.image.ys = e.getY();
            Display.this.image.repaint();
            Display.this.addBtn.setEnabled(false);
		}
	});
	
	image.addMouseMotionListener(new MouseMotionListener() {
         public void mouseDragged(MouseEvent e) {
        	 Display.this.image.xs = e.getX();
             Display.this.image.ys = e.getY();
             addBtn.setEnabled(true);
             Display.this.image.repaint();
         }
         
		@Override
		public void mouseMoved(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	});

	detectDtn.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {

			Runnable task = () -> {
                Mat mat = detectErys2(choosenImage.image);
                Image img = ConvertMat2Image(mat);
                System.out.println(String.format("click3"));
                image.setNewImage(img);
                image.repaint();
                System.out.println(String.format("done"));
			};
			Thread thread = new Thread(task);
			thread.start();
		}
	});

	 addBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				errorField.setVisible(false);
				LoaderStatus sl = new LoaderStatus(m_frame, loadingLabel,"Добавляем", display);
				sl.start();
				addBtn.setEnabled(false);
				Runnable task = () -> {
					ErythrocyteInfo information = new ErythrocyteInfo(image.get_width(), image.get_height(),
							image.get_pos_x(), image.get_pos_y());
					InitEryInfo(information);
					information.addMeta(true);
					information.gemoglob = (float)(gemoglob_slide.getValue()) / 100;
					information.oxygen = (float)(oxygen_slide.getValue()) / 100;
					information.form = (float)(form_slide.getValue()) / 100;

					Model model = null;
					try {
						model = new Model(choosenForm.file.getAbsolutePath());
					}
					catch (IOException exception) {
						exception.printStackTrace();
					}
					if (model == null) {
						errorField.setVisible(true);
						errorField.setText("Один из загружаемых ресурсов(снимок/текстура/форма) не доступен");
					} else {
						ErythrocyteForm er = new ErythrocyteForm(display, TPS, information, choosenTexture, choosenImage, model);
						actionErythrocyte(er, true, false);

						ErythrocyteResponse ewt = ApiHandler.createErythrocyte(TPS, information);
						ApiHandler.hasError(errorField, ewt, false);
						er.info.id = ewt.erythrocyte.id;
					}
					sl.interrupt();
					addBtn.setEnabled(true);

					image.x = 0;
					image.y = 0;
					image.xs = 0;
					image.ys = 0;
					image.repaint();
				};
				Thread thread = new Thread(task);
				thread.start();
			}
		});
}

	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(Image img)
	{
		if (img instanceof BufferedImage)
		{
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}

	public static Mat detectErys2(Image originalImage){
        System.out.println(String.format("click5"));
	    BufferedImage bi = toBufferedImage(originalImage);
        System.out.println(String.format("click6"));
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        System.out.println(String.format("click7"));
        mat.put(0, 0, data);
        System.out.println(String.format("click8"));

		CascadeClassifier detector = new CascadeClassifier("/home/artyom/labs/bauman/1/vkr/haarcascade5/cascade5.xml");

//		String base = "/home/artyom/labs/bauman/1/vkr/Best/";
//		Mat image = Imgcodecs.imread(base + "Picture 142.jpg");
		MatOfRect faceDetections = new MatOfRect();
        System.out.println(String.format("click9"));
        detector.detectMultiScale(mat, faceDetections, 10, 20, 0,new Size(30,30), new Size(100,100));

        System.out.println(String.format("click10"));

		System.out.println(String.format("Detected %s cars", faceDetections.toArray().length));

		for (Rect rect : faceDetections.toArray()) {
			Imgproc.rectangle(mat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(0, 255, 0));
		}

        /* String filename = "C:\\Users\\User\\Desktop\\Road and Cars\\Outputs\\";
         System.out.println(String.format("Writing %s", filename));*/
		return mat;
	}


    private static BufferedImage ConvertMat2Image(Mat kameraVerisi) {
        MatOfByte byteMatVerisi = new MatOfByte();
        //Ara belle?e verilen formatta görüntü kodlar
        Imgcodecs.imencode(".jpg", kameraVerisi, byteMatVerisi);
        //Mat nesnesinin toArray() metodu elemanlary byte dizisine çevirir
        byte[] byteArray = byteMatVerisi.toArray();
        BufferedImage goruntu = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            goruntu = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return goruntu;
    }


	/**
	 * Displays in the window.
	 */
	public void SwapBuffers()
	{
		//Display components should be the byte array used for displayImage's pixels.
		//Therefore, this call should effectively copy the frameBuffer into the
		//displayImage.
		m_frameBuffer.CopyToByteArray(m_displayComponents);
		m_graphics.drawImage(m_displayImage, 10, 40,
			fieldWidth - 10, fieldHeight - 40, null);
		m_bufferStrategy.show();
	}

	Display display = this;
	void Load(TokenProjectScene tps) {
		SceneWithObjectsResponse swo = ApiHandler.getScene(TPS);
		if (ApiHandler.hasError(errorField, swo, false)) {
			return;
		}
		Update(tps, swo.sceneWithObjects.files,  swo.sceneWithObjects.erythrocytes);
	}

	ArrayList<EryObjectForm> formsArray = new ArrayList<EryObjectForm>();
	ArrayList<EryObjectForm> imagesArray = new ArrayList<EryObjectForm>();
	ArrayList<EryObjectForm> texturesArray = new ArrayList<EryObjectForm>();
	int images = 0, textures = 0, forms = 0, erythocytes = 0;
	void Update(TokenProjectScene tps, ArrayList<EryObject> objects, ArrayList<ErythrocyteInfo> erys) {

		LoaderStatus sl = new LoaderStatus(m_frame, loadingLabel,"Загружаем ресурсы", display);
		sl.start();
		Runnable task = () -> {
			this.pause = true;
			allowEryUpdate.lock();
			Clear();
			//Disable();

			System.out.println("этап апдейта:"+objects.size());

			sl.setOrigin("Загружаем снимки, текстуры и формы");
			images = 0; textures = 0; forms = 0;
			try{
				CountDownLatch latch = new CountDownLatch(objects.size());
				for (EryObject object: objects) {
					Thread t = new Thread(new UpdateEryObject(display, object, latch));
					t.start();
				}
				latch.await();
			}catch(Exception err){
				err.printStackTrace();
			}

			sl.setOrigin("Загружаем модели");
			erythocytes = 0;
			try{
				CountDownLatch latch = new CountDownLatch(erys.size());
				for (ErythrocyteInfo ery: erys) {
					Thread t = new Thread(new UpdateErythrocyte(display, ery, latch));
					t.start();
				}
				latch.await();
			}catch(Exception err){
				err.printStackTrace();
			}
			this.pause = false;
			allowEryUpdate.unlock();
			sl.interrupt();

			//Enable();
		 };
		Thread thread = new Thread(task);
		thread.start();
	}

	void updateChoosenImage(EryObjectForm newForm) {
		if (choosenImage != null) {
			choosenImage.setUnchoosen();
		}
		choosenImage = newForm;
		newForm.setChoosen();
		//display.applyImage(newFile);
		addBtn.setEnabled(false);
		try {
			image.updateImage2(newForm.eryobject.path);
			image.repaint();
			imageLabel.setText("Выбрано:'" + newForm.file.getName()+"'");
		}
		catch (NullPointerException exception) {
			System.out.println("No file ");
		}
		catch (IOException exception) {
			System.out.println("io error");
		}
	}

	void updateChoosenTexture(EryObjectForm newForm) {
		if (choosenTexture != null) {
			choosenTexture.setUnchoosen();
		}
		choosenTexture = newForm;
		newForm.setChoosen();
		//display.applyTexture(newFile);
		addBtn.setEnabled(false);
		textureLabel.setText("Выбрано:'" + newForm.file.getName()+"'");
	}

	void updateChoosenForm(EryObjectForm newForm) {
		if (choosenForm != null) {
			choosenForm.setUnchoosen();
		}
		choosenForm = newForm;
		newForm.setChoosen();
		//display.applyForm(newForm);
		addBtn.setEnabled(false);
		formLabel.setText("Выбрано:'" + newForm.file.getName()+"'");
	}

	private void chooseResourse(Boolean isForm, Boolean isImage) {
		String fileChooserName = "Выбрите текстуру";
		if (isForm) {
			fileChooserName = "Выберите форму";
		} else if (isImage)  {
			fileChooserName = "Выберите снимок";
		}

		errorField.setVisible(false);

		fileChooser.setDialogTitle(fileChooserName);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		File newFile = null;
		int result = fileChooser.showOpenDialog(Display.this);
		if (result == JFileChooser.APPROVE_OPTION) {
			newFile = fileChooser.getSelectedFile();
		} else if (result == JFileChooser.CANCEL_OPTION){
			return;
		}

		if (newFile == null || !isFileValid(newFile, isForm, isImage)) {
			return;
		}

		Boolean isTexture = !(isImage || isForm);

		final File finalFile = newFile;
		LoaderStatus sl = new LoaderStatus(m_frame, loadingLabel,"Загружаем", display);
		sl.start();
		Runnable task = () -> {
			EryObjectResponse eryOBJ = ApiHandler.createEryObject(TPS, isForm, isTexture,isImage, finalFile);
			if (ApiHandler.hasError(errorField, eryOBJ, false)) {
				return;
			}
			EryObjectForm eryForm = new EryObjectForm(display, TPS, eryOBJ.eryObject);
			if (isForm) {
				updateChoosenForm(eryForm);
				actionForm(eryForm, true);
			} else if (isImage) {
				updateChoosenImage(eryForm);
				eryForm.file=finalFile;
				actionImage(eryForm, true);

			} else {
				updateChoosenTexture(eryForm);
				actionTexture(eryForm, true);
			}
			sl.interrupt();
		};
		Thread thread = new Thread(task);
		thread.start();
	}

	private Boolean isFileValid(File newFile, Boolean isForm, Boolean isImage) {
		if (!FileHandler.pathValid(newFile.getName())) {
			errorField.setVisible(true);
			errorField.setText("Разрешён всего 1 символ точки('.') в имени файла");
			return false;
		}

		String fileType = FileHandler.getType(newFile.getName());
		boolean condition = fileType.equals("obj");
		String errorText = "Формой модели могут быть только файлы формата .obj";
		if (!isForm) {
			condition = (fileType.equals("png")) || (fileType.equals("jpg"))|| (fileType.equals("jpeg"));
			if (isImage) {
				errorText = "Снимками могут являться только файлы форматов .png, .jpg, .jpeg";
			} else {
				errorText = "Текстурами могут являться только файлы форматов .png, .jpg, .jpeg";
			}
		}
		if (!condition) {
			errorField.setVisible(true);
			System.out.println("fileType:"+ fileType);
			errorField.setText(errorText);
			return false;
		}
		return true;
	}

	void actionKill1Rbt() {
		if (!toAll.isSelected()) {
			return;
		}
		for (ErythrocyteForm form : erys) {
			form.info.form_factor = 1;
		}
	}

	void actionKill2Rbt() {
		if (!toAll.isSelected()) {
			return;
		}
		for (ErythrocyteForm form : erys) {
			form.info.gemoglob_factor = -1;
		}
	}

	void actionKill3Rbt() {
		if (!toAll.isSelected()) {
			return;
		}
		for (ErythrocyteForm form : erys) {
			form.info.oxygen_factor = -1;
		}
	}

	void actionKill4Rbt() {
		if (!toAll.isSelected()) {
			return;
		}
		for (ErythrocyteForm form : erys) {
			form.info.form_factor = 1;
			form.info.gemoglob_factor = -1;
			form.info.oxygen_factor = -1;
		}
	}

	void actionSaveRbt() {
		if (!toAll.isSelected()) {
			return;
		}
		for (ErythrocyteForm form : erys) {
			form.info.form_factor = 2;
			form.info.gemoglob_factor = 2;
			form.info.oxygen_factor = 1;
		}
	}

	void actionNoRbt() {
		if (!toAll.isSelected()) {
			return;
		}
		for (ErythrocyteForm form : erys) {
			form.info.form_factor = 0;
			form.info.gemoglob_factor = 0;
			form.info.oxygen_factor = 0;
		}
	}

	void actionForm(EryObjectForm eryForm, Boolean increment) {
		if (increment) {
			forms++;
			formPanel.add(eryForm.root);
			formsArray.add(eryForm);
		} else {
			forms--;
			formPanel.remove(eryForm.root);
			formsArray.remove(eryForm);
		}

		formPanel.setPreferredSize(new Dimension(205, 360*forms));
		formPanel.repaint();
		formAmount.setText("Загружено форм:"+forms);
	}

	void actionTexture(EryObjectForm eryForm, Boolean increment) {
		if (increment) {
			textures++;
			texturePanel.add(eryForm.root);
			texturesArray.add(eryForm);
		} else {
			textures--;
			texturePanel.remove(eryForm.root);
			texturesArray.remove(eryForm);
		}

		texturePanel.setPreferredSize(new Dimension(205, 340*textures));
		texturePanel.repaint();
		textureAmount.setText("Загружено текстур:"+textures);
	}

	void actionImage(EryObjectForm eryForm, Boolean increment) {
		if (increment) {
			images++;
			imagesPanel.add(eryForm.root);
			imagesArray.add(eryForm);
		} else {
			images--;
			imagesPanel.remove(eryForm.root);
			imagesArray.remove(eryForm);
		}

		imagesPanel.setPreferredSize(new Dimension(205, 360*images));
		imagesPanel.repaint();
		imageAmount.setText("Загружено снимков:"+images);
	}

	void actionErythrocyte(ErythrocyteForm eryForm, Boolean increment, Boolean mutexLocked) {
		if (!mutexLocked) {
			this.pause = true;
			System.out.println("actionErythrocyte lock!:");
			allowEryUpdate.lock();
			System.out.println("actionErythrocyte locked!:");
		}

		if (increment) {
			erythocytes++;
			objs.add(eryForm.model);
			erys.add(eryForm);
			eryPanel.add(eryForm);
		} else {
			erythocytes--;
			objs.remove(eryForm.model);
			erys.remove(eryForm);
			eryPanel.remove(eryForm);

			errorField.setVisible(false);
			Result result = ApiHandler.deleteErythrocyte(TPS, eryForm.info);
			if (ApiHandler.hasError(errorField, result, false)) {
				return;
			}

		}
		scrollPane.revalidate();
		eryPanel.revalidate();
		eryPanel.repaint();

		erysAmount.setText("Эритроцитов:"+erythocytes);
		addBtn.setEnabled(false);

		if (erythocytes == 1) {
			leftBtn.setEnabled(true);
			rightBtn.setEnabled(true);
			upBtn.setEnabled(true);
			downBtn.setEnabled(true);
			Display.this.setEnabled(true);
			far_slider.setEnabled(true);
		}

		if (!mutexLocked) {
			this.pause = false;
			System.out.println("actionErythrocyte unlock!:");
			allowEryUpdate.unlock();
			System.out.println("actionErythrocyte unlocked!:");
		}

		eryPanel.setPreferredSize(new Dimension(850 * erys.size(), 290));
		eryPanel.revalidate();
	}

	EryObjectForm searchImage(int id) {
		if (imagesArray == null) {
			return null;
		}
		for (EryObjectForm form : imagesArray) {
			if (form.eryobject.id == id) {
				return form;
			}
		}
		return null;
	}

	EryObjectForm searchTexture(int id) {
		if (texturesArray == null) {
			return null;
		}
		for (EryObjectForm form : texturesArray) {
			if (form.eryobject.id == id) {
				return form;
			}
		}
		return null;
	}

	EryObjectForm searchForm(int id) {
		if (formsArray == null) {
			return null;
		}
		for (EryObjectForm form : formsArray) {
			if (form.eryobject.id == id) {
				return form;
			}
		}
		return null;
	}

	void Clear() {
		//DeleteAllEry();
//		this.pause = true;
//		allowEryUpdate.lock();

		eryPanel.removeAll();
		formPanel.removeAll();
		texturePanel.removeAll();
		imagesPanel.removeAll();
		formsArray.removeAll(formsArray);
		imagesArray.removeAll(imagesArray);
		texturesArray.removeAll(texturesArray);
		objs.removeAll(objs);
//		while (objs.size() > 0) {
//			objs.remove(0);
//		}

//		this.pause = false;
//		allowEryUpdate.unlock();
	}

	public void Disable() {
		updateButton.setEnabled(false);

		setImage.setEnabled(false);
		setTexture.setEnabled(false);
		setForm.setEnabled(false);

		imageLabel.setEnabled(false);
		textureLabel.setEnabled(false);
		formLabel.setEnabled(false);

		scrollPane.setEnabled(false);
		addBtn.setEnabled(false);

		imagesPane.setEnabled(false);
		texturePane.setEnabled(false);
		formPane.setEnabled(false);

		gemoglob_label.setEnabled(false);
		gemoglob_slide.setEnabled(false);
		oxygen_label.setEnabled(false);
		oxygen_slide.setEnabled(false);
		def.setEnabled(false);
		form_slide.setEnabled(false);

		imageLabel.setEnabled(false);
		imageLabel.setEnabled(false);
		imageLabel.setEnabled(false);

		this.setEnabled(false);
	}

	public void Enable() {
		updateButton.setEnabled(true);

		setImage.setEnabled(true);
		setTexture.setEnabled(true);
		setForm.setEnabled(true);

		imageLabel.setEnabled(true);
		textureLabel.setEnabled(true);
		formLabel.setEnabled(true);

		scrollPane.setEnabled(true);
		addBtn.setEnabled(true);

		imagesPane.setEnabled(true);
		texturePane.setEnabled(true);
		formPane.setEnabled(true);

		gemoglob_label.setEnabled(true);
		gemoglob_slide.setEnabled(true);
		oxygen_label.setEnabled(true);
		oxygen_slide.setEnabled(true);
		def.setEnabled(true);
		form_slide.setEnabled(true);

		imageLabel.setEnabled(true);
		imageLabel.setEnabled(true);
		imageLabel.setEnabled(true);

		this.setEnabled(true);
	}

	void InitEryInfo(ErythrocyteInfo information) {
		information.textureID = choosenTexture.eryobject.id;
		information.id = choosenTexture.eryobject.id;
		information.textureID = choosenTexture.eryobject.id;
		information.imageID = choosenImage.eryobject.id;
		information.formID = choosenForm.eryobject.id;
	}
}

// 954 -> 867 -> 904 -> 1010
