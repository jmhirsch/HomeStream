package enums;
/*
Defines a set of enums used to save preferences to a file.
 */
public enum Property {
    AUTO_LAUNCH_SERVER("auto_launch_server"), // bool
    LOCAL_PORT("local_port"), // int
    MOVIE_FOLDER("movie_folder"), // str
    PASSWORD("password"), // str
    REMOTE_ACCESS_ENABLED("remote_access_enabled"), // bool
    REMOTE_PORT("remote_port"), // int
    REQUIRE_AUTHENTICATION("require_authentication"), //bool
    START_ON_LOGIN("start_on_login"), // bool
    USE_DIFFERENT_REMOTE_PORT("use_different_remote_port"), // bool
    USERNAME("username"), // str
    AUTO_DELETE_CACHE("auto_delete_cache"), //str
    CACHE_AUTODELETE_SIZE("cache_autodelete_size");// int
    
    
    private final String tag;
    
     Property(String tag){
        this.tag = tag;
    }
    
    @Override
    public String toString(){
         return this.tag;
    }
}
