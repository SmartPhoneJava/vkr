import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

public class ProjectPage extends JPanel implements EnableDisableI{
    public JPanel root;
    private JRadioButton membersRB;
    private JRadioButton invitedRB;
    private JRadioButton appliedRB;
    private JRadioButton anyRB;
    private JRadioButton busyRB;
    private JRadioButton freeRB;
    private JPanel scenePanel;
    private JPanel membersPanel;
    JLabel nameLabel;
    private JButton createSceneButton;
    private JButton exitButtom;
    private JButton inviteButton;
    private JButton peopleButton;
    private JButton profileButton;
    private JButton projectsButton;
    private JScrollPane membersScroll;
    private JScrollPane sceneScroll;
    private JPanel informationPanel;
    private JLabel errorField;
    private JLabel membersAmount;
    private JLabel sceneAmount;
    private JLabel loaderLabel;
    private JButton updateButton;
    private Boolean informationPanelEdited = false;
    private JPanel currentOwner;
    ProjectDescriptionForm desription;
    JFrame Frame;
    int MYID;
    Icon AcceptIcon, RefuseIcon, DeleteIcon, SettingsIcon, OpenIcon;
    ProjectWithMembers saved;

    ProjectPage(JFrame frame, JPanel back, AuthToken token, ProjectWithMembers project, int myID)  {
        Frame = frame;
        MYID = myID;
        saved = project;

        errorField.setVisible(false);
        loaderLabel.setVisible(false);

        AcceptIcon = imagePanel.getIcon("res/accep.png",20,20);
        RefuseIcon = imagePanel.getIcon("res/refuse.png",20,20);
        DeleteIcon = imagePanel.getIcon("res/deleteProject.png",20,20);
        SettingsIcon = imagePanel.getIcon("res/settings2.png",20,20);
        OpenIcon = imagePanel.getIcon("res/link.png",20,20);

        ProjectPage projectPage = this;
        desription = new ProjectDescriptionForm(frame, back, project.project, myID, project.you.Token, token, "", projectPage);
        setInformationPanel(desription.root);

        ButtonGroup  groupM = new ButtonGroup();
        groupM.add(membersRB);
        groupM.add(invitedRB);
        groupM.add(appliedRB);

        applyCorrectEnables();

        nameLabel.setName(project.project.Name);

        tryUpdate(token,project.project.ID);

        membersRB.setSelected(true);
        int size = membersForm.size();
        for (int i = 0; i < size; i++) {
            if (memebersType.get(i).User.UserConfirmed && memebersType.get(i).User.ProjectConfirmed) {
                membersForm.get(i).root.setVisible(true);
                membersForm.get(i).membersMod(project.you.Token);
            } else {
                membersForm.get(i).root.setVisible(false);
            }
        }

        ButtonGroup  groupS = new ButtonGroup();
        groupS.add(anyRB);
        groupS.add(busyRB);
        groupS.add(freeRB);
        anyRB.setSelected(true);
        membersPanel.setLayout(new BoxLayout(membersPanel, BoxLayout.Y_AXIS));
        scenePanel.setLayout(new BoxLayout(scenePanel, BoxLayout.Y_AXIS));

        membersRB.addActionListener(actionEvent -> selectMembers(project.you.Token));
        invitedRB.addActionListener(actionEvent -> selectInvited(project.you.Token));
        appliedRB.addActionListener(actionEvent -> selectApplied(project.you.Token));

        peopleButton.addActionListener(actionEvent -> {
            ListForm listform = new ListForm(frame, root, token,  true, myID, OpenIcon);
            frame.setContentPane(listform.root);
            frame.revalidate();
        });

        projectsButton.addActionListener(actionEvent -> {
            ListForm listform = new ListForm(frame, root, token,  false, myID, OpenIcon);
            frame.setContentPane(listform.root);
            frame.revalidate();
        });

        profileButton.addActionListener(actionEvent -> {
            ProfileForm profileForm = new ProfileForm(frame, null, myID, token);
            frame.setContentPane(profileForm.root);
            frame.revalidate();
        });

        int projectID = project.project.ID;

        inviteButton.addActionListener(actionEvent -> {
            ProjectWithMembersResponse uwt = ApiHandler.getProject(token, projectID);
            if (ApiHandler.hasError(errorField, uwt, false)) {
                return;
            }
            ProjectWithMembers inviteToProject = uwt.project;

            InviteForm inviteForm = new InviteForm(page, frame, desription.root, token, inviteToProject);
            setInformationPanel(inviteForm.root);
        });

        ProjectWithMembers pwt = project;

        exitButtom.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame,  loaderLabel,"Покидаем", projectPage);
            sl.start();
            Runnable task = () -> {
                Result result = ApiHandler.exitProject(token, pwt.project.ID);
                if (ApiHandler.hasError(errorField, result, false)) {
                    return;
                }
                ProfileForm profileForm = new ProfileForm(frame, null, myID, token);
                frame.setContentPane(profileForm.root);
                sl.interrupt();
                frame.revalidate();
            };
            Thread thread = new Thread(task);
            thread.start();
        });

        updateButton.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame,  loaderLabel,"Обновляем", projectPage);
            sl.start();
            Runnable task = () -> {
                tryUpdate(token, pwt.project.ID);
                sl.interrupt();
            };
            Thread thread = new Thread(task);
            thread.start();
        });


        createSceneButton.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Создаём сцену", projectPage);
            sl.start();
            Runnable task = () -> {
                SceneWithObjectsResponse swo = ApiHandler.createScene(token, projectID);
                if (ApiHandler.hasError(errorField, swo, false)) {
                    return;
                }
                final String dir = System.getProperty("user.dir");
                File textureFile = new File(dir+"/res/blood.png");
                File imageFile = new File(dir+"/res/erys.jpg");
                File formFile = new File(dir+"/res/blood_cell.obj");

                TokenProjectScene tps = new TokenProjectScene(token, projectID, swo.sceneWithObjects.scene.ID);
                ApiHandler.createEryObject(tps, false, true,false, textureFile);
                ApiHandler.createEryObject(tps, true, false,false, formFile);
                ApiHandler.createEryObject(tps, false, false,true, imageFile);
                swo = ApiHandler.getScene(tps);

                Main.Lauch(token, projectID,swo.sceneWithObjects);
                tryUpdate(token, projectID);
                sl.interrupt();
                frame.revalidate();
            };
            Thread thread = new Thread(task);
            thread.start();
        });
    }

    public void setInformationPanel(JPanel anotherPanel) {
        if (currentOwner != null) {
            informationPanel.remove(currentOwner);
        }
        informationPanel.add(anotherPanel);
        currentOwner = anotherPanel;

        informationPanel.revalidate();
        root.revalidate();
        //Frame.repaint();
    }

    public void selectApplied(ProjectToken token) {
        int size = membersForm.size();
        for (int i = 0; i < size; i++) {
            if (memebersType.get(i).User.UserConfirmed && !memebersType.get(i).User.ProjectConfirmed) {
                membersForm.get(i).root.setVisible(true);
                membersForm.get(i).applyMod(token);
            } else {
                membersForm.get(i).root.setVisible(false);
            }
        }
    }

    public void selectMembers(ProjectToken token) {
        int size = membersForm.size();
        for (int i = 0; i < size; i++) {
            if (memebersType.get(i).User.UserConfirmed && memebersType.get(i).User.ProjectConfirmed) {
                membersForm.get(i).root.setVisible(true);
                membersForm.get(i).membersMod(token);
            } else {
                membersForm.get(i).root.setVisible(false);
            }
        }
    }

    public void selectInvited(ProjectToken token) {
        int size = membersForm.size();
        for (int i = 0; i < size; i++) {
            if (!memebersType.get(i).User.UserConfirmed && memebersType.get(i).User.ProjectConfirmed) {
                membersForm.get(i).root.setVisible(true);
                membersForm.get(i).inviteMod(token);
            } else {
                membersForm.get(i).root.setVisible(false);
            }
        }
    }

    public void selectAll() {
        int size = scenesForm.size();
        for (int i = 0; i < size; i++) {
            scenesForm.get(i).root.setVisible(true);
        }
    }

    public void selectActive() {
        int size = scenesForm.size();
        for (int i = 0; i < size; i++) {
            if (ProjectForm.isOnline(scenesType.get(i).Edit)) {
                scenesForm.get(i).root.setVisible(true);
            } else {
                scenesForm.get(i).root.setVisible(false);
            }
        }
    }

    public void selectPassive() {
        int size = scenesForm.size();
        for (int i = 0; i < size; i++) {
            if (!ProjectForm.isOnline(scenesType.get(i).Edit)) {
                scenesForm.get(i).root.setVisible(true);
            } else {
                scenesForm.get(i).root.setVisible(false);
            }
        }
    }

    public void tryUpdate(AuthToken token, int projectID) {
        ProjectWithMembersResponse pwm = ApiHandler.getProject(token, projectID);
        if (ApiHandler.hasError(errorField, pwm, false)) {
            return;
        }
        Update(token, projectID, pwm.project);
    }

    public void Update(AuthToken token, int projectID, ProjectWithMembers project) {
        String editString = ProjectForm.editString(project.project.Add);
        SetProject(project.project, project.you.Token, editString);
        SetMembers(Frame, MYID, token, project.you.Token, projectID, project.members);
        SetScenes(token, projectID, project.you, project.project, project.scenes);
    }

    public void SetProject(Project project, ProjectToken token, String editString) {
       nameLabel.setText(project.Name);
       desription.Update(project, token, editString);
    }

    ArrayList<ProjectMemberForm> membersForm = new  ArrayList<ProjectMemberForm>();
    ArrayList<ProjectMember> memebersType = new ArrayList<ProjectMember>();
    public void SetMembers(JFrame frame, int myID, AuthToken token, ProjectToken projectToken, int projectID, ArrayList<ProjectMember> members) {
        membersPanel.removeAll();
        int membersA = 0, applyed = 0, invited = 0;

        membersForm.removeAll(membersForm);
        memebersType.removeAll(memebersType);

        for (ProjectMember member:members) {
            ProjectMemberForm memberForm = new ProjectMemberForm(frame, myID,this, desription.root, member, token,
                    projectID, AcceptIcon, RefuseIcon, DeleteIcon, SettingsIcon, OpenIcon);
            membersPanel.add(memberForm.root);
            membersForm.add(memberForm);
            memebersType.add(member);
            if (member.User.ProjectConfirmed && !member.User.UserConfirmed) {
                invited++;
            }
            if (!member.User.ProjectConfirmed && member.User.UserConfirmed) {
                applyed++;
            }
            if (member.User.ProjectConfirmed && member.User.UserConfirmed) {
                membersA++;
            }
        }

        if (membersRB.isSelected()) {
            selectMembers(projectToken);
        } else if (invitedRB.isSelected()) {
            selectInvited(projectToken);
        } else {
            selectApplied(projectToken);
        }

        //membersAmount.setText("Участников: "+members.size());
        appliedRB.setText("Подали заявку("+applyed+")");
        membersRB.setText("Участники("+membersA+")");
        invitedRB.setText("Приглашены("+invited+")");
        membersPanel.revalidate();
        membersScroll.revalidate();
    }

    ArrayList<SceneForm> scenesForm = new  ArrayList<SceneForm>();
    ArrayList<Scene> scenesType = new ArrayList<Scene>();
    ProjectPage page = this;
    public void SetScenes(AuthToken token, int projectID,  ProjectMember projectMember, Project project,
                          ArrayList<SceneWithObjects> scenes) {
        scenePanel.removeAll();
        int active = 0, passive = 0;

        scenesForm.removeAll(scenesForm);
        scenesType.removeAll(scenesType);

        System.out.println("scenes:"+scenes.size());
        for (SceneWithObjects scene:scenes) {
            SceneForm sceneForm = new SceneForm(page, token, OpenIcon, DeleteIcon, scene,
                    projectMember, project, projectID);
            scenePanel.add(sceneForm.root);
            scenesForm.add(sceneForm);
            scenesType.add(scene.scene);
            if (ProjectForm.isOnline(scene.scene.Edit)) {
                active++;
            } else {
                passive++;
            }
        }

        if (busyRB.isSelected()) {
            selectActive();
        } else if (freeRB.isSelected()) {
            selectPassive();
        } else {
            selectAll();
        }

        //membersAmount.setText("Участников: "+members.size());
        int all = passive+active;
        busyRB.setText("Активные("+active+")");
        freeRB.setText("Свободные("+passive+")");
        anyRB.setText("Все("+all+")");
        scenePanel.revalidate();
        sceneScroll.revalidate();
    }

    void applyCorrectEnables() {
        if (saved.isMember()) {
            inviteButton.setEnabled(true);
            exitButtom.setEnabled(true);
            createSceneButton.setEnabled(true);
        } else {
            inviteButton.setEnabled(false);
            exitButtom.setEnabled(false);
            if (saved.project.PublicEdit) {
                createSceneButton.setEnabled(true);
            } else {
                createSceneButton.setEnabled(false);
            }
        }
    }
    @Override
    public void Enable() {
        createSceneButton.setEnabled(true);
        exitButtom.setEnabled(true);
        inviteButton.setEnabled(true);
        applyCorrectEnables();
    }

    @Override
    public void Disable() {
        createSceneButton.setEnabled(false);
        exitButtom.setEnabled(false);
        inviteButton.setEnabled(false);
        applyCorrectEnables();
    }
}
