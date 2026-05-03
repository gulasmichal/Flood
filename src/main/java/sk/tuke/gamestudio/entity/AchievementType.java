package sk.tuke.gamestudio.entity;

public enum AchievementType {
    FIRST_WIN("Prvá výhra", "Vyhraj svoju prvú hru."),
    HINTLESS("Bez nápovedy", "Vyhraj hru bez použitia tlačidla nápovedy."),
    PERFECTIONIST("Perfekcionista", "Vyhraj hru s použitím najviac 50 % povolených ťahov."),
    SPEEDRUNNER("Rýchlostný bežec", "Vyhraj hru za menej ako 60 sekúnd."),
    HARD_WINNER("Víťaz ťažkej hry", "Vyhraj hru na ťažkej obtiažnosti."),
    VETERAN("Veterán", "Vyhraj celkovo 10 hier.");

    private final String displayName;
    private final String description;

    AchievementType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
