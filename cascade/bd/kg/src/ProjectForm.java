import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;

interface ListOfProjectsI {
    void remove(ProjectForm pf);
    void edit(ProjectWithMembers oldProject);
}

public class ProjectForm extends JPanel implements EnableDisableI {
    private JLabel addLabel;
    private JButton enterButton;
    private JButton open;
    public JPanel root;
    private JLabel nameLabel;
    private JLabel membersLabel;
    private JLabel sceneLabel;
   // private JEditorPane descriptionLabel;
    private JLabel editLabel;
    private JButton exitButton;
    private JLabel errorField;
    private JLabel loaderLabel;
    public ProjectWithMembers saved;

    public ProjectForm(JFrame frame, JPanel back, ListOfProjectsI listOfProjectsI,
                       AuthToken token, ProjectWithMembers project, Icon openIcon, int myID) {
        saved = project;
        loaderLabel.setVisible(false);
        Update(project);
        open.setIcon(openIcon);

        ProjectForm pf = this;
        enterButton.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Загрузка", pf);
            sl.start();
            Runnable task = () -> {
                Result result = ApiHandler.enterProject(token, saved.project.ID);
                if (ApiHandler.hasError(errorField, result, false)) {
                    return;
                }
                listOfProjectsI.edit(project);
                project.you.User.UserConfirmed = true;
                Update(project);
                sl.interrupt();
            };
            Thread thread = new Thread(task);
            thread.start();
        });

        exitButton.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Загрузка", pf);
            sl.start();
            Runnable task = () -> {
                Result result = ApiHandler.exitProject(token, saved.project.ID);
                if (ApiHandler.hasError(errorField, result, false)) {
                    return;
                }
                project.you.User.UserConfirmed = false;
                project.you.User.ProjectConfirmed = false;
                Update(project);
                listOfProjectsI.remove(pf);
                sl.interrupt();
            };
            Thread thread = new Thread(task);
            thread.start();
        });

        open.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Открываем", pf);
            sl.start();
            Runnable task = () -> {
                ProjectPage pp = new ProjectPage(frame,back, token, saved, myID);
                frame.setContentPane(pp.root);
                frame.revalidate();
                sl.interrupt();
            };
            Thread thread = new Thread(task);
            thread.start();
        });

        setVisible(true);
    }

    static String editString(Date getTime) {
        long timeUp = getTime.getTime();
        long diff = System.currentTimeMillis() - timeUp;

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        String when = "более чем год назад";
        if (diffDays > 365) {
            when = "более чем год назад";
        } else if (diffDays <= 365 && diffDays > 180) {
            when = "более чем пол года назад";
        } else  if (diffDays <= 180 && diffDays > 60) {
            when = "более чем два месяца назад";
        } else  if (diffDays <= 60 && diffDays > 30) {
            when = "более месяца назад";
        } else  if (diffDays <= 30 && diffDays > 15) {
            when = "месяц назад";
        }  else  if (diffDays <= 15 && diffDays > 7) {
            when = "на той неделе";
        }  else  if (diffDays <= 7 && diffDays > 2) {
            when = "несколько дней назад";
        }  else if (diffDays <= 2 && diffDays > 1) {
            when = "вчера";
        } else if (diffHours <= 24 && diffHours >= 5) {
            when = diffHours + " часов назад";
        }  else if (diffHours < 4 && diffHours >= 2) {
            when = diffHours + " часа назад";
        }   else if (diffHours < 2 && diffHours >= 1) {
            when = diffHours + " час назад";
        } else if (diffMinutes > 1 ) {
            long last = diffMinutes % 10;
            if (last == 0 || last > 4) {
                when = diffMinutes + " минут назад";
            } else if (last != 1){
                when = diffMinutes + " минуты назад";
            } else {
                when = diffMinutes + " минуту назад";
            }
        } else {
            long last = diffSeconds % 10;
            if (last == 0 || last > 4) {
                when = diffSeconds + " секунд назад";
            } else if (last != 1){
                when = diffSeconds + " секунды назад";
            } else {
                when = diffSeconds + " секунду назад";
            }
        }
        return when;
    }

    static Boolean isOnline(Date getTime) {
        long timeUp = getTime.getTime();
        long diff = System.currentTimeMillis() - timeUp;

        long diffMinutes = diff / (60 * 1000) % 60;
        return diffMinutes < 5;
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
    void Update(ProjectWithMembers project) {
        addLabel.setText(dateFormat.format( project.project.Add ));
        nameLabel.setText(project.project.Name);
        membersLabel.setText(Integer.toString(project.project.MembersAmount));
        sceneLabel.setText(Integer.toString(project.project.ScenesAmount));

        if (project.canAccess()) {
            open.setEnabled(true);
        } else {
            open.setEnabled(false);
        }

        errorField.setVisible(false);

        String when = ProjectForm.editString(project.project.Edit);
        editLabel.setText("Обновлено "+when);
        Boolean member =  project.you != null && project.you.User.UserConfirmed && project.you.User.ProjectConfirmed;

        if (member || project.project.PublicAccess) {
            open.setEnabled(true);
        } else {
            open.setEnabled(false);
        }

        enterButton.setVisible(false);
        exitButton.setVisible(false);

        if (member) {
            exitButton.setVisible(true);
            exitButton.setText("Покинуть проект");
        } else if (project.you.User.UserConfirmed) {
            exitButton.setVisible(true);
            exitButton.setText("Отменить заявку");
        } else if (project.you.User.ProjectConfirmed) {
            enterButton.setVisible(true);
            exitButton.setVisible(true);
            enterButton.setText("Принять приглашение");
            exitButton.setText("Отклонить приглашение");
        } else {
            enterButton.setVisible(true);
            enterButton.setText("Подать заявку");
        }
    }

    @Override
    public void Enable() {
        enterButton.setEnabled(true);
        open.setEnabled(true);
        exitButton.setEnabled(true);
    }

    @Override
    public void Disable() {
        enterButton.setEnabled(false);
        open.setEnabled(false);
        exitButton.setEnabled(false);
    }

    private void createUIComponents() {
        JButton open = new JButton();
        Icon openIcon = imagePanel.getIcon("res/open.png", 30,30);
        open.setIcon(openIcon);
        // TODO: place custom component creation code here
    }
}
