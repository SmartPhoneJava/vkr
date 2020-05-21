import javax.swing.*;

class Invitation {
    public int projectID;
    public Boolean enable;
    public Boolean inverse;
    public String label;
    public ProjectPage page;

    Invitation(int ProjectID, Boolean Enable, Boolean Inverse, String Label, ProjectPage Page) {
        projectID = ProjectID;
        enable = Enable;
        inverse = Inverse;
        label = Label;
        page = Page;
    }
}

public class UserForm extends JPanel implements EnableDisableI{
    public JPanel root;
    private JButton openButton;
    private JLabel photoLabel;
    private JButton inviteButton;
    private JLabel nameLabel;
    private JLabel errorField;
    private JLabel loaderLabel;

    UserForm(AuthToken token, JFrame frame, Icon openIcon, User user, int myID, Invitation invitation) {
        errorField.setVisible(false);

        LoaderStatus sl = new LoaderStatus(frame,loaderLabel,"Загружается аватарка", this);
        sl.start();
        Runnable task = () -> {
            ProfilePageForm.updateUserImage(photoLabel, errorField, user.PhotoTitle, user.ID, 30, 30);
            sl.interrupt();
        };
        Thread thread = new Thread(task);
        thread.start();

        openButton.setIcon(openIcon);
        nameLabel.setText(user.Name);
        if (invitation != null) {
            inviteButton.setVisible(true);
            if (invitation.label != "") {
                inviteButton.setText(invitation.label);
            } else {
                inviteButton.setText("пригласить");
            }
            inviteButton.setEnabled(invitation.enable);
        } else {
            inviteButton.setVisible(false);
        }
        inviteButton.addActionListener(actionEvent -> {
            Result result;
            if (!invitation.inverse) {
                result = ApiHandler.inviteUser(token, invitation.projectID, user.ID);
            } else {
                result = ApiHandler.kickUser(token, invitation.projectID, user.ID);
            }
            if (ApiHandler.hasError(nameLabel, result, false)) {
                return;
            }
            nameLabel.setVisible(true);

            if (!invitation.inverse) {
                inviteButton.setText("отменить");
            } else {
                inviteButton.setText("пригласить");
            }
            invitation.page.tryUpdate(token,invitation.projectID);
            invitation.inverse = !invitation.inverse;

        });

        openButton.addActionListener(actionEvent -> {
            ProfileForm profileForm = new ProfileForm(frame, user, myID, token);
            frame.setContentPane(profileForm.root);
        });
    }

    @Override
    public void Enable() {

    }

    @Override
    public void Disable() {

    }
}
