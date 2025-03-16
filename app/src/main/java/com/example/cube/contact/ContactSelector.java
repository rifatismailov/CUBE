package com.example.cube.contact;

public class ContactSelector {
    private ContactData contactData;
    private String contact;

    public ContactData getContactData() {
        return contactData;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(ContactData contactData) {
        this.contactData = contactData;
        this.contact = contactData.getId();
    }
    public void setContact(String contact) {
        this.contact = contact;
    }
}
