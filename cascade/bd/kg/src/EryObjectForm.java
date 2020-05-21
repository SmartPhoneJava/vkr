import org.apache.commons.lang.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;

public class EryObjectForm extends JPanel{
    public JPanel root;
    private JLabel addLabel;
    private JEditorPane descriptionLabel;
    private JLabel nameLabel;
    private JButton chooseButton;
    private JLabel imageLabel;
    private JButton deleteButton;
    private JEditorPane errorField;
    File file = null;
    Image image = null;
    EryObject eryobject;

    public Boolean equals(EryObjectForm another) {
        return this.eryobject.id == another.eryobject.id;
    }

    EryObjectForm(Display display, TokenProjectScene tps, EryObject eryobj) {
        String name = FileHandler.getName(eryobj.path);
        System.out.println("look at my path"+ eryobj.path);
        file = new File(name);
        eryobject = eryobj;
        errorField.setVisible(false);

        if (!eryobj.is_form) {
            image = FileHandler.loadImage(eryobj.path, name);
            Icon icon = imagePanel.getSceledIcon(image, 100, 100);
            imageLabel.setIcon(icon);
        } else {
            try {
                FileHandler.downloadUsingStream(eryobj.path, name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
        addLabel.setText(dateFormat.format( eryobj.add ));
        nameLabel.setText(eryobj.name);
        descriptionLabel.setText(eryobject.about);

        setUnchoosen();

        EryObjectForm form = this;
        chooseButton.addActionListener(actionEvent -> {
            if (eryobj.is_image) {
                display.updateChoosenImage(form);
            } else if (eryobj.is_texture){
                display.updateChoosenTexture(form);
            } else if (eryobj.is_form){
                display.updateChoosenForm(form);
            }
        });

        deleteButton.addActionListener(actionEvent -> {
            errorField.setVisible(false);
            Result result = ApiHandler.deleteEryObject(tps, eryobj.id);
            if (ApiHandler.hasError(nameLabel, result, false)) {
                return;
            }
            if (eryobj.is_image) {
                display.actionImage(this, false);
            } else  if (eryobj.is_texture) {
                display.actionTexture(this, false);
            } else  if (eryobj.is_form) {
                display.actionForm(this, false);
            }
            display.Load(tps);
        });
    }

    EryObjectForm setChoosen() {
        chooseButton.setEnabled(false);
        chooseButton.setText("выбрано");
        deleteButton.setEnabled(false);
        return this;
    }

    void setUnchoosen() {
        chooseButton.setEnabled(true);
        chooseButton.setText("выбрать");
        deleteButton.setEnabled(true);
    }
}
