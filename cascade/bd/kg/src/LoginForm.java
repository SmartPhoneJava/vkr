import javax.swing.*;

public class LoginForm extends JFrame implements EnableDisableI{
    private JPanel root;
    private JButton register;
    private  JTextField name;
    private JPasswordField password;
    private JButton login;
    JLabel errorField;
    private JCheckBox saveMe;
    private JLabel loaderLabel;

    public LoginForm(boolean exit) {
        LoginForm frame = this;
        setVisible(true);
        setSize(1400, 1000);
        setTitle("Forum");

        Icon open = imagePanel.getIcon("res/link.png", 30, 30);
        AuthToken token = new AuthToken();
        if ( token.Init() && token.autoSave) {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Переподключаемся", frame);
            sl.start();
            Runnable task = new Runnable() {
                public void run() {
                    UserResponse uwt = ApiHandler.getUser(token);
                    if (!ApiHandler.hasError(errorField, uwt, true)) {
                        ProfileForm profileForm = new ProfileForm(frame, uwt.user, uwt.user.ID, token);
                        setContentPane(profileForm.root);
                        revalidate();
                        return;
                    }
                    sl.interrupt();
                }
            };
            Thread thread = new Thread(task);
            thread.start();
        }

        add(root);
        setContentPane(root);
        loaderLabel.setVisible(false);
        errorField.setVisible(false);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        register.addActionListener(actionEvent -> {
            RegisterForm registerForm = new RegisterForm(frame, root);
            setContentPane(registerForm.root);
            revalidate();
        });
        login.addActionListener(actionEvent -> {
            LoaderStatus sl = new LoaderStatus(frame, loaderLabel,"Добавляем", frame);
            sl.start();
            Runnable task = new Runnable() {
                public void run() {
                    User user = new User();
                    user.Name = name.getText();
                    user.Password = String.valueOf(password.getPassword());
                    UserResponse uwt = ApiHandler.login(user);
                    if (!ApiHandler.hasError(errorField, uwt, false)) {
                        if (saveMe.isSelected()) {
                            uwt.token.autoSave = true;
                            uwt.token.Save();
                        }
                        if (!exit) {
                            ProfileForm profileForm = new ProfileForm(frame, uwt.user, uwt.user.ID, uwt.token);
                            setContentPane(profileForm.root);
                            revalidate();
                        } else {
                            dispose();
                        }
                    }

                    sl.interrupt();
                }
            };
            Thread thread = new Thread(task);
            thread.start();
        });
    }
    public void Enable() {
        root.setEnabled(true);
        register.setEnabled(true);
        name.setEnabled(true);
        password.setEnabled(true);
        login.setEnabled(true);
        saveMe.setEnabled(true);
    }
    public void Disable() {
        root.setEnabled(false);
        register.setEnabled(false);
        name.setEnabled(false);
        password.setEnabled(false);
        login.setEnabled(false);
        saveMe.setEnabled(false);
    }
}
