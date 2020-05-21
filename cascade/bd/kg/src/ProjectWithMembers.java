import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ProjectWithMembers {
    @SerializedName("project")
    public Project project;
    @SerializedName("members")
    public ArrayList<ProjectMember> members;
    @SerializedName("scenes")
    public ArrayList<SceneWithObjects>  scenes;
    @SerializedName("you")
    public ProjectMember you;

    ProjectWithMembers(Project sproject, ArrayList<ProjectMember> smembers, ArrayList<SceneWithObjects>  sscenes, ProjectMember syou) {
        project = sproject;
        members = smembers;
        scenes = sscenes;
        you = syou;
    }

    Boolean canAccess() {
        return this.project.PublicAccess || (this.you.User.UserConfirmed && this.you.User.ProjectConfirmed);
    }

    Boolean isMember() {
        return this.you.User.UserConfirmed && this.you.User.ProjectConfirmed;
    }
}