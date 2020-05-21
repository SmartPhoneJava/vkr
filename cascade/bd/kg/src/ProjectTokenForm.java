import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProjectTokenForm extends JPanel implements EnableDisableI{
    private JCheckBox nameCheck;
    private JCheckBox infoCheck;
    private JCheckBox statusCheck;
    private JButton cancelButton;
    private JButton saveButton;
    private JLabel errorField;
    private JCheckBox membersListCheck;
    private JCheckBox membersTokenCheck;
    private JCheckBox closeCheck;
    private JLabel userNameLabel;
    public JPanel root;
    private JCheckBox sceneCheck;
    private JLabel loaderLabel;
    public ProjectMember saved;

    ProjectTokenForm(JFrame frame, AuthToken authToken, int projectID, ProjectPage page, JPanel backPanel, ProjectMember member) {
        saved = member;

        System.out.println("ProjectTokenForm see:"+projectID);

        errorField.setVisible(false);
        loaderLabel.setVisible(false);

        cancelButton.addActionListener(actionEvent -> page.setInformationPanel(backPanel));
        userNameLabel.setText(member.Name);
        Update(member.Token);

        ProjectTokenForm ptf = this;
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Сохраняем", ptf);
                sl.start();
                Runnable task = () -> {
                    ProjectToken projectToken = new ProjectToken(member.Token.ID, closeCheck.isSelected(),
                            nameCheck.isSelected(), infoCheck.isSelected(), statusCheck.isSelected(),
                            sceneCheck.isSelected(), membersListCheck.isSelected(), membersTokenCheck.isSelected());
                    ProjectTokenResponse wt = ApiHandler.updateToken(authToken, projectID, member.ID, projectToken);
                    if (ApiHandler.hasError(errorField, wt, false)) {
                        return;
                    }
                    Update(projectToken);
                    sl.interrupt();
                    frame.revalidate();
                };
                Thread thread = new Thread(task);
                thread.start();
            }
        });
    }

    void Update(ProjectToken token) {
        if (token.EditName) {
            nameCheck.setSelected(true);
        }
        if (token.EditAccess) {
            statusCheck.setSelected(true);
        }
        if (token.EditInfo) {
            infoCheck.setSelected(true);
        }
        if (token.EditMembersList) {
            membersListCheck.setSelected(true);
        }
        if (token.EditScene) {
            sceneCheck.setSelected(true);
        }
        if (token.EditMembersToken) {
            membersTokenCheck.setSelected(true);
        }
        if (token.Owner) {
            closeCheck.setSelected(true);
        }
    }

    @Override
    public void Enable() {

    }

    @Override
    public void Disable() {

    }
}
