import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InviteForm extends JPanel{
    private JTextField userNameLabel;
    private JButton backButtom;
    private JPanel peoplePanel;
    public JPanel root;
    private JLabel errorField;
    private JLabel inviteLabel;
    private JButton applyButton;
    private JScrollPane scroll;

    InviteForm(ProjectPage page,  JFrame frame, JPanel backPanel, AuthToken token, ProjectWithMembers project) {
        errorField.setVisible(false);

        backButtom.addActionListener(actionEvent -> page.setInformationPanel(backPanel));
        peoplePanel.setLayout(new BoxLayout(peoplePanel, BoxLayout.Y_AXIS));

        Icon open = getIcon("res/link.png", 20, 20);

        setUserForms(token, frame, "", open, project, page);
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setUserForms(token, frame, userNameLabel.getText(), open, project, page);
            }
        });
    }

    public void setUserForms(AuthToken token, JFrame frame, String name, Icon open,
                             ProjectWithMembers project, ProjectPage page) {
        UsersResponse uwt = ApiHandler.getAllUsers(token, name);
        if (ApiHandler.hasError(errorField, uwt, false)) {
            return;
        }

        int height = 0;
        peoplePanel.removeAll();
        project.members.sort(ProjectMember.COMPARE_BY_ID);
        uwt.users.users.sort(User.COMPARE_BY_ID);
        System.out.println("members:"+project.members.size());
        System.out.println("users:"+uwt.users.users.size());
        int index = 0;
        for (User user: uwt.users.users) {
            Boolean enable = true;
            Boolean inverse = false;
            String inviteString = "пригласить";
            if (index < project.members.size()) {
                ProjectMember member = project.members.get(index);
                if (user.ID == member.ID) {
                    System.out.println("found:"+user.ID);
                    index++;
                    if (member.User.UserConfirmed && member.User.ProjectConfirmed) {
                        enable = false;
                        if (project.you.ID == user.ID) {
                            //inviteString = "я";
                            continue;
                        } else {
                            inviteString = "участник";
                        }
                    } else if (member.User.UserConfirmed) {
                        enable = true;
                        inverse = false;
                        inviteString = "принять заявку";
                    } else if (member.User.ProjectConfirmed) {
                        enable = true;
                        inverse = true;
                        inviteString = "отменить";
                    }
                }
            }
            UserForm uForm = new UserForm(token, frame, open, user, project.you.ID,
                    new Invitation(project.project.ID, enable, inverse, inviteString, page));
            peoplePanel.add(uForm.root);
        }
        peoplePanel.revalidate();
        scroll.revalidate();

    }

    public Icon getIcon(String path, int width, int heigt) {
        imagePanel image = new imagePanel(width, heigt);
        image.setImage(path);
        return new ImageIcon(image.image);
    }
}
