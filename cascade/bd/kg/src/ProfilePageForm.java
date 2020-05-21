import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;

public class ProfilePageForm implements EnableDisableI{
    private JTextField nameLabel;
    private JTextField emailLabel;
    private JEditorPane AboutLabel;
    private JButton exitButton;
    private JButton saveButton;
    private JButton updateAvaButtom;
    private JLabel errorField;
    private JLabel loaderLabel;
    private JLabel imageLabel;
    private JLabel addLabel;
    private JTextField websiteLabel;
    private JTextField contactsLabel;
    public JPanel root;
    private JButton cancelButton;

    ProfilePageForm(JFrame frame, User user, AuthToken token, Boolean myProfile) {
        loaderLabel.setVisible(false);
        errorField.setVisible(false);
        cancelButton.setVisible(false);
        setUneditable();

        if (myProfile) {
            exitButton.setVisible(true);
            updateAvaButtom.setVisible(true);
            saveButton.setVisible(true);
        } else {
            exitButton.setVisible(false);
            cancelButton.setVisible(false);
            updateAvaButtom.setVisible(false);
            saveButton.setVisible(false);
        }

        ProfilePageForm form = this;
        saveButton.addActionListener(actionEvent -> {
            if (editing) {
                setUneditable();
                LoaderStatus sl = new LoaderStatus(frame, loaderLabel, "Обновляем", form);
                Runnable task = () -> {
                    User user1 = new User(0, nameLabel.getText(), null, null, websiteLabel.getText(),
                            AboutLabel.getText(), emailLabel.getText(),
                            contactsLabel.getText(), null, null, null);
                    UserResponse uwt = ApiHandler.updateUser(user1, token);
                    if (ApiHandler.hasError(errorField, uwt, false)) {
                        return;
                    }
                    Update(uwt.user);
                    sl.interrupt();
                };
                Thread thread = new Thread(task);
                thread.start();
            } else {
                setEditable();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (editing) {
                    setUneditable();
                }
            }
        });

        exitButton.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel, "Выходим", form);
            Runnable task = () -> {
                Result result = ApiHandler.logout(token);
                token.autoSave = false;
                token.Save();
                if (ApiHandler.hasError(errorField, result, false)) {
                    return;
                }
                frame.dispose();
                LoginForm loginForm = new LoginForm(false);
                loginForm.setVisible(true);
                sl.interrupt();
            };
            Thread thread = new Thread(task);
            thread.start();
        });

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выбрите аватар");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        updateAvaButtom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                LoaderStatus sl = new LoaderStatus(frame, loaderLabel, "Обновляем аватарку", form);
                Runnable task = () -> {
                    int result = fileChooser.showOpenDialog(frame);
                    File newFile = null;
                    if (result == JFileChooser.APPROVE_OPTION) {
                        newFile = fileChooser.getSelectedFile();
                    } else if (result == JFileChooser.CANCEL_OPTION){
                        return;
                    }
                    if (newFile == null) {
                        return;
                    }
                    UserResponse uwt = ApiHandler.createAva(token, newFile);
                    if (ApiHandler.hasError(errorField, uwt, false)) {
                        return;
                    }
                    updateImage(uwt.user.PhotoTitle, uwt.user.ID);
                    sl.interrupt();
                };
                Thread thread = new Thread(task);
                thread.start();
            }
        });

        if (user == null) {
            if (myProfile) {
                UserResponse uwt = ApiHandler.getUser(token);
                if (ApiHandler.hasError(errorField, uwt, false)) {
                    return;
                }
                user = uwt.user;
                System.out.println("user photo:"+ user.PhotoTitle);
            } else {
                errorField.setText("Ошибка: нет данных о пользователе");
                return;
            }
        }
        Update(user);
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
    public void Update(User user) {
        nameLabel.setText(user.Name);
        emailLabel.setText(user.Email);
        AboutLabel.setText(user.About);
        websiteLabel.setText(user.Website);
        contactsLabel.setText(user.Phone);

        updateImage(user.PhotoTitle, user.ID);
        addLabel.setText(dateFormat.format( user.Add ));
    }

    public void updateImage(String path, int userID) {
        updateUserImage(imageLabel, errorField, path, userID, 300, 300);
    }

    public static void updateUserImage(JLabel imageLabel, JLabel errorField, String path,
                                       int userID, int width, int height) {
        Image image = FileHandler.getAva(path, userID);
        if (image == null) {
            errorField.setVisible(true);
            errorField.setText("Ошибка: не удалось загрузить изображение");
            return;
        }
        Icon icon = imagePanel.getSceledIcon(image, width, height);
        imageLabel.setIcon(icon);
    }


    boolean editing = false;
    private void setEditable() {
        cancelButton.setVisible(true);

        nameLabel.setEditable(true);
        nameLabel.setBackground(Color.decode("#FFFFFF"));

        AboutLabel.setEditable(true);
        AboutLabel.setBackground(Color.decode("#FFFFFF"));

        emailLabel.setEditable(true);
        emailLabel.setBackground(Color.decode("#FFFFFF"));

        contactsLabel.setEditable(true);
        contactsLabel.setBackground(Color.decode("#FFFFFF"));

        websiteLabel.setEditable(true);
        websiteLabel.setBackground(Color.decode("#FFFFFF"));

        editing = true;
        saveButton.setText("Сохранить");
    }

    private void setUneditable() {
        cancelButton.setVisible(false);

        nameLabel.setEditable(false);
        nameLabel.setBackground(Color.decode("#F4F5F6"));

        AboutLabel.setEditable(true);
        AboutLabel.setBackground(Color.decode("#F4F5F6"));

        emailLabel.setEditable(false);
        emailLabel.setBackground(Color.decode("#F4F5F6"));

        contactsLabel.setEditable(false);
        contactsLabel.setBackground(Color.decode("#F4F5F6"));

        websiteLabel.setEditable(false);
        websiteLabel.setBackground(Color.decode("#F4F5F6"));

        editing = false;
        saveButton.setText("Редактировать");
    }

    @Override
    public void Enable() {
        nameLabel.setEnabled(false);
        emailLabel.setEnabled(false);
        AboutLabel.setEnabled(false);
        saveButton.setEnabled(false);
        updateAvaButtom.setEnabled(false);
        websiteLabel.setEnabled(false);
        contactsLabel.setEnabled(false);
        root.setEnabled(false);
    }

    @Override
    public void Disable() {
        nameLabel.setEnabled(true);
        emailLabel.setEnabled(true);
        AboutLabel.setEnabled(true);
        saveButton.setEnabled(true);
        updateAvaButtom.setEnabled(true);
        websiteLabel.setEnabled(true);
        contactsLabel.setEnabled(true);
        root.setEnabled(true);
    }
}
