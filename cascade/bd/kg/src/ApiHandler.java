import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

interface ResponseI {
    void recordSuccess(Gson gson, String data);
    void recordFailed(Gson gson, String data);
    String getError();
}

@Builder
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class Result implements ResponseI{
    @SerializedName("place")
    public String place;
    @SerializedName("success")
    public Boolean success;
    @SerializedName("message")
    public String message;
    Result( String place, boolean success, String message) {
        this.place = place;
        this.success = success;
        this.message = message;
    }

    @Override
    public void recordSuccess(Gson gson, String data) {
        Result result;
        result =  gson.fromJson(data, Result.class);
        this.place = result.place;
        this.success = result.success;
        this.message = result.message;
    }

    @Override
    public void recordFailed(Gson gson, String data) {
        recordSuccess(gson, data);
    }

    @Override
    public String getError() {
        if (message == null) {
            return "Не получен ответ от сервера";
        }
        if (!success) {
            return message;
        }
        return null;
    }
}

class BaseResponse implements ResponseI {
    public Result result;

    @Override
    public void recordSuccess(Gson gson,  String data) {
    }

    @Override
    public void recordFailed(Gson gson, String data) {
        this.result =  gson.fromJson(data, Result.class);
    }

    @Override
    public String getError() {
        if (result == null || result.message == null) {
            return "Не получен ответ от сервера";
        }
        if (result.success) {
            return null;
        }
        return result.message;
    }
}

class UserResponse extends BaseResponse{
    public User user;
    public AuthToken token;

    UserResponse() {
        token = new AuthToken();
    }

    @Override
    public void recordSuccess(Gson gson,  String data) {
        this.user =  gson.fromJson(data, User.class);
    }

    @Override
    public String getError() {
        if (user == null || user.Password == null) {
            return super.getError();
        }
        return null;
    }
}

class ProjectsResponse extends BaseResponse{
    public Projects projects;

    @Override
    public void recordSuccess(Gson gson,  String data) {
        this.projects =  gson.fromJson(data, Projects.class);
    }

    @Override
    public String getError() {
        if (projects == null || projects.projects == null) {
            return super.getError();
        }
        return null;
    }
}
class ProjectTokenResponse extends BaseResponse{
    public ProjectToken projectToken;

    @Override
    public void recordSuccess(Gson gson,  String data) {
        this.projectToken =  gson.fromJson(data, ProjectToken.class);
    }

    @Override
    public String getError() {
        if (projectToken == null || projectToken.EditMembersList == null) {
            return super.getError();
        }
        return null;
    }
}

class ProjectResponse extends BaseResponse{
    public Project project;

    @Override
    public void recordSuccess(Gson gson,  String data) {
        this.project =  gson.fromJson(data, Project.class);
    }

    @Override
    public String getError() {
        if (project == null ||  project.About == null) {
            return super.getError();
        }
        return null;
    }
}

class SceneWithObjectsResponse extends BaseResponse{
    public SceneWithObjects sceneWithObjects;

    @Override
    public void recordSuccess(Gson gson,  String data) {
        this.sceneWithObjects =  gson.fromJson(data, SceneWithObjects.class);
    }

    @Override
    public String getError() {
        if (sceneWithObjects == null || sceneWithObjects.scene == null) {
            return super.getError();
        }
        return null;
    }
}

class UsersResponse extends BaseResponse{
    public Users users;

    @Override
    public void recordSuccess(Gson gson,  String data) {
        this.users =  gson.fromJson(data, Users.class);
    }

    @Override
    public String getError() {
        if (users == null || users.users == null) {
            return super.getError();
        }
        return null;
    }
}

class ProjectWithMembersResponse extends BaseResponse{
    public ProjectWithMembers project;

    @Override
    public void recordSuccess(Gson gson,  String data) {
        this.project =  gson.fromJson(data, ProjectWithMembers.class);
    }

    @Override
    public String getError() {
        if (project == null || project.project == null) {
            return super.getError();
        }
        return null;
    }
}

class EryObjectResponse extends BaseResponse{
    public EryObject eryObject;
    @Override
    public void recordSuccess(Gson gson,  String data) {
        this.eryObject =  gson.fromJson(data, EryObject.class);
    }

    @Override
    public String getError() {
        if (eryObject == null || eryObject.is_form == null) {
            return super.getError();
        }
        return null;
    }
}

class ErythrocyteResponse extends BaseResponse{
    public ErythrocyteInfo erythrocyte;
    @Override
    public void recordSuccess(Gson gson,  String data) {
        this.erythrocyte =  gson.fromJson(data, ErythrocyteInfo.class);
    }

    @Override
    public String getError() {
        if (erythrocyte == null || erythrocyte.add == null) {
            return super.getError();
        }
        return null;
    }
}

class TokenProjectScene {
    AuthToken token;
    int projectID;
    int sceneID;

    TokenProjectScene(AuthToken token, int projectID, int sceneID) {
        this.token = token;
        this.projectID = projectID;
        this.sceneID = sceneID;
    }
}

abstract public class ApiHandler {
    static Boolean hasError(JLabel label, ResponseI responseI, Boolean ignoreError) {
        label.setVisible(false);
        String errorText = responseI.getError();
        if (errorText != null) {
            if (!ignoreError) {
                String beginning = "invalid character";
                int begin = errorText.indexOf(beginning);
                if (begin != -1) {
                    LoginForm loginForm = new LoginForm(true);
                    loginForm.setVisible(true);
                    loginForm.errorField.setVisible(true);
                    loginForm.errorField.setText("Сессия истекла, повторно введите ваш логин и пароль.\nПосле успешного ввода, данное окно закроется и вы сможете продолжить работу.");
                } else {
                    label.setVisible(true);
                    label.setText(errorText);
                }
            }
            return true;
        }
       return false;
    }

    static UserResponse postUser(User user) {
        Gson gson = new Gson();
        UserResponse uwt = new UserResponse();
        System.out.println("token was:"+ uwt.token.tokenType);
        getResult(gson.toJson(user), "/user", "POST", uwt.token, uwt);
        System.out.println("what we know:"+uwt.token.access);
        return uwt;
    }

    static UserResponse getUser(AuthToken token) {
        UserResponse uwt = new UserResponse();
        getResult(null, "/user", "GET", token, uwt);
        return uwt;
    }

    static ProjectsResponse getProjects(AuthToken token) {
        ProjectsResponse pwt = new ProjectsResponse();
        getResult(null, "/user/projects", "GET", token, pwt);
        return pwt;
    }

    static ProjectWithMembersResponse postProject(Project project, AuthToken token) {
        Gson gson = new Gson();
        ProjectWithMembersResponse pwm = new ProjectWithMembersResponse();
        getResult(gson.toJson(project), "/user/projects", "POST", token, pwm);
        return pwm;
    }

    static UserResponse updateUser(User user, AuthToken token) {
        Gson gson = new Gson();
        UserResponse uwt = new UserResponse();
        getResult(gson.toJson(user), "/user", "PUT", token, uwt);
        return uwt;
    }

    static UserResponse createAva(AuthToken token, File file) {
        Gson gson = new Gson();
        String path ="http://localhost:3100/ery/user/image";

        String response = null;
        MultipartUtilityV2 multipart = null;
        try {
            multipart = new MultipartUtilityV2(path,token);
            System.out.println("file type:"+ file.getName());
            multipart.addFilePart("File", file);
            response = multipart.finish();
            System.out.println("response:"+ response);
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        UserResponse uwt = new UserResponse();

        uwt.user =  gson.fromJson(response, User.class);
        uwt.result =  gson.fromJson(response, Result.class);

        return uwt;
    }

    static Result logout(AuthToken token) {
        Result result = new Result("", false, "");
        getResult(null, "/session", "DELETE", token, result);
        return result;
    }

    static UserResponse login(User user) {
        Gson gson = new Gson();
        UserResponse uwt = new UserResponse();
        getResult(gson.toJson(user), "/session", "POST", uwt.token, uwt);
        return uwt;
    }

    static ProjectWithMembersResponse projectGet(AuthToken token, int id) {
        ProjectWithMembersResponse pwm = new ProjectWithMembersResponse();
        getResult(null, "/project/"+id, "GET", token, pwm);
        return pwm;
    }

    static Result enterProject(AuthToken token, int projectID) {
        Result result = new Result("", false, "");
        getResult(null, "/project/"+projectID, "POST", token, result);
        return result;
    }

    static Result exitProject(AuthToken token, int projectID) {
        Result result = new Result("", false, "");
        getResult(null, "/project/"+projectID, "DELETE", token, result);
        return result;
    }

    static Result deleteProject(AuthToken token, int projectID) {
        Result result = new Result("", false, "");
        getResult(null, "/project/"+projectID+"?delete=true", "DELETE", token, result);
        return result;
    }

    static ProjectResponse updateProject(Project project, AuthToken token, int projectID) {
        Gson gson = new Gson();
        ProjectResponse pwt = new ProjectResponse();
        getResult( gson.toJson(project), "/project/"+projectID, "PUT", token, pwt);
        return pwt;
    }

    static ProjectWithMembersResponse getProject(AuthToken token, int projectID) {
        ProjectWithMembersResponse pwm = new ProjectWithMembersResponse();
        getResult(null, "/project/"+projectID, "GET", token, pwm);
        return pwm;
    }

    static SceneWithObjectsResponse getScene(TokenProjectScene tps) {
        SceneWithObjectsResponse swo = new SceneWithObjectsResponse();
        getResult(null, "/project/"+tps.projectID+"/scene/"+tps.sceneID, "GET", tps.token, swo);
        return swo;
    }

    static  Result deleteScene(TokenProjectScene tps) {
        Result result = new Result("", false, "");
        getResult(null, "/project/"+tps.projectID+"/scene/"+tps.sceneID, "DELETE", tps.token, result);
        return result;
    }

    static SceneWithObjectsResponse createScene(AuthToken token, int projectID) {
        Gson gson = new Gson();
        SceneWithObjectsResponse swo = new SceneWithObjectsResponse();
        getResult(gson.toJson(new Scene()), "/project/"+projectID+"/scene", "POST", token, swo);
        return swo;
    }

    static ProjectsResponse getAllProjects(AuthToken token, String name) {
        ProjectsResponse pwt = new ProjectsResponse();
        String path ="/projects";
        if (name != "") {
            path = path+"?name="+name;
        }
        getResult(null, path, "GET", token, pwt);
        return pwt;
    }

    static UsersResponse getAllUsers(AuthToken token, String name) {
        UsersResponse uwt = new UsersResponse();
        String path ="/users";
        if (name != "") {
            path = path+"?name="+name;
        }
        getResult(null, path, "GET", token, uwt);
        return uwt;
    }

    static UserResponse getUserByID(AuthToken token, int userID) {
        UserResponse uwt = new UserResponse();
        String path ="/users/"+userID;
        getResult(null, path, "GET", token, uwt);
        return uwt;
    }

    static EryObjectResponse createEryObject(TokenProjectScene tps, Boolean isForm,
                                             Boolean isTexture, Boolean isImage, File file) {
        Gson gson = new Gson();
        String path ="http://localhost:3100/ery/project/"+tps.projectID+
                "/scene/"+tps.sceneID+"/erythrocyte_object";
        if (isImage) {
            path += "?is_image=true";
        } else  if (isTexture) {
            path += "?is_texture=true";
        } else if (isForm){
            path += "?is_form=true";
        }

        String response = null;
        MultipartUtilityV2 multipart = null;
        try {
            multipart = new MultipartUtilityV2(path,tps.token);
            System.out.println("file type:"+ file.getName());
            multipart.addFilePart("File", file);
            response = multipart.finish();
            System.out.println("response:"+ response);
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        EryObjectResponse eowt = new EryObjectResponse();

        eowt.eryObject =  gson.fromJson(response, EryObject.class);
        eowt.result =  gson.fromJson(response, Result.class);

        return eowt;
    }
    static Result deleteEryObject(TokenProjectScene tps, int objectID) {
        Result result = new Result("", false, "");
        String path ="/project/"+tps.projectID+"/scene/"+tps.sceneID+"/erythrocyte_object/"+objectID;
        getResult(null, path, "DELETE", tps.token, result);
        return result;
    }

    static ErythrocyteResponse createErythrocyte(TokenProjectScene tps, ErythrocyteInfo erythrocyte) {
        Gson gson = new Gson();
        String path ="/project/"+tps.projectID+"/scene/"+tps.sceneID+"/erythrocyte";
        ErythrocyteResponse ewt = new ErythrocyteResponse();
        getResult(gson.toJson(erythrocyte), path, "POST", tps.token, ewt);
        return ewt;
    }

    static ErythrocyteResponse updateErythrocyte(TokenProjectScene tps, ErythrocyteInfo erythrocyte) {
        Gson gson = new Gson();
        String path ="/project/"+tps.projectID+"/scene/"+tps.sceneID+"/erythrocyte/"+erythrocyte.id;
        System.out.println("send to"+tps.projectID+" "+tps.sceneID+" "+erythrocyte.id);
        ErythrocyteResponse ewt = new ErythrocyteResponse();
        getResult(gson.toJson(erythrocyte), path, "PUT", tps.token, ewt);
        return ewt;
    }

    static Result deleteErythrocyte(TokenProjectScene tps, ErythrocyteInfo erythrocyte) {
        Result result = new Result("", false, "");
        String path ="/project/"+tps.projectID+"/scene/"+tps.sceneID+"/erythrocyte/"+erythrocyte.id;
        getResult(null, path, "DELETE", tps.token, result);
        return result;
    }

    static Result inviteUser(AuthToken token, int projectID, int userID) {
        Result result = new Result("", false, "");
        getResult(null, "/project/"+projectID+"/members/"+userID, "POST", token, result);
        return result;
    }

    static Result kickUser(AuthToken token, int projectID, int userID) {
        Result result = new Result("", false, "");
        getResult(null, "/project/"+projectID+"/members/"+userID, "DELETE", token, result);
        return result;
    }

    static ProjectTokenResponse updateToken(AuthToken token, int projectID, int userID, ProjectToken newToken) {
        Gson gson = new Gson();
        ProjectTokenResponse pt = new ProjectTokenResponse();
        getResult(gson.toJson(newToken), "/project/"+projectID+"/members/"+userID+"/token", "PUT", token, pt);
        return pt;
    }

    static void getResult(String data, String path, String method, AuthToken token, ResponseI response) {

        String result = null;
        try {
            result = sendRequest(data, path, method, token);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        Gson gson = new Gson();
        if (result == null) {
            response.recordFailed(gson, "{\"message\":\"нет связи с сервером\", \"success\":false}");
            return;
        }
        response.recordFailed(gson, result);
        response.recordSuccess(gson, result);
    }

    static String sendRequest(String data, String path, String method, AuthToken token) throws IOException {

        URL urlForGetRequest = new URL("http://localhost:3100/ery"+path);
        String readLine = null;

        HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setDoOutput(true);

        connection.setRequestProperty("Content-Type", "application/json");

        if (token != null) {
            connection.setRequestProperty("Authorization-access", token.access);
            connection.setRequestProperty("Authorization-type", token.tokenType);
            connection.setRequestProperty("Authorization-refresh", token.refresh);
        }

        if (data != null) {
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = data.getBytes("utf-8");
                os.write(input, 0, input.length);
            } catch (Exception e) {
                e.printStackTrace();
                return  "{\"message\":\"нет связи с сервером\", \"success\":false}";
            }
        }

        AuthToken newToken = new AuthToken();
        newToken.InitWhenLogin(connection.getHeaderField("Authorization-Access"),
                connection.getHeaderField("Authorization-Type"),
                connection.getHeaderField("Authorization-Refresh"));
        if (newToken.access != null && token != null) {
            //System.out.println("tokens:"+ newToken.access + " " + newToken.tokenType + " " + newToken.refresh);
            token.InitWhenLogin(newToken.access, newToken.tokenType, newToken.refresh);
        } else {
            //System.out.println("access:"+ newToken.access );
            //System.out.println("old:"+ token == null );
            //System.out.println("no token!");
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            StringBuffer response = new StringBuffer();
            while ((readLine = in .readLine()) != null) {
                response.append(readLine);
            } in .close();
            // print result
            System.out.println("JSON String Result " + response.toString());
            return response.toString();
            //GetAndPost.POSTRequest(response.toString());
        } else {
            System.out.println("NOT WORKED:"+path);
        }
        return null;
    }

}

// 495 -> 666 -> 447 -> 541 -> 503
