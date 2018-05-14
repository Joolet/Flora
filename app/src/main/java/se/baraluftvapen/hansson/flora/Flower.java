/*
    Innehåller all information om en växt
    Innehåller både get- & setmetoder
*/
package se.baraluftvapen.hansson.flora;
import java.util.HashMap;
import java.util.Objects;

class Flower {
    private String name;                //namnet på blomman
    private String color;               //blommas färger
    private String otherName;           //alternativa namn
    private String areacode;            //landskap
    private String family;              //familj
    private String spread;              //utbredning
    private String length;              //höjd
    private String latin;               //latinska namnet
    private String category;            //kategorier
    private final String id;            //bild/växt referens, varje är unik och innehåller EJ åäö
    private String description;         //beskrvning
    private String bloomID;             //bloomningstid
    private String edited;              //visa redigerans namn
    private final HashMap<String, String> intToMonth = new HashMap<String, String>();

    //-----------------------------------------------------------------------------------------------------------------------------------------------------------
    //constructor
    public Flower(String name, String id, String color, String otherName, String areacode,
                  String length, String family, String spread, String bloomID, String category, String description, String latin, String edited) {
        this.name = name;
        this.otherName = otherName;
        this.length = length;
        this.color = color;
        this.category = category;
        this.id = id;
        this.description = description;
        this.areacode = areacode;
        this.family = family;
        this.spread = spread;
        this.bloomID = bloomID;
        this.latin = latin;
        this.edited = edited;

        //intToMonth är en Hashmap som kommer omvandla siffran för en månad till text
        intToMonth.put("1", "Januari");
        intToMonth.put("2", "Feburari");
        intToMonth.put("3", "Mars");
        intToMonth.put("4", "April");
        intToMonth.put("5", "Maj");
        intToMonth.put("6", "Juni");
        intToMonth.put("7", "Juli");
        intToMonth.put("8", "Augusti");
        intToMonth.put("9", "September");
        //används ej, skulle kunna ersätta med bokstav? då det används istället
        intToMonth.put("10", "Oktober");   
        intToMonth.put("11", "November");
        intToMonth.put("12", "December");
    }
 
    
    //------------------------------------------------------------------------------------------------------------------------------------------------------
    //Möjlihet att ersätta växtens nuvarande egenskaper
    //------------------------------------------------------------------------------------------------------------------------------------------------------

    public void setDescription(String replace) {
        description = replace;
    }
    public void setColor(String replace) {
        color = replace;
    }
    public void setOtherName(String replace) {
        otherName = replace;
    }
    public void setAreacode(String replace) {
        areacode = replace;
    }
    public void setFamily(String replace) {
        family = replace;
    }
    public void setSpread(String replace) {
        spread = replace;
    }
    public void setCategory(String replace) {
        category = replace;
    }
    public void setEdited(String replace) {
        edited = replace;
    }
    public void setLength(String replace) {
        length = replace;
    }
    public void setName(String replace) {
        name = replace;
    }
    public void setBloomID(String replace) {
        bloomID = replace;
    }
    public void setLatin(String replace){
        latin = replace;
    }
    
    //-------------------------------------------------------------------------------------------------------
    //Metoder som retunerar växtens egenskaper
    //OBS!
    //getToxic retunerar otherName
    public String getToxic() {
        return otherName;
    }
    public String getColor() {
        return color;
    }
    public String getLength() {
        return length;
    }
    public String getName() {
        return name;
    }
    public String getFamily() {
        return family;
    }
    public String getID() {
        return id;
    }
    public String getCategory() {
        return category;
    }
    public String getDescription() {
        return description;
    }
    public String getAreacode() {
        return areacode;
    }
    public String getEdited() {
        return edited;
    }
    public String getSpread() {
        return spread;
    }
    public String getBloomID() {
        return bloomID;
    }
    public String getLatin () {
        return latin;
    }
    //retunerar all info om blomma
    public String getAll () {
        return  name+"\t"+id+"\t"+color+"\t"+otherName+"\t"+areacode+"\t"+ length+"\t"+family+"\t"+spread+"\t"+bloomID+"\t"+category+"\t"+description+"\t"+latin+"\t"+edited;
    }
    /*Denna metod retunerar en sträng när blomning sker
    Omvandlar siffra till månad med formatet "StartMånad - SlutMånad"
    */
    public String getBloom() {
        //vissa växter saknar blomtid
        if (bloomID.equals("saknas")) {
            return "Ej angivit";
        } else {
            //blomtiden sepereras med mellanslag i databasen
            String[] blooming = bloomID.split(" ");
            //lagrar sista blommånaden
            String last = blooming[blooming.length - 1]; 
            //om blomning sker bara en månad
            if (blooming.length == 1){
                return intToMonth.get(blooming[0]);
            }
            //om blomning sker i flera månader
            //Fick eliminera talen 10 11 12, eftersom de talen innehåller två siffror
            else if (last.matches("[a-zA-Z ]")) {
                if (last.equals("A")) {
                    return intToMonth.get(blooming[0]) + " - Oktober";
                }
                if (last.equals("B")) {
                    return intToMonth.get(blooming[0]) + " - November";
                } else {
                    return intToMonth.get(blooming[0]) + " - December";
                }
            }
            //normalfallet, sträcker sig flera månader utan undantag eller att data råkas fattas
            else {
                return intToMonth.get(blooming[0]) + " - " + intToMonth.get(last);
            }
        }
    }
}
