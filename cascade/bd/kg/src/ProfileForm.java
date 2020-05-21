import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ProfileForm extends JPanel implements EnableDisableI, ListOfProjectsI{
    public JPanel root;
    private JScrollPane srollProjects;
    private JPanel some;
    private JRadioButton memberRB;
    private JRadioButton applyRB;
    private JRadioButton invitedRB;
    private JRadioButton ownerRB;
    private JButton createProject;
    private JButton peopleButton;
    private JButton profileButton;
    private JButton projectsButton;
    private JLabel errorField;
    private JLabel noProjectsLabel;
    private JLabel loaderLabel;
    private JPanel profilePagePanel;
    private JLabel showProjectsLabel;
    private Icon open;

    private ProfileForm profileForm;

    static Icon openIcon;

    static Icon setOpenIcon() {
        openIcon = imagePanel.getIcon("res/link.png", 30, 30);
        return openIcon;
    }

    static Icon getOpenIcon() {
        return openIcon;
    }

    ProfileForm(JFrame frame, User user, int myID, AuthToken token) {
        open = ProfileForm.getOpenIcon();
        if (open == null) {
            open = ProfileForm.setOpenIcon();
        }
        loaderLabel.setVisible(false);
        errorField.setVisible(false);
        profileForm = this;

       // System.out.println("compare two ids:"+user.ID+","+myID);
        Boolean myProfile = user == null || user.ID == myID;
        if (myProfile) {
            profileButton.setBackground(Color.decode("#3BBA36"));

            showProjectsLabel.setVisible(true);
            memberRB.setVisible(true);
            applyRB.setVisible(true);
            invitedRB.setVisible(true);
            ownerRB.setVisible(true);

            memberRB.setVisible(true);
            applyRB.setVisible(true);
            invitedRB.setVisible(true);
            ownerRB.setVisible(true);
            createProject.setVisible(true);
            noProjectsLabel.setText("Проектов, удовлетворяющих условиям, нет");
            updateProjects(frame, token, myID);
        } else {
            profileButton.setBackground(Color.decode("#1A65AE"));

            showProjectsLabel.setVisible(false);
            memberRB.setVisible(false);
            applyRB.setVisible(false);
            invitedRB.setVisible(false);
            ownerRB.setVisible(false);

            noProjectsLabel.setText("Только владелец страницы может увидеть список своих проектов");
            createProject.setVisible(false);
            srollProjects.setVisible(false);
        }

        ProfilePageForm ppf = new ProfilePageForm(frame, user, token, myProfile);
        profilePagePanel.add(ppf.root);

        ButtonGroup  group = new ButtonGroup();
        group.add(memberRB);
        group.add(applyRB);
        group.add(invitedRB);
        group.add(ownerRB);

        memberRB.setSelected(true);
        memberRB.addActionListener(actionEvent -> {
            chooseProjectType((member) -> member.User.UserConfirmed && member.User.ProjectConfirmed);
        });
        applyRB.addActionListener(actionEvent -> {
            chooseProjectType((member) -> member.User.UserConfirmed && !member.User.ProjectConfirmed);
        });
        invitedRB.addActionListener(actionEvent -> {
            chooseProjectType((member) -> !member.User.UserConfirmed && member.User.ProjectConfirmed);
        });
        ownerRB.addActionListener(actionEvent -> {
            chooseProjectType((member) -> member.Token.Owner);
        });

        createProject.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Создаем проект", profileForm);
            sl.start();
            Runnable task = () -> {
                Project project = new Project(1, "", false, false, "",
                        null , true, true,  null ,
                        0, 1, 1, true, 1);
                ProjectWithMembersResponse uwt = ApiHandler.postProject(project, token);
                if (ApiHandler.hasError(errorField, uwt, false)) {
                    return;
                }

                updateProjects(frame, token, myID);
                ProjectPage pp = new ProjectPage(frame,root, token, uwt.project, myID);
                frame.setContentPane(pp.root);
                sl.interrupt();
                frame.revalidate();
            };
            Thread thread = new Thread(task);
            thread.start();
        });


        peopleButton.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Загружаем списки людей", profileForm);
            sl.start();
            Runnable task = () -> {
                ListForm listform = new ListForm(frame, root, token,  true, myID, open);
                sl.interrupt();
                frame.setContentPane(listform.root);
                frame.revalidate();
            };
            Thread thread = new Thread(task);
            thread.start();
        });

        projectsButton.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Загружаем списки проектов", profileForm);
            sl.start();
            Runnable task = () -> {
                ListForm listform = new ListForm(frame, root, token,  false, myID, open);
                sl.interrupt();
                frame.setContentPane(listform.root);
                frame.revalidate();
            };
            Thread thread = new Thread(task);
            thread.start();
        });

        profileButton.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Заходим в профиль", profileForm);
            sl.start();
            Runnable task = () -> {
                ProfileForm profileForm = new ProfileForm(frame, null, myID, token);
                sl.interrupt();
                frame.setContentPane(profileForm.root);
                frame.revalidate();
            };
            Thread thread = new Thread(task);
            thread.start();
        });

        revalidate();

    }

    @Override
    public void Enable() {
        peopleButton.setVisible(true);
        profileButton.setVisible(true);
        projectsButton.setVisible(true);
    }

    @Override
    public void Disable() {
        peopleButton.setVisible(false);
        profileButton.setVisible(false);
        projectsButton.setVisible(false);
    }

    @Override
    public void remove(ProjectForm pf) {
        some.remove(pf.root);
        int newWidth = some.getPreferredSize().width;
        if (newWidth > 450) {
            newWidth -= 450;
        }
        some.setPreferredSize(new Dimension(newWidth, 180));
        some.revalidate();
        srollProjects.revalidate();

        UserInProject uip = pf.saved.you.User;
        ProjectToken pt = pf.saved.you.Token;
        if (pt.Owner) {
            owners--;
            members--;
        } else if (uip.ProjectConfirmed) {
            if (uip.UserConfirmed) {
                members--;
            } else {
                invited--;
            }
        } else {
            applyed--;
        }
        showProjects(-1);
        setAmounts();

    }

    @Override
    public void edit(ProjectWithMembers oldProject) {
        UserInProject oldUser = oldProject.you.User;

        if (!oldUser.UserConfirmed) {
            invited--;
            members++;
        } else {
            applyed--;
            members++;
        }
        setAmounts();
    }

    interface projectVisibleI {
        public boolean valid(ProjectMember member);
    }

    void chooseProjectType(projectVisibleI pvi){
        int size = projectsPanel.size();
        int found = 0;
        for (int i = 0; i < size; i++) {
            if (pvi.valid(projectsType.get(i))) {
                projectsPanel.get(i).setVisible(true);
                found++;
            } else {
                projectsPanel.get(i).setVisible(false);
            }
        }
        showProjects(found);
    }

    private void createUIComponents() {

    }

    private ArrayList<JPanel> projectsPanel = new  ArrayList<JPanel>();
    private ArrayList<ProjectMember> projectsType = new ArrayList<ProjectMember>();
    private int members = 0, applyed = 0, invited = 0, owners = 0;
    private void updateProjects(JFrame frame, AuthToken token, int myID) {
        errorField.setVisible(false);

        ProjectsResponse uwt = ApiHandler.getProjects(token);

        String errorText = uwt.getError();
        if (errorText != null) {
            errorField.setVisible(true);
            errorField.setText(errorText);
            return;
        }

        some.removeAll();
        projectsPanel.removeAll(projectsPanel);
        projectsType.removeAll(projectsType);
        int width = 0;
        for (ProjectWithMembers project: uwt.projects.projects) {
            ProjectForm pf = new ProjectForm(frame, root, profileForm, token, project, open, myID);
            pf.setVisible(true);
            some.add(pf.root);
            projectsPanel.add(pf.root);
            projectsType.add(project.you);
           width += 450;
        }

        int size = projectsPanel.size();
        members = 0; applyed = 0; invited = 0; owners = 0;
        for (int i = 0; i < size; i++) {
            if (projectsType.get(i).User.ProjectConfirmed && projectsType.get(i).User.UserConfirmed) {
                projectsPanel.get(i).setVisible(true);
                members++;
            } else {
                projectsPanel.get(i).setVisible(false);
            }
            if (projectsType.get(i).User.ProjectConfirmed && !projectsType.get(i).User.UserConfirmed) {
                invited++;
            }
            if (!projectsType.get(i).User.ProjectConfirmed && projectsType.get(i).User.UserConfirmed) {
                applyed++;
            }
            if (projectsType.get(i).Token.Owner) {
                owners++;
            }
        }

        setAmounts();

        if (memberRB.isSelected()) {
            chooseProjectType((member) -> member.User.UserConfirmed && member.User.ProjectConfirmed);
        } else if (applyRB.isSelected()) {
            chooseProjectType((member) -> member.User.UserConfirmed && !member.User.ProjectConfirmed);
        }  else if (invitedRB.isSelected()) {
            chooseProjectType((member) -> !member.User.UserConfirmed && member.User.ProjectConfirmed);
        } else {
            chooseProjectType((member) -> member.Token.Owner);
        }

        Dimension pref = new Dimension(width,180);
        some.setPreferredSize(pref);
        some.revalidate();
        srollProjects.revalidate();
    }

    private void setAmounts() {
        invitedRB.setText("которые отправили мне приглашение("+invited+")");
        applyRB.setText("в которые я подал заявку("+applyed+")");
        memberRB.setText("в которых я состою("+members+")");
        ownerRB.setText("которые принадлежат мне("+owners+")");
    }

    private void showProjects(int found) {
        if (found == -1) {
            if (invitedRB.isSelected()) {
                found = invited;
            } else   if (applyRB.isSelected()) {
                found = applyed;
            } else   if (memberRB.isSelected()) {
                found = members;
            } else {
                found = owners;
            }
        }

        if (found == 0) {
            noProjectsLabel.setVisible(true);
            srollProjects.setVisible(false);
        } else {
            noProjectsLabel.setVisible(false);
            srollProjects.setVisible(true);
        }
    }
}

// 263
