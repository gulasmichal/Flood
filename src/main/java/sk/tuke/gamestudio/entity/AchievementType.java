package sk.tuke.gamestudio.entity;

public enum AchievementType {
    FIRST_WIN("Prvá výhra",       "Vyhraj svoju prvú hru.",                              "🎮", "Herné",        0),
    HINTLESS("Bez nápovedy",      "Vyhraj hru bez použitia tlačidla nápovedy.",          "🧠", "Herné",        0),
    PERFECTIONIST("Perfekcionista","Vyhraj hru s použitím najviac 50 % povolených ťahov.","✨", "Herné",        0),
    SPEEDRUNNER("Rýchlostný bežec","Vyhraj hru za menej ako 60 sekúnd.",                 "⏱️", "Herné",        0),
    HARD_WINNER("Víťaz ťažkej hry","Vyhraj hru na ťažkej obtiažnosti.",                 "💪", "Herné",        0),
    VETERAN("Veterán",            "Vyhraj celkovo 10 hier.",                             "🎖️", "Herné",        0),
    DAILY_FIRST("Prvá denná výzva","Dokonči svoju prvú dennú výzvu.",                    "📅", "Denná výzva",  0),
    DAILY_10("Denný hrdina",      "Dokonči 10 denných výziev.",                         "🔥", "Denná výzva", 10),
    DAILY_50("Denný šampión",     "Dokonči 50 denných výziev.",                         "🏆", "Denná výzva", 50),
    DAILY_100("Denná legenda",    "Dokonči 100 denných výziev.",                        "👑", "Denná výzva", 100);

    private final String displayName;
    private final String description;
    private final String icon;
    private final String category;
    private final int target;

    AchievementType(String displayName, String description, String icon, String category, int target) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.category = category;
        this.target = target;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIcon()        { return icon; }
    public String getCategory()    { return category; }
    public int    getTarget()      { return target; }
}
