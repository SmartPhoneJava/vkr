
import com.google.gson.annotations.SerializedName;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class ErythrocyteInfo {
	@SerializedName("id")
	public int id;
	@SerializedName("user_id")
	public int userID;
	@SerializedName("texture_id")
	public int textureID;
	@SerializedName("form_id")
	public int formID;
	@SerializedName("image_id")
	public int imageID;
	@SerializedName("scene_id")
	public int sceneID;
	@SerializedName("disease_id")
	public int diseaseID;

	@SerializedName("size_x")
	public float size_x;
	@SerializedName("size_y")
	public float size_y;
	@SerializedName("size_z")
	public float size_z;

	@SerializedName("angle_x")
	public float angle_x;
	@SerializedName("angle_y")
	public float angle_y;
	@SerializedName("angle_z")
	public float angle_z;

	@SerializedName("scale_x")
	public float scale_x;
	@SerializedName("scale_y")
	public float scale_y;
	@SerializedName("scale_z")
	public float scale_z;


	@SerializedName("position_x")
	public float position_x;
	@SerializedName("position_y")
	public float position_y;
	@SerializedName("position_z")
	public float position_z;

	@SerializedName("form")
	public float form;
	public float max_form, min_form;

	@SerializedName("oxygen")
	public float oxygen;
	public float max_oxygen, min_oxygen;

	@SerializedName("gemoglob")
	public float gemoglob;
	public float max_gemog, min_gemog;

	@SerializedName("add")
	public Date add;
	
	public int form_factor;
	public int gemoglob_factor;
	public int oxygen_factor;
	
	public Transform transform;

	private float correct_anything(int factor, float min, float max, float value, float add) {
		if (factor > 0) {
			if (value < max) {
				return value + add;
			}
		}
		if (factor < 0) {
			if (value > min) {
				return value - add;
			}
		}
		return value;
	}
	
	private boolean comparef(float a, float b) {
		float eps = 0.0001f;
		return (Math.abs(a - b) < eps);
	}
	
	public boolean correct_oxygen() {
		float num = correct_anything(oxygen_factor, min_oxygen, 0.98f,
				oxygen, 0.01f);
		if (comparef(num, oxygen) == false) {
			oxygen = num;
			return true;
		}
		return false;
	}
	
	public boolean correct_form() {
		float num;
		if (form_factor == 2) {
			if (form > 0) {
				num = correct_anything(-1, 0, max_form,
						form, 0.01f);
			} else {
				num = correct_anything(1, min_form, 0,
						form, 0.01f);
			}
		} else {
			num = correct_anything(form_factor, min_form, max_form,
				form, 0.01f);
		}
		if (comparef(num, form) == false) {
			form = num;
			return true;
		}
		return false;
	}
	
	public boolean correct_gemoglob() {
		float num;
		if (gemoglob_factor == 2) {
			if (gemoglob > 1) {
				num = correct_anything(-1, 1, max_gemog,
						gemoglob, 0.01f);
			} else {
				num = correct_anything(1, min_gemog, 1,
						gemoglob, 0.01f);
			}
		} else {
			num = correct_anything(gemoglob_factor, min_gemog, max_gemog,
					gemoglob, 0.01f);
		}
		if (comparef(num, gemoglob) == false) {
			gemoglob = num;
			return true;
		}
		return false;
	}
	
	public ErythrocyteInfo(ErythrocyteInfo info) {
		size_x = info.size_x;
		size_y = info.size_y;
		size_z = info.size_y;
		
		angle_x = info.angle_x;
		angle_y = info.angle_y;
		angle_z = info.angle_z;
		
		position_x = info.position_x;
		position_y = info.position_y;
		position_z = info.position_z;
		
		form = info.form;
		max_form = info.max_form;
		min_form = info.min_form;
		
		oxygen = info.oxygen;
		max_oxygen = info.max_oxygen;
		min_oxygen = info.min_oxygen;
		
		gemoglob = info.gemoglob;
		max_gemog = info.max_gemog;
		min_gemog = info.min_gemog;
		
		form_factor = info.form_factor;
		gemoglob_factor = info.gemoglob_factor;
		oxygen_factor = info.oxygen_factor;

		create();
	}

    ErythrocyteInfo() {
        size_x = 0;
        size_y = 0;
        size_z = 0;

        angle_x = 0;
        angle_y = 0;
        angle_z = 0;

        position_x = 0;
        position_y = 0;
        position_z = 0;
        create();
    }
	
	public ErythrocyteInfo(float sx, float sy,
			float px, float py) {
		size_x = sx;
		size_y = sy;
		size_z = 0;
		
		angle_x = 0;
		angle_y = 0;
		angle_z = 0;
		
		position_x = px;
		position_y = py;
		position_z = 0;
		
		create();
	}
	
	public void create() {
		
		transform = new Transform(new Vector(position_x, position_y, 0.0f));
		float ideal = 100;
		float k = (size_x) / ideal;
		if (k < 0.1f) {
			k = 0.1f;
		}
		transform.scale_ = new Vector(k, k, k, 1.0f);
		transform = transform.Rotate(new Quaternion(new Vector(1,0,0),
        		90 * (float)(Math.PI / 180)));
		//transform.SetPos(new Vector(position_x, position_y, 0.0f));
		//System.out.println("dimension1:" + position_x + ", " + position_y);
		scale_x = k;
		scale_y = k;
		scale_z = k;
	}

	public void addMeta(Boolean NotInit) {
		this.min_gemog = 0.5f;
		this.max_gemog = 1.2f;
		if (NotInit)
			this.gemoglob = 1;

		this.min_oxygen = 0.6f;
		this.max_oxygen = 1;
		if (NotInit)
			this.oxygen = 0.98f;

		this.min_form = -0.5f;
		this.max_form = 0.85f;
		if (NotInit)
			this.form = 0;

		transform = new Transform(new Vector(position_x, position_y, 0.0f));
		transform.scale_ = new Vector(scale_x, scale_y, scale_z, 1.0f);
		transform = transform.Rotate(new Quaternion(new Vector(1,0,0),
				90 * (float)(Math.PI / 180)));
	}
}

class ErythrocyteForm extends JPanel{
	public ErythrocyteInfo info;
	public Bitmap texture;
	//private JTextField lsize_x, lsize_y, lsize_z;
	public JLabel pos, angle, scale;
	public imagePanel image_size, image_texture;
	public String name;
	
	private JSlider rotate_x, rotate_y, rotate_z;
	public JSlider scale_slide;
	public JSlider form_slide, gemoglob_slide, oxygen_slide;
	public JLabel def, gemoglob_label, oxygen_label;
	public JLabel errorField;
	private JLabel actionLabel;
	
	private JButton recoverBtn;
	
	private JRadioButton actionNoRbt, actionSaveRbt, actionKill1Rbt;
	private JRadioButton actionKill2Rbt, actionKill3Rbt, actionKill4Rbt;
	
	// 0 - ������, 1 - ��������, 2 - ���������
	int status;
	
	JLabel label_ax, label_ay, label_az;
	JLabel label_x, label_y, label_z;
	
	private void updateAnything(JSlider slide, float value) {
		slide.setValue((int)(value * 100));
	}
	
	public void updateGemoglob() {
		updateAnything(gemoglob_slide, info.gemoglob);
	}
	
	public void updateOxygen() {
		updateAnything(oxygen_slide, info.oxygen);
	}
	
	public void updateForm() {
		updateAnything(form_slide, info.form);
	}

	Model model = null;
	public ErythrocyteForm(Display display, TokenProjectScene TPS, ErythrocyteInfo ei,
						   EryObjectForm textureForm, EryObjectForm imageForm, Model newModel) {
		model = newModel;
		info = ei;
		System.out.println("id is:"+ ei.id);

		info.transform.scale_ = new Vector(info.scale_x, info.scale_y, info.scale_z, 1.0f);
		info.transform = info.transform.Rotate(new Quaternion(new Vector(0,0,1),
				(info.angle_z) * (float)(Math.PI / 180)));
		info.transform = info.transform.Rotate(new Quaternion(new Vector(0,1,0),
				(info.angle_y) * (float)(Math.PI / 180)));
		info.transform = info.transform.Rotate(new Quaternion(new Vector(1,0,0),
				(info.angle_x) * (float)(Math.PI / 180)));

		
		status = 0;
		
		pos = new JLabel("Положение");
		
		angle = new JLabel("Угол");
		
		scale = new JLabel("Масштаб:" + info.scale_x);
		
		recoverBtn = new JButton("<html>"
				+ "Восстановить"
				+ "<p>"
				+ "настройки"
				+ "<p>"
				+ "по умолчанию"
				+ "</html>");
		System.out.println("image:"+imageForm.file.getAbsolutePath());
		System.out.println("texture:"+textureForm.file.getAbsolutePath());
		image_size = new imagePanel(200, 200);
		image_texture = new imagePanel(60, 60);
		try {
			image_size.newImage(imageForm.image);
			image_texture.newImage(textureForm.image);

			texture = new Bitmap(textureForm.file.getAbsolutePath());
			}
			catch (IOException exception) {
				exception.printStackTrace();
			}
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel jpan = new JPanel();
		jpan.setLayout(null);
		
		
		//image.setBackground(Color.RED);
		//jpan.setBackground(Color.BLUE);
		
		label_x = new JLabel("x:"+info.position_x);
		label_y = new JLabel("y:"+info.position_y);
		label_z = new JLabel("z:"+info.position_z);
		
		label_ax = new JLabel("по оси x:"+info.angle_x);
		label_ay = new JLabel("по оси y:"+info.angle_y);
		label_az = new JLabel("по оси z:"+info.angle_z);
		
		rotate_x = new JSlider(-180, 180, (int)info.angle_x);
		rotate_y = new JSlider(-180, 180, (int)info.angle_y);
		rotate_z = new JSlider(-180, 180, (int)info.angle_z);
		
		rotate_x.addChangeListener(event -> {
			info.transform = info.transform.Rotate(new Quaternion(new Vector(1,0,0),
					(rotate_x.getValue() - info.angle_x) * (float)(Math.PI / 180)));
			info.angle_x = rotate_x.getValue();
			label_ax.setText("по оси x:"+info.angle_x);
			recoverBtn.setEnabled(true);
			ErythrocyteResponse ersInfo = ApiHandler.updateErythrocyte(TPS, ei );
			if (ApiHandler.hasError(errorField, ersInfo, false)) {
				return;
			}
		  });
		
		rotate_y.addChangeListener(event -> {
			info.transform = info.transform.Rotate(new Quaternion(new Vector(0,1,0),
					(rotate_y.getValue() - info.angle_y) * (float)(Math.PI / 180)));
			info.angle_y = rotate_y.getValue();
			label_ay.setText("по оси y:"+info.angle_y);
			recoverBtn.setEnabled(true);
			ErythrocyteResponse ersInfo = ApiHandler.updateErythrocyte(TPS, ei );
			if (ApiHandler.hasError(errorField, ersInfo, false)) {
				return;
			}
		  });
		
		rotate_z.addChangeListener(event -> {
			info.transform = info.transform.Rotate(new Quaternion(new Vector(0,0,1),
					(rotate_z.getValue() - info.angle_z) * (float)(Math.PI / 180)));
			info.angle_z = rotate_z.getValue();
			label_az.setText("по оси z:"+info.angle_z);
			recoverBtn.setEnabled(true);
			ErythrocyteResponse ersInfo = ApiHandler.updateErythrocyte(TPS, ei );
			if (ApiHandler.hasError(errorField, ersInfo, false)) {
				return;
			}
		  });

		System.out.println("info.scale_x:"+ info.scale_x );
		scale_slide = new JSlider(1, 100, (int)(info.scale_x * 10));

		scale_slide.addChangeListener(event -> {
			float k = (float)(scale_slide.getValue()) / 10;
			info.transform.scale_ = new Vector(k, k, k, 1.0f);
			info.scale_x = k;
			info.scale_y = k;
			info.scale_z = k;
			scale.setText("масштаб" + k);
			System.out.println("info.transform.scale_:"+ info.transform.scale_+" "+info.scale_x);
			System.out.println("info.transform.size:"+ info.size_x+" "+info.size_y+" "+info.size_z);
			System.out.println("scaling:"+ info.scale_x + "," + info.scale_y + "," + info.scale_z);
			recoverBtn.setEnabled(true);
			ErythrocyteResponse ersInfo = ApiHandler.updateErythrocyte(TPS, info );
			if (ApiHandler.hasError(errorField, ersInfo, false)) {
				return;
			}
		  });
		
		scale.setText("масштаб" + (float)(scale_slide.getValue()) / 10);
		
		int int_min_gemog = (int)(100*info.min_gemog);
		int int_max_gemog = (int)(100*info.max_gemog);
		int int_gemoglob = (int)(100*info.gemoglob);
		
		gemoglob_slide = new JSlider(int_min_gemog,
				int_max_gemog, int_gemoglob);
		
		oxygen_slide = new JSlider((int)(100*info.min_oxygen),
				(int)(100*info.max_oxygen), (int)(100*info.oxygen));
		
		gemoglob_label = new JLabel("гемоглобин:" + info.gemoglob);
		oxygen_label = new JLabel("кислород:" + info.oxygen);
		
		oxygen_label.setBounds(425, 210, 200, 20);
		gemoglob_label.setBounds(220, 210, 200, 20);
		
		gemoglob_slide.addChangeListener(event -> {
			info.gemoglob = (float)(gemoglob_slide.getValue()) / 100;
			gemoglob_label.setText("гемоглобин:" + info.gemoglob);
			recoverBtn.setEnabled(true);
			ErythrocyteResponse ersInfo = ApiHandler.updateErythrocyte(TPS, info );
			if (ApiHandler.hasError(errorField, ersInfo, false)) {
				return;
			}
		  });
		
		oxygen_slide.addChangeListener(new ChangeListener() {
			@Override  
			public void stateChanged(ChangeEvent event) {
				info.oxygen = (float)(oxygen_slide.getValue()) / 100;
				oxygen_label.setText("кислород:" + info.oxygen);
		        recoverBtn.setEnabled(true);
				ErythrocyteResponse ersInfo = ApiHandler.updateErythrocyte(TPS, info );
				if (ApiHandler.hasError(errorField, ersInfo, false)) {
					return;
				}
		      }
		});
		
		def = new JLabel("деформация:" +  info.form / 2);
		errorField = new JLabel();
		errorField.setVisible(false);
		
		form_slide = new JSlider((int)(100*info.min_form),
				(int)(100*info.max_form), (int)(100*info.form));
		form_slide.addChangeListener(new ChangeListener() {
			@Override  
			public void stateChanged(ChangeEvent event) {
		        info.form = (float)(form_slide.getValue()) / 100;
		        def.setText("деформация:" + info.form / 2);
		        recoverBtn.setEnabled(true);
				ErythrocyteResponse ersInfo = ApiHandler.updateErythrocyte(TPS, info );
				if (ApiHandler.hasError(errorField, ersInfo, false)) {
					return;
				}
		      }
		});
		actionLabel = new JLabel("Действие:");
		actionNoRbt = new JRadioButton("Отсутсвует");
		actionKill1Rbt = new JRadioButton("Болезнь 1");
		actionKill2Rbt = new JRadioButton("Болезнь 2");
		actionKill3Rbt = new JRadioButton("Болезнь 3");
		actionKill4Rbt = new JRadioButton("Болезнь 4");
		actionSaveRbt = new JRadioButton("Лечение");
		
		gemoglob_slide.setBounds(205, 230, 200, 30);
		oxygen_slide.setBounds(405, 230, 200, 30);
		def.setBounds(5, 205, 250, 20);
		errorField.setBounds(755, 225, 250, 20);
		form_slide.setBounds(0, 230, 200, 30);
		
		actionNoRbt.setSelected(true);
		
		recoverBtn.setBounds(615, 5, 230, 75);
		
		actionLabel.setBounds(635, 80, 250, 20);
		actionNoRbt.setBounds(635, 100, 250, 20);
		actionKill1Rbt.setBounds(635, 120, 250, 20);
		actionKill2Rbt.setBounds(635, 140, 250, 20);
		actionKill3Rbt.setBounds(635, 160, 250, 20);
		actionKill4Rbt.setBounds(635, 180, 250, 20);
		actionSaveRbt.setBounds(635, 200, 250, 20);
		
		actionNoRbt.addActionListener(e -> {
			ErythrocyteForm.this.info.form_factor = 0;
			ErythrocyteForm.this.info.gemoglob_factor = 0;
			ErythrocyteForm.this.info.oxygen_factor = 0;
			display.actionNoRbt();
		});
		
		actionKill1Rbt.addActionListener(e -> {
			ErythrocyteForm.this.info.form_factor = 1;
			display.actionKill1Rbt();
		});
		actionKill2Rbt.addActionListener(e -> {
			ErythrocyteForm.this.info.gemoglob_factor = -1;
			display.actionKill2Rbt();
		});
		actionKill3Rbt.addActionListener(e -> {
			ErythrocyteForm.this.info.oxygen_factor = -1;
			display.actionKill3Rbt();
		});
		
		actionKill4Rbt.addActionListener(e -> {
			ErythrocyteForm.this.info.form_factor = 1;
			ErythrocyteForm.this.info.gemoglob_factor = -1;
			ErythrocyteForm.this.info.oxygen_factor = -1;
			display.actionKill4Rbt();
		});
		
		actionSaveRbt.addActionListener(e -> {
			ErythrocyteForm.this.info.form_factor = 2;
			ErythrocyteForm.this.info.gemoglob_factor = 2;
			ErythrocyteForm.this.info.oxygen_factor = 1;
			display.actionSaveRbt();
		});
		
		 ButtonGroup  group = new ButtonGroup();
		 group.add(actionNoRbt);
	     group.add(actionSaveRbt);
	     group.add(actionKill1Rbt);
	     group.add(actionKill2Rbt);
	     group.add(actionKill3Rbt);
	     group.add(actionKill4Rbt);
		
		image_size.setBounds(0, 0, 200, 200);
		pos.setBounds(255, 0, 150, 15);
		
		label_x.setBounds(205, 16, 50, 15);
		label_y.setBounds(205, 32, 50, 15);
		label_z.setBounds(205, 48, 50, 15);
		
		angle.setBounds(255, 70, 200, 15);
		
		label_ax.setBounds(205, 85, 110, 29);
		label_ay.setBounds(205, 115, 110, 29);
		label_az.setBounds(205, 145, 110, 29);
		
		rotate_x.setBounds(320, 90, 300, 29);
		rotate_y.setBounds(320, 120, 300, 29);
		rotate_z.setBounds(320, 150, 300, 29);
		
		scale.setBounds(205, 175, 90, 20);
		scale_slide.setBounds(300, 180, 280, 29);

		//scale_slide.setMajorTickSpacing(1);
		
		Dictionary <Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
        labels.put(1, new JLabel("<html><font color=red size=4>0.1"));
        labels.put(10, new JLabel("<html><font color=gray size=4>1"));
        labels.put(20, new JLabel("<html><font color=gray size=4>2"));
        labels.put(40, new JLabel("<html><font color=gray size=4>4"));
        labels.put(60, new JLabel("<html><font color=gray size=4>6"));
        labels.put(80, new JLabel("<html><font color=gray size=4>8"));
        labels.put(100, new JLabel("<html><font color=red size=4>10"));
		
		scale_slide.setLabelTable(labels);
		
		scale_slide.setPaintLabels(true);
		
		Dictionary <Integer, JLabel> form_labels = new Hashtable<Integer, JLabel>();
		form_labels.put(85, new JLabel("<html><font color=red size=4>0.425"));
        form_labels.put(40, new JLabel("<html><font color=gray size=4>0.2"));
        form_labels.put(0, new JLabel("<html><font color=gray size=4>0"));
        form_labels.put(-20, new JLabel("<html><font color=gray size=4>-0.1"));
        form_labels.put(-50, new JLabel("<html><font color=red size=4>-0.25"));
		
        form_slide.setLabelTable(form_labels);
		
        form_slide.setPaintLabels(true);
        
        Dictionary <Integer, JLabel> gemob_labels = new Hashtable<Integer, JLabel>();
        // ������������ ������ 110
        // ������������� ������ 80
        gemob_labels.put(110, new JLabel("<html><font color=red size=4>1.1"));
        gemob_labels.put(100, new JLabel("<html><font color=gray size=4>1"));
        gemob_labels.put(80, new JLabel("<html><font color=red size=4>0.8"));
		
        gemoglob_slide.setLabelTable(gemob_labels);
        gemoglob_slide.setPaintLabels(true);
        
        Dictionary <Integer, JLabel> oxygen_labels = new Hashtable<Integer, JLabel>();
        // �������� ��������� 98
        oxygen_labels.put(98, new JLabel("<html><font color=green size=4>0.98"));
        // �������� 92
        //oxygen_labels.put(new Integer(92), new JLabel("<html><font color=blue size=4>0.92"));
        // ������������ ����������� 88
        oxygen_labels.put(88, new JLabel("<html><font color=red size=4>0.88"));
        /*
        oxygen_labels.put(new Integer(44), new JLabel("<html><font color=gray size=4>����� ��������������"));
        oxygen_labels.put(new Integer(34), new JLabel("<html><font color=gray size=4>����� ��������"));
        oxygen_labels.put(new Integer(24), new JLabel("<html><font color=gray size=4>������� ��������"));
		*/
        oxygen_slide.setLabelTable(oxygen_labels);
        oxygen_slide.setPaintLabels(true);
        		
		JButton deleteBtn = new JButton("Удалить");
		JButton deleteAllBtn = new JButton("Удалить все");

		recoverBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				scale_slide.setValue(10);
				rotate_x.setValue(0);
				rotate_y.setValue(0);
				rotate_z.setValue(0);
				info.create();
				recoverBtn.setEnabled(false);
				info.form = 0;
			    def.setText("деформация:" + info.form);
			    form_slide.setValue(0);
			    info.oxygen = 0.98f;
			    oxygen_label.setText("кислород:" + info.oxygen);
			    oxygen_slide.setValue(98);
			    info.gemoglob = 1;
			    gemoglob_label.setText("гемоглобин:" +  info.gemoglob);
			    gemoglob_slide.setValue(100);
			}
		});
		
		recoverBtn.setEnabled(false);
		
		JLabel texture_label = new JLabel("Текстура");
		
		texture_label.setBounds(405, 0, 80, 15);
		image_texture.setBounds(400, 20, 60, 60);
		
		deleteBtn.setBounds(480, 20, 120, 27);
		deleteAllBtn.setBounds(480, 50, 120, 27);
		
		recoverBtn.setBounds(615, 5, 100, 75);

		ErythrocyteForm form = this;
		deleteBtn.addActionListener(e -> display.actionErythrocyte(form, false, false));
		
		deleteAllBtn.addActionListener(e -> display.DeleteAllEry());
		
		rotate_x.setPaintLabels(true);
		rotate_x.setMajorTickSpacing(45);
		rotate_y.setPaintLabels(true);
		rotate_y.setMajorTickSpacing(45);
		rotate_z.setPaintLabels(true);
		rotate_z.setMajorTickSpacing(45);
		
		jpan.add(gemoglob_slide);
		jpan.add(oxygen_slide);
		jpan.add(gemoglob_label);
		jpan.add(oxygen_label);
		jpan.add(actionLabel);
		jpan.add(actionNoRbt);
		jpan.add(actionKill1Rbt);
		jpan.add(actionKill2Rbt);
		jpan.add(actionKill3Rbt);
		jpan.add(actionKill4Rbt);
		jpan.add(actionSaveRbt);
		jpan.add(recoverBtn);
		jpan.add(texture_label);
		jpan.add(image_size);
		jpan.add(image_texture);
		jpan.add(pos);
		jpan.add(label_x);
		jpan.add(label_y);
		jpan.add(label_z);
		jpan.add(label_ax);
		jpan.add(label_ay);
		jpan.add(label_az);
		jpan.add(angle);
		jpan.add(rotate_x);
		jpan.add(rotate_y);
		jpan.add(rotate_z);
		jpan.add(scale);
		jpan.add(scale_slide);
		jpan.add(def);
		jpan.add(errorField);
		jpan.add(form_slide);
		jpan.add(deleteBtn);
		//jpan.add(deleteAllBtn);
		this.add(jpan);
	}
}

class imagePanel extends JPanel{
	public Image image;
	String name;
	public int x, y;
	public int xs, ys;
	public int width, height;
	public imagePanel(int wid, int heig) {
		x = y = xs = ys = 0;
		width = wid;
		height = heig;
		this.setSize(wid, heig);
		
		try {
			image = ImageIO.read(new File("res/man.png"));
			image= image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			}
			catch (IOException exception) {
				System.out.println("error no image");
				exception.printStackTrace();
			}
	}
	
	public int get_width() {
		return Math.abs(xs - x);
	}
	
	public int get_height() {
		return Math.abs(ys - y);
	}
	
	public int get_x() {
		if (x < xs) {
			return x;
		} else {
			return xs;
		}
	}
	
	public int get_y() {
		if (y < ys) {
			return y;
		} else {
			return ys;
		}
	}
	
	public int get_pos_x() {
		if (x < xs) {
			return -width/2 + width + xs - (xs - x) / 2;
		} else {
			return -width/2 + width + x - (x - xs) / 2;
		}
	}
	
	public int get_pos_y() {
		if (y < ys) {
			return height - ys + (ys - y) / 2;
		} else {
			return height - y + (y - ys) / 2;
		}
	}

	public void setImage(String path) {
		try {
			image = ImageIO.read(new File(path));
			image= image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		}
		catch (IOException exception) {
			System.out.println("error no image");
		}
	}

	public void updateImage2(String path) throws IOException {
//		String name = FileHandler.getName(path);
//
//		File f = new File(name);

		URL url = new URL(path);
		image = ImageIO.read(url);



		BufferedImage bi = toBufferedImage(image);
		float MAX_WIDTH = 340;
		float MAX_HEIGHT =  340;
		float w = bi.getWidth();
		float h =  bi.getHeight();



		if (w > h) {
			if (w > MAX_WIDTH) {
				h *= MAX_WIDTH / w;
				w = MAX_WIDTH;
			}
		} else {
			if (h > MAX_HEIGHT) {
				w *= MAX_HEIGHT / h;
				h = MAX_HEIGHT;
			}
		}
		System.out.println("sooo is it "+w+" "+h);

		image= image.getScaledInstance(Math.round(w), Math.round(h), Image.SCALE_SMOOTH);

//		name = f.getName();
//		System.out.println("2image path:" + path);
//		System.out.println("2image name:" + name);
//		if (f != null) {
//			try {
//				image=ImageIO.read(f);
//				image= image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
//			}
//			catch (IOException exception) {
//				System.out.println("2error no image:" + name + exception);
//			}
//		}
	}

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
	
	public void updateImage(File f) throws IOException {
		name = f.getName();
		System.out.println("image name:" + name);
		if (f != null) {
		try {
		image=ImageIO.read(f);
		image= image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		}
		catch (IOException exception) {
			System.out.println("error no image:" + name);
		}
		}
	}

	public void setNewImage(Image i)  {
		image= i.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	}

	public void newImage(Image updadetedImage) {
		image= updadetedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.drawImage(image, 0, 0, this);
		//g.drawOval(x, y, x, y);
		//g.setColor(Color.BLACK);
        //g.fillOval(x, y, 2, 2);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.blue);
        g2d.setStroke(new BasicStroke(2));
        Rectangle2D rect;
        if (xs > x) {
        	if (ys > y) {
        		rect = new Rectangle2D.Double(x, y,
        		xs - x, ys - y);
        	} else {
        		rect = new Rectangle2D.Double(x, ys,
                		xs - x, y - ys);
        	}
        } else {
        	if (ys > y) {
        		rect = new Rectangle2D.Double(xs, y,
        		x - xs, ys - y);
        	} else {
        		rect = new Rectangle2D.Double(xs, ys,
                		x - xs, y - ys);
        	}
        }
        g2d.draw(rect);
	}

	static Icon getIcon(String path, int width, int heigt) {
		imagePanel image = new imagePanel(width, heigt);
		image.setImage(path);
		return new ImageIcon(image.image);
	}


	static Icon getSceledIcon(Image newImg, int width, int height) {
		return new ImageIcon(newImg.getScaledInstance(width, height, Image.SCALE_SMOOTH));
	}
}