

import javax.swing.*;

public class RegisterForm extends JPanel implements EnableDisableI{
    private JTextField name;
    private JPasswordField password;
    private JButton register;
    private JLabel errorField;
    public JPanel root;
    private JPasswordField repeatPassword;
    private JTextField email;
    private JTextField contacts;
    private JLabel label1;
    private JLabel loadingLabel;
    private JButton cancel;
    public JFrame saveFrame;

    public RegisterForm(JFrame frame, JPanel back) {
        saveFrame = frame;
        loadingLabel.setVisible(false);
        cancel.addActionListener(actionEvent -> { frame.setContentPane(back); });
        RegisterForm rf = this;

        register.addActionListener(actionEvent -> {

            LoaderStatus sl = new LoaderStatus(frame, loadingLabel,"Регистрируемся", rf);
            sl.start();
            Runnable task = () -> {
                PostUser();
                sl.interrupt();
            };
            Thread thread = new Thread(task);
            thread.start();
        });
        errorField.setVisible(false);
    }

    public void PostUser() {
        if (!String.valueOf(password.getPassword()).equals(String.valueOf(repeatPassword.getPassword()))) {
            errorField.setVisible(true);
            errorField.setText("Ошибка: пароли не совпадают");
            return;
        }
        if (String.valueOf(name.getText()).equals("")) {
            errorField.setVisible(true);
            errorField.setText("Ошибка: не указано имя");
            return;
        }
        if (name.getText().length() < 3) {
            errorField.setVisible(true);
            errorField.setText("Ошибка: Слишком короткое имя. Необходимо не менее 3 символов");
            return;
        }
        if (String.valueOf(password.getPassword()).equals("")) {
            errorField.setVisible(true);
            errorField.setText("Ошибка: не указан пароль");
            return;
        }
        User user = new User(0, name.getText(),
                String.valueOf(password.getPassword()), null,
                null, null, email.getText(), contacts.getText(),
                null, null, null);
       UserResponse uwt = ApiHandler.postUser(user);
        if (ApiHandler.hasError(errorField, uwt, false)) {
            return;
        }

        Icon open = imagePanel.getIcon("res/link.png", 30, 30);
        ProfileForm profileForm = new ProfileForm(saveFrame, uwt.user, uwt.user.ID, uwt.token);
        saveFrame.setContentPane(profileForm.root);
        if (uwt.token != null) {
            System.out.println("begin save");
            uwt.token.autoSave = true;
            uwt.token.Save();
        }
        System.out.println("6.");
        saveFrame.revalidate();
    }

    @Override
    public void Enable() {
        name.setEnabled(true);
        password.setEnabled(true);
        register.setEnabled(true);
        root.setEnabled(true);
        repeatPassword.setEnabled(true);
        email.setEnabled(true);
        contacts.setEnabled(true);
        cancel.setEnabled(true);
    }

    @Override
    public void Disable() {
        name.setEnabled(false);
        password.setEnabled(false);
        register.setEnabled(false);
        root.setEnabled(false);
        repeatPassword.setEnabled(false);
        email.setEnabled(false);
        contacts.setEnabled(false);
        cancel.setEnabled(false);
    }
}

