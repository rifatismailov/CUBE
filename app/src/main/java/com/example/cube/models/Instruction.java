package com.example.cube.models;

public class Instruction {
    int imageId;
    String equipment;
    String information;
   public Instruction (String equipment, String information){
       this.equipment=equipment;
       this.information=information;
   }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }
}
