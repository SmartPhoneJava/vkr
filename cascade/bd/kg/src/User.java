import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

class Users {
    @SerializedName("users")
    public ArrayList<User> users;
}

@Builder
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @JsonProperty("id")
    @SerializedName("id")
    public int ID;
    @JsonProperty("name")
    @SerializedName("name")
    public String Name;
    @JsonProperty("password")
    @SerializedName("password")
    public String Password;
    @JsonProperty("photoTitle")
    @SerializedName("photo_title")
    public String PhotoTitle;
    @JsonProperty("website")
    @SerializedName("website")
    public String Website;
    @JsonProperty("about")
    @SerializedName("about")
    public String About;
    @JsonProperty("email")
    @SerializedName("email")
    public String Email;
    @JsonProperty("phone")
    @SerializedName("phone")
    public String Phone;
    @JsonProperty("birthday")
    @SerializedName("birthday")
    public Date Birthday;
    @JsonProperty("add")
    @SerializedName("add")
    public Date Add;
    @JsonProperty("last_seen")
    @SerializedName("lastSeen")
    public Date LastSeen;

    User() {

    }

    User(int id, String name, String password, String photoTitle, String website, String about, String email,
            String phone,  Date birthday, Date add, Date lastSeen) {
        ID = id;
        Name = name;
        Password = password;
        PhotoTitle = photoTitle;
        Website = website;
        About = about;
        Email = email;
        Phone = phone;
        Birthday = birthday;
        Add = add;
        LastSeen = lastSeen;
    }

    public static final Comparator<User> COMPARE_BY_ID = new Comparator<User>() {
        @Override
        public int compare(User left, User right) {
            return left.ID - right.ID;
        }
    };
}

class AuthToken {
    @SerializedName("access")
    public String access;
    @SerializedName("tokenType")
    public String tokenType;
    @SerializedName("expire")
    public Date expire;
    @SerializedName("refresh")
    public String refresh;
    @SerializedName("autoSave")
    public Boolean autoSave;

    AuthToken() {
        access = "";
        tokenType = "";
        refresh = "";
        autoSave = false;
    }


    public AuthToken InitWhenLogin(String access, String tokenType, String refresh) {
        this.access = access;
        this.tokenType = tokenType;
        this.refresh = refresh;
        return this;
    }


    public void Save() {
        Gson gson = new Gson();

        String data = gson.toJson(this);
        try {
            System.out.println("1.");
            final String dir = System.getProperty("user.dir");
            File file = new File(dir+"/auth/token.json");
            System.out.println("2.");
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            System.out.println("3.");
            if (!file.exists())
                file.createNewFile();
            System.out.println("4.");
            Files.write(Paths.get(dir+"/auth/token.json"), data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("5.");
    }

    public Boolean Init() {
        Gson gson = new Gson();
        String data = null;

        try {
            final String dir = System.getProperty("user.dir");
            Path path = Paths.get(dir+"/auth/token.json");
            if (path != null) {
                byte[] bytes = Files.readAllBytes(path);
                if (bytes != null) {
                    data = new String(bytes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (data == null) {
            return false;
        }

        AuthToken token = gson.fromJson(data, AuthToken.class);
        if (token == null) {
            return false;
        }
        this.access = token.access;
        this.tokenType = token.tokenType;
        this.expire = token.expire;
        this.refresh = token.refresh;
        this.autoSave = token.autoSave;

        return this.access != null;
    }
}

