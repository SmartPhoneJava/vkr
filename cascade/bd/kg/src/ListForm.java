import javax.swing.*;
import java.awt.*;

public class ListForm extends JPanel implements EnableDisableI, ListOfProjectsI{
    private JPanel elementsPanel;
    private JLabel mainLabel;
    private JLabel errorField;
    private JButton applyButton;
    private JTextField searchNameLabel;
    public JPanel root;
    private JLabel findLabel;
    private JButton peopleButton;
    private JButton profileButton;
    private JButton projectsButton;
    private JScrollPane scroll;
    private JLabel loaderLabel;
    private JLabel notFound;
    private JPanel pan;
    ListForm listFrom;

    ListForm(JFrame frame, JPanel back, AuthToken token, Boolean users, int myID, Icon open) {
        listFrom = this;
        if (users) {
            mainLabel.setText("Найти пользователя");
            findLabel.setText("по имени");
            peopleButton.setBackground(Color.decode("#3BBA36"));
            projectsButton.setBackground(Color.decode("#1A65AE"));
        } else {
            mainLabel.setText("Найти проект");
            findLabel.setText("по названию");
            projectsButton.setBackground(Color.decode("#3BBA36"));
            peopleButton.setBackground(Color.decode("#1A65AE"));
        }

        loaderLabel.setVisible(false);
        elementsPanel.setLayout(new BoxLayout(elementsPanel, BoxLayout.Y_AXIS));

        setElements(frame, back, token, users, "", myID, open);
        applyButton.addActionListener(actionEvent -> setElements(frame, back, token, users, searchNameLabel.getText(), myID, open));

        ListForm lf = this;
        peopleButton.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Загружаем списки людей", lf);
            sl.start();
            Runnable task = () -> {
                ListForm listform = new ListForm(frame, back, token,  true, myID, open);
                frame.setContentPane(listform.root);
                frame.revalidate();
                sl.interrupt();
            };
            Thread thread = new Thread(task);
            thread.start();
        });

        projectsButton.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Загружаем списки проектов", lf);
            sl.start();
            Runnable task = () -> {
                ListForm listform = new ListForm(frame, back, token,  false, myID, open);
                frame.setContentPane(listform.root);
                frame.revalidate();
                sl.interrupt();
            };
            Thread thread = new Thread(task);
            thread.start();
        });

        profileButton.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Заходим в профиль", lf);
            sl.start();
            Runnable task = () -> {
                ProfileForm profileForm = new ProfileForm(frame,null, myID, token);
                frame.setContentPane(profileForm.root);
                frame.revalidate();
                sl.interrupt();
            };
            Thread thread = new Thread(task);
            thread.start();
        });
    }

    void setElements(JFrame frame, JPanel back, AuthToken token, Boolean users, String name, int myID, Icon open) {
        notFound.setVisible(false);
        notFound.setText("Не найдено");
        pan.setVisible(true);
        int height = 0;
        if (users) {
            UsersResponse uwt = ApiHandler.getAllUsers(token, name);
            if (ApiHandler.hasError(errorField, uwt, false)) {
                return;
            }

            elementsPanel.removeAll();
            for (User user: uwt.users.users) {
                UserForm uf = new UserForm(token, frame, open, user, myID, null);
                uf.setVisible(true);
                elementsPanel.add(uf.root);
                height += 50;
            }
            notFound.setText("Пользователей '"+name+"' не найдено");
        } else {
            ProjectsResponse pwt = ApiHandler.getAllProjects(token, name);
            if (ApiHandler.hasError(errorField, pwt, false)) {
                return;
            }
            elementsPanel.removeAll();
            for (ProjectWithMembers project: pwt.projects.projects) {
                ProjectForm pf = new ProjectForm(frame, back, listFrom, token, project, open, myID);
                pf.setVisible(true);
                height += 300;
                elementsPanel.add(pf.root);
            }
            notFound.setText("Проектов '"+name+"' не найдено");
        }
        System.out.println("height is "+height);
        if (height == 0) {
            notFound.setVisible(true);
            height = 100;
            pan.setVisible(false);
        }
        elementsPanel.setPreferredSize(new Dimension(500, height));
        elementsPanel.revalidate();
        scroll.revalidate();
    }

    @Override
    public void Enable() {
        applyButton.setVisible(true);
        peopleButton.setVisible(true);
        profileButton.setVisible(true);
        projectsButton.setVisible(true);
    }

    @Override
    public void Disable() {
        applyButton.setVisible(false);
        peopleButton.setVisible(false);
        profileButton.setVisible(false);
        projectsButton.setVisible(false);
    }

    @Override
    public void remove(ProjectForm pf) {
        ;
    }

    @Override
    public void edit(ProjectWithMembers oldProject) {

    }
}
