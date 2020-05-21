import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.text.SimpleDateFormat;

public class ProjectDescriptionForm extends JPanel implements EnableDisableI{
    private JButton closeButton;
    private JButton saveButton;
    private JLabel errorField;
    private JTextField nameLabel;
    private JRadioButton publicRB;
    private JRadioButton privateRB;
    private JEditorPane descriptionLabel;
    public JPanel root;
    private JLabel addLabel;
    private JLabel editLabel;
    private JButton backButton;
    private JLabel loaderLabel;

    ProjectDescriptionForm(JFrame frame, JPanel back, Project project, int myID,
                           ProjectToken token, AuthToken authToken, String editString,
                           ProjectPage pp) {
        ButtonGroup  group = new ButtonGroup();
        group.add(publicRB);
        group.add(privateRB);

        errorField.setVisible(false);
        saveButton.setVisible(false);
        loaderLabel.setVisible(false);

        Update(project);

        ProjectDescriptionForm pdf = this;
        saveButton.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Сохраняем", pdf);
            sl.start();
            Runnable task = () -> {
                Project updated = new Project();
                updated.Name = nameLabel.getText();
                updated.About = descriptionLabel.getText();
                updated.PublicAccess = publicRB.isSelected();

                ProjectResponse pwt = ApiHandler.updateProject(updated, authToken,project.ID);
                if (ApiHandler.hasError(errorField, pwt, false)) {
                    return;
                }
                pp.nameLabel.setText(pwt.project.Name);
                saveButton.setVisible(false);
                updated = pwt.project;
                sl.interrupt();
                Update(updated);
                frame.revalidate();
            };
            Thread thread = new Thread(task);
            thread.start();
        });

        closeButton.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Закрываем", pdf);
            sl.start();
            Runnable task = () -> {
                Result result = ApiHandler.deleteProject(authToken,project.ID);
                if (ApiHandler.hasError(errorField, result, false)) {
                    return;
                }
                saveButton.setVisible(false);
                ProfileForm profileForm = new ProfileForm(frame, null, myID, authToken);
                sl.interrupt();
                frame.setContentPane(profileForm.root);
                frame.revalidate();
            };
            Thread thread = new Thread(task);
            thread.start();
        });


        backButton.addActionListener(actionEvent -> {
            frame.setContentPane(back);
            revalidate();
        });

        Update(project, token, editString);
    }

    private String originName;
    private Boolean originIsPublic;
    private String originDesription;
    public void Update(Project project) {
        originName = project.Name;
        originIsPublic = project.PublicAccess;
        originDesription = project.About;

        publicRB.addActionListener(actionEvent -> checkSave());
        privateRB.addActionListener(actionEvent -> checkSave());

        descriptionLabel.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                checkSave();
            }
            public void removeUpdate(DocumentEvent e) {
                checkSave();
            }
            public void insertUpdate(DocumentEvent e) {
                checkSave();
            }
        });

        nameLabel.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                checkSave();
            }
            public void removeUpdate(DocumentEvent e) {
                checkSave();
            }
            public void insertUpdate(DocumentEvent e) {
                checkSave();
            }
        });
    }

    public void checkSave() {
        saveButton.setVisible((!(nameLabel.getText().equals(originName)) ||
                !(descriptionLabel.getText().equals(originDesription)) ||
                !(publicRB.isSelected() == originIsPublic)));
    };

    public void applyToken(ProjectToken token) {
        closeButton.setVisible(token.Owner);
        nameLabel.setEditable(token.EditName);
        descriptionLabel.setEditable(token.EditInfo);
        publicRB.setEnabled(token.EditAccess);
        privateRB.setEnabled(token.EditAccess);
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
    public void Update(Project project, ProjectToken token, String editString) {
        saveButton.setVisible(false);
        nameLabel.setText(project.Name);
        descriptionLabel.setText(project.About);
        addLabel.setText(dateFormat.format(project.Add));
        editLabel.setText(editString);

        if (project.PublicAccess) {
            publicRB.setSelected(true);
        } else {
            privateRB.setSelected(true);
        }
        applyToken(token);
    }

    @Override
    public void Enable() {
        closeButton.setEnabled(true);
        saveButton.setEnabled(true);
        backButton.setEnabled(true);
    }

    @Override
    public void Disable() {
        closeButton.setEnabled(false);
        saveButton.setEnabled(false);
        backButton.setEnabled(false);
    }
}
