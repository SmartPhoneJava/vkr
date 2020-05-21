import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

interface EnableDisableI {
    void Enable();
    void Disable();
}

class LoaderStatus extends Thread {
    private JButton btn;
    private JLabel loadingLabel;
    private String _origin;
    private String after;
    private String dots = "";
    private int originWidth, maxWidth, height;
    private EnableDisableI edi;
    private JFrame frame;
    private ReentrantLock originM = new ReentrantLock();

    public String origin() {
        originM.lock();
        String orig = _origin;
        originM.unlock();
        return orig;
    }

    public void setOrigin(String newOrigin) {
        originM.lock();
        _origin = newOrigin;
        originM.unlock();
    }

    public LoaderStatus(JFrame frame, JLabel loadingLabel, String origin, EnableDisableI edi){
        this.btn = btn;
        this.loadingLabel = loadingLabel;
        this.after = after;
        this.originWidth = originWidth;
        this.maxWidth = maxWidth;
        this.height = height;
        this.edi = edi;
        this.frame = frame;
        setOrigin(origin);
    }
    public void run() {
        try{
            edi.Disable();
            if (btn != null)
                btn.setSize(maxWidth, height);
            if (loadingLabel != null)
                loadingLabel.setVisible(true);

            do {
                sleep(300);

                dots += ".";
                if (dots.length() > 5) {
                    dots = "";
                }
                if (btn != null)
                    btn.setText(origin() + dots);
                if (loadingLabel != null)
                    loadingLabel.setText(origin() + dots);
                if (frame != null)
                    frame.repaint();
            } while (true);
        }catch(Exception err){
            if (btn != null) {
                btn.setSize(originWidth, height);
                btn.setText(after);
            }
            if (loadingLabel != null)
                loadingLabel.setVisible(false);
            edi.Enable();
        }
    }
}

// Обновить снимки/текстуры/формы
class UpdateEryObject implements Runnable{
    CountDownLatch latch;
    Display display;
    EryObject object;
    public UpdateEryObject(Display display, EryObject object, CountDownLatch latch){
        this.latch = latch;
        this.display = display;
        this.object = object;
    }
    public void run() {
        try{
            if (FileHandler.getType(FileHandler.getName(object.path)).equals("obj") && !object.is_form) {
                System.out.println("panic():" + object.path);
                return;//continue;
            }
            EryObjectForm form = new EryObjectForm(display, display.TPS, object);

            if (object.is_texture) {
                display.actionTexture(form, true);
                if (display.choosenTexture == null || display.choosenTexture.equals(form)) {
                    display.updateChoosenTexture(form);
                }
            } else if (object.is_image) {
                display.actionImage(form, true);
                if (display.choosenImage == null || display.choosenImage.equals(form)) {
                    display.updateChoosenImage(form);
                }
            } else if (object.is_form) {
                display.actionForm(form, true);
                if (display.choosenForm == null || display.choosenForm.equals(form)) {
                    display.updateChoosenForm(form);
                }
            }
            latch.countDown();
        }catch(Exception err){
            err.printStackTrace();
        }
    }
}

class UpdateErythrocyte implements Runnable{
    CountDownLatch latch;
    Display display;
    ErythrocyteInfo ery;
    public UpdateErythrocyte(Display display, ErythrocyteInfo ery, CountDownLatch latch){
        this.latch = latch;
        this.display = display;
        this.ery = ery;
    }
    public void run() {
        try{
            ery.addMeta(false);
            System.out.println("очередной элемент:"+ ery.id);
            EryObjectForm formOBJ = display.searchForm(ery.formID);
            if (formOBJ == null) {
                System.out.println("cant find form with id:"+ery.formID);
                formOBJ = display.choosenForm;
            }

            EryObjectForm textureOBJ = display.searchTexture(ery.textureID);
            if (textureOBJ == null) {
                System.out.println("cant find texture with id:"+ery.textureID);
                textureOBJ = display.choosenTexture;
            }

            EryObjectForm imageOBJ = display.searchImage(ery.imageID);
            if (imageOBJ == null) {
                System.out.println("cant find image with id:"+ery.imageID);
                imageOBJ = display.choosenImage;
            }

            Model model = null;
            try {
                model = new Model(formOBJ.file.getAbsolutePath());
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
            //
            ErythrocyteForm er = new ErythrocyteForm(display, display.TPS, ery, textureOBJ, imageOBJ, model);
            System.out.println("добавим!:"+ ery.id);
            System.out.println("Углы!:"+ ery.angle_x + " " + ery.angle_y + " " + ery.angle_z);
            display.actionErythrocyte(er, true, true);

            latch.countDown();
        }catch(Exception err){
            err.printStackTrace();
        }
    }
}