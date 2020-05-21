import javax.swing.*;
import java.text.SimpleDateFormat;

public class SceneForm extends JPanel implements EnableDisableI{
    private JLabel addLabel;
    private JLabel erysLabel;
    private JLabel formLabel;
    private JEditorPane descriptionLabel;
    private JLabel nameLabel;
    private JButton open;
    private JButton delete;
    public JPanel root;
    private JLabel errorField;
    private JLabel editLabel;
    private JLabel imagesLabel;
    private JLabel textureLabel;
    private JLabel loaderLabel;
    TokenProjectScene TPS;
    ProjectMember projectMember;
    Project project;

    SceneForm(ProjectPage page, AuthToken token, Icon openIcon, Icon deleteIcon,
              SceneWithObjects scene, ProjectMember projectMember, Project project, int projectID) {
        this.projectMember = projectMember;
        this.project = project;
        TPS = new TokenProjectScene(token, projectID, scene.scene.ID);
        errorField.setVisible(false);
        loaderLabel.setVisible(false);

        open.setIcon(openIcon);
        delete.setIcon(deleteIcon);

        SceneForm sceneForm = this;
        open.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(null, loaderLabel,"Открываем", sceneForm);
            sl.start();
            Runnable task = () -> {
                SceneWithObjectsResponse swo = ApiHandler.getScene(TPS);
                if (ApiHandler.hasError(errorField, swo,false)) {
                    return;
                }
                sl.interrupt();
                Main.Lauch(token, projectID,swo.sceneWithObjects);
            };
            Thread thread = new Thread(task);
            thread.start();
        });

        delete.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(null, loaderLabel,"Удаляем", sceneForm);
            sl.start();
            Runnable task = () -> {
                Result result = ApiHandler.deleteScene(TPS);
                if (ApiHandler.hasError(errorField, result, false)) {
                    return;
                }
                page.tryUpdate(token, projectID);
                sl.interrupt();
            };
            Thread thread = new Thread(task);
            thread.start();
        });

        Update(scene);
    }

    public void Update( SceneWithObjects scene){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
        addLabel.setText(dateFormat.format( scene.scene.Add ));

        descriptionLabel.setText(scene.scene.About);
        nameLabel.setText(scene.scene.Name);
        editLabel.setText(ProjectForm.editString(scene.scene.Edit));
        int textures = 0, images = 0, forms = 0;
        for (EryObject obj: scene.files) {
            if (obj.is_form) {
                forms++;
            }
            else if (obj.is_texture) {
                textures++;
            }
            else if (obj.is_image) {
                images++;
            }
        }
        erysLabel.setText(""+scene.erythrocytes.size());
        formLabel.setText(""+forms);
        textureLabel.setText(""+textures);
        imagesLabel.setText(""+images);
    }

    void correctEnables() {
        if (projectMember.Token.Owner) {
            delete.setEnabled(true);
        } else {
            delete.setEnabled(false);
        }
    }

    @Override
    public void Enable() {
        delete.setEnabled(true);
        open.setEnabled(true);
        correctEnables();
    }

    @Override
    public void Disable() {
        delete.setEnabled(false);
        open.setEnabled(false);
    }
}
