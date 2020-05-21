import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

class EryObject {
    @SerializedName("id")
    int id;
    @SerializedName("user_id")
    int user_id;
    @SerializedName("scene_id")
    int scene_id;
    @SerializedName("name")
    String name;
    @SerializedName("path")
    String path;
    @SerializedName("about")
    String about;
    @SerializedName("source")
    String source;
    @SerializedName("is_form")
    Boolean is_form;
    @SerializedName("is_texture")
    Boolean is_texture;
    @SerializedName("is_image")
    Boolean is_image;
    @SerializedName("public")
    Boolean is_public;
    @SerializedName("add")
    Date add;

    EryObject(int ID, int userID, int sceneID, String Name, String Path, String About, String Source,
              Boolean isForm,  Boolean isTexture, Boolean isImage,Boolean isPublic, Date Add) {
        id = ID;
        user_id = userID;
        scene_id = sceneID;
        name = Name;
        path = Path;
        about = About;
        source = Source;
        is_form = isForm;
        is_texture = isTexture;
        is_image = isImage;
        is_public = isPublic;
        add = Add;
    }
}

class Disease {
    @SerializedName("id")
    int id;
    @SerializedName("user_id")
    int user_id;
    @SerializedName("scene_id")
    int scene_id;
    @SerializedName("name")
    String name;
    @SerializedName("about")
    String about;
    @SerializedName("form")
    float form;
    @SerializedName("oxygen")
    float oxygen;
    @SerializedName("gemoglob")
    Boolean is_public;
    @SerializedName("add")
    Date add;
}

class SceneWithObjects {
    @SerializedName("scene")
    Scene scene;
    @SerializedName("erythrocytes")
    ArrayList<ErythrocyteInfo> erythrocytes;
    @SerializedName("files")
    ArrayList<EryObject> files;
    @SerializedName("diseases")
    ArrayList<Disease> diseases;
}

public class Scene {
    @SerializedName("id")
    public int ID;
    @SerializedName("user_id")
    public int UserID;
    @SerializedName("user_name")
    public String UserName;
    @SerializedName("user_photo")
    public String UserPhoto;
    @SerializedName("name")
    public String Name;
    @SerializedName("about")
    public String About;
    @SerializedName("project_id")
    public int ProjectID;
    @SerializedName("edit")
    public Date Edit;
    @SerializedName("editor_id")
    public int EditorID;
    @SerializedName("add")
    public Date Add;

    Scene() {

    }
    Scene(int id, int userID, String userName, String userPhoto, String name, String about,
                  int projectID, Date edit, int editorID, Date add) {
        ID = id;
        UserID = userID;
        UserName = userName;
        UserPhoto = userPhoto;
        Name = name;
        About = about;
        ProjectID = projectID;
        Edit = edit;
        EditorID = editorID;
        Add = add;
    }
}
