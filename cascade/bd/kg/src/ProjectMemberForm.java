import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

public class ProjectMemberForm  implements EnableDisableI{
    public JPanel root;
    private JTextField positionLabel;
    private JTextField statusLabel;
    private JLabel avaLabel;
    private JButton settingsButton;
    private JButton kickButton;
    private JButton acceptButton;
    private JLabel dateLabel;
    private JButton openButton;
    private JLabel nameLabel;
    private JLabel dateNearLabel;
    private JLabel errorField;
    private JLabel loaderLabel;

    public Icon AcceptImage;
    public Icon RefuseImage;
    public Icon KickImage;
    public Icon SettingsImage;

    public ProjectMemberForm(JFrame frame, int myID, ProjectPage page, JPanel backPanel, ProjectMember projectMember,
                             AuthToken token, int projectID,  Icon acceptImage, Icon refuseImage,
                             Icon kickImage, Icon settingsImage,  Icon linkImage) {
        AcceptImage = acceptImage;
        RefuseImage = refuseImage;
        KickImage = kickImage;
        SettingsImage = settingsImage;
        ProjectMemberForm pmf = this;

        System.out.println("ProjectMemberForm see:"+projectID);

        kickButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Выгоняем", pmf);
                sl.start();
                Runnable task = () -> {
                    Result result = ApiHandler.kickUser(token, projectID, projectMember.ID);
                    if (ApiHandler.hasError(errorField, result, false)) {
                        return;
                    }
                    page.tryUpdate(token, projectID);
                    sl.interrupt();
                    frame.revalidate();
                };
                Thread thread = new Thread(task);
                thread.start();
            }
        });

        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Одобряем", pmf);
                sl.start();
                Runnable task = () -> {
                    Result result = ApiHandler.inviteUser(token, projectID, projectMember.ID);
                    if (ApiHandler.hasError(errorField, result, false)) {
                        return;
                    }
                    page.tryUpdate(token, projectID);
                    sl.interrupt();
                    frame.revalidate();
                };
                Thread thread = new Thread(task);
                thread.start();
            }
        });

        openButton.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Переходим", pmf);
            sl.start();
            Runnable task = () -> {
                UserResponse uwt = ApiHandler.getUserByID(token, projectMember.ID);
                if (ApiHandler.hasError(errorField, uwt, false)) {
                    return;
                }
                ProfileForm profileForm = new ProfileForm(frame, uwt.user, myID, token);
                frame.setContentPane(profileForm.root);
                sl.interrupt();
                frame.revalidate();
            };
            Thread thread = new Thread(task);
            thread.start();
        });

        errorField.setVisible(false);
        loaderLabel.setVisible(false);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        dateLabel.setText(dateFormat.format( projectMember.User.From));

        positionLabel.setText(projectMember.User.Position);
        nameLabel.setText(projectMember.Name);

        openButton.setIcon(linkImage);
        if (projectMember.User.ProjectConfirmed && projectMember.User.UserConfirmed) {
            statusLabel.setText("Полноправный участник");
        } else {
            statusLabel.setText("Кандидат");
        }

        settingsButton.addActionListener(actionEvent -> {
            ProjectTokenForm form = new ProjectTokenForm(frame, token, projectID, page,  backPanel, projectMember);
            page.setInformationPanel(form.root);
        });

        Image image = FileHandler.getAva(projectMember.PhotoTitle, projectMember.ID);
        Icon icon = imagePanel.getSceledIcon(image, 100, 100);
        avaLabel.setIcon(icon);
        settingsButton.setIcon(SettingsImage);
        acceptButton.setIcon(AcceptImage);

        membersMod(projectMember.Token);
    }


    public void inviteMod(ProjectToken token) {
        if (token.EditMembersList || token.Owner) {
            kickButton.setEnabled(true);
        } else {
            kickButton.setEnabled(false);
        }
        settingsButton.setVisible(false);
        acceptButton.setVisible(false);

        kickButton.setIcon(RefuseImage);
        dateNearLabel.setText("Приглашен:");
    }

    public void membersMod(ProjectToken token) {
        if (token.EditMembersList || token.Owner) {
            kickButton.setEnabled(true);
            settingsButton.setEnabled(true);
        } else {
            settingsButton.setEnabled(false);
            kickButton.setEnabled(false);
        }
        settingsButton.setVisible(true);
        acceptButton.setVisible(false);
        kickButton.setIcon(KickImage);
        dateNearLabel.setText("С нами с:");
    }

    public void applyMod(ProjectToken token) {
        if (token.EditMembersList || token.Owner) {
            kickButton.setEnabled(true);
            acceptButton.setEnabled(true);
        } else {
            acceptButton.setEnabled(false);
            kickButton.setEnabled(false);
        }
        settingsButton.setVisible(false);
        acceptButton.setVisible(true);
        kickButton.setIcon(RefuseImage);
        dateNearLabel.setText("Подал заявку:");
    }

    @Override
    public void Enable() {

    }

    @Override
    public void Disable() {

    }
}
